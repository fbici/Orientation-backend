package com.orientation.orientationapp.modules.admin.service.impl;

import com.orientation.orientationapp.modules.admin.dto.request.CreateOrganizationRequest;
import com.orientation.orientationapp.modules.admin.dto.response.OrganizationResponse;
import com.orientation.orientationapp.modules.admin.service.OrganizationService;
import com.orientation.orientationapp.modules.auth.entity.Organization;
import com.orientation.orientationapp.modules.auth.repository.OrganizationRepository;
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
public class OrganizationServiceImpl implements OrganizationService {

    private final OrganizationRepository organizationRepository;

    public OrganizationResponse create(CreateOrganizationRequest request) {
        if (organizationRepository.existsByName(request.getName())) {
            throw new RuntimeException("Organization name already exists");
        }
        Organization org = Organization.builder()
                .name(request.getName())
                .code(request.getCode())
                .description(request.getDescription())
                .email(request.getEmail())
                .phone(request.getPhone())
                .website(request.getWebsite())
                .active(true)
                .build();
        Organization saved = organizationRepository.save(org);
        return mapToResponse(saved);
    }

    public OrganizationResponse update(UUID id, CreateOrganizationRequest request) {
        Organization org = organizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organization not found"));
        org.setName(request.getName());
        org.setCode(request.getCode());
        org.setDescription(request.getDescription());
        org.setEmail(request.getEmail());
        org.setPhone(request.getPhone());
        org.setWebsite(request.getWebsite());
        return mapToResponse(organizationRepository.save(org));
    }

    public OrganizationResponse getById(UUID id) {
        return mapToResponse(organizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organization not found")));
    }

    public Page<OrganizationResponse> list(String search, Pageable pageable) {
        return organizationRepository.findAll(pageable).map(this::mapToResponse);
    }

    public void activate(UUID id) {
        Organization org = organizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organization not found"));
        org.setActive(true);
        organizationRepository.save(org);
    }

    public void suspend(UUID id) {
        Organization org = organizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organization not found"));
        org.setActive(false);
        organizationRepository.save(org);
    }

    public void archive(UUID id) {
        Organization org = organizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organization not found"));
        org.setActive(false);
        org.setDeleted(true);
        organizationRepository.save(org);
    }

    public void delete(UUID id) {
        Organization org = organizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organization not found"));
        org.setDeleted(true);
        organizationRepository.save(org);
    }

    private OrganizationResponse mapToResponse(Organization org) {
        return OrganizationResponse.builder()
                .id(org.getId())
                .name(org.getName())
                .code(org.getCode())
                .description(org.getDescription())
                .email(org.getEmail())
                .phone(org.getPhone())
                .website(org.getWebsite())
                .active(org.getActive())
                .build();
    }
}
