package com.ebstracker.expense_budget_savings_tracker.service;

import com.ebstracker.expense_budget_savings_tracker.entity.Expense;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ExpenseService {
    List<Expense> findByUserIdAndExpenseDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
    Expense addExpense(Expense expense);

    Expense updateExpense(Expense expense);

    void deleteExpense(Long id);

    Expense getExpenseById(Long id);

    List<Expense> getAllExpensesByUser(Long userId);

    List<Expense> getRecentExpenses(Long userId);

    BigDecimal getTotalExpensesForCurrentMonth(Long userId);

    BigDecimal getTotalExpenses(Long userId);

    List<Object[]> getExpenseCategoriesWithTotals(Long userId);
}