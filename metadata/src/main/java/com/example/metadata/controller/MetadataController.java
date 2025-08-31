package com.example.metadata.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.metadata.entity.MetadataEntity;
import com.example.metadata.service.MetadataService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/metadata")
@RequiredArgsConstructor
public class MetadataController {

    private final MetadataService metadataService;
    
    @GetMapping
    public ResponseEntity<List<MetadataEntity>> getAllMetadata() {
        return ResponseEntity.ok(metadataService.getAllMetadata());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<MetadataEntity> getMetadataById(@PathVariable Long id) {
        return metadataService.getMetadataById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<MetadataEntity> createMetadata(@RequestBody MetadataEntity metadata) {
        return ResponseEntity.status(HttpStatus.CREATED).body(metadataService.saveMetadata(metadata));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<MetadataEntity> updateMetadata(@PathVariable Long id, @RequestBody MetadataEntity metadata) {
        return metadataService.getMetadataById(id)
                .map(existingMetadata -> {
                    metadata.setId(id);
                    return ResponseEntity.ok(metadataService.saveMetadata(metadata));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMetadata(@PathVariable Long id) {
        return metadataService.getMetadataById(id)
                .map(metadata -> {
                    metadataService.deleteMetadata(id);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}

