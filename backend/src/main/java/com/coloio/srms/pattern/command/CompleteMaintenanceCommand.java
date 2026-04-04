package com.coloio.srms.pattern.command;

import com.coloio.srms.entity.MaintenanceTicketEntity;
import com.coloio.srms.repository.MaintenanceTicketRepository;

import java.time.LocalDateTime;

public class CompleteMaintenanceCommand extends AbstractServerCommand {

    private final MaintenanceTicketRepository ticketRepository;
    private final Long ticketId;
    private String previousStatus;
    private LocalDateTime previousResolvedAt;

    public CompleteMaintenanceCommand(MaintenanceTicketRepository ticketRepository, Long ticketId) {
        this.ticketRepository = ticketRepository;
        this.ticketId = ticketId;
    }

    @Override
    public void execute() {
        MaintenanceTicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + ticketId));
        previousStatus = ticket.getStatus();
        previousResolvedAt = ticket.getResolvedAt();
        ticket.setStatus("RESOLVED");
        ticket.setResolvedAt(LocalDateTime.now());
        ticketRepository.save(ticket);
        markExecuted();
    }

    @Override
    public void undo() {
        MaintenanceTicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + ticketId));
        ticket.setStatus(previousStatus);
        ticket.setResolvedAt(previousResolvedAt);
        ticketRepository.save(ticket);
        markUndone();
    }

    @Override
    public String getDescription() { return "Complete maintenance ticket " + ticketId; }

    @Override
    public String getCommandType() { return "COMPLETE_MAINTENANCE"; }
}
