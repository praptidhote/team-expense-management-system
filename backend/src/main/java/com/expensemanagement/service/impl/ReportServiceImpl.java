package com.expensemanagement.service.impl;

import com.expensemanagement.dto.response.ExpenseResponse;
import com.expensemanagement.dto.response.PagedResponse;
import com.expensemanagement.entity.Expense;
import com.expensemanagement.enums.ExpenseStatus;
import com.expensemanagement.enums.RoleName;
import com.expensemanagement.exception.ForbiddenException;
import com.expensemanagement.repository.ExpenseRepository;
import com.expensemanagement.repository.UserRepository;
import com.expensemanagement.security.SecurityUtils;
import com.expensemanagement.service.ReportService;
import com.expensemanagement.specification.ExpenseSpecification;
import com.expensemanagement.util.CsvGeneratorUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ExpenseResponse> getReportExpenses(
            Long employeeId,
            Long categoryId,
            ExpenseStatus status,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    ) {
        Specification<Expense> spec = buildReportSpecification(employeeId, categoryId, status, startDate, endDate);
        Page<Expense> page = expenseRepository.findAll(spec, pageable);
        return PagedResponse.fromPage(page.map(this::mapToExpenseResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getReportSummary(
            Long employeeId,
            Long categoryId,
            ExpenseStatus status,
            LocalDate startDate,
            LocalDate endDate
    ) {
        Specification<Expense> spec = buildReportSpecification(employeeId, categoryId, status, startDate, endDate);
        List<Expense> expenses = expenseRepository.findAll(spec);

        BigDecimal totalAmount = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalCount = expenses.size();
        long approvedCount = expenses.stream().filter(e -> e.getStatus() == ExpenseStatus.APPROVED).count();
        long pendingCount = expenses.stream().filter(e -> e.getStatus() == ExpenseStatus.PENDING).count();
        long rejectedCount = expenses.stream().filter(e -> e.getStatus() == ExpenseStatus.REJECTED).count();

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalAmount", totalAmount);
        summary.put("totalCount", totalCount);
        summary.put("approvedCount", approvedCount);
        summary.put("pendingCount", pendingCount);
        summary.put("rejectedCount", rejectedCount);

        return summary;
    }

    @Override
    @Transactional(readOnly = true)
    public InputStreamResource exportReportCsv(
            Long employeeId,
            Long categoryId,
            ExpenseStatus status,
            LocalDate startDate,
            LocalDate endDate
    ) {
        Specification<Expense> spec = buildReportSpecification(employeeId, categoryId, status, startDate, endDate);
        List<Expense> expenses = expenseRepository.findAll(spec);
        List<ExpenseResponse> dtoList = expenses.stream().map(this::mapToExpenseResponse).collect(Collectors.toList());

        ByteArrayInputStream csvStream = CsvGeneratorUtil.expensesToCsv(dtoList);
        return new InputStreamResource(csvStream);
    }

    private Specification<Expense> buildReportSpecification(
            Long employeeId,
            Long categoryId,
            ExpenseStatus status,
            LocalDate startDate,
            LocalDate endDate
    ) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        boolean isAdmin = SecurityUtils.hasRole(RoleName.ROLE_ADMIN.name());
        boolean isManager = SecurityUtils.hasRole(RoleName.ROLE_MANAGER.name());

        Long managerScopeId = null;
        Long userScopeId = employeeId;

        if (!isAdmin) {
            if (isManager) {
                // If manager specifies an employee, ensure the employee reports to them
                if (employeeId != null) {
                    boolean isAssigned = userRepository.findById(employeeId)
                            .map(u -> u.getManager() != null && u.getManager().getId().equals(currentUserId))
                            .orElse(false);
                    if (!isAssigned) {
                        throw new ForbiddenException("You can only generate reports for employees directly assigned to you");
                    }
                } else {
                    managerScopeId = currentUserId;
                }
            } else {
                // Regular employees can only view their own reports
                userScopeId = currentUserId;
            }
        }

        return ExpenseSpecification.filter(
                userScopeId,
                managerScopeId,
                categoryId,
                status,
                startDate,
                endDate,
                null
        );
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
