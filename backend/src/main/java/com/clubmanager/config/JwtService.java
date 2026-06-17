package com.clubmanager.config;

import com.clubmanager.domain.Admin;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtConfig jwtConfig;



    public String generateToken(Admin admin) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(admin.getUsername())
                .claim("uuid", admin.getUuid().toString())
                .claim("role", "ADMIN")
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusMillis(jwtConfig.expirationMs())))
                .signWith(signingKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        parseClaims(token);
        return true;
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key signingKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtConfig.secret());
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

