package com.coloio.srms.controller;

import com.coloio.srms.entity.SlaEntity;
import com.coloio.srms.service.SlaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Tag(name = "SLA", description = "Customer SLA agreement management")
@RestController
@RequestMapping("/api/sla")
public class SlaController {

    private final SlaService slaService;

    public SlaController(SlaService slaService) {
        this.slaService = slaService;
    }

    @Operation(summary = "List all SLA agreements")
    @GetMapping
    @PreAuthorize("hasAnyRole('DC_ADMIN','MANAGER')")
    public List<SlaEntity> getAll() {
        return slaService.getAll();
    }

    @Operation(summary = "Get SLA by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DC_ADMIN','MANAGER','CUSTOMER')")
    public SlaEntity getById(@PathVariable Long id) {
        return slaService.getById(id);
    }

    @Operation(summary = "Get SLAs for a specific customer")
    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('DC_ADMIN','MANAGER','CUSTOMER')")
    public List<SlaEntity> getByCustomer(@PathVariable Long customerId) {
        return slaService.getByCustomer(customerId);
    }

    @Operation(summary = "Create a new SLA agreement")
    @PostMapping
    @PreAuthorize("hasAnyRole('DC_ADMIN','MANAGER')")
    public SlaEntity create(@RequestBody Map<String, Object> body) {
        return slaService.create(
                Long.parseLong(body.get("customerId").toString()),
                body.get("name").toString(),
                body.getOrDefault("description", "").toString(),
                Double.parseDouble(body.get("uptimeGuaranteePct").toString()),
                Integer.parseInt(body.get("responseTimeMinutes").toString()),
                Integer.parseInt(body.get("resolutionTimeHours").toString()),
                LocalDate.parse(body.get("startDate").toString()),
                body.containsKey("endDate") ? LocalDate.parse(body.get("endDate").toString()) : null
        );
    }

    @Operation(summary = "Update SLA status")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('DC_ADMIN','MANAGER')")
    public SlaEntity updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return slaService.updateStatus(id, body.get("status"));
    }

    @Operation(summary = "Delete an SLA agreement")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('DC_ADMIN')")
    public void delete(@PathVariable Long id) {
        slaService.delete(id);
    }
}
