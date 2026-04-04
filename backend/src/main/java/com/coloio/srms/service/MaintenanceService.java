package com.coloio.srms.service;

import com.coloio.srms.entity.ChecklistItemEntity;
import com.coloio.srms.entity.MaintenanceTicketEntity;
import com.coloio.srms.pattern.command.*;
import com.coloio.srms.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class MaintenanceService {

    private final MaintenanceTicketRepository ticketRepository;
    private final ChecklistItemRepository checklistRepository;
    private final ServerRepository serverRepository;
    private final UserRepository userRepository;
    private final CommandInvoker commandInvoker;

    public MaintenanceService(MaintenanceTicketRepository ticketRepository,
                               ChecklistItemRepository checklistRepository,
                               ServerRepository serverRepository,
                               UserRepository userRepository,
                               CommandInvoker commandInvoker) {
        this.ticketRepository = ticketRepository;
        this.checklistRepository = checklistRepository;
        this.serverRepository = serverRepository;
        this.userRepository = userRepository;
        this.commandInvoker = commandInvoker;
    }

    public MaintenanceTicketEntity scheduleTicket(Long serverId, String title,
                                                   String description, String priority,
                                                   LocalDateTime scheduledAt) {
        ScheduleMaintenanceCommand cmd = new ScheduleMaintenanceCommand(
                ticketRepository, serverRepository,
                serverId, title, description, priority, scheduledAt);
        commandInvoker.execute(cmd);
        return ticketRepository.findById(cmd.getCreatedTicketId())
                .orElseThrow(() -> new IllegalStateException("Ticket not created"));
    }

    public MaintenanceTicketEntity assignTechnician(Long ticketId, Long technicianId) {
        commandInvoker.execute(new AssignTechnicianCommand(
                ticketRepository, userRepository, ticketId, technicianId));
        return getTicket(ticketId);
    }

    public MaintenanceTicketEntity startTicket(Long ticketId) {
        commandInvoker.execute(new StartMaintenanceCommand(ticketRepository, ticketId));
        return getTicket(ticketId);
    }

    public MaintenanceTicketEntity completeTicket(Long ticketId) {
        commandInvoker.execute(new CompleteMaintenanceCommand(ticketRepository, ticketId));
        return getTicket(ticketId);
    }

    public MaintenanceTicketEntity cancelTicket(Long ticketId, String reason) {
        commandInvoker.execute(new CancelMaintenanceCommand(ticketRepository, ticketId, reason));
        return getTicket(ticketId);
    }

    public MaintenanceTicketEntity approveTicket(Long ticketId) {
        MaintenanceTicketEntity ticket = getTicket(ticketId);
        ticket.setApproved(true);
        ticket.setStatus("PENDING");
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

    // Checklist operations
    public ChecklistItemEntity addChecklistItem(Long ticketId, String description) {
        MaintenanceTicketEntity ticket = getTicket(ticketId);
        ChecklistItemEntity item = new ChecklistItemEntity();
        item.setTicket(ticket);
        item.setDescription(description);
        return checklistRepository.save(item);
    }

    public ChecklistItemEntity tickChecklistItem(Long itemId, String completedBy) {
        ChecklistItemEntity item = checklistRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Checklist item not found: " + itemId));
        item.setCompleted(true);
        item.setCompletedBy(completedBy);
        return checklistRepository.save(item);
    }

    @Transactional(readOnly = true)
    public List<ChecklistItemEntity> getChecklist(Long ticketId) {
        return checklistRepository.findByTicket_TicketIdOrderByItemIdAsc(ticketId);
    }
}
