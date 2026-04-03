package com.coloio.srms.controller;

import com.coloio.srms.domain.enums.ServerStatus;
import com.coloio.srms.entity.ServerEntity;
import com.coloio.srms.entity.ServerMetricEntity;
import com.coloio.srms.entity.UserEntity;
import com.coloio.srms.pattern.strategy.AllocationResult;
import com.coloio.srms.repository.ServerMetricRepository;
import com.coloio.srms.repository.UserRepository;
import com.coloio.srms.service.ServerService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/servers")
public class ServerController {

    private final ServerService serverService;
    private final ServerMetricRepository metricRepository;
    private final UserRepository userRepository;

    public ServerController(ServerService serverService,
                            ServerMetricRepository metricRepository,
                            UserRepository userRepository) {
        this.serverService = serverService;
        this.metricRepository = metricRepository;
        this.userRepository = userRepository;
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

    @GetMapping("/{id}/metrics")
    @PreAuthorize("hasAnyRole('DC_ADMIN','TECHNICIAN','CUSTOMER')")
    public ResponseEntity<Map<String, Object>> getLatestMetrics(@PathVariable Long id) {
        ServerEntity server = serverService.getServer(id);
        Optional<ServerMetricEntity> metric = metricRepository.findTopByServer_ServerIdOrderByRecordedAtDesc(id);
        if (metric.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "serverId", server.getServerId(),
                    "hostname", server.getHostname(),
                    "cpuUsagePercent", 0.0,
                    "ramUsagePercent", 0.0,
                    "diskUsagePercent", 0.0,
                    "recordedAt", null
            ));
        }
        ServerMetricEntity m = metric.get();
        return ResponseEntity.ok(Map.of(
                "serverId", server.getServerId(),
                "hostname", server.getHostname(),
                "cpuUsagePercent", m.getCpuUsagePct(),
                "ramUsagePercent", m.getRamUsagePct(),
                "diskUsagePercent", m.getDiskUsagePct(),
                "recordedAt", m.getRecordedAt()
        ));
    }

    @GetMapping("/{id}/metrics/history")
    @PreAuthorize("hasAnyRole('DC_ADMIN','TECHNICIAN','CUSTOMER')")
    public ResponseEntity<List<Map<String, Object>>> getMetricsHistory(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        ServerEntity server = serverService.getServer(id);
        List<ServerMetricEntity> metrics = metricRepository.findAllByServer_ServerIdOrderByRecordedAtDesc(
                id, PageRequest.of(page, size)
        );
        return ResponseEntity.ok(metrics.stream().map(m -> Map.of(
                "metricId", m.getMetricId(),
                "serverId", server.getServerId(),
                "hostname", server.getHostname(),
                "cpuUsagePercent", m.getCpuUsagePct(),
                "ramUsagePercent", m.getRamUsagePct(),
                "diskUsagePercent", m.getDiskUsagePct(),
                "recordedAt", m.getRecordedAt()
        )).toList());
    }
}
