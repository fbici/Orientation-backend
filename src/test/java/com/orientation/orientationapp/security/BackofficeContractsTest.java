package com.orientation.orientationapp.security;
import com.orientation.orientationapp.modules.admin.controller.UserController;
import com.orientation.orientationapp.modules.admin.service.UserAdminService;
import com.orientation.orientationapp.modules.admin.dto.request.UpdateUserRequest;
import com.orientation.orientationapp.backoffice.monitoring.service.impl.MonitoringServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import javax.sql.DataSource;
import java.sql.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
class BackofficeContractsTest {
 private static final String BODY="{\"email\":\"fixture@example.invalid\",\"firstName\":\"Test\",\"lastName\":\"Account\",\"tenantId\":\"00000000-0000-0000-0000-000000000001\"}";
 @Test void coordinateUpdateDoesNotRequireOrChangeAPassword() throws Exception {
  var service=mock(UserAdminService.class);
  MockMvcBuilders.standaloneSetup(new UserController(service)).build().perform(put("/admin/users/00000000-0000-0000-0000-000000000002").contentType("application/json").content(BODY)).andExpect(status().isOk());
  verify(service).update(any(),any(UpdateUserRequest.class));
 }
 @Test void creationStillRequiresItsOwnSecret() throws Exception {
  var service=mock(UserAdminService.class);
  MockMvcBuilders.standaloneSetup(new UserController(service)).build().perform(post("/admin/users").contentType("application/json").content(BODY)).andExpect(status().isBadRequest());verifyNoInteractions(service);
 }
 @Test void updateStillValidatesTheEmail() throws Exception {
  var service=mock(UserAdminService.class);
  MockMvcBuilders.standaloneSetup(new UserController(service)).build().perform(put("/admin/users/00000000-0000-0000-0000-000000000002").contentType("application/json").content(BODY.replace("fixture@example.invalid","invalid"))).andExpect(status().isBadRequest());verifyNoInteractions(service);
 }
 @Test void monitoringUsesTheActualConnectionAndDoesNotInventApiStatistics() throws Exception {
  var source=mock(DataSource.class);var connection=mock(Connection.class);when(source.getConnection()).thenReturn(connection);when(connection.isValid(2)).thenReturn(true);
  var result=new MonitoringServiceImpl(source).getMonitoringData();assertEquals("UP",result.getHealthChecks().get(0).getStatus());
  assertNull(result.getApplicationMetrics().getAverageResponseTime());assertNull(result.getApplicationMetrics().getActiveSessions());verify(connection).close();
 }
 @Test void unavailableDatabaseIsNotReportedAsHealthy() throws Exception {
  var source=mock(DataSource.class);when(source.getConnection()).thenThrow(new SQLException("private diagnostic"));
  var result=new MonitoringServiceImpl(source).getMonitoringData();assertEquals("DOWN",result.getHealthChecks().get(0).getStatus());assertFalse(result.getHealthChecks().get(0).getMessage().contains("private diagnostic"));
 }
}
