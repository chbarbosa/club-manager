package com.clubmanager.config;

import com.clubmanager.service.AppMetricsService;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.Customizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties({JwtConfig.class, AuditInternalApiConfig.class, AppSecurityConfig.class, CorsConfigProperties.class})
public class SecurityConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityConfig.class);

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final InternalAuditAccessFilter internalAuditAccessFilter;
    private final AppMetricsService appMetricsService;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            InternalAuditAccessFilter internalAuditAccessFilter,
            AppMetricsService appMetricsService) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.internalAuditAccessFilter = internalAuditAccessFilter;
        this.appMetricsService = appMetricsService;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) -> {
                            appMetricsService.recordAccessDenied();
                            LOGGER.warn("Authentication required path={}", request.getRequestURI());
                            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
                        })
                        .accessDeniedHandler((request, response, exception) -> {
                            appMetricsService.recordAccessDenied();
                            LOGGER.warn("Access denied path={}", request.getRequestURI());
                            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
                        }))
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/club").permitAll()
                        .requestMatchers("/h2-console/**", "/actuator/health", "/actuator/health/**").permitAll()
                        .anyRequest().hasRole("ADMIN")
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(internalAuditAccessFilter, JwtAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
