package com.expensemanagement.repository;

import com.expensemanagement.entity.ExpenseStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseStatusHistoryRepository extends JpaRepository<ExpenseStatusHistory, Long> {
    List<ExpenseStatusHistory> findByExpenseIdOrderByChangedAtAsc(Long expenseId);
}
