package com.expensemanagement.service;

import com.expensemanagement.dto.response.DashboardStatsResponse;

public interface DashboardService {
    DashboardStatsResponse getEmployeeDashboard();
    DashboardStatsResponse getManagerDashboard();
    DashboardStatsResponse getAdminDashboard();
}
