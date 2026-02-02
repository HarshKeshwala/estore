package com.estore.productservice.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/products/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/products/**").access((authentication, context) ->
                                new AuthorizationDecision(isAdmin(context.getRequest())))
                        .requestMatchers(HttpMethod.PUT, "/products/**").access((authentication, context) ->
                                new AuthorizationDecision(isAdmin(context.getRequest())))
                        .requestMatchers(HttpMethod.DELETE, "/products/**").access((authentication, context) ->
                                new AuthorizationDecision(isAdmin(context.getRequest())))
                        .anyRequest().denyAll()
                );

        return http.build();
    }

    private boolean isAdmin(HttpServletRequest request) {
        String role = request.getHeader("X-User-Role");
        return "ADMIN".equals(role);
    }
}
