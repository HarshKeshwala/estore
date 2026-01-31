package com.estore.userservice.user.dto.request;

import com.estore.userservice.user.entity.User;
import jakarta.validation.constraints.NotBlank;

public record UserUpdateRequest(
        @NotBlank
        String firstName,
        @NotBlank
        String lastName
) {
        public User updateEntity(User user) {
                user.setFirstName(this.firstName);
                user.setLastName(this.lastName);
                return user;
        }
}
