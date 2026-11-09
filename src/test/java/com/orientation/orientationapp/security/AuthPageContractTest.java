package com.orientation.orientationapp.security;
import com.orientation.orientationapp.modules.auth.controller.AuthPageController;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
class AuthPageContractTest {
 @Test void explicitAuthPagesServeHtmlWithoutCachingSensitiveQueryLinks() throws Exception {
  var mvc=MockMvcBuilders.standaloneSetup(new AuthPageController()).build();
  for(String path:new String[]{"/auth/login","/auth/register","/auth/verify-email","/auth/forgot-password","/auth/reset-password"})
   mvc.perform(get(path).accept("text/html")).andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith("text/html")).andExpect(header().string("Cache-Control","no-store")).andExpect(header().string("Referrer-Policy","no-referrer"));
 }
 @Test void publicHtmlControllerDoesNotReplaceRestVerificationOrPrivateApi() throws Exception {
  var mvc=MockMvcBuilders.standaloneSetup(new AuthPageController()).build();
  mvc.perform(get("/auth/verify").accept("application/json")).andExpect(status().isNotFound());
  mvc.perform(get("/admin/users").accept("text/html")).andExpect(status().isNotFound());
 }
}
