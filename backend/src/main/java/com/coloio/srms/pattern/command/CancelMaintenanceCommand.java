package com.coloio.srms.pattern.command;

import com.coloio.srms.entity.MaintenanceTicketEntity;
import com.coloio.srms.repository.MaintenanceTicketRepository;

public class CancelMaintenanceCommand extends AbstractServerCommand {

    private final MaintenanceTicketRepository ticketRepository;
    private final Long ticketId;
    private final String reason;
    private String previousStatus;

    public CancelMaintenanceCommand(MaintenanceTicketRepository ticketRepository,
                                     Long ticketId, String reason) {
        this.ticketRepository = ticketRepository;
        this.ticketId = ticketId;
        this.reason = reason;
    }

    @Override
    public void execute() {
        MaintenanceTicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + ticketId));
        previousStatus = ticket.getStatus();
        ticket.setStatus("CANCELLED");
        ticket.setDescription((ticket.getDescription() != null ? ticket.getDescription() + " | " : "")
                + "Cancelled: " + reason);
        ticketRepository.save(ticket);
        markExecuted();
    }

    @Override
    public void undo() {
        MaintenanceTicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + ticketId));
        ticket.setStatus(previousStatus);
        ticketRepository.save(ticket);
        markUndone();
    }

    @Override
    public String getDescription() { return "Cancel maintenance ticket " + ticketId + ": " + reason; }

    @Override
    public String getCommandType() { return "CANCEL_MAINTENANCE"; }
}
