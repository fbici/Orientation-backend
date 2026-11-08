package com.orientation.orientationapp.modules.transcript.service;
import com.orientation.orientationapp.modules.transcript.entity.Transcript;
import com.orientation.orientationapp.modules.transcript.repository.TranscriptRepository;
import com.orientation.orientationapp.modules.orientation.repository.AcademicYearRepository;
import com.orientation.orientationapp.security.service.ResourceAccess;
import com.orientation.orientationapp.storage.service.StorageService;
import com.orientation.orientationapp.common.enums.TranscriptStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;
@Service @RequiredArgsConstructor @Transactional
public class TranscriptAccessService {
    private final TranscriptRepository transcripts;
    private final AcademicYearRepository years;
    private final ResourceAccess access;
    private final StorageService storage;
    @Transactional(readOnly=true) public Transcript get(UUID id) {
        Transcript t=transcripts.findById(id).orElseThrow(ResourceAccess::denied);
        access.candidate(t.getCandidate().getId());return t;
    }
    @Transactional(readOnly=true) public Page<Transcript> list(Pageable pageable) {
        if(access.authority("CANDIDATE_READ")) return transcripts.findScoped(access.tenant(null),pageable);
        return transcripts.findByCandidateId(access.ownCandidate().getId(),pageable);
    }
    public Transcript upload(MultipartFile file) {return upload(file,null);}
    public Transcript upload(MultipartFile file, UUID academicYearId) {
        var candidate=access.ownCandidate();
        var year=(academicYearId==null?years.findByCurrentTrue():years.findById(academicYearId)).orElseThrow(() -> new com.orientation.orientationapp.exception.BusinessException(
            "No current academic year is configured","ACADEMIC_YEAR_REQUIRED"));
        Transcript transcript=new Transcript();transcript.setCandidate(candidate);transcript.setAcademicYear(year);
        transcript.setOriginalFileName(file.getOriginalFilename());transcript.setTitle("Uploaded transcript");
        transcript.setFileUrl(storage.store(file));transcript.setStatus(TranscriptStatus.DRAFT);transcript.setSource("UPLOAD");
        return transcripts.saveAndFlush(transcript);
    }
}
