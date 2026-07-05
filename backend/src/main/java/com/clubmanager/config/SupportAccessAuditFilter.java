package com.clubmanager.config;

import com.clubmanager.service.SupportAccessViewAuditService;
import com.clubmanager.service.UserLoginService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class SupportAccessAuditFilter extends OncePerRequestFilter {

    private final SupportAccessViewAuditService supportAccessViewAuditService;
    private final SupportAccessConfig supportAccessConfig;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        filterChain.doFilter(request, response);
        if (!supportAccessConfig.enabled()) {
            return;
        }
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !"GET".equals(request.getMethod()) || response.getStatus() >= 400) {
            return;
        }
        boolean support = authentication.getAuthorities().stream()
                .anyMatch(authority -> ("ROLE_" + UserLoginService.ROLE_SUPPORT).equals(authority.getAuthority()));
        if (support && request.getRequestURI().startsWith("/api/v1/")) {
            supportAccessViewAuditService.recordView(authentication.getName(), request.getMethod(), request.getRequestURI());
        }
    }
}
