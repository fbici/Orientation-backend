package com.orientation.orientationapp.intelligence.pipeline;

import com.orientation.orientationapp.intelligence.extraction.EntityExtractionService.ExtractedEntities;
import com.orientation.orientationapp.modules.university.entity.University;
import com.orientation.orientationapp.modules.university.entity.Program;
import com.orientation.orientationapp.modules.university.repository.UniversityRepository;
import com.orientation.orientationapp.modules.university.repository.ProgramRepository;
import com.orientation.orientationapp.modules.scholarship.entity.Scholarship;
import com.orientation.orientationapp.modules.scholarship.repository.ScholarshipRepository;
import com.orientation.orientationapp.modules.orientation.entity.Subject;
import com.orientation.orientationapp.modules.orientation.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service de persistance des entités extraites.
 *
 * Insère dans PostgreSQL les entités extraites par le pipeline :
 * - Universités (si non existantes)
 * - Programmes (si non existants)
 * - Bourses (si non existantes)
 * - Matières (si non existantes)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PersistenceService {

    private final UniversityRepository universityRepository;
    private final ProgramRepository programRepository;
    private final ScholarshipRepository scholarshipRepository;
    private final SubjectRepository subjectRepository;

    @Transactional
    public void persist(ExtractedEntities entities, UUID documentId) {
        log.info("Persisting extracted entities for document {}", documentId);

        // Persister les universités
        if (entities.getUniversities() != null) {
            for (var ext : entities.getUniversities()) {
                if (ext.getName() == null || ext.getName().isBlank()) continue;
                // Vérifier si l'université existe déjà
                boolean exists = universityRepository.findByNameContainingIgnoreCase(ext.getName()).stream()
                        .anyMatch(u -> u.getName().equalsIgnoreCase(ext.getName()));
                if (!exists) {
                    University uni = new University();
                    uni.setName(ext.getName());
                    uni.setActive(true);
                    universityRepository.save(uni);
                    log.info("Created university: {}", ext.getName());
                }
            }
        }

        // Persister les matières
        if (entities.getSubjects() != null) {
            for (var ext : entities.getSubjects()) {
                if (ext.getName() == null || ext.getName().isBlank()) continue;
                boolean exists = subjectRepository.findAll().stream()
                        .anyMatch(s -> s.getName().equalsIgnoreCase(ext.getName()));
                if (!exists) {
                    Subject sub = new Subject();
                    sub.setName(ext.getName());
                    subjectRepository.save(sub);
                    log.info("Created subject: {}", ext.getName());
                }
            }
        }

        // Persister les bourses
        if (entities.getScholarships() != null) {
            for (var ext : entities.getScholarships()) {
                if (ext.getName() == null || ext.getName().isBlank()) continue;
                Scholarship sch = new Scholarship();
                sch.setName(ext.getName());
                if (ext.getAmount() != null) sch.setAmount(ext.getAmount());
                scholarshipRepository.save(sch);
                log.info("Created scholarship: {}", ext.getName());
            }
        }

        log.info("Persistence completed for document {}", documentId);
    }
}
