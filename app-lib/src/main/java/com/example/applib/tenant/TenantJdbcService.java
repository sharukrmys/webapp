package com.example.applib.tenant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Service for executing JDBC operations in a tenant-specific context.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TenantJdbcService {

    @Qualifier("tenantJdbcTemplate")
    private final JdbcTemplate jdbcTemplate;

    /**
     * Executes a SQL query and returns a list of maps representing the result set.
     *
     * @param tenantId The tenant ID to execute the query for
     * @param sql The SQL query to execute
     * @return A list of maps representing the result set
     */
    public List<Map<String, Object>> queryForList(String tenantId, String sql) {
        try {
            TenantContext.setTenantId(tenantId);
            return jdbcTemplate.queryForList(sql);
        } catch (DataAccessException e) {
            log.error("Error executing query for tenant {}: {}", tenantId, e.getMessage(), e);
            throw e;
        } finally {
            TenantContext.clear();
        }
    }

    /**
     * Executes a SQL query with parameters and returns a list of maps representing the result set.
     *
     * @param tenantId The tenant ID to execute the query for
     * @param sql The SQL query to execute
     * @param args The arguments to bind to the query
     * @return A list of maps representing the result set
     */
    public List<Map<String, Object>> queryForList(String tenantId, String sql, Object... args) {
        try {
            TenantContext.setTenantId(tenantId);
            return jdbcTemplate.queryForList(sql, args);
        } catch (DataAccessException e) {
            log.error("Error executing query for tenant {}: {}", tenantId, e.getMessage(), e);
            throw e;
        } finally {
            TenantContext.clear();
        }
    }

    /**
     * Executes a SQL query and returns a single object.
     *
     * @param tenantId The tenant ID to execute the query for
     * @param sql The SQL query to execute
     * @param requiredType The type of object to return
     * @return The result of the query
     */
    public <T> T queryForObject(String tenantId, String sql, Class<T> requiredType) {
        try {
            TenantContext.setTenantId(tenantId);
            return jdbcTemplate.queryForObject(sql, requiredType);
        } catch (DataAccessException e) {
            log.error("Error executing query for tenant {}: {}", tenantId, e.getMessage(), e);
            throw e;
        } finally {
            TenantContext.clear();
        }
    }

    /**
     * Executes a SQL query with parameters and returns a single object.
     *
     * @param tenantId The tenant ID to execute the query for
     * @param sql The SQL query to execute
     * @param requiredType The type of object to return
     * @param args The arguments to bind to the query
     * @return The result of the query
     */
    public <T> T queryForObject(String tenantId, String sql, Class<T> requiredType, Object... args) {
        try {
            TenantContext.setTenantId(tenantId);
            return jdbcTemplate.queryForObject(sql, requiredType, args);
        } catch (DataAccessException e) {
            log.error("Error executing query for tenant {}: {}", tenantId, e.getMessage(), e);
            throw e;
        } finally {
            TenantContext.clear();
        }
    }

    /**
     * Executes a SQL update statement.
     *
     * @param tenantId The tenant ID to execute the update for
     * @param sql The SQL update statement to execute
     * @return The number of rows affected
     */
    public int update(String tenantId, String sql) {
        try {
            TenantContext.setTenantId(tenantId);
            return jdbcTemplate.update(sql);
        } catch (DataAccessException e) {
            log.error("Error executing update for tenant {}: {}", tenantId, e.getMessage(), e);
            throw e;
        } finally {
            TenantContext.clear();
        }
    }

    /**
     * Executes a SQL update statement with parameters.
     *
     * @param tenantId The tenant ID to execute the update for
     * @param sql The SQL update statement to execute
     * @param args The arguments to bind to the update statement
     * @return The number of rows affected
     */
    public int update(String tenantId, String sql, Object... args) {
        try {
            TenantContext.setTenantId(tenantId);
            return jdbcTemplate.update(sql, args);
        } catch (DataAccessException e) {
            log.error("Error executing update for tenant {}: {}", tenantId, e.getMessage(), e);
            throw e;
        } finally {
            TenantContext.clear();
        }
    }

    /**
     * Executes a batch update with the same SQL statement for each batch.
     *
     * @param tenantId The tenant ID to execute the batch update for
     * @param sql The SQL statement to execute
     * @param batchArgs The arguments for each batch
     * @return An array of the number of rows affected by each batch
     */
    public int[] batchUpdate(String tenantId, String sql, List<Object[]> batchArgs) {
        try {
            TenantContext.setTenantId(tenantId);
            return jdbcTemplate.batchUpdate(sql, batchArgs);
        } catch (DataAccessException e) {
            log.error("Error executing batch update for tenant {}: {}", tenantId, e.getMessage(), e);
            throw e;
        } finally {
            TenantContext.clear();
        }
    }

    /**
     * Creates a named parameter JDBC template for the current tenant.
     *
     * @param tenantId The tenant ID to create the template for
     * @return A named parameter JDBC template
     */
    public NamedParameterJdbcTemplate getNamedParameterJdbcTemplate(String tenantId) {
        TenantContext.setTenantId(tenantId);
        return new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    /**
     * Executes a SQL query with named parameters and returns a list of maps representing the result set.
     *
     * @param tenantId The tenant ID to execute the query for
     * @param sql The SQL query to execute
     * @param paramMap The named parameters to bind to the query
     * @return A list of maps representing the result set
     */
    public List<Map<String, Object>> queryForList(String tenantId, String sql, Map<String, Object> paramMap) {
        try {
            NamedParameterJdbcTemplate namedTemplate = getNamedParameterJdbcTemplate(tenantId);
            return namedTemplate.queryForList(sql, paramMap);
        } catch (DataAccessException e) {
            log.error("Error executing query for tenant {}: {}", tenantId, e.getMessage(), e);
            throw e;
        } finally {
            TenantContext.clear();
        }
    }

    /**
     * Executes a SQL update statement with named parameters.
     *
     * @param tenantId The tenant ID to execute the update for
     * @param sql The SQL update statement to execute
     * @param paramMap The named parameters to bind to the update statement
     * @return The number of rows affected
     */
    public int update(String tenantId, String sql, Map<String, Object> paramMap) {
        try {
            NamedParameterJdbcTemplate namedTemplate = getNamedParameterJdbcTemplate(tenantId);
            return namedTemplate.update(sql, paramMap);
        } catch (DataAccessException e) {
            log.error("Error executing update for tenant {}: {}", tenantId, e.getMessage(), e);
            throw e;
        } finally {
            TenantContext.clear();
        }
    }

    /**
     * Executes a batch update with named parameters.
     *
     * @param tenantId The tenant ID to execute the batch update for
     * @param sql The SQL statement to execute
     * @param batchValues The named parameters for each batch
     * @return An array of the number of rows affected by each batch
     */
    public int[] batchUpdate(String tenantId, String sql, List<Map<String, Object>> batchValues) {
        try {
            NamedParameterJdbcTemplate namedTemplate = getNamedParameterJdbcTemplate(tenantId);
            return namedTemplate.batchUpdate(sql, batchValues.toArray(new Map[0]));
        } catch (DataAccessException e) {
            log.error("Error executing batch update for tenant {}: {}", tenantId, e.getMessage(), e);
            throw e;
        } finally {
            TenantContext.clear();
        }
    }
}

