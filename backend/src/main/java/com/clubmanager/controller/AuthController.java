package com.clubmanager.controller;

import com.clubmanager.config.JwtService;
import com.clubmanager.domain.Admin;
import com.clubmanager.dto.AdminRegisterRequest;
import com.clubmanager.dto.AdminResponse;
import com.clubmanager.dto.LoginRequest;
import com.clubmanager.dto.LoginResponse;
import com.clubmanager.mapper.AdminMapper;
import com.clubmanager.service.AdminService;
import jakarta.validation.Valid;
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

    private final AdminService adminService;
    private final AdminMapper adminMapper;
    private final JwtService jwtService;

    public AuthController(AdminService adminService, AdminMapper adminMapper, JwtService jwtService) {
        this.adminService = adminService;
        this.adminMapper = adminMapper;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        Admin admin = adminService.authenticate(request);
        return new LoginResponse(jwtService.generateToken(admin), admin.getUuid(), admin.getName());
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public AdminResponse register(@Valid @RequestBody AdminRegisterRequest request) {
        return adminMapper.toResponse(adminService.register(request));
    }
}

