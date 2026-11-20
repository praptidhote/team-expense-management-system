package com.expensemanagement.service;

import com.expensemanagement.dto.request.ExpenseCreateRequest;
import com.expensemanagement.dto.request.ExpenseUpdateRequest;
import com.expensemanagement.dto.response.ExpenseResponse;
import com.expensemanagement.entity.Expense;
import com.expensemanagement.entity.ExpenseCategory;
import com.expensemanagement.entity.User;
import com.expensemanagement.enums.ExpenseStatus;
import com.expensemanagement.enums.PaymentMethod;
import com.expensemanagement.exception.BadRequestException;
import com.expensemanagement.exception.ForbiddenException;
import com.expensemanagement.repository.ExpenseCategoryRepository;
import com.expensemanagement.repository.ExpenseRepository;
import com.expensemanagement.repository.ExpenseStatusHistoryRepository;
import com.expensemanagement.repository.UserRepository;
import com.expensemanagement.security.UserPrincipal;
import com.expensemanagement.service.impl.ExpenseServiceImpl;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ExpenseCategoryRepository categoryRepository;

    @Mock
    private ExpenseStatusHistoryRepository statusHistoryRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private ExpenseServiceImpl expenseService;

    private User employee;
    private ExpenseCategory activeCategory;

    @BeforeEach
    void setUp() {
        employee = User.builder()
                .id(1L)
                .email("emp@company.com")
                .firstName("John")
                .lastName("Doe")
                .active(true)
                .build();

        activeCategory = ExpenseCategory.builder()
                .id(5L)
                .name("Office Supplies")
                .active(true)
                .build();

        UserPrincipal principal = new UserPrincipal(
                1L,
                "emp@company.com",
                "pass",
                "John",
                "Doe",
                true,
                List.of(new SimpleGrantedAuthority("ROLE_EMPLOYEE"))
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should successfully create expense with valid parameters")
    void createExpense_Success() {
        ExpenseCreateRequest request = new ExpenseCreateRequest();
        request.setTitle("Ergonomic Keyboard");
        request.setDescription("Replaced damaged keyboard");
        request.setCategoryId(5L);
        request.setAmount(new BigDecimal("129.99"));
        request.setCurrency("USD");
        request.setExpenseDate(LocalDate.now().minusDays(1));
        request.setPaymentMethod(PaymentMethod.PERSONAL_CREDIT_CARD);

        when(userRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(categoryRepository.findById(5L)).thenReturn(Optional.of(activeCategory));

        Expense savedExpense = Expense.builder()
                .id(10L)
                .user(employee)
                .category(activeCategory)
                .title("Ergonomic Keyboard")
                .description("Replaced damaged keyboard")
                .amount(new BigDecimal("129.99"))
                .currency("USD")
                .expenseDate(LocalDate.now().minusDays(1))
                .paymentMethod(PaymentMethod.PERSONAL_CREDIT_CARD)
                .status(ExpenseStatus.PENDING)
                .build();

        when(expenseRepository.save(any(Expense.class))).thenReturn(savedExpense);

        ExpenseResponse response = expenseService.createExpense(request, null);

        assertNotNull(response);
        assertEquals(ExpenseStatus.PENDING, response.getStatus());
        assertEquals(new BigDecimal("129.99"), response.getAmount());
        verify(expenseRepository).save(any(Expense.class));
        verify(statusHistoryRepository).save(any());
    }

    @Test
    @DisplayName("Should reject expense creation when date is in the future")
    void createExpense_FutureDate_ThrowsBadRequest() {
        ExpenseCreateRequest request = new ExpenseCreateRequest();
        request.setCategoryId(5L);
        request.setAmount(new BigDecimal("50.00"));
        request.setExpenseDate(LocalDate.now().plusDays(2)); // Future date!

        when(userRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(categoryRepository.findById(5L)).thenReturn(Optional.of(activeCategory));

        assertThrows(BadRequestException.class, () -> expenseService.createExpense(request, null));
        verify(expenseRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw BadRequestException when editing an already approved expense")
    void updateExpense_AlreadyApproved_ThrowsBadRequest() {
        Expense approvedExpense = Expense.builder()
                .id(20L)
                .user(employee)
                .status(ExpenseStatus.APPROVED) // Already approved
                .build();

        when(expenseRepository.findById(20L)).thenReturn(Optional.of(approvedExpense));

        ExpenseUpdateRequest request = new ExpenseUpdateRequest();
        request.setCategoryId(5L);

        assertThrows(BadRequestException.class, () -> expenseService.updateExpense(20L, request, null));
    }

    @Test
    @DisplayName("Should successfully cancel pending expense")
    void cancelExpense_Pending_Success() {
        Expense pendingExpense = Expense.builder()
                .id(30L)
                .user(employee)
                .category(activeCategory)
                .status(ExpenseStatus.PENDING)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(expenseRepository.findById(30L)).thenReturn(Optional.of(pendingExpense));
        when(expenseRepository.save(any(Expense.class))).thenAnswer(i -> i.getArgument(0));

        ExpenseResponse response = expenseService.cancelExpense(30L);

        assertNotNull(response);
        assertEquals(ExpenseStatus.CANCELLED, response.getStatus());
        verify(statusHistoryRepository).save(any());
    }

    @Test
    @DisplayName("IDOR prevention: User cannot access another user's expense")
    void getExpenseById_UnauthorizedUser_ThrowsForbidden() {
        User otherUser = User.builder().id(999L).email("other@company.com").build();
        Expense otherExpense = Expense.builder()
                .id(40L)
                .user(otherUser)
                .build();

        when(expenseRepository.findById(40L)).thenReturn(Optional.of(otherExpense));

        assertThrows(ForbiddenException.class, () -> expenseService.getExpenseById(40L));
    }
}
