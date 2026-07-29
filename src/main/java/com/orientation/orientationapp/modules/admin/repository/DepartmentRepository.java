package com.orientation.orientationapp.modules.admin.repository;

import com.orientation.orientationapp.modules.admin.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, UUID>, JpaSpecificationExecutor<Department> {
    List<Department> findByOrganizationIdAndParentIsNull(UUID organizationId);
    List<Department> findByTenantIdAndParentIsNull(UUID tenantId);
    List<Department> findByParentId(UUID parentId);
}
