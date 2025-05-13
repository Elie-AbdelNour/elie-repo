package com.RecipeManagement.Recipe.Security.JWT;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import jakarta.annotation.PostConstruct;

import javax.crypto.SecretKey;

@Component
public class JwtUtils {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${recipe.app.jwtSecret:mySecretKey12345678901234567890123456789012}") // Must be 256-bit key
    private String jwtSecret;

    @Value("${recipe.app.jwtExpirationMs:86400000}") // 1 day
    private int jwtExpirationMs;

    private Key signingKey;

    @PostConstruct
    public void init() {
        // HMAC-SHA-256 requires a 256-bit (32-byte) key minimum
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    // Generate JWT token using the authentication object
    public String generateJwtToken(Authentication authentication) {
        String username = authentication.getName();

        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(signingKey)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith((SecretKey) signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.getSubject();
        } catch (Exception e) {
            logger.error("Error parsing JWT token: {}", e.getMessage());
            return null;
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith((SecretKey) signingKey).build().parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            logger.warn("JWT expired: {}", e.getMessage());
        } catch (JwtException e) {
            logger.warn("JWT invalid: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.warn("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }
}