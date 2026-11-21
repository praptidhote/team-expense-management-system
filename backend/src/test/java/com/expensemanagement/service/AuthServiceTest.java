package com.expensemanagement.service;

import com.expensemanagement.dto.request.LoginRequest;
import com.expensemanagement.dto.request.RegisterRequest;
import com.expensemanagement.dto.response.AuthResponse;
import com.expensemanagement.entity.Role;
import com.expensemanagement.entity.User;
import com.expensemanagement.enums.RoleName;
import com.expensemanagement.exception.BadRequestException;
import com.expensemanagement.repository.RoleRepository;
import com.expensemanagement.repository.UserRepository;
import com.expensemanagement.security.JwtTokenProvider;
import com.expensemanagement.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User sampleUser;
    private Role employeeRole;

    @BeforeEach
    void setUp() {
        employeeRole = Role.builder().id(1L).name(RoleName.ROLE_EMPLOYEE).build();

        sampleUser = User.builder()
                .id(1L)
                .email("test@company.com")
                .password("encoded_pass")
                .firstName("John")
                .lastName("Doe")
                .roles(Set.of(employeeRole))
                .active(true)
                .build();
    }

    @Test
    @DisplayName("Should successfully authenticate user and return JWT token")
    void login_Success() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@company.com");
        request.setPassword("Password123!");

        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(tokenProvider.generateToken(auth)).thenReturn("mock.jwt.token");
        when(userRepository.findByEmail("test@company.com")).thenReturn(Optional.of(sampleUser));

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mock.jwt.token", response.getToken());
        assertEquals("test@company.com", response.getEmail());
        assertTrue(response.getRoles().contains("ROLE_EMPLOYEE"));
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("Should throw BadRequestException if user is deactivated during login")
    void login_DeactivatedUser_ThrowsException() {
        sampleUser.setActive(false);
        LoginRequest request = new LoginRequest();
        request.setEmail("test@company.com");
        request.setPassword("Password123!");

        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(userRepository.findByEmail("test@company.com")).thenReturn(Optional.of(sampleUser));

        assertThrows(BadRequestException.class, () -> authService.login(request));
    }

    @Test
    @DisplayName("Should register new employee successfully")
    void register_Success() {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Jane");
        request.setLastName("Smith");
        request.setEmail("jane.smith@company.com");
        request.setPassword("Password123!");
        request.setDepartment("Finance");

        when(userRepository.existsByEmail("jane.smith@company.com")).thenReturn(false);
        when(roleRepository.findByName(RoleName.ROLE_EMPLOYEE)).thenReturn(Optional.of(employeeRole));
        when(passwordEncoder.encode("Password123!")).thenReturn("encoded_pass");
        when(userRepository.saveAndFlush(any(User.class))).thenReturn(sampleUser);

        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(tokenProvider.generateToken(auth)).thenReturn("mock.jwt.token");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("mock.jwt.token", response.getToken());
        verify(userRepository).saveAndFlush(any(User.class));
    }

    @Test
    @DisplayName("Should throw BadRequestException on duplicate email registration")
    void register_DuplicateEmail_ThrowsException() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@company.com");

        when(userRepository.existsByEmail("existing@company.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }
}
