package com.coloio.srms.controller;

import com.coloio.srms.entity.ChecklistItemEntity;
import com.coloio.srms.entity.MaintenanceTicketEntity;
import com.coloio.srms.pattern.command.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.coloio.srms.repository.RackRepository;
import com.coloio.srms.repository.ServerRepository;
import com.coloio.srms.service.MaintenanceService;
import com.coloio.srms.service.ServerService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Tag(name = "Maintenance", description = "Maintenance lifecycle, checklist, and command undo/redo")
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

    // --- Ticket CRUD ---

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
    public MaintenanceTicketEntity schedule(@RequestBody Map<String, String> body) {
        LocalDateTime scheduledAt = body.containsKey("scheduledAt")
                ? LocalDateTime.parse(body.get("scheduledAt")) : LocalDateTime.now().plusDays(1);
        return maintenanceService.scheduleTicket(
                Long.parseLong(body.get("serverId")),
                body.get("title"), body.get("description"),
                body.getOrDefault("priority", "MEDIUM"), scheduledAt);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('DC_ADMIN','TECHNICIAN')")
    public MaintenanceTicketEntity updateStatus(@PathVariable Long id,
                                                 @RequestBody Map<String, String> body) {
        return maintenanceService.updateStatus(id, body.get("status"), body.get("assignedTo"));
    }

    // --- Lifecycle commands ---

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('DC_ADMIN','TECHNICIAN')")
    public MaintenanceTicketEntity assign(@PathVariable Long id,
                                          @RequestBody Map<String, String> body) {
        return maintenanceService.assignTechnician(id, Long.parseLong(body.get("technicianId")));
    }

    @Operation(summary = "Transition ticket to IN_PROGRESS")
    @PostMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('DC_ADMIN','TECHNICIAN')")
    public MaintenanceTicketEntity start(@PathVariable Long id) {
        return maintenanceService.startTicket(id);
    }

    @Operation(summary = "Mark ticket as RESOLVED")
    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('DC_ADMIN','TECHNICIAN')")
    public MaintenanceTicketEntity complete(@PathVariable Long id) {
        return maintenanceService.completeTicket(id);
    }

    @Operation(summary = "Cancel ticket with reason")
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('DC_ADMIN','TECHNICIAN')")
    public MaintenanceTicketEntity cancel(@PathVariable Long id,
                                           @RequestBody Map<String, String> body) {
        return maintenanceService.cancelTicket(id, body.getOrDefault("reason", "No reason given"));
    }

    @Operation(summary = "Manager approval for a ticket")
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('MANAGER')")
    public MaintenanceTicketEntity approve(@PathVariable Long id) {
        return maintenanceService.approveTicket(id);
    }

    // --- Checklist ---

    @GetMapping("/{id}/checklist")
    public List<ChecklistItemEntity> getChecklist(@PathVariable Long id) {
        return maintenanceService.getChecklist(id);
    }

    @PostMapping("/{id}/checklist")
    @PreAuthorize("hasAnyRole('DC_ADMIN','TECHNICIAN')")
    public ChecklistItemEntity addItem(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return maintenanceService.addChecklistItem(id, body.get("description"));
    }

    @PatchMapping("/checklist/{itemId}/tick")
    @PreAuthorize("hasAnyRole('DC_ADMIN','TECHNICIAN')")
    public ChecklistItemEntity tickItem(@PathVariable Long itemId,
                                         @RequestBody Map<String, String> body) {
        return maintenanceService.tickChecklistItem(itemId, body.getOrDefault("completedBy", "unknown"));
    }

    // --- Server command endpoints (with undo/redo) ---

    @PostMapping("/commands/allocate")
    @PreAuthorize("hasAnyRole('DC_ADMIN','TECHNICIAN')")
    public ResponseEntity<Map<String, Object>> allocate(@RequestBody Map<String, String> body) {
        commandInvoker.execute(new AllocateServerCommand(
                serverService, serverRepository, rackRepository,
                Long.parseLong(body.get("serverId")), body.get("strategy")));
        return ResponseEntity.ok(Map.of("message", "allocated", "canUndo", commandInvoker.canUndo()));
    }

    @PostMapping("/commands/move")
    @PreAuthorize("hasAnyRole('DC_ADMIN','TECHNICIAN')")
    public ResponseEntity<Map<String, Object>> move(@RequestBody Map<String, String> body) {
        commandInvoker.execute(new MoveServerCommand(
                serverRepository, rackRepository,
                Long.parseLong(body.get("serverId")),
                Long.parseLong(body.get("targetRackId"))));
        return ResponseEntity.ok(Map.of("message", "moved", "canUndo", commandInvoker.canUndo()));
    }

    @PostMapping("/commands/deallocate")
    @PreAuthorize("hasAnyRole('DC_ADMIN','TECHNICIAN')")
    public ResponseEntity<Map<String, Object>> deallocate(@RequestBody Map<String, String> body) {
        commandInvoker.execute(new DeallocateServerCommand(
                serverRepository, rackRepository,
                Long.parseLong(body.get("serverId"))));
        return ResponseEntity.ok(Map.of("message", "deallocated", "canUndo", commandInvoker.canUndo()));
    }

    @PostMapping("/commands/undo")
    @PreAuthorize("hasAnyRole('DC_ADMIN','TECHNICIAN')")
    public ResponseEntity<Map<String, Object>> undo() {
        boolean result = commandInvoker.undo();
        return ResponseEntity.ok(Map.of("undone", result,
                "canUndo", commandInvoker.canUndo(), "canRedo", commandInvoker.canRedo()));
    }

    @PostMapping("/commands/redo")
    @PreAuthorize("hasAnyRole('DC_ADMIN','TECHNICIAN')")
    public ResponseEntity<Map<String, Object>> redo() {
        boolean result = commandInvoker.redo();
        return ResponseEntity.ok(Map.of("redone", result,
                "canUndo", commandInvoker.canUndo(), "canRedo", commandInvoker.canRedo()));
    }

    @GetMapping("/commands/history")
    public ResponseEntity<Map<String, Object>> history() {
        return ResponseEntity.ok(Map.of(
                "history", commandInvoker.getHistory(),
                "canUndo", commandInvoker.canUndo(),
                "canRedo", commandInvoker.canRedo()));
    }
}
