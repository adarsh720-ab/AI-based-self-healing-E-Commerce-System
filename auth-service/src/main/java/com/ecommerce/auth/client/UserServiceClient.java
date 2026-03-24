package com.ecommerce.auth.client;


import com.ecommerce.auth.entity.dtos.request.CreateUserRequest;
import com.ecommerce.auth.entity.dtos.response.UserServiceResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

// ================================================================
// USER SERVICE CLIENT
//
// auth-service uses this to talk to user-service over HTTP.
// OpenFeign generates the actual HTTP implementation at runtime —
// you just define the interface.
//
// url = the address of user-service when running locally.
// In production this would be a service discovery name.
// ================================================================
@FeignClient(name = "user-service", url = "${services.user-service.url:http://localhost:8081}")
public interface UserServiceClient {

    @PostMapping("/internal/users")
    UserServiceResponse createUser(@RequestBody CreateUserRequest request);

    @GetMapping("/internal/users/by-email")
    Optional<UserServiceResponse> findByEmail(@RequestParam("email") String email);

    @GetMapping("/internal/users/exists")
    boolean existsByEmail(@RequestParam("email") String email);
}
