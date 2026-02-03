package com.estore.userservice.auth.service;

import com.estore.userservice.auth.config.JwtProperties;
import com.estore.userservice.auth.security.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    private final JwtProperties jwtProperties;

    public  JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    // get signing key from secret
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // generate token with user details
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        if (userDetails instanceof CustomUserDetails customUserDetails) {
            claims.put("userId", customUserDetails.user().getId());
            claims.put("userEmail", customUserDetails.user().getEmail());
            claims.put("role", customUserDetails.user().getRole().name());
        } else {
            claims.put("userEmail", userDetails.getUsername());
        }
        return generateToken(claims, userDetails);
    }

    // generate token with extra claims
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtProperties.getExpiration());
    }

    // generate refresh token with user details
    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, jwtProperties.getRefreshExpiration());
    }

    // common method to build token with provided claims and user details
    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    // validate token against provided user and if token has expired
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    // check if token has expired or not
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // extract all claims from token
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // common method to extract specific claim from token
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // extract username(subject) from token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // extract expiration date from token
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}