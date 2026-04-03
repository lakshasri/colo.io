package com.coloio.srms.domain.server;

public class ResourceSummary {

    private final double cpuUsagePercent;
    private final double ramUsagePercent;
    private final double diskUsagePercent;

    public static final ResourceSummary UNAVAILABLE = new ResourceSummary(-1, -1, -1);

    public ResourceSummary(double cpuUsagePercent, double ramUsagePercent, double diskUsagePercent) {
        this.cpuUsagePercent = cpuUsagePercent;
        this.ramUsagePercent = ramUsagePercent;
        this.diskUsagePercent = diskUsagePercent;
    }

    public boolean isAvailable() { return cpuUsagePercent >= 0; }

    public double getCpuUsagePercent() { return cpuUsagePercent; }
    public double getRamUsagePercent() { return ramUsagePercent; }
    public double getDiskUsagePercent() { return diskUsagePercent; }
}
