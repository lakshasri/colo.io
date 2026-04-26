package com.coloio.srms.service;

import com.coloio.srms.entity.MaintenanceTicketEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final SimpMessagingTemplate messaging;

    public NotificationService(SimpMessagingTemplate messaging) {
        this.messaging = messaging;
    }

    public void notifyScheduled(MaintenanceTicketEntity ticket) {
        send("SCHEDULED", ticket, "Maintenance scheduled: " + ticket.getTitle());
    }

    public void notifyStarted(MaintenanceTicketEntity ticket) {
        send("STARTED", ticket, "Maintenance started: " + ticket.getTitle());
    }

    public void notifyCompleted(MaintenanceTicketEntity ticket) {
        send("COMPLETED", ticket, "Maintenance completed: " + ticket.getTitle());
    }

    public void notifyCancelled(MaintenanceTicketEntity ticket) {
        send("CANCELLED", ticket, "Maintenance cancelled: " + ticket.getTitle());
    }

    public void notifyApproved(MaintenanceTicketEntity ticket) {
        send("APPROVED", ticket, "Maintenance approved: " + ticket.getTitle());
    }

    private void send(String eventType, MaintenanceTicketEntity ticket, String message) {
        Map<String, Object> payload = Map.of(
                "event", eventType,
                "ticketId", ticket.getTicketId(),
                "title", ticket.getTitle(),
                "message", message,
                "timestamp", LocalDateTime.now().toString()
        );
        try {
            messaging.convertAndSend("/topic/maintenance", payload);
        } catch (Exception e) {
            log.warn("Failed to push notification: {}", e.getMessage());
        }
        log.info("[NOTIFICATION] {} — {}", eventType, message);
    }
}
