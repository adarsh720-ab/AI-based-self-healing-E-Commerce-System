package com.ecommerce.auth.entity.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserServiceResponse {
    private UUID id;
    private String        name;
    private String        email;
    private String        passwordHash;  // needed so auth-service can verify BCrypt
    private String        phone;
    private String        role;
    private boolean       active;
    private LocalDateTime createdAt;
}
