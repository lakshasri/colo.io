package com.coloio.srms.service;

import com.coloio.srms.domain.enums.AlertSeverity;
import com.coloio.srms.domain.enums.AlertType;
import com.coloio.srms.domain.enums.ServerStatus;
import com.coloio.srms.domain.alert.AlertManager;
import com.coloio.srms.domain.server.AlertableServer;
import com.coloio.srms.domain.server.MonitoredServer;
import com.coloio.srms.domain.server.ServerComponent;
import com.coloio.srms.domain.server.ServerDecorator;
import com.coloio.srms.entity.ServerEntity;
import com.coloio.srms.entity.ServerMetricEntity;
import com.coloio.srms.pattern.factory.ServerDecoratorFactory;
import com.coloio.srms.repository.ServerMetricRepository;
import com.coloio.srms.repository.ServerRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MonitoringService {

    private static volatile MonitoringService instance;

    private final ServerRepository serverRepository;
    private final ServerMetricRepository metricRepository;
    private final ServerService serverService;
    private final SimpMessagingTemplate messagingTemplate;

    // Live decorated server cache
    private final Map<Long, ServerComponent> monitoredServers = new ConcurrentHashMap<>();
    private final Random rng = new Random();

    public MonitoringService(ServerRepository serverRepository,
                             ServerMetricRepository metricRepository,
                             ServerService serverService,
                             SimpMessagingTemplate messagingTemplate) {
        this.serverRepository = serverRepository;
        this.metricRepository = metricRepository;
        this.serverService = serverService;
        this.messagingTemplate = messagingTemplate;
        instance = this;
    }

    public static MonitoringService getInstance() { return instance; }

    /** Poll every 30 seconds — simulate metrics and persist. */
    @Scheduled(fixedDelay = 30_000)
    @Transactional
    public void pollAllServers() {
        serverRepository.findAllByStatus(ServerStatus.OPERATIONAL).forEach(entity -> {
            ServerComponent component = monitoredServers.computeIfAbsent(
                    entity.getServerId(),
                    id -> ServerDecoratorFactory.wrap(
                            serverService.toDomain(entity),
                            ServerDecoratorFactory.AllocationState.PROVISIONED
                    )
            );

            MonitoredServer monitored = extractMonitored(component);
            if (monitored == null) return;

            // Simulate realistic metrics (replace with real agent data later)
            double cpu  = 20 + rng.nextDouble() * 60;
            double ram  = 30 + rng.nextDouble() * 50;
            double disk = 40 + rng.nextDouble() * 40;
            monitored.updateMetrics(cpu, ram, disk);

            persistMetric(entity, cpu, ram, disk);
            checkAlertThresholds(component, entity);
            pushMetricsOverWebSocket(entity.getServerId(), cpu, ram, disk);
        });
    }

    private void persistMetric(ServerEntity entity, double cpu, double ram, double disk) {
        ServerMetricEntity metric = new ServerMetricEntity();
        metric.setServer(entity);
        metric.setCpuUsagePct(cpu);
        metric.setRamUsagePct(ram);
        metric.setDiskUsagePct(disk);
        metricRepository.save(metric);
    }

    private void checkAlertThresholds(ServerComponent component, ServerEntity entity) {
        ServerComponent current = component;
        while (current instanceof ServerDecorator d) {
            if (current instanceof AlertableServer alertable) {
                Map<AlertableServer.MetricType, Double> breaches = alertable.checkBreaches();
                breaches.forEach((metric, value) -> {
                    AlertManager.getInstance().raiseAlert(
                            AlertType.HEALTH,
                            value > 95 ? AlertSeverity.CRITICAL : AlertSeverity.HIGH,
                            String.format("Server %s: %s at %.1f%%",
                                    entity.getHostname(), metric.name(), value),
                            String.valueOf(entity.getServerId()),
                            "SERVER"
                    );
                });
                break;
            }
            current = d.getWrapped();
        }
    }

    private void pushMetricsOverWebSocket(Long serverId, double cpu, double ram, double disk) {
        messagingTemplate.convertAndSend(
                "/topic/server/" + serverId + "/metrics",
                Map.of(
                        "serverId",         serverId,
                        "cpuUsagePercent",  cpu,
                        "ramUsagePercent",  ram,
                        "diskUsagePercent", disk
                )
        );
    }

    private MonitoredServer extractMonitored(ServerComponent component) {
        ServerComponent current = component;
        while (current instanceof ServerDecorator d) {
            if (current instanceof MonitoredServer m) return m;
            current = d.getWrapped();
        }
        return null;
    }
}
