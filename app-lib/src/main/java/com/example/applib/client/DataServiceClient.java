package com.example.applib.client;

import java.util.List;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "data-service", url = "${services.data.url:http://data-service}")
public interface DataServiceClient {

    @GetMapping("/api/data/{id}")
    ResponseEntity<Map<String, Object>> getDataById(@PathVariable("id") String id);

    @GetMapping("/api/data")
    ResponseEntity<List<Map<String, Object>>> getAllData();

    @PostMapping("/api/data")
    ResponseEntity<Map<String, Object>> createData(@RequestBody Map<String, Object> data);
}
