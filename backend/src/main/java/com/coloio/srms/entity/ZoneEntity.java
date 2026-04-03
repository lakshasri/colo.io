package com.coloio.srms.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "zones")
public class ZoneEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long zoneId;

    @Column(nullable = false, length = 50)
    private String name;

    private Integer floor;

    private Double powerBudgetKw;

    private Double coolingCapacity;

    // Getters & Setters
    public Long getZoneId() { return zoneId; }
    public void setZoneId(Long zoneId) { this.zoneId = zoneId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getFloor() { return floor; }
    public void setFloor(Integer floor) { this.floor = floor; }

    public Double getPowerBudgetKw() { return powerBudgetKw; }
    public void setPowerBudgetKw(Double powerBudgetKw) { this.powerBudgetKw = powerBudgetKw; }

    public Double getCoolingCapacity() { return coolingCapacity; }
    public void setCoolingCapacity(Double coolingCapacity) { this.coolingCapacity = coolingCapacity; }
}
