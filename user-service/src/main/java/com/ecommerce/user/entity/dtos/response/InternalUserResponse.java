package com.ecommerce.user.entity.dtos.response;

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
public class InternalUserResponse {
    private UUID id;
    private String name;
    private String email;
    private String passwordHash;
    private String phone;
    private String role;
    private boolean active;
    private LocalDateTime createdAt;
}

