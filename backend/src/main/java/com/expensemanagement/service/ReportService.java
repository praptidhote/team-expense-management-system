package com.expensemanagement.service;

import com.expensemanagement.dto.response.ExpenseResponse;
import com.expensemanagement.dto.response.PagedResponse;
import com.expensemanagement.enums.ExpenseStatus;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public interface ReportService {
    PagedResponse<ExpenseResponse> getReportExpenses(
            Long employeeId,
            Long categoryId,
            ExpenseStatus status,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    );

    Map<String, Object> getReportSummary(
            Long employeeId,
            Long categoryId,
            ExpenseStatus status,
            LocalDate startDate,
            LocalDate endDate
    );

    InputStreamResource exportReportCsv(
            Long employeeId,
            Long categoryId,
            ExpenseStatus status,
            LocalDate startDate,
            LocalDate endDate
    );
}
