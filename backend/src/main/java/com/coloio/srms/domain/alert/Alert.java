package com.coloio.srms.domain.alert;

import com.coloio.srms.domain.enums.AlertSeverity;
import com.coloio.srms.domain.enums.AlertType;

import java.time.LocalDateTime;

public class Alert {

    private Long alertId;
    private AlertType type;
    private AlertSeverity severity;
    private String message;
    private String sourceId;
    private String sourceType;
    private LocalDateTime createdAt;
    private boolean acknowledged;
    private Long acknowledgedBy;
    private LocalDateTime acknowledgedAt;

    public Alert() {}

    public Alert(AlertType type, AlertSeverity severity, String message,
                 String sourceId, String sourceType) {
        this.type = type;
        this.severity = severity;
        this.message = message;
        this.sourceId = sourceId;
        this.sourceType = sourceType;
        this.createdAt = LocalDateTime.now();
        this.acknowledged = false;
    }

    public void acknowledge(Long userId) {
        this.acknowledged = true;
        this.acknowledgedBy = userId;
        this.acknowledgedAt = LocalDateTime.now();
    }

    public boolean isCritical() {
        return severity == AlertSeverity.CRITICAL || severity == AlertSeverity.HIGH;
    }

    // Getters & Setters
    public Long getAlertId() { return alertId; }
    public void setAlertId(Long alertId) { this.alertId = alertId; }

    public AlertType getType() { return type; }
    public void setType(AlertType type) { this.type = type; }

    public AlertSeverity getSeverity() { return severity; }
    public void setSeverity(AlertSeverity severity) { this.severity = severity; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getSourceId() { return sourceId; }
    public void setSourceId(String sourceId) { this.sourceId = sourceId; }

    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isAcknowledged() { return acknowledged; }
    public void setAcknowledged(boolean acknowledged) { this.acknowledged = acknowledged; }

    public Long getAcknowledgedBy() { return acknowledgedBy; }
    public void setAcknowledgedBy(Long acknowledgedBy) { this.acknowledgedBy = acknowledgedBy; }

    public LocalDateTime getAcknowledgedAt() { return acknowledgedAt; }
    public void setAcknowledgedAt(LocalDateTime acknowledgedAt) { this.acknowledgedAt = acknowledgedAt; }
}
