package com.orientation.orientationapp.modules.user.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orientation.orientationapp.modules.user.dto.AcademicProfileRequest;
import com.orientation.orientationapp.modules.user.entity.Candidate;
import com.orientation.orientationapp.modules.user.repository.CandidateRepository;
import com.orientation.orientationapp.modules.orientation.repository.*;
import com.orientation.orientationapp.modules.university.repository.CountryRepository;
import com.orientation.orientationapp.recengine.profile.model.AcademicProfile;
import com.orientation.orientationapp.recengine.profile.normalization.GradeNormalizer;
import com.orientation.orientationapp.security.service.ResourceAccess;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.time.Instant;
import java.util.*;

@Service @RequiredArgsConstructor @Transactional
public class CandidateAcademicService {
    private final ResourceAccess access;
    private final CandidateRepository candidates;
    private final GradeScaleRepository scales;
    private final AcademicYearRepository years;
    private final CountryRepository countries;
    private final GradeNormalizer normalizer;
    private final ObjectMapper json;
    private final com.orientation.orientationapp.modules.transcript.service.TranscriptAnalysisService transcriptAnalyses;
    private final SubjectRepository subjects;

    public record View(UUID candidateId, Long version, String source, boolean officiallyVerified,
                       Instant confirmedAt, AcademicProfileRequest data) {}

    @Transactional(readOnly=true) public View read(UUID id) {
        var c=access.candidate(id);return view(c);
    }
    public View save(UUID id, AcademicProfileRequest request) {
        Candidate c=access.candidate(id);
        if (!c.getUser().getId().equals(access.current().getId())) access.require("CANDIDATE_MANAGE");
        if (!Objects.equals(c.getVersion(),request.version()))
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Profile changed; reload its version");
        if (request.countryId()!=null) c.setCountry(countries.findById(request.countryId())
                .orElseThrow(()->invalid("Unknown country")));
        else c.setCountry(null);
        if (request.academicYearId()!=null && !years.existsById(request.academicYearId()))
            throw invalid("Unknown admission year");
        if (request.gradesAcademicYearId()!=null && !years.existsById(request.gradesAcademicYearId()))
            throw invalid("Unknown grades year");
        var scale=request.gradeScaleId()==null?null:scales.findById(request.gradeScaleId())
                .orElseThrow(()->invalid("Unknown grade scale"));
        if (scale!=null && (!normalizer.valid(scale) || request.countryId()==null ||
                !scale.getCountry().getId().equals(request.countryId()) ||
                !scale.getAcademicYear().getId().equals(request.gradesAcademicYearId())))
            throw invalid("Grade scale does not match country and grades year");
        if (request.generalAverage()!=null && normalizer.convert(request.generalAverage(),scale,scale).isEmpty())
            throw invalid("An in-range average and an explicit active grade scale are required");
        if (request.subjectGrades()!=null) for(var value:request.subjectGrades().values())
            if(normalizer.convert(value,scale,scale).isEmpty()) throw invalid("Subject grade is outside the configured scale");
        if (request.budget()!=null && request.budgetCurrency()==null)
            throw invalid("Budget currency is required");
        if(request.nationalityCountryId()!=null && !countries.existsById(request.nationalityCountryId()))
            throw invalid("Unknown nationality country");
        if(request.householdIncome()!=null && (request.incomeCurrency()==null||request.incomePeriod()==null))
            throw invalid("Financial resources require an explicit currency and period");
        c.setHighSchool(request.highSchool());c.setDateOfBirth(request.dateOfBirth());
        c.setBacYear(request.bacYear());c.setBacType(request.bacType());
        // No write to Candidate.verified: owner confirmation is not certification.
        if(request.transcriptAnalysisId()!=null) {
            var analysis=transcriptAnalyses.confirmed(request.transcriptAnalysisId(),id);
            var review=transcriptAnalyses.review(analysis);
            if(!Objects.equals(review.gradeScaleId(),request.gradeScaleId()) ||
                    !Objects.equals(analysis.getTranscript().getAcademicYear().getId(),request.gradesAcademicYearId()))
                throw invalid("Selected transcript does not match profile scale and grades year");
        }
        c.setAcademicData(encode(request));
        c.setAcademicConfirmedAt(request.confirmed()?Instant.now():null);
        candidates.saveAndFlush(c);
        return view(c);
    }
    @Transactional(readOnly=true) public AcademicProfile analyze(UUID id) {
        var c=access.candidate(id);
        var p=AcademicProfile.builder().candidateId(c.getId()).tenantId(c.getTenant().getId())
                .firstName(c.getFirstName()).lastName(c.getLastName()).build();
        var data=decode(c);
        if(data==null || c.getAcademicConfirmedAt()==null) return p;
        p.setAcademicYearId(data.academicYearId());p.setGradesAcademicYearId(data.gradesAcademicYearId());
        p.setGradeScale(data.gradeScaleId()==null?null:scales.findById(data.gradeScaleId()).orElse(null));
        p.setCountry(c.getCountry()==null?null:c.getCountry().getId().toString());
        p.setNationality(data.nationalityCountryId()==null?null:data.nationalityCountryId().toString());
        p.setHouseholdIncome(data.householdIncome());p.setIncomeCurrency(data.incomeCurrency());p.setIncomePeriod(data.incomePeriod());
        p.setBacType(data.bacType()==null?null:data.bacType().name());p.setBacYear(data.bacYear());
        p.setBacSeries(data.bacSeries());p.setDiploma(data.diploma());p.setEducationLevel(data.educationLevel());
        p.setGeneralAverage(data.generalAverage());p.setSubjectGrades(data.subjectGrades());
        p.setPreferredFields(data.interests());p.setPreferredCountries(data.preferredCountries());
        p.setPreferredLanguages(data.preferredLanguages());p.setLanguage(data.language());p.setBudget(data.budget());
        if(data.transcriptAnalysisId()!=null) {
            var analysis=transcriptAnalyses.confirmed(data.transcriptAnalysisId(),id);
            var review=transcriptAnalyses.review(analysis);
            p.setGeneralAverage(review.average());
            p.setGradeScale(scales.findById(review.gradeScaleId()).orElse(null));
            p.setGradesAcademicYearId(analysis.getTranscript().getAcademicYear().getId());
            var marks=new LinkedHashMap<String,java.math.BigDecimal>();
            for(var mark:review.marks()) if(mark.subjectId()!=null && mark.value()!=null) {
                var subject=subjects.findById(mark.subjectId()).orElse(null);
                if(subject!=null && subject.getCode()!=null) marks.put(subject.getCode(),mark.value());
            }
            p.setSubjectGrades(marks);
        }
        return p;
    }
    private View view(Candidate c) {return new View(c.getId(),c.getVersion(),"SELF_DECLARED",
            Boolean.TRUE.equals(c.getVerified()),c.getAcademicConfirmedAt(),decode(c));}
    private AcademicProfileRequest decode(Candidate c) {
        if(c.getAcademicData()==null)return null;
        try{return json.readValue(c.getAcademicData(),AcademicProfileRequest.class);}
        catch(java.io.IOException ex){throw new IllegalStateException("Unreadable persisted academic profile",ex);}
    }
    private String encode(Object value) {
        try{return json.writeValueAsString(value);}
        catch(com.fasterxml.jackson.core.JsonProcessingException ex){throw new IllegalStateException(ex);}
    }
    private ResponseStatusException invalid(String text){return new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,text);}
}
