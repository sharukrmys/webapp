package com.example.applib.tenant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Slf4j
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = {"com.example.applib.tenant", "com.example.applib.repository"},
        entityManagerFactoryRef = "masterEntityManagerFactory",
        transactionManagerRef = "masterTransactionManager"
)
@RequiredArgsConstructor
public class TenantDataSourceConfig {

    private final MasterTenantRepository masterTenantRepository;
    private final ObjectMapper objectMapper;

    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean masterEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(masterDataSource());
        em.setPackagesToScan("com.example.applib.tenant", "com.example.applib.model");

        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.put("hibernate.show_sql", "true");
        properties.put("hibernate.format_sql", "true");

        em.setJpaPropertyMap(properties);

        return em;
    }

    @Bean
    @Primary
    public DataSource masterDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setJdbcUrl("jdbc:postgresql://localhost:5432/master_db");
        dataSource.setUsername("postgres");
        dataSource.setPassword("postgres");
        dataSource.setConnectionTimeout(30000);
        dataSource.setIdleTimeout(600000);
        dataSource.setMaxLifetime(1800000);
        dataSource.setMaximumPoolSize(10);
        dataSource.setMinimumIdle(5);
        return dataSource;
    }

    @Bean
    @Primary
    public JdbcTemplate masterJdbcTemplate(@Qualifier("masterDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    @Primary
    public PlatformTransactionManager masterTransactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(masterEntityManagerFactory().getObject());
        return transactionManager;
    }

    @Bean
    public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
        return new PersistenceExceptionTranslationPostProcessor();
    }

    @Bean
    public TenantRoutingDataSource tenantRoutingDataSource() {
        TenantRoutingDataSource dataSource = new TenantRoutingDataSource();
        dataSource.setDefaultTargetDataSource(masterDataSource());
        dataSource.setTargetDataSources(new HashMap<>());
        return dataSource;
    }

    @Bean
    public JdbcTemplate tenantJdbcTemplate(@Qualifier("tenantRoutingDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public DataSource tacDataSource() {
        return new TenantAwareDataSource("tac");
    }

    @Bean
    public JdbcTemplate tacJdbcTemplate(@Qualifier("tacDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public DataSource flexDataSource() {
        return new TenantAwareDataSource("flex");
    }

    @Bean
    public JdbcTemplate flexJdbcTemplate(@Qualifier("flexDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public DataSource readDataSource() {
        return new TenantAwareDataSource("read");
    }

    @Bean
    public JdbcTemplate readJdbcTemplate(@Qualifier("readDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public DataSource appstoreDataSource() {
        return new TenantAwareDataSource("appstore");
    }

    @Bean
    public JdbcTemplate appstoreJdbcTemplate(@Qualifier("appstoreDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @PostConstruct
    public void loadTenants() {
        log.info("Loading all tenants from the master database");

        Iterable<MasterTenant> tenants = masterTenantRepository.findByIsActiveTrue();
        Map<Object, Object> tenantDataSources = new HashMap<>();

        for (MasterTenant tenant : tenants) {
            try {
                DataSource dataSource = createDataSource(tenant);
                tenantDataSources.put(tenant.getTenantId(), dataSource);
                log.info("Loaded tenant: {}", tenant.getTenantId());
            } catch (Exception e) {
                log.error("Error loading tenant: {}", tenant.getTenantId(), e);
            }
        }

        TenantRoutingDataSource tenantRoutingDataSource = tenantRoutingDataSource();
        tenantRoutingDataSource.setTargetDataSources(tenantDataSources);
        tenantRoutingDataSource.afterPropertiesSet();

        log.info("Loaded {} active tenants", tenantDataSources.size());
    }

    private DataSource createDataSource(MasterTenant tenant) {
        try {
            HikariDataSource dataSource = new HikariDataSource();
            dataSource.setDriverClassName("org.postgresql.Driver");
            dataSource.setJdbcUrl(tenant.getUrl());
            dataSource.setUsername(tenant.getUsername());
            dataSource.setPassword(tenant.getPassword());

            // Parse DB properties from JSON string
            if (tenant.getDbProperties() != null && !tenant.getDbProperties().isEmpty()) {
                Map<String, Object> dbProps = objectMapper.readValue(tenant.getDbProperties(), Map.class);

                if (dbProps.containsKey("connectionTimeout") && tenant.getConnectionTimeout() == null) {
                    dataSource.setConnectionTimeout(Long.parseLong(dbProps.get("connectionTimeout").toString()));
                } else if (tenant.getConnectionTimeout() != null) {
                    dataSource.setConnectionTimeout(tenant.getConnectionTimeout());
                }

                if (dbProps.containsKey("idleTimeout") && tenant.getIdleTimeout() == null) {
                    dataSource.setIdleTimeout(Long.parseLong(dbProps.get("idleTimeout").toString()));
                } else if (tenant.getIdleTimeout() != null) {
                    dataSource.setIdleTimeout(tenant.getIdleTimeout());
                }

                if (dbProps.containsKey("maxPoolSize") && tenant.getMaxPoolSize() == null) {
                    dataSource.setMaximumPoolSize(Integer.parseInt(dbProps.get("maxPoolSize").toString()));
                } else if (tenant.getMaxPoolSize() != null) {
                    dataSource.setMaximumPoolSize(tenant.getMaxPoolSize());
                }

                if (dbProps.containsKey("minIdle") && tenant.getMinIdle() == null) {
                    dataSource.setMinimumIdle(Integer.parseInt(dbProps.get("minIdle").toString()));
                } else if (tenant.getMinIdle() != null) {
                    dataSource.setMinimumIdle(tenant.getMinIdle());
                }
            } else {
                // Default values
                dataSource.setConnectionTimeout(30000);
                dataSource.setIdleTimeout(600000);
                dataSource.setMaxLifetime(1800000);
                dataSource.setMaximumPoolSize(10);
                dataSource.setMinimumIdle(5);
            }

            return dataSource;
        } catch (Exception e) {
            log.error("Error creating data source for tenant: {}", tenant.getTenantId(), e);
            throw new RuntimeException("Failed to create data source for tenant: " + tenant.getTenantId(), e);
        }
    }
}
