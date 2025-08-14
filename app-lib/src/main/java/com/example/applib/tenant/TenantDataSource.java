package com.example.applib.tenant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class TenantDataSource {

    private final Map<String, DataSource> dataSources = new HashMap<>();
    private final MasterTenantRepository masterTenantRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public TenantDataSource(MasterTenantRepository masterTenantRepository, 
                           @Qualifier("objectMapper") ObjectMapper objectMapper) {
        this.masterTenantRepository = masterTenantRepository;
        this.objectMapper = objectMapper;
    }

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

    private DataSource createDataSource(String tenantId) {
        Optional<MasterTenant> masterTenantOpt = masterTenantRepository.findByTenantIdAndIsActiveTrue(tenantId);
        
        if (masterTenantOpt.isEmpty()) {
            log.error("No active tenant found with ID: {}", tenantId);
            return null;
        }
        
        MasterTenant masterTenant = masterTenantOpt.get();
        
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(masterTenant.getUrl());
            config.setUsername(masterTenant.getUsername());
            config.setPassword(masterTenant.getPassword());
            config.setDriverClassName("org.postgresql.Driver");
            config.setPoolName(tenantId + "-hikari-pool");
            
            // Parse and apply DB properties from JSON
            if (masterTenant.getDbProperties() != null) {
                try {
                    Map<String, Object> props = objectMapper.readValue(masterTenant.getDbProperties(), Map.class);
                    
                    if (props.containsKey("minIdle") && props.get("minIdle") != null) {
                        config.setMinimumIdle(Integer.parseInt(props.get("minIdle").toString()));
                    } else if (masterTenant.getMinIdle() != null) {
                        config.setMinimumIdle(masterTenant.getMinIdle());
                    } else {
                        config.setMinimumIdle(1);
                    }
                    
                    if (props.containsKey("maxPoolSize") && props.get("maxPoolSize") != null) {
                        config.setMaximumPoolSize(Integer.parseInt(props.get("maxPoolSize").toString()));
                    } else if (masterTenant.getMaxPoolSize() != null) {
                        config.setMaximumPoolSize(masterTenant.getMaxPoolSize());
                    } else {
                        config.setMaximumPoolSize(3);
                    }
                    
                    if (props.containsKey("connectionTimeout") && props.get("connectionTimeout") != null) {
                        config.setConnectionTimeout(Long.parseLong(props.get("connectionTimeout").toString()) * 1000);
                    } else if (masterTenant.getConnectionTimeout() != null) {
                        config.setConnectionTimeout(masterTenant.getConnectionTimeout() * 1000);
                    } else {
                        config.setConnectionTimeout(30000);
                    }
                    
                    if (props.containsKey("idleTimeout") && props.get("idleTimeout") != null) {
                        config.setIdleTimeout(Long.parseLong(props.get("idleTimeout").toString()) * 1000);
                    } else if (masterTenant.getIdleTimeout() != null) {
                        config.setIdleTimeout(masterTenant.getIdleTimeout() * 1000);
                    } else {
                        config.setIdleTimeout(600000);
                    }
                    
                } catch (Exception e) {
                    log.error("Error parsing DB properties for tenant {}: {}", tenantId, e.getMessage());
                    // Use default values if parsing fails
                    config.setMinimumIdle(1);
                    config.setMaximumPoolSize(3);
                    config.setConnectionTimeout(30000);
                    config.setIdleTimeout(600000);
                }
            } else {
                // Use values from individual fields if available, otherwise use defaults
                config.setMinimumIdle(masterTenant.getMinIdle() != null ? masterTenant.getMinIdle() : 1);
                config.setMaximumPoolSize(masterTenant.getMaxPoolSize() != null ? masterTenant.getMaxPoolSize() : 3);
                config.setConnectionTimeout(masterTenant.getConnectionTimeout() != null ? masterTenant.getConnectionTimeout() * 1000 : 30000);
                config.setIdleTimeout(masterTenant.getIdleTimeout() != null ? masterTenant.getIdleTimeout() * 1000 : 600000);
            }
            
            config.setMaxLifetime(1800000); // 30 minutes
            config.setAutoCommit(true);
            config.setConnectionTestQuery("SELECT 1");
            
            return new HikariDataSource(config);
            
        } catch (Exception e) {
            log.error("Error creating data source for tenant {}: {}", tenantId, e.getMessage());
            return null;
        }
    }
}

