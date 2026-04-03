package com.ecommerce.user.service;

import com.ecommerce.user.entity.dtos.request.CreateUserRequest;
import com.ecommerce.user.entity.dtos.response.InternalUserResponse;

import java.util.Optional;

public interface InternalUserService {
    InternalUserResponse createUser(CreateUserRequest request);
    Optional<InternalUserResponse> findByEmail(String email);
    boolean existsByEmail(String email);
}
