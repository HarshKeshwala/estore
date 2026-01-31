package com.estore.userservice.user.service.impl;

import com.estore.userservice.user.dto.request.UserUpdateRequest;
import com.estore.userservice.user.dto.response.UserResponse;
import com.estore.userservice.user.entity.User;
import com.estore.userservice.user.repository.UserRepository;
import com.estore.userservice.user.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    public User updateUser(User user, UserUpdateRequest request) {
        User updatedUser = request.updateEntity(user);
        return this.userRepository.save(updatedUser);
    }
}
