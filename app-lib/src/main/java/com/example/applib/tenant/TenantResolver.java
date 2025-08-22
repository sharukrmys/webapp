package com.example.applib.tenant;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
public class TenantResolver {

    private static final String DEFAULT_TENANT_ID = "default";
    
    @Value("${multitenancy.tenant-header:X-Tenant-ID}")
    private String tenantHeader;
    
    @Value("${multitenancy.tenant-parameter:tenantId}")
    private String tenantParameter;
    
    @Value("${multitenancy.default-tenant:default}")
    private String defaultTenant;

    public String resolveTenantId(HttpServletRequest request) {
        // Try to get tenant from header
        String tenantId = request.getHeader(tenantHeader);
        
        // If not in header, try to get from request parameter
        if (!StringUtils.hasText(tenantId)) {
            tenantId = request.getParameter(tenantParameter);
        }
        
        // If still not found, try to extract from subdomain
        if (!StringUtils.hasText(tenantId)) {
            tenantId = extractTenantFromSubdomain(request);
        }
        
        // If still not found, use default tenant
        if (!StringUtils.hasText(tenantId)) {
            log.debug("No tenant ID found in request, using default tenant: {}", defaultTenant);
            return defaultTenant;
        }
        
        log.debug("Resolved tenant ID: {}", tenantId);
        return tenantId;
    }
    
    private String extractTenantFromSubdomain(HttpServletRequest request) {
        String host = request.getServerName();
        if (host.contains(".")) {
            String subdomain = host.split("\\.")[0];
            if (!subdomain.equals("www") && StringUtils.hasText(subdomain)) {
                return subdomain;
            }
        }
        return null;
    }
}
