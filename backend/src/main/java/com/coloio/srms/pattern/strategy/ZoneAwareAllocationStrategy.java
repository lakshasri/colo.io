package com.coloio.srms.pattern.strategy;

import com.coloio.srms.domain.rack.Rack;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Distributes load evenly across zones by picking a rack in the zone
 * that currently has the lowest total power consumption.
 */
@Component
public class ZoneAwareAllocationStrategy implements AllocationStrategy {

    @Override
    public Optional<AllocationResult> findSlot(List<Rack> racks, int uSize, double powerKw) {
        // Sum current power per zone
        Map<Long, Double> zonePowerTotals = racks.stream()
                .filter(r -> r.getZoneId() != null)
                .collect(Collectors.groupingBy(
                        Rack::getZoneId,
                        Collectors.summingDouble(Rack::getCurrentPowerKw)));

        // Find rack in the lightest-loaded zone that can fit
        return racks.stream()
                .filter(r -> r.canFit(uSize, powerKw) && r.getZoneId() != null)
                .min(Comparator.comparingDouble(r ->
                        zonePowerTotals.getOrDefault(r.getZoneId(), 0.0)))
                .map(r -> new AllocationResult(
                        r.getRackId(), r.getName(),
                        r.getUsedUSpace() + 1,
                        getStrategyName()));
    }

    @Override
    public String getStrategyName() { return "ZONE_AWARE"; }
}
