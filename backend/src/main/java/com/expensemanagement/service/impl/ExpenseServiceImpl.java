package com.expensemanagement.service.impl;

import com.expensemanagement.dto.request.ExpenseCreateRequest;
import com.expensemanagement.dto.request.ExpenseUpdateRequest;
import com.expensemanagement.dto.response.ExpenseResponse;
import com.expensemanagement.dto.response.ExpenseStatusHistoryResponse;
import com.expensemanagement.dto.response.PagedResponse;
import com.expensemanagement.entity.*;
import com.expensemanagement.enums.AuditAction;
import com.expensemanagement.enums.ExpenseStatus;
import com.expensemanagement.enums.NotificationType;
import com.expensemanagement.enums.RoleName;
import com.expensemanagement.exception.BadRequestException;
import com.expensemanagement.exception.ForbiddenException;
import com.expensemanagement.exception.ResourceNotFoundException;
import com.expensemanagement.repository.*;
import com.expensemanagement.security.SecurityUtils;
import com.expensemanagement.service.AuditLogService;
import com.expensemanagement.service.ExpenseService;
import com.expensemanagement.service.FileStorageService;
import com.expensemanagement.service.NotificationService;
import com.expensemanagement.specification.ExpenseSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final ExpenseCategoryRepository categoryRepository;
    private final ExpenseStatusHistoryRepository statusHistoryRepository;
    private final FileStorageService fileStorageService;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public ExpenseResponse createExpense(ExpenseCreateRequest request, MultipartFile receiptFile) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId));

        ExpenseCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("ExpenseCategory", "id", request.getCategoryId()));

        if (!category.isActive()) {
            throw new BadRequestException("The selected expense category is inactive");
        }

        if (request.getExpenseDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("Expense date cannot be in the future");
        }

        String receiptUrl = null;
        String originalName = null;
        String contentType = null;
        Long sizeBytes = null;

        if (receiptFile != null && !receiptFile.isEmpty()) {
            receiptUrl = fileStorageService.storeFile(receiptFile);
            originalName = receiptFile.getOriginalFilename();
            contentType = receiptFile.getContentType();
            sizeBytes = receiptFile.getSize();
        }

        Expense expense = Expense.builder()
                .user(currentUser)
                .category(category)
                .title(request.getTitle().trim())
                .description(request.getDescription().trim())
                .amount(request.getAmount())
                .currency(request.getCurrency() != null ? request.getCurrency() : "USD")
                .expenseDate(request.getExpenseDate())
                .paymentMethod(request.getPaymentMethod())
                .status(ExpenseStatus.PENDING)
                .receiptUrl(receiptUrl)
                .receiptOriginalName(originalName)
                .receiptContentType(contentType)
                .receiptSizeBytes(sizeBytes)
                .notes(request.getNotes())
                .build();

        Expense saved = expenseRepository.save(expense);

        // Record Initial History
        ExpenseStatusHistory history = ExpenseStatusHistory.builder()
                .expense(saved)
                .previousStatus(null)
                .newStatus(ExpenseStatus.PENDING)
                .changedBy(currentUser)
                .changeReason("Expense submitted for review")
                .build();
        statusHistoryRepository.save(history);

        // Notify Assigned Manager if present
        if (currentUser.getManager() != null) {
            notificationService.createNotification(
                    currentUser.getManager(),
                    "New Expense Pending Approval",
                    currentUser.getFullName() + " submitted an expense request: '" + saved.getTitle() + "' for $" + saved.getAmount(),
                    NotificationType.EXPENSE_SUBMITTED,
                    saved.getId()
            );
        }

        auditLogService.log(
                currentUser,
                AuditAction.EXPENSE_CREATE,
                "Expense",
                saved.getId(),
                "Submitted expense: " + saved.getTitle() + " ($" + saved.getAmount() + ")",
                null
        );

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public ExpenseResponse updateExpense(Long id, ExpenseUpdateRequest request, MultipartFile receiptFile) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", "id", id));

        // Submitter Ownership Check
        if (!expense.getUser().getId().equals(currentUserId)) {
            throw new ForbiddenException("You are not authorized to edit this expense");
        }

        // Only PENDING can be updated
        if (expense.getStatus() != ExpenseStatus.PENDING) {
            throw new BadRequestException("Cannot edit expense with status: " + expense.getStatus() + ". Only PENDING expenses can be edited.");
        }

        ExpenseCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("ExpenseCategory", "id", request.getCategoryId()));

        if (request.getExpenseDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("Expense date cannot be in the future");
        }

        expense.setTitle(request.getTitle().trim());
        expense.setDescription(request.getDescription().trim());
        expense.setCategory(category);
        expense.setAmount(request.getAmount());
        expense.setExpenseDate(request.getExpenseDate());
        expense.setPaymentMethod(request.getPaymentMethod());
        expense.setNotes(request.getNotes());

        if (receiptFile != null && !receiptFile.isEmpty()) {
            // Delete old file if present
            if (expense.getReceiptUrl() != null) {
                fileStorageService.deleteFile(expense.getReceiptUrl());
            }
            String storedName = fileStorageService.storeFile(receiptFile);
            expense.setReceiptUrl(storedName);
            expense.setReceiptOriginalName(receiptFile.getOriginalFilename());
            expense.setReceiptContentType(receiptFile.getContentType());
            expense.setReceiptSizeBytes(receiptFile.getSize());
        }

        Expense updated = expenseRepository.save(expense);

        auditLogService.log(
                AuditAction.EXPENSE_UPDATE,
                "Expense",
                updated.getId(),
                "Updated pending expense: " + updated.getTitle()
        );

        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public ExpenseResponse cancelExpense(Long id) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId));

        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", "id", id));

        if (!expense.getUser().getId().equals(currentUserId)) {
            throw new ForbiddenException("You can only cancel your own expenses");
        }

        if (expense.getStatus() != ExpenseStatus.PENDING) {
            throw new BadRequestException("Only pending expenses can be cancelled. Current status: " + expense.getStatus());
        }

        ExpenseStatus prevStatus = expense.getStatus();
        expense.setStatus(ExpenseStatus.CANCELLED);
        Expense saved = expenseRepository.save(expense);

        // Record history
        ExpenseStatusHistory history = ExpenseStatusHistory.builder()
                .expense(saved)
                .previousStatus(prevStatus)
                .newStatus(ExpenseStatus.CANCELLED)
                .changedBy(currentUser)
                .changeReason("Cancelled by submitter")
                .build();
        statusHistoryRepository.save(history);

        auditLogService.log(
                currentUser,
                AuditAction.EXPENSE_CANCEL,
                "Expense",
                saved.getId(),
                "Cancelled expense: " + saved.getTitle(),
                null
        );

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseResponse getExpenseById(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", "id", id));

        validateAccess(expense);
        return mapToResponse(expense);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseStatusHistoryResponse> getExpenseHistory(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", "id", id));

        validateAccess(expense);

        return statusHistoryRepository.findByExpenseIdOrderByChangedAtAsc(id).stream()
                .map(h -> ExpenseStatusHistoryResponse.builder()
                        .id(h.getId())
                        .expenseId(h.getExpense().getId())
                        .previousStatus(h.getPreviousStatus())
                        .newStatus(h.getNewStatus())
                        .changedByUserId(h.getChangedBy().getId())
                        .changedByUserName(h.getChangedBy().getFullName())
                        .changeReason(h.getChangeReason())
                        .changedAt(h.getChangedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ExpenseResponse> getMyExpenses(Long categoryId, ExpenseStatus status, LocalDate startDate, LocalDate endDate, String search, Pageable pageable) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        Specification<Expense> spec = ExpenseSpecification.filter(
                currentUserId,
                null,
                categoryId,
                status,
                startDate,
                endDate,
                search
        );

        Page<Expense> page = expenseRepository.findAll(spec, pageable);
        return PagedResponse.fromPage(page.map(this::mapToResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public Resource getReceiptResource(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", "id", id));

        validateAccess(expense);

        if (expense.getReceiptUrl() == null) {
            throw new ResourceNotFoundException("No receipt uploaded for expense ID: " + id);
        }

        return fileStorageService.loadFileAsResource(expense.getReceiptUrl());
    }

    private void validateAccess(Expense expense) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new ForbiddenException("Authentication required");
        }

        // 1. Is submitter?
        if (expense.getUser().getId().equals(currentUserId)) {
            return;
        }

        // 2. Is assigned manager?
        if (expense.getUser().getManager() != null &&
                expense.getUser().getManager().getId().equals(currentUserId)) {
            return;
        }

        // 3. Is Admin?
        if (SecurityUtils.hasRole(RoleName.ROLE_ADMIN.name())) {
            return;
        }

        throw new ForbiddenException("You are not authorized to view this expense");
    }

    private ExpenseResponse mapToResponse(Expense e) {
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
