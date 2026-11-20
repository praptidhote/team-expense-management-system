package com.expensemanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    // Counts
    private long totalExpenses;
    private long pendingCount;
    private long approvedCount;
    private long rejectedCount;

    // Monetary aggregates
    private BigDecimal totalAmount;
    private BigDecimal pendingAmount;
    private BigDecimal approvedAmount;
    private BigDecimal rejectedAmount;

    // Admin/Manager metrics
    private Long totalEmployees;
    private Long totalManagers;

    // Visual data
    private List<MonthlySpendingResponse> monthlyTrends;
    private List<CategorySpendingResponse> categoryBreakdown;
    private List<ExpenseResponse> recentExpenses;
}
