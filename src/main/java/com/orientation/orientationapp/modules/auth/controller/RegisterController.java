package com.orientation.orientationapp.modules.auth.controller;
import com.orientation.orientationapp.modules.auth.dto.request.RegisterRequest;
import com.orientation.orientationapp.modules.auth.dto.response.RegisterResponse;
import com.orientation.orientationapp.modules.auth.service.*;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/auth") @RequiredArgsConstructor
public class RegisterController {
    private final RegistrationService registration;
    private final AccountRecoveryService recovery;
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        registration.register(request);
        return ResponseEntity.accepted().body(RegisterResponse.builder()
            .message("Registration request accepted. If eligible, check your email.").build());
    }
    @GetMapping("/verify")
    public ResponseEntity<com.orientation.orientationapp.modules.auth.dto.response.MessageResponse> verifyEmail(@RequestParam String token) {
        recovery.verify(token);
        return ResponseEntity.ok(com.orientation.orientationapp.modules.auth.dto.response.MessageResponse.success("Email verified"));
    }
}
