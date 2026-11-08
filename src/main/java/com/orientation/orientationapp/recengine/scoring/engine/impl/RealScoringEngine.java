package com.orientation.orientationapp.recengine.scoring.engine.impl;

import com.orientation.orientationapp.modules.orientation.repository.AdmissionCriterionRepository;
import com.orientation.orientationapp.modules.university.repository.ProgramRepository;
import com.orientation.orientationapp.recengine.profile.model.AcademicProfile;
import com.orientation.orientationapp.recengine.scoring.model.CompositeScore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service @RequiredArgsConstructor @Transactional(readOnly=true)
public class RealScoringEngine {
    private final AdmissionCriterionRepository admissionCriterionRepository;
    private final ProgramRepository programRepository;
    private final DefaultScoringEngine scoring;
    public CompositeScore computeScore(AcademicProfile profile,UUID programId,UUID guideVersionId) {
        var program=programRepository.findById(programId).orElseThrow();
        var data=new HashMap<String,Object>();
        data.put("studyField",program.getStudyField());data.put("language",program.getLanguage());
        data.put("country",program.getFaculty().getCampus().getUniversity().getCountry().getId().toString());
        if(guideVersionId!=null)data.put("subjectCodes",admissionCriterionRepository
            .findByGuideVersionIdAndProgramId(guideVersionId,programId).stream()
            .filter(r->r.getSubject()!=null && r.getSubject().getCode()!=null)
            .map(r->r.getSubject().getCode()).distinct().toList());
        return scoring.computeScore(profile,data);
    }
}
