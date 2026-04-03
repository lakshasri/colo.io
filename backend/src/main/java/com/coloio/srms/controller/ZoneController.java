package com.coloio.srms.controller;

import com.coloio.srms.domain.zone.Zone;
import com.coloio.srms.service.ZoneService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/zones")
public class ZoneController {

    private final ZoneService zoneService;

    public ZoneController(ZoneService zoneService) {
        this.zoneService = zoneService;
    }

    @PostMapping
    @PreAuthorize("hasRole('DC_ADMIN')")
    public ResponseEntity<Zone> createZone(@RequestBody Zone zone) {
        return ResponseEntity.status(HttpStatus.CREATED).body(zoneService.createZone(zone));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DC_ADMIN','MANAGER','TECHNICIAN')")
    public ResponseEntity<List<Zone>> getAllZones() {
        return ResponseEntity.ok(zoneService.getAllZones());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DC_ADMIN','MANAGER','TECHNICIAN')")
    public ResponseEntity<Zone> getZone(@PathVariable Long id) {
        return ResponseEntity.ok(zoneService.getZone(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DC_ADMIN')")
    public ResponseEntity<Zone> updateZone(@PathVariable Long id, @RequestBody Zone zone) {
        return ResponseEntity.ok(zoneService.updateZone(id, zone));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('DC_ADMIN')")
    public ResponseEntity<Void> deleteZone(@PathVariable Long id) {
        zoneService.deleteZone(id);
        return ResponseEntity.noContent().build();
    }
}
