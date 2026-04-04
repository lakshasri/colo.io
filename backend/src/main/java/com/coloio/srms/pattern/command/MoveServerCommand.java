package com.coloio.srms.pattern.command;

import com.coloio.srms.entity.RackEntity;
import com.coloio.srms.entity.ServerEntity;
import com.coloio.srms.repository.RackRepository;
import com.coloio.srms.repository.ServerRepository;

public class MoveServerCommand extends AbstractServerCommand {

    private final ServerRepository serverRepository;
    private final RackRepository rackRepository;
    private final Long serverId;
    private final Long targetRackId;

    // state for undo
    private Long sourceRackId;
    private int sourceUPosition;

    public MoveServerCommand(ServerRepository serverRepository,
                              RackRepository rackRepository,
                              Long serverId,
                              Long targetRackId) {
        this.serverRepository = serverRepository;
        this.rackRepository = rackRepository;
        this.serverId = serverId;
        this.targetRackId = targetRackId;
    }

    @Override
    public void execute() {
        ServerEntity server = serverRepository.findById(serverId)
                .orElseThrow(() -> new IllegalArgumentException("Server not found: " + serverId));
        sourceRackId = server.getRack() != null ? server.getRack().getRackId() : null;
        sourceUPosition = server.getUPosition();

        RackEntity target = rackRepository.findById(targetRackId)
                .orElseThrow(() -> new IllegalArgumentException("Target rack not found: " + targetRackId));
        server.setRack(target);
        serverRepository.save(server);
        markExecuted();
    }

    @Override
    public void undo() {
        ServerEntity server = serverRepository.findById(serverId)
                .orElseThrow(() -> new IllegalArgumentException("Server not found: " + serverId));
        if (sourceRackId != null) {
            RackEntity source = rackRepository.findById(sourceRackId)
                    .orElseThrow(() -> new IllegalStateException("Source rack not found for undo"));
            server.setRack(source);
        } else {
            server.setRack(null);
        }
        server.setUPosition(sourceUPosition);
        serverRepository.save(server);
        markUndone();
    }

    @Override
    public String getDescription() {
        return "Move server " + serverId + " to rack " + targetRackId;
    }

    @Override
    public String getCommandType() { return "MOVE"; }
}
