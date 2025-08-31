package com.example.usermanagement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/test-users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Test Users", description = "Endpoints for testing user management operations")
public class TestUserController {

    private final Map<String, Map<String, Object>> userStore = new HashMap<>();

    @GetMapping
    @Operation(summary = "Get all test users", description = "Returns all test users stored in memory")
    public ResponseEntity<Map<String, Map<String, Object>>> getAllUsers() {
        return ResponseEntity.ok(userStore);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get test user by ID", description = "Returns test user for a specific ID")
    public ResponseEntity<Map<String, Object>> getUserById(
            @Parameter(description = "User ID", required = true) @PathVariable String id) {
        
        if (userStore.containsKey(id)) {
            return ResponseEntity.ok(userStore.get(id));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @Operation(summary = "Create test user", description = "Creates a new test user and returns the ID")
    public ResponseEntity<Map<String, Object>> createUser(
            @Parameter(description = "User data", required = true) @RequestBody Map<String, Object> userData) {
        
        String id = UUID.randomUUID().toString();
        userData.put("id", id);
        userData.put("createdAt", new Date());
        
        userStore.put(id, userData);
        
        Map<String, Object> response = new HashMap<>();
        response.put("id", id);
        response.put("message", "User created successfully");
        response.put("user", userData);
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update test user", description = "Updates existing test user by ID")
    public ResponseEntity<Map<String, Object>> updateUser(
            @Parameter(description = "User ID", required = true) @PathVariable String id,
            @Parameter(description = "Updated user data", required = true) @RequestBody Map<String, Object> userData) {
        
        if (userStore.containsKey(id)) {
            Map<String, Object> existingUser = userStore.get(id);
            existingUser.putAll(userData);
            existingUser.put("updatedAt", new Date());
            
            userStore.put(id, existingUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", id);
            response.put("message", "User updated successfully");
            response.put("user", existingUser);
            
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete test user", description = "Deletes test user by ID")
    public ResponseEntity<Map<String, Object>> deleteUser(
            @Parameter(description = "User ID", required = true) @PathVariable String id) {
        
        if (userStore.containsKey(id)) {
            userStore.remove(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", id);
            response.put("message", "User deleted successfully");
            
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/generate/{count}")
    @Operation(summary = "Generate test users", description = "Generates a specified number of test users")
    public ResponseEntity<List<Map<String, Object>>> generateUsers(
            @Parameter(description = "Number of users to generate", required = true) @PathVariable int count) {
        
        List<Map<String, Object>> generatedUsers = new ArrayList<>();
        String[] roles = {"ADMIN", "USER", "MANAGER", "GUEST"};
        
        for (int i = 0; i < count; i++) {
            String id = UUID.randomUUID().toString();
            
            Map<String, Object> user = new HashMap<>();
            user.put("id", id);
            user.put("username", "user" + (i + 1));
            user.put("email", "user" + (i + 1) + "@example.com");
            user.put("firstName", "Test");
            user.put("lastName", "User" + (i + 1));
            user.put("role", roles[new Random().nextInt(roles.length)]);
            user.put("active", new Random().nextBoolean());
            user.put("createdAt", new Date());
            
            userStore.put(id, user);
            generatedUsers.add(user);
        }
        
        return ResponseEntity.ok(generatedUsers);
    }
}

