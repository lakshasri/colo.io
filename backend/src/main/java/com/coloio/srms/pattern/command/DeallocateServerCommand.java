package com.coloio.srms.pattern.command;

import com.coloio.srms.domain.enums.ServerStatus;
import com.coloio.srms.entity.RackEntity;
import com.coloio.srms.entity.ServerEntity;
import com.coloio.srms.repository.RackRepository;
import com.coloio.srms.repository.ServerRepository;

public class DeallocateServerCommand extends AbstractServerCommand {

    private final ServerRepository serverRepository;
    private final RackRepository rackRepository;
    private final Long serverId;

    // state for undo
    private Long savedRackId;
    private int savedUPosition;

    public DeallocateServerCommand(ServerRepository serverRepository,
                                    RackRepository rackRepository,
                                    Long serverId) {
        this.serverRepository = serverRepository;
        this.rackRepository = rackRepository;
        this.serverId = serverId;
    }

    @Override
    public void execute() {
        ServerEntity server = serverRepository.findById(serverId)
                .orElseThrow(() -> new IllegalArgumentException("Server not found: " + serverId));
        savedRackId = server.getRack() != null ? server.getRack().getRackId() : null;
        savedUPosition = server.getUPosition();

        server.setRack(null);
        server.setUPosition(0);
        server.setStatus(ServerStatus.UNALLOCATED);
        serverRepository.save(server);
        markExecuted();
    }

    @Override
    public void undo() {
        ServerEntity server = serverRepository.findById(serverId)
                .orElseThrow(() -> new IllegalArgumentException("Server not found: " + serverId));
        if (savedRackId != null) {
            RackEntity rack = rackRepository.findById(savedRackId)
                    .orElseThrow(() -> new IllegalStateException("Rack not found for undo"));
            server.setRack(rack);
            server.setUPosition(savedUPosition);
            server.setStatus(ServerStatus.OPERATIONAL);
        }
        serverRepository.save(server);
        markUndone();
    }

    @Override
    public String getDescription() { return "Deallocate server " + serverId; }

    @Override
    public String getCommandType() { return "DEALLOCATE"; }
}
