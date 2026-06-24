package com.ebstracker.expense_budget_savings_tracker.service;

import com.ebstracker.expense_budget_savings_tracker.entity.Savings;

import java.math.BigDecimal;
import java.util.List;

public interface SavingsService {

    Savings addSavings(Savings savings);

    Savings updateSavings(Savings savings);

    void deleteSavings(Long id);

    Savings getSavingsById(Long id);

    List<Savings> getAllSavingsByUser(Long userId);

    List<Savings> getActiveGoals(Long userId);

    List<Savings> getCompletedGoals(Long userId);

    BigDecimal getTotalSavings(Long userId);

    BigDecimal getTotalSavingsTarget(Long userId);
}