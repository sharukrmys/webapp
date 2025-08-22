package com.example.applib.repository;

import com.example.applib.entity.MasterTenant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MasterTenantRepository extends JpaRepository<MasterTenant, Long> {
    
    Optional<MasterTenant> findByTenantId(String tenantId);
    
    boolean existsByTenantId(String tenantId);
}
