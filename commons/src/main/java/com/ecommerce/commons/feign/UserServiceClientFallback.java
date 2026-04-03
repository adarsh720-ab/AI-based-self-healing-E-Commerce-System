package com.ecommerce.commons.feign;

class UserServiceClientFallback implements UserServiceClient {
    @Override
    public UserResponse createUser(CreateUserRequest request) {
        return null;
    }

    @Override
    public UserResponse getUserByEmail(String email) {
        return null;
    }

    @Override
    public boolean userExists(String email) {
        return false;
    }
}
