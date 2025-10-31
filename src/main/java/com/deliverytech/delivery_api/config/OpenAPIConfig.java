package com.deliverytech.delivery_api.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
@OpenAPIDefinition(
        info = @Info(
        title = "Delivery API",
        description = "API RESTful para o sistema de delivery da empresa Delivery Tech, " +
                "desenvolvido como parte da prática de Spring Boot 3 e Java 21.",
        version = "1.0.0",
        contact = @Contact(
                name = "Guilherme Rodrigues Machado",
                email = "guilhermerodriguesm23@gmail.com",
                url = "https://github.com/guilhermerodrigues17/delivery-tech"
        )),
        servers = {
                @Server(
                        description = "Ambiente de Desenvolvimento Local",
                        url = "http://localhost:8080"
                )
        })
public class OpenAPIConfig {}
