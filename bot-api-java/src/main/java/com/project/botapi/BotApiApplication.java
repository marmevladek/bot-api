package com.project.botapi;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition
public class BotApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(BotApiApplication.class, args);
    }

}
