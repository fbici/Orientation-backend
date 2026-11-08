package com.orientation.orientationapp.recengine.ranking.engine.impl;
import com.orientation.orientationapp.recengine.ranking.model.RankedRecommendation;
import com.orientation.orientationapp.recengine.scoring.model.CompositeScore;
import com.orientation.orientationapp.recengine.explanation.model.Explanation;
import com.orientation.orientationapp.recengine.eligibility.model.*;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class DefaultRankingEngine {
    public List<RankedRecommendation> rank(List<CompositeScore> scores,List<Map<String,Object>> programmes) {
        if(scores.size()!=programmes.size())throw new IllegalArgumentException("One score per programme is required");
        var ranked=new ArrayList<RankedRecommendation>();
        for(int i=0;i<scores.size();i++) {
            var p=programmes.get(i);var score=scores.get(i);
            if(p.get("id")==null)throw new IllegalArgumentException("A persisted programme id is required");
            var id=p.get("id") instanceof UUID uuid?uuid:UUID.fromString(p.get("id").toString());
            var eligibility=p.get("eligibility") instanceof EligibilityStatus state?state:EligibilityStatus.INSUFFICIENT_DATA;
            ranked.add(RankedRecommendation.builder().programId(id).programName((String)p.get("name"))
                .universityName((String)p.get("university")).facultyName((String)p.get("faculty"))
                .score(score).confidence(score.getConfidence()).eligibilityStatus(eligibility).eligible(eligibility==EligibilityStatus.ELIGIBLE).build());
        }
        ranked.sort(Comparator.comparingInt((RankedRecommendation r)->r.getEligibilityStatus()==EligibilityStatus.ELIGIBLE?0:
                r.getEligibilityStatus()==EligibilityStatus.INSUFFICIENT_DATA?1:2)
                .thenComparing((RankedRecommendation r)->r.getScore().getFinalScore(),
                Comparator.nullsLast(Comparator.reverseOrder())).thenComparing(r->r.getProgramId().toString()));
        for(int i=0;i<ranked.size();i++)ranked.get(i).setRank(i+1);
        return ranked;
    }
    public List<RankedRecommendation> rankWithExplanations(List<CompositeScore> scores,List<Map<String,Object>> programmes) {
        var ranked=rank(scores,programmes);
        for(var r:ranked)r.setExplanation(Explanation.builder().summary(r.getScore().getSummary())
            .strengths(List.of()).criteria(List.of()).warnings(List.of())
            .recommendation("Consult published eligibility conditions; no admission guarantee").build());
        return ranked;
    }
}
