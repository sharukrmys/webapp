package com.example.applib.tenant;

import com.zaxxer.hikari.HikariDataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Component for managing tenant data sources.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TenantDataSource {

    private final MasterTenantRepository masterTenantRepository;
    private final TenantDataSourceConfig tenantDataSourceConfig;

    private final Map<String, DataSource> dataSources = new HashMap<>();

    @Value("${tenant.default-tenant:default}")
    private String defaultTenant;

    /**
     * Gets the data source for a specific tenant.
     *
     * @param tenantId The tenant ID
     * @return The data source for the tenant
     */
    public DataSource getDataSource(String tenantId) {
        if (dataSources.containsKey(tenantId)) {
            return dataSources.get(tenantId);
        }

        DataSource dataSource = createDataSource(tenantId);
        if (dataSource != null) {
            dataSources.put(tenantId, dataSource);
        }

        return dataSource;
    }

    /**
     * Gets the default data source.
     *
     * @return The default data source
     */
    public DataSource getDefaultDataSource() {
        // Create a default data source if it doesn't exist
        if (!dataSources.containsKey(defaultTenant)) {
            DataSource defaultDataSource = createDefaultDataSource();
            dataSources.put(defaultTenant, defaultDataSource);
        }

        return dataSources.get(defaultTenant);
    }

    /**
     * Creates a data source for a specific tenant.
     *
     * @param tenantId The tenant ID
     * @return The data source for the tenant
     */
    private DataSource createDataSource(String tenantId) {
        Optional<MasterTenant> masterTenantOpt = masterTenantRepository.findByTenantId(tenantId);
        if (masterTenantOpt.isPresent()) {
            return tenantDataSourceConfig.createAndConfigureDataSource(masterTenantOpt.get());
        }

        log.warn("Tenant {} not found in master database", tenantId);
        return getDefaultDataSource();
    }

    /**
     * Creates a default data source.
     *
     * @return The default data source
     */
    private DataSource createDefaultDataSource() {
        // Try to find the default tenant in the master database
        Optional<MasterTenant> defaultMasterTenantOpt = masterTenantRepository.findByTenantId(defaultTenant);
        if (defaultMasterTenantOpt.isPresent()) {
            return tenantDataSourceConfig.createAndConfigureDataSource(defaultMasterTenantOpt.get());
        }

        // If the default tenant is not found, create a dummy data source
        log.warn("Default tenant {} not found in master database, creating dummy data source", defaultTenant);
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl("jdbc:h2:mem:default");
        ds.setUsername("sa");
        ds.setPassword("");
        ds.setDriverClassName("org.h2.Driver");
        ds.setPoolName("DefaultHikariPool");
        return ds;
    }

    /**
     * Gets all tenant IDs.
     *
     * @return A list of all tenant IDs
     */
    public List<String> getAllTenantIds() {
        return masterTenantRepository.findAll().stream()
                .map(MasterTenant::getTenantId)
                .toList();
    }

    /**
     * Resets the data source for a specific tenant.
     *
     * @param tenantId The tenant ID
     */
    public void resetDataSource(String tenantId) {
        DataSource dataSource = dataSources.remove(tenantId);
        if (dataSource instanceof HikariDataSource) {
            ((HikariDataSource) dataSource).close();
        }
    }

    /**
     * Resets all data sources.
     */
    public void resetAllDataSources() {
        dataSources.forEach((tenantId, dataSource) -> {
            if (dataSource instanceof HikariDataSource) {
                ((HikariDataSource) dataSource).close();
            }
        });
        dataSources.clear();
    }
}
