package com.orientation.orientationapp.modules.admin.service;

import com.orientation.orientationapp.modules.admin.dto.request.CreateTenantRequest;
import com.orientation.orientationapp.modules.admin.dto.response.TenantResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TenantService {
    TenantResponse create(CreateTenantRequest request);
    TenantResponse update(UUID id, CreateTenantRequest request);
    TenantResponse getById(UUID id);
    Page<TenantResponse> list(UUID organizationId, String search, Pageable pageable);
    void activate(UUID id);
    void suspend(UUID id);
    void delete(UUID id);
}
