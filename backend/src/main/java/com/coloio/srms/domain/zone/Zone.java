package com.coloio.srms.domain.zone;

public class Zone {

    private Long zoneId;
    private String name;
    private Integer floor;
    private Double powerBudgetKw;
    private Double coolingCapacity;

    public Zone() {}

    public Zone(Long zoneId, String name, Integer floor,
                Double powerBudgetKw, Double coolingCapacity) {
        this.zoneId = zoneId;
        this.name = name;
        this.floor = floor;
        this.powerBudgetKw = powerBudgetKw;
        this.coolingCapacity = coolingCapacity;
    }

    public double getRemainingPowerKw(double currentUsageKw) {
        return powerBudgetKw - currentUsageKw;
    }

    public boolean canAccommodatePower(double additionalKw, double currentUsageKw) {
        return (currentUsageKw + additionalKw) <= powerBudgetKw;
    }

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
