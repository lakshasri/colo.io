package com.coloio.srms.pattern.command;

import com.coloio.srms.entity.MaintenanceTicketEntity;
import com.coloio.srms.repository.MaintenanceTicketRepository;

import java.time.LocalDateTime;

public class StartMaintenanceCommand extends AbstractServerCommand {

    private final MaintenanceTicketRepository ticketRepository;
    private final Long ticketId;
    private String previousStatus;

    public StartMaintenanceCommand(MaintenanceTicketRepository ticketRepository, Long ticketId) {
        this.ticketRepository = ticketRepository;
        this.ticketId = ticketId;
    }

    @Override
    public void execute() {
        MaintenanceTicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + ticketId));
        previousStatus = ticket.getStatus();
        ticket.setStatus("IN_PROGRESS");
        ticket.setStartedAt(LocalDateTime.now());
        ticketRepository.save(ticket);
        markExecuted();
    }

    @Override
    public void undo() {
        MaintenanceTicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + ticketId));
        ticket.setStatus(previousStatus);
        ticket.setStartedAt(null);
        ticketRepository.save(ticket);
        markUndone();
    }

    @Override
    public String getDescription() { return "Start maintenance ticket " + ticketId; }

    @Override
    public String getCommandType() { return "START_MAINTENANCE"; }
}
