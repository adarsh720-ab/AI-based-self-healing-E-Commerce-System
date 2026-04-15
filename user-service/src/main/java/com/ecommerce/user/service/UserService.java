package com.ecommerce.user.service;

import com.ecommerce.user.entity.dtos.request.CreateAddressRequest;
import com.ecommerce.user.entity.dtos.request.CreateUserRequest;
import com.ecommerce.user.entity.dtos.request.UpdateUserRequest;
import com.ecommerce.user.entity.dtos.response.AddressResponse;
import com.ecommerce.user.entity.dtos.response.UserResponse;

import java.util.List;
import java.util.UUID;

public interface UserService {
    UserResponse createUser(CreateUserRequest request);
    UserResponse getUserById(UUID userId);
    UserResponse getUserByEmail(String email);
    boolean userExists(String email);
    UserResponse updateUser(UUID userId, UpdateUserRequest request);
    void deleteUser(UUID userId);
    AddressResponse createAddress(UUID userId, CreateAddressRequest request);
    AddressResponse getAddress(UUID addressId);
    List<AddressResponse> getUserAddresses(UUID userId);
    void deleteAddress(UUID addressId);
}