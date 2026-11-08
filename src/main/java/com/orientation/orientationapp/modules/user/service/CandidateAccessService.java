package com.orientation.orientationapp.modules.user.service;
import com.orientation.orientationapp.modules.user.entity.Candidate;
import com.orientation.orientationapp.modules.user.repository.CandidateRepository;
import com.orientation.orientationapp.security.service.ResourceAccess;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;
import java.util.UUID;
@Service @RequiredArgsConstructor @Transactional(readOnly=true)
public class CandidateAccessService {
    private final CandidateRepository candidates;
    private final ResourceAccess access;
    public Candidate get(UUID id) { return access.candidate(id); }
    public Page<Candidate> list(String search,Pageable pageable) {
        access.require("CANDIDATE_READ");
        return candidates.findScoped(access.tenant(null),search, pageable);
    }
}
