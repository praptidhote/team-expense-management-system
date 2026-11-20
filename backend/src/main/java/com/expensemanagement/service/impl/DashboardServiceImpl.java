package com.expensemanagement.service.impl;

import com.expensemanagement.dto.response.CategorySpendingResponse;
import com.expensemanagement.dto.response.DashboardStatsResponse;
import com.expensemanagement.dto.response.ExpenseResponse;
import com.expensemanagement.dto.response.MonthlySpendingResponse;
import com.expensemanagement.entity.Expense;
import com.expensemanagement.entity.Role;
import com.expensemanagement.enums.ApprovalDecision;
import com.expensemanagement.enums.ExpenseStatus;
import com.expensemanagement.enums.RoleName;
import com.expensemanagement.repository.ExpenseApprovalRepository;
import com.expensemanagement.repository.ExpenseRepository;
import com.expensemanagement.repository.RoleRepository;
import com.expensemanagement.repository.UserRepository;
import com.expensemanagement.security.SecurityUtils;
import com.expensemanagement.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseApprovalRepository approvalRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsResponse getEmployeeDashboard() {
        Long currentUserId = SecurityUtils.getCurrentUserId();

        long total = expenseRepository.countByUserId(currentUserId);
        long pending = expenseRepository.countByUserIdAndStatus(currentUserId, ExpenseStatus.PENDING);
        long approved = expenseRepository.countByUserIdAndStatus(currentUserId, ExpenseStatus.APPROVED);
        long rejected = expenseRepository.countByUserIdAndStatus(currentUserId, ExpenseStatus.REJECTED);

        BigDecimal totalAmt = expenseRepository.sumTotalAmountByUserId(currentUserId);
        BigDecimal pendingAmt = expenseRepository.sumAmountByUserIdAndStatus(currentUserId, ExpenseStatus.PENDING);
        BigDecimal approvedAmt = expenseRepository.sumAmountByUserIdAndStatus(currentUserId, ExpenseStatus.APPROVED);
        BigDecimal rejectedAmt = expenseRepository.sumAmountByUserIdAndStatus(currentUserId, ExpenseStatus.REJECTED);

        // Past 6 months trends
        LocalDate sixMonthsAgo = LocalDate.now().minusMonths(5).withDayOfMonth(1);
        List<Object[]> rawMonthly = expenseRepository.findMonthlyUserSpending(currentUserId, sixMonthsAgo);
        List<MonthlySpendingResponse> monthlyTrends = parseMonthlyData(rawMonthly);

        // Recent 5 expenses
        List<ExpenseResponse> recent = expenseRepository.findByUserId(
                currentUserId,
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"))
        ).getContent().stream().map(this::mapToExpenseResponse).collect(Collectors.toList());

        return DashboardStatsResponse.builder()
                .totalExpenses(total)
                .pendingCount(pending)
                .approvedCount(approved)
                .rejectedCount(rejected)
                .totalAmount(totalAmt != null ? totalAmt : BigDecimal.ZERO)
                .pendingAmount(pendingAmt != null ? pendingAmt : BigDecimal.ZERO)
                .approvedAmount(approvedAmt != null ? approvedAmt : BigDecimal.ZERO)
                .rejectedAmount(rejectedAmt != null ? rejectedAmt : BigDecimal.ZERO)
                .monthlyTrends(monthlyTrends)
                .recentExpenses(recent)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsResponse getManagerDashboard() {
        Long currentUserId = SecurityUtils.getCurrentUserId();

        long pendingApprovals = expenseRepository.countByManagerIdAndStatus(currentUserId, ExpenseStatus.PENDING);
        long totalTeamExpenses = expenseRepository.countByManagerId(currentUserId);
        long approvedCount = approvalRepository.countByReviewerIdAndDecision(currentUserId, ApprovalDecision.APPROVED);
        long rejectedCount = approvalRepository.countByReviewerIdAndDecision(currentUserId, ApprovalDecision.REJECTED);

        BigDecimal teamTotalSpend = expenseRepository.sumTotalAmountByManagerId(currentUserId);
        BigDecimal pendingAmt = expenseRepository.sumAmountByManagerIdAndStatus(currentUserId, ExpenseStatus.PENDING);
        BigDecimal approvedAmt = expenseRepository.sumAmountByManagerIdAndStatus(currentUserId, ExpenseStatus.APPROVED);

        List<ExpenseResponse> recentPending = expenseRepository.findPendingForManager(
                currentUserId,
                ExpenseStatus.PENDING,
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"))
        ).getContent().stream().map(this::mapToExpenseResponse).collect(Collectors.toList());

        return DashboardStatsResponse.builder()
                .totalExpenses(totalTeamExpenses)
                .pendingCount(pendingApprovals)
                .approvedCount(approvedCount)
                .rejectedCount(rejectedCount)
                .totalAmount(teamTotalSpend != null ? teamTotalSpend : BigDecimal.ZERO)
                .pendingAmount(pendingAmt != null ? pendingAmt : BigDecimal.ZERO)
                .approvedAmount(approvedAmt != null ? approvedAmt : BigDecimal.ZERO)
                .recentExpenses(recentPending)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsResponse getAdminDashboard() {
        long total = expenseRepository.count();
        long pending = expenseRepository.countByStatus(ExpenseStatus.PENDING);
        long approved = expenseRepository.countByStatus(ExpenseStatus.APPROVED);
        long rejected = expenseRepository.countByStatus(ExpenseStatus.REJECTED);

        BigDecimal totalAmt = expenseRepository.sumTotalCompanyAmount();
        BigDecimal pendingAmt = expenseRepository.sumAmountByStatus(ExpenseStatus.PENDING);
        BigDecimal approvedAmt = expenseRepository.sumAmountByStatus(ExpenseStatus.APPROVED);
        BigDecimal rejectedAmt = expenseRepository.sumAmountByStatus(ExpenseStatus.REJECTED);

        Role employeeRole = roleRepository.findByName(RoleName.ROLE_EMPLOYEE).orElse(null);
        Role managerRole = roleRepository.findByName(RoleName.ROLE_MANAGER).orElse(null);
        long totalEmployees = employeeRole != null ? userRepository.countByRolesContaining(employeeRole) : 0L;
        long totalManagers = managerRole != null ? userRepository.countByRolesContaining(managerRole) : 0L;

        // Past 6 months spending trends
        LocalDate sixMonthsAgo = LocalDate.now().minusMonths(5).withDayOfMonth(1);
        List<Object[]> rawMonthly = expenseRepository.findMonthlyCompanySpending(sixMonthsAgo);
        List<MonthlySpendingResponse> monthlyTrends = parseMonthlyData(rawMonthly);

        // Category breakdown
        List<Object[]> rawCategory = expenseRepository.findCategorySpendingSummary();
        List<CategorySpendingResponse> categoryBreakdown = new ArrayList<>();
        for (Object[] row : rawCategory) {
            String name = (String) row[0];
            BigDecimal amt = (BigDecimal) row[1];
            categoryBreakdown.add(new CategorySpendingResponse(name, amt != null ? amt : BigDecimal.ZERO));
        }

        // Recent 5 system expenses
        List<ExpenseResponse> recent = expenseRepository.findAll(
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"))
        ).getContent().stream().map(this::mapToExpenseResponse).collect(Collectors.toList());

        return DashboardStatsResponse.builder()
                .totalExpenses(total)
                .pendingCount(pending)
                .approvedCount(approved)
                .rejectedCount(rejected)
                .totalAmount(totalAmt != null ? totalAmt : BigDecimal.ZERO)
                .pendingAmount(pendingAmt != null ? pendingAmt : BigDecimal.ZERO)
                .approvedAmount(approvedAmt != null ? approvedAmt : BigDecimal.ZERO)
                .rejectedAmount(rejectedAmt != null ? rejectedAmt : BigDecimal.ZERO)
                .totalEmployees(totalEmployees)
                .totalManagers(totalManagers)
                .monthlyTrends(monthlyTrends)
                .categoryBreakdown(categoryBreakdown)
                .recentExpenses(recent)
                .build();
    }

    private List<MonthlySpendingResponse> parseMonthlyData(List<Object[]> raw) {
        List<MonthlySpendingResponse> list = new ArrayList<>();
        for (Object[] row : raw) {
            int year = ((Number) row[0]).intValue();
            int month = ((Number) row[1]).intValue();
            BigDecimal amount = (BigDecimal) row[2];
            String monthName = Month.of(month).getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            list.add(new MonthlySpendingResponse(year, month, monthName, amount != null ? amount : BigDecimal.ZERO));
        }
        return list;
    }

    private ExpenseResponse mapToExpenseResponse(Expense e) {
        return ExpenseResponse.builder()
                .id(e.getId())
                .userId(e.getUser() != null ? e.getUser().getId() : null)
                .submitterName(e.getUser() != null ? e.getUser().getFullName() : null)
                .submitterEmail(e.getUser() != null ? e.getUser().getEmail() : null)
                .categoryId(e.getCategory() != null ? e.getCategory().getId() : null)
                .categoryName(e.getCategory() != null ? e.getCategory().getName() : null)
                .title(e.getTitle())
                .description(e.getDescription())
                .amount(e.getAmount())
                .currency(e.getCurrency())
                .expenseDate(e.getExpenseDate())
                .paymentMethod(e.getPaymentMethod())
                .status(e.getStatus())
                .receiptUrl(e.getReceiptUrl())
                .receiptOriginalName(e.getReceiptOriginalName())
                .receiptContentType(e.getReceiptContentType())
                .receiptSizeBytes(e.getReceiptSizeBytes())
                .notes(e.getNotes())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
