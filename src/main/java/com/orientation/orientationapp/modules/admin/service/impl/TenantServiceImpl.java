package com.orientation.orientationapp.modules.admin.service.impl;

import com.orientation.orientationapp.modules.admin.dto.request.CreateTenantRequest;
import com.orientation.orientationapp.modules.admin.dto.response.TenantResponse;
import com.orientation.orientationapp.modules.admin.service.TenantService;
import com.orientation.orientationapp.modules.auth.entity.Organization;
import com.orientation.orientationapp.modules.auth.entity.Tenant;
import com.orientation.orientationapp.modules.auth.repository.OrganizationRepository;
import com.orientation.orientationapp.modules.auth.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantServiceImpl implements TenantService {

    private final TenantRepository tenantRepository;
    private final OrganizationRepository organizationRepository;

    public TenantResponse create(CreateTenantRequest request) {
        Organization org = organizationRepository.findById(request.getOrganizationId())
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        if (tenantRepository.existsByCode(request.getCode())) {
            throw new RuntimeException("Tenant code already exists");
        }

        Tenant tenant = Tenant.builder()
                .name(request.getName())
                .code(request.getCode())
                .organization(org)
                .description(request.getDescription())
                .active(true)
                .build();

        Tenant saved = tenantRepository.save(tenant);
        return mapToResponse(saved);
    }

    public TenantResponse update(UUID id, CreateTenantRequest request) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        Organization org = organizationRepository.findById(request.getOrganizationId())
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        tenant.setName(request.getName());
        tenant.setCode(request.getCode());
        tenant.setOrganization(org);
        tenant.setDescription(request.getDescription());

        Tenant saved = tenantRepository.save(tenant);
        return mapToResponse(saved);
    }

    public TenantResponse getById(UUID id) {
        return mapToResponse(tenantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tenant not found")));
    }

    public Page<TenantResponse> list(UUID organizationId, String search, Pageable pageable) {
        if (organizationId != null) {
            return tenantRepository.findByOrganizationId(organizationId)
                    .stream()
                    .map(this::mapToResponse)
                    .collect(java.util.stream.Collectors.collectingAndThen(
                            java.util.stream.Collectors.toList(),
                            list -> new org.springframework.data.domain.PageImpl<>(list, pageable, list.size())
                    ));
        }
        return tenantRepository.findAll(pageable).map(this::mapToResponse);
    }

    public void activate(UUID id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        tenant.setActive(true);
        tenantRepository.save(tenant);
    }

    public void suspend(UUID id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        tenant.setActive(false);
        tenantRepository.save(tenant);
    }

    public void delete(UUID id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        tenant.setDeleted(true);
        tenantRepository.save(tenant);
    }

    private TenantResponse mapToResponse(Tenant tenant) {
        return TenantResponse.builder()
                .id(tenant.getId())
                .name(tenant.getName())
                .code(tenant.getCode())
                .organizationId(tenant.getOrganization().getId())
                .organizationName(tenant.getOrganization().getName())
                .description(tenant.getDescription())
                .active(tenant.getActive())
                .build();
    }
}
