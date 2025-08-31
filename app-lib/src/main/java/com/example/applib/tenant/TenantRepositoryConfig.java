package com.example.applib.tenant;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Configuration for tenant repositories.
 * This ensures that the tenant repositories are properly scanned and configured.
 */
@Configuration
@EntityScan(basePackages = "com.example.applib.tenant")
@EnableJpaRepositories(
    basePackages = "com.example.applib.tenant",
    entityManagerFactoryRef = "entityManagerFactory",
    transactionManagerRef = "transactionManager"
)
public class TenantRepositoryConfig {
    // Configuration class to enable JPA repositories for tenant package
}

