package com.orientation.orientationapp.security.service;

import com.orientation.orientationapp.modules.auth.entity.User;
import com.orientation.orientationapp.modules.auth.repository.UserRepository;
import com.orientation.orientationapp.modules.user.entity.Candidate;
import com.orientation.orientationapp.modules.user.repository.CandidateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import com.orientation.orientationapp.security.util.SecurityUtils;
import java.util.UUID;

@Service("resourceAccess")
@RequiredArgsConstructor
@Transactional(readOnly=true)
public class ResourceAccess {
    private final UserRepository users;
    private final CandidateRepository candidates;
    public boolean authority(String code) {
        var a=SecurityContextHolder.getContext().getAuthentication();
        return a!=null && a.isAuthenticated() && a.getAuthorities().stream().anyMatch(x -> x.getAuthority().equals(code));
    }
    public User current() {
        String id=SecurityUtils.getCurrentUserId().orElseThrow(() -> new BadCredentialsException("Authentication required"));
        return users.findById(UUID.fromString(id)).filter(AccountState::active)
                .orElseThrow(() -> new BadCredentialsException("Invalid session"));
    }
    public boolean global() { return authority("ROLE_SUPER_ADMIN") && authority("TENANT_ACCESS_ALL"); }
    public void require(String code) {
        current();
        if (!authority(code)) throw new AccessDeniedException("Operation not permitted");
    }
    public UUID tenant(UUID requested) {
        UUID own=current().getTenant().getId();
        if (requested==null) return own;
        if (!own.equals(requested) && !global()) throw denied();
        return requested;
    }
    public void sameTenant(UUID tenant) {
        if (tenant==null) throw denied();
        if (!tenant.equals(current().getTenant().getId()) && !global()) throw denied();
    }
    public void sameTenant(String tenant) {
        try { sameTenant(UUID.fromString(tenant)); }
        catch (IllegalArgumentException|NullPointerException ex) { throw denied(); }
    }
    public Candidate ownCandidate() {
        User user=current();
        Candidate candidate=candidates.findByUserId(user.getId()).orElseThrow(ResourceAccess::denied);
        sameTenant(candidate.getTenant()==null?null:candidate.getTenant().getId());
        return candidate;
    }
    public Candidate candidate(UUID id) {
        Candidate candidate=candidates.findById(id).orElseThrow(ResourceAccess::denied);
        sameTenant(candidate.getTenant()==null?null:candidate.getTenant().getId());
        if (candidate.getUser()==null) throw denied();
        if (!candidate.getUser().getId().equals(current().getId()) && !authority("CANDIDATE_READ")) throw denied();
        return candidate;
    }
    public void document(String tenant,String owner,boolean writing) {
        sameTenant(tenant);
        if (current().getId().toString().equals(owner)) return;
        require(writing?"DOCUMENT_MANAGE":"DOCUMENT_READ");
    }
    public String personalUser(String requested) {
        String own=current().getId().toString();
        if (requested!=null && !own.equals(requested)) throw denied();
        return own;
    }
    public static AccessDeniedException denied() { return new AccessDeniedException("Resource unavailable"); }
}
