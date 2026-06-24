package com.ebstracker.expense_budget_savings_tracker.repository;

import com.ebstracker.expense_budget_savings_tracker.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    // Get all expenses for a specific user
    List<Expense> findByUserIdOrderByExpenseDateDesc(Long userId);

    // Get expenses by category
    List<Expense> findByUserIdAndCategory(Long userId, String category);

    // Get expenses within date range
    List<Expense> findByUserIdAndExpenseDateBetween(
            Long userId, LocalDate startDate, LocalDate endDate);

    // Calculate total expenses for a user (current month)
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e " +
            "WHERE e.user.id = :userId " +
            "AND MONTH(e.expenseDate) = MONTH(CURRENT_DATE) " +
            "AND YEAR(e.expenseDate) = YEAR(CURRENT_DATE)")
    BigDecimal getTotalExpensesForCurrentMonth(@Param("userId") Long userId);

    // Calculate total expenses for a user (all time)
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user.id = :userId")
    BigDecimal getTotalExpenses(@Param("userId") Long userId);

    // Get recent expenses (last 5)
    List<Expense> findTop5ByUserIdOrderByExpenseDateDesc(Long userId);

    // Get expense categories with totals for chart
    @Query("SELECT e.category, SUM(e.amount) FROM Expense e " +
            "WHERE e.user.id = :userId " +
            "AND MONTH(e.expenseDate) = MONTH(CURRENT_DATE) " +
            "AND YEAR(e.expenseDate) = YEAR(CURRENT_DATE) " +
            "GROUP BY e.category")
    List<Object[]> getExpenseCategoriesWithTotals(@Param("userId") Long userId);
}