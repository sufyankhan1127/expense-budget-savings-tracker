package com.ebstracker.expense_budget_savings_tracker.serviceimpl;

import com.ebstracker.expense_budget_savings_tracker.entity.Expense;
import com.ebstracker.expense_budget_savings_tracker.entity.User;
import com.ebstracker.expense_budget_savings_tracker.exception.ResourceNotFoundException;
import com.ebstracker.expense_budget_savings_tracker.repository.ExpenseRepository;
import com.ebstracker.expense_budget_savings_tracker.service.ExpenseService;
import com.ebstracker.expense_budget_savings_tracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserService userService;

    @Override
    public Expense addExpense(Expense expense) {
        User user = userService.getUserById(expense.getUser().getId());
        expense.setUser(user);
        return expenseRepository.save(expense);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Expense> findByUserIdAndExpenseDateBetween(Long userId, LocalDate startDate, LocalDate endDate) {
        return expenseRepository.findByUserIdAndExpenseDateBetween(userId, startDate, endDate);
    }

    @Override
    public Expense updateExpense(Expense expense) {
        Expense existingExpense = getExpenseById(expense.getId());

        // Ensure user cannot change ownership
        if (!existingExpense.getUser().getId().equals(expense.getUser().getId())) {
            throw new ResourceNotFoundException("Unauthorized access to expense");
        }

        existingExpense.setCategory(expense.getCategory());
        existingExpense.setDescription(expense.getDescription());
        existingExpense.setAmount(expense.getAmount());
        existingExpense.setExpenseDate(expense.getExpenseDate());

        return expenseRepository.save(existingExpense);
    }

    @Override
    public void deleteExpense(Long id) {
        Expense expense = getExpenseById(id);
        expenseRepository.delete(expense);
    }

    @Override
    @Transactional(readOnly = true)
    public Expense getExpenseById(Long id) {
        return expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Expense> getAllExpensesByUser(Long userId) {
        return expenseRepository.findByUserIdOrderByExpenseDateDesc(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Expense> getRecentExpenses(Long userId) {
        return expenseRepository.findTop5ByUserIdOrderByExpenseDateDesc(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalExpensesForCurrentMonth(Long userId) {
        return expenseRepository.getTotalExpensesForCurrentMonth(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalExpenses(Long userId) {
        return expenseRepository.getTotalExpenses(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getExpenseCategoriesWithTotals(Long userId) {
        return expenseRepository.getExpenseCategoriesWithTotals(userId);
    }
}