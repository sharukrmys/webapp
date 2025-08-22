package com.example.applib.config;

import com.example.applib.entity.MasterTenant;
import com.example.applib.repository.MasterTenantRepository;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = "com.example.applib.repository",
    entityManagerFactoryRef = "masterEntityManagerFactory",
    transactionManagerRef = "masterTransactionManager"
)
@RequiredArgsConstructor
@Slf4j
public class DatabaseConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.master")
    public DataSourceProperties masterDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    public DataSource masterDataSource() {
        return masterDataSourceProperties()
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean masterEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(masterDataSource());
        em.setPackagesToScan("com.example.applib.entity");
        
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "validate");
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.put("hibernate.format_sql", "true");
        em.setJpaPropertyMap(properties);
        
        return em;
    }

    @Bean
    @Primary
    public PlatformTransactionManager masterTransactionManager(
            @Qualifier("masterEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean
    @Primary
    public JdbcTemplate masterJdbcTemplate() {
        return new JdbcTemplate(masterDataSource());
    }

    @Bean
    @Primary
    public NamedParameterJdbcTemplate masterNamedParameterJdbcTemplate() {
        return new NamedParameterJdbcTemplate(masterDataSource());
    }

    @Bean
    public JdbcTemplate tacJdbcTemplate(MasterTenantRepository masterTenantRepository) {
        MasterTenant tenant = masterTenantRepository.findAll().stream()
                .filter(MasterTenant::getIsActive)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No active tenant found"));
        
        return new JdbcTemplate(createTenantDataSource(tenant.getUrl(), tenant.getUsername(), tenant.getPassword()));
    }

    @Bean
    public JdbcTemplate flexJdbcTemplate(MasterTenantRepository masterTenantRepository) {
        MasterTenant tenant = masterTenantRepository.findAll().stream()
                .filter(MasterTenant::getIsActive)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No active tenant found"));
        
        return new JdbcTemplate(createTenantDataSource(tenant.getFlexDb(), tenant.getUsername(), tenant.getPassword()));
    }

    @Bean
    public JdbcTemplate readJdbcTemplate(MasterTenantRepository masterTenantRepository) {
        MasterTenant tenant = masterTenantRepository.findAll().stream()
                .filter(MasterTenant::getIsActive)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No active tenant found"));
        
        return new JdbcTemplate(createTenantDataSource(tenant.getReadDb(), tenant.getUsername(), tenant.getPassword()));
    }

    @Bean
    public JdbcTemplate appstoreJdbcTemplate(MasterTenantRepository masterTenantRepository) {
        MasterTenant tenant = masterTenantRepository.findAll().stream()
                .filter(MasterTenant::getIsActive)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No active tenant found"));
        
        return new JdbcTemplate(createTenantDataSource(tenant.getAppstoreDb(), tenant.getUsername(), tenant.getPassword()));
    }

    private DataSource createTenantDataSource(String url, String username, String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("org.postgresql.Driver");
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(5);
        
        return new HikariDataSource(config);
    }
}
