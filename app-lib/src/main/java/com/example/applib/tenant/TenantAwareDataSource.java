package com.example.applib.tenant;

import com.zaxxer.hikari.HikariDataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Component for managing tenant-aware data sources.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TenantAwareDataSource {

    private final MasterTenantRepository masterTenantRepository;
    private final Map<String, DataSource> tenantDataSources = new HashMap<>();

    @Value("${tenant.default-tenant:default}")
    private String defaultTenant;

    /**
     * Gets the data source for a specific tenant.
     *
     * @param tenantId The tenant ID
     * @return The data source for the tenant
     */
    public DataSource getDataSource(String tenantId) {
        if (tenantDataSources.containsKey(tenantId)) {
            return tenantDataSources.get(tenantId);
        }

        DataSource dataSource = createDataSource(tenantId);
        if (dataSource != null) {
            tenantDataSources.put(tenantId, dataSource);
        }

        return dataSource;
    }

    /**
     * Creates a data source for a specific tenant.
     *
     * @param tenantId The tenant ID
     * @return The data source for the tenant
     */
    private DataSource createDataSource(String tenantId) {
        try {
            // Find the tenant in the master database
            Optional<MasterTenant> optionalTenant = masterTenantRepository.findByTenantId(tenantId);

            if (optionalTenant.isPresent() && optionalTenant.get().isActive()) {
                MasterTenant tenant = optionalTenant.get();

                // Create a new data source for the tenant
                HikariDataSource dataSource = new HikariDataSource();
                dataSource.setJdbcUrl(tenant.getUrl());
                dataSource.setUsername(tenant.getUsername());
                dataSource.setPassword(tenant.getPassword());
                dataSource.setDriverClassName("org.postgresql.Driver");

                // Configure connection pool settings
                dataSource.setMinimumIdle(tenant.getMinIdle() != null ? tenant.getMinIdle() : 1);
                dataSource.setMaximumPoolSize(tenant.getMaxPoolSize() != null ? tenant.getMaxPoolSize() : 5);
                dataSource.setConnectionTimeout(tenant.getConnectionTimeout() != null ? tenant.getConnectionTimeout() : 30000);
                dataSource.setIdleTimeout(tenant.getIdleTimeout() != null ? tenant.getIdleTimeout() : 600000);
                dataSource.setPoolName("HikariPool-" + tenant.getTenantId());

                log.info("Created data source for tenant {}", tenant.getTenantId());
                return dataSource;
            } else {
                log.warn("Tenant {} not found or not active", tenantId);
                return getDefaultDataSource();
            }
        } catch (Exception e) {
            log.error("Error creating data source for tenant {}: {}", tenantId, e.getMessage(), e);
            return getDefaultDataSource();
        }
    }

    /**
     * Gets the default data source.
     *
     * @return The default data source
     */
    public DataSource getDefaultDataSource() {
        if (tenantDataSources.containsKey(defaultTenant)) {
            return tenantDataSources.get(defaultTenant);
        }

        // Try to find the default tenant in the master database
        Optional<MasterTenant> defaultTenantOpt = masterTenantRepository.findByTenantId(defaultTenant);
        if (defaultTenantOpt.isPresent()) {
            DataSource dataSource = createDataSource(defaultTenant);
            tenantDataSources.put(defaultTenant, dataSource);
            return dataSource;
        }

        // If the default tenant is not found, create a dummy data source
        log.warn("Default tenant {} not found, creating dummy data source", defaultTenant);
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:h2:mem:default");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setPoolName("DefaultHikariPool");

        tenantDataSources.put(defaultTenant, dataSource);
        return dataSource;
    }
}
