package com.kikoba.kikoba_backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
@Slf4j
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshTokenExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(UUID userId, String phoneNumber) {
        return buildToken(Map.of("phone", phoneNumber, "type", "access"), userId, accessTokenExpiration);
    }

    public String generateRefreshToken(UUID userId) {
        return buildToken(Map.of("type", "refresh"), userId, refreshTokenExpiration);
    }

    private String buildToken(Map<String, Object> claims, UUID userId, long expiration) {
        return Jwts.builder()
                .claims(claims)
                .subject(userId.toString())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    public UUID extractUserId(String token) {
        String subject = extractClaim(token, Claims::getSubject);
        return UUID.fromString(subject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenValid(String token, UUID userId) {
        try {
            final UUID extractedId = extractUserId(token);
            return extractedId.equals(userId) && !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    public long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    /**
     * Generates a short-lived token (5 minutes) used to gate MFA verification.
     * This token can ONLY be used to complete the login step — it cannot access
     * any other endpoint because it has purpose=MFA_PENDING.
     */
    public String generateMfaSessionToken(UUID userId) {
        return buildToken(
                Map.of("purpose", "MFA_PENDING", "type", "mfa"),
                userId,
                5 * 60 * 1000L   // 5 minutes
        );
    }

    /**
     * Extracts the user ID from an MFA session token.
     * Returns null if the token is invalid, expired, or has the wrong purpose.
     */
    public UUID extractUserIdFromMfaToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            String purpose = claims.get("purpose", String.class);
            if (!"MFA_PENDING".equals(purpose)) {
                return null;
            }
            return UUID.fromString(claims.getSubject());
        } catch (Exception e) {
            log.warn("Invalid MFA session token: {}", e.getMessage());
            return null;
        }
    }
}