package com.expensemanagement.service;

import com.expensemanagement.dto.request.LoginRequest;
import com.expensemanagement.dto.request.RegisterRequest;
import com.expensemanagement.dto.response.AuthResponse;
import com.expensemanagement.dto.response.UserResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse register(RegisterRequest request);
    UserResponse getCurrentUserProfile();
}
