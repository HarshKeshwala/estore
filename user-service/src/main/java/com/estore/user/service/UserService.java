package com.estore.user.service;

import com.estore.user.dto.request.UserRegisterRequest;
import com.estore.user.entity.User;
import java.util.List;

public interface UserService {
    boolean save(UserRegisterRequest userRequest);
    User getById(Long id);
    User getByEmail(String email);
    List<User> getAll();
    boolean delete(Long id);
}
