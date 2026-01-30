package com.estore.user.dto.request;

public record UserLoginRequest(
        String email,
        String password
) {}
