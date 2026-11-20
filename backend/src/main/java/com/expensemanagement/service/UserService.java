package com.expensemanagement.service;

import com.expensemanagement.dto.response.PagedResponse;
import com.expensemanagement.dto.response.UserResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {
    PagedResponse<UserResponse> getAllUsers(Pageable pageable);
    List<UserResponse> getActiveManagers();
    UserResponse getUserById(Long id);
    UserResponse updateUserStatus(Long id, boolean active);
    UserResponse assignManager(Long userId, Long managerId);
}
