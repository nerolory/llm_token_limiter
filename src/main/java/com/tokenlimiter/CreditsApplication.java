package com.tokenlimiter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the token credit and consumption microservice.
 */
@SpringBootApplication
public class CreditsApplication {

    /**
     * Starts the Spring Boot application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(CreditsApplication.class, args);
    }
}
