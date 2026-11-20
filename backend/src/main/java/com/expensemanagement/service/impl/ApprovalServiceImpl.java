package com.expensemanagement.service.impl;

import com.expensemanagement.dto.request.ApprovalDecisionRequest;
import com.expensemanagement.dto.response.ExpenseApprovalResponse;
import com.expensemanagement.dto.response.ExpenseResponse;
import com.expensemanagement.dto.response.PagedResponse;
import com.expensemanagement.entity.Expense;
import com.expensemanagement.entity.ExpenseApproval;
import com.expensemanagement.entity.ExpenseStatusHistory;
import com.expensemanagement.entity.User;
import com.expensemanagement.enums.ApprovalDecision;
import com.expensemanagement.enums.AuditAction;
import com.expensemanagement.enums.ExpenseStatus;
import com.expensemanagement.enums.NotificationType;
import com.expensemanagement.enums.RoleName;
import com.expensemanagement.exception.BadRequestException;
import com.expensemanagement.exception.ForbiddenException;
import com.expensemanagement.exception.ResourceNotFoundException;
import com.expensemanagement.repository.ExpenseApprovalRepository;
import com.expensemanagement.repository.ExpenseRepository;
import com.expensemanagement.repository.ExpenseStatusHistoryRepository;
import com.expensemanagement.repository.UserRepository;
import com.expensemanagement.security.SecurityUtils;
import com.expensemanagement.service.ApprovalService;
import com.expensemanagement.service.AuditLogService;
import com.expensemanagement.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalServiceImpl implements ApprovalService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseApprovalRepository approvalRepository;
    private final ExpenseStatusHistoryRepository statusHistoryRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ExpenseResponse> getPendingApprovals(Pageable pageable) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        boolean isAdmin = SecurityUtils.hasRole(RoleName.ROLE_ADMIN.name());

        Page<Expense> page;
        if (isAdmin) {
            // Admins can see all pending expenses
            page = expenseRepository.findAll(
                    (root, query, cb) -> cb.equal(root.get("status"), ExpenseStatus.PENDING),
                    pageable
            );
        } else {
            // Managers see pending expenses of assigned direct reports
            page = expenseRepository.findPendingForManager(currentUserId, ExpenseStatus.PENDING, pageable);
        }

        return PagedResponse.fromPage(page.map(this::mapToExpenseResponse));
    }

    @Override
    @Transactional
    public ExpenseResponse reviewExpense(Long expenseId, ApprovalDecisionRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        User reviewer = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId));

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", "id", expenseId));

        // Invariant 1: Status must be PENDING
        if (expense.getStatus() != ExpenseStatus.PENDING) {
            throw new BadRequestException("Expense is not in PENDING status. Current status: " + expense.getStatus());
        }

        // Invariant 2: Segregation of duties - cannot approve own expense
        if (expense.getUser().getId().equals(currentUserId)) {
            throw new ForbiddenException("Segregation of Duties: You cannot approve or reject your own expense requests");
        }

        // Invariant 3: Must be assigned manager or Admin
        boolean isAdmin = SecurityUtils.hasRole(RoleName.ROLE_ADMIN.name());
        boolean isAssignedManager = expense.getUser().getManager() != null &&
                expense.getUser().getManager().getId().equals(currentUserId);

        if (!isAdmin && !isAssignedManager) {
            throw new ForbiddenException("You are not authorized to review this expense. You are not the employee's assigned manager.");
        }

        // Invariant 4: Mandatory reason if rejected
        if (request.getDecision() == ApprovalDecision.REJECTED && !StringUtils.hasText(request.getComments())) {
            throw new BadRequestException("Rejection comments are mandatory when rejecting an expense request");
        }

        ExpenseStatus prevStatus = expense.getStatus();
        ExpenseStatus newStatus = request.getDecision() == ApprovalDecision.APPROVED
                ? ExpenseStatus.APPROVED
                : ExpenseStatus.REJECTED;

        expense.setStatus(newStatus);
        Expense savedExpense = expenseRepository.save(expense);

        // Record Approval entry
        ExpenseApproval approval = ExpenseApproval.builder()
                .expense(savedExpense)
                .reviewer(reviewer)
                .decision(request.getDecision())
                .comments(request.getComments())
                .build();
        approvalRepository.save(approval);

        // Record Status History
        ExpenseStatusHistory history = ExpenseStatusHistory.builder()
                .expense(savedExpense)
                .previousStatus(prevStatus)
                .newStatus(newStatus)
                .changedBy(reviewer)
                .changeReason(request.getDecision() + (StringUtils.hasText(request.getComments()) ? ": " + request.getComments() : ""))
                .build();
        statusHistoryRepository.save(history);

        // Notify submitter
        NotificationType notifType = request.getDecision() == ApprovalDecision.APPROVED
                ? NotificationType.EXPENSE_APPROVED
                : NotificationType.EXPENSE_REJECTED;

        String notifMsg = String.format("Your expense '%s' ($%s) was %s by %s.%s",
                savedExpense.getTitle(),
                savedExpense.getAmount(),
                newStatus.name(),
                reviewer.getFullName(),
                StringUtils.hasText(request.getComments()) ? " Reason: " + request.getComments() : ""
        );

        notificationService.createNotification(
                savedExpense.getUser(),
                "Expense " + newStatus.name(),
                notifMsg,
                notifType,
                savedExpense.getId()
        );

        // Audit log
        AuditAction auditAction = request.getDecision() == ApprovalDecision.APPROVED
                ? AuditAction.EXPENSE_APPROVE
                : AuditAction.EXPENSE_REJECT;

        auditLogService.log(
                reviewer,
                auditAction,
                "Expense",
                savedExpense.getId(),
                "Expense " + savedExpense.getTitle() + " marked as " + newStatus.name() + " by " + reviewer.getFullName(),
                null
        );

        return mapToExpenseResponse(savedExpense);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ExpenseApprovalResponse> getReviewHistory(Pageable pageable) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        Page<ExpenseApproval> page = approvalRepository.findByReviewerIdOrderByReviewedAtDesc(currentUserId, pageable);

        Page<ExpenseApprovalResponse> mapped = page.map(a -> ExpenseApprovalResponse.builder()
                .id(a.getId())
                .expenseId(a.getExpense().getId())
                .reviewerId(a.getReviewer().getId())
                .reviewerName(a.getReviewer().getFullName())
                .decision(a.getDecision())
                .comments(a.getComments())
                .reviewedAt(a.getReviewedAt())
                .build());

        return PagedResponse.fromPage(mapped);
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
