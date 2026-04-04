package com.coloio.srms.service;

import com.coloio.srms.entity.MaintenanceTicketEntity;
import com.coloio.srms.pattern.command.CommandInvoker;
import com.coloio.srms.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaintenanceServiceTest {

    @Mock MaintenanceTicketRepository ticketRepository;
    @Mock ChecklistItemRepository checklistRepository;
    @Mock ServerRepository serverRepository;
    @Mock UserRepository userRepository;
    @Mock CommandInvoker commandInvoker;
    @Mock NotificationService notificationService;
    @InjectMocks MaintenanceService maintenanceService;

    private MaintenanceTicketEntity ticket(String status) {
        MaintenanceTicketEntity t = new MaintenanceTicketEntity();
        t.setTicketId(1L);
        t.setTitle("Replace disk");
        t.setStatus(status);
        t.setPriority("HIGH");
        t.setCreatedAt(LocalDateTime.now());
        return t;
    }

    @Test
    void getTicket_returnsTicketById() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket("OPEN")));
        MaintenanceTicketEntity result = maintenanceService.getTicket(1L);
        assertEquals("OPEN", result.getStatus());
        assertEquals(1L, result.getTicketId());
    }

    @Test
    void getTicket_throwsWhenNotFound() {
        when(ticketRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> maintenanceService.getTicket(999L));
    }

    @Test
    void getTicketsByStatus_delegatesToRepository() {
        when(ticketRepository.findByStatusOrderByCreatedAtDesc("OPEN"))
                .thenReturn(List.of(ticket("OPEN")));
        List<MaintenanceTicketEntity> results = maintenanceService.getTicketsByStatus("OPEN");
        assertEquals(1, results.size());
        assertEquals("OPEN", results.get(0).getStatus());
    }

    @Test
    void getAllTickets_returnsAll() {
        when(ticketRepository.findAllByOrderByCreatedAtDesc())
                .thenReturn(List.of(ticket("OPEN"), ticket("RESOLVED")));
        List<MaintenanceTicketEntity> results = maintenanceService.getAllTickets();
        assertEquals(2, results.size());
    }

    @Test
    void cancelTicket_executesCommandAndNotifies() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket("OPEN")));
        doNothing().when(commandInvoker).execute(any());
        doNothing().when(notificationService).notifyCancelled(any());

        MaintenanceTicketEntity result = maintenanceService.cancelTicket(1L, "hardware failure");

        verify(commandInvoker).execute(any());
        verify(notificationService).notifyCancelled(any());
        assertNotNull(result);
    }

    @Test
    void approveTicket_setsApprovedAndPendingStatus() {
        MaintenanceTicketEntity t = ticket("OPEN");
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(t));
        when(ticketRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(notificationService).notifyApproved(any());

        MaintenanceTicketEntity result = maintenanceService.approveTicket(1L);

        assertTrue(result.isApproved());
        assertEquals("PENDING", result.getStatus());
    }

    @Test
    void updateStatus_setsResolvedAtOnResolve() {
        MaintenanceTicketEntity t = ticket("IN_PROGRESS");
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(t));
        when(ticketRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        MaintenanceTicketEntity result = maintenanceService.updateStatus(1L, "RESOLVED", null);

        assertEquals("RESOLVED", result.getStatus());
        assertNotNull(result.getResolvedAt());
    }
}
