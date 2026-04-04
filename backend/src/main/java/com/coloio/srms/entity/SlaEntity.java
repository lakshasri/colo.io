package com.coloio.srms.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "sla_agreements")
public class SlaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long slaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private UserEntity customer;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 300)
    private String description;

    // Uptime guarantee in percent (e.g. 99.9)
    @Column(nullable = false)
    private double uptimeGuaranteePct;

    // Max response time for critical alerts in minutes
    @Column(nullable = false)
    private int responseTimeMinutes;

    // Max resolution time for maintenance in hours
    @Column(nullable = false)
    private int resolutionTimeHours;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    @Column(nullable = false, length = 20)
    private String status = "ACTIVE"; // ACTIVE, EXPIRED, SUSPENDED

    public Long getSlaId() { return slaId; }
    public void setSlaId(Long slaId) { this.slaId = slaId; }

    public UserEntity getCustomer() { return customer; }
    public void setCustomer(UserEntity customer) { this.customer = customer; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getUptimeGuaranteePct() { return uptimeGuaranteePct; }
    public void setUptimeGuaranteePct(double uptimeGuaranteePct) { this.uptimeGuaranteePct = uptimeGuaranteePct; }

    public int getResponseTimeMinutes() { return responseTimeMinutes; }
    public void setResponseTimeMinutes(int responseTimeMinutes) { this.responseTimeMinutes = responseTimeMinutes; }

    public int getResolutionTimeHours() { return resolutionTimeHours; }
    public void setResolutionTimeHours(int resolutionTimeHours) { this.resolutionTimeHours = resolutionTimeHours; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
