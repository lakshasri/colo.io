package com.coloio.srms.service;

import com.coloio.srms.entity.*;
import com.coloio.srms.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock RackRepository rackRepository;
    @Mock ServerRepository serverRepository;
    @Mock ServerMetricRepository metricRepository;
    @Mock MaintenanceTicketRepository ticketRepository;
    @Mock ZoneRepository zoneRepository;

    @InjectMocks ReportService reportService;

    @Test
    void capacityReport_returnsCorrectTotals() {
        when(zoneRepository.findAll()).thenReturn(List.of());
        when(rackRepository.count()).thenReturn(5L);
        when(serverRepository.count()).thenReturn(20L);
        Map<String, Object> report = reportService.capacityReport();
        assertEquals(5L, report.get("totalRacks"));
        assertEquals(20L, report.get("totalServers"));
        assertNotNull(report.get("zones"));
    }

    @Test
    void serverUtilizationReport_returnsEmptyWhenNoServers() {
        when(serverRepository.findAll()).thenReturn(List.of());
        List<Map<String, Object>> result = reportService.serverUtilizationReport();
        assertTrue(result.isEmpty());
    }

    @Test
    void maintenanceHistoryReport_withNoTickets_returnsZeroCounts() {
        when(ticketRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of());
        Map<String, Object> report = reportService.maintenanceHistoryReport();
        assertEquals(0L, report.get("total"));
        assertEquals(0L, report.get("resolved"));
        assertEquals(0L, report.get("cancelled"));
        assertEquals(0, (int) (long) report.get("completionRatePct"));
    }

    @Test
    void maintenanceHistoryReport_calculatesCompletionRateCorrectly() {
        MaintenanceTicketEntity t1 = new MaintenanceTicketEntity();
        t1.setStatus("RESOLVED");
        t1.setPriority("HIGH");

        MaintenanceTicketEntity t2 = new MaintenanceTicketEntity();
        t2.setStatus("OPEN");
        t2.setPriority("LOW");

        when(ticketRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(t1, t2));
        Map<String, Object> report = reportService.maintenanceHistoryReport();

        assertEquals(2L, report.get("total"));
        assertEquals(1L, report.get("resolved"));
        assertEquals(50L, report.get("completionRatePct"));
    }

    @Test
    void maintenanceHistoryReport_groupsByPriority() {
        MaintenanceTicketEntity high = new MaintenanceTicketEntity();
        high.setStatus("RESOLVED");
        high.setPriority("HIGH");

        MaintenanceTicketEntity critical = new MaintenanceTicketEntity();
        critical.setStatus("OPEN");
        critical.setPriority("CRITICAL");

        when(ticketRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(high, critical));
        Map<String, Object> report = reportService.maintenanceHistoryReport();

        @SuppressWarnings("unchecked")
        Map<String, Long> byPriority = (Map<String, Long>) report.get("byPriority");
        assertEquals(1L, byPriority.get("HIGH"));
        assertEquals(1L, byPriority.get("CRITICAL"));
    }
}
