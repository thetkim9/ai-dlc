package com.tableorder.security;

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

    private final SecretKey key;
    private final long expiryHours;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiry-hours}") long expiryHours) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiryHours = expiryHours;
    }

    public String generateAdminToken(Long adminId, Long storeId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiryHours * 3600_000L);
        return Jwts.builder()
                .claim("adminId", adminId)
                .claim("storeId", storeId)
                .claim("role", "ADMIN")
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public String generateTableToken(Long tableId, Long sessionId, Long storeId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiryHours * 3600_000L);
        return Jwts.builder()
                .claim("tableId", tableId)
                .claim("sessionId", sessionId)
                .claim("storeId", storeId)
                .claim("role", "TABLE")
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            throw com.tableorder.common.exception.ApiException.unauthorized("유효하지 않은 토큰입니다.");
        }
    }

    protected SecretKey getKey() {
        return key;
    }

    protected long getExpiryHours() {
        return expiryHours;
    }
}
