package com.coloio.srms.controller;

import com.coloio.srms.service.ReportService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

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
}
