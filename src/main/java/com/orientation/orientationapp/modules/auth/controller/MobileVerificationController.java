package com.orientation.orientationapp.modules.auth.controller;
import com.orientation.orientationapp.modules.auth.dto.request.RegisterRequest;
import com.orientation.orientationapp.modules.auth.dto.response.MessageResponse;
import com.orientation.orientationapp.modules.auth.service.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.UUID;
@RestController @RequestMapping("/auth/mobile") @RequiredArgsConstructor
public class MobileVerificationController {
 private final RegistrationService registration;
 private final EmailVerificationCodeService codes;
 public record VerifyRequest(@NotNull UUID challengeId,@NotBlank @Pattern(regexp="[0-9]{6}") String code) {}
 public record ResendRequest(@NotNull UUID challengeId) {}
 @PostMapping("/register")
 public ResponseEntity<EmailVerificationCodeService.Receipt> register(@Valid @RequestBody RegisterRequest request){
  return ResponseEntity.accepted().body(EmailVerificationCodeService.receipt(registration.registerMobile(request)));
 }
 @PostMapping("/verify-email")
 public MessageResponse verify(@Valid @RequestBody VerifyRequest request){codes.verify(request.challengeId(),request.code());return MessageResponse.success("Adresse e-mail vérifiée. Connectez-vous dans l’application.");}
 @PostMapping("/resend-code")
 public ResponseEntity<EmailVerificationCodeService.Receipt> resend(@Valid @RequestBody ResendRequest request){return ResponseEntity.accepted().body(codes.resend(request.challengeId()));}
}
