package com.orientation.orientationapp.backoffice.analytics.service.impl;
import com.orientation.orientationapp.backoffice.analytics.dto.response.AnalyticsResponse;
import com.orientation.orientationapp.backoffice.analytics.service.AnalyticsService;
import com.orientation.orientationapp.security.service.ResourceAccess;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.UUID;
@Service @RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {
 private final ResourceAccess access;
 public AnalyticsResponse getAnalytics(){return unavailable(access.tenant(null));}
 public AnalyticsResponse getAnalyticsByTenant(String tenantId){return unavailable(access.tenant(UUID.fromString(tenantId)));}
 private AnalyticsResponse unavailable(UUID tenant) {
  return AnalyticsResponse.builder().tenantId(tenant).available(false)
   .unavailableReason("Indicateurs indisponibles : aucune mesure validee des admissions, de la satisfaction ou des classements.").build();
 }
}
