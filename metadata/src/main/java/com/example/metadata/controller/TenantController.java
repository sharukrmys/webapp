package com.example.metadata.controller;

import com.example.applib.tenant.MasterTenant;
import com.example.applib.tenant.MasterTenantRepository;
import com.example.applib.tenant.TenantJdbcService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for tenant operations.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/tenants")
public class TenantController {

    private final MasterTenantRepository masterTenantRepository;
    private final TenantJdbcService tenantJdbcService;

    /**
     * Gets all tenants.
     */
    @GetMapping
    public ResponseEntity<List<MasterTenant>> getAllTenants() {
        List<MasterTenant> tenants = masterTenantRepository.findAll();
        return ResponseEntity.ok(tenants);
    }

    /**
     * Gets a tenant by ID.
     */
    @GetMapping("/{tenantId}")
    public ResponseEntity<MasterTenant> getTenantById(@PathVariable String tenantId) {
        Optional<MasterTenant> tenant = masterTenantRepository.findByTenantId(tenantId);
        return tenant.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Creates a new tenant.
     */
    @PostMapping
    public ResponseEntity<MasterTenant> createTenant(@RequestBody MasterTenant tenant) {
        if (masterTenantRepository.existsByTenantId(tenant.getTenantId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        MasterTenant savedTenant = masterTenantRepository.save(tenant);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedTenant);
    }

    /**
     * Updates a tenant.
     */
    @PutMapping("/{tenantId}")
    public ResponseEntity<MasterTenant> updateTenant(@PathVariable String tenantId, @RequestBody MasterTenant tenant) {
        Optional<MasterTenant> existingTenant = masterTenantRepository.findByTenantId(tenantId);
        if (existingTenant.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Update the existing tenant
        MasterTenant tenantToUpdate = existingTenant.get();
        tenantToUpdate.setUrl(tenant.getUrl());
        tenantToUpdate.setUsername(tenant.getUsername());
        tenantToUpdate.setPassword(tenant.getPassword());
        tenantToUpdate.setDialect(tenant.getDialect());
        tenantToUpdate.setIsactive(tenant.isActive());
        tenantToUpdate.setConnectionTimeout(tenant.getConnectionTimeout());
        tenantToUpdate.setIdleTimeout(tenant.getIdleTimeout());
        tenantToUpdate.setMaxPoolSize(tenant.getMaxPoolSize());
        tenantToUpdate.setMinIdle(tenant.getMinIdle());

        MasterTenant updatedTenant = masterTenantRepository.save(tenantToUpdate);
        return ResponseEntity.ok(updatedTenant);
    }

    /**
     * Executes a SQL query for a specific tenant.
     */
    @PostMapping("/{tenantId}/query")
    public ResponseEntity<List<Map<String, Object>>> executeQuery(@PathVariable String tenantId, @RequestBody String sql) {
        try {
            List<Map<String, Object>> results = tenantJdbcService.queryForList(tenantId, sql);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Error executing query for tenant {}: {}", tenantId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
