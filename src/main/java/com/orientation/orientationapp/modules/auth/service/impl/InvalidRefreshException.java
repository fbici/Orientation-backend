package com.orientation.orientationapp.modules.auth.service.impl;
public class InvalidRefreshException extends org.springframework.security.authentication.BadCredentialsException {
    public InvalidRefreshException() { super("Invalid session"); }
}
