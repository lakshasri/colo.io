package com.coloio.srms.controller;

import com.coloio.srms.entity.AlertEntity;
import com.coloio.srms.repository.UserRepository;
import com.coloio.srms.service.AlertService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alertService;
    private final UserRepository userRepository;

    public AlertController(AlertService alertService, UserRepository userRepository) {
        this.alertService = alertService;
        this.userRepository = userRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DC_ADMIN','TECHNICIAN','MANAGER')")
    public ResponseEntity<List<AlertEntity>> getActiveAlerts() {
        return ResponseEntity.ok(alertService.getActiveAlerts());
    }

    @PutMapping("/{id}/acknowledge")
    @PreAuthorize("hasAnyRole('DC_ADMIN','TECHNICIAN')")
    public ResponseEntity<AlertEntity> acknowledge(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow().getUserId();
        return ResponseEntity.ok(alertService.acknowledge(id, userId));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('DC_ADMIN','MANAGER')")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(alertService.getStats());
    }

    @GetMapping("/source/{sourceType}/{sourceId}")
    @PreAuthorize("hasAnyRole('DC_ADMIN','TECHNICIAN','MANAGER')")
    public ResponseEntity<List<AlertEntity>> getBySource(
            @PathVariable String sourceType,
            @PathVariable String sourceId) {
        return ResponseEntity.ok(alertService.getAlertsBySource(sourceType, sourceId));
    }
}
