package com.example.applib.client;

import java.util.List;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "user-management-service", url = "${services.user-management.url:http://user-management-service}")
public interface UserManagementServiceClient {

    @GetMapping("/api/users/{id}")
    ResponseEntity<Map<String, Object>> getUserById(@PathVariable("id") String id);

    @GetMapping("/api/users")
    ResponseEntity<List<Map<String, Object>>> getAllUsers();

    @PostMapping("/api/users")
    ResponseEntity<Map<String, Object>> createUser(@RequestBody Map<String, Object> user);

    @PutMapping("/api/users/{id}")
    ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable("id") String id,
            @RequestBody Map<String, Object> user);

    @DeleteMapping("/api/users/{id}")
    ResponseEntity<Void> deleteUser(@PathVariable("id") String id);

    @PostMapping("/api/auth/login")
    ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials);

    @PostMapping("/api/auth/refresh")
    ResponseEntity<Map<String, Object>> refreshToken(@RequestBody Map<String, String> refreshToken);
}
