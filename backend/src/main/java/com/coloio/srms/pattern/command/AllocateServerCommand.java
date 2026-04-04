package com.coloio.srms.pattern.command;

import com.coloio.srms.domain.enums.ServerStatus;
import com.coloio.srms.entity.RackEntity;
import com.coloio.srms.entity.ServerEntity;
import com.coloio.srms.repository.RackRepository;
import com.coloio.srms.repository.ServerRepository;
import com.coloio.srms.service.RackService;
import com.coloio.srms.service.ServerService;
import com.coloio.srms.pattern.strategy.AllocationResult;

public class AllocateServerCommand extends AbstractServerCommand {

    private final ServerService serverService;
    private final ServerRepository serverRepository;
    private final RackRepository rackRepository;
    private final Long serverId;
    private final String strategyName;

    // state saved for undo
    private Long previousRackId;
    private Integer previousUPosition;
    private ServerStatus previousStatus;

    public AllocateServerCommand(ServerService serverService,
                                  ServerRepository serverRepository,
                                  RackRepository rackRepository,
                                  Long serverId,
                                  String strategyName) {
        this.serverService = serverService;
        this.serverRepository = serverRepository;
        this.rackRepository = rackRepository;
        this.serverId = serverId;
        this.strategyName = strategyName;
    }

    @Override
    public void execute() {
        ServerEntity server = serverRepository.findById(serverId)
                .orElseThrow(() -> new IllegalArgumentException("Server not found: " + serverId));
        previousRackId = server.getRack() != null ? server.getRack().getRackId() : null;
        previousUPosition = server.getUPosition();
        previousStatus = server.getStatus();

        serverService.allocateToRack(serverId, strategyName);
        markExecuted();
    }

    @Override
    public void undo() {
        ServerEntity server = serverRepository.findById(serverId)
                .orElseThrow(() -> new IllegalArgumentException("Server not found: " + serverId));
        RackEntity rack = previousRackId != null
                ? rackRepository.findById(previousRackId).orElse(null) : null;
        server.setRack(rack);
        server.setUPosition(previousUPosition);
        server.setStatus(previousStatus != null ? previousStatus : ServerStatus.UNALLOCATED);
        serverRepository.save(server);
        markUndone();
    }

    @Override
    public String getDescription() {
        return "Allocate server " + serverId + " using " + strategyName;
    }

    @Override
    public String getCommandType() { return "ALLOCATE"; }
}
