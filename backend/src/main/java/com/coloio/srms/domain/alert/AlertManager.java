package com.coloio.srms.domain.alert;

import com.coloio.srms.domain.enums.AlertSeverity;
import com.coloio.srms.domain.enums.AlertType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class AlertManager {

    private static volatile AlertManager instance;

    private final Map<Long, Alert> activeAlerts = new ConcurrentHashMap<>();
    private final List<AlertListener> listeners = new CopyOnWriteArrayList<>();
    private long idSequence = 1;

    private AlertManager() {}

    public static AlertManager getInstance() {
        if (instance == null) {
            synchronized (AlertManager.class) {
                if (instance == null) {
                    instance = new AlertManager();
                }
            }
        }
        return instance;
    }

    public Alert raiseAlert(AlertType type, AlertSeverity severity,
                            String message, String sourceId, String sourceType) {
        Alert alert = new Alert(type, severity, message, sourceId, sourceType);
        alert.setAlertId(idSequence++);
        activeAlerts.put(alert.getAlertId(), alert);
        listeners.forEach(l -> l.onAlert(alert));
        return alert;
    }

    public void acknowledge(Long alertId, Long userId) {
        Alert alert = activeAlerts.get(alertId);
        if (alert != null) {
            alert.acknowledge(userId);
        }
    }

    public List<Alert> getActiveAlerts() {
        return activeAlerts.values().stream()
                .filter(a -> !a.isAcknowledged())
                .toList();
    }

    public List<Alert> getAllAlerts() {
        return new ArrayList<>(activeAlerts.values());
    }

    public void addListener(AlertListener listener) {
        listeners.add(listener);
    }

    public void removeListener(AlertListener listener) {
        listeners.remove(listener);
    }

    public void clearAll() {
        activeAlerts.clear();
    }
}
