package com.example.usermanagement.controller;

import com.example.applib.service.InterServiceCommunicationService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user-data")
@RequiredArgsConstructor
@Slf4j
public class UserDataController {

    private final InterServiceCommunicationService interServiceCommunicationService;

    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> getUserData(@PathVariable String userId) {
        return interServiceCommunicationService.getUserData(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<String> createUserWithData(
            @RequestBody Map<String, Object> request) {
        
        @SuppressWarnings("unchecked")
        Map<String, Object> userData = (Map<String, Object>) request.get("user");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> dataRecord = (Map<String, Object>) request.get("data");
        
        if (userData == null || dataRecord == null) {
            return ResponseEntity.badRequest().body("Both 'user' and 'data' are required");
        }
        
        boolean success = interServiceCommunicationService.createUserWithData(userData, dataRecord);
        
        if (success) {
            return ResponseEntity.ok("User and data created successfully");
        } else {
            return ResponseEntity.internalServerError().body("Failed to create user and data");
        }
    }
}
