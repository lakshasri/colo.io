package com.coloio.srms.pattern.strategy;

import com.coloio.srms.domain.rack.Rack;

import java.util.List;
import java.util.Optional;

public class AllocationContext {

    private AllocationStrategy strategy;

    public AllocationContext(AllocationStrategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(AllocationStrategy strategy) {
        this.strategy = strategy;
    }

    public AllocationStrategy getStrategy() { return strategy; }

    public Optional<AllocationResult> allocate(List<Rack> racks, int uSize, double powerKw) {
        return strategy.findSlot(racks, uSize, powerKw);
    }
}
