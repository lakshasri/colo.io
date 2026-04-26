package com.coloio.srms.pattern.strategy;

import com.coloio.srms.domain.rack.Rack;

import java.util.List;
import java.util.Optional;

public interface AllocationStrategy {
    Optional<AllocationResult> findSlot(List<Rack> racks, int uSize, double powerKw);
    String getStrategyName();
}
