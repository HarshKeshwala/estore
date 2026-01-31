package com.estore.userservice.user.mapper;

import com.estore.userservice.user.dto.response.UserResponse;
import com.estore.userservice.user.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    //User toEntity(UserRegisterRequest userRegisterRequest);
    UserResponse toDto(User user);
}
