package com.orientation.orientationapp.modules.auth.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EmailTemplateRendererTest {
    private String render(String name,String message,String url) {
        return EmailTemplateRenderer.render("https://orientia.example.invalid","Votre compte",name,message,
            "Continuer",url,"Lien personnel à usage unique.");
    }
    @Test void escapesUserControlledMarkupAndAttributeCharacters() {
        String html=render("<img src=x onerror=alert(1)>","<script>alert(1)</script>","https://orientia.example.invalid/reset?token=a&other=%22value%22");
        assertFalse(html.contains("<script>"));
        assertFalse(html.contains("<img src=x"));
        assertTrue(html.contains("&lt;script&gt;"));
        assertTrue(html.contains("&amp;other=%22value%22"));
    }
    @Test void refusesExecutableAndCredentialBearingLinks() {
        for(String url:new String[]{"javascript:alert(1)","https://user:password@example.invalid/path","//example.invalid/path"})
            assertThrows(IllegalArgumentException.class,()->render("Test","Message",url));
    }
    @Test void fallbackLinkAndMeaningRemainAvailableWithoutImages() {
        String url="https://orientia.example.invalid/auth/verify-email?token=synthetic";
        String html=render(null,"Confirmez votre compte.",url);
        assertTrue(html.contains("Bonjour,"));
        assertTrue(html.contains("Confirmez votre compte."));
        assertTrue(html.contains(">"+url+"</a>"));
        assertTrue(html.contains("alt=\"Orientia\""));
        assertFalse(html.contains("data:image"));
    }
    @Test void logoAndAllEmailMarkupStaySmallEnoughForMailClients() {
        String html=render("Test","Message","https://orientia.example.invalid");
        assertTrue(html.getBytes(java.nio.charset.StandardCharsets.UTF_8).length<20_000);
        assertTrue(html.contains("https://orientia.example.invalid/assets/email/orientia-logo.png"));
    }
}
