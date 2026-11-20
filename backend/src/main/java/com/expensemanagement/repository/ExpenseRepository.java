package com.expensemanagement.repository;

import com.expensemanagement.entity.Expense;
import com.expensemanagement.enums.ExpenseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long>, JpaSpecificationExecutor<Expense> {

    Page<Expense> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT e FROM Expense e WHERE e.user.manager.id = :managerId AND e.status = :status")
    Page<Expense> findPendingForManager(@Param("managerId") Long managerId, @Param("status") ExpenseStatus status, Pageable pageable);

    @Query("SELECT e FROM Expense e WHERE e.user.manager.id = :managerId")
    Page<Expense> findAllForManager(@Param("managerId") Long managerId, Pageable pageable);

    long countByUserId(Long userId);
    long countByUserIdAndStatus(Long userId, ExpenseStatus status);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user.id = :userId AND e.status = :status")
    BigDecimal sumAmountByUserIdAndStatus(@Param("userId") Long userId, @Param("status") ExpenseStatus status);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user.id = :userId")
    BigDecimal sumTotalAmountByUserId(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user.manager.id = :managerId AND e.status = :status")
    BigDecimal sumAmountByManagerIdAndStatus(@Param("managerId") Long managerId, @Param("status") ExpenseStatus status);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user.manager.id = :managerId")
    BigDecimal sumTotalAmountByManagerId(@Param("managerId") Long managerId);

    @Query("SELECT COUNT(e) FROM Expense e WHERE e.user.manager.id = :managerId AND e.status = :status")
    long countByManagerIdAndStatus(@Param("managerId") Long managerId, @Param("status") ExpenseStatus status);

    @Query("SELECT COUNT(e) FROM Expense e WHERE e.user.manager.id = :managerId")
    long countByManagerId(@Param("managerId") Long managerId);

    long countByStatus(ExpenseStatus status);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.status = :status")
    BigDecimal sumAmountByStatus(@Param("status") ExpenseStatus status);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e")
    BigDecimal sumTotalCompanyAmount();

    @Query("SELECT e.category.name, COALESCE(SUM(e.amount), 0) FROM Expense e GROUP BY e.category.name")
    List<Object[]> findCategorySpendingSummary();

    @Query("SELECT FUNCTION('YEAR', e.expenseDate), FUNCTION('MONTH', e.expenseDate), COALESCE(SUM(e.amount), 0) " +
           "FROM Expense e WHERE e.expenseDate >= :startDate " +
           "GROUP BY FUNCTION('YEAR', e.expenseDate), FUNCTION('MONTH', e.expenseDate) " +
           "ORDER BY FUNCTION('YEAR', e.expenseDate), FUNCTION('MONTH', e.expenseDate)")
    List<Object[]> findMonthlyCompanySpending(@Param("startDate") LocalDate startDate);

    @Query("SELECT FUNCTION('YEAR', e.expenseDate), FUNCTION('MONTH', e.expenseDate), COALESCE(SUM(e.amount), 0) " +
           "FROM Expense e WHERE e.user.id = :userId AND e.expenseDate >= :startDate " +
           "GROUP BY FUNCTION('YEAR', e.expenseDate), FUNCTION('MONTH', e.expenseDate) " +
           "ORDER BY FUNCTION('YEAR', e.expenseDate), FUNCTION('MONTH', e.expenseDate)")
    List<Object[]> findMonthlyUserSpending(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);
}
