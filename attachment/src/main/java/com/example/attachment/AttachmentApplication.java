package com.example.attachment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.example.attachment", "com.example.applib"})
@EntityScan(basePackages = {"com.example.attachment.model", "com.example.applib.tenant"})
@EnableJpaRepositories(basePackages = {"com.example.attachment.repository"})
public class AttachmentApplication {

    public static void main(String[] args) {
        SpringApplication.run(AttachmentApplication.class, args);
    }
}

