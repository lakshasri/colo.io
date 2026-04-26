package com.coloio.srms.domain.enums;

public enum RackEventType {
    SERVER_ADDED,
    SERVER_REMOVED,
    POWER_UPDATED,
    POWER_THRESHOLD_EXCEEDED,  // > 85%
    POWER_CRITICAL,            // > 95%
    USPACE_LOW,                // < 5U remaining
    USPACE_FULL,
    STATUS_CHANGED
}
