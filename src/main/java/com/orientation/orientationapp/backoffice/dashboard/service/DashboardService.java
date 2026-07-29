package com.orientation.orientationapp.backoffice.dashboard.service;

import com.orientation.orientationapp.backoffice.dashboard.dto.response.DashboardResponse;

public interface DashboardService {
    DashboardResponse getExecutiveDashboard();
    DashboardResponse getDashboardByTenant(String tenantId);
}
