package com.expensemanagement.service;

import com.expensemanagement.dto.request.ApprovalDecisionRequest;
import com.expensemanagement.dto.response.ExpenseApprovalResponse;
import com.expensemanagement.dto.response.ExpenseResponse;
import com.expensemanagement.dto.response.PagedResponse;
import org.springframework.data.domain.Pageable;

public interface ApprovalService {
    PagedResponse<ExpenseResponse> getPendingApprovals(Pageable pageable);
    ExpenseResponse reviewExpense(Long expenseId, ApprovalDecisionRequest request);
    PagedResponse<ExpenseApprovalResponse> getReviewHistory(Pageable pageable);
}
