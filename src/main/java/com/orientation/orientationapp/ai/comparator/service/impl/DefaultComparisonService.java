package com.orientation.orientationapp.ai.comparator.service.impl;

import com.orientation.orientationapp.ai.comparator.model.ComparisonResult;
import com.orientation.orientationapp.ai.comparator.service.ComparisonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class DefaultComparisonService implements ComparisonService {

    @Override
    public ComparisonResult comparePrograms(UUID programIdA, UUID programIdB) {
        log.info("Comparing programs: {} vs {}", programIdA, programIdB);

        ComparisonResult.ComparisonItem itemA = ComparisonResult.ComparisonItem.builder()
                .name("Programme A")
                .type("LICENSE")
                .score(78.5)
                .attributes(Map.of("duration", "3 ans", "language", "Français", "tuition", "5000 MAD"))
                .build();

        ComparisonResult.ComparisonItem itemB = ComparisonResult.ComparisonItem.builder()
                .name("Programme B")
                .type("LICENSE")
                .score(72.3)
                .attributes(Map.of("duration", "3 ans", "language", "Français", "tuition", "4500 MAD"))
                .build();

        List<ComparisonResult.ComparisonFeature> features = List.of(
                ComparisonResult.ComparisonFeature.builder()
                        .feature("Score global")
                        .valueA("78.5")
                        .valueB("72.3")
                        .winner("A")
                        .explanation("Programme A a un score plus élevé")
                        .build(),
                ComparisonResult.ComparisonFeature.builder()
                        .feature("Coût")
                        .valueA("5000 MAD")
                        .valueB("4500 MAD")
                        .winner("B")
                        .explanation("Programme B est moins cher")
                        .build()
        );

        return ComparisonResult.builder()
                .itemA(itemA)
                .itemB(itemB)
                .features(features)
                .summary("Programme A est légèrement meilleur en score mais plus cher")
                .recommendation("Choisissez selon vos priorités : score vs coût")
                .build();
    }

    @Override
    public ComparisonResult compareUniversities(UUID universityIdA, UUID universityIdB) {
        log.info("Comparing universities: {} vs {}", universityIdA, universityIdB);
        return comparePrograms(universityIdA, universityIdB);
    }
}
