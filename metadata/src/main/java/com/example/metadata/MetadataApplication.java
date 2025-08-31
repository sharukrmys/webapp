package com.example.metadata;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {
    "com.example.metadata", 
    "com.example.applib.service", 
    "com.example.applib.config", 
    "com.example.applib.tenant"
})
@EntityScan(basePackages = {
    "com.example.metadata.entity", 
    "com.example.applib.entity", 
    "com.example.applib.tenant"
})
@EnableJpaRepositories(basePackages = {
    "com.example.metadata.repository", 
    "com.example.applib.repository", 
    "com.example.applib.tenant"
})
public class MetadataApplication {

    public static void main(String[] args) {
        SpringApplication.run(MetadataApplication.class, args);
    }
}

