package com.coloio.srms.entity;

import com.coloio.srms.domain.enums.RackStatus;
import jakarta.persistence.*;

@Entity
@Table(name = "racks")
public class RackEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long rackId;

    @Column(nullable = false, length = 20)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id")
    private ZoneEntity zone;

    @Column(length = 100)
    private String location;

    @Column(nullable = false)
    private int totalUSpace = 42;

    @Column(nullable = false)
    private int usedUSpace = 0;

    private Double maxPowerKw;

    private Double currentPowerKw = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RackStatus status = RackStatus.ACTIVE;

    // Getters & Setters
    public Long getRackId() { return rackId; }
    public void setRackId(Long rackId) { this.rackId = rackId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public ZoneEntity getZone() { return zone; }
    public void setZone(ZoneEntity zone) { this.zone = zone; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public int getTotalUSpace() { return totalUSpace; }
    public void setTotalUSpace(int totalUSpace) { this.totalUSpace = totalUSpace; }

    public int getUsedUSpace() { return usedUSpace; }
    public void setUsedUSpace(int usedUSpace) { this.usedUSpace = usedUSpace; }

    public Double getMaxPowerKw() { return maxPowerKw; }
    public void setMaxPowerKw(Double maxPowerKw) { this.maxPowerKw = maxPowerKw; }

    public Double getCurrentPowerKw() { return currentPowerKw; }
    public void setCurrentPowerKw(Double currentPowerKw) { this.currentPowerKw = currentPowerKw; }

    public RackStatus getStatus() { return status; }
    public void setStatus(RackStatus status) { this.status = status; }
}
