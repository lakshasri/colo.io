package com.coloio.srms.domain.alert;

import com.coloio.srms.domain.rack.RackEvent;
import com.coloio.srms.domain.rack.RackObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sends in-app / email notifications to customers and admins when
 * critical rack events occur. Email integration is a stub — replaced
 * with a real mail sender (Spring Mail) in a later sprint.
 */
public class NotificationObserver implements RackObserver {

    private static final Logger log = LoggerFactory.getLogger(NotificationObserver.class);

    @Override
    public void onRackEvent(RackEvent event) {
        switch (event.getType()) {
            case POWER_CRITICAL, USPACE_FULL -> {
                log.warn("[NOTIFY] CRITICAL event on rack '{}': {}",
                        event.getRackName(), event.getDetails());
                // TODO: send email via JavaMailSender to DC Admin
            }
            case POWER_THRESHOLD_EXCEEDED, USPACE_LOW -> {
                log.info("[NOTIFY] Warning on rack '{}': {}",
                        event.getRackName(), event.getDetails());
                // TODO: in-app notification to affected customers
            }
            case STATUS_CHANGED -> {
                log.info("[NOTIFY] Rack '{}' status changed: {}",
                        event.getRackName(), event.getDetails());
                // TODO: notify customers whose servers are in this rack
            }
            default -> { /* no notification needed */ }
        }
    }
}
