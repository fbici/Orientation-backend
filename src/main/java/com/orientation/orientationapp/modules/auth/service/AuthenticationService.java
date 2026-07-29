package com.orientation.orientationapp.modules.auth.service;

import com.orientation.orientationapp.modules.auth.dto.request.*;
import com.orientation.orientationapp.modules.auth.dto.response.LoginResponse;
import com.orientation.orientationapp.modules.auth.dto.response.MessageResponse;
import com.orientation.orientationapp.modules.auth.dto.response.UserInfo;

public interface AuthenticationService {
    LoginResponse login(LoginRequest request, String ipAddress, String userAgent);
    LoginResponse refreshToken(RefreshTokenRequest request);
    MessageResponse logout(String refreshToken, String userId);
    MessageResponse forgotPassword(ForgotPasswordRequest request);
    MessageResponse resetPassword(ResetPasswordRequest request);
    UserInfo getCurrentUser(String userId);
}
