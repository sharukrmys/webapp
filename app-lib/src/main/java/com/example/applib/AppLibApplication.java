package com.example.applib;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * This is a simple application class to verify that Spring Boot and Spring Cloud annotations work correctly.
 * This class is not meant to be run as a standalone application, but rather to be used as a base class for
 * other modules to extend.
 */
@SpringBootApplication
@EnableFeignClients
public class AppLibApplication {

    public static void main(String[] args) {
        SpringApplication.run(AppLibApplication.class, args);
    }
}
