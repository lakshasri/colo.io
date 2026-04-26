package com.coloio.srms.pattern.command;

import com.coloio.srms.entity.MaintenanceTicketEntity;
import com.coloio.srms.entity.ServerEntity;
import com.coloio.srms.repository.MaintenanceTicketRepository;
import com.coloio.srms.repository.ServerRepository;

import java.time.LocalDateTime;

public class ScheduleMaintenanceCommand extends AbstractServerCommand {

    private final MaintenanceTicketRepository ticketRepository;
    private final ServerRepository serverRepository;
    private final Long serverId;
    private final String title;
    private final String description;
    private final String priority;
    private final LocalDateTime scheduledAt;

    private Long createdTicketId;

    public ScheduleMaintenanceCommand(MaintenanceTicketRepository ticketRepository,
                                       ServerRepository serverRepository,
                                       Long serverId, String title,
                                       String description, String priority,
                                       LocalDateTime scheduledAt) {
        this.ticketRepository = ticketRepository;
        this.serverRepository = serverRepository;
        this.serverId = serverId;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.scheduledAt = scheduledAt;
    }

    @Override
    public void execute() {
        ServerEntity server = serverRepository.findById(serverId)
                .orElseThrow(() -> new IllegalArgumentException("Server not found: " + serverId));
        MaintenanceTicketEntity ticket = new MaintenanceTicketEntity();
        ticket.setServer(server);
        ticket.setTitle(title);
        ticket.setDescription(description);
        ticket.setPriority(priority);
        ticket.setScheduledAt(scheduledAt);
        ticket.setStatus("PENDING");
        MaintenanceTicketEntity saved = ticketRepository.save(ticket);
        createdTicketId = saved.getTicketId();
        markExecuted();
    }

    @Override
    public void undo() {
        if (createdTicketId != null) {
            ticketRepository.deleteById(createdTicketId);
        }
        markUndone();
    }

    @Override
    public String getDescription() {
        return "Schedule maintenance for server " + serverId + " at " + scheduledAt;
    }

    @Override
    public String getCommandType() { return "SCHEDULE_MAINTENANCE"; }

    public Long getCreatedTicketId() { return createdTicketId; }
}
