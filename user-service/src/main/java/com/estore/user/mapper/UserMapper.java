package com.estore.user.mapper;

import com.estore.user.dto.request.UserRegisterRequest;
import com.estore.user.dto.response.UserResponse;
import com.estore.user.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toEntity(UserRegisterRequest userRegisterRequest);
    UserResponse toDto(User user);
}
