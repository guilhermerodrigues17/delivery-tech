package com.deliverytech.delivery_api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
public class HealthController {

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

    public record AppInfo(
            String application,
            String version,
            String developer,
            String javaVersion,
            String framework
    ) {
    }
}
