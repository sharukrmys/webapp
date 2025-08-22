package com.example.applib.tenant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.jdbc.datasource.lookup.DataSourceLookupFailureException;

@Slf4j
public class TenantAwareDataSource extends AbstractDataSource {

    private final String dbType;
    private final Map<String, DataSource> tenantDataSources = new ConcurrentHashMap<>();
    
    @Autowired
    private MasterTenantRepository masterTenantRepository;
    
    @Autowired
    private ObjectMapper objectMapper;

    public TenantAwareDataSource(String dbType) {
        this.dbType = dbType;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return lookupDataSource().getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return lookupDataSource().getConnection(username, password);
    }

    private DataSource lookupDataSource() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new DataSourceLookupFailureException("No tenant ID found in context");
        }

        // Check if we already have a data source for this tenant and db type
        String key = tenantId + "-" + dbType;
        if (tenantDataSources.containsKey(key)) {
            return tenantDataSources.get(key);
        }

        // Otherwise, create a new data source
        DataSource dataSource = createDataSourceForTenant(tenantId);
        tenantDataSources.put(key, dataSource);
        return dataSource;
    }

    private DataSource createDataSourceForTenant(String tenantId) {
        try {
            // Get the tenant from the repository
            Optional<MasterTenant> optionalTenant = masterTenantRepository.findByTenantIdAndIsActiveTrue(tenantId);
            
            if (optionalTenant.isEmpty()) {
                throw new DataSourceLookupFailureException("Tenant not found or not active: " + tenantId);
            }
            
            MasterTenant tenant = optionalTenant.get();

            // Determine which database URL to use based on the db type
            String dbUrl;
            switch (dbType) {
                case "flex":
                    dbUrl = tenant.getFlexdb();
                    break;
                case "tac":
                    dbUrl = tenant.getUrl();
                    break;
                case "read":
                    dbUrl = tenant.getReaddb();
                    break;
                case "appstore":
                    dbUrl = tenant.getAppstoredb();
                    break;
                default:
                    dbUrl = tenant.getUrl();
            }

            if (dbUrl == null || dbUrl.isEmpty()) {
                throw new DataSourceLookupFailureException("No database URL found for tenant " + tenantId + " and db type " + dbType);
            }

            // Create a new data source
            HikariDataSource dataSource = new HikariDataSource();
            dataSource.setDriverClassName("org.postgresql.Driver");
            dataSource.setJdbcUrl(dbUrl);
            dataSource.setUsername(tenant.getUsername());
            dataSource.setPassword(tenant.getPassword());
            
            // Parse DB properties from JSON string
            if (tenant.getDbProperties() != null && !tenant.getDbProperties().isEmpty()) {
                Map<String, Object> dbProps = objectMapper.readValue(tenant.getDbProperties(), Map.class);
                
                if (dbProps.containsKey("connectionTimeout") && tenant.getConnectionTimeout() == null) {
                    dataSource.setConnectionTimeout(Long.parseLong(dbProps.get("connectionTimeout").toString()));
                } else if (tenant.getConnectionTimeout() != null) {
                    dataSource.setConnectionTimeout(tenant.getConnectionTimeout());
                }
                
                if (dbProps.containsKey("idleTimeout") && tenant.getIdleTimeout() == null) {
                    dataSource.setIdleTimeout(Long.parseLong(dbProps.get("idleTimeout").toString()));
                } else if (tenant.getIdleTimeout() != null) {
                    dataSource.setIdleTimeout(tenant.getIdleTimeout());
                }
                
                if (dbProps.containsKey("maxPoolSize") && tenant.getMaxPoolSize() == null) {
                    dataSource.setMaximumPoolSize(Integer.parseInt(dbProps.get("maxPoolSize").toString()));
                } else if (tenant.getMaxPoolSize() != null) {
                    dataSource.setMaximumPoolSize(tenant.getMaxPoolSize());
                }
                
                if (dbProps.containsKey("minIdle") && tenant.getMinIdle() == null) {
                    dataSource.setMinimumIdle(Integer.parseInt(dbProps.get("minIdle").toString()));
                } else if (tenant.getMinIdle() != null) {
                    dataSource.setMinimumIdle(tenant.getMinIdle());
                }
            } else {
                // Default values
                dataSource.setConnectionTimeout(30000);
                dataSource.setIdleTimeout(600000);
                dataSource.setMaxLifetime(1800000);
                dataSource.setMaximumPoolSize(10);
                dataSource.setMinimumIdle(5);
            }
            
            return dataSource;
        } catch (Exception e) {
            log.error("Error creating data source for tenant: {} and db type: {}", tenantId, dbType, e);
            throw new DataSourceLookupFailureException("Failed to create data source for tenant: " + tenantId, e);
        }
    }
}
