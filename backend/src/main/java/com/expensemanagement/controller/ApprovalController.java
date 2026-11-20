package com.expensemanagement.controller;

import com.expensemanagement.dto.request.ApprovalDecisionRequest;
import com.expensemanagement.dto.response.ApiResponse;
import com.expensemanagement.dto.response.ExpenseApprovalResponse;
import com.expensemanagement.dto.response.ExpenseResponse;
import com.expensemanagement.dto.response.PagedResponse;
import com.expensemanagement.service.ApprovalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/approvals")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
public class ApprovalController {

    private final ApprovalService approvalService;

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<PagedResponse<ExpenseResponse>>> getPendingApprovals(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PagedResponse<ExpenseResponse> pending = approvalService.getPendingApprovals(pageable);
        return ResponseEntity.ok(ApiResponse.ok(pending));
    }

    @PostMapping("/{id}/review")
    public ResponseEntity<ApiResponse<ExpenseResponse>> reviewExpense(
            @PathVariable Long id,
            @Valid @RequestBody ApprovalDecisionRequest request
    ) {
        ExpenseResponse response = approvalService.reviewExpense(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Expense review submitted successfully"));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<PagedResponse<ExpenseApprovalResponse>>> getReviewHistory(
            @PageableDefault(size = 10, sort = "reviewedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PagedResponse<ExpenseApprovalResponse> history = approvalService.getReviewHistory(pageable);
        return ResponseEntity.ok(ApiResponse.ok(history));
    }
}
