package com.ecommerce.user.service.impl;

import com.ecommerce.commons.exception.ConflictException;
import com.ecommerce.user.entity.User;
import com.ecommerce.user.entity.dtos.request.CreateUserRequest;
import com.ecommerce.user.entity.dtos.response.UserResponse;
import com.ecommerce.user.mapper.UserMapper;
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
    private final UserMapper     userMapper;

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
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
        return userMapper.userToResponse(saved);
    }

    @Override
    public Optional<UserResponse> findByEmail(String email) {
        return userRepository.findByEmail(email).map(userMapper::userToResponse);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
