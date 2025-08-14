package com.example.applib.tenant;

import org.springframework.util.StringUtils;

public class TenantContext {
    
    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();
    
    public static void setTenantId(String tenantId) {
        CURRENT_TENANT.set(tenantId);
    }
    
    public static String getTenantId() {
        return CURRENT_TENANT.get();
    }
    
    public static void clear() {
        CURRENT_TENANT.remove();
    }
    
    public static boolean hasTenant() {
        return StringUtils.hasText(getTenantId());
    }
}

