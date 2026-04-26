package com.coloio.srms.pattern.strategy;

public class AllocationResult {
    private final Long rackId;
    private final String rackName;
    private final int uPosition;
    private final String strategyUsed;

    public AllocationResult(Long rackId, String rackName, int uPosition, String strategyUsed) {
        this.rackId = rackId;
        this.rackName = rackName;
        this.uPosition = uPosition;
        this.strategyUsed = strategyUsed;
    }

    public Long getRackId() { return rackId; }
    public String getRackName() { return rackName; }
    public int getUPosition() { return uPosition; }
    public String getStrategyUsed() { return strategyUsed; }
}
