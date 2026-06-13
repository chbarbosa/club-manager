package com.clubmanager.controller;

import com.clubmanager.config.JwtService;
import com.clubmanager.domain.Admin;
import com.clubmanager.dto.AdminRegisterRequest;
import com.clubmanager.dto.AdminResponse;
import com.clubmanager.dto.LoginRequest;
import com.clubmanager.dto.LoginResponse;
import com.clubmanager.mapper.AdminMapper;
import com.clubmanager.service.AdminService;
import com.clubmanager.service.AppMetricsService;
import com.clubmanager.service.AuditEventService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    private final AdminService adminService;
    private final AdminMapper adminMapper;
    private final JwtService jwtService;
    private final AuditEventService auditEventService;
    private final AppMetricsService appMetricsService;

    public AuthController(
            AdminService adminService,
            AdminMapper adminMapper,
            JwtService jwtService,
            AuditEventService auditEventService,
            AppMetricsService appMetricsService) {
        this.adminService = adminService;
        this.adminMapper = adminMapper;
        this.jwtService = jwtService;
        this.auditEventService = auditEventService;
        this.appMetricsService = appMetricsService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        Admin admin = adminService.authenticate(request, clientAddress(httpRequest));
        appMetricsService.recordLoginSuccess();
        LOGGER.info("Admin login succeeded for username={}", admin.getUsername());
        return new LoginResponse(jwtService.generateToken(admin), admin.getUuid(), admin.getName());
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public AdminResponse register(@Valid @RequestBody AdminRegisterRequest request) {
        Admin admin = adminService.register(request);
        auditEventService.record(
                AuditEventService.CREATED,
                AuditEventService.ADMIN,
                admin.getUuid(),
                admin.getUsername(),
                "Admin registered: " + admin.getUsername());
        return adminMapper.toResponse(admin);
    }

    private String clientAddress(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
