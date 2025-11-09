package com.Food.FoodOrdering.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Key;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class JwtService {

    // fallback default only for quick local testing — replace with env/properties in prod
    private static final String DEFAULT_SECRET_BASE64 =
            "bXlTdXBlclNlY3JldEtleU15U3VwZXJTZWNyZXRLZXlNeVN1cGVyU2VjcmV0S2V5"; // keep safe

    private final Key signingKey;
    private final long expirationMs;

    public JwtService(
            @Value("${jwt.secret:" + DEFAULT_SECRET_BASE64 + "}") String base64Secret,
            @Value("${jwt.expiration-ms:3600000}") long expirationMs) {
        // base64Secret is expected to be a base64-encoded random byte sequence (>= 256 bits recommended)
        byte[] keyBytes = Base64.getDecoder().decode(base64Secret);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = expirationMs;
    }

    /**
     * Generate a token that includes username (subject) and roles claim.
     * roles: a collection of role strings, e.g. ["ROLE_USER","ROLE_ADMIN"]
     */
    public String generateToken(String username, Collection<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles);

        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Convenience overload when you only have a single role or don't need to pass roles.
     */
    public String generateToken(String username) {
        return generateToken(username, Collections.emptyList());
    }

    /* --- claim extraction helpers --- */

    public String extractUserName(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Extract roles stored in the token as a list of strings.
     * Returns empty list if roles claim is missing.
     */
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        Object rolesObj = claims.get("roles");
        if (rolesObj instanceof Collection<?>) {
            return ((Collection<?>) rolesObj).stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /* --- validation --- */

    public boolean isTokenExpired(String token) {
        try {
            Date exp = extractExpiration(token);
            return exp.before(new Date());
        } catch (Exception ex) {
            return true; // if parsing fails treat token as expired/invalid
        }
    }

    /**
     * Validate token against UserDetails (username match + not expired).
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUserName(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * Validate token integrity + expiration (without a UserDetails lookup).
     */
    public boolean validateToken(String token) {
        try {
            extractAllClaims(token); // will throw if invalid signature / malformed / expired
            return !isTokenExpired(token);
        } catch (Exception ex) {
            return false;
        }
    }
}

