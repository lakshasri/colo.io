package com.coloio.srms.controller;

import com.coloio.srms.repository.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@Tag(name = "Dashboard", description = "Role-specific KPI summaries")
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final RackRepository rackRepository;
    private final ServerRepository serverRepository;
    private final AlertRepository alertRepository;
    private final MaintenanceTicketRepository ticketRepository;
    private final ZoneRepository zoneRepository;
    private final UserRepository userRepository;

    public DashboardController(RackRepository rackRepository,
                                ServerRepository serverRepository,
                                AlertRepository alertRepository,
                                MaintenanceTicketRepository ticketRepository,
                                ZoneRepository zoneRepository,
                                UserRepository userRepository) {
        this.rackRepository = rackRepository;
        this.serverRepository = serverRepository;
        this.alertRepository = alertRepository;
        this.ticketRepository = ticketRepository;
        this.zoneRepository = zoneRepository;
        this.userRepository = userRepository;
    }

    @Operation(summary = "Role-specific dashboard KPIs")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getDashboard(
            @AuthenticationPrincipal UserDetails userDetails) {

        String role = userDetails.getAuthorities().iterator().next()
                .getAuthority().replace("ROLE_", "");

        Map<String, Object> kpis = new LinkedHashMap<>();
        kpis.put("role", role);

        switch (role) {
            case "DC_ADMIN" -> {
                kpis.put("totalRacks", rackRepository.count());
                kpis.put("totalServers", serverRepository.count());
                kpis.put("activeAlerts", alertRepository.countByAcknowledgedFalse());
                kpis.put("openTickets", ticketRepository.countByStatus("OPEN"));
                kpis.put("totalUsers", userRepository.count());
                kpis.put("totalZones", zoneRepository.count());
            }
            case "TECHNICIAN" -> {
                kpis.put("openTickets", ticketRepository.countByStatus("OPEN"));
                kpis.put("inProgressTickets", ticketRepository.countByStatus("IN_PROGRESS"));
                kpis.put("activeAlerts", alertRepository.countByAcknowledgedFalse());
                kpis.put("totalServers", serverRepository.count());
            }
            case "MANAGER" -> {
                kpis.put("totalZones", zoneRepository.count());
                kpis.put("totalRacks", rackRepository.count());
                kpis.put("resolvedTickets", ticketRepository.countByStatus("RESOLVED"));
                kpis.put("pendingApprovals", ticketRepository.countByApprovedFalseAndStatus("OPEN"));
            }
            case "CUSTOMER" -> {
                Long userId = userRepository.findByUsername(userDetails.getUsername())
                        .orElseThrow().getUserId();
                kpis.put("allocatedServers", serverRepository.findAllByCustomer_UserId(userId).size());
                kpis.put("activeAlerts", alertRepository.countByAcknowledgedFalse());
            }
        }

        return ResponseEntity.ok(kpis);
    }
}
