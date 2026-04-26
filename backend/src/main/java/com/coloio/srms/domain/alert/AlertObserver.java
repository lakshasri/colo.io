package com.coloio.srms.domain.alert;

import com.coloio.srms.domain.enums.AlertSeverity;
import com.coloio.srms.domain.enums.AlertType;
import com.coloio.srms.domain.rack.RackEvent;
import com.coloio.srms.domain.rack.RackObserver;
import com.coloio.srms.entity.AlertEntity;
import com.coloio.srms.repository.AlertRepository;

public class AlertObserver implements RackObserver {

    private final AlertRepository alertRepository;

    public AlertObserver(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    @Override
    public void onRackEvent(RackEvent event) {
        AlertType type;
        AlertSeverity severity;

        switch (event.getType()) {
            case POWER_CRITICAL -> {
                type = AlertType.POWER;
                severity = AlertSeverity.CRITICAL;
            }
            case POWER_THRESHOLD_EXCEEDED -> {
                type = AlertType.POWER;
                severity = AlertSeverity.HIGH;
            }
            case USPACE_FULL -> {
                type = AlertType.USPACE;
                severity = AlertSeverity.HIGH;
            }
            case USPACE_LOW -> {
                type = AlertType.USPACE;
                severity = AlertSeverity.MEDIUM;
            }
            case STATUS_CHANGED -> {
                type = AlertType.HEALTH;
                severity = AlertSeverity.LOW;
            }
            default -> { return; } // SERVER_ADDED / REMOVED / POWER_UPDATED — no alert needed
        }

        AlertEntity alert = new AlertEntity();
        alert.setType(type);
        alert.setSeverity(severity);
        alert.setMessage(event.getDetails());
        alert.setSourceId(String.valueOf(event.getRackId()));
        alert.setSourceType("RACK");

        alertRepository.save(alert);

        // Also raise in in-memory AlertManager so listeners (WebSocket) are notified
        AlertManager.getInstance().raiseAlert(
                type, severity, event.getDetails(),
                String.valueOf(event.getRackId()), "RACK"
        );
    }
}
