package com.clubmanager.controller;

import com.clubmanager.dto.AdminResponse;
import com.clubmanager.mapper.AdminMapper;
import com.clubmanager.service.AdminService;
import com.clubmanager.service.AuditEventService;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admins")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final AdminMapper adminMapper;
    private final AuditEventService auditEventService;

    public AdminController(AdminService adminService, AdminMapper adminMapper, AuditEventService auditEventService) {
        this.adminService = adminService;
        this.adminMapper = adminMapper;
        this.auditEventService = auditEventService;
    }

    @GetMapping
    public List<AdminResponse> getAllAdmins(@RequestParam(required = false) Boolean active) {
        return (Boolean.TRUE.equals(active) ? adminService.getActiveAdmins() : adminService.getAllAdmins()).stream()
                .map(adminMapper::toResponse)
                .toList();
    }

    @GetMapping("/{uuid}")
    public AdminResponse getAdminByUuid(@PathVariable UUID uuid) {
        return adminMapper.toResponse(adminService.getAdminByUuid(uuid));
    }

    @PatchMapping("/{uuid}/deactivate")
    public AdminResponse deactivateAdmin(@PathVariable UUID uuid) {
        var admin = adminService.deactivateAdmin(uuid);
        auditEventService.record(
                AuditEventService.DEACTIVATED,
                AuditEventService.ADMIN,
                admin.getUuid(),
                admin.getUsername(),
                "Admin deactivated: " + admin.getUsername());
        return adminMapper.toResponse(admin);
    }

    @PatchMapping("/{uuid}/reactivate")
    public AdminResponse reactivateAdmin(@PathVariable UUID uuid) {
        var admin = adminService.reactivateAdmin(uuid);
        auditEventService.record(
                AuditEventService.REACTIVATED,
                AuditEventService.ADMIN,
                admin.getUuid(),
                admin.getUsername(),
                "Admin reactivated: " + admin.getUsername());
        return adminMapper.toResponse(admin);
    }
}
