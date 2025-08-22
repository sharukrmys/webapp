package com.example.data.controller;

import com.example.applib.tenant.TenantContext;
import com.example.data.service.JdbcTemplateService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/jdbc")
@RequiredArgsConstructor
public class JdbcController {

    private final JdbcTemplateService jdbcTemplateService;

    @GetMapping("/master")
    public ResponseEntity<List<Map<String, Object>>> queryMaster(@RequestParam String sql) {
        try {
            List<Map<String, Object>> results = jdbcTemplateService.queryMaster(sql);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Error executing query on master database", e);
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/tac")
    public ResponseEntity<List<Map<String, Object>>> queryTac(@RequestParam String sql) {
        try {
            List<Map<String, Object>> results = jdbcTemplateService.queryTac(sql);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Error executing query on TAC database", e);
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/flex")
    public ResponseEntity<List<Map<String, Object>>> queryFlex(@RequestParam String sql) {
        try {
            List<Map<String, Object>> results = jdbcTemplateService.queryFlex(sql);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Error executing query on FLEX database", e);
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/read")
    public ResponseEntity<List<Map<String, Object>>> queryRead(@RequestParam String sql) {
        try {
            List<Map<String, Object>> results = jdbcTemplateService.queryRead(sql);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Error executing query on READ database", e);
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/appstore")
    public ResponseEntity<List<Map<String, Object>>> queryAppstore(@RequestParam String sql) {
        try {
            List<Map<String, Object>> results = jdbcTemplateService.queryAppstore(sql);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Error executing query on APPSTORE database", e);
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/tac")
    public ResponseEntity<Map<String, Integer>> updateTac(@RequestParam String sql) {
        try {
            int rowsAffected = jdbcTemplateService.updateTac(sql);
            return ResponseEntity.ok(Map.of("rowsAffected", rowsAffected));
        } catch (Exception e) {
            log.error("Error executing update on TAC database", e);
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/flex")
    public ResponseEntity<Map<String, Integer>> updateFlex(@RequestParam String sql) {
        try {
            int rowsAffected = jdbcTemplateService.updateFlex(sql);
            return ResponseEntity.ok(Map.of("rowsAffected", rowsAffected));
        } catch (Exception e) {
            log.error("Error executing update on FLEX database", e);
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/tenant/{tenantId}")
    public ResponseEntity<List<Map<String, Object>>> queryForTenant(
            @PathVariable String tenantId,
            @RequestParam String sql) {
        try {
            List<Map<String, Object>> results = jdbcTemplateService.queryForTenant(tenantId, sql);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Error executing query for tenant: {}", tenantId, e);
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/tenant/{tenantId}")
    public ResponseEntity<Map<String, Integer>> updateForTenant(
            @PathVariable String tenantId,
            @RequestParam String sql) {
        try {
            int rowsAffected = jdbcTemplateService.updateForTenant(tenantId, sql);
            return ResponseEntity.ok(Map.of("rowsAffected", rowsAffected));
        } catch (Exception e) {
            log.error("Error executing update for tenant: {}", tenantId, e);
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/current-tenant")
    public ResponseEntity<Map<String, String>> getCurrentTenant() {
        String tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(Map.of("tenantId", tenantId != null ? tenantId : "none"));
    }
}
