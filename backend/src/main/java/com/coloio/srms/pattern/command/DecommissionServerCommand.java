package com.coloio.srms.pattern.command;

import com.coloio.srms.domain.enums.ServerStatus;
import com.coloio.srms.entity.RackEntity;
import com.coloio.srms.entity.ServerEntity;
import com.coloio.srms.repository.RackRepository;
import com.coloio.srms.repository.ServerRepository;

public class DecommissionServerCommand extends AbstractServerCommand {

    private final ServerRepository serverRepository;
    private final RackRepository rackRepository;
    private final Long serverId;

    // state for undo
    private ServerStatus previousStatus;
    private Long previousRackId;
    private int previousUPosition;

    public DecommissionServerCommand(ServerRepository serverRepository,
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

        // save state for undo
        previousStatus = server.getStatus();
        previousRackId = server.getRack() != null ? server.getRack().getRackId() : null;
        previousUPosition = server.getUPosition();

        // remove from rack and mark decommissioned
        server.setRack(null);
        server.setUPosition(0);
        server.setStatus(ServerStatus.DECOMMISSIONED);
        serverRepository.save(server);
        markExecuted();
    }

    @Override
    public void undo() {
        ServerEntity server = serverRepository.findById(serverId)
                .orElseThrow(() -> new IllegalArgumentException("Server not found: " + serverId));

        server.setStatus(previousStatus != null ? previousStatus : ServerStatus.UNALLOCATED);
        server.setUPosition(previousUPosition);
        if (previousRackId != null) {
            RackEntity rack = rackRepository.findById(previousRackId)
                    .orElse(null);
            server.setRack(rack);
        }
        serverRepository.save(server);
        markUndone();
    }

    @Override
    public String getDescription() { return "Decommission server " + serverId; }

    @Override
    public String getCommandType() { return "DECOMMISSION"; }
}
