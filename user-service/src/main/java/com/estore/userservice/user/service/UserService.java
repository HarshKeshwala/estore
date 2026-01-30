package com.estore.userservice.user.service;

import com.estore.userservice.user.entity.User;
import java.util.List;

public interface UserService {
    boolean save(User user);
    User getById(Long id);
    User getByEmail(String email);
    List<User> getAll();
    boolean delete(Long id);
}
