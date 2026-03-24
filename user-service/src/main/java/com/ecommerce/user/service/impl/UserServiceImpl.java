package com.ecommerce.user.service.impl;

import com.ecommerce.commons.exception.ResourceNotFoundException;
import com.ecommerce.user.entity.Address;
import com.ecommerce.user.entity.User;
import com.ecommerce.user.entity.dtos.request.CreateAddressRequest;
import com.ecommerce.user.entity.dtos.request.CreateUserRequest;
import com.ecommerce.user.entity.dtos.request.UpdateUserRequest;
import com.ecommerce.user.entity.dtos.response.AddressResponse;
import com.ecommerce.user.entity.dtos.response.UserResponse;
import com.ecommerce.user.mapper.AddressMapper;
import com.ecommerce.user.mapper.UserMapper;
import com.ecommerce.user.repository.AddressRepository;
import com.ecommerce.user.repository.UserRepository;
import com.ecommerce.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.ecommerce.user.utils.enums.Role;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository    userRepository;
    private final AddressRepository addressRepository;
    private final UserMapper        userMapper;
    private final AddressMapper     addressMapper;

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        User user = userMapper.createUserRequestToEntity(request);
        user.setRole(Role.valueOf(request.getRole().toUpperCase()));
        user.setActive(true);

        User saved = userRepository.save(user);
        log.info("User created: {}", saved.getId());
        return userMapper.userToResponse(saved);
    }

    @Override
    public UserResponse getUserById(UUID userId) {
        return userMapper.userToResponse(
                userRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId))
        );
    }

    @Override
    public UserResponse getUserByEmail(String email) {
        return userMapper.userToResponse(
                userRepository.findByEmail(email)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email))
        );
    }

    @Override
    public boolean userExists(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional
    public UserResponse updateUser(UUID userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        user.setName(request.getName());
        user.setPhone(request.getPhone());

        User updated = userRepository.save(user);
        log.info("User updated: {}", userId);
        return userMapper.userToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found: " + userId);
        }
        userRepository.deleteById(userId);
        log.info("User deleted: {}", userId);
    }

    @Override
    @Transactional
    public AddressResponse createAddress(UUID userId, CreateAddressRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        Address address = addressMapper.createAddressRequestToEntity(request);
        address.setUser(user);

        Address saved = addressRepository.save(address);
        log.info("Address created: {}", saved.getId());
        return addressMapper.addressToResponse(saved);
    }

    @Override
    public AddressResponse getAddress(UUID addressId) {
        return addressMapper.addressToResponse(
                addressRepository.findById(addressId)
                        .orElseThrow(() -> new ResourceNotFoundException("Address not found: " + addressId))
        );
    }

    @Override
    public List<AddressResponse> getUserAddresses(UUID userId) {
        return addressRepository.findByUserId(userId)
                .stream()
                .map(addressMapper::addressToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteAddress(UUID addressId) {
        if (!addressRepository.existsById(addressId)) {
            throw new ResourceNotFoundException("Address not found: " + addressId);
        }
        addressRepository.deleteById(addressId);
        log.info("Address deleted: {}", addressId);
    }
}
