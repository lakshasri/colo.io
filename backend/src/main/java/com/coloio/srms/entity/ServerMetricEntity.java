package com.coloio.srms.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "server_metrics")
public class ServerMetricEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long metricId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false)
    private ServerEntity server;

    private Double cpuUsagePct;
    private Double ramUsagePct;
    private Double diskUsagePct;

    @Column(nullable = false)
    private LocalDateTime recordedAt;

    @PrePersist
    protected void onCreate() {
        if (recordedAt == null) recordedAt = LocalDateTime.now();
    }

    // Getters & Setters
    public Long getMetricId() { return metricId; }

    public ServerEntity getServer() { return server; }
    public void setServer(ServerEntity server) { this.server = server; }

    public Double getCpuUsagePct() { return cpuUsagePct; }
    public void setCpuUsagePct(Double cpuUsagePct) { this.cpuUsagePct = cpuUsagePct; }

    public Double getRamUsagePct() { return ramUsagePct; }
    public void setRamUsagePct(Double ramUsagePct) { this.ramUsagePct = ramUsagePct; }

    public Double getDiskUsagePct() { return diskUsagePct; }
    public void setDiskUsagePct(Double diskUsagePct) { this.diskUsagePct = diskUsagePct; }

    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
}
