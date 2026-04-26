package com.coloio.srms.service;

import com.coloio.srms.domain.enums.ServerStatus;
import com.coloio.srms.domain.rack.Rack;
import com.coloio.srms.domain.server.BaseServer;
import com.coloio.srms.domain.server.ServerComponent;
import com.coloio.srms.entity.RackEntity;
import com.coloio.srms.entity.ServerEntity;
import com.coloio.srms.entity.UserEntity;
import com.coloio.srms.pattern.factory.ServerDecoratorFactory;
import com.coloio.srms.pattern.strategy.*;
import com.coloio.srms.repository.ServerRepository;
import com.coloio.srms.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ServerService {

    private final ServerRepository serverRepository;
    private final UserRepository userRepository;
    private final RackService rackService;

    // Strategy implementations injected by Spring
    private final FirstFitAllocationStrategy firstFit;
    private final BestFitAllocationStrategy bestFit;
    private final PowerOptimizedAllocationStrategy powerOptimized;
    private final ZoneAwareAllocationStrategy zoneAware;

    public ServerService(ServerRepository serverRepository,
                         UserRepository userRepository,
                         RackService rackService,
                         FirstFitAllocationStrategy firstFit,
                         BestFitAllocationStrategy bestFit,
                         PowerOptimizedAllocationStrategy powerOptimized,
                         ZoneAwareAllocationStrategy zoneAware) {
        this.serverRepository = serverRepository;
        this.userRepository = userRepository;
        this.rackService = rackService;
        this.firstFit = firstFit;
        this.bestFit = bestFit;
        this.powerOptimized = powerOptimized;
        this.zoneAware = zoneAware;
    }

    public ServerComponent registerServer(ServerEntity request) {
        if (serverRepository.existsByHostname(request.getHostname())) {
            throw new IllegalArgumentException("Hostname already exists: " + request.getHostname());
        }
        request.setStatus(ServerStatus.UNALLOCATED);
        request.setInstalledDate(LocalDate.now());
        ServerEntity saved = serverRepository.save(request);
        return ServerDecoratorFactory.wrap(toDomain(saved), ServerDecoratorFactory.AllocationState.BARE);
    }

    public AllocationResult allocateToRack(Long serverId, String strategyName) {
        ServerEntity server = findEntity(serverId);
        List<RackEntity> rackEntities = rackService.findActiveRackEntities();
        List<Rack> racks = rackEntities.stream().map(rackService::toDomain).toList();

        AllocationStrategy strategy = resolveStrategy(strategyName);
        AllocationContext ctx = new AllocationContext(strategy);

        // Estimate server power from CPU (simple heuristic: 10W per core)
        double estimatedPowerKw = server.getCpuCores() * 0.01;

        AllocationResult result = ctx.allocate(racks, server.getUSize(), estimatedPowerKw)
                .orElseThrow(() -> new IllegalStateException("No rack available for allocation"));

        RackEntity rack = rackService.findEntity(result.getRackId());
        server.setRack(rack);
        server.setUPosition(result.getUPosition());
        server.setStatus(ServerStatus.OPERATIONAL);
        serverRepository.save(server);

        rackService.updateUSpaceAndPower(result.getRackId(), server.getUSize(), estimatedPowerKw);

        return result;
    }

    public ServerComponent provisionToCustomer(Long serverId, Long customerId) {
        ServerEntity server = findEntity(serverId);
        UserEntity customer = userRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));

        server.setCustomer(customer);
        serverRepository.save(server);

        return ServerDecoratorFactory.wrap(toDomain(server), ServerDecoratorFactory.AllocationState.PROVISIONED);
    }

    public void updateStatus(Long serverId, ServerStatus newStatus) {
        ServerEntity server = findEntity(serverId);
        server.setStatus(newStatus);
        serverRepository.save(server);
    }

    @Transactional(readOnly = true)
    public ServerComponent getServer(Long id) {
        ServerEntity entity = findEntity(id);
        ServerDecoratorFactory.AllocationState state = resolveAllocationState(entity);
        return ServerDecoratorFactory.wrap(toDomain(entity), state);
    }

    @Transactional(readOnly = true)
    public List<ServerComponent> getAllServers() {
        return serverRepository.findAll().stream()
                .map(e -> ServerDecoratorFactory.wrap(toDomain(e), resolveAllocationState(e)))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ServerComponent> getServersByCustomer(Long customerId) {
        return serverRepository.findAllByCustomer_UserId(customerId).stream()
                .map(e -> ServerDecoratorFactory.wrap(toDomain(e), ServerDecoratorFactory.AllocationState.PROVISIONED))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ServerComponent> getServersByRack(Long rackId) {
        return serverRepository.findAllByRack_RackId(rackId).stream()
                .map(e -> ServerDecoratorFactory.wrap(toDomain(e), resolveAllocationState(e)))
                .toList();
    }

    // --- Helpers ---

    private AllocationStrategy resolveStrategy(String name) {
        return switch (name.toUpperCase()) {
            case "BEST_FIT"        -> bestFit;
            case "POWER_OPTIMIZED" -> powerOptimized;
            case "ZONE_AWARE"      -> zoneAware;
            default                -> firstFit;
        };
    }

    private ServerDecoratorFactory.AllocationState resolveAllocationState(ServerEntity e) {
        if (e.getCustomer() != null) return ServerDecoratorFactory.AllocationState.PROVISIONED;
        if (e.getRack()     != null) return ServerDecoratorFactory.AllocationState.ALLOCATED;
        return ServerDecoratorFactory.AllocationState.BARE;
    }

    public ServerEntity findEntity(Long id) {
        return serverRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Server not found: " + id));
    }

    public BaseServer toDomain(ServerEntity e) {
        return new BaseServer(
                e.getServerId(),
                e.getRack()     != null ? e.getRack().getRackId()         : null,
                e.getCustomer() != null ? e.getCustomer().getUserId()     : null,
                e.getHostname(), e.getIpAddress(),
                e.getUSize(), e.getUPosition(),
                e.getCpuCores(), e.getRamGb(),
                e.getDiskTb() != null ? e.getDiskTb() : 0,
                e.getStatus(), e.getInstalledDate()
        );
    }

    public Map<String, Object> toResponseMap(ServerComponent s) {
        return Map.of(
                "serverId",    s.getServerId(),
                "hostname",    s.getHostname(),
                "ipAddress",   s.getIpAddress() != null ? s.getIpAddress() : "",
                "status",      s.getStatus(),
                "cpuCores",    s.getCpuCores(),
                "ramGb",       s.getRamGb(),
                "diskTb",      s.getDiskTb(),
                "uSize",       s.getUSize(),
                "description", s.getDescription(),
                "metrics",     s.getResourceSummary()
        );
    }
}
