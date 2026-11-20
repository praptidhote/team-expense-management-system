package com.expensemanagement.service;

import com.expensemanagement.entity.Expense;
import com.expensemanagement.entity.ExpenseCategory;
import com.expensemanagement.entity.User;
import com.expensemanagement.enums.ExpenseStatus;
import com.expensemanagement.enums.PaymentMethod;
import com.expensemanagement.repository.ExpenseRepository;
import com.expensemanagement.repository.UserRepository;
import com.expensemanagement.security.UserPrincipal;
import com.expensemanagement.service.impl.ReportServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReportServiceImpl reportService;

    private User adminUser;
    private User employeeUser;
    private ExpenseCategory category;
    private Expense expense1;
    private Expense expense2;

    @BeforeEach
    void setUp() {
        adminUser = User.builder().id(99L).email("admin@test.com").firstName("Admin").lastName("Test").build();
        employeeUser = User.builder().id(10L).email("emp@test.com").firstName("UserOne").lastName("Test").build();
        category = ExpenseCategory.builder().id(1L).name("General").build();

        expense1 = Expense.builder()
                .id(1L)
                .user(employeeUser)
                .category(category)
                .title("Expense Alpha")
                .description("Initial operational cost")
                .amount(new BigDecimal("150.00"))
                .currency("USD")
                .expenseDate(LocalDate.now().minusDays(3))
                .paymentMethod(PaymentMethod.COMPANY_CARD)
                .status(ExpenseStatus.APPROVED)
                .build();

        expense2 = Expense.builder()
                .id(2L)
                .user(employeeUser)
                .category(category)
                .title("Expense Beta")
                .description("Secondary operational cost")
                .amount(new BigDecimal("250.50"))
                .currency("USD")
                .expenseDate(LocalDate.now().minusDays(2))
                .paymentMethod(PaymentMethod.COMPANY_CARD)
                .status(ExpenseStatus.PENDING)
                .build();

        UserPrincipal principal = new UserPrincipal(
                99L,
                "admin@test.com",
                "pass",
                "Admin",
                "Test",
                true,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
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
    @DisplayName("Should calculate report aggregate summary correctly")
    void getReportSummary_CalculatesTotals() {
        when(expenseRepository.findAll(any(Specification.class))).thenReturn(List.of(expense1, expense2));

        Map<String, Object> summary = reportService.getReportSummary(null, null, null, null, null);

        assertNotNull(summary);
        assertEquals(2L, summary.get("totalCount"));
        assertEquals(new BigDecimal("400.50"), summary.get("totalAmount"));
        assertEquals(1L, summary.get("approvedCount"));
        assertEquals(1L, summary.get("pendingCount"));
    }

    @Test
    @DisplayName("Should export CSV stream with proper content")
    void exportReportCsv_GeneratesValidStream() throws Exception {
        when(expenseRepository.findAll(any(Specification.class))).thenReturn(List.of(expense1, expense2));

        InputStreamResource resource = reportService.exportReportCsv(null, null, null, null, null);

        assertNotNull(resource);
        byte[] bytes = resource.getInputStream().readAllBytes();
        String csvContent = new String(bytes);

        assertTrue(csvContent.contains("Expense ID"));
        assertTrue(csvContent.contains("Expense Alpha"));
        assertTrue(csvContent.contains("150.00"));
    }
}
