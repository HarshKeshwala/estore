package com.estore.userservice.user.service;

import com.estore.userservice.user.dto.request.UserUpdateRequest;
import com.estore.userservice.user.dto.response.UserResponse;
import com.estore.userservice.user.entity.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;

public interface UserService {
    User updateUser(User user, UserUpdateRequest userUpdateRequest);
}
