package com.clubmanager.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.util.matcher.IpAddressMatcher;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class InternalAuditAccessFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(InternalAuditAccessFilter.class);
    private static final String AUDIT_PATH_PREFIX = "/internal/api/v1/audit-events";

    private final AuditInternalApiConfig config;

    public InternalAuditAccessFilter(AuditInternalApiConfig config) {
        this.config = config;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith(AUDIT_PATH_PREFIX);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        if (!isAllowed(request)) {
            LOGGER.warn("Internal audit API rejected clientAddress={}", clientAddress(request));
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Internal audit API is not available");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean isAllowed(HttpServletRequest request) {
        if (!config.enabled() || config.allowedCidrs().isEmpty()) {
            return false;
        }
        String clientAddress = clientAddress(request);
        return config.allowedCidrs().stream()
                .filter(StringUtils::hasText)
                .map(IpAddressMatcher::new)
                .anyMatch(matcher -> matcher.matches(clientAddress));
    }

    private String clientAddress(HttpServletRequest request) {
        if (StringUtils.hasText(config.trustedProxyHeader())) {
            String header = request.getHeader(config.trustedProxyHeader());
            if (StringUtils.hasText(header)) {
                return header.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }
}
