package com.estore.userservice.user.dto.request;

public record UserLoginRequest(
        String email,
        String password
) {}
