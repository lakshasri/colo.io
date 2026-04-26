package com.coloio.srms.controller;

import com.coloio.srms.service.PowerBudgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Power Budget", description = "Zone power budget monitoring")
@RestController
@RequestMapping("/api/power")
public class PowerBudgetController {

    private final PowerBudgetService powerBudgetService;

    public PowerBudgetController(PowerBudgetService powerBudgetService) {
        this.powerBudgetService = powerBudgetService;
    }

    @Operation(summary = "Get power summary for all zones")
    @GetMapping("/zones")
    @PreAuthorize("hasAnyRole('DC_ADMIN','MANAGER','TECHNICIAN')")
    public List<Map<String, Object>> getAllZonesPower() {
        return powerBudgetService.getAllZonesPowerStatus();
    }

    @Operation(summary = "Get power summary for a specific zone")
    @GetMapping("/zones/{zoneId}")
    @PreAuthorize("hasAnyRole('DC_ADMIN','MANAGER','TECHNICIAN')")
    public Map<String, Object> getZonePower(@PathVariable Long zoneId) {
        return powerBudgetService.getZonePowerSummary(zoneId);
    }

    @Operation(summary = "Check if power can be allocated in a zone")
    @GetMapping("/zones/{zoneId}/can-allocate")
    @PreAuthorize("hasAnyRole('DC_ADMIN','MANAGER','TECHNICIAN')")
    public Map<String, Object> canAllocate(@PathVariable Long zoneId,
                                            @RequestParam double kw) {
        boolean ok = powerBudgetService.canAllocatePower(zoneId, kw);
        return Map.of("zoneId", zoneId, "requestedKw", kw, "canAllocate", ok);
    }
}
