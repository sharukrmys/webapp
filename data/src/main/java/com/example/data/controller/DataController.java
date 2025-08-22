package com.example.data.controller;

import com.example.applib.tenant.TenantContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/data")
@RequiredArgsConstructor
@Slf4j
public class DataController {

    private final JdbcTemplate tacJdbcTemplate;

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getDataById(@PathVariable String id) {
        log.info("Getting data with ID: {} for tenant: {}", id, TenantContext.getTenantId());
        
        try {
            String sql = "SELECT * FROM data WHERE id = ?";
            List<Map<String, Object>> results = tacJdbcTemplate.queryForList(sql, id);
            
            if (results.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(results.get(0));
        } catch (Exception e) {
            log.error("Error getting data with ID: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllData() {
        log.info("Getting all data for tenant: {}", TenantContext.getTenantId());
        
        try {
            String sql = "SELECT * FROM data";
            List<Map<String, Object>> results = tacJdbcTemplate.queryForList(sql);
            
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Error getting all data", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createData(@RequestBody Map<String, Object> data) {
        log.info("Creating data for tenant: {}", TenantContext.getTenantId());
        
        try {
            String id = UUID.randomUUID().toString();
            data.put("id", id);
            
            StringBuilder columns = new StringBuilder();
            StringBuilder values = new StringBuilder();
            Map<String, Object> params = new HashMap<>();
            
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                if (columns.length() > 0) {
                    columns.append(", ");
                    values.append(", ");
                }
                
                columns.append(entry.getKey());
                values.append("?");
                params.put(entry.getKey(), entry.getValue());
            }
            
            String sql = String.format("INSERT INTO data (%s) VALUES (%s)", columns, values);
            
            tacJdbcTemplate.update(sql, params.values().toArray());
            
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            log.error("Error creating data", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
