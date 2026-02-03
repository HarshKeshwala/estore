package com.estore.userservice.auth.security;


import com.estore.userservice.auth.service.JwtService;
import com.estore.userservice.user.entity.User;
import com.estore.userservice.user.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain
                                    ) throws ServletException, IOException {

        // get Authorization header
        final String authHeader = request.getHeader("Authorization");

        // check if header exists and starts with Bearer
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // extract JWT token
        final String jwt = authHeader.substring(7);
        final String username;

        try {
            // extract username from JWT
            username = jwtService.extractUsername(jwt);

            // if username exists and user is not already authenticated
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // load user details
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                // validate token
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    // Create authentication token
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    // set additional details
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // update security context
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // log the exception
            logger.error("JWT authentication failed: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
