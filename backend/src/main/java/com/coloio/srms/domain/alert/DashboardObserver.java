package com.coloio.srms.domain.alert;

import com.coloio.srms.domain.rack.RackEvent;
import com.coloio.srms.domain.rack.RackObserver;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Map;

public class DashboardObserver implements RackObserver {

    private final SimpMessagingTemplate messagingTemplate;

    public DashboardObserver(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void onRackEvent(RackEvent event) {
        // Push rack event to subscribers of the rack-specific topic
        messagingTemplate.convertAndSend(
                "/topic/rack/" + event.getRackId(),
                Map.of(
                        "rackId",      event.getRackId(),
                        "rackName",    event.getRackName(),
                        "eventType",   event.getType().name(),
                        "details",     event.getDetails(),
                        "occurredAt",  event.getOccurredAt().toString()
                )
        );

        // Also push to the global alerts topic for critical events
        switch (event.getType()) {
            case POWER_CRITICAL, POWER_THRESHOLD_EXCEEDED, USPACE_FULL, USPACE_LOW -> {
                messagingTemplate.convertAndSend(
                        "/topic/alerts",
                        Map.of(
                                "sourceType",  "RACK",
                                "sourceId",    String.valueOf(event.getRackId()),
                                "sourceName",  event.getRackName(),
                                "eventType",   event.getType().name(),
                                "message",     event.getDetails(),
                                "occurredAt",  event.getOccurredAt().toString()
                        )
                );
            }
            default -> { /* no broadcast needed */ }
        }
    }
}
