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
    /** No observed admission outcomes are persisted; no statistical value can be returned. */
    public BigDecimal computeHistoricalScore(AcademicProfile profile, UUID programId) { return null; }
    public BigDecimal computeAdmissionProbability(AcademicProfile profile, UUID programId, UUID guideVersionId) { return null; }
    public BigDecimal computeScholarshipScore(AcademicProfile profile, UUID scholarshipId) { return null; }

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
