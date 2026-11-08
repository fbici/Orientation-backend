package com.orientation.orientationapp.modules.transcript.service;

import com.fasterxml.jackson.databind.*;
import com.orientation.orientationapp.modules.transcript.entity.*;
import com.orientation.orientationapp.modules.transcript.repository.*;
import com.orientation.orientationapp.modules.transcript.dto.TranscriptReview;
import com.orientation.orientationapp.modules.orientation.repository.*;
import com.orientation.orientationapp.modules.orientation.entity.*;
import com.orientation.orientationapp.docintel.ocr.engine.impl.PdfImageOcrEngine;
import com.orientation.orientationapp.storage.service.StorageService;
import com.orientation.orientationapp.security.service.ResourceAccess;
import com.orientation.orientationapp.recengine.profile.normalization.GradeNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.*;
import java.util.*;
import java.time.Instant;
import java.math.BigDecimal;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.regex.*;

@Service @RequiredArgsConstructor @Transactional
public class TranscriptAnalysisService {
    private final TranscriptAccessService accessService;
    private final TranscriptAnalysisRepository analyses;
    private final GradeScaleRepository scales;
    private final SubjectRepository subjects;
    private final ResourceAccess access;
    private final StorageService storage;
    private final PdfImageOcrEngine ocr;
    private final GradeNormalizer normalizer;
    private final ObjectMapper json;

    public record View(UUID id,UUID transcriptId,UUID parentAnalysisId,Instant analyzedAt,Instant confirmedAt,
        String sourceChecksum,String status,boolean officiallyVerified,JsonNode data) {}
    public record Analyze(@jakarta.validation.constraints.NotNull UUID gradeScaleId,
        @jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(max=100) String period) {}

