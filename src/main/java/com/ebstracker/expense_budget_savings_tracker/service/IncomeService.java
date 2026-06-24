package com.ebstracker.expense_budget_savings_tracker.service;

import com.ebstracker.expense_budget_savings_tracker.entity.Income;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface IncomeService {

    List<Income> findByUserIdAndIncomeDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    Income addIncome(Income income);

    Income updateIncome(Income income);

    void deleteIncome(Long id);

    Income getIncomeById(Long id);

    List<Income> getAllIncomeByUser(Long userId);

    List<Income> getRecentIncome(Long userId);

    BigDecimal getTotalIncomeForCurrentMonth(Long userId);

    BigDecimal getTotalIncome(Long userId);

    List<Object[]> getMonthlyIncomeData(Long userId);
}