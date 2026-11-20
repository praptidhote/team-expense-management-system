package com.expensemanagement.service;

import com.expensemanagement.dto.request.ApprovalDecisionRequest;
import com.expensemanagement.dto.response.ExpenseResponse;
import com.expensemanagement.entity.Expense;
import com.expensemanagement.entity.ExpenseCategory;
import com.expensemanagement.entity.Role;
import com.expensemanagement.entity.User;
import com.expensemanagement.enums.ApprovalDecision;
import com.expensemanagement.enums.ExpenseStatus;
import com.expensemanagement.enums.PaymentMethod;
import com.expensemanagement.enums.RoleName;
import com.expensemanagement.exception.BadRequestException;
import com.expensemanagement.exception.ForbiddenException;
import com.expensemanagement.repository.ExpenseApprovalRepository;
import com.expensemanagement.repository.ExpenseRepository;
import com.expensemanagement.repository.ExpenseStatusHistoryRepository;
import com.expensemanagement.repository.UserRepository;
import com.expensemanagement.security.UserPrincipal;
import com.expensemanagement.service.impl.ApprovalServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApprovalServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private ExpenseApprovalRepository approvalRepository;

    @Mock
    private ExpenseStatusHistoryRepository statusHistoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private ApprovalServiceImpl approvalService;

    private User employee;
    private User manager;
    private Expense pendingExpense;

    @BeforeEach
    void setUp() {
        Role managerRole = Role.builder().id(2L).name(RoleName.ROLE_MANAGER).build();
        Role employeeRole = Role.builder().id(1L).name(RoleName.ROLE_EMPLOYEE).build();

        manager = User.builder()
                .id(20L)
                .email("manager@company.com")
                .firstName("Jordan")
                .lastName("Lee")
                .roles(Set.of(managerRole))
                .active(true)
                .build();

        employee = User.builder()
                .id(10L)
                .email("employee@company.com")
                .firstName("Taylor")
                .lastName("Reed")
                .manager(manager)
                .roles(Set.of(employeeRole))
                .active(true)
                .build();

        ExpenseCategory category = ExpenseCategory.builder().id(1L).name("Travel").active(true).build();

        pendingExpense = Expense.builder()
                .id(100L)
                .user(employee)
                .category(category)
                .title("Flight to NYC")
                .description("Client demonstration")
                .amount(new BigDecimal("450.00"))
                .currency("USD")
                .expenseDate(LocalDate.now().minusDays(2))
                .paymentMethod(PaymentMethod.COMPANY_CARD)
                .status(ExpenseStatus.PENDING)
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(User user, String roleName) {
        UserPrincipal principal = new UserPrincipal(
                user.getId(),
                user.getEmail(),
                "pass",
                user.getFirstName(),
                user.getLastName(),
                true,
                List.of(new SimpleGrantedAuthority(roleName))
        );
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(token);
    }

    @Test
    @DisplayName("Manager should successfully approve pending expense of assigned report")
    void reviewExpense_Approve_Success() {
        authenticateAs(manager, "ROLE_MANAGER");

        when(userRepository.findById(20L)).thenReturn(Optional.of(manager));
        when(expenseRepository.findById(100L)).thenReturn(Optional.of(pendingExpense));
        when(expenseRepository.save(any(Expense.class))).thenAnswer(i -> i.getArgument(0));

        ApprovalDecisionRequest request = new ApprovalDecisionRequest();
        request.setDecision(ApprovalDecision.APPROVED);
        request.setComments("Receipt verified, approved");

        ExpenseResponse response = approvalService.reviewExpense(100L, request);

        assertNotNull(response);
        assertEquals(ExpenseStatus.APPROVED, response.getStatus());
        verify(approvalRepository).save(any());
        verify(statusHistoryRepository).save(any());
        verify(notificationService).createNotification(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Manager rejecting expense must provide comments or throw BadRequestException")
    void reviewExpense_RejectWithoutComments_ThrowsBadRequest() {
        authenticateAs(manager, "ROLE_MANAGER");

        when(userRepository.findById(20L)).thenReturn(Optional.of(manager));
        when(expenseRepository.findById(100L)).thenReturn(Optional.of(pendingExpense));

        ApprovalDecisionRequest request = new ApprovalDecisionRequest();
        request.setDecision(ApprovalDecision.REJECTED);
        request.setComments(""); // Blank comments

        assertThrows(BadRequestException.class, () -> approvalService.reviewExpense(100L, request));
    }

    @Test
    @DisplayName("Segregation of duties: Submitter cannot approve their own expense")
    void reviewExpense_SelfApproval_ThrowsForbidden() {
        authenticateAs(employee, "ROLE_EMPLOYEE");

        when(userRepository.findById(10L)).thenReturn(Optional.of(employee));
        when(expenseRepository.findById(100L)).thenReturn(Optional.of(pendingExpense));

        ApprovalDecisionRequest request = new ApprovalDecisionRequest();
        request.setDecision(ApprovalDecision.APPROVED);

        assertThrows(ForbiddenException.class, () -> approvalService.reviewExpense(100L, request));
    }

    @Test
    @DisplayName("Unrelated manager cannot approve another manager's report")
    void reviewExpense_UnassignedManager_ThrowsForbidden() {
        User otherManager = User.builder().id(99L).email("other@company.com").build();
        authenticateAs(otherManager, "ROLE_MANAGER");

        when(userRepository.findById(99L)).thenReturn(Optional.of(otherManager));
        when(expenseRepository.findById(100L)).thenReturn(Optional.of(pendingExpense));

        ApprovalDecisionRequest request = new ApprovalDecisionRequest();
        request.setDecision(ApprovalDecision.APPROVED);

        assertThrows(ForbiddenException.class, () -> approvalService.reviewExpense(100L, request));
    }
}
