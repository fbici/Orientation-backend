package com.orientation.orientationapp.modules.auth.controller;
import com.orientation.orientationapp.modules.auth.service.GoogleLoginService;
import com.orientation.orientationapp.modules.auth.dto.response.LoginResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
@RestController @RequestMapping("/auth") @RequiredArgsConstructor
public class GoogleAuthController {
    public record Request(@NotBlank @Size(max=16000) String idToken) {}
    private final GoogleLoginService google;
    @PostMapping("/google")
    public LoginResponse login(@Valid @RequestBody Request request,HttpServletRequest http) {
        try {return google.login(request.idToken(),http.getRemoteAddr(),http.getHeader("User-Agent"));}
        catch(org.springframework.dao.DataIntegrityViolationException conflict) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,"GOOGLE_SIGNIN_RETRY");
        }
    }
}
