package com.clubmanager.config;

import com.clubmanager.repository.AdminRepository;
import com.clubmanager.repository.TrainerRepository;
import com.clubmanager.service.UserLoginService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AdminRepository adminRepository;
    private final TrainerRepository trainerRepository;



    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.substring(7);
        try {
            String username = jwtService.extractUsername(token);
            String role = jwtService.extractRole(token);
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                if (UserLoginService.ROLE_ADMIN.equals(role)) {
                    adminRepository.findByUsername(username)
                            .filter(admin -> admin.isActive())
                            .ifPresent(admin -> authenticate(admin.getUsername(), role));
                } else if (UserLoginService.ROLE_TRAINER.equals(role)) {
                    trainerRepository.findByEmailIgnoreCase(username)
                            .filter(trainer -> trainer.isActive())
                            .ifPresent(trainer -> authenticate(trainer.getEmail(), role));
                }
            }
        } catch (RuntimeException ignored) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private void authenticate(String principal, String role) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
