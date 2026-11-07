package com.cesde.proyectointegrador.modules.health;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> apiRoot() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ONLINE");
        response.put("project", "Proyecto Integrador CESDE Gamified Challenges");
        response.put("database", "TiDB Cloud");
        response.put("endpoints", Arrays.asList(
                "/api/health",
                "/api/categorias",
                "/api/empresas",
                "/api/usuarios",
                "/api/retos",
                "/api/registros",
                "/api/auth/login",
                "/api/auth/register"
        ));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ONLINE");
        response.put("technology", "Java Spring Boot 3 + TiDB Cloud");
        response.put("project", "Proyecto Integrador CESDE Gamified Challenges");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("version", "1.0.0");
        return ResponseEntity.ok(response);
    }
}
