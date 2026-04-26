package com.coloio.srms.controller;

import com.coloio.srms.entity.AuditLogEntity;
import com.coloio.srms.repository.AuditLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Audit", description = "Paginated audit trail (DC Admin only)")
@RestController
@RequestMapping("/api/audit")
@PreAuthorize("hasRole('DC_ADMIN')")
public class AuditController {

    private final AuditLogRepository auditLogRepository;

    public AuditController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Operation(summary = "Paginated audit log, newest first")
    @GetMapping
    public Page<AuditLogEntity> getAuditLog(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return auditLogRepository.findAllByOrderByTimestampDesc(PageRequest.of(page, size));
    }

    @GetMapping("/entity/{type}/{id}")
    public java.util.List<AuditLogEntity> getByEntity(@PathVariable String type,
                                                        @PathVariable Long id) {
        return auditLogRepository.findByEntityTypeAndEntityId(type, id);
    }
}
