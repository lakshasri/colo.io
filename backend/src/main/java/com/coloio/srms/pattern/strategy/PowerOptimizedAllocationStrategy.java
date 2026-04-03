package com.coloio.srms.pattern.strategy;

import com.coloio.srms.domain.rack.Rack;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Picks the rack with the highest current power load that still fits the server.
 * This consolidates load and leaves other racks with more headroom (bin-packing by power).
 */
@Component
public class PowerOptimizedAllocationStrategy implements AllocationStrategy {

    @Override
    public Optional<AllocationResult> findSlot(List<Rack> racks, int uSize, double powerKw) {
        return racks.stream()
                .filter(r -> r.canFit(uSize, powerKw))
                .max(Comparator.comparingDouble(Rack::getCurrentPowerKw))
                .map(r -> new AllocationResult(
                        r.getRackId(), r.getName(),
                        r.getUsedUSpace() + 1,
                        getStrategyName()));
    }

    @Override
    public String getStrategyName() { return "POWER_OPTIMIZED"; }
}
