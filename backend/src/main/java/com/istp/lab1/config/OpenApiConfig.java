package com.istp.lab1.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ISTP Lab 1 API")
                        .description("Distance learning system API")
                        .version("v1"))
                .servers(List.of(new Server()
                        .url("http://localhost:8080")
                        .description("Local development server")));
    }
}
