package com.expensemanagement.service.impl;

import com.expensemanagement.dto.response.PagedResponse;
import com.expensemanagement.dto.response.UserResponse;
import com.expensemanagement.entity.User;
import com.expensemanagement.enums.AuditAction;
import com.expensemanagement.enums.RoleName;
import com.expensemanagement.exception.BadRequestException;
import com.expensemanagement.exception.ResourceNotFoundException;
import com.expensemanagement.repository.UserRepository;
import com.expensemanagement.service.AuditLogService;
import com.expensemanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<UserResponse> getAllUsers(Pageable pageable) {
        Page<User> page = userRepository.findAll(pageable);
        return PagedResponse.fromPage(page.map(this::mapToUserResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getActiveManagers() {
        return userRepository.findActiveUsersByRoleName(RoleName.ROLE_MANAGER).stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return mapToUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUserStatus(Long id, boolean active) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        user.setActive(active);
        User saved = userRepository.save(user);

        auditLogService.log(
                AuditAction.USER_STATUS_CHANGE,
                "User",
                saved.getId(),
                "Changed user status to: " + (active ? "ACTIVE" : "INACTIVE")
        );

        return mapToUserResponse(saved);
    }

    @Override
    @Transactional
    public UserResponse assignManager(Long userId, Long managerId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (managerId != null) {
            if (userId.equals(managerId)) {
                throw new BadRequestException("An employee cannot be assigned as their own manager");
            }

            User manager = userRepository.findById(managerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Manager", "id", managerId));

            boolean isManagerRole = manager.getRoles().stream()
                    .anyMatch(r -> r.getName() == RoleName.ROLE_MANAGER || r.getName() == RoleName.ROLE_ADMIN);

            if (!isManagerRole) {
                throw new BadRequestException("Assigned user does not hold a Manager or Admin role");
            }

            user.setManager(manager);
        } else {
            user.setManager(null);
        }

        User saved = userRepository.save(user);

        auditLogService.log(
                AuditAction.MANAGER_ASSIGN,
                "User",
                saved.getId(),
                "Assigned manager ID: " + managerId + " to employee: " + saved.getEmail()
        );

        return mapToUserResponse(saved);
    }

    private UserResponse mapToUserResponse(User user) {
        Set<String> roleNames = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .department(user.getDepartment())
                .jobTitle(user.getJobTitle())
                .managerId(user.getManager() != null ? user.getManager().getId() : null)
                .managerName(user.getManager() != null ? user.getManager().getFullName() : null)
                .active(user.isActive())
                .roles(roleNames)
                .createdAt(user.getCreatedAt())
                .build();
    }
}
