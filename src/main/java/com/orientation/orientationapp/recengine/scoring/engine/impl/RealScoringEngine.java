package com.orientation.orientationapp.recengine.scoring.engine.impl;

import com.orientation.orientationapp.modules.orientation.entity.AdmissionCriterion;
import com.orientation.orientationapp.modules.orientation.repository.AdmissionCriterionRepository;
import com.orientation.orientationapp.modules.scholarship.entity.Scholarship;
import com.orientation.orientationapp.modules.scholarship.repository.ScholarshipRepository;
import com.orientation.orientationapp.modules.university.entity.Program;
import com.orientation.orientationapp.modules.university.entity.University;
import com.orientation.orientationapp.modules.university.repository.ProgramRepository;
import com.orientation.orientationapp.recengine.profile.model.AcademicProfile;
import com.orientation.orientationapp.recengine.scoring.model.CompositeScore;
import com.orientation.orientationapp.recengine.scoring.model.ScoreDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RealScoringEngine {

    private final AdmissionCriterionRepository admissionCriterionRepository;
    private final ProgramRepository programRepository;
    private final ScholarshipRepository scholarshipRepository;

    private static final Map<String, BigDecimal> WEIGHTS = Map.of(
            "academic", BigDecimal.valueOf(0.30),
            "subject", BigDecimal.valueOf(0.20),
            "scholarship", BigDecimal.valueOf(0.10),
            "competency", BigDecimal.valueOf(0.15),
            "preference", BigDecimal.valueOf(0.10),
            "country", BigDecimal.valueOf(0.05),
            "language", BigDecimal.valueOf(0.05),
            "historical", BigDecimal.valueOf(0.05)
    );

    public CompositeScore computeScore(AcademicProfile profile, UUID programId, UUID guideVersionId) {
        log.info("Computing score for program: {} with guide: {}", programId, guideVersionId);

        List<ScoreDetail> components = new ArrayList<>();

        // Load real program data
        Program program = programRepository.findById(programId).orElse(null);
        if (program == null) {
            return CompositeScore.builder()
                    .finalScore(BigDecimal.ZERO)
                    .confidence(BigDecimal.ZERO)
                    .components(List.of())
                    .summary("Programme non trouvé")
                    .build();
        }

        // Load real admission criteria
        List<AdmissionCriterion> criteria = admissionCriterionRepository
                .findByGuideVersionIdAndProgramId(guideVersionId, programId);

        // Academic Score - based on real criteria
        ScoreDetail academicScore = computeAcademicScoreFromCriteria(profile, criteria);
        components.add(academicScore);

        // Subject Score
        ScoreDetail subjectScore = computeSubjectScoreFromCriteria(profile, criteria);
        components.add(subjectScore);

        // Competency Score
        ScoreDetail competencyScore = computeCompetencyScore(profile, program);
        components.add(competencyScore);

        // Preference Score
        ScoreDetail preferenceScore = computePreferenceScore(profile, program);
        components.add(preferenceScore);

        // Country Score
        ScoreDetail countryScore = computeCountryScore(profile, program);
        components.add(countryScore);

        // Language Score
        ScoreDetail languageScore = computeLanguageScore(profile, program);
        components.add(languageScore);

        // Historical Score (placeholder - will use real data)
        ScoreDetail historicalScore = ScoreDetail.builder()
                .scoreType("historical")
                .score(BigDecimal.valueOf(50))
                .weight(WEIGHTS.get("historical"))
                .explanation("Données historiques en cours d'accumulation")
                .computed(true)
                .build();
        components.add(historicalScore);

        // Compute composite score
        BigDecimal finalScore = components.stream()
                .map(s -> s.getScore().multiply(s.getWeight()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal confidence = computeConfidence(components);

        return CompositeScore.builder()
                .finalScore(finalScore)
                .confidence(confidence)
                .components(components)
                .summary(buildSummary(finalScore, confidence))
                .build();
    }

    private ScoreDetail computeAcademicScoreFromCriteria(AcademicProfile profile, List<AdmissionCriterion> criteria) {
        BigDecimal score = BigDecimal.ZERO;
        String explanation = "Score basé sur les critères d'admission";

        if (criteria.isEmpty()) {
            // Fallback to basic scoring
            if (profile.getBacAverage() != null) {
                score = profile.getBacAverage().multiply(BigDecimal.valueOf(5));
                if (score.compareTo(BigDecimal.valueOf(100)) > 0) score = BigDecimal.valueOf(100);
                explanation = "Moyenne: " + profile.getBacAverage() + "/20";
            }
        } else {
            // Score based on how well the profile meets criteria
            long metCount = 0;
            for (AdmissionCriterion criterion : criteria) {
                if (criterion.getMandatory()) {
                    BigDecimal actual = getActualValue(profile, criterion);
                    if (actual != null && criterion.getMinValue() != null) {
                        if (actual.compareTo(criterion.getMinValue()) >= 0) {
                            metCount++;
                        }
                    }
                }
            }
            long mandatoryCount = criteria.stream().filter(AdmissionCriterion::getMandatory).count();
            if (mandatoryCount > 0) {
                score = BigDecimal.valueOf((double) metCount / mandatoryCount * 100);
                explanation = metCount + "/" + mandatoryCount + " critères obligatoires remplis";
            }
        }

        return ScoreDetail.builder()
                .scoreType("academic")
                .score(score.min(BigDecimal.valueOf(100)))
                .weight(WEIGHTS.get("academic"))
                .explanation(explanation)
                .computed(true)
                .build();
    }

    private ScoreDetail computeSubjectScoreFromCriteria(AcademicProfile profile, List<AdmissionCriterion> criteria) {
        BigDecimal score = BigDecimal.valueOf(50);
        String explanation = "Score par matière";

        // Find subject-specific criteria
        long subjectCriteria = criteria.stream()
                .filter(c -> c.getSubject() != null)
                .count();

        if (subjectCriteria > 0) {
            long metSubjectCriteria = criteria.stream()
                    .filter(c -> c.getSubject() != null)
                    .filter(c -> {
                        BigDecimal actual = getActualValue(profile, c);
                        return actual != null && c.getMinValue() != null &&
                                actual.compareTo(c.getMinValue()) >= 0;
                    })
                    .count();

            score = BigDecimal.valueOf((double) metSubjectCriteria / subjectCriteria * 100);
            explanation = metSubjectCriteria + "/" + subjectCriteria + " matières spécifiques satisfaites";
        } else if (profile.getStrongSubjects() != null && !profile.getStrongSubjects().isEmpty()) {
            score = BigDecimal.valueOf(Math.min(100, 50 + profile.getStrongSubjects().size() * 10));
            explanation = profile.getStrongSubjects().size() + " matières fortes identifiées";
        }

        return ScoreDetail.builder()
                .scoreType("subject")
                .score(score)
                .weight(WEIGHTS.get("subject"))
                .explanation(explanation)
                .computed(true)
                .build();
    }

    private ScoreDetail computeCompetencyScore(AcademicProfile profile, Program program) {
        BigDecimal score = BigDecimal.valueOf(50);
        String explanation = "Compétences évaluées";

        if (profile.getCompetencyScores() != null && !profile.getCompetencyScores().isEmpty()) {
            BigDecimal maxCompetency = profile.getCompetencyScores().values().stream()
                    .max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            score = maxCompetency.multiply(BigDecimal.valueOf(10)).min(BigDecimal.valueOf(100));
            explanation = "Compétence dominante: " + profile.getDominantCompetency();
        }

        return ScoreDetail.builder()
                .scoreType("competency")
                .score(score)
                .weight(WEIGHTS.get("competency"))
                .explanation(explanation)
                .computed(true)
                .build();
    }

    private ScoreDetail computePreferenceScore(AcademicProfile profile, Program program) {
        BigDecimal score = BigDecimal.valueOf(50);
        String explanation = "Préférences";

        if (profile.getPreferredFields() != null && !profile.getPreferredFields().isEmpty()) {
            score = BigDecimal.valueOf(70);
            explanation = profile.getPreferredFields().size() + " domaine(s) d'intérêt";
        }

        return ScoreDetail.builder()
                .scoreType("preference")
                .score(score)
                .weight(WEIGHTS.get("preference"))
                .explanation(explanation)
                .computed(true)
                .build();
    }

    private ScoreDetail computeCountryScore(AcademicProfile profile, Program program) {
        BigDecimal score = BigDecimal.valueOf(60);
        String explanation = "Score pays";

        if (profile.getPreferredCountries() != null && !profile.getPreferredCountries().isEmpty()) {
            score = BigDecimal.valueOf(80);
            explanation = profile.getPreferredCountries().size() + " pays préféré(s)";
        }

        return ScoreDetail.builder()
                .scoreType("country")
                .score(score)
                .weight(WEIGHTS.get("country"))
                .explanation(explanation)
                .computed(true)
                .build();
    }

    private ScoreDetail computeLanguageScore(AcademicProfile profile, Program program) {
        BigDecimal score = BigDecimal.valueOf(60);
        String explanation = "Score langue";

        if (program.getLanguage() != null && profile.getLanguage() != null) {
            if (program.getLanguage().equalsIgnoreCase(profile.getLanguage())) {
                score = BigDecimal.valueOf(90);
                explanation = "Langue du programme correspondante: " + program.getLanguage();
            } else {
                score = BigDecimal.valueOf(40);
                explanation = "Langue du programme: " + program.getLanguage() + " (différente)";
            }
        }

        return ScoreDetail.builder()
                .scoreType("language")
                .score(score)
                .weight(WEIGHTS.get("language"))
                .explanation(explanation)
                .computed(true)
                .build();
    }

    private BigDecimal getActualValue(AcademicProfile profile, AdmissionCriterion criterion) {
        return switch (criterion.getCriterionType()) {
            case MIN_GRADE, MAX_GRADE, MIN_AVERAGE, MAX_AVERAGE -> profile.getBacAverage();
            case SPECIFIC_GRADE -> profile.getGeneralAverage();
            default -> profile.getBacAverage();
        };
    }

    private BigDecimal computeConfidence(List<ScoreDetail> components) {
        long computedCount = components.stream().filter(ScoreDetail::isComputed).count();
        return BigDecimal.valueOf((double) computedCount / components.size() * 100)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private String buildSummary(BigDecimal score, BigDecimal confidence) {
        if (score.compareTo(BigDecimal.valueOf(80)) >= 0) return "Excellent profil académique";
        if (score.compareTo(BigDecimal.valueOf(60)) >= 0) return "Bon profil académique";
        if (score.compareTo(BigDecimal.valueOf(40)) >= 0) return "Profil académique moyen";
        return "Profil académique à améliorer";
    }
}
