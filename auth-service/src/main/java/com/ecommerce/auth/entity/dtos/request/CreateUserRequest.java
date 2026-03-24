package com.ecommerce.auth.entity.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {
    private String name;
    private String email;
    private String passwordHash;  // already BCrypt-hashed by auth-service
    private String phone;
    private String role;          // always "CUSTOMER" on register
}
