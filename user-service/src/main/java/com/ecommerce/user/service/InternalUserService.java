package com.ecommerce.user.service;

import com.ecommerce.user.entity.dtos.request.CreateUserRequest;
import com.ecommerce.user.entity.dtos.response.UserResponse;

import java.util.Optional;

public interface InternalUserService {
    UserResponse createUser(CreateUserRequest request);
    Optional<UserResponse> findByEmail(String email);
    boolean existsByEmail(String email);
}
