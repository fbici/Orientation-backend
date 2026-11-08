package com.orientation.orientationapp.recengine.profile.analyzer.impl;

import com.orientation.orientationapp.recengine.profile.analyzer.ProfileAnalyzer;
import com.orientation.orientationapp.recengine.profile.model.AcademicProfile;
import com.orientation.orientationapp.modules.orientation.entity.GradeScale;
import com.orientation.orientationapp.recengine.profile.normalization.GradeNormalizer;
import com.orientation.orientationapp.modules.user.service.CandidateAcademicService;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import java.math.*;
import java.util.*;

@Component
public class DefaultProfileAnalyzer implements ProfileAnalyzer {
    private CandidateAcademicService academic;
    public DefaultProfileAnalyzer() {}
    @Autowired public DefaultProfileAnalyzer(CandidateAcademicService academic){this.academic=academic;}
    @Override public AcademicProfile analyze(UUID candidateId) {
        if(academic==null)throw new IllegalStateException("Persistent academic profile service is required");
        return academic.analyze(candidateId);
    }
    /** Legacy unscaled input cannot yield normalized values or an inferred overall average. */
    @Override public AcademicProfile analyze(String bacType,BigDecimal average,Map<String,BigDecimal> grades) {
        return AcademicProfile.builder().bacType(bacType).bacAverage(average)
                .subjectGrades(grades==null?null:Collections.unmodifiableMap(new LinkedHashMap<>(grades))).build();
    }
    /** Explicit-scale adapter for pure calculations; production loads persisted, confirmed inputs. */
    public AcademicProfile analyze(String bacType,BigDecimal average,Map<String,BigDecimal> grades,GradeScale scale) {
        var profile=analyze(bacType,average,grades);
        var normalizer=new GradeNormalizer();profile.setGradeScale(scale);
        if(normalizer.convert(average,scale,scale).isPresent()) {
            profile.setGeneralAverage(average);
            profile.setNormalizedScore(percent(average,scale));
        }
        var strong=new ArrayList<String>();var weak=new ArrayList<String>();
        if(grades!=null)for(var entry:new TreeMap<>(grades).entrySet()) {
            if(normalizer.convert(entry.getValue(),scale,scale).isEmpty())continue;
            var percentage=percent(entry.getValue(),scale);
            if(percentage.compareTo(new BigDecimal("75"))>=0)strong.add(entry.getKey());
            if(percentage.compareTo(new BigDecimal("50"))<0)weak.add(entry.getKey());
        }
        profile.setStrongSubjects(strong);profile.setWeakSubjects(weak);
        // No competency model is configured: missing competencies must not become zero scores.
        return profile;
    }
    private BigDecimal percent(BigDecimal value,GradeScale scale) {
        return value.subtract(scale.getMinScore()).multiply(new BigDecimal("100"))
            .divide(scale.getMaxScore().subtract(scale.getMinScore()),MathContext.DECIMAL128);
    }
}
