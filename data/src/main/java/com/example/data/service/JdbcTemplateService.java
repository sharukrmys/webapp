package com.example.data.service;

import com.example.applib.tenant.TenantContext;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class JdbcTemplateService {

    @Qualifier("masterJdbcTemplate")
    private final JdbcTemplate masterJdbcTemplate;

    @Qualifier("tacJdbcTemplate")
    private final JdbcTemplate tacJdbcTemplate;

    @Qualifier("flexJdbcTemplate")
    private final JdbcTemplate flexJdbcTemplate;

    @Qualifier("readJdbcTemplate")
    private final JdbcTemplate readJdbcTemplate;

    @Qualifier("appstoreJdbcTemplate")
    private final JdbcTemplate appstoreJdbcTemplate;

    /**
     * Execute a query on the master database
     *
     * @param sql SQL query
     * @param args Query arguments
     * @return Query results
     */
    public List<Map<String, Object>> queryMaster(String sql, Object... args) {
        return masterJdbcTemplate.queryForList(sql, args);
    }

    /**
     * Execute a query on the TAC database
     *
     * @param sql SQL query
     * @param args Query arguments
     * @return Query results
     */
    public List<Map<String, Object>> queryTac(String sql, Object... args) {
        return tacJdbcTemplate.queryForList(sql, args);
    }

    /**
     * Execute a query on the FLEX database
     *
     * @param sql SQL query
     * @param args Query arguments
     * @return Query results
     */
    public List<Map<String, Object>> queryFlex(String sql, Object... args) {
        return flexJdbcTemplate.queryForList(sql, args);
    }

    /**
     * Execute a query on the READ database
     *
     * @param sql SQL query
     * @param args Query arguments
     * @return Query results
     */
    public List<Map<String, Object>> queryRead(String sql, Object... args) {
        return readJdbcTemplate.queryForList(sql, args);
    }

    /**
     * Execute a query on the APPSTORE database
     *
     * @param sql SQL query
     * @param args Query arguments
     * @return Query results
     */
    public List<Map<String, Object>> queryAppstore(String sql, Object... args) {
        return appstoreJdbcTemplate.queryForList(sql, args);
    }

    /**
     * Execute an update on the TAC database
     *
     * @param sql SQL update
     * @param args Update arguments
     * @return Number of rows affected
     */
    public int updateTac(String sql, Object... args) {
        return tacJdbcTemplate.update(sql, args);
    }

    /**
     * Execute an update on the FLEX database
     *
     * @param sql SQL update
     * @param args Update arguments
     * @return Number of rows affected
     */
    public int updateFlex(String sql, Object... args) {
        return flexJdbcTemplate.update(sql, args);
    }

    /**
     * Execute a batch update on the TAC database
     *
     * @param sql SQL update
     * @param batchArgs Batch arguments
     * @return Array of rows affected
     */
    public int[] batchUpdateTac(String sql, List<Object[]> batchArgs) {
        return tacJdbcTemplate.batchUpdate(sql, batchArgs);
    }

    /**
     * Execute a batch update on the FLEX database
     *
     * @param sql SQL update
     * @param batchArgs Batch arguments
     * @return Array of rows affected
     */
    public int[] batchUpdateFlex(String sql, List<Object[]> batchArgs) {
        return flexJdbcTemplate.batchUpdate(sql, batchArgs);
    }

    /**
     * Execute a query for a specific tenant
     *
     * @param tenantId Tenant ID
     * @param sql SQL query
     * @param args Query arguments
     * @return Query results
     */
    public List<Map<String, Object>> queryForTenant(String tenantId, String sql, Object... args) {
        // Set the tenant context
        TenantContext.setTenantId(tenantId);

        try {
            // Execute the query
            return tacJdbcTemplate.queryForList(sql, args);
        } finally {
            // Clear the tenant context
            TenantContext.clear();
        }
    }

    /**
     * Execute an update for a specific tenant
     *
     * @param tenantId Tenant ID
     * @param sql SQL update
     * @param args Update arguments
     * @return Number of rows affected
     */
    public int updateForTenant(String tenantId, String sql, Object... args) {
        // Set the tenant context
        TenantContext.setTenantId(tenantId);

        try {
            // Execute the update
            return tacJdbcTemplate.update(sql, args);
        } finally {
            // Clear the tenant context
            TenantContext.clear();
        }
    }
}
