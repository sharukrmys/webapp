package com.example.metadata.controller;

import com.example.applib.tenant.MasterTenant;
import com.example.applib.tenant.TenantContext;
import com.example.applib.tenant.TenantJdbcService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantJdbcService tenantJdbcService;
    private final JdbcTemplate masterJdbcTemplate;
    private final JdbcTemplate tenantJdbcTemplate;
    private final JdbcTemplate flexJdbcTemplate;
    private final JdbcTemplate tacJdbcTemplate;

    @GetMapping
    public ResponseEntity<List<MasterTenant>> getAllTenants() {
        List<MasterTenant> tenants = tenantJdbcService.getAllActiveTenants();
        return ResponseEntity.ok(tenants);
    }

    @GetMapping("/{tenantId}")
    public ResponseEntity<MasterTenant> getTenantById(@PathVariable String tenantId) {
        Optional<MasterTenant> tenant = tenantJdbcService.getTenantById(tenantId);
        return tenant.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<String> createTenant(@RequestBody MasterTenant tenant) {
        boolean success = tenantJdbcService.createTenant(tenant);
        return success
                ? ResponseEntity.ok("Tenant created successfully")
                : ResponseEntity.badRequest().body("Failed to create tenant");
    }

    @PutMapping("/{tenantId}")
    public ResponseEntity<String> updateTenant(@PathVariable String tenantId, @RequestBody MasterTenant tenant) {
        tenant.setTenantId(tenantId);
        boolean success = tenantJdbcService.updateTenant(tenant);
        return success
                ? ResponseEntity.ok("Tenant updated successfully")
                : ResponseEntity.badRequest().body("Failed to update tenant");
    }

    @GetMapping("/current")
    public ResponseEntity<String> getCurrentTenant() {
        String tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok("Current tenant: " + tenantId);
    }

    @GetMapping("/{tenantId}/query")
    public ResponseEntity<List<Map<String, Object>>> executeQuery(
            @PathVariable String tenantId,
            @RequestParam String sql) {
        List<Map<String, Object>> results = tenantJdbcService.executeQuery(tenantId, sql);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/test-connections")
    public ResponseEntity<Map<String, Object>> testConnections() {
        // Set a tenant ID for testing
        TenantContext.setTenantId("test-tenant");

        try {
            // Test master connection
            int masterCount = masterJdbcTemplate.queryForObject("SELECT COUNT(*) FROM master_tenant", Integer.class);

            // Test tenant connection
            int tenantCount = tenantJdbcTemplate.queryForObject("SELECT 1", Integer.class);

            // Test flex connection
            int flexCount = flexJdbcTemplate.queryForObject("SELECT 1", Integer.class);

            // Test tac connection
            int tacCount = tacJdbcTemplate.queryForObject("SELECT 1", Integer.class);

            Map<String, Object> results = Map.of(
                    "masterConnection", "Success: " + masterCount + " tenants found",
                    "tenantConnection", "Success: " + tenantCount,
                    "flexConnection", "Success: " + flexCount,
                    "tacConnection", "Success: " + tacCount
            );

            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Error testing connections", e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        } finally {
            TenantContext.clear();
        }
    }
}

