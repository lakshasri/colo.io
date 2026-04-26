package com.coloio.srms.domain.server;

import com.coloio.srms.domain.enums.ServerStatus;

import java.time.LocalDate;

public class BaseServer implements ServerComponent {

    private Long serverId;
    private Long rackId;
    private Long customerId;
    private String hostname;
    private String ipAddress;
    private int uSize;
    private int uPosition;
    private int cpuCores;
    private int ramGb;
    private double diskTb;
    private ServerStatus status;
    private LocalDate installedDate;

    public BaseServer() {}

    public BaseServer(Long serverId, Long rackId, Long customerId, String hostname,
                      String ipAddress, int uSize, int uPosition, int cpuCores,
                      int ramGb, double diskTb, ServerStatus status, LocalDate installedDate) {
        this.serverId = serverId;
        this.rackId = rackId;
        this.customerId = customerId;
        this.hostname = hostname;
        this.ipAddress = ipAddress;
        this.uSize = uSize;
        this.uPosition = uPosition;
        this.cpuCores = cpuCores;
        this.ramGb = ramGb;
        this.diskTb = diskTb;
        this.status = status;
        this.installedDate = installedDate;
    }

    @Override public Long getServerId() { return serverId; }
    @Override public String getHostname() { return hostname; }
    @Override public String getIpAddress() { return ipAddress; }
    @Override public int getUSize() { return uSize; }
    @Override public int getCpuCores() { return cpuCores; }
    @Override public int getRamGb() { return ramGb; }
    @Override public double getDiskTb() { return diskTb; }
    @Override public ServerStatus getStatus() { return status; }
    @Override public ResourceSummary getResourceSummary() { return ResourceSummary.UNAVAILABLE; }
    @Override public String getDescription() { return "BaseServer[" + hostname + "]"; }

    // Extra getters
    public Long getRackId() { return rackId; }
    public void setRackId(Long rackId) { this.rackId = rackId; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public int getUPosition() { return uPosition; }
    public void setUPosition(int uPosition) { this.uPosition = uPosition; }

    public LocalDate getInstalledDate() { return installedDate; }
    public void setInstalledDate(LocalDate installedDate) { this.installedDate = installedDate; }

    public void setServerId(Long serverId) { this.serverId = serverId; }
    public void setHostname(String hostname) { this.hostname = hostname; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public void setUSize(int uSize) { this.uSize = uSize; }
    public void setCpuCores(int cpuCores) { this.cpuCores = cpuCores; }
    public void setRamGb(int ramGb) { this.ramGb = ramGb; }
    public void setDiskTb(double diskTb) { this.diskTb = diskTb; }
    public void setStatus(ServerStatus status) { this.status = status; }
}
