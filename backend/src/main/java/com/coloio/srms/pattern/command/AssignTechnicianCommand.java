package com.coloio.srms.pattern.command;

import com.coloio.srms.entity.MaintenanceTicketEntity;
import com.coloio.srms.entity.UserEntity;
import com.coloio.srms.repository.MaintenanceTicketRepository;
import com.coloio.srms.repository.UserRepository;

public class AssignTechnicianCommand extends AbstractServerCommand {

    private final MaintenanceTicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final Long ticketId;
    private final Long technicianId;

    // state for undo
    private UserEntity previousTechnician;
    private String previousAssignedTo;

    public AssignTechnicianCommand(MaintenanceTicketRepository ticketRepository,
                                    UserRepository userRepository,
                                    Long ticketId, Long technicianId) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.ticketId = ticketId;
        this.technicianId = technicianId;
    }

    @Override
    public void execute() {
        MaintenanceTicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + ticketId));
        UserEntity technician = userRepository.findById(technicianId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + technicianId));

        previousTechnician = ticket.getTechnician();
        previousAssignedTo = ticket.getAssignedTo();

        ticket.setTechnician(technician);
        ticket.setAssignedTo(technician.getUsername());
        ticketRepository.save(ticket);
        markExecuted();
    }

    @Override
    public void undo() {
        MaintenanceTicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + ticketId));
        ticket.setTechnician(previousTechnician);
        ticket.setAssignedTo(previousAssignedTo);
        ticketRepository.save(ticket);
        markUndone();
    }

    @Override
    public String getDescription() {
        return "Assign technician " + technicianId + " to ticket " + ticketId;
    }

    @Override
    public String getCommandType() { return "ASSIGN_TECHNICIAN"; }
}
