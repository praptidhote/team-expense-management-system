package com.expensemanagement.controller;

import com.expensemanagement.dto.response.ApiResponse;
import com.expensemanagement.dto.response.DashboardStatsResponse;
import com.expensemanagement.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/employee")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getEmployeeDashboard() {
        DashboardStatsResponse response = dashboardService.getEmployeeDashboard();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/manager")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getManagerDashboard() {
        DashboardStatsResponse response = dashboardService.getManagerDashboard();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getAdminDashboard() {
        DashboardStatsResponse response = dashboardService.getAdminDashboard();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
