package com.ecommerce.user.mapper;

import com.ecommerce.user.entity.User;
import com.ecommerce.user.entity.dtos.request.CreateUserRequest;
import com.ecommerce.user.entity.dtos.response.UserResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User createUserRequestToEntity(CreateUserRequest request);
    UserResponse userToResponse(User user);
}

