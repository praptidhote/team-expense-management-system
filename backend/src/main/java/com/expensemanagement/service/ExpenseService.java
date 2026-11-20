package com.expensemanagement.service;

import com.expensemanagement.dto.request.ExpenseCreateRequest;
import com.expensemanagement.dto.request.ExpenseUpdateRequest;
import com.expensemanagement.dto.response.ExpenseResponse;
import com.expensemanagement.dto.response.ExpenseStatusHistoryResponse;
import com.expensemanagement.dto.response.PagedResponse;
import com.expensemanagement.enums.ExpenseStatus;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseService {
    ExpenseResponse createExpense(ExpenseCreateRequest request, MultipartFile receiptFile);
    ExpenseResponse updateExpense(Long id, ExpenseUpdateRequest request, MultipartFile receiptFile);
    ExpenseResponse cancelExpense(Long id);
    ExpenseResponse getExpenseById(Long id);
    List<ExpenseStatusHistoryResponse> getExpenseHistory(Long id);
    PagedResponse<ExpenseResponse> getMyExpenses(Long categoryId, ExpenseStatus status, LocalDate startDate, LocalDate endDate, String search, Pageable pageable);
    Resource getReceiptResource(Long id);
}
