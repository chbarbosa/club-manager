package com.clubmanager.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class CorsConfig implements WebMvcConfigurer {

    private final CorsConfigProperties corsConfigProperties;



    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(corsConfigProperties.allowedOrigins().toArray(String[]::new))
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
    }
}
