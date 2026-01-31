package com.estore.userservice.auth.dto.response;

public record AuthResponse(
        String authToken,
        String refreshToken
) {}
