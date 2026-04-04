package com.coloio.srms.service;

import com.coloio.srms.entity.RackEntity;
import com.coloio.srms.entity.ZoneEntity;
import com.coloio.srms.repository.RackRepository;
import com.coloio.srms.repository.ZoneRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class PowerBudgetService {

    private final ZoneRepository zoneRepository;
    private final RackRepository rackRepository;

    public PowerBudgetService(ZoneRepository zoneRepository, RackRepository rackRepository) {
        this.zoneRepository = zoneRepository;
        this.rackRepository = rackRepository;
    }

    public Map<String, Object> getZonePowerSummary(Long zoneId) {
        ZoneEntity zone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new IllegalArgumentException("Zone not found: " + zoneId));

        List<RackEntity> racks = rackRepository.findAllByZone_ZoneId(zoneId);
        double totalUsed = racks.stream()
                .mapToDouble(r -> r.getCurrentPowerKw() != null ? r.getCurrentPowerKw() : 0.0)
                .sum();

        double budget = zone.getPowerBudgetKw() != null ? zone.getPowerBudgetKw() : 0.0;
        double remaining = budget - totalUsed;
        double utilizationPct = budget > 0 ? (totalUsed / budget) * 100.0 : 0.0;

        Map<String, Object> summary = new HashMap<>();
        summary.put("zoneId", zoneId);
        summary.put("zoneName", zone.getName());
        summary.put("budgetKw", budget);
        summary.put("usedKw", totalUsed);
        summary.put("remainingKw", remaining);
        summary.put("utilizationPct", Math.round(utilizationPct * 10.0) / 10.0);
        summary.put("overBudget", totalUsed > budget);
        summary.put("rackCount", racks.size());
        return summary;
    }

    public boolean canAllocatePower(Long zoneId, double additionalKw) {
        ZoneEntity zone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new IllegalArgumentException("Zone not found: " + zoneId));
        if (zone.getPowerBudgetKw() == null) return true;

        List<RackEntity> racks = rackRepository.findAllByZone_ZoneId(zoneId);
        double totalUsed = racks.stream()
                .mapToDouble(r -> r.getCurrentPowerKw() != null ? r.getCurrentPowerKw() : 0.0)
                .sum();
        return (totalUsed + additionalKw) <= zone.getPowerBudgetKw();
    }

    public List<Map<String, Object>> getAllZonesPowerStatus() {
        List<ZoneEntity> zones = zoneRepository.findAll();
        return zones.stream()
                .map(z -> getZonePowerSummary(z.getZoneId()))
                .toList();
    }
}
