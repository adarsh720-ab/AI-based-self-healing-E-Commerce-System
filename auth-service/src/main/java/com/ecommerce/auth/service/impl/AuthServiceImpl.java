package com.ecommerce.auth.service.impl;

import com.ecommerce.auth.client.UserServiceClient;
import com.ecommerce.auth.entity.dtos.request.CreateUserRequest;
import com.ecommerce.auth.entity.dtos.request.LoginRequest;
import com.ecommerce.auth.entity.dtos.request.RefreshRequest;
import com.ecommerce.auth.entity.dtos.request.RegisterRequest;
import com.ecommerce.auth.entity.dtos.response.AuthResponse;
import com.ecommerce.auth.entity.dtos.response.UserServiceResponse;
import com.ecommerce.auth.entity.dtos.response.ValidateResponse;
import com.ecommerce.auth.service.AuthService;
import com.ecommerce.commons.exception.ConflictException;
import com.ecommerce.commons.exception.UnauthorizedException;
import com.ecommerce.commons.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserServiceClient   userServiceClient;
    private final JwtUtil             jwtUtil;
    private final PasswordEncoder     passwordEncoder;
    private final StringRedisTemplate redis;

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userServiceClient.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already registered: " + request.getEmail());
        }

        CreateUserRequest createReq = CreateUserRequest.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role("CUSTOMER")
                .build();

        UserServiceResponse user = userServiceClient.createUser(createReq);

        String accessToken  = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        redis.opsForValue().set(
                "refresh:" + user.getId(), refreshToken, 7, TimeUnit.DAYS);

        log.info("User registered: {}", user.getEmail());
        return buildAuthResponse(user, accessToken, refreshToken);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        UserServiceResponse user = userServiceClient.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        if (!user.isActive()) {
            throw new UnauthorizedException("Account is deactivated");
        }

        String accessToken  = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        redis.opsForValue().set(
                "refresh:" + user.getId(), refreshToken, 7, TimeUnit.DAYS);

        log.info("User logged in: {}", user.getEmail());
        return buildAuthResponse(user, accessToken, refreshToken);
    }

    @Override
    public void logout(String token) {
        String jti       = jwtUtil.getJti(token);
        UUID   userId    = jwtUtil.getUserId(token);
        long   expiryMs  = jwtUtil.parseToken(token).getExpiration().getTime()
                - System.currentTimeMillis();

        redis.opsForValue().set(
                "token:blacklist:" + jti, "revoked",
                Math.max(expiryMs, 0), TimeUnit.MILLISECONDS);

        redis.delete("refresh:" + userId);
        log.info("Token blacklisted for user: {}", userId);
    }

    @Override
    public AuthResponse refresh(RefreshRequest request) {
        if (!jwtUtil.isValid(request.getRefreshToken())) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        UUID   userId      = jwtUtil.getUserId(request.getRefreshToken());
        String storedToken = redis.opsForValue().get("refresh:" + userId);

        if (!request.getRefreshToken().equals(storedToken)) {
            throw new UnauthorizedException("Refresh token has been invalidated");
        }

        String email       = jwtUtil.getEmail(request.getRefreshToken());
        String role        = jwtUtil.getRole(request.getRefreshToken());
        String accessToken = jwtUtil.generateToken(userId, email, role);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(86400)
                .build();
    }

    @Override
    public ValidateResponse validate(String token) {
        if (!jwtUtil.isValid(token)) {
            return ValidateResponse.builder().valid(false).build();
        }

        String  jti           = jwtUtil.getJti(token);
        boolean isBlacklisted = Boolean.TRUE.equals(redis.hasKey("token:blacklist:" + jti));

        if (isBlacklisted) {
            return ValidateResponse.builder().valid(false).build();
        }

        return ValidateResponse.builder()
                .valid(true)
                .userId(jwtUtil.getUserId(token))
                .email(jwtUtil.getEmail(token))
                .role(jwtUtil.getRole(token))
                .build();
    }

    // ── private helper ───────────────────────────────────────────────────────

    private AuthResponse buildAuthResponse(UserServiceResponse user,
                                           String accessToken,
                                           String refreshToken) {
        return AuthResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(86400)
                .refreshExpiresIn(604800)
                .build();
    }
}