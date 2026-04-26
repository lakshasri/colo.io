package com.coloio.srms.controller;

import com.coloio.srms.domain.enums.ServerStatus;
import com.coloio.srms.entity.ServerEntity;
import com.coloio.srms.repository.ServerRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Bulk Operations", description = "Bulk server management operations")
@RestController
@RequestMapping("/api/bulk")
public class BulkOperationController {

    private final ServerRepository serverRepository;

    public BulkOperationController(ServerRepository serverRepository) {
        this.serverRepository = serverRepository;
    }

    @Operation(summary = "Bulk update server status")
    @PatchMapping("/servers/status")
    @PreAuthorize("hasAnyRole('DC_ADMIN','MANAGER')")
    public Map<String, Object> bulkUpdateStatus(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Integer> rawIds = (List<Integer>) body.get("serverIds");
        String statusStr = body.get("status").toString();
        ServerStatus status = ServerStatus.valueOf(statusStr);

        List<Long> serverIds = rawIds.stream().map(Long::valueOf).toList();
        List<ServerEntity> servers = serverRepository.findAllById(serverIds);
        servers.forEach(s -> s.setStatus(status));
        serverRepository.saveAll(servers);

        return Map.of(
            "updated", servers.size(),
            "status", statusStr,
            "serverIds", serverIds
        );
    }

    @Operation(summary = "Bulk decommission servers")
    @PostMapping("/servers/decommission")
    @PreAuthorize("hasRole('DC_ADMIN')")
    public Map<String, Object> bulkDecommission(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Integer> rawIds = (List<Integer>) body.get("serverIds");
        List<Long> serverIds = rawIds.stream().map(Long::valueOf).toList();

        List<ServerEntity> servers = serverRepository.findAllById(serverIds);
        servers.forEach(s -> {
            s.setStatus(ServerStatus.DECOMMISSIONED);
            s.setRack(null);
            s.setUPosition(0);
        });
        serverRepository.saveAll(servers);

        return Map.of("decommissioned", servers.size(), "serverIds", serverIds);
    }
}
