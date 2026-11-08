package com.orientation.orientationapp.modules.transcript.dto;
import com.orientation.orientationapp.modules.transcript.entity.Transcript;
import java.util.UUID;
import java.math.BigDecimal;
import java.time.Instant;
public record TranscriptResponse(UUID id,String title,String institution,BigDecimal average,
        Integer totalSubjects,Integer totalCredits,String mention,String status,String source,
        String originalFileName,BigDecimal ocrConfidence,UUID candidateId,UUID academicYearId,
        Instant createdAt,Instant updatedAt) {
    public static TranscriptResponse from(Transcript t) {
        return new TranscriptResponse(t.getId(),t.getTitle(),t.getInstitution(),t.getAverage(),
            t.getTotalSubjects(),t.getTotalCredits(),t.getMention(),t.getStatus().name(),t.getSource(),
            t.getOriginalFileName(),t.getOcrConfidence(),t.getCandidate().getId(),t.getAcademicYear().getId(),
            t.getCreatedAt(),t.getUpdatedAt());
    }
}
