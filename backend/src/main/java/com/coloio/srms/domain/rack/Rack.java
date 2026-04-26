package com.coloio.srms.domain.rack;

import com.coloio.srms.domain.enums.RackEventType;
import com.coloio.srms.domain.enums.RackStatus;

import java.util.ArrayList;
import java.util.List;

public class Rack implements RackSubject {

    private Long rackId;
    private String name;
    private Long zoneId;
    private String location;
    private int totalUSpace;
    private int usedUSpace;
    private double maxPowerKw;
    private double currentPowerKw;
    private RackStatus status;

    private final List<RackObserver> observers = new ArrayList<>();

    private static final double POWER_WARN_THRESHOLD  = 0.85;
    private static final double POWER_CRIT_THRESHOLD  = 0.95;
    private static final int    USPACE_LOW_THRESHOLD  = 5;

    public Rack() {}

    public Rack(Long rackId, String name, Long zoneId, String location,
                int totalUSpace, int usedUSpace,
                double maxPowerKw, double currentPowerKw, RackStatus status) {
        this.rackId = rackId;
        this.name = name;
        this.zoneId = zoneId;
        this.location = location;
        this.totalUSpace = totalUSpace;
        this.usedUSpace = usedUSpace;
        this.maxPowerKw = maxPowerKw;
        this.currentPowerKw = currentPowerKw;
        this.status = status;
    }

    // --- Observer Pattern ---

    @Override
    public void addObserver(RackObserver observer) {
        if (!observers.contains(observer)) observers.add(observer);
    }

    @Override
    public void removeObserver(RackObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(RackEvent event) {
        for (RackObserver observer : observers) {
            observer.onRackEvent(event);
        }
    }

    // --- Domain Operations ---

    public void addServer(int uSize, double serverPowerKw) {
        this.usedUSpace += uSize;
        this.currentPowerKw += serverPowerKw;

        notifyObservers(new RackEvent(RackEventType.SERVER_ADDED, rackId, name,
                String.format("Server added: %dU, %.2fkW", uSize, serverPowerKw)));

        checkThresholds();
    }

    public void removeServer(int uSize, double serverPowerKw) {
        this.usedUSpace = Math.max(0, this.usedUSpace - uSize);
        this.currentPowerKw = Math.max(0, this.currentPowerKw - serverPowerKw);

        notifyObservers(new RackEvent(RackEventType.SERVER_REMOVED, rackId, name,
                String.format("Server removed: %dU, %.2fkW freed", uSize, serverPowerKw)));
    }

    public void updatePower(double newPowerKw) {
        this.currentPowerKw = newPowerKw;
        notifyObservers(new RackEvent(RackEventType.POWER_UPDATED, rackId, name,
                String.format("Power updated to %.2fkW", newPowerKw)));
        checkThresholds();
    }

    public void changeStatus(RackStatus newStatus) {
        RackStatus prev = this.status;
        this.status = newStatus;
        notifyObservers(new RackEvent(RackEventType.STATUS_CHANGED, rackId, name,
                String.format("Status changed: %s -> %s", prev, newStatus)));
    }

    private void checkThresholds() {
        double powerPct = currentPowerKw / maxPowerKw;
        int remaining = totalUSpace - usedUSpace;

        if (powerPct >= POWER_CRIT_THRESHOLD) {
            notifyObservers(new RackEvent(RackEventType.POWER_CRITICAL, rackId, name,
                    String.format("CRITICAL: Power at %.1f%%", powerPct * 100)));
        } else if (powerPct >= POWER_WARN_THRESHOLD) {
            notifyObservers(new RackEvent(RackEventType.POWER_THRESHOLD_EXCEEDED, rackId, name,
                    String.format("WARNING: Power at %.1f%%", powerPct * 100)));
        }

        if (remaining == 0) {
            notifyObservers(new RackEvent(RackEventType.USPACE_FULL, rackId, name, "Rack is full"));
        } else if (remaining <= USPACE_LOW_THRESHOLD) {
            notifyObservers(new RackEvent(RackEventType.USPACE_LOW, rackId, name,
                    String.format("Only %dU remaining", remaining)));
        }
    }

    // --- Utility ---

    public double getPowerUtilizationPercent() {
        return maxPowerKw > 0 ? (currentPowerKw / maxPowerKw) * 100 : 0;
    }

    public double getUSpaceUtilizationPercent() {
        return totalUSpace > 0 ? ((double) usedUSpace / totalUSpace) * 100 : 0;
    }

    public int getAvailableUSpace() { return totalUSpace - usedUSpace; }

    public double getAvailablePowerKw() { return maxPowerKw - currentPowerKw; }

    public boolean canFit(int uSize, double powerKw) {
        return getAvailableUSpace() >= uSize && getAvailablePowerKw() >= powerKw;
    }

    // Getters & Setters
    public Long getRackId() { return rackId; }
    public void setRackId(Long rackId) { this.rackId = rackId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getZoneId() { return zoneId; }
    public void setZoneId(Long zoneId) { this.zoneId = zoneId; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public int getTotalUSpace() { return totalUSpace; }
    public void setTotalUSpace(int totalUSpace) { this.totalUSpace = totalUSpace; }

    public int getUsedUSpace() { return usedUSpace; }
    public void setUsedUSpace(int usedUSpace) { this.usedUSpace = usedUSpace; }

    public double getMaxPowerKw() { return maxPowerKw; }
    public void setMaxPowerKw(double maxPowerKw) { this.maxPowerKw = maxPowerKw; }

    public double getCurrentPowerKw() { return currentPowerKw; }
    public void setCurrentPowerKw(double currentPowerKw) { this.currentPowerKw = currentPowerKw; }

    public RackStatus getStatus() { return status; }
    public void setStatus(RackStatus status) { this.status = status; }
}
