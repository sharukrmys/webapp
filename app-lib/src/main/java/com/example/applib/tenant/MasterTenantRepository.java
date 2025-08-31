package com.example.applib.tenant;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MasterTenantRepository extends JpaRepository<MasterTenant, Long> {

    Optional<MasterTenant> findByTenantId(String tenantId);

    Optional<MasterTenant> findByTenantIdAndIsActiveTrue(String tenantId);

    List<MasterTenant> findByIsActiveTrue();
}
