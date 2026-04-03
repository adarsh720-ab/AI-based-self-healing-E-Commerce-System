package com.ecommerce.user.service.impl;

import com.ecommerce.commons.exception.ConflictException;
import com.ecommerce.user.entity.User;
import com.ecommerce.user.entity.dtos.request.CreateUserRequest;
import com.ecommerce.user.entity.dtos.response.InternalUserResponse;
import com.ecommerce.user.repository.UserRepository;
import com.ecommerce.user.service.InternalUserService;
import com.ecommerce.user.utils.enums.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InternalUserServiceImpl implements InternalUserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public InternalUserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already exists: " + request.getEmail());
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(request.getPasswordHash())
                .phone(request.getPhone())
                .role(Role.valueOf(request.getRole().toUpperCase()))
                .isActive(true)
                .build();

        User saved = userRepository.save(user);
        log.info("User created internally: {}", saved.getEmail());
        return toInternalResponse(saved);
    }

    @Override
    public Optional<InternalUserResponse> findByEmail(String email) {
        return userRepository.findByEmail(email).map(this::toInternalResponse);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    private InternalUserResponse toInternalResponse(User user) {
        return InternalUserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .passwordHash(user.getPasswordHash())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
