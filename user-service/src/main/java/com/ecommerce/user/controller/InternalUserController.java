package com.ecommerce.user.controller;

import com.ecommerce.user.entity.dtos.request.CreateUserRequest;
import com.ecommerce.user.entity.dtos.response.UserResponse;
import com.ecommerce.user.service.InternalUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

// Called ONLY by auth-service via Feign. Blocked at API Gateway for external clients.
@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final InternalUserService internalUserService;

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(internalUserService.createUser(request));
    }

    @GetMapping("/by-email")
    public ResponseEntity<UserResponse> findByEmail(@RequestParam String email) {
        Optional<UserResponse> user = internalUserService.findByEmail(email);
        return user.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> existsByEmail(@RequestParam String email) {
        return ResponseEntity.ok(internalUserService.existsByEmail(email));
    }
}
