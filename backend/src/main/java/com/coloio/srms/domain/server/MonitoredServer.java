package com.coloio.srms.domain.server;

import java.time.LocalDateTime;

public class MonitoredServer extends ServerDecorator {

    private double cpuUsagePercent;
    private double ramUsagePercent;
    private double diskUsagePercent;
    private LocalDateTime lastPolledAt;

    public MonitoredServer(ServerComponent wrapped) {
        super(wrapped);
    }

    @Override
    public ResourceSummary getResourceSummary() {
        return new ResourceSummary(cpuUsagePercent, ramUsagePercent, diskUsagePercent);
    }

    @Override
    public String getDescription() {
        return "Monitored[" + wrapped.getDescription() + "]";
    }

    public void updateMetrics(double cpu, double ram, double disk) {
        this.cpuUsagePercent = cpu;
        this.ramUsagePercent = ram;
        this.diskUsagePercent = disk;
        this.lastPolledAt = LocalDateTime.now();
    }

    public LocalDateTime getLastPolledAt() { return lastPolledAt; }
    public double getCpuUsagePercent() { return cpuUsagePercent; }
    public double getRamUsagePercent() { return ramUsagePercent; }
    public double getDiskUsagePercent() { return diskUsagePercent; }
}
