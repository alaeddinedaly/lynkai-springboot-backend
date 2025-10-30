package com.lynkai.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private SecretKey secretKey;

    // Move these to class-level fields
    @Getter
    private final long accessTokenValidityMs = 15L * 60L * 1000L; // 15 minutes
    @Getter
    private final long refreshTokenValidityMs = 30L * 24 * 60 * 60 * 1000L; // 30 days

    @PostConstruct
    public void init() {
        byte[] keyBytes = Base64.getDecoder().decode(jwtSecret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    private String generateToken(String userId, String type, long expiryMs) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiryMs);

        return Jwts.builder()
                .setSubject(userId)
                .claim("type", type)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateAccessToken(String userId) {
        return generateToken(userId, "access", accessTokenValidityMs);
    }

    public String generateRefreshToken(String userId) {
        return generateToken(userId, "refresh", refreshTokenValidityMs);
    }

    public boolean validateAccessToken(String token) {
        Claims claims = parseAllClaims(token);
        if (claims == null) return false;
        String tokenType = claims.get("type", String.class);
        return "access".equals(tokenType);
    }

    public boolean validateRefreshToken(String token) {
        Claims claims = parseAllClaims(token);
        if (claims == null) return false;
        String tokenType = claims.get("type", String.class);
        return "refresh".equals(tokenType);
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = parseAllClaims(token);
        if (claims == null) throw new IllegalArgumentException("Invalid Token");
        return Long.parseLong(claims.getSubject());
    }

    private Claims parseAllClaims(String token) {
        String rawToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(rawToken)
                    .getBody();
        } catch (Exception e) {
            return null;
        }
    }

}

