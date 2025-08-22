package com.example.applib.service;

import com.example.applib.client.AttachmentServiceClient;
import com.example.applib.client.DataServiceClient;
import com.example.applib.client.UserManagementServiceClient;
import feign.FeignException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterServiceCommunicationService {

    private final DataServiceClient dataServiceClient;
    private final AttachmentServiceClient attachmentServiceClient;
    private final UserManagementServiceClient userManagementServiceClient;

    public Optional<Map<String, Object>> getUserData(String userId) {
        try {
            // Get user details
            ResponseEntity<Map<String, Object>> userResponse = userManagementServiceClient.getUserById(userId);
            
            if (!userResponse.getStatusCode().is2xxSuccessful() || userResponse.getBody() == null) {
                log.error("Failed to get user with ID: {}", userId);
                return Optional.empty();
            }
            
            Map<String, Object> userData = userResponse.getBody();
            
            // Get user's data
            try {
                ResponseEntity<List<Map<String, Object>>> dataResponse = dataServiceClient.getAllData();
                if (dataResponse.getStatusCode().is2xxSuccessful() && dataResponse.getBody() != null) {
                    userData.put("data", dataResponse.getBody());
                } else {
                    userData.put("data", Collections.emptyList());
                }
            } catch (FeignException e) {
                log.error("Error fetching data for user {}: {}", userId, e.getMessage());
                userData.put("data", Collections.emptyList());
            }
            
            // Get user's attachments
            try {
                ResponseEntity<List<Map<String, Object>>> attachmentsResponse = attachmentServiceClient.getAllAttachments();
                if (attachmentsResponse.getStatusCode().is2xxSuccessful() && attachmentsResponse.getBody() != null) {
                    userData.put("attachments", attachmentsResponse.getBody());
                } else {
                    userData.put("attachments", Collections.emptyList());
                }
            } catch (FeignException e) {
                log.error("Error fetching attachments for user {}: {}", userId, e.getMessage());
                userData.put("attachments", Collections.emptyList());
            }
            
            return Optional.of(userData);
            
        } catch (FeignException e) {
            log.error("Error in inter-service communication: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public boolean createUserWithData(Map<String, Object> userData, Map<String, Object> dataRecord) {
        try {
            // Create user
            ResponseEntity<Map<String, Object>> userResponse = userManagementServiceClient.createUser(userData);
            
            if (!userResponse.getStatusCode().is2xxSuccessful() || userResponse.getBody() == null) {
                log.error("Failed to create user");
                return false;
            }
            
            Map<String, Object> createdUser = userResponse.getBody();
            String userId = createdUser.get("id").toString();
            
            // Add user ID to data record
            dataRecord.put("userId", userId);
            
            // Create data record
            try {
                ResponseEntity<Map<String, Object>> dataResponse = dataServiceClient.createData(dataRecord);
                if (!dataResponse.getStatusCode().is2xxSuccessful()) {
                    log.error("Failed to create data record for user {}", userId);
                    // Consider rolling back user creation here
                    return false;
                }
            } catch (FeignException e) {
                log.error("Error creating data for user {}: {}", userId, e.getMessage());
                // Consider rolling back user creation here
                return false;
            }
            
            return true;
            
        } catch (FeignException e) {
            log.error("Error in inter-service communication: {}", e.getMessage());
            return false;
        }
    }
}
