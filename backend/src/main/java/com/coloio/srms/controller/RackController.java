package com.coloio.srms.controller;

import com.coloio.srms.domain.enums.RackStatus;
import com.coloio.srms.domain.rack.Rack;
import com.coloio.srms.service.RackService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Racks", description = "Rack inventory and utilization management")
@RestController
@RequestMapping("/api/racks")
public class RackController {

    private final RackService rackService;

    public RackController(RackService rackService) {
        this.rackService = rackService;
    }

    @PostMapping
    @PreAuthorize("hasRole('DC_ADMIN')")
    public ResponseEntity<Rack> createRack(@RequestBody Rack rack) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rackService.createRack(rack));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DC_ADMIN','TECHNICIAN','MANAGER')")
    public ResponseEntity<List<Rack>> getAllRacks(@RequestParam(required = false) Long zoneId) {
        List<Rack> racks = (zoneId != null)
                ? rackService.getRacksByZone(zoneId)
                : rackService.getAllRacks();
        return ResponseEntity.ok(racks);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DC_ADMIN','TECHNICIAN','MANAGER')")
    public ResponseEntity<Rack> getRack(@PathVariable Long id) {
        return ResponseEntity.ok(rackService.getRack(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DC_ADMIN')")
    public ResponseEntity<Rack> updateRack(@PathVariable Long id, @RequestBody Rack rack) {
        return ResponseEntity.ok(rackService.updateRack(id, rack));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('DC_ADMIN')")
    public ResponseEntity<Void> changeStatus(@PathVariable Long id,
                                             @RequestParam RackStatus status) {
        rackService.changeStatus(id, status);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('DC_ADMIN')")
    public ResponseEntity<Void> deleteRack(@PathVariable Long id) {
        rackService.deleteRack(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/utilization")
    @PreAuthorize("hasAnyRole('DC_ADMIN','MANAGER')")
    public ResponseEntity<Map<String, Object>> getUtilization(@PathVariable Long id) {
        return ResponseEntity.ok(rackService.getUtilization(id));
    }
}
