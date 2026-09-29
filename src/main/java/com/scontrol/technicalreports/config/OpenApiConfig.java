package com.scontrol.technicalreports.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

/**
 * Metadatos de la especificación OpenAPI y esquema de seguridad JWT (Bearer) de la API.
 *
 * @author Roger Rojas Effio - roger.rojas@rmsolutions.pe
 */
@Configuration
public class OpenApiConfig {

    public static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI technicalReportsOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Technical Reports API — Scontrol Ingeniería")
                        .version("1.0.0")
                        .description("API REST para registrar informes técnicos de mantenimiento de equipos "
                                + "y generar el documento PDF. Todos los endpoints, salvo el login, requieren "
                                + "un JWT en la cabecera Authorization: Bearer <token>.")
                        .contact(new Contact().name("Roger Rojas Effio").email("roger.rojas@rmsolutions.pe")))
                // Servidor fijo para que la especificación exportada no dependa del host de la petición
                .addServersItem(new Server().url("http://localhost:8080").description("Local"))
                .components(new Components().addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
    }
}
