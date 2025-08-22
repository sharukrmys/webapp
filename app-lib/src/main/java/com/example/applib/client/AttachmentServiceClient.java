package com.example.applib.client;

import java.util.List;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(name = "attachment-service", url = "${services.attachment.url:http://attachment-service}")
public interface AttachmentServiceClient {

    @GetMapping("/api/attachments/{id}")
    ResponseEntity<Map<String, Object>> getAttachmentById(@PathVariable("id") String id);

    @GetMapping("/api/attachments")
    ResponseEntity<List<Map<String, Object>>> getAllAttachments();

    @PostMapping(value = "/api/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<Map<String, Object>> uploadAttachment(
            @RequestPart("file") MultipartFile file,
            @RequestParam("metadata") String metadata);

    @DeleteMapping("/api/attachments/{id}")
    ResponseEntity<Void> deleteAttachment(@PathVariable("id") String id);
}
