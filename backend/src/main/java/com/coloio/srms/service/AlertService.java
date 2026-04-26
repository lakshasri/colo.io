package com.coloio.srms.service;

import com.coloio.srms.domain.enums.AlertSeverity;
import com.coloio.srms.domain.enums.AlertType;
import com.coloio.srms.entity.AlertEntity;
import com.coloio.srms.repository.AlertRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class AlertService {

    private final AlertRepository alertRepository;

    public AlertService(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    @Transactional(readOnly = true)
    public List<AlertEntity> getActiveAlerts() {
        return alertRepository.findAllByAcknowledgedFalseOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<AlertEntity> getAlertsBySource(String sourceType, String sourceId) {
        return alertRepository.findAllBySourceTypeAndSourceId(sourceType, sourceId);
    }

    @Transactional(readOnly = true)
    public List<AlertEntity> getAlertsBySeverity(AlertSeverity severity) {
        return alertRepository.findAllBySeverityOrderByCreatedAtDesc(severity);
    }

    @Transactional(readOnly = true)
    public List<AlertEntity> getAlertsByType(AlertType type) {
        return alertRepository.findAllByTypeOrderByCreatedAtDesc(type);
    }

    public AlertEntity acknowledge(Long alertId, Long userId) {
        AlertEntity alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found: " + alertId));
        alert.setAcknowledged(true);
        alert.setAcknowledgedBy(userId);
        alert.setAcknowledgedAt(LocalDateTime.now());
        return alertRepository.save(alert);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getStats() {
        long total  = alertRepository.count();
        long active = alertRepository.countByAcknowledgedFalse();
        return Map.of(
                "total",        total,
                "active",       active,
                "acknowledged", total - active
        );
    }
}
