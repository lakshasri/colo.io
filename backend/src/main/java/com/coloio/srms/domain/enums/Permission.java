package com.coloio.srms.domain.enums;

public enum Permission {
    // Rack & Server
    MANAGE_RACKS,
    MANAGE_SERVERS,
    VIEW_RACKS,
    VIEW_SERVERS,
    UPDATE_SERVER_STATUS,

    // Users
    MANAGE_USERS,

    // Maintenance
    SCHEDULE_MAINTENANCE,
    PERFORM_MAINTENANCE,
    APPROVE_MAINTENANCE,
    VIEW_MAINTENANCE,

    // Zones
    MANAGE_ZONES,
    VIEW_ZONES,

    // Alerts
    CONFIGURE_ALERTS,
    ACKNOWLEDGE_ALERTS,
    VIEW_ALERTS,

    // Monitoring
    VIEW_METRICS,
    VIEW_OWN_SERVERS,

    // Reports
    VIEW_REPORTS,
    VIEW_AUDIT_LOG
}
