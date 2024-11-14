package com.project.botapi;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@OpenAPIDefinition
@EnableRetry
public class BotApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(BotApiApplication.class, args);
    }

}
