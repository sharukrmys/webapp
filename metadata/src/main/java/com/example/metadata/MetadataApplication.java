package com.example.metadata;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.example.metadata", "com.example.applib"})
public class MetadataApplication {

    public static void main(String[] args) {
        SpringApplication.run(MetadataApplication.class, args);
    }
}
