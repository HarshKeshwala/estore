package com.estore.userservice.user.dto.request;

import com.estore.userservice.user.dto.request.validator.UniqueEmail;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserRegisterRequest(
       @NotBlank(message = "email is required")
       @Email (message = "provide valid email address")
       @UniqueEmail
       String email,

       @NotBlank (message = "password is required")
       String password,

       @NotBlank (message = "first name is required")
       String firstName,

       @NotBlank (message = "last name is required")
       String lastName
) {}
