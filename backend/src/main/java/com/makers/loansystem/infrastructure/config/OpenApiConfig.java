package com.makers.loansystem.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

    @Bean
    public OpenAPI loanSystemOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Gestión de Préstamos Bancarios")
                        .description("""
                                API RESTful para la gestión del ciclo de vida de préstamos bancarios.
                                
                                ### Roles y Accesos:
                                - **ROLE_USER**: Puede solicitar préstamos y consultar sus propias solicitudes.
                                - **ROLE_ADMIN**: Puede listar todas las solicitudes, aprobarlas o rechazarlas.
                                
                                ### Credenciales Semilla para Pruebas:
                                - **Usuario**: `usuario@test.com` / `123`
                                - **Admin**: `admin@test.com` / `123`
                                
                                Autentícate en `/api/v1/auth/login`, copia el token y colócalo en el botón **Authorize** superior.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Makers Tech Assessment")
                                .email("dev@makers.tech"))
                        .license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Ingresa el token JWT recibido tras el login en el formato: Bearer <token>")));
    }
}
