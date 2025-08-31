package com.example.applib.tenant;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Properties;

/**
 * Configuration class for tenant-specific data sources.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = {"com.example.*.repository"},
        excludeFilters = @org.springframework.context.annotation.ComponentScan.Filter(
                type = org.springframework.context.annotation.FilterType.REGEX,
                pattern = "com.example.applib.tenant.*"
        ),
        entityManagerFactoryRef = "tenantEntityManagerFactory",
        transactionManagerRef = "tenantTransactionManager"
)
public class TenantDataSourceConfig {

    private final TenantDataSource tenantDataSource;

    @Value("${spring.jpa.database-platform}")
    private String databasePlatform;

    @Value("${spring.jpa.hibernate.ddl-auto:update}")
    private String ddlAuto;

    @Value("${spring.jpa.properties.hibernate.format_sql:true}")
    private String formatSql;

    @Value("${spring.jpa.properties.hibernate.show_sql:false}")
    private String showSql;

    /**
     * Creates a routing data source that selects the appropriate tenant data source
     * based on the current tenant context.
     */
    @Bean
    public DataSource tenantRoutingDataSource() {
        TenantRoutingDataSource routingDataSource = new TenantRoutingDataSource();
        routingDataSource.setTargetDataSources(new HashMap<>());
        routingDataSource.setDefaultTargetDataSource(tenantDataSource.getDefaultDataSource());
        return routingDataSource;
    }

    /**
     * Wraps the routing data source in a lazy connection proxy to defer
     * getting a connection until it's actually needed.
     */
    @Bean
    @Primary
    public DataSource tenantDataSource() {
        return new LazyConnectionDataSourceProxy(tenantRoutingDataSource());
    }

    /**
     * Creates a JDBC template for the tenant data source.
     */
    @Bean
    public JdbcTemplate tenantJdbcTemplate(@Qualifier("tenantDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    /**
     * Creates a JDBC template for the master tenant data source.
     */
    @Bean
    public JdbcTemplate tacJdbcTemplate(@Qualifier("masterDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    /**
     * Creates an entity manager factory for tenant entities.
     */
    @Primary
    @Bean
    public LocalContainerEntityManagerFactoryBean tenantEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(tenantDataSource());
        em.setPackagesToScan("com.example.*.entity");
        
        // Exclude the master tenant entity using Java configuration
        em.setPackagesToExclude("com.example.applib.tenant");
        
        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        em.setJpaProperties(hibernateProperties());
        return em;
    }

    /**
     * Creates a transaction manager for tenant transactions.
     */
    @Primary
    @Bean
    public PlatformTransactionManager tenantTransactionManager(
            @Qualifier("tenantEntityManagerFactory") EntityManagerFactory emf) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(emf);
        return transactionManager;
    }

    /**
     * Creates a post-processor for translating JPA exceptions to Spring's
     * DataAccessException hierarchy.
     */
    @Bean
    public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
        return new PersistenceExceptionTranslationPostProcessor();
    }

    /**
     * Configures Hibernate properties for the tenant entity manager factory.
     */
    private Properties hibernateProperties() {
        Properties properties = new Properties();
        properties.put("hibernate.dialect", databasePlatform);
        properties.put("hibernate.hbm2ddl.auto", ddlAuto);
        properties.put("hibernate.format_sql", formatSql);
        properties.put("hibernate.show_sql", showSql);
        properties.put("hibernate.multiTenancy", "DATABASE");
        properties.put("hibernate.tenant_identifier_resolver", "com.example.applib.tenant.TenantSchemaResolver");
        properties.put("hibernate.jdbc.lob.non_contextual_creation", "true");
        return properties;
    }

    /**
     * Creates a data source for a specific tenant.
     */
    public DataSource createAndConfigureDataSource(MasterTenant masterTenant) {
        HikariDataSource ds = new HikariDataSource();
        ds.setUsername(masterTenant.getUsername());
        ds.setPassword(masterTenant.getPassword());
        ds.setJdbcUrl(masterTenant.getUrl());
        ds.setDriverClassName("org.postgresql.Driver");
        
        // HikariCP settings
        ds.setConnectionTimeout(masterTenant.getConnectionTimeout() != null ? 
                masterTenant.getConnectionTimeout() : 30000);
        ds.setIdleTimeout(masterTenant.getIdleTimeout() != null ? 
                masterTenant.getIdleTimeout() : 600000);
        ds.setMaximumPoolSize(masterTenant.getMaxPoolSize() != null ? 
                masterTenant.getMaxPoolSize() : 10);
        ds.setMinimumIdle(masterTenant.getMinIdle() != null ? 
                masterTenant.getMinIdle() : 2);
        ds.setPoolName("HikariPool-" + masterTenant.getTenantId());
        
        log.info("Configured datasource for tenant {}. Connection pool name: {}", 
                masterTenant.getTenantId(), ds.getPoolName());
        return ds;
    }
}

