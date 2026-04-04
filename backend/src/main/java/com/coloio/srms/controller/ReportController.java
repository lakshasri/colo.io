package com.coloio.srms.controller;

import com.coloio.srms.service.ReportService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

@Tag(name = "Reports", description = "Capacity, utilization, and maintenance history reports")
@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasAnyRole('DC_ADMIN','MANAGER')")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/capacity")
    public Map<String, Object> capacity() {
        return reportService.capacityReport();
    }

    @GetMapping("/utilization")
    public List<Map<String, Object>> utilization() {
        return reportService.serverUtilizationReport();
    }

    @GetMapping("/maintenance-history")
    public Map<String, Object> maintenanceHistory() {
        return reportService.maintenanceHistoryReport();
    }

    @GetMapping("/utilization/export")
    public void exportUtilizationCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=utilization.csv");
        PrintWriter writer = response.getWriter();
        writer.println("serverId,hostname,status,avgCpuPct,avgRamPct,avgDiskPct");
        for (Map<String, Object> row : reportService.serverUtilizationReport()) {
            writer.printf("%s,%s,%s,%s,%s,%s%n",
                    row.get("serverId"), row.get("hostname"), row.get("status"),
                    row.get("avgCpuPct"), row.get("avgRamPct"), row.get("avgDiskPct"));
        }
        writer.flush();
    }

    @GetMapping("/capacity/export")
    public void exportCapacityCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=capacity.csv");
        PrintWriter writer = response.getWriter();
        writer.println("zoneId,zoneName,rackCount,totalUSpace,usedUSpace,uUtilizationPct,totalPowerKw,usedPowerKw,powerUtilizationPct");
        Map<String, Object> report = reportService.capacityReport();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> zones = (List<Map<String, Object>>) report.get("zones");
        for (Map<String, Object> zone : zones) {
            writer.printf("%s,%s,%s,%s,%s,%s,%s,%s,%s%n",
                    zone.get("zoneId"), zone.get("zoneName"), zone.get("rackCount"),
                    zone.get("totalUSpace"), zone.get("usedUSpace"), zone.get("uUtilizationPct"),
                    zone.get("totalPowerKw"), zone.get("usedPowerKw"), zone.get("powerUtilizationPct"));
        }
        writer.flush();
    }
}
