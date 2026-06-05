package com.clubmanager.controller;

import com.clubmanager.dto.AdminResponse;
import com.clubmanager.mapper.AdminMapper;
import com.clubmanager.service.AdminService;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admins")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final AdminMapper adminMapper;

    public AdminController(AdminService adminService, AdminMapper adminMapper) {
        this.adminService = adminService;
        this.adminMapper = adminMapper;
    }

    @GetMapping
    public List<AdminResponse> getAllAdmins() {
        return adminService.getAllAdmins().stream()
                .map(adminMapper::toResponse)
                .toList();
    }

    @GetMapping("/{uuid}")
    public AdminResponse getAdminByUuid(@PathVariable UUID uuid) {
        return adminMapper.toResponse(adminService.getAdminByUuid(uuid));
    }

    @PatchMapping("/{uuid}/deactivate")
    public AdminResponse deactivateAdmin(@PathVariable UUID uuid) {
        return adminMapper.toResponse(adminService.deactivateAdmin(uuid));
    }

    @PatchMapping("/{uuid}/reactivate")
    public AdminResponse reactivateAdmin(@PathVariable UUID uuid) {
        return adminMapper.toResponse(adminService.reactivateAdmin(uuid));
    }
}
