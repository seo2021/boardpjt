package com.example.boardpjt.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {
    private final SecretKey secretKey;
    private final Long accessExpiry;
    private final Long refreshExpiry;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiry.access}") Long accessExpiry,
            @Value("${jwt.expiry.refresh}") Long refreshExpiry) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpiry = accessExpiry;
        this.refreshExpiry = refreshExpiry;
        System.out.println("secret = " + secret);
        System.out.println("accessExpiry = " + accessExpiry);
        System.out.println("refreshExpiry = " + refreshExpiry);
    }

    public String generateToken(String username, String role, boolean isRefresh) {
        return Jwts.builder()
                // ----
                .subject(username)
                .claim("role", role)
                // ------
                .issuedAt(new Date())
                // System.currentTimeMillis() -> 현재 시간을 ms로
                .expiration(new Date(System.currentTimeMillis() + (isRefresh ? refreshExpiry : accessExpiry)))
                // ----
                .signWith(secretKey)
                .compact();
    }

    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload(); // 데이터
    }

    public String getUsername(String token) {
        return getClaims(token).getSubject();
    }

    public String getRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    public boolean validateToken(String token) {
        try {
            getClaims(token); // 정상적인 토큰
            return true;
        } catch (Exception e) {
            // Secret, 형식, 만료...
            return false;
        }
    }
}
