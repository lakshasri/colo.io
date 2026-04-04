package com.coloio.srms.controller;

import com.coloio.srms.repository.AlertRepository;
import com.coloio.srms.repository.ServerRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@Tag(name = "Health", description = "System health and status")
@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final ServerRepository serverRepository;
    private final AlertRepository alertRepository;

    public HealthController(ServerRepository serverRepository, AlertRepository alertRepository) {
        this.serverRepository = serverRepository;
        this.alertRepository = alertRepository;
    }

    @Operation(summary = "System health check")
    @GetMapping
    public Map<String, Object> health() {
        return Map.of(
                "status", "UP",
                "timestamp", LocalDateTime.now().toString(),
                "serverCount", serverRepository.count(),
                "activeAlerts", alertRepository.count()
        );
    }
}
