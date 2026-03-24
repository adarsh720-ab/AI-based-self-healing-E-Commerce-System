package com.ecommerce.user.controller;

import com.ecommerce.user.entity.dtos.request.UpdateUserRequest;
import com.ecommerce.user.entity.dtos.response.UserResponse;
import com.ecommerce.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request,
            Authentication authentication) {

        UUID loggedInUserId = (UUID) authentication.getPrincipal();
        if (!loggedInUserId.equals(id)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(userService.updateUser(id, request));
    }
}
