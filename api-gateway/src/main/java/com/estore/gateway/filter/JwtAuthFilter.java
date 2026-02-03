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

    public static final String USER_ID_ATTR = "X-User-Id";
    public static final String USER_EMAIL_ATTR = "X-User-Email";
    public static final String USER_ROLE_ATTR = "X-User-Role";

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

                // Use request attributes instead of headers to pass user info to ProxyHandler
                ServerRequest modifiedRequest = ServerRequest.from(request)
                        .attribute(USER_ID_ATTR, userId != null ? userId.toString() : "")
                        .attribute(USER_EMAIL_ATTR, userEmail != null ? userEmail : "")
                        .attribute(USER_ROLE_ATTR, role != null ? role : "")
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
