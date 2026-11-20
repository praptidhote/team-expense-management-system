package com.expensemanagement.repository;

import com.expensemanagement.entity.ExpenseApproval;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseApprovalRepository extends JpaRepository<ExpenseApproval, Long> {
    List<ExpenseApproval> findByExpenseIdOrderByReviewedAtDesc(Long expenseId);
    Page<ExpenseApproval> findByReviewerIdOrderByReviewedAtDesc(Long reviewerId, Pageable pageable);
    long countByReviewerId(Long reviewerId);
    long countByReviewerIdAndDecision(Long reviewerId, com.expensemanagement.enums.ApprovalDecision decision);
}
