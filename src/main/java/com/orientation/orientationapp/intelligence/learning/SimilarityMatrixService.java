package com.orientation.orientationapp.intelligence.learning;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service de matrice de similarité.
 *
 * Calcule les similarités entre candidats et programmes
 * basées sur le feedback historique.
 *
 * Quand un candidat accepte un programme, le score de similarité
 * augmente pour les candidats ayant un profil similaire.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SimilarityMatrixService {

    // En production, stocker en base (table similarity_matrix)
    private final Map<String, Double> matrix = new HashMap<>();

    /**
     * Renforce ou affaiblit le lien candidat-programme.
     */
    public void reinforce(UUID candidateId, UUID programId, double delta) {
        String key = candidateId + ":" + programId;
        double current = matrix.getOrDefault(key, 1.0);
        double updated = Math.max(0.1, Math.min(2.0, current + delta));
        matrix.put(key, updated);
        log.debug("Similarity updated: {} -> {}", key, updated);
    }

    /**
     * Récupère le score de similarité entre un candidat et un programme.
     */
    public double getScore(UUID candidateId, UUID programId) {
        String key = candidateId + ":" + programId;
        return matrix.getOrDefault(key, 1.0);
    }

    /**
     * Trouve les programmes populaires pour un candidat donné.
     */
    public List<UUID> findSimilarPrograms(UUID candidateId) {
        List<UUID> programs = new ArrayList<>();
        String prefix = candidateId.toString() + ":";
        for (Map.Entry<String, Double> entry : matrix.entrySet()) {
            if (entry.getKey().startsWith(prefix) && entry.getValue() > 1.0) {
                String programIdStr = entry.getKey().substring(prefix.length());
                try {
                    programs.add(UUID.fromString(programIdStr));
                } catch (IllegalArgumentException ignored) {}
            }
        }
        return programs;
    }
}
