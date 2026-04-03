package com.coloio.srms.pattern.strategy;

import com.coloio.srms.domain.rack.Rack;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class FirstFitAllocationStrategy implements AllocationStrategy {

    @Override
    public Optional<AllocationResult> findSlot(List<Rack> racks, int uSize, double powerKw) {
        return racks.stream()
                .filter(r -> r.canFit(uSize, powerKw))
                .findFirst()
                .map(r -> new AllocationResult(
                        r.getRackId(), r.getName(),
                        r.getUsedUSpace() + 1,
                        getStrategyName()));
    }

    @Override
    public String getStrategyName() { return "FIRST_FIT"; }
}
