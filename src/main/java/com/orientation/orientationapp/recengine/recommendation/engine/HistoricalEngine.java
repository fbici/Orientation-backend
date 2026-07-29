package com.orientation.orientationapp.recengine.recommendation.engine;

import com.orientation.orientationapp.modules.orientation.entity.AdmissionCriterion;
import com.orientation.orientationapp.modules.orientation.repository.AdmissionCriterionRepository;
import com.orientation.orientationapp.modules.scholarship.entity.Scholarship;
import com.orientation.orientationapp.modules.scholarship.repository.ScholarshipRepository;
import com.orientation.orientationapp.modules.university.entity.Program;
import com.orientation.orientationapp.modules.university.entity.University;
import com.orientation.orientationapp.modules.university.repository.ProgramRepository;
import com.orientation.orientationapp.modules.university.repository.UniversityRepository;
import com.orientation.orientationapp.recengine.profile.model.AcademicProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class HistoricalEngine {

    private final ProgramRepository programRepository;
    private final UniversityRepository universityRepository;
    private final AdmissionCriterionRepository admissionCriterionRepository;
    private final ScholarshipRepository scholarshipRepository;

    /**
     * Compute historical score based on real data.
     */
    public BigDecimal computeHistoricalScore(AcademicProfile profile, UUID programId) {
        Program program = programRepository.findById(programId).orElse(null);
        if (program == null) return BigDecimal.valueOf(50);

        BigDecimal score = BigDecimal.valueOf(50);

        // Factor 1: Program availability
        if (Boolean.TRUE.equals(program.getAvailable())) {
            score = score.add(BigDecimal.valueOf(10));
        } else {
            score = score.subtract(BigDecimal.valueOf(20));
        }

        // Factor 2: Capacity
        if (program.getMaxStudents() != null && program.getMaxStudents() > 0) {
            score = score.add(BigDecimal.valueOf(5));
        }

        // Factor 3: University ranking
        University university = program.getFaculty().getCampus().getUniversity();
        if (university.getRanking() != null && university.getRanking() <= 10) {
            score = score.add(BigDecimal.valueOf(10));
        }

        return score.min(BigDecimal.valueOf(100)).max(BigDecimal.ZERO);
    }

    /**
     * Compute admission probability based on historical patterns.
     */
    public BigDecimal computeAdmissionProbability(AcademicProfile profile, UUID programId, UUID guideVersionId) {
        List<AdmissionCriterion> criteria = admissionCriterionRepository
                .findByGuideVersionIdAndProgramId(guideVersionId, programId);

        if (criteria.isEmpty()) {
            return BigDecimal.valueOf(50);
        }

        long metCount = 0;
        long totalWeight = 0;

        for (AdmissionCriterion criterion : criteria) {
            if (criterion.getMandatory()) {
                totalWeight += 10;
                // Simplified: check if profile meets basic criteria
                if (profile.getBacAverage() != null && criterion.getMinValue() != null) {
                    if (profile.getBacAverage().compareTo(criterion.getMinValue()) >= 0) {
                        metCount += 10;
                    }
                }
            }
        }

        if (totalWeight == 0) return BigDecimal.valueOf(50);

        return BigDecimal.valueOf((double) metCount / totalWeight * 100)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Get scholarship eligibility score.
     */
    public BigDecimal computeScholarshipScore(AcademicProfile profile, UUID scholarshipId) {
        Scholarship scholarship = scholarshipRepository.findById(scholarshipId).orElse(null);
        if (scholarship == null) return BigDecimal.ZERO;

        BigDecimal score = BigDecimal.valueOf(50);

        // Factor: merit-based scholarships reward high averages
        if (scholarship.getType().name().contains("MERIT")) {
            if (profile.getBacAverage() != null && profile.getBacAverage().compareTo(BigDecimal.valueOf(16)) >= 0) {
                score = score.add(BigDecimal.valueOf(30));
            }
        }

        // Factor: available slots
        if (scholarship.getRemainingSlots() != null && scholarship.getRemainingSlots() > 0) {
            score = score.add(BigDecimal.valueOf(10));
        }

        return score.min(BigDecimal.valueOf(100));
    }

    /**
     * Get university statistics for scoring.
     */
    public Map<String, Object> getUniversityStats(UUID universityId) {
        Map<String, Object> stats = new HashMap<>();
        University university = universityRepository.findById(universityId).orElse(null);
        if (university != null) {
            stats.put("ranking", university.getRanking());
            stats.put("studentCount", university.getStudentCount());
            stats.put("acceptanceRate", university.getAcceptanceRate());
            stats.put("active", university.getActive());
        }
        return stats;
    }

    /**
     * Get program statistics.
     */
    public Map<String, Object> getProgramStats(UUID programId) {
        Map<String, Object> stats = new HashMap<>();
        Program program = programRepository.findById(programId).orElse(null);
        if (program != null) {
            stats.put("available", program.getAvailable());
            stats.put("maxStudents", program.getMaxStudents());
            stats.put("tuitionFee", program.getTuitionFee());
            stats.put("duration", program.getDuration());
        }
        return stats;
    }
}
