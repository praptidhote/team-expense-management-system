package com.expensemanagement.repository;

import com.expensemanagement.entity.ExpenseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseCategoryRepository extends JpaRepository<ExpenseCategory, Long> {
    Optional<ExpenseCategory> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
    List<ExpenseCategory> findByActiveTrue();
}
