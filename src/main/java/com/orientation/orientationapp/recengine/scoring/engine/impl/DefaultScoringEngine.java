package com.orientation.orientationapp.recengine.scoring.engine.impl;

import com.orientation.orientationapp.recengine.profile.model.AcademicProfile;
import com.orientation.orientationapp.recengine.scoring.model.*;
import com.orientation.orientationapp.recengine.profile.normalization.GradeNormalizer;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.*;
import java.math.*;
import java.util.*;

/** Deterministic compatibility. Missing dimensions remain uncomputed. */
@Component
public class DefaultScoringEngine {
    private final Map<String,BigDecimal> weights;
    public DefaultScoringEngine(){this(new BigDecimal(".50"),new BigDecimal(".20"),new BigDecimal(".15"),new BigDecimal(".10"),new BigDecimal(".05"));}
    @Autowired public DefaultScoringEngine(
        @Value("${app.recommendations.weights.academic:0.50}") BigDecimal academic,
        @Value("${app.recommendations.weights.subject:0.20}") BigDecimal subject,
        @Value("${app.recommendations.weights.preference:0.15}") BigDecimal preference,
        @Value("${app.recommendations.weights.country:0.10}") BigDecimal country,
        @Value("${app.recommendations.weights.language:0.05}") BigDecimal language) {
        var configured=new LinkedHashMap<String,BigDecimal>();
        configured.put("academic",academic);configured.put("subject",subject);configured.put("preference",preference);
        configured.put("country",country);configured.put("language",language);
        if(configured.values().stream().anyMatch(w->w==null||w.signum()<0) ||
           configured.values().stream().reduce(BigDecimal.ZERO,BigDecimal::add).signum()==0)
            throw new IllegalArgumentException("Scoring weights must be nonnegative with a positive total");
        weights=Collections.unmodifiableMap(configured);
    }
    public CompositeScore computeScore(AcademicProfile profile,Map<String,Object> programme) {
        var parts=new ArrayList<ScoreDetail>();
        add(parts,"academic",percentage(profile.getGeneralAverage(),profile.getGradeScale()),"Recorded average / configured scale");
        BigDecimal subjects=null;
        var relevant=programme.get("subjectCodes");
        if(relevant instanceof Collection<?> codes && !codes.isEmpty() && profile.getSubjectGrades()!=null) {
            var values=new ArrayList<BigDecimal>();boolean complete=true;
            for(var code:codes) {
                var scale=profile.getSubjectGradeScales()==null?profile.getGradeScale():profile.getSubjectGradeScales().get(code);
                var value=percentage(profile.getSubjectGrades().get(code),scale);
                if(value==null){complete=false;break;}values.add(value);
            }
            if(complete)subjects=values.stream().reduce(BigDecimal.ZERO,BigDecimal::add)
                    .divide(BigDecimal.valueOf(values.size()),MathContext.DECIMAL128);
        }
        add(parts,"subject",subjects,"Only subjects explicitly referenced by published criteria");
        add(parts,"preference",match(profile.getPreferredFields(),programme.get("studyField")),"Exact declared field preference");
        add(parts,"country",match(profile.getPreferredCountries(),programme.get("country")),"Exact declared destination preference");
        add(parts,"language",match(profile.getPreferredLanguages()!=null?profile.getPreferredLanguages():
                profile.getLanguage()==null?null:List.of(profile.getLanguage()),programme.get("language")),"Exact declared language preference");
        var known=parts.stream().filter(ScoreDetail::isComputed).toList();
        var denominator=known.stream().map(ScoreDetail::getWeight).reduce(BigDecimal.ZERO,BigDecimal::add);
        var total=weights.values().stream().reduce(BigDecimal.ZERO,BigDecimal::add);
        BigDecimal score=denominator.signum()==0?null:known.stream().map(p->p.getScore().multiply(p.getWeight()))
                .reduce(BigDecimal.ZERO,BigDecimal::add).divide(denominator,MathContext.DECIMAL128);
        return CompositeScore.builder().finalScore(score)
                .confidence(denominator.multiply(BigDecimal.valueOf(100)).divide(total,MathContext.DECIMAL128))
                .components(parts).summary("Compatibility; confidence is data coverage, never admission probability").build();
    }
    private void add(List<ScoreDetail> parts,String type,BigDecimal value,String explanation) {
        parts.add(ScoreDetail.builder().scoreType(type).score(value).weight(weights.get(type)).computed(value!=null)
                .explanation(value==null?"Not computed: missing reliable data":explanation).build());
    }
    private BigDecimal match(List<String> preferences,Object actual) {
        if(preferences==null||preferences.isEmpty()||!(actual instanceof String value)||value.isBlank())return null;
        return preferences.stream().anyMatch(p->p.equalsIgnoreCase(value))?BigDecimal.valueOf(100):BigDecimal.ZERO;
    }
    private BigDecimal percentage(BigDecimal value,com.orientation.orientationapp.modules.orientation.entity.GradeScale scale) {
        var normalizer=new GradeNormalizer();
        if(normalizer.convert(value,scale,scale).isEmpty())return null;
        return value.subtract(scale.getMinScore()).multiply(BigDecimal.valueOf(100))
                .divide(scale.getMaxScore().subtract(scale.getMinScore()),MathContext.DECIMAL128);
    }
}
