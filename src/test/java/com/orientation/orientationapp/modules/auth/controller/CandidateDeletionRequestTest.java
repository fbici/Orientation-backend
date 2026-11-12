package com.orientation.orientationapp.modules.auth.controller;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class CandidateDeletionRequestTest {
 @Test void diagnosticStringCannotExposeReauthenticationCredentials() {
  var request=new CandidateDeletionController.Request("SUPPRIMER","synthetic-secret-password","synthetic-google-token");
  assertFalse(request.toString().contains(request.currentPassword()));
  assertFalse(request.toString().contains(request.googleIdToken()));
  assertTrue(request.toString().contains("REDACTED"));
 }
}
