package com.example.data.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/test-data")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Test Data", description = "Endpoints for testing data operations")
public class TestDataController {

    private final Map<String, Map<String, Object>> dataStore = new HashMap<>();

    @GetMapping
    @Operation(summary = "Get all test data", description = "Returns all test data stored in memory")
    public ResponseEntity<Map<String, Map<String, Object>>> getAllData() {
        return ResponseEntity.ok(dataStore);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get test data by ID", description = "Returns test data for a specific ID")
    public ResponseEntity<Map<String, Object>> getDataById(
            @Parameter(description = "Data ID", required = true) @PathVariable String id) {
        
        if (dataStore.containsKey(id)) {
            return ResponseEntity.ok(dataStore.get(id));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @Operation(summary = "Create test data", description = "Creates new test data and returns the ID")
    public ResponseEntity<Map<String, Object>> createData(
            @Parameter(description = "Data to store", required = true) @RequestBody Map<String, Object> data) {
        
        String id = UUID.randomUUID().toString();
        data.put("id", id);
        data.put("createdAt", new Date());
        
        dataStore.put(id, data);
        
        Map<String, Object> response = new HashMap<>();
        response.put("id", id);
        response.put("message", "Data created successfully");
        response.put("data", data);
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update test data", description = "Updates existing test data by ID")
    public ResponseEntity<Map<String, Object>> updateData(
            @Parameter(description = "Data ID", required = true) @PathVariable String id,
            @Parameter(description = "Updated data", required = true) @RequestBody Map<String, Object> data) {
        
        if (dataStore.containsKey(id)) {
            Map<String, Object> existingData = dataStore.get(id);
            existingData.putAll(data);
            existingData.put("updatedAt", new Date());
            
            dataStore.put(id, existingData);
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", id);
            response.put("message", "Data updated successfully");
            response.put("data", existingData);
            
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete test data", description = "Deletes test data by ID")
    public ResponseEntity<Map<String, Object>> deleteData(
            @Parameter(description = "Data ID", required = true) @PathVariable String id) {
        
        if (dataStore.containsKey(id)) {
            dataStore.remove(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", id);
            response.put("message", "Data deleted successfully");
            
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/generate/{count}")
    @Operation(summary = "Generate test data", description = "Generates a specified number of test data entries")
    public ResponseEntity<List<Map<String, Object>>> generateData(
            @Parameter(description = "Number of entries to generate", required = true) @PathVariable int count) {
        
        List<Map<String, Object>> generatedData = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            String id = UUID.randomUUID().toString();
            
            Map<String, Object> data = new HashMap<>();
            data.put("id", id);
            data.put("name", "Test Item " + (i + 1));
            data.put("value", new Random().nextInt(1000));
            data.put("active", new Random().nextBoolean());
            data.put("createdAt", new Date());
            
            dataStore.put(id, data);
            generatedData.add(data);
        }
        
        return ResponseEntity.ok(generatedData);
    }
}

