package com.coloio.srms.pattern.strategy;

import com.coloio.srms.domain.enums.RackStatus;
import com.coloio.srms.domain.rack.Rack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AllocationStrategyTest {

    private Rack rackA;
    private Rack rackB;
    private Rack rackFull;

    @BeforeEach
    void setUp() {
        // rackA: 42U total, 10U used, 20kW max, 5kW used
        rackA = new Rack(1L, "Rack-A", 1L, "Row-1", 42, 10, 20.0, 5.0, RackStatus.ACTIVE);
        // rackB: 42U total, 35U used, 20kW max, 15kW used (less free space)
        rackB = new Rack(2L, "Rack-B", 1L, "Row-1", 42, 35, 20.0, 15.0, RackStatus.ACTIVE);
        // rackFull: completely full
        rackFull = new Rack(3L, "Rack-Full", 1L, "Row-2", 42, 42, 20.0, 20.0, RackStatus.ACTIVE);
    }

    @Test
    void firstFit_picksFirstAvailableRack() {
        AllocationStrategy strategy = new FirstFitAllocationStrategy();
        Optional<AllocationResult> result = strategy.findSlot(List.of(rackA, rackB), 4, 2.0);
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getRackId());
    }

    @Test
    void firstFit_skipsFullRack() {
        AllocationStrategy strategy = new FirstFitAllocationStrategy();
        Optional<AllocationResult> result = strategy.findSlot(List.of(rackFull, rackA), 4, 2.0);
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getRackId());
    }

    @Test
    void firstFit_returnsEmptyWhenNoFit() {
        AllocationStrategy strategy = new FirstFitAllocationStrategy();
        Optional<AllocationResult> result = strategy.findSlot(List.of(rackFull), 4, 2.0);
        assertTrue(result.isEmpty());
    }

    @Test
    void bestFit_picksTightestFit() {
        AllocationStrategy strategy = new BestFitAllocationStrategy();
        // rackB has less free U-space (7U vs 32U), so it's a tighter fit
        Optional<AllocationResult> result = strategy.findSlot(List.of(rackA, rackB), 4, 1.0);
        assertTrue(result.isPresent());
        assertEquals(2L, result.get().getRackId());
    }

    @Test
    void powerOptimized_picksHigherLoadRack() {
        AllocationStrategy strategy = new PowerOptimizedAllocationStrategy();
        // rackB is more loaded (75% power used vs rackA's 25%), consolidates better
        Optional<AllocationResult> result = strategy.findSlot(List.of(rackA, rackB), 4, 1.0);
        assertTrue(result.isPresent());
        assertEquals(2L, result.get().getRackId());
    }

    @Test
    void strategyNames_areCorrect() {
        assertEquals("FIRST_FIT", new FirstFitAllocationStrategy().getStrategyName());
        assertEquals("BEST_FIT", new BestFitAllocationStrategy().getStrategyName());
        assertEquals("POWER_OPTIMIZED", new PowerOptimizedAllocationStrategy().getStrategyName());
    }

    @Test
    void allocationResult_hasCorrectFields() {
        AllocationStrategy strategy = new FirstFitAllocationStrategy();
        AllocationResult result = strategy.findSlot(List.of(rackA), 4, 2.0).orElseThrow();
        assertEquals(1L, result.getRackId());
        assertEquals("Rack-A", result.getRackName());
        assertNotNull(result.getStrategyUsed());
        assertTrue(result.getUPosition() > 0);
    }
}
