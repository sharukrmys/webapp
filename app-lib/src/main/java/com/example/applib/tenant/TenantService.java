package com.example.applib.tenant;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantService {

    private final JdbcTemplate masterJdbcTemplate;

    /**
     * Get a tenant by ID
     * 
     * @param tenantId The tenant ID
     * @return Optional containing the tenant if found
     */
    public Optional<MasterTenant> getTenantById(String tenantId) {
        try {
            MasterTenant tenant = masterJdbcTemplate.queryForObject(
                    "SELECT * FROM master_tenant WHERE tenant_id = ?",
                    new Object[]{tenantId},
                    (rs, rowNum) -> {
                        MasterTenant t = new MasterTenant();
                        t.setId(rs.getLong("id"));
                        t.setTenantId(rs.getString("tenant_id"));
                        t.setUrl(rs.getString("url"));
                        t.setUsername(rs.getString("username"));
                        t.setPassword(rs.getString("password"));
                        t.setDialect(rs.getString("dialect"));
                        t.setVersion(rs.getInt("version"));
                        t.setFlexdb(rs.getString("flexdb"));
                        t.setProceduresFilename(rs.getString("procedures_filename"));
                        t.setReaddb(rs.getString("readdb"));
                        t.setAppstoredb(rs.getString("appstoredb"));
                        t.setDbProperties(rs.getString("db_properties"));
                        t.setIsActive(rs.getBoolean("isactive"));
                        
                        // Handle nullable columns
                        if (rs.getObject("connectiontimeout") != null) {
                            t.setConnectionTimeout(rs.getLong("connectiontimeout"));
                        }
                        if (rs.getObject("idletimeout") != null) {
                            t.setIdleTimeout(rs.getLong("idletimeout"));
                        }
                        if (rs.getObject("maxpoolsize") != null) {
                            t.setMaxPoolSize(rs.getInt("maxpoolsize"));
                        }
                        if (rs.getObject("minidle") != null) {
                            t.setMinIdle(rs.getInt("minidle"));
                        }
                        
                        return t;
                    }
            );
            
            return Optional.ofNullable(tenant);
        } catch (Exception e) {
            log.error("Error retrieving tenant: {}", tenantId, e);
            return Optional.empty();
        }
    }

    /**
     * Get all active tenants
     * 
     * @return List of active tenants
     */
    public List<MasterTenant> getAllActiveTenants() {
        try {
            return masterJdbcTemplate.query(
                    "SELECT * FROM master_tenant WHERE isactive = true",
                    (rs, rowNum) -> {
                        MasterTenant t = new MasterTenant();
                        t.setId(rs.getLong("id"));
                        t.setTenantId(rs.getString("tenant_id"));
                        t.setUrl(rs.getString("url"));
                        t.setUsername(rs.getString("username"));
                        t.setPassword(rs.getString("password"));
                        t.setDialect(rs.getString("dialect"));
                        t.setVersion(rs.getInt("version"));
                        t.setFlexdb(rs.getString("flexdb"));
                        t.setProceduresFilename(rs.getString("procedures_filename"));
                        t.setReaddb(rs.getString("readdb"));
                        t.setAppstoredb(rs.getString("appstoredb"));
                        t.setDbProperties(rs.getString("db_properties"));
                        t.setIsActive(rs.getBoolean("isactive"));
                        
                        // Handle nullable columns
                        if (rs.getObject("connectiontimeout") != null) {
                            t.setConnectionTimeout(rs.getLong("connectiontimeout"));
                        }
                        if (rs.getObject("idletimeout") != null) {
                            t.setIdleTimeout(rs.getLong("idletimeout"));
                        }
                        if (rs.getObject("maxpoolsize") != null) {
                            t.setMaxPoolSize(rs.getInt("maxpoolsize"));
                        }
                        if (rs.getObject("minidle") != null) {
                            t.setMinIdle(rs.getInt("minidle"));
                        }
                        
                        return t;
                    }
            );
        } catch (Exception e) {
            log.error("Error retrieving active tenants", e);
            return List.of();
        }
    }

    /**
     * Create a new tenant
     * 
     * @param tenant The tenant to create
     * @return true if successful, false otherwise
     */
    public boolean createTenant(MasterTenant tenant) {
        try {
            int result = masterJdbcTemplate.update(
                    "INSERT INTO master_tenant (tenant_id, url, username, password, dialect, version, flexdb, " +
                    "procedures_filename, readdb, appstoredb, db_properties, isactive, connectiontimeout, " +
                    "idletimeout, maxpoolsize, minidle) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    tenant.getTenantId(),
                    tenant.getUrl(),
                    tenant.getUsername(),
                    tenant.getPassword(),
                    tenant.getDialect(),
                    tenant.getVersion(),
                    tenant.getFlexdb(),
                    tenant.getProceduresFilename(),
                    tenant.getReaddb(),
                    tenant.getAppstoredb(),
                    tenant.getDbProperties(),
                    tenant.getIsActive(),
                    tenant.getConnectionTimeout(),
                    tenant.getIdleTimeout(),
                    tenant.getMaxPoolSize(),
                    tenant.getMinIdle()
            );
            
            return result > 0;
        } catch (Exception e) {
            log.error("Error creating tenant: {}", tenant.getTenantId(), e);
            return false;
        }
    }

    /**
     * Update an existing tenant
     * 
     * @param tenant The tenant to update
     * @return true if successful, false otherwise
     */
    public boolean updateTenant(MasterTenant tenant) {
        try {
            int result = masterJdbcTemplate.update(
                    "UPDATE master_tenant SET url = ?, username = ?, password = ?, dialect = ?, version = ?, " +
                    "flexdb = ?, procedures_filename = ?, readdb = ?, appstoredb = ?, db_properties = ?, " +
                    "isactive = ?, connectiontimeout = ?, idletimeout = ?, maxpoolsize = ?, minidle = ? " +
                    "WHERE tenant_id = ?",
                    tenant.getUrl(),
                    tenant.getUsername(),
                    tenant.getPassword(),
                    tenant.getDialect(),
                    tenant.getVersion(),
                    tenant.getFlexdb(),
                    tenant.getProceduresFilename(),
                    tenant.getReaddb(),
                    tenant.getAppstoredb(),
                    tenant.getDbProperties(),
                    tenant.getIsActive(),
                    tenant.getConnectionTimeout(),
                    tenant.getIdleTimeout(),
                    tenant.getMaxPoolSize(),
                    tenant.getMinIdle(),
                    tenant.getTenantId()
            );
            
            return result > 0;
        } catch (Exception e) {
            log.error("Error updating tenant: {}", tenant.getTenantId(), e);
            return false;
        }
    }

    /**
     * Execute a query on a specific tenant's database
     * 
     * @param tenantId The tenant ID
     * @param sql The SQL query
     * @param args The query arguments
     * @return List of results as maps
     */
    public List<Map<String, Object>> executeQuery(String tenantId, String sql, Object... args) {
        // Set the tenant context
        TenantContext.setTenantId(tenantId);
        
        try {
            // Use the tenant-specific JDBC template
            JdbcTemplate tenantJdbcTemplate = new JdbcTemplate(new TenantAwareDataSource("tac"));
            return tenantJdbcTemplate.queryForList(sql, args);
        } finally {
            // Clear the tenant context
            TenantContext.clear();
        }
    }
}
