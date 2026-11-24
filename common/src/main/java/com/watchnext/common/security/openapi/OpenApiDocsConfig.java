package com.watchnext.common.security.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// documentacion swagger comun: todas las pruebas pasan por el gateway y llevan el token
@Configuration
@ConditionalOnClass(name = "io.swagger.v3.oas.models.OpenAPI")
public class OpenApiDocsConfig {

    @Bean
    public OpenAPI watchNextOpenApi(
        @Value("${spring.application.name:watchnext}") String serviceName
    ) {
        // 1. servidor relativo: swagger ui llama al mismo gateway donde esta abierto
        return new OpenAPI()
            .info(new Info().title("WatchNext · " + serviceName).version("v1"))
            .servers(List.of(new Server().url("/").description("gateway")))
            // 2. boton "Authorize" para pegar el token del login
            .components(
                new Components().addSecuritySchemes(
                    "bearer",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                )
            )
            .addSecurityItem(new SecurityRequirement().addList("bearer"));
    }
}
