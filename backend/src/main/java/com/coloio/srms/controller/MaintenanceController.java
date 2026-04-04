package com.coloio.srms.controller;

import com.coloio.srms.entity.MaintenanceTicketEntity;
import com.coloio.srms.pattern.command.*;
import com.coloio.srms.repository.RackRepository;
import com.coloio.srms.repository.ServerRepository;
import com.coloio.srms.service.MaintenanceService;
import com.coloio.srms.service.ServerService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/maintenance")
public class MaintenanceController {

    private final MaintenanceService maintenanceService;
    private final CommandInvoker commandInvoker;
    private final ServerService serverService;
    private final ServerRepository serverRepository;
    private final RackRepository rackRepository;

    public MaintenanceController(MaintenanceService maintenanceService,
                                  CommandInvoker commandInvoker,
                                  ServerService serverService,
                                  ServerRepository serverRepository,
                                  RackRepository rackRepository) {
        this.maintenanceService = maintenanceService;
        this.commandInvoker = commandInvoker;
        this.serverService = serverService;
        this.serverRepository = serverRepository;
        this.rackRepository = rackRepository;
    }

    @GetMapping
    public List<MaintenanceTicketEntity> getAllTickets() {
        return maintenanceService.getAllTickets();
    }

    @GetMapping("/{id}")
    public MaintenanceTicketEntity getTicket(@PathVariable Long id) {
        return maintenanceService.getTicket(id);
    }

    @GetMapping("/server/{serverId}")
    public List<MaintenanceTicketEntity> getByServer(@PathVariable Long serverId) {
        return maintenanceService.getTicketsByServer(serverId);
    }

    @GetMapping("/status/{status}")
    public List<MaintenanceTicketEntity> getByStatus(@PathVariable String status) {
        return maintenanceService.getTicketsByStatus(status);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('DC_ADMIN','TECHNICIAN')")
    public MaintenanceTicketEntity createTicket(@RequestBody Map<String, String> body) {
        return maintenanceService.createTicket(
                Long.parseLong(body.get("serverId")),
                body.get("title"),
                body.get("description"),
                body.get("priority")
        );
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('DC_ADMIN','TECHNICIAN')")
    public MaintenanceTicketEntity updateStatus(@PathVariable Long id,
                                                 @RequestBody Map<String, String> body) {
        return maintenanceService.updateStatus(id, body.get("status"), body.get("assignedTo"));
    }

    // Command pattern: allocate via invoker (supports undo)
    @PostMapping("/commands/allocate")
    @PreAuthorize("hasAnyRole('DC_ADMIN','TECHNICIAN')")
    public ResponseEntity<Map<String, Object>> allocate(@RequestBody Map<String, String> body) {
        AllocateServerCommand cmd = new AllocateServerCommand(
                serverService, serverRepository, rackRepository,
                Long.parseLong(body.get("serverId")),
                body.get("strategy")
        );
        commandInvoker.execute(cmd);
        return ResponseEntity.ok(Map.of("message", "allocated", "canUndo", commandInvoker.canUndo()));
    }

    // Command pattern: move server
    @PostMapping("/commands/move")
    @PreAuthorize("hasAnyRole('DC_ADMIN','TECHNICIAN')")
    public ResponseEntity<Map<String, Object>> move(@RequestBody Map<String, String> body) {
        MoveServerCommand cmd = new MoveServerCommand(
                serverRepository, rackRepository,
                Long.parseLong(body.get("serverId")),
                Long.parseLong(body.get("targetRackId"))
        );
        commandInvoker.execute(cmd);
        return ResponseEntity.ok(Map.of("message", "moved", "canUndo", commandInvoker.canUndo()));
    }

    // Command pattern: deallocate
    @PostMapping("/commands/deallocate")
    @PreAuthorize("hasAnyRole('DC_ADMIN','TECHNICIAN')")
    public ResponseEntity<Map<String, Object>> deallocate(@RequestBody Map<String, String> body) {
        DeallocateServerCommand cmd = new DeallocateServerCommand(
                serverRepository, rackRepository,
                Long.parseLong(body.get("serverId"))
        );
        commandInvoker.execute(cmd);
        return ResponseEntity.ok(Map.of("message", "deallocated", "canUndo", commandInvoker.canUndo()));
    }

    @PostMapping("/commands/undo")
    @PreAuthorize("hasAnyRole('DC_ADMIN','TECHNICIAN')")
    public ResponseEntity<Map<String, Object>> undo() {
        boolean result = commandInvoker.undo();
        return ResponseEntity.ok(Map.of("undone", result, "canUndo", commandInvoker.canUndo(),
                "canRedo", commandInvoker.canRedo()));
    }

    @PostMapping("/commands/redo")
    @PreAuthorize("hasAnyRole('DC_ADMIN','TECHNICIAN')")
    public ResponseEntity<Map<String, Object>> redo() {
        boolean result = commandInvoker.redo();
        return ResponseEntity.ok(Map.of("redone", result, "canUndo", commandInvoker.canUndo(),
                "canRedo", commandInvoker.canRedo()));
    }

    @GetMapping("/commands/history")
    public ResponseEntity<Map<String, Object>> history() {
        return ResponseEntity.ok(Map.of(
                "history", commandInvoker.getHistory(),
                "canUndo", commandInvoker.canUndo(),
                "canRedo", commandInvoker.canRedo()
        ));
    }
}
