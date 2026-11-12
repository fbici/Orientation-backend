package com.orientation.orientationapp.security;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("f1-test")
@EnabledIfSystemProperty(named="f1.jdbc.url",matches="jdbc:postgresql://127[.]0[.]0[.]1:[0-9]+/orientia_f1")
class FoundationsIntegrationTest {
    @DynamicPropertySource static void isolated(DynamicPropertyRegistry properties) {
        String schema=System.getProperty("f1.jdbc.schema","public");
        if(!schema.matches("[a-z][a-z0-9_]{0,50}")) throw new IllegalArgumentException("Invalid isolated schema");
        properties.add("spring.datasource.url",()->System.getProperty("f1.jdbc.url")+"?currentSchema="+schema+",public");
        properties.add("spring.flyway.schemas",()->schema);
        properties.add("spring.flyway.default-schema",()->schema);
        properties.add("spring.jpa.properties.hibernate.default_schema",()->schema);
        properties.add("spring.datasource.username",()->"orientia_f1");
        properties.add("spring.datasource.password",()->System.getenv("F1_TEST_DB_PASSWORD"));
        properties.add("app.storage.upload-dir",()->System.getProperty("java.io.tmpdir")+"/orientia-f1-test-uploads");
    }
    @Autowired JdbcTemplate jdbc;
    @Autowired MockMvc mvc;
    @Test void inactiveAndDeletedAddressesNeverReceiveMobileChallenges() throws Exception {
        a.setEnabled(false);users.saveAndFlush(a);
        b.setDeleted(true);users.saveAndFlush(b);
        for (var user:java.util.List.of(a,b)) {
            var payload=java.util.Map.of("email",user.getEmail().toUpperCase(java.util.Locale.ROOT),"password",password,"firstName","Test","lastName","Candidate");
            mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/auth/mobile/register")
                .contentType("application/json").content(json.writeValueAsString(payload)))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isBadRequest())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.code").value("ACCOUNT_INACTIVE"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.challengeId").doesNotExist());
            assertEquals(0,jdbc.queryForObject("select count(*) from email_verification_challenges where user_id=?",Integer.class,user.getId()));
        }
    }
    @Test void personalAccountReturnsPhoneForSafeEditing() throws Exception {
        a.setPhone("+2290197000000");users.saveAndFlush(a);
        var session=login(a);
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/auth/me")
            .header("Authorization","Bearer "+session.getAccessToken()))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.phone").value(a.getPhone()));
    }
    @Autowired com.orientation.orientationapp.modules.auth.service.CandidateErasureService erasure;
    private void requestDeletion(com.orientation.orientationapp.modules.auth.dto.response.LoginResponse session) throws Exception {
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/auth/me/deletion-request")
            .header("Authorization","Bearer "+session.getAccessToken()).contentType("application/json")
            .content(json.writeValueAsString(java.util.Map.of("confirmation","SUPPRIMER","currentPassword",password))))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.purgeAfter").isNotEmpty());
    }
    private void makeDeletionDue(java.util.UUID id) {
        jdbc.update("UPDATE candidate_deletion_requests SET requested_at=now()-interval '31 days',purge_after=now()-interval '1 day' WHERE user_id=?",id);
    }
    @Test void deletionRequiresOwnCandidateIdentityAndExplicitConfirmation() throws Exception {
        var session=login(a);
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/auth/me/deletion-request")
            .header("Authorization","Bearer "+session.getAccessToken()).contentType("application/json")
            .content(json.writeValueAsString(java.util.Map.of("confirmation","SUPPRIMER","currentPassword","incorrect-password"))))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isBadRequest())
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.code").value("DELETION_IDENTITY_REQUIRED"));
        assertEquals(0,jdbc.queryForObject("SELECT count(*) FROM candidate_deletion_requests WHERE user_id=?",Integer.class,a.getId()));
        assertTrue(users.findById(a.getId()).orElseThrow().getEnabled());
        var admin=login(adminA);
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/auth/me/deletion-request")
            .header("Authorization","Bearer "+admin.getAccessToken()).contentType("application/json")
            .content(json.writeValueAsString(java.util.Map.of("confirmation","SUPPRIMER","currentPassword",password))))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isForbidden());
    }
    @Test void pendingDeletionRevokesSessionsAndReservesEmailForAtLeastThirtyDays() throws Exception {
        var session=login(a);requestDeletion(session);
        assertFalse(erasure.eraseDue(a.getId()));
        assertTrue(jdbc.queryForObject("SELECT purge_after>=requested_at+interval '30 days' FROM candidate_deletion_requests WHERE user_id=?",Boolean.class,a.getId()));
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/auth/me").header("Authorization","Bearer "+session.getAccessToken()))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isUnauthorized());
        assertThrows(com.orientation.orientationapp.modules.auth.service.impl.InvalidRefreshException.class,()->authentication.refreshToken(renewal(session.getRefreshToken())));
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/auth/mobile/register").contentType("application/json")
            .content(json.writeValueAsString(java.util.Map.of("email",a.getEmail(),"password",password,"firstName","Test","lastName","Candidate"))))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isBadRequest())
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.code").value("ACCOUNT_DELETION_PENDING"));
        assertEquals(0,jdbc.queryForObject("SELECT count(*) FROM email_verification_challenges WHERE user_id=?",Integer.class,a.getId()));
    }
    @Test void dueErasureDeletesPrivateChildrenFilesAndAddressButPreservesOtherCandidates() throws Exception {
        var doc=document(a);var other=document(b);var session=login(a);
        jdbc.update("INSERT INTO doc_document_pages(document_id,page_number,raw_text) VALUES (?,1,'synthetic private OCR')",doc.getId());
        var year=jdbc.queryForObject("SELECT id FROM academic_years LIMIT 1",java.util.UUID.class);
        var transcript=java.util.UUID.randomUUID();
        jdbc.update("INSERT INTO transcripts(id,candidate_id,academic_year_id,title) VALUES (?,?,?,'synthetic personal transcript')",transcript,ca.getId(),year);
        jdbc.update("INSERT INTO transcript_analyses(transcript_id,analysis_version,analyzed_at,review_payload) VALUES (?,'1',now(),'{}'::jsonb)",transcript);
        var chat=java.util.UUID.randomUUID();
        jdbc.update("INSERT INTO decision_records(id,candidate_id,candidate_version,actor_id,kind,payload) VALUES (?,?,0,?,'CHAT_SESSION','{}')",chat,ca.getId(),a.getId());
        jdbc.update("INSERT INTO decision_records(candidate_id,candidate_version,actor_id,kind,parent_id,payload) VALUES (?,0,?,'CHAT_MESSAGE',?,'{}')",ca.getId(),a.getId(),chat);
        requestDeletion(session);makeDeletionDue(a.getId());assertTrue(erasure.eraseDue(a.getId()));
        assertEquals(0,users.countAllByEmail(a.getEmail()));
        for(String table:java.util.List.of("users","candidates","doc_documents")) {
            var id=table.equals("users")?a.getId():table.equals("candidates")?ca.getId():doc.getId();
            assertEquals(0,jdbc.queryForObject("SELECT count(*) FROM "+table+" WHERE id=?",Integer.class,id));
        }
        assertEquals(0,jdbc.queryForObject("SELECT count(*) FROM transcripts WHERE id=?",Integer.class,transcript));
        assertEquals(0,jdbc.queryForObject("SELECT count(*) FROM decision_records WHERE candidate_id=?",Integer.class,ca.getId()));
        assertFalse(storage.exists(doc.getStoredFileName()));assertTrue(storage.exists(other.getStoredFileName()));
        assertEquals(1,users.countAllByEmail(b.getEmail()));assertFalse(erasure.eraseDue(a.getId()));
    }
    @Test void administrativeSuspensionNeverBecomesAutomaticErasure() {
        a.setEnabled(false);users.saveAndFlush(a);
        assertFalse(erasure.eraseDue(a.getId()));assertEquals(1,users.countAllByEmail(a.getEmail()));
    }
    @Test void erasureRefusesSharedFilesAndKeepsAddressReserved() throws Exception {
        var doc=document(a);var shared=document(b);
        shared.setStoredFileName(doc.getStoredFileName());documents.saveAndFlush(shared);
        requestDeletion(login(a));makeDeletionDue(a.getId());
        assertThrows(IllegalStateException.class,()->erasure.eraseDue(a.getId()));
        assertEquals(1,users.countAllByEmail(a.getEmail()));assertTrue(storage.exists(doc.getStoredFileName()));
        assertFalse(users.findById(a.getId()).orElseThrow().getEnabled());
    }
    @org.springframework.test.context.bean.override.mockito.MockitoBean
    com.orientation.orientationapp.modules.auth.service.GoogleIdentityVerifier deletionGoogle;
    @Test void googleDeletionMustMatchTheLinkedSubjectOfTheCurrentCandidate() throws Exception {
        String own="own-"+a.getId(),other="other-"+b.getId();
        jdbc.update("INSERT INTO auth_external_identities(issuer,subject,user_id) VALUES ('https://accounts.google.com',?,?)",own,a.getId());
        jdbc.update("INSERT INTO auth_external_identities(issuer,subject,user_id) VALUES ('https://accounts.google.com',?,?)",other,b.getId());
        org.mockito.Mockito.when(deletionGoogle.verify("other-token")).thenReturn(new com.orientation.orientationapp.modules.auth.service.GoogleIdentityVerifier.Identity(other,b.getEmail(),"Other","Candidate"));
        org.mockito.Mockito.when(deletionGoogle.verify("own-token")).thenReturn(new com.orientation.orientationapp.modules.auth.service.GoogleIdentityVerifier.Identity(own,a.getEmail(),"Own","Candidate"));
        var session=login(a);
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/auth/me/deletion-request")
            .header("Authorization","Bearer "+session.getAccessToken()).contentType("application/json")
            .content(json.writeValueAsString(java.util.Map.of("confirmation","SUPPRIMER","googleIdToken","other-token"))))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isBadRequest());
        assertTrue(users.findById(a.getId()).orElseThrow().getEnabled());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/auth/me/deletion-request")
            .header("Authorization","Bearer "+session.getAccessToken()).contentType("application/json")
            .content(json.writeValueAsString(java.util.Map.of("confirmation","SUPPRIMER","googleIdToken","own-token"))))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
        assertFalse(users.findById(a.getId()).orElseThrow().getEnabled());assertTrue(users.findById(b.getId()).orElseThrow().getEnabled());
    }
    @Test void administratorSeesDeletionDeadlineButCannotReactivateThePendingAccount() throws Exception {
        requestDeletion(login(a));
        identity(adminA,"ROLE_ADMIN","USER_READ","USER_UPDATE");
        var view=userAdmin.getById(a.getId());assertNotNull(view.getDeletionRequestedAt());assertNotNull(view.getPurgeAfter());
        assertThrows(com.orientation.orientationapp.exception.BusinessException.class,()->userAdmin.reactivate(a.getId()));
        assertFalse(users.findById(a.getId()).orElseThrow().getEnabled());
    }
    @Autowired com.orientation.orientationapp.modules.admin.service.UserAdminService userAdmin;
    @Test void allMigrationsAndHibernateValidateOnIsolatedPostgres() {
        assertTrue(jdbc.queryForObject("select count(*) from flyway_schema_history where success",Integer.class)>=68);
        assertNotNull(jdbc.queryForObject("select to_regclass('bo_notifications')::text",String.class));
        assertNotNull(jdbc.queryForObject("select to_regclass('audit_events')::text",String.class));
        assertEquals(1,jdbc.queryForObject("select count(*) from information_schema.columns where table_schema=current_schema() and table_name='knowledge_nodes' and column_name='embedding'",Integer.class));
    }

    @Test void beninRegistryKeepsUnknownCitiesAndEditorialReviewWithoutDuplicatingSeeds() throws Exception {
        String source="https://enseignementsuperieur.gouv.bj/uploads/document_actualite/07ccfec01c103dab63756ab2b15455be.pdf";
        assertEquals(117,jdbc.queryForObject("select count(*) from universities where source_url=? and tenant_id is null",Integer.class,source));
        assertEquals(4,jdbc.queryForObject("select count(*) from universities where source_url=? and institution_type='PUBLIC'",Integer.class,source));
        assertEquals(112,jdbc.queryForObject("select count(*) from universities where source_url=? and institution_type='PRIVATE'",Integer.class,source));
        assertEquals(0,jdbc.queryForObject("select count(*) from universities where source_url=? and verified",Integer.class,source));
        assertTrue(jdbc.queryForObject("select count(*) from universities where source_url=? and city_id is null",Integer.class,source)>100);
        var session=login(superUser);
        var institution=jdbc.queryForObject("select id from universities where short_name='UNSTIM' and tenant_id is null",java.util.UUID.class);
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/universities/"+institution)
            .header("Authorization","Bearer "+session.getAccessToken()))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.shortName").value("UNSTIM"))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.cityId").doesNotExist());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/locations/cities")
            .header("Authorization","Bearer "+session.getAccessToken()))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
    }

    @Autowired com.orientation.orientationapp.modules.auth.repository.UserRepository users;
    @Autowired com.orientation.orientationapp.modules.auth.repository.RoleRepository roles;
    @Autowired com.orientation.orientationapp.modules.auth.repository.TenantRepository tenants;
    @Autowired com.orientation.orientationapp.modules.auth.repository.OrganizationRepository organizations;
    @Autowired com.orientation.orientationapp.modules.user.repository.CandidateRepository candidates;
    @Autowired com.orientation.orientationapp.modules.transcript.repository.TranscriptRepository transcripts;
    @Autowired com.orientation.orientationapp.modules.orientation.repository.AcademicYearRepository years;
    @Autowired com.orientation.orientationapp.docintel.document.repository.DocumentRepository documents;
    @Autowired com.orientation.orientationapp.backoffice.notification_center.repository.NotificationRepository notifications;
    @Autowired com.orientation.orientationapp.modules.auth.repository.RefreshTokenRepository refreshTokens;
    @Autowired com.orientation.orientationapp.modules.auth.repository.AccountTokenRepository accountTokens;
    @Autowired com.orientation.orientationapp.modules.auth.service.AuthenticationService authentication;
    @org.springframework.test.context.bean.override.mockito.MockitoSpyBean
    com.orientation.orientationapp.modules.auth.service.EmailService emailProvider;
    @Autowired org.springframework.core.env.Environment environment;
    @Autowired com.orientation.orientationapp.modules.auth.service.AccountRecoveryService recovery;
    @Autowired com.orientation.orientationapp.modules.auth.service.impl.RefreshTokenServiceImpl sessions;
    @Autowired com.orientation.orientationapp.security.service.ResourceAccess access;
    @Autowired org.springframework.security.crypto.password.PasswordEncoder encoder;
    @Autowired com.fasterxml.jackson.databind.ObjectMapper json;
    @Autowired org.springframework.transaction.support.TransactionTemplate transactions;
    private com.orientation.orientationapp.modules.auth.entity.User a,b,adminA,adminB,superUser;
    private com.orientation.orientationapp.modules.user.entity.Candidate ca,cb;
    private com.orientation.orientationapp.modules.auth.entity.Tenant ta,tb;
    private final String password="Only-test-password-42!";
    private com.orientation.orientationapp.modules.auth.entity.Tenant tenant(String suffix) {
        var org=new com.orientation.orientationapp.modules.auth.entity.Organization();
        org.setName("F1 "+suffix);org.setCode("F1-"+java.util.UUID.randomUUID());
        organizations.saveAndFlush(org);
        var tenant=new com.orientation.orientationapp.modules.auth.entity.Tenant();
        tenant.setName("F1 "+suffix);tenant.setCode("F1-"+java.util.UUID.randomUUID());tenant.setOrganization(org);
        return tenants.saveAndFlush(tenant);
    }
    private com.orientation.orientationapp.modules.auth.entity.User user(
            com.orientation.orientationapp.modules.auth.entity.Tenant tenant,String role) {
        var user=new com.orientation.orientationapp.modules.auth.entity.User();
        user.setEmail(java.util.UUID.randomUUID()+"@example.test");user.setFirstName("F1");user.setLastName("Fixture");
        user.setPassword(encoder.encode(password));user.setTenant(tenant);user.setEmailVerified(true);
        var membership=new com.orientation.orientationapp.modules.auth.entity.UserRole();
        membership.setUser(user);membership.setRole(roles.findByCode(role).orElseThrow());user.getUserRoles().add(membership);
        return users.saveAndFlush(user);
    }
    private com.orientation.orientationapp.modules.user.entity.Candidate candidate(
            com.orientation.orientationapp.modules.auth.entity.User user) {
        var c=new com.orientation.orientationapp.modules.user.entity.Candidate();
        c.setFirstName("F1");c.setLastName("Fixture");c.setEmail(user.getEmail());c.setUser(user);c.setTenant(user.getTenant());
        return candidates.saveAndFlush(c);
    }
    @BeforeEach void fixture() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
        ta=tenant("A");tb=tenant("B");a=user(ta,"CANDIDAT");b=user(tb,"CANDIDAT");
        adminA=user(ta,"ADMIN");adminB=user(tb,"ADMIN");superUser=user(ta,"SUPER_ADMIN");
        ca=candidate(a);cb=candidate(b);
    }
    @AfterEach void clearIdentity() { org.springframework.security.core.context.SecurityContextHolder.clearContext(); }
    private com.orientation.orientationapp.modules.auth.dto.response.LoginResponse login(
            com.orientation.orientationapp.modules.auth.entity.User user) throws Exception {
        var result=mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/auth/login")
            .contentType("application/json").content(json.writeValueAsString(java.util.Map.of("email",user.getEmail(),"password",password))))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk()).andReturn();
        return json.readValue(result.getResponse().getContentAsByteArray(),
            com.orientation.orientationapp.modules.auth.dto.response.LoginResponse.class);
    }
    private com.orientation.orientationapp.modules.auth.dto.request.RefreshTokenRequest renewal(String token) {
        var request=new com.orientation.orientationapp.modules.auth.dto.request.RefreshTokenRequest();
        request.setRefreshToken(token);return request;
    }
    private void identity(com.orientation.orientationapp.modules.auth.entity.User user,String... authorities) {
        var details=com.orientation.orientationapp.security.service.CustomUserDetails.builder()
            .userId(user.getId().toString()).email(user.getEmail()).enabled(true)
            .authorities(java.util.Arrays.stream(authorities).map(org.springframework.security.core.authority.SimpleGrantedAuthority::new).toList()).build();
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(details,null,details.getAuthorities()));
    }
    @Test void typedAccessAcceptedButOpaqueRefreshBearerRefused() throws Exception {
        var session=login(a);
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/auth/me")
            .header("Authorization","Bearer "+session.getAccessToken()))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/auth/me")
            .header("Authorization","Bearer "+session.getRefreshToken()))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isUnauthorized());
        assertTrue(refreshTokens.findByToken(
            com.orientation.orientationapp.modules.auth.service.impl.RefreshTokenServiceImpl.digest(session.getRefreshToken())).isPresent());
        assertTrue(refreshTokens.findByToken(session.getRefreshToken()).isEmpty());
    }
    @Test void refreshRotatesAndReplayRevokesItsFamily() throws Exception {
        var original=login(a);
        var rotated=authentication.refreshToken(renewal(original.getRefreshToken()));
        assertNotEquals(original.getRefreshToken(),rotated.getRefreshToken());
        assertThrows(com.orientation.orientationapp.modules.auth.service.impl.InvalidRefreshException.class,
            () -> authentication.refreshToken(renewal(original.getRefreshToken())));
        assertThrows(com.orientation.orientationapp.modules.auth.service.impl.InvalidRefreshException.class,
            () -> authentication.refreshToken(renewal(rotated.getRefreshToken())));
    }
    @Test void logoutRevokesAccessAndRefreshButNotAnotherSession() throws Exception {
        var first=login(a);var second=login(a);
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/auth/logout")
            .header("Authorization","Bearer "+first.getAccessToken()).contentType("application/json")
            .content(json.writeValueAsString(java.util.Map.of("refreshToken",first.getRefreshToken()))))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/auth/me")
            .header("Authorization","Bearer "+first.getAccessToken()))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isUnauthorized());
        assertThrows(com.orientation.orientationapp.modules.auth.service.impl.InvalidRefreshException.class,
            () -> authentication.refreshToken(renewal(first.getRefreshToken())));
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/auth/me")
            .header("Authorization","Bearer "+second.getAccessToken()))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
    }
    @Test void disabledAccountCannotUseAccessOrRefresh() throws Exception {
        var session=login(a);a.setEnabled(false);users.saveAndFlush(a);
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/auth/me")
            .header("Authorization","Bearer "+session.getAccessToken()))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isUnauthorized());
        assertThrows(com.orientation.orientationapp.modules.auth.service.impl.InvalidRefreshException.class,
            () -> authentication.refreshToken(renewal(session.getRefreshToken())));
    }
    @Test void deletedAccountCannotUseAccessOrRefresh() throws Exception {
        var session=login(a);a.setDeleted(true);users.saveAndFlush(a);
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/auth/me")
            .header("Authorization","Bearer "+session.getAccessToken()))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isUnauthorized());
        assertThrows(com.orientation.orientationapp.modules.auth.service.impl.InvalidRefreshException.class,
            () -> authentication.refreshToken(renewal(session.getRefreshToken())));
    }
    @Test void concurrentRenewalIsSerializedAndReplayInvalidatesFamily() throws Exception {
        var session=login(a);
        var pool=java.util.concurrent.Executors.newFixedThreadPool(2);
        var start=new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.Callable<Boolean> task=() -> {
            start.await();
            try { authentication.refreshToken(renewal(session.getRefreshToken()));return true; }
            catch(com.orientation.orientationapp.modules.auth.service.impl.InvalidRefreshException ex) { return false; }
        };
        try {
            var first=pool.submit(task);var second=pool.submit(task);start.countDown();
            assertNotEquals(first.get(20,java.util.concurrent.TimeUnit.SECONDS),second.get(20,java.util.concurrent.TimeUnit.SECONDS));
            var family=refreshTokens.findByToken(
                com.orientation.orientationapp.modules.auth.service.impl.RefreshTokenServiceImpl.digest(session.getRefreshToken())).orElseThrow().getFamilyId();
            assertFalse(sessions.isSessionActive(family,a.getId()));
        } finally { pool.shutdownNow(); }
    }
    @Test void failureCounterCommitsAndLockExpires() throws Exception {
        for(int i=0;i<5;i++) mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/auth/login")
            .contentType("application/json").content(json.writeValueAsString(java.util.Map.of("email",a.getEmail(),"password","incorrect"))))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isUnauthorized());
        var locked=users.findById(a.getId()).orElseThrow();
        assertEquals(5,locked.getFailedLoginAttempts());assertTrue(locked.getAccountLocked());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/auth/login")
            .contentType("application/json").content(json.writeValueAsString(java.util.Map.of("email",a.getEmail(),"password",password))))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isUnauthorized());
        locked.setLockedUntil(java.time.Instant.now().minusSeconds(1));users.saveAndFlush(locked);
        login(a);assertEquals(0,users.findById(a.getId()).orElseThrow().getFailedLoginAttempts());
    }
    @Test void candidateCannotReadOtherCandidateByUuid() throws Exception {
        var session=login(a);
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/candidates/"+ca.getId())
            .header("Authorization","Bearer "+session.getAccessToken()))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/candidates/"+cb.getId())
            .header("Authorization","Bearer "+session.getAccessToken()))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isForbidden());
    }
    @Test void transcriptsAreScopedToCandidateAndTenant() throws Exception {
        var year=years.findByCurrentTrue().orElseThrow();
        var one=new com.orientation.orientationapp.modules.transcript.entity.Transcript();one.setCandidate(ca);one.setAcademicYear(year);
        var two=new com.orientation.orientationapp.modules.transcript.entity.Transcript();two.setCandidate(cb);two.setAcademicYear(year);
        transcripts.saveAndFlush(one);transcripts.saveAndFlush(two);
        var session=login(a);
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/transcripts/"+one.getId())
            .header("Authorization","Bearer "+session.getAccessToken()))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/transcripts/"+two.getId())
            .header("Authorization","Bearer "+session.getAccessToken()))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isForbidden());
    }
    @Test void notificationsCannotSelectAnotherInboxOrMessage() throws Exception {
        var notification=com.orientation.orientationapp.backoffice.notification_center.entity.Notification.builder()
            .type("SYSTEM").title("Private").userId(b.getId().toString()).createdAt(java.time.Instant.now()).build();
        notifications.saveAndFlush(notification);var session=login(a);
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/backoffice/notifications")
            .param("userId",b.getId().toString()).header("Authorization","Bearer "+session.getAccessToken()))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isForbidden());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/backoffice/notifications/"+notification.getId()+"/read")
            .header("Authorization","Bearer "+session.getAccessToken()))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isForbidden());
    }
    @Test void administratorCannotSelectAnotherTenantButExplicitGlobalPermissionWorks() throws Exception {
        var session=login(adminA);
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/admin/users/"+adminB.getId())
            .header("Authorization","Bearer "+session.getAccessToken()))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isForbidden());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/admin/users")
            .param("tenantId",tb.getId().toString()).header("Authorization","Bearer "+session.getAccessToken()))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isForbidden());
        var global=login(superUser);
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/admin/users/"+adminB.getId())
            .header("Authorization","Bearer "+global.getAccessToken()))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
        identity(superUser,"ROLE_SUPER_ADMIN");
        assertThrows(org.springframework.security.access.AccessDeniedException.class,()->access.tenant(tb.getId()));
    }
    @Test void candidateAndModeratorCannotEnterAdministrationAndAnonymousIs401() throws Exception {
        var session=login(a);
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/admin/users")
            .header("Authorization","Bearer "+session.getAccessToken()))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isForbidden());
        var moderator=login(user(ta,"MODERATEUR"));
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/admin/users")
            .header("Authorization","Bearer "+moderator.getAccessToken()))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isForbidden());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/admin/users"))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isUnauthorized());
    }
    @Test void recoveryTokenIsHashedExpiringSingleUseAndRevokesSessions() throws Exception {
        var session=login(a);
        String raw=transactions.execute(tx -> recovery.issue(users.findById(a.getId()).orElseThrow(),
            com.orientation.orientationapp.modules.auth.entity.AccountToken.Purpose.RESET_PASSWORD,3600));
        assertTrue(accountTokens.findByTokenHash(
            com.orientation.orientationapp.modules.auth.service.impl.RefreshTokenServiceImpl.digest(raw)).isPresent());
        recovery.reset(raw,"Changed-only-test-password-43!");
        assertThrows(com.orientation.orientationapp.exception.BusinessException.class,()->recovery.reset(raw,"Changed-only-test-password-44!"));
        assertTrue(encoder.matches("Changed-only-test-password-43!",users.findById(a.getId()).orElseThrow().getPassword()));
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/auth/me")
            .header("Authorization","Bearer "+session.getAccessToken()))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isUnauthorized());
    }
    @Test void recoveryRejectsExpiredAndWrongPurposeTokens() {
        String expired=transactions.execute(tx -> recovery.issue(users.findById(a.getId()).orElseThrow(),
            com.orientation.orientationapp.modules.auth.entity.AccountToken.Purpose.RESET_PASSWORD,-10));
        assertThrows(com.orientation.orientationapp.exception.BusinessException.class,()->recovery.reset(expired,password));
        String verification=transactions.execute(tx -> recovery.issue(users.findById(a.getId()).orElseThrow(),
            com.orientation.orientationapp.modules.auth.entity.AccountToken.Purpose.VERIFY_EMAIL,3600));
        assertThrows(com.orientation.orientationapp.exception.BusinessException.class,()->recovery.reset(verification,password));
    }
    @Test void verificationIsSingleUseAndDoesNotReuseMfaSecret() {
        a.setEmailVerified(false);a.setMfaSecret("test-existing-mfa");users.saveAndFlush(a);
        String raw=transactions.execute(tx -> recovery.issue(users.findById(a.getId()).orElseThrow(),
            com.orientation.orientationapp.modules.auth.entity.AccountToken.Purpose.VERIFY_EMAIL,3600));
        recovery.verify(raw);
        assertTrue(users.findById(a.getId()).orElseThrow().getEmailVerified());
        assertEquals("test-existing-mfa",users.findById(a.getId()).orElseThrow().getMfaSecret());
        assertThrows(com.orientation.orientationapp.exception.BusinessException.class,()->recovery.verify(raw));
    }
    @Test void absentEmailProviderReturns503ForExistingAndUnknownAccounts() throws Exception {
        for(String address:java.util.List.of(a.getEmail(),"unknown@example.test"))
            mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/auth/forgot-password")
                .contentType("application/json").content(json.writeValueAsString(java.util.Map.of("email",address))))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isServiceUnavailable());
    }


    @Test void registrationRollsBackAfterProviderFailureAndCanBeRetriedWithoutSigningIn() throws Exception {
        // The mocked facade never sends an email. PostgreSQL remains the isolated
        // F1 database enforced by this class; HTTP-provider acceptance is tested
        // separately in EmailServiceTest.
        String address=java.util.UUID.randomUUID()+"@example.test";
        String tenantCode=environment.getProperty("app.registration.tenant-code","DEFAULT");
        var publicTenant=tenants.findByCode(tenantCode).orElseGet(()->{
            var created=tenant("Registration");created.setCode(tenantCode);
            return tenants.saveAndFlush(created);
        });
        assertTrue(Boolean.TRUE.equals(publicTenant.getActive()));
        String request=json.writeValueAsString(java.util.Map.of(
            "email",address,"firstName","Registration","lastName","Fixture","password",password));
        var failedOwner=new java.util.concurrent.atomic.AtomicReference<java.util.UUID>();
        org.mockito.Mockito.doNothing().when(emailProvider).requireConfigured();
        org.mockito.Mockito.doAnswer(invocation->{
            java.util.UUID owner=jdbc.queryForObject("select id from users where email=?",java.util.UUID.class,address);
            failedOwner.set(owner);
            assertEquals(1,jdbc.queryForObject("select count(*) from candidates where user_id=?",Integer.class,owner));
            assertEquals(1,jdbc.queryForObject("select count(*) from account_tokens where user_id=?",Integer.class,owner));
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE,
                "Email delivery unavailable; no delivery is confirmed");
        }).when(emailProvider).sendVerificationEmail(
            org.mockito.ArgumentMatchers.eq(address),org.mockito.ArgumentMatchers.anyString(),org.mockito.ArgumentMatchers.anyString());
        try {
            mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/auth/register")
                .contentType("application/json").content(request))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isServiceUnavailable())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.message")
                    .value("Email delivery unavailable; no delivery is confirmed"));
            assertNotNull(failedOwner.get(),"The provider failed after user, candidate and token were inserted");
            assertEquals(0,jdbc.queryForObject("select count(*) from users where email=?",Integer.class,address));
            assertEquals(0,jdbc.queryForObject("select count(*) from candidates where email=?",Integer.class,address));
            assertEquals(0,jdbc.queryForObject("select count(*) from account_tokens where user_id=?",Integer.class,failedOwner.get()));
            assertEquals(0,jdbc.queryForObject("select count(*) from refresh_tokens where user_id=?",Integer.class,failedOwner.get()));

            // Restore only the mocked verification operation, then retry exactly
            // the same registration payload without creating an app session.
            org.mockito.Mockito.doNothing().when(emailProvider).sendVerificationEmail(
                org.mockito.ArgumentMatchers.eq(address),org.mockito.ArgumentMatchers.anyString(),org.mockito.ArgumentMatchers.anyString());
            var accepted=mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/auth/register")
                .contentType("application/json").content(request))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isAccepted())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.accessToken").doesNotExist())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.refreshToken").doesNotExist())
                .andReturn();
            assertNull(accepted.getRequest().getSession(false));
            var registered=users.findByEmail(address).orElseThrow();
            assertEquals(publicTenant.getId(),registered.getTenant().getId());
            assertFalse(Boolean.TRUE.equals(registered.getEmailVerified()));
            assertEquals(1,jdbc.queryForObject("select count(*) from users where email=?",Integer.class,address));
            assertEquals(1,jdbc.queryForObject("select count(*) from candidates where user_id=? and not verified",Integer.class,registered.getId()));
            assertEquals(1,jdbc.queryForObject("select count(*) from account_tokens where user_id=? and purpose='VERIFY_EMAIL' and not consumed",Integer.class,registered.getId()));
            assertEquals(0,jdbc.queryForObject("select count(*) from refresh_tokens where user_id=?",Integer.class,registered.getId()));
            mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/auth/me"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isUnauthorized());
            org.mockito.Mockito.verify(emailProvider,org.mockito.Mockito.times(2)).sendVerificationEmail(
                org.mockito.ArgumentMatchers.eq(address),org.mockito.ArgumentMatchers.anyString(),org.mockito.ArgumentMatchers.anyString());
        } finally {
            // Preserve real disabled-provider behavior for every other test.
            org.mockito.Mockito.reset(emailProvider);
        }
    }


    @Test void officialPublicFacultiesAreAssociatedWithoutInventingLocationsOrPublishingData() throws Exception {
        assertEquals(66,jdbc.queryForObject("select count(*) from faculties where registry_source_url is not null and not deleted",Integer.class));
        assertEquals(40,jdbc.queryForObject("select count(*) from faculties f join campuses c on c.id=f.campus_id join universities u on u.id=c.university_id where f.registry_source_url is not null and u.short_name='UAC'",Integer.class));
        assertEquals(0,jdbc.queryForObject("select count(*) from faculties f join campuses c on c.id=f.campus_id join universities u on u.id=c.university_id join countries p on p.id=u.country_id where f.registry_source_url is not null and p.code<>'BEN'",Integer.class));
        assertTrue(jdbc.queryForObject("select count(*) from campuses where city_id is null and not verified",Integer.class)>0);
        var session=login(superUser);
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/faculties").param("size","100").param("registryOnly","true")
            .header("Authorization","Bearer "+session.getAccessToken()))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.totalElements").value(66));
    }

    @Test void mobileCodeVerificationCommitsWrongAttemptsThenVerifiesOnceWithoutOpeningSession() throws Exception {
        String address=java.util.UUID.randomUUID()+"@example.test";
        var tenantCode=environment.getProperty("app.registration.tenant-code","DEFAULT");
        tenants.findByCode(tenantCode).orElseGet(()->{var created=tenant("Mobile registration");created.setCode(tenantCode);return tenants.saveAndFlush(created);});
        var received=new java.util.concurrent.atomic.AtomicReference<String>();
        org.mockito.Mockito.doNothing().when(emailProvider).requireConfigured();
        org.mockito.Mockito.doAnswer(invocation->{received.set(invocation.getArgument(2));return null;}).when(emailProvider)
            .sendVerificationCode(org.mockito.ArgumentMatchers.eq(address),org.mockito.ArgumentMatchers.anyString(),org.mockito.ArgumentMatchers.anyString());
        try {
            String payload=json.writeValueAsString(java.util.Map.of("email",address,"firstName","Mobile","lastName","Fixture","password",password));
            var result=mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/auth/mobile/register").contentType("application/json").content(payload))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isAccepted())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.accessToken").doesNotExist()).andReturn();
            assertNotNull(received.get());assertTrue(received.get().matches("[0-9]{6}"));
            String id=json.readTree(result.getResponse().getContentAsString()).path("challengeId").asText();
            String wrong=received.get().equals("000000")?"000001":"000000";
            mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/auth/mobile/verify-email").contentType("application/json")
                .content(json.writeValueAsString(java.util.Map.of("challengeId",id,"code",wrong))))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isBadRequest());
            assertEquals(1,jdbc.queryForObject("select attempts from email_verification_challenges where id=?",Integer.class,java.util.UUID.fromString(id)));
            assertFalse(Boolean.TRUE.equals(users.findByEmail(address).orElseThrow().getEmailVerified()));
            mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/auth/mobile/verify-email").contentType("application/json")
                .content(json.writeValueAsString(java.util.Map.of("challengeId",id,"code",received.get()))))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
            assertTrue(Boolean.TRUE.equals(users.findByEmail(address).orElseThrow().getEmailVerified()));
            assertEquals(0,jdbc.queryForObject("select count(*) from refresh_tokens where user_id=?",Integer.class,users.findByEmail(address).orElseThrow().getId()));
            mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/auth/mobile/verify-email").contentType("application/json")
                .content(json.writeValueAsString(java.util.Map.of("challengeId",id,"code",received.get()))))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isBadRequest());
            var candidateSession=authentication.login(com.orientation.orientationapp.modules.auth.dto.request.LoginRequest.builder().email(address).password(password).build(),"127.0.0.1","isolated-fixture");
            mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/backoffice/dashboard")
                .header("Authorization","Bearer "+candidateSession.getAccessToken()))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isForbidden());
            mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/admin/users")
                .header("Authorization","Bearer "+candidateSession.getAccessToken()))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isForbidden());
        } finally {org.mockito.Mockito.reset(emailProvider);}
    }

    @Autowired com.orientation.orientationapp.docintel.document.service.DocumentService documentService;
    @Autowired com.orientation.orientationapp.storage.service.StorageService storage;
    private com.orientation.orientationapp.docintel.document.entity.Document document(
            com.orientation.orientationapp.modules.auth.entity.User owner) {
        var file=new org.springframework.mock.web.MockMultipartFile("file","private.pdf","application/pdf","%PDF-1.4\n".getBytes());
        var d=new com.orientation.orientationapp.docintel.document.entity.Document();
        d.setTitle("Private F1");d.setStoredFileName(storage.store(file));d.setOriginalFileName("private.pdf");
        d.setMimeType("application/pdf");d.setFileSize(file.getSize());d.setChecksum("test-only-checksum");
        d.setTenantId(owner.getTenant().getId().toString());d.setUploadedBy(owner.getId().toString());
        return documents.saveAndFlush(d);
    }
    @Test void documentReadWriteDownloadAndSearchRespectOwnershipAndTenant() throws Exception {
        var docA=document(a);var docB=document(b);var session=login(a);
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/documents/"+docA.getId()+"/download")
            .header("Authorization","Bearer "+session.getAccessToken()))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/documents/"+docB.getId()+"/download")
            .header("Authorization","Bearer "+session.getAccessToken()))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isForbidden());
        identity(a,"ROLE_CANDIDAT");
        assertThrows(org.springframework.security.access.AccessDeniedException.class,()->documentService.delete(docB.getId()));
        var admin=login(adminA);
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/documents")
            .param("search","Private F1").param("tenantId",tb.getId().toString())
            .header("Authorization","Bearer "+admin.getAccessToken()))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isForbidden());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/documents/"+docB.getId())
            .header("Authorization","Bearer "+admin.getAccessToken()))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isForbidden());
    }
    @Test void ownerCheckAlsoAppliesWithinSameTenant() throws Exception {
        var another=user(ta,"CANDIDAT");var other=candidate(another);var session=login(a);
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/candidates/"+other.getId())
            .header("Authorization","Bearer "+session.getAccessToken()))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isForbidden());
    }
    @Test void logoutCanRevokeFamilyWhileAnOldRefreshWasJustRotated() throws Exception {
        var first=login(a);
        var rotated=authentication.refreshToken(renewal(first.getRefreshToken()));
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/auth/logout")
            .header("Authorization","Bearer "+first.getAccessToken()).contentType("application/json")
            .content(json.writeValueAsString(java.util.Map.of("refreshToken",first.getRefreshToken()))))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
        assertThrows(com.orientation.orientationapp.modules.auth.service.impl.InvalidRefreshException.class,
            () -> authentication.refreshToken(renewal(rotated.getRefreshToken())));
    }
    @Test void feedbackAndHistoryCannotForgeCandidateId() throws Exception {
        var session=login(a);
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/intelligence/history/"+cb.getId())
            .header("Authorization","Bearer "+session.getAccessToken()))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isForbidden());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/intelligence/feedback")
            .header("Authorization","Bearer "+session.getAccessToken()).contentType("application/json")
            .content(json.writeValueAsString(java.util.Map.of("candidateId",cb.getId(),"programId",java.util.UUID.randomUUID(),
                "recommendationId",java.util.UUID.randomUUID(),"action","ACCEPTED"))))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isForbidden());
    }


    @Test void concurrentRecoveryTokenConsumptionAllowsExactlyOneReset() throws Exception {
        String raw=transactions.execute(tx -> recovery.issue(users.findById(a.getId()).orElseThrow(),
            com.orientation.orientationapp.modules.auth.entity.AccountToken.Purpose.RESET_PASSWORD,3600));
        var start=new java.util.concurrent.CountDownLatch(1);
        var pool=java.util.concurrent.Executors.newFixedThreadPool(2);
        try {
            java.util.concurrent.Callable<Boolean> action=() -> {
                start.await();
                try { recovery.reset(raw,"New-recovery-password-42!");return true; }
                catch(com.orientation.orientationapp.exception.BusinessException ex) { return false; }
            };
            var first=pool.submit(action);var second=pool.submit(action);start.countDown();
            assertEquals(1,(first.get(30,java.util.concurrent.TimeUnit.SECONDS)?1:0)
                +(second.get(30,java.util.concurrent.TimeUnit.SECONDS)?1:0));
        } finally { pool.shutdownNow(); }
    }
    @Test void passwordChangeRevokesCurrentAndParallelSessions() throws Exception {
        var first=login(a);var second=login(a);
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/auth/me/password")
            .header("Authorization","Bearer "+first.getAccessToken()).contentType("application/json")
            .content(json.writeValueAsString(java.util.Map.of("currentPassword",password,"newPassword","Changed-password-42!"))))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/auth/me")
            .header("Authorization","Bearer "+second.getAccessToken()))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isUnauthorized());
        assertThrows(com.orientation.orientationapp.modules.auth.service.impl.InvalidRefreshException.class,
            () -> authentication.refreshToken(renewal(first.getRefreshToken())));
        assertThrows(com.orientation.orientationapp.modules.auth.service.impl.InvalidRefreshException.class,
            () -> authentication.refreshToken(renewal(second.getRefreshToken())));
    }
    @Test void dashboardSelectorIsScopedAndStatisticsCountOnlyOwnCandidates() throws Exception {
        var session=login(adminA);
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/backoffice/dashboard")
            .header("Authorization","Bearer "+session.getAccessToken()))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.kpis.totalCandidates").value(1));
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/backoffice/dashboard/tenant/"+tb.getId())
            .header("Authorization","Bearer "+session.getAccessToken()))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isForbidden());
    }

    @Autowired com.orientation.orientationapp.modules.auth.service.impl.LoginAttemptService attempts;
    @Test void concurrentFailuresDoNotLoseCounterUpdates() throws Exception {
        var start=new java.util.concurrent.CountDownLatch(1);
        var pool=java.util.concurrent.Executors.newFixedThreadPool(5);
        try {
            var jobs=new java.util.ArrayList<java.util.concurrent.Future<?>>();
            for(int i=0;i<5;i++) jobs.add(pool.submit(() -> {
                try { start.await();attempts.failed(a.getEmail()); }
                catch(InterruptedException ex) { Thread.currentThread().interrupt();throw new RuntimeException(ex); }
            }));
            start.countDown();
            for(var job:jobs) job.get(30,java.util.concurrent.TimeUnit.SECONDS);
            var locked=users.findById(a.getId()).orElseThrow();
            assertEquals(5,locked.getFailedLoginAttempts());assertTrue(locked.getAccountLocked());
        } finally { pool.shutdownNow(); }
    }

}
