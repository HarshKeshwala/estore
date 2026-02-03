package com.estore.userservice.user.controller;

import com.estore.userservice.auth.security.CustomUserDetails;
import com.estore.userservice.user.dto.request.UserUpdateRequest;
import com.estore.userservice.user.dto.response.UserResponse;
import com.estore.userservice.user.entity.User;
import com.estore.userservice.user.mapper.UserMapper;
import com.estore.userservice.user.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.user();
        return ResponseEntity.ok(this.userMapper.toDto(user));
    }

    @PutMapping("me")
    public ResponseEntity<?> update(@AuthenticationPrincipal CustomUserDetails userDetails, @Valid @RequestBody UserUpdateRequest request) {
        // this is authenticated user in context
        User currentUser = userDetails.user();
        // update user from request and save in db
        User updated = this.userService.updateUser(currentUser, request);
        return  ResponseEntity.ok(this.userMapper.toDto(updated));
    }

//    @PostMapping()
//    public ResponseEntity<?> register(@Valid @RequestBody UserRegisterRequest userRegisterRequest) {
//        User user = this.userMapper.toEntity(userRegisterRequest);
//        this.userService.save(user);
//        return ResponseEntity.ok("User registered successfully");
//    }

//    @GetMapping()
//    public ResponseEntity<List<User>> getAll() {
//        List<User> users = this.userService.getAll();
//        return ResponseEntity.ok(users);
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<UserResponse> getUserById(@Positive @PathVariable Long id) {
//
//        User user  = this.userService.getById(id);
//        if  (user == null) {
//            return ResponseEntity.notFound().build();
//        }
//        return ResponseEntity.ok(this.userMapper.toDto(user));
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deleteUserById(@Positive @PathVariable Long id) {
//        this.userService.delete(id);
//        return ResponseEntity.ok().build();
//    }
}
