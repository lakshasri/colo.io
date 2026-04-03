package com.coloio.srms.config;

import com.coloio.srms.domain.alert.AlertObserver;
import com.coloio.srms.domain.alert.DashboardObserver;
import com.coloio.srms.domain.alert.NotificationObserver;
import com.coloio.srms.domain.rack.Rack;
import com.coloio.srms.repository.AlertRepository;
import com.coloio.srms.repository.RackRepository;
import com.coloio.srms.service.RackService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ObserverRegistrar implements ApplicationRunner {

    // Keeps live Rack domain objects (with observers) keyed by rackId
    public static final Map<Long, Rack> liveRacks = new ConcurrentHashMap<>();

    private final RackRepository rackRepository;
    private final RackService rackService;
    private final AlertRepository alertRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ObserverRegistrar(RackRepository rackRepository,
                             RackService rackService,
                             AlertRepository alertRepository,
                             SimpMessagingTemplate messagingTemplate) {
        this.rackRepository = rackRepository;
        this.rackService = rackService;
        this.alertRepository = alertRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        rackRepository.findAll().forEach(entity -> {
            Rack rack = rackService.toDomain(entity);
            attachObservers(rack);
            liveRacks.put(rack.getRackId(), rack);
        });
    }

    public static Rack getOrCreate(Long rackId, Rack fallback,
                                   AlertRepository alertRepository,
                                   SimpMessagingTemplate messagingTemplate) {
        return liveRacks.computeIfAbsent(rackId, id -> {
            attachObservers(fallback, alertRepository, messagingTemplate);
            return fallback;
        });
    }

    private void attachObservers(Rack rack) {
        rack.addObserver(new AlertObserver(alertRepository));
        rack.addObserver(new NotificationObserver());
        rack.addObserver(new DashboardObserver(messagingTemplate));
    }

    private static void attachObservers(Rack rack,
                                        AlertRepository alertRepository,
                                        SimpMessagingTemplate messagingTemplate) {
        rack.addObserver(new AlertObserver(alertRepository));
        rack.addObserver(new NotificationObserver());
        rack.addObserver(new DashboardObserver(messagingTemplate));
    }
}
