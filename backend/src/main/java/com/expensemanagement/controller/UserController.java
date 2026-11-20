package com.expensemanagement.controller;

import com.expensemanagement.dto.request.AssignManagerRequest;
import com.expensemanagement.dto.request.UserStatusUpdateRequest;
import com.expensemanagement.dto.response.ApiResponse;
import com.expensemanagement.dto.response.PagedResponse;
import com.expensemanagement.dto.response.UserResponse;
import com.expensemanagement.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> getAllUsers(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PagedResponse<UserResponse> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(ApiResponse.ok(users));
    }

    @GetMapping("/managers")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getActiveManagers() {
        List<UserResponse> managers = userService.getActiveManagers();
        return ResponseEntity.ok(ApiResponse.ok(managers));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.ok(user));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody UserStatusUpdateRequest request
    ) {
        UserResponse response = userService.updateUserStatus(id, request.getActive());
        return ResponseEntity.ok(ApiResponse.ok(response, "User status updated successfully"));
    }

    @PutMapping("/{id}/assign-manager")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> assignManager(
            @PathVariable Long id,
            @RequestBody AssignManagerRequest request
    ) {
        UserResponse response = userService.assignManager(id, request.getManagerId());
        return ResponseEntity.ok(ApiResponse.ok(response, "Manager assigned successfully"));
    }
}