    public View analyze(UUID transcriptId,Analyze request) {
        var t=accessService.get(transcriptId);write(t);
        var scale=scale(t,request.gradeScaleId());
        try {
            var path=storage.load(t.getFileUrl());var bytes=Files.readAllBytes(path);
            String mime=bytes.length>=5&&new String(bytes,0,5,java.nio.charset.StandardCharsets.US_ASCII).equals("%PDF-")
                    ?"application/pdf":bytes.length>2&&bytes[0]==(byte)0xff&&bytes[1]==(byte)0xd8?"image/jpeg":"image/png";
            var extraction=ocr.performOcr(new java.io.ByteArrayInputStream(bytes),mime);
            var available=subjects.findByGradeScaleId(scale.getId()).stream().filter(s->Boolean.TRUE.equals(s.getActive())).toList();
            var marks=new ArrayList<TranscriptReview.Mark>();BigDecimal average=null;
            var pattern=Pattern.compile("^\\s*([^:;]{1,200})\\s*:\\s*([^;]{1,30})(?:;\\s*coef\\s*:\\s*([0-9.,]+))?\\s*$",Pattern.CASE_INSENSITIVE);
            for(var page:extraction.getPages()) for(var line:page.getText().split("\\R")) {
                var match=pattern.matcher(line);if(!match.matches())continue;
                String name=match.group(1).trim(),raw=match.group(2).trim();
                var pieces=raw.split("/");BigDecimal value=null;
                try {
                    // Without an explicit matching denominator the number remains unresolved.
                    if(pieces.length==2 && new BigDecimal(pieces[1].trim().replace(',','.')).compareTo(scale.getMaxScore())==0) {
                        var parsed=new BigDecimal(pieces[0].trim().replace(',','.'));
                        if(normalizer.convert(parsed,scale,scale).isPresent())value=parsed;
                    }
                } catch(NumberFormatException ignored) {}
                if(value==null && pieces.length==1)value=normalizer.convertLabel(raw,scale,scale).orElse(null);
                if(name.equalsIgnoreCase("MOYENNE")||name.equalsIgnoreCase("AVERAGE")){average=value;continue;}
                var found=available.stream().filter(s->name.equalsIgnoreCase(s.getCode())||name.equalsIgnoreCase(s.getName())).toList();
                BigDecimal coefficient=null;
                try{if(match.group(3)!=null)coefficient=new BigDecimal(match.group(3).replace(',','.'));}catch(NumberFormatException ignored){}
                marks.add(new TranscriptReview.Mark(name,found.size()==1?found.getFirst().getId():null,raw,value,coefficient,page.getPageNumber(),line));
            }
            var review=new TranscriptReview(scale.getId(),request.period(),average,marks);
            var payload=new LinkedHashMap<String,Object>();payload.put("review",review);
            payload.put("pages",extraction.getPages());payload.put("engine",extraction.getEngineUsed());
            payload.put("confidence",extraction.getConfidence());payload.put("action","EXTRACTED");
            var checksum=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
            return view(append(t,null,payload,checksum,false));
        } catch(java.io.IOException|java.security.NoSuchAlgorithmException ex) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,"Transcript could not be read",ex);
        }
    }
    public View correct(UUID transcriptId,UUID analysisId,TranscriptReview review) {
        var original=getEntity(transcriptId,analysisId);write(original.getTranscript());
        if(original.getConfirmedAt()!=null)throw invalid("Confirmed analysis is immutable; reanalyze to create a new draft");
        validate(original.getTranscript(),review);
        provenance(original,review);
        var payload=decode(original.getReviewPayload()).deepCopy();
        ((com.fasterxml.jackson.databind.node.ObjectNode)payload).set("review",json.valueToTree(review));
        ((com.fasterxml.jackson.databind.node.ObjectNode)payload).put("action","CORRECTED");
        return view(append(original.getTranscript(),original,payload,original.getSourceChecksum(),false));
    }
    public View confirm(UUID transcriptId,UUID analysisId) {
        var original=getEntity(transcriptId,analysisId);write(original.getTranscript());
        original=analyses.lockById(analysisId).orElseThrow(ResourceAccess::denied);
        if(original.getConfirmedAt()!=null)return view(original);
        var existing=analyses.findByParentAnalysisIdAndConfirmedAtIsNotNull(analysisId);
        if(existing.isPresent())return view(existing.get());
        var payload=decode(original.getReviewPayload());
        var review=json.convertValue(payload.get("review"),TranscriptReview.class);
        validate(original.getTranscript(),review);
        provenance(original,review);
        ((com.fasterxml.jackson.databind.node.ObjectNode)payload).put("action","SELF_CONFIRMED");
        return view(append(original.getTranscript(),original,payload,original.getSourceChecksum(),true));
    }
    @Transactional(readOnly=true) public Page<View> history(UUID transcriptId,int page,int size) {
        accessService.get(transcriptId);
        return analyses.findByTranscriptIdOrderByAnalyzedAtDesc(transcriptId,
                PageRequest.of(Math.max(0,page),Math.max(1,Math.min(100,size)))).map(this::view);
    }
    @Transactional(readOnly=true) public TranscriptAnalysis confirmed(UUID id,UUID candidateId) {
        var analysis=analyses.findById(id).orElseThrow(ResourceAccess::denied);
        access.candidate(analysis.getTranscript().getCandidate().getId());
        if(!analysis.getTranscript().getCandidate().getId().equals(candidateId))throw ResourceAccess.denied();
        if(analysis.getConfirmedAt()==null)throw invalid("Transcript analysis must be personally confirmed");
        return analysis;
    }
    public TranscriptReview review(TranscriptAnalysis analysis) {
        return json.convertValue(decode(analysis.getReviewPayload()).get("review"),TranscriptReview.class);
    }
    private void provenance(TranscriptAnalysis original,TranscriptReview review) {
        var pages=decode(original.getReviewPayload()).path("pages");
        for(var mark:review.marks()) {
            boolean found=false;
            for(var page:pages) if(page.path("pageNumber").asInt()==mark.page() &&
                    !mark.passage().isBlank() && page.path("text").asText().contains(mark.passage()))found=true;
            if(!found)throw invalid("Each mark must retain an exact passage from its original page");
        }
    }
    private void validate(Transcript t,TranscriptReview review) {
        var scale=scale(t,review.gradeScaleId());
        if(review.average()!=null&&normalizer.convert(review.average(),scale,scale).isEmpty())throw invalid("Average outside scale");
        var seen=new HashSet<UUID>();
        for(var mark:review.marks()) {
            if(mark.value()!=null&&normalizer.convert(mark.value(),scale,scale).isEmpty())throw invalid("Mark outside scale");
            if(mark.subjectId()!=null) {
                var subject=subjects.findById(mark.subjectId()).orElseThrow(()->invalid("Unknown subject"));
                if(!Boolean.TRUE.equals(subject.getActive())||!subject.getGradeScale().getId().equals(scale.getId()))
                    throw invalid("Subject belongs to another scale");
                if(!seen.add(subject.getId()))throw invalid("Duplicate subject; split different periods into separate analyses");
            }
            // Null subject/value/coefficient remains unknown and can never become an invented grade.
        }
    }
    private GradeScale scale(Transcript t,UUID id) {
        var s=scales.findById(id).orElseThrow(()->invalid("Unknown grade scale"));
        if(!normalizer.valid(s)||!s.getAcademicYear().getId().equals(t.getAcademicYear().getId())||
                t.getCandidate().getCountry()==null||!s.getCountry().getId().equals(t.getCandidate().getCountry().getId()))
            throw invalid("Scale must match the candidate country and transcript year");
        return s;
    }
    private TranscriptAnalysis append(Transcript t,TranscriptAnalysis parent,Object data,String checksum,boolean confirmed) {
        var analysis=TranscriptAnalysis.builder().transcript(t).analysisVersion("F3.1")
                .analyzedAt(Instant.now()).parentAnalysisId(parent==null?null:parent.getId())
                .sourceChecksum(checksum).reviewPayload(encode(data)).confirmedAt(confirmed?Instant.now():null).build();
        return analyses.saveAndFlush(analysis);
    }
    private TranscriptAnalysis getEntity(UUID transcriptId,UUID analysisId) {
        accessService.get(transcriptId);
        return analyses.findById(analysisId).filter(a->a.getTranscript().getId().equals(transcriptId))
            .filter(a->a.getReviewPayload()!=null).orElseThrow(ResourceAccess::denied);
    }
    private void write(Transcript t) {
        var c=t.getCandidate();if(c.getUser()==null||!c.getUser().getId().equals(access.current().getId()))access.require("CANDIDATE_MANAGE");
    }
    private View view(TranscriptAnalysis a) {return new View(a.getId(),a.getTranscript().getId(),a.getParentAnalysisId(),
        a.getAnalyzedAt(),a.getConfirmedAt(),a.getSourceChecksum(),a.getConfirmedAt()==null?"DRAFT":"SELF_CONFIRMED",false,
        a.getReviewPayload()==null?null:decode(a.getReviewPayload()));}
    private JsonNode decode(String value){try{return json.readTree(value);}catch(Exception e){throw new IllegalStateException(e);}}
    private String encode(Object value){try{return json.writeValueAsString(value);}catch(Exception e){throw new IllegalStateException(e);}}
    private ResponseStatusException invalid(String value){return new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,value);}
}
