package com.coloio.srms.service;

import com.coloio.srms.entity.*;
import com.coloio.srms.repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private final RackRepository rackRepository;
    private final ServerRepository serverRepository;
    private final ServerMetricRepository metricRepository;
    private final MaintenanceTicketRepository ticketRepository;
    private final ZoneRepository zoneRepository;

    public ReportService(RackRepository rackRepository,
                          ServerRepository serverRepository,
                          ServerMetricRepository metricRepository,
                          MaintenanceTicketRepository ticketRepository,
                          ZoneRepository zoneRepository) {
        this.rackRepository = rackRepository;
        this.serverRepository = serverRepository;
        this.metricRepository = metricRepository;
        this.ticketRepository = ticketRepository;
        this.zoneRepository = zoneRepository;
    }

    // 4.13 — Capacity report: zone/rack utilization breakdown
    public Map<String, Object> capacityReport() {
        List<ZoneEntity> zones = zoneRepository.findAll();
        List<Map<String, Object>> zoneBreakdown = new ArrayList<>();

        for (ZoneEntity zone : zones) {
            List<RackEntity> racks = rackRepository.findAllByZone_ZoneId(zone.getZoneId());
            int totalU = racks.stream().mapToInt(RackEntity::getTotalUSpace).sum();
            int usedU = racks.stream().mapToInt(RackEntity::getUsedUSpace).sum();
            double totalPower = racks.stream()
                    .mapToDouble(r -> r.getMaxPowerKw() != null ? r.getMaxPowerKw() : 0).sum();
            double usedPower = racks.stream()
                    .mapToDouble(r -> r.getCurrentPowerKw() != null ? r.getCurrentPowerKw() : 0).sum();

            Map<String, Object> zoneData = new LinkedHashMap<>();
            zoneData.put("zoneId", zone.getZoneId());
            zoneData.put("zoneName", zone.getName());
            zoneData.put("rackCount", racks.size());
            zoneData.put("totalUSpace", totalU);
            zoneData.put("usedUSpace", usedU);
            zoneData.put("uUtilizationPct", totalU > 0 ? Math.round(usedU * 100.0 / totalU) : 0);
            zoneData.put("totalPowerKw", totalPower);
            zoneData.put("usedPowerKw", usedPower);
            zoneData.put("powerUtilizationPct", totalPower > 0 ? Math.round(usedPower * 100.0 / totalPower) : 0);
            zoneBreakdown.add(zoneData);
        }

        long totalRacks = rackRepository.count();
        long totalServers = serverRepository.count();
        return Map.of("zones", zoneBreakdown, "totalRacks", totalRacks, "totalServers", totalServers);
    }

    // 4.14 — Server utilization summary: avg CPU/RAM/Disk per server
    public List<Map<String, Object>> serverUtilizationReport() {
        List<ServerEntity> servers = serverRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (ServerEntity server : servers) {
            List<ServerMetricEntity> metrics = metricRepository
                    .findAllByServer_ServerIdOrderByRecordedAtDesc(server.getServerId(), PageRequest.of(0, 10));
            if (metrics.isEmpty()) continue;

            double avgCpu = metrics.stream().mapToDouble(m -> m.getCpuUsagePct() != null ? m.getCpuUsagePct() : 0).average().orElse(0);
            double avgRam = metrics.stream().mapToDouble(m -> m.getRamUsagePct() != null ? m.getRamUsagePct() : 0).average().orElse(0);
            double avgDisk = metrics.stream().mapToDouble(m -> m.getDiskUsagePct() != null ? m.getDiskUsagePct() : 0).average().orElse(0);

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("serverId", server.getServerId());
            row.put("hostname", server.getHostname());
            row.put("status", server.getStatus());
            row.put("avgCpuPct", Math.round(avgCpu * 10.0) / 10.0);
            row.put("avgRamPct", Math.round(avgRam * 10.0) / 10.0);
            row.put("avgDiskPct", Math.round(avgDisk * 10.0) / 10.0);
            result.add(row);
        }
        return result;
    }

    // 4.15 — Maintenance history report: completion rate, avg duration
    public Map<String, Object> maintenanceHistoryReport() {
        List<MaintenanceTicketEntity> all = ticketRepository.findAllByOrderByCreatedAtDesc();

        long total = all.size();
        long resolved = all.stream().filter(t -> "RESOLVED".equals(t.getStatus())).count();
        long cancelled = all.stream().filter(t -> "CANCELLED".equals(t.getStatus())).count();
        long inProgress = all.stream().filter(t -> "IN_PROGRESS".equals(t.getStatus())).count();

        OptionalDouble avgDurationHours = all.stream()
                .filter(t -> t.getStartedAt() != null && t.getResolvedAt() != null)
                .mapToLong(t -> Duration.between(t.getStartedAt(), t.getResolvedAt()).toHours())
                .average();

        Map<String, Long> byPriority = all.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getPriority() != null ? t.getPriority() : "UNKNOWN",
                        Collectors.counting()));

        return Map.of(
                "total", total,
                "resolved", resolved,
                "cancelled", cancelled,
                "inProgress", inProgress,
                "completionRatePct", total > 0 ? Math.round(resolved * 100.0 / total) : 0,
                "avgResolutionHours", avgDurationHours.isPresent() ? Math.round(avgDurationHours.getAsDouble() * 10.0) / 10.0 : 0,
                "byPriority", byPriority
        );
    }
}
