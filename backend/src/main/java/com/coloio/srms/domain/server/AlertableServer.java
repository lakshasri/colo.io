package com.coloio.srms.domain.server;

import java.util.HashMap;
import java.util.Map;

public class AlertableServer extends ServerDecorator {

    public enum MetricType { CPU, RAM, DISK }

    private final Map<MetricType, Double> thresholds = new HashMap<>();

    public AlertableServer(ServerComponent wrapped) {
        super(wrapped);
        // Defaults
        thresholds.put(MetricType.CPU,  85.0);
        thresholds.put(MetricType.RAM,  90.0);
        thresholds.put(MetricType.DISK, 85.0);
    }

    public void setThreshold(MetricType metric, double percent) {
        thresholds.put(metric, percent);
    }

    public Map<MetricType, Double> checkBreaches() {
        ResourceSummary summary = getResourceSummary();
        if (!summary.isAvailable()) return Map.of();

        Map<MetricType, Double> breaches = new HashMap<>();
        if (summary.getCpuUsagePercent()  > thresholds.get(MetricType.CPU))
            breaches.put(MetricType.CPU,  summary.getCpuUsagePercent());
        if (summary.getRamUsagePercent()  > thresholds.get(MetricType.RAM))
            breaches.put(MetricType.RAM,  summary.getRamUsagePercent());
        if (summary.getDiskUsagePercent() > thresholds.get(MetricType.DISK))
            breaches.put(MetricType.DISK, summary.getDiskUsagePercent());
        return breaches;
    }

    @Override
    public String getDescription() {
        return "Alertable[" + wrapped.getDescription() + "]";
    }

    public Map<MetricType, Double> getThresholds() { return Map.copyOf(thresholds); }
}
