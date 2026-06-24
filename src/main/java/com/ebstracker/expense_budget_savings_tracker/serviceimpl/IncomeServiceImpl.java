package com.ebstracker.expense_budget_savings_tracker.serviceimpl;

import com.ebstracker.expense_budget_savings_tracker.entity.Income;
import com.ebstracker.expense_budget_savings_tracker.entity.User;
import com.ebstracker.expense_budget_savings_tracker.exception.ResourceNotFoundException;
import com.ebstracker.expense_budget_savings_tracker.repository.IncomeRepository;
import com.ebstracker.expense_budget_savings_tracker.service.IncomeService;
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
public class IncomeServiceImpl implements IncomeService {

    private final IncomeRepository incomeRepository;
    private final UserService userService;

    @Override
    public Income addIncome(Income income) {
        User user = userService.getUserById(income.getUser().getId());
        income.setUser(user);
        return incomeRepository.save(income);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Income> findByUserIdAndIncomeDateBetween(Long userId, LocalDate startDate, LocalDate endDate) {
        return incomeRepository.findByUserIdAndIncomeDateBetween(userId, startDate, endDate);
    }

    @Override
    public Income updateIncome(Income income) {
        Income existingIncome = getIncomeById(income.getId());

        if (!existingIncome.getUser().getId().equals(income.getUser().getId())) {
            throw new ResourceNotFoundException("Unauthorized access to income record");
        }

        existingIncome.setSource(income.getSource());
        existingIncome.setDescription(income.getDescription());
        existingIncome.setAmount(income.getAmount());
        existingIncome.setIncomeDate(income.getIncomeDate());

        return incomeRepository.save(existingIncome);
    }

    @Override
    public void deleteIncome(Long id) {
        Income income = getIncomeById(id);
        incomeRepository.delete(income);
    }

    @Override
    @Transactional(readOnly = true)
    public Income getIncomeById(Long id) {
        return incomeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Income not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Income> getAllIncomeByUser(Long userId) {
        return incomeRepository.findByUserIdOrderByIncomeDateDesc(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Income> getRecentIncome(Long userId) {
        return incomeRepository.findTop5ByUserIdOrderByIncomeDateDesc(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalIncomeForCurrentMonth(Long userId) {
        return incomeRepository.getTotalIncomeForCurrentMonth(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalIncome(Long userId) {
        return incomeRepository.getTotalIncome(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getMonthlyIncomeData(Long userId) {
        LocalDate sixMonthsAgo = LocalDate.now().minusMonths(6);
        return incomeRepository.getMonthlyIncomeData(userId, sixMonthsAgo);
    }
}