package com.university.universitymanagement.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI universityOpenApi() {
        return new OpenAPI().info(new Info()
                .title("University Course Management API")
                .description("REST API for managing students, departments, courses and enrollments")
                .version("1.0.0"));
    }
}
