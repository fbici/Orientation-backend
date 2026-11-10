package com.orientation.orientationapp.security;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import com.orientation.orientationapp.modules.auth.service.GoogleIdentityVerifier;
import com.orientation.orientationapp.modules.auth.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.util.*;
@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("f1-test")
@EnabledIfSystemProperty(named="f1.jdbc.url",matches="jdbc:postgresql://127[.]0[.]0[.]1:[0-9]+/orientia_f1")
class GoogleSignInIntegrationTest {
    @DynamicPropertySource static void isolated(DynamicPropertyRegistry p) {
        String schema=System.getProperty("f1.jdbc.schema","public");
        if(!schema.matches("[a-z][a-z0-9_]{0,50}"))throw new IllegalArgumentException("Isolated schema required");
        p.add("spring.datasource.url",()->System.getProperty("f1.jdbc.url")+"?currentSchema="+schema+",public");
        p.add("spring.flyway.schemas",()->schema);p.add("spring.flyway.default-schema",()->schema);
        p.add("spring.jpa.properties.hibernate.default_schema",()->schema);
        p.add("spring.datasource.username",()->"orientia_f1");p.add("spring.datasource.password",()->System.getenv("F1_TEST_DB_PASSWORD"));
    }
    @Autowired MockMvc mvc;@Autowired JdbcTemplate jdbc;@Autowired UserRepository users;@Autowired ObjectMapper json;
    @MockBean GoogleIdentityVerifier verifier;
    String subject,email;
    @BeforeEach void setup(){subject=UUID.randomUUID().toString();email=UUID.randomUUID()+"@example.test";identity(email);}
    void identity(String mail){when(verifier.verify(anyString())).thenReturn(new GoogleIdentityVerifier.Identity(subject,mail,"Google","Fixture"));}
    Map<?,?> login() throws Exception {
        var result=mvc.perform(post("/auth/google").contentType("application/json").content("{\"idToken\":\"synthetic-verifier-fixture\"}"))
            .andExpect(status().isOk()).andReturn();
        return json.readValue(result.getResponse().getContentAsString(),Map.class);
    }
    @Test void newGoogleUserGetsOnlyCandidateRoleAndRealSession() throws Exception {
        var session=login();assertNotNull(session.get("refreshToken"));
        var user=(Map<?,?>)session.get("user");assertEquals(List.of("CANDIDAT"),user.get("roles"));
        var id=UUID.fromString(user.get("id").toString());
        assertEquals(1,jdbc.queryForObject("select count(*) from auth_external_identities where user_id=?",Integer.class,id));
        assertEquals(1,jdbc.queryForObject("select count(*) from candidates where user_id=?",Integer.class,id));
        mvc.perform(get("/auth/me").header("Authorization","Bearer "+session.get("accessToken"))).andExpect(status().isOk());
        mvc.perform(post("/auth/refresh").contentType("application/json").content(json.writeValueAsString(Map.of("refreshToken",session.get("refreshToken")))))
            .andExpect(status().isOk());
    }
    @Test void subjectIsStableAndExistingEmailNeverAutoLinks() throws Exception {
        var first=login();String id=((Map<?,?>)first.get("user")).get("id").toString();
        identity(UUID.randomUUID()+"@example.test");var again=login();assertEquals(id,((Map<?,?>)again.get("user")).get("id"));
        subject=UUID.randomUUID().toString();identity(email);
        mvc.perform(post("/auth/google").contentType("application/json").content("{\"idToken\":\"other-subject\"}"))
            .andExpect(status().isConflict()).andExpect(jsonPath("$.message").value("GOOGLE_ACCOUNT_EXISTS"));
    }
    @Test void disabledAccountCannotGetAnotherSession() throws Exception {
        var first=login();var id=UUID.fromString(((Map<?,?>)first.get("user")).get("id").toString());
        jdbc.update("update users set enabled=false where id=?",id);
        mvc.perform(post("/auth/google").contentType("application/json").content("{\"idToken\":\"synthetic\"}"))
            .andExpect(status().isUnauthorized());
    }
}
