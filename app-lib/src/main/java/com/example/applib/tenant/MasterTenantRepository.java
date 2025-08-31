package com.example.applib.tenant;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for master tenant entities.
 */
@Repository
public interface MasterTenantRepository extends JpaRepository<MasterTenant, Long> {

    /**
     * Finds a master tenant by tenant ID.
     *
     * @param tenantId The tenant ID
     * @return An optional containing the master tenant, or empty if not found
     */
    Optional<MasterTenant> findByTenantId(String tenantId);

    /**
     * Checks if a tenant exists by tenant ID.
     *
     * @param tenantId The tenant ID
     * @return True if the tenant exists, false otherwise
     */
    boolean existsByTenantId(String tenantId);
}
