package com.ecommerce.auth.service;

import com.ecommerce.auth.entity.dtos.request.LoginRequest;
import com.ecommerce.auth.entity.dtos.request.RefreshRequest;
import com.ecommerce.auth.entity.dtos.request.RegisterRequest;
import com.ecommerce.auth.entity.dtos.response.AuthResponse;
import com.ecommerce.auth.entity.dtos.response.ValidateResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    void logout(String token);
    AuthResponse refresh(RefreshRequest request);
    ValidateResponse validate(String token);
}
