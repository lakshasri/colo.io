package com.coloio.srms.entity;

import com.coloio.srms.domain.enums.ServerStatus;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "servers")
public class ServerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long serverId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rack_id")
    private RackEntity rack;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private UserEntity customer;

    @Column(unique = true, nullable = false, length = 100)
    private String hostname;

    @Column(length = 45)
    private String ipAddress;

    @Column(nullable = false)
    private int uSize;

    private int uPosition;

    private int cpuCores;

    private int ramGb;

    private Double diskTb;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ServerStatus status = ServerStatus.UNALLOCATED;

    private LocalDate installedDate;

    // Getters & Setters
    public Long getServerId() { return serverId; }
    public void setServerId(Long serverId) { this.serverId = serverId; }

    public RackEntity getRack() { return rack; }
    public void setRack(RackEntity rack) { this.rack = rack; }

    public UserEntity getCustomer() { return customer; }
    public void setCustomer(UserEntity customer) { this.customer = customer; }

    public String getHostname() { return hostname; }
    public void setHostname(String hostname) { this.hostname = hostname; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public int getUSize() { return uSize; }
    public void setUSize(int uSize) { this.uSize = uSize; }

    public int getUPosition() { return uPosition; }
    public void setUPosition(int uPosition) { this.uPosition = uPosition; }

    public int getCpuCores() { return cpuCores; }
    public void setCpuCores(int cpuCores) { this.cpuCores = cpuCores; }

    public int getRamGb() { return ramGb; }
    public void setRamGb(int ramGb) { this.ramGb = ramGb; }

    public Double getDiskTb() { return diskTb; }
    public void setDiskTb(Double diskTb) { this.diskTb = diskTb; }

    public ServerStatus getStatus() { return status; }
    public void setStatus(ServerStatus status) { this.status = status; }

    public LocalDate getInstalledDate() { return installedDate; }
    public void setInstalledDate(LocalDate installedDate) { this.installedDate = installedDate; }
}
