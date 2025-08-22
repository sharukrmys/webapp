package com.example.attachment.controller;

import com.example.applib.service.S3Service;
import com.example.applib.tenant.TenantContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/attachments")
@RequiredArgsConstructor
@Slf4j
public class AttachmentController {

    private final JdbcTemplate tacJdbcTemplate;
    private final S3Service s3Service;

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getAttachmentById(@PathVariable String id) {
        log.info("Getting attachment with ID: {} for tenant: {}", id, TenantContext.getTenantId());
        
        try {
            String sql = "SELECT * FROM attachments WHERE id = ?";
            List<Map<String, Object>> results = tacJdbcTemplate.queryForList(sql, id);
            
            if (results.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(results.get(0));
        } catch (Exception e) {
            log.error("Error getting attachment with ID: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllAttachments() {
        log.info("Getting all attachments for tenant: {}", TenantContext.getTenantId());
        
        try {
            String sql = "SELECT * FROM attachments";
            List<Map<String, Object>> results = tacJdbcTemplate.queryForList(sql);
            
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Error getting all attachments", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadAttachment(
            @RequestPart("file") MultipartFile file,
            @RequestParam("metadata") String metadata) {
        
        log.info("Uploading attachment for tenant: {}", TenantContext.getTenantId());
        
        try {
            String id = UUID.randomUUID().toString();
            String fileName = file.getOriginalFilename();
            String contentType = file.getContentType();
            long size = file.getSize();
            
            // Here you would upload the file to S3 or MinIO
            // For demonstration purposes, we'll just log the file details
            log.info("File details: name={}, contentType={}, size={}", fileName, contentType, size);
            
            // Insert attachment metadata into database
            String sql = "INSERT INTO attachments (id, file_name, content_type, size, metadata) VALUES (?, ?, ?, ?, ?)";
            tacJdbcTemplate.update(sql, id, fileName, contentType, size, metadata);
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", id);
            response.put("fileName", fileName);
            response.put("contentType", contentType);
            response.put("size", size);
            response.put("metadata", metadata);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error uploading attachment", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAttachment(@PathVariable String id) {
        log.info("Deleting attachment with ID: {} for tenant: {}", id, TenantContext.getTenantId());
        
        try {
            // Get attachment details
            String selectSql = "SELECT * FROM attachments WHERE id = ?";
            List<Map<String, Object>> results = tacJdbcTemplate.queryForList(selectSql, id);
            
            if (results.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            // Here you would delete the file from S3 or MinIO
            // For demonstration purposes, we'll just log the deletion
            log.info("Deleting file with ID: {}", id);
            
            // Delete attachment metadata from database
            String deleteSql = "DELETE FROM attachments WHERE id = ?";
            tacJdbcTemplate.update(deleteSql, id);
            
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting attachment with ID: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
