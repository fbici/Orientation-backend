package com.orientation.orientationapp.modules.auth.controller;
import com.orientation.orientationapp.modules.auth.service.CandidateDeletionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
@RestController @RequestMapping("/auth/me/deletion-request") @RequiredArgsConstructor
public class CandidateDeletionController {
    private final CandidateDeletionService service;
    public record Request(@NotBlank @Pattern(regexp="SUPPRIMER") String confirmation,
        @Size(max=72) String currentPassword,@Size(max=16000) String googleIdToken) {
        @Override public String toString() { return "CandidateDeletionRequest[credentials=REDACTED]"; }
    }
    @PostMapping @PreAuthorize("hasRole('CANDIDAT')")
    public CandidateDeletionService.Receipt request(@Valid @RequestBody Request request) {
        return service.request(request.confirmation(),request.currentPassword(),request.googleIdToken());
    }
}
