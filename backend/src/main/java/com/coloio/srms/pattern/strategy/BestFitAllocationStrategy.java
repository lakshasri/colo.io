package com.coloio.srms.pattern.strategy;

import com.coloio.srms.domain.rack.Rack;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Picks the rack that has the least wasted U-space after placing the server
 * (i.e., the tightest fit), minimising fragmentation.
 */
@Component
public class BestFitAllocationStrategy implements AllocationStrategy {

    @Override
    public Optional<AllocationResult> findSlot(List<Rack> racks, int uSize, double powerKw) {
        return racks.stream()
                .filter(r -> r.canFit(uSize, powerKw))
                .min(Comparator.comparingInt(r -> r.getAvailableUSpace() - uSize))
                .map(r -> new AllocationResult(
                        r.getRackId(), r.getName(),
                        r.getUsedUSpace() + 1,
                        getStrategyName()));
    }

    @Override
    public String getStrategyName() { return "BEST_FIT"; }
}
