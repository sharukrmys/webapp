package com.example.applib.tenant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MasterTenantRepository extends JpaRepository<MasterTenant, Long> {
    
    Optional<MasterTenant> findByTenantId(String tenantId);
    
    Optional<MasterTenant> findByTenantIdAndIsActiveTrue(String tenantId);
}

