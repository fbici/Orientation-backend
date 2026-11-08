package com.orientation.orientationapp.recengine.profile.normalization;

import com.orientation.orientationapp.modules.orientation.entity.GradeScale;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** Explicit numeric scales only. No guessed denominator or display rounding. */
@Component
public class GradeNormalizer {
    public Optional<BigDecimal> convert(BigDecimal raw, GradeScale source, GradeScale target) {
        if (!valid(source) || !valid(target) || raw == null ||
                source.getNormalizeTo().compareTo(target.getNormalizeTo()) != 0 ||
                raw.compareTo(source.getMinScore()) < 0 || raw.compareTo(source.getMaxScore()) > 0)
            return Optional.empty();
        return Optional.of(raw.subtract(source.getMinScore())
                .multiply(target.getMaxScore().subtract(target.getMinScore()))
                .divide(source.getMaxScore().subtract(source.getMinScore()), MathContext.DECIMAL128)
                .add(target.getMinScore()));
    }

    /** Cross multiplication preserves threshold boundaries, including recurring ratios. */
    public Optional<Integer> compare(BigDecimal raw, GradeScale source, BigDecimal threshold, GradeScale target) {
        if (convert(raw, source, target).isEmpty() || threshold == null ||
                threshold.compareTo(target.getMinScore()) < 0 || threshold.compareTo(target.getMaxScore()) > 0)
            return Optional.empty();
        return Optional.of(raw.subtract(source.getMinScore())
                .multiply(target.getMaxScore().subtract(target.getMinScore()))
                .compareTo(threshold.subtract(target.getMinScore())
                        .multiply(source.getMaxScore().subtract(source.getMinScore()))));
    }

    public Optional<BigDecimal> convertLabel(String raw,GradeScale source,GradeScale target) {
        if(raw==null || !valid(source) || !valid(target) ||
                source.getNormalizeTo().compareTo(target.getNormalizeTo())!=0 || source.getItems()==null)
            return Optional.empty();
        var mappings=source.getItems().stream().filter(i->!Boolean.TRUE.equals(i.getDeleted()))
            .filter(i->raw.equals(i.getRawGrade())).toList();
        if(mappings.size()!=1)return Optional.empty();
        var normalized=mappings.getFirst().getNormalizedValue();
        if(normalized==null || normalized.signum()<0 || normalized.compareTo(source.getNormalizeTo())>0)
            return Optional.empty();
        return Optional.of(normalized.multiply(target.getMaxScore().subtract(target.getMinScore()))
            .divide(source.getNormalizeTo(),MathContext.DECIMAL128).add(target.getMinScore()));
    }
    public boolean valid(GradeScale scale) {
        return scale != null && Boolean.TRUE.equals(scale.getActive()) && !Boolean.TRUE.equals(scale.getDeleted()) &&
                scale.getCountry() != null && scale.getAcademicYear() != null &&
                scale.getMinScore() != null && scale.getMaxScore() != null &&
                scale.getMaxScore().compareTo(scale.getMinScore()) > 0 &&
                scale.getNormalizeTo() != null && scale.getNormalizeTo().signum() > 0;
    }
}
