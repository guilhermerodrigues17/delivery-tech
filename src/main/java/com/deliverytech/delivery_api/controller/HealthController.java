package com.deliverytech.delivery_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@Tag(name = "Informações", description = "Endpoints para informações sobre a API")
public class HealthController {

    @Operation(summary = "Health Check", description = "Retorna informações sobre a saúde e o estado do servidor.")
    @ApiResponse(
            responseCode = "200",
            description = "Informações retornadas com sucesso",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(
                            type = "object",
                            additionalPropertiesSchema = String.class,
                            example =
                                    "{\"" +
                                    "status\": \"UP\", \"" +
                                    "timestamp\": \"2025-10-01T00:00:00Z\"" +
                                    "}"
                    )
            )
    )
    @GetMapping("health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = Map.of(
                "status", "UP",
                "timestamp", LocalDateTime.now().toString(),
                "service", "Delivery API",
                "javaVersion", System.getProperty("java.version")
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Informações do servidor", description = "Retorna informações técnicas sobre o servidor.")
    @ApiResponse(
            responseCode = "200",
            description = "Informações retornadas com sucesso",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppInfo.class)
            )
    )
    @GetMapping("info")
    public ResponseEntity<AppInfo> info() {
        var appInfo = new AppInfo(
                "Delivery Tech API",
                "1.0.0",
                "Guilherme Rodrigues",
                "JDK 21",
                "Spring Boot 3.5.6"
        );

        return ResponseEntity.ok(appInfo);
    }

    @Schema(description = "Informações técnicas do servidor")
    public record AppInfo(
            String application,
            String version,
            String developer,
            String javaVersion,
            String framework
    ) {
    }
}
