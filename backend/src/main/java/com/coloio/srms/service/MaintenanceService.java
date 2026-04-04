package com.coloio.srms.service;

import com.coloio.srms.entity.MaintenanceTicketEntity;
import com.coloio.srms.entity.ServerEntity;
import com.coloio.srms.repository.MaintenanceTicketRepository;
import com.coloio.srms.repository.ServerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class MaintenanceService {

    private final MaintenanceTicketRepository ticketRepository;
    private final ServerRepository serverRepository;

    public MaintenanceService(MaintenanceTicketRepository ticketRepository,
                               ServerRepository serverRepository) {
        this.ticketRepository = ticketRepository;
        this.serverRepository = serverRepository;
    }

    public MaintenanceTicketEntity createTicket(Long serverId, String title,
                                                 String description, String priority) {
        ServerEntity server = serverRepository.findById(serverId)
                .orElseThrow(() -> new IllegalArgumentException("Server not found: " + serverId));
        MaintenanceTicketEntity ticket = new MaintenanceTicketEntity();
        ticket.setServer(server);
        ticket.setTitle(title);
        ticket.setDescription(description);
        ticket.setPriority(priority);
        ticket.setStatus("OPEN");
        return ticketRepository.save(ticket);
    }

    @Transactional(readOnly = true)
    public List<MaintenanceTicketEntity> getAllTickets() {
        return ticketRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public MaintenanceTicketEntity getTicket(Long ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + ticketId));
    }

    @Transactional(readOnly = true)
    public List<MaintenanceTicketEntity> getTicketsByServer(Long serverId) {
        return ticketRepository.findByServer_ServerIdOrderByCreatedAtDesc(serverId);
    }

    @Transactional(readOnly = true)
    public List<MaintenanceTicketEntity> getTicketsByStatus(String status) {
        return ticketRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    public MaintenanceTicketEntity updateStatus(Long ticketId, String newStatus, String assignedTo) {
        MaintenanceTicketEntity ticket = getTicket(ticketId);
        ticket.setStatus(newStatus);
        if (assignedTo != null) ticket.setAssignedTo(assignedTo);
        if ("RESOLVED".equals(newStatus) || "CLOSED".equals(newStatus)) {
            ticket.setResolvedAt(LocalDateTime.now());
        }
        return ticketRepository.save(ticket);
    }
}
