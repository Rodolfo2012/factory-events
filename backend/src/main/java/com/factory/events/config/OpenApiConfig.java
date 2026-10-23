package com.factory.events.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI factoryEventsOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Factory Events API")
                .version("1.0.0")
                .description("API para recebimento, registro e consulta de eventos de equipamentos de fábrica."));
    }
}
