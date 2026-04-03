package com.coloio.srms.controller;

import com.coloio.srms.domain.enums.ServerStatus;
import com.coloio.srms.entity.ServerEntity;
import com.coloio.srms.pattern.strategy.AllocationResult;
import com.coloio.srms.service.ServerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/servers")
public class ServerController {

    private final ServerService serverService;

    public ServerController(ServerService serverService) {
        this.serverService = serverService;
    }

    @PostMapping
    @PreAuthorize("hasRole('DC_ADMIN')")
    public ResponseEntity<Map<String, Object>> registerServer(@RequestBody ServerEntity request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(serverService.toResponseMap(serverService.registerServer(request)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DC_ADMIN','TECHNICIAN')")
    public ResponseEntity<List<Map<String, Object>>> getAllServers() {
        return ResponseEntity.ok(
                serverService.getAllServers().stream().map(serverService::toResponseMap).toList()
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DC_ADMIN','TECHNICIAN','CUSTOMER')")
    public ResponseEntity<Map<String, Object>> getServer(@PathVariable Long id) {
        return ResponseEntity.ok(serverService.toResponseMap(serverService.getServer(id)));
    }

    @GetMapping("/rack/{rackId}")
    @PreAuthorize("hasAnyRole('DC_ADMIN','TECHNICIAN')")
    public ResponseEntity<List<Map<String, Object>>> getServersByRack(@PathVariable Long rackId) {
        return ResponseEntity.ok(
                serverService.getServersByRack(rackId).stream().map(serverService::toResponseMap).toList()
        );
    }

    @PostMapping("/{id}/allocate")
    @PreAuthorize("hasRole('DC_ADMIN')")
    public ResponseEntity<AllocationResult> allocate(
            @PathVariable Long id,
            @RequestParam(defaultValue = "FIRST_FIT") String strategy) {
        return ResponseEntity.ok(serverService.allocateToRack(id, strategy));
    }

    @PostMapping("/{id}/provision")
    @PreAuthorize("hasRole('DC_ADMIN')")
    public ResponseEntity<Map<String, Object>> provision(
            @PathVariable Long id,
            @RequestParam Long customerId) {
        return ResponseEntity.ok(
                serverService.toResponseMap(serverService.provisionToCustomer(id, customerId))
        );
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('DC_ADMIN','TECHNICIAN')")
    public ResponseEntity<Void> updateStatus(@PathVariable Long id,
                                             @RequestParam ServerStatus status) {
        serverService.updateStatus(id, status);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<Map<String, Object>>> getMyServers(
            @AuthenticationPrincipal UserDetails userDetails) {
        // Customer server lookup wired in Sprint 3 once MonitoringService is in place
        return ResponseEntity.ok(List.of());
    }
}
