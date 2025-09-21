package com.chatify.chat_backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    /**
     * Base64-encoded secret string. Set this in application.yml as:
     * jwt.secret: ${JWT_SECRET}
     * and export JWT_SECRET in your environment.
     * Must be at least 256 bits (32 bytes) before Base64 encoding.
     */
    @Value("${jwt.secret}")
    private String base64Secret;

    /**
     * Token lifetime in milliseconds.
     * Example in application.yml:
     * jwt.expiration-ms: 3600000   # 1 hour
     */
    @Value("${jwt.expiration-ms:3600000}") // default to 1 hour if not set
    private long tokenLifetimeMs;

    /**
     * The signing key object derived from the Base64 secret.
     * Built once after properties are injected.
     */
    private Key signingKey;

    /**
     * Initialize signingKey after Spring injects properties.
     * Decodes the Base64 string and creates a proper HMAC key.
     * Keys.hmacShaKeyFor() will throw if the key is too short.
     */
    @PostConstruct
    public void initializeSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(base64Secret);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generate a JWT for a given username with default claims.
     * @param username the subject of the token
     * @return signed JWT as compact string
     */
    public String generateToken(String username) {
        return generateToken(Map.of(), username);
    }

    /**
     * Generate a JWT with custom claims and subject.
     * @param extraClaims additional key/value pairs to include in token body
     * @param subject     typically the username or user id
     * @return signed JWT as compact string
     */
    public String generateToken(Map<String, Object> extraClaims, String subject) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(subject)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + tokenLifetimeMs))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extract the username (subject) from a token.
     * @param token JWT string
     * @return subject claim (username/email/etc.)
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }


    /**
     * Validate token by checking signature and expiration only.
     * @param token JWT string
     * @return true if valid and not expired
     */
    public boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    /**
     * Validate token by checking signature, expiration, and matching subject.
     * @param token        JWT string
     * @param expectedUser username we expect to see
     * @return true if valid and not expired
     */
    public boolean isTokenValid(String token, String expectedUser) {
        try {
            final String username = extractUsername(token);
            return username.equals(expectedUser) && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException ex) {
            // signature invalid, malformed, or other parsing error
            return false;
        }
    }



    /** ----------- Internal helpers ----------- */

    private boolean isTokenExpired(String token) {
        Date expiration = extractClaim(token, Claims::getExpiration);
        return expiration.before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = parseClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Parse token and return all claims. Throws JwtException if signature is invalid.
     */
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
