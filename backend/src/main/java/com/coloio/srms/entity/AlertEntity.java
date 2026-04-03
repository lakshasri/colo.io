package com.coloio.srms.entity;

import com.coloio.srms.domain.enums.AlertSeverity;
import com.coloio.srms.domain.enums.AlertType;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "alerts")
public class AlertEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long alertId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlertType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AlertSeverity severity;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    private String sourceId;

    @Column(length = 20)
    private String sourceType;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private boolean acknowledged = false;

    private Long acknowledgedBy;

    private LocalDateTime acknowledgedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
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
