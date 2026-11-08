package com.orientation.orientationapp.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Real Dart client over loopback HTTP; never uses the main datasource. */
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.DEFINED_PORT, properties="server.port=59833")
@AutoConfigureMockMvc
@ActiveProfiles("f1-test")
class MobileFlutterJourneyIT {
 @DynamicPropertySource static void isolated(DynamicPropertyRegistry p) {
  String url=System.getProperty("f1.jdbc.url", "");
  if(!url.equals("jdbc:postgresql://127.0.0.1:59745/orientia_f1"))
   throw new IllegalStateException("Only the private F3.3 PostgreSQL instance is allowed");
  if(!System.getProperty("f1.jdbc.schema", "").startsWith("f33_"))
   throw new IllegalStateException("A dedicated F3.3 schema is required");
  FoundationsIntegrationTest.isolated(p);
 }
 @Autowired ApplicationContext context;
 @Test void realFlutterClientCompletesTheCandidateJourney() throws Exception {
  var f=new AcademicWorkflowIntegrationTest();
  context.getAutowireCapableBeanFactory().autowireBean(f);
  f.setup();
  f.candidateFlow(true);
  // Publish a new reviewed, source-backed version after the historical tests archive theirs.
  f.upload();var facts=f.validateAll();f.addFundingFacts(facts);
  f.mvc.perform(post("/documents/"+f.doc+"/publish").header("Authorization","Bearer "+f.token)
   .contentType("application/json").content(f.json.writeValueAsBytes(f.publication(facts))))
   .andExpect(status().isOk());
  UUID ownerId=UUID.fromString(f.jwt.getSubjectFromToken(f.candidate));
  String email=f.users.findById(ownerId).orElseThrow().getEmail();
  String otherEmail=f.jdbc.queryForObject("select u.email from candidates c join users u on u.id=c.user_id where u.id<>? and c.tenant_id<>(select tenant_id from users where id=?) limit 1",String.class,ownerId,ownerId);
  Path folder=Files.createTempDirectory("orientia-f33-live-");
  Path pdf=folder.resolve("synthetic-school.pdf"),config=folder.resolve("fixture.json");
  Path log=Path.of(System.getProperty("java.io.tmpdir"),"orientia-f33-live-flutter.log");
  Files.write(pdf,f.schoolPdf());
  Files.writeString(config,f.json.writeValueAsString(Map.of("url","http://127.0.0.1:59833/api/v1","email",email,
   "otherEmail",otherEmail,"password","F2-test-password-42!","pdf",pdf.toString())));
  Process process=null;
  try {
   var builder=new ProcessBuilder("cmd.exe","/d","/c","C:\\flutter\\bin\\flutter.bat","test","test_live/backend_journey_test.dart","--reporter","expanded");
   builder.directory(Path.of("C:\\Users\\HP\\Documents\\projets\\Orientation-mobile").toFile());
   builder.environment().put("F33_LIVE_FIXTURE",config.toString());
   builder.redirectErrorStream(true);builder.redirectOutput(log.toFile());
   process=builder.start();
   assertTrue(process.waitFor(5,TimeUnit.MINUTES),"Dart journey timed out; see private test log");
   assertEquals(0,process.exitValue(),"Dart journey failed; see orientia-f33-live-flutter.log");
  } finally {
   if(process!=null&&process.isAlive())process.destroyForcibly();
   Files.deleteIfExists(config);Files.deleteIfExists(pdf);Files.deleteIfExists(folder);
  }
 }
}
