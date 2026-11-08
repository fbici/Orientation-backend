package com.orientation.orientationapp.security;

import com.orientation.orientationapp.security.handler.JwtAuthenticationEntryPoint;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticationEntryPointTest {
    @Test
    void internalAuthenticationDetailsStayPrivate() throws Exception {
        var response = new MockHttpServletResponse();
        new JwtAuthenticationEntryPoint().commence(new MockHttpServletRequest(), response,
                new BadCredentialsException("INTERNAL_PRIVATE_DIAGNOSTIC"));
        assertEquals(401, response.getStatus());
        assertFalse(response.getContentAsString().contains("INTERNAL_PRIVATE_DIAGNOSTIC"));
        assertTrue(response.getContentAsString().contains("Authentication required"));
    }

    @Test
    void verificationMarkerIsPublicAndForbidden() throws Exception {
        var response = new MockHttpServletResponse();
        new JwtAuthenticationEntryPoint().commence(new MockHttpServletRequest(), response,
                new BadCredentialsException("EMAIL_NOT_VERIFIED"));
        assertEquals(403, response.getStatus());
        assertTrue(response.getContentAsString().contains("EMAIL_NOT_VERIFIED"));
    }

    @Test
    void wrappedVerificationMarkerKeepsItsMeaning() throws Exception {
        var response = new MockHttpServletResponse();
        new JwtAuthenticationEntryPoint().commence(new MockHttpServletRequest(), response,
                new BadCredentialsException("wrapper", new IllegalStateException("EMAIL_NOT_VERIFIED")));
        assertEquals(403, response.getStatus());
        assertTrue(response.getContentAsString().contains("EMAIL_NOT_VERIFIED"));
    }
}
