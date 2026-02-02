package com.estore.gateway.filter;

import com.estore.gateway.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtAuthFilter {

    private final JwtProperties jwtProperties;

    public JwtAuthFilter(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public HandlerFunction<ServerResponse> filter(HandlerFunction<ServerResponse> next) {
        return request -> {
            String authHeader = request.headers().firstHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ServerResponse.status(HttpStatus.UNAUTHORIZED)
                        .bodyValue("{\"error\": \"Missing or invalid Authorization header\"}");
            }

            String token = authHeader.substring(7);

            try {
                Claims claims = validateTokenAndGetClaims(token);

                Long userId = claims.get("userId", Long.class);
                String userEmail = claims.get("userEmail", String.class);
                String role = claims.get("role", String.class);

                // Create a modified request with user info headers
                ServerRequest modifiedRequest = ServerRequest.from(request)
                        .header("X-User-Id", userId != null ? userId.toString() : "")
                        .header("X-User-Email", userEmail != null ? userEmail : "")
                        .header("X-User-Role", role != null ? role : "")
                        .build();

                return next.handle(modifiedRequest);

            } catch (JwtException e) {
                return ServerResponse.status(HttpStatus.UNAUTHORIZED)
                        .bodyValue("{\"error\": \"Invalid or expired token\"}");
            }
        };
    }

    private Claims validateTokenAndGetClaims(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
