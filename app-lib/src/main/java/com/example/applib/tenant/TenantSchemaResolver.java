package com.example.applib.tenant;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.util.StringUtils;

@Slf4j
public class TenantSchemaResolver implements CurrentTenantIdentifierResolver<String> {

    private static final String DEFAULT_TENANT = "default";

    @Override
    public String resolveCurrentTenantIdentifier() {
        String tenantId = TenantContext.getTenantId();
        if (!StringUtils.hasText(tenantId)) {
            log.warn("No tenant ID found in context, using default tenant");
            return DEFAULT_TENANT;
        }
        return tenantId;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}

