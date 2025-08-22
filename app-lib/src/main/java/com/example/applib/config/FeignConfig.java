package com.example.applib.config;

import com.example.applib.tenant.TenantContext;
import feign.Logger;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import feign.okhttp.OkHttpClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.example")
@Slf4j
public class FeignConfig {

    private static final String TENANT_HEADER = "X-TenantID";

    @Bean
    public OkHttpClient client() {
        return new OkHttpClient();
    }

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public RequestInterceptor tenantRequestInterceptor() {
        return requestTemplate -> {
            String tenantId = TenantContext.getTenantId();
            if (tenantId != null && !tenantId.isEmpty()) {
                log.debug("Adding tenant header to Feign request: {}", tenantId);
                requestTemplate.header(TENANT_HEADER, tenantId);
            } else {
                log.warn("No tenant ID found in context for Feign request");
            }
        };
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new FeignErrorDecoder();
    }

    public static class FeignErrorDecoder implements ErrorDecoder {
        private final ErrorDecoder defaultErrorDecoder = new Default();

        @Override
        public Exception decode(String methodKey, feign.Response response) {
            log.error("Error in Feign client call: {} - Status: {}", methodKey, response.status());
            
            // You can customize error handling based on response status or body
            if (response.status() >= 500) {
                log.error("Server error in Feign client call");
                // You could return a custom exception here
            } else if (response.status() == 404) {
                log.error("Resource not found in Feign client call");
                // You could return a custom exception here
            }
            
            // Default error handling
            return defaultErrorDecoder.decode(methodKey, response);
        }
    }
}
