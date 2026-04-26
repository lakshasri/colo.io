package com.coloio.srms.pattern.command;

import com.coloio.srms.domain.enums.ServerStatus;
import com.coloio.srms.entity.RackEntity;
import com.coloio.srms.entity.ServerEntity;
import com.coloio.srms.repository.RackRepository;
import com.coloio.srms.repository.ServerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DecommissionServerCommandTest {

    @Mock ServerRepository serverRepository;
    @Mock RackRepository rackRepository;

    private ServerEntity server;
    private RackEntity rack;
    private DecommissionServerCommand command;

    @BeforeEach
    void setUp() {
        rack = new RackEntity();
        rack.setRackId(5L);

        server = new ServerEntity();
        server.setServerId(1L);
        server.setHostname("srv-test");
        server.setStatus(ServerStatus.OPERATIONAL);
        server.setRack(rack);
        server.setUPosition(10);

        command = new DecommissionServerCommand(serverRepository, rackRepository, 1L);
    }

    @Test
    void execute_setsStatusDecommissionedAndClearsRack() {
        when(serverRepository.findById(1L)).thenReturn(Optional.of(server));
        when(serverRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        command.execute();

        assertEquals(ServerStatus.DECOMMISSIONED, server.getStatus());
        assertNull(server.getRack());
        assertEquals(0, server.getUPosition());
        verify(serverRepository).save(server);
    }

    @Test
    void execute_throwsWhenServerNotFound() {
        when(serverRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> command.execute());
    }

    @Test
    void undo_restoresPreviousStateWithRack() {
        when(serverRepository.findById(1L)).thenReturn(Optional.of(server));
        when(serverRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        command.execute();

        // Now undo
        ServerEntity restored = new ServerEntity();
        restored.setServerId(1L);
        restored.setStatus(ServerStatus.DECOMMISSIONED);
        when(serverRepository.findById(1L)).thenReturn(Optional.of(restored));
        when(rackRepository.findById(5L)).thenReturn(Optional.of(rack));

        command.undo();

        assertEquals(ServerStatus.OPERATIONAL, restored.getStatus());
        assertEquals(rack, restored.getRack());
        assertEquals(10, restored.getUPosition());
    }

    @Test
    void getCommandType_returnsDecommission() {
        assertEquals("DECOMMISSION", command.getCommandType());
    }

    @Test
    void getDescription_containsServerId() {
        assertTrue(command.getDescription().contains("1"));
    }
}
