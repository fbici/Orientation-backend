package com.orientation.orientationapp.backoffice.analytics.service;

import com.orientation.orientationapp.backoffice.analytics.dto.response.AnalyticsResponse;

public interface AnalyticsService {
    AnalyticsResponse getAnalytics();
    AnalyticsResponse getAnalyticsByTenant(String tenantId);
}
