package com.example.applib.config;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Base application configuration that can be extended by all modules.
 * This provides common component scanning, entity scanning, and repository configuration.
 * 
 * Usage example:
 * 
 * <pre>
 * @SpringBootApplication
 * @ComponentScan(basePackages = {"com.example.mymodule", "com.example.applib"})
 * @EntityScan(basePackages = {"com.example.mymodule.entity", "com.example.applib.entity", "com.example.applib.tenant"})
 * @EnableJpaRepositories(basePackages = {"com.example.mymodule.repository", "com.example.applib.repository", "com.example.applib.tenant"})
 * public class MyModuleApplication {
 *     public static void main(String[] args) {
 *         SpringApplication.run(MyModuleApplication.class, args);
 *     }
 * }
 * </pre>
 */
public abstract class BaseApplication {
    // This is a marker class to be extended by all module applications
}

