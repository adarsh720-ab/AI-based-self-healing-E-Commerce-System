package com.ecommerce.commons.feign;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(
        name = "user-service",
        url = "http://user-service:8081",
        fallback = UserServiceClientFallback.class
)
public interface UserServiceClient {

    @PostMapping("/internal/users")
    UserResponse createUser(@RequestBody CreateUserRequest request);

    @GetMapping("/internal/users/by-email")
    UserResponse getUserByEmail(@RequestParam String email);

    @GetMapping("/internal/users/exists")
    boolean userExists(@RequestParam String email);

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    class CreateUserRequest {
        private String name;
        private String email;
        private String passwordHash;
        private String phone;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    class UserResponse {
        private UUID id;
        private String name;
        private String email;
        private String passwordHash;
        private String phone;
        private String role;
        private boolean isActive;
    }
}

