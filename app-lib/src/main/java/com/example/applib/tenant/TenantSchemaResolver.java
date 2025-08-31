package com.example.applib.tenant;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TenantSchemaResolver {

    public String resolveCurrentTenantIdentifier() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId != null) {
            log.debug("Resolved tenant ID: {}", tenantId);
            return tenantId;
        }

        log.debug("No tenant ID found in context, using default");
        return "default";
    }
}
