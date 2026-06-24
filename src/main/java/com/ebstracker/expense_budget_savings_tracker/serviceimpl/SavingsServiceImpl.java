package com.ebstracker.expense_budget_savings_tracker.serviceimpl;

import com.ebstracker.expense_budget_savings_tracker.entity.Savings;
import com.ebstracker.expense_budget_savings_tracker.entity.User;
import com.ebstracker.expense_budget_savings_tracker.exception.ResourceNotFoundException;
import com.ebstracker.expense_budget_savings_tracker.repository.SavingsRepository;
import com.ebstracker.expense_budget_savings_tracker.service.SavingsService;
import com.ebstracker.expense_budget_savings_tracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SavingsServiceImpl implements SavingsService {

    private final SavingsRepository savingsRepository;
    private final UserService userService;

    @Override
    public Savings addSavings(Savings savings) {
        User user = userService.getUserById(savings.getUser().getId());
        savings.setUser(user);

        // Ensure saved amount doesn't exceed target
        if (savings.getSavedAmount().compareTo(savings.getTargetAmount()) > 0) {
            savings.setSavedAmount(savings.getTargetAmount());
        }

        return savingsRepository.save(savings);
    }

    @Override
    public Savings updateSavings(Savings savings) {
        Savings existingSavings = getSavingsById(savings.getId());

        if (!existingSavings.getUser().getId().equals(savings.getUser().getId())) {
            throw new ResourceNotFoundException("Unauthorized access to savings record");
        }

        existingSavings.setGoalName(savings.getGoalName());
        existingSavings.setDescription(savings.getDescription());
        existingSavings.setTargetAmount(savings.getTargetAmount());
        existingSavings.setSavedAmount(savings.getSavedAmount());
        existingSavings.setSavingsDate(savings.getSavingsDate());

        // Ensure saved amount doesn't exceed target
        if (existingSavings.getSavedAmount().compareTo(existingSavings.getTargetAmount()) > 0) {
            existingSavings.setSavedAmount(existingSavings.getTargetAmount());
        }

        return savingsRepository.save(existingSavings);
    }

    @Override
    public void deleteSavings(Long id) {
        Savings savings = getSavingsById(id);
        savingsRepository.delete(savings);
    }

    @Override
    @Transactional(readOnly = true)
    public Savings getSavingsById(Long id) {
        return savingsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Savings not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Savings> getAllSavingsByUser(Long userId) {
        return savingsRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Savings> getActiveGoals(Long userId) {
        return savingsRepository.findActiveGoalsByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Savings> getCompletedGoals(Long userId) {
        return savingsRepository.findCompletedGoalsByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalSavings(Long userId) {
        return savingsRepository.getTotalSavings(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalSavingsTarget(Long userId) {
        return savingsRepository.getTotalSavingsTarget(userId);
    }
}