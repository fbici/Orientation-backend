package com.orientation.orientationapp.modules.admin.service;

import com.orientation.orientationapp.modules.admin.dto.request.CreateOrganizationRequest;
import com.orientation.orientationapp.modules.admin.dto.response.OrganizationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface OrganizationService {
    OrganizationResponse create(CreateOrganizationRequest request);
    OrganizationResponse update(UUID id, CreateOrganizationRequest request);
    OrganizationResponse getById(UUID id);
    Page<OrganizationResponse> list(String search, Pageable pageable);
    void activate(UUID id);
    void suspend(UUID id);
    void archive(UUID id);
    void delete(UUID id);
}
