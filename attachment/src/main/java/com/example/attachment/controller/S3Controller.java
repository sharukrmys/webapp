package com.example.attachment.controller;

import com.example.applib.entity.TurboS3Config;
import com.example.applib.repository.TurboS3ConfigRepository;
import com.example.applib.util.S3Util;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/s3")
@RequiredArgsConstructor
public class S3Controller {

    private final S3Util s3Util;
    private final TurboS3ConfigRepository s3ConfigRepository;

    @GetMapping("/config")
    public ResponseEntity<List<TurboS3Config>> getS3Config() {
        List<TurboS3Config> s3ConfigList = s3ConfigRepository.findAll();
        return ResponseEntity.ok(s3ConfigList);
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // Create a temporary file
            Path tempFile = Files.createTempFile("upload-", "-" + file.getOriginalFilename());
            file.transferTo(tempFile.toFile());

            // Generate a unique key for the file
            String keyName = UUID.randomUUID().toString() + "-" + file.getOriginalFilename();

            // Upload the file to S3
            s3Util.uploadFile(tempFile.toString(), keyName);

            // Delete the temporary file
            Files.delete(tempFile);

            return ResponseEntity.ok(Map.of(
                    "message", "File uploaded successfully",
                    "keyName", keyName
            ));
        } catch (IOException e) {
            log.error("Error uploading file to S3", e);
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Error uploading file: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/download/{keyName}")
    public ResponseEntity<Map<String, String>> downloadFile(@PathVariable String keyName) {
        try {
            // Create a temporary file to download to
            Path tempFile = Files.createTempFile("download-", "");

            // Download the file from S3
            s3Util.downloadFile(keyName, tempFile.toString());

            return ResponseEntity.ok(Map.of(
                    "message", "File downloaded successfully",
                    "filePath", tempFile.toString()
            ));
        } catch (IOException e) {
            log.error("Error downloading file from S3", e);
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Error downloading file: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/presigned-download/{keyName}")
    public ResponseEntity<Map<String, String>> getPresignedDownloadUrl(
            @PathVariable String keyName,
            @RequestParam(defaultValue = "60") int expirationMinutes) {

        URL presignedUrl = s3Util.generatePresignedDownloadUrl(keyName, expirationMinutes);

        if (presignedUrl != null) {
            return ResponseEntity.ok(Map.of(
                    "presignedUrl", presignedUrl.toString(),
                    "expiresIn", expirationMinutes + " minutes"
            ));
        } else {
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Error generating pre-signed URL"
            ));
        }
    }

    @GetMapping("/presigned-upload/{keyName}")
    public ResponseEntity<Map<String, String>> getPresignedUploadUrl(
            @PathVariable String keyName,
            @RequestParam(defaultValue = "60") int expirationMinutes) {

        URL presignedUrl = s3Util.generatePresignedUploadUrl(keyName, expirationMinutes);

        if (presignedUrl != null) {
            return ResponseEntity.ok(Map.of(
                    "presignedUrl", presignedUrl.toString(),
                    "expiresIn", expirationMinutes + " minutes"
            ));
        } else {
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Error generating pre-signed URL"
            ));
        }
    }

    @DeleteMapping("/{keyName}")
    public ResponseEntity<Map<String, String>> deleteFile(@PathVariable String keyName) {
        boolean deleted = s3Util.deleteObject(keyName);

        if (deleted) {
            return ResponseEntity.ok(Map.of(
                    "message", "File deleted successfully"
            ));
        } else {
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Error deleting file"
            ));
        }
    }

    @GetMapping("/exists/{keyName}")
    public ResponseEntity<Map<String, Boolean>> doesObjectExist(@PathVariable String keyName) {
        boolean exists = s3Util.doesObjectExist(keyName);

        return ResponseEntity.ok(Map.of(
                "exists", exists
        ));
    }
}
