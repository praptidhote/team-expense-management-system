package com.expensemanagement.service;

import com.expensemanagement.dto.response.UserResponse;
import com.expensemanagement.entity.Role;
import com.expensemanagement.entity.User;
import com.expensemanagement.enums.RoleName;
import com.expensemanagement.exception.BadRequestException;
import com.expensemanagement.repository.UserRepository;
import com.expensemanagement.service.impl.UserServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("Should prevent assigning employee as their own manager")
    void assignManager_SelfAssignment_ThrowsBadRequest() {
        User user = User.builder().id(5L).email("user@company.com").build();
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class, () -> userService.assignManager(5L, 5L));
    }

    @Test
    @DisplayName("Should prevent assigning user who does not have ROLE_MANAGER or ROLE_ADMIN")
    void assignManager_NonManagerRole_ThrowsBadRequest() {
        User employee = User.builder().id(5L).email("emp@company.com").build();
        Role employeeRole = Role.builder().name(RoleName.ROLE_EMPLOYEE).build();
        User candidate = User.builder().id(8L).email("candidate@company.com").roles(Set.of(employeeRole)).build();

        when(userRepository.findById(5L)).thenReturn(Optional.of(employee));
        when(userRepository.findById(8L)).thenReturn(Optional.of(candidate));

        assertThrows(BadRequestException.class, () -> userService.assignManager(5L, 8L));
    }

    @Test
    @DisplayName("Should successfully assign qualified manager")
    void assignManager_Success() {
        User employee = User.builder().id(5L).email("emp@company.com").roles(Set.of()).build();
        Role managerRole = Role.builder().name(RoleName.ROLE_MANAGER).build();
        User manager = User.builder().id(8L).email("mgr@company.com").firstName("Jordan").lastName("Lee").roles(Set.of(managerRole)).build();

        when(userRepository.findById(5L)).thenReturn(Optional.of(employee));
        when(userRepository.findById(8L)).thenReturn(Optional.of(manager));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        UserResponse response = userService.assignManager(5L, 8L);

        assertNotNull(response);
        assertEquals(8L, response.getManagerId());
        verify(userRepository).save(any(User.class));
    }
}
