package com.coloio.srms.domain.rack;

import com.coloio.srms.domain.enums.RackEventType;

import java.time.LocalDateTime;

public class RackEvent {

    private final RackEventType type;
    private final Long rackId;
    private final String rackName;
    private final String details;
    private final LocalDateTime occurredAt;

    public RackEvent(RackEventType type, Long rackId, String rackName, String details) {
        this.type = type;
        this.rackId = rackId;
        this.rackName = rackName;
        this.details = details;
        this.occurredAt = LocalDateTime.now();
    }

    public RackEventType getType() { return type; }
    public Long getRackId() { return rackId; }
    public String getRackName() { return rackName; }
    public String getDetails() { return details; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
}
