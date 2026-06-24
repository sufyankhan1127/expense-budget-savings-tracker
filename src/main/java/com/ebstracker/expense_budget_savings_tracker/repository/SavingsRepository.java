package com.ebstracker.expense_budget_savings_tracker.repository;

import com.ebstracker.expense_budget_savings_tracker.entity.Savings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface SavingsRepository extends JpaRepository<Savings, Long> {

    // Get all savings goals for a specific user
    List<Savings> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Calculate total savings for a user
    @Query("SELECT COALESCE(SUM(s.savedAmount), 0) FROM Savings s WHERE s.user.id = :userId")
    BigDecimal getTotalSavings(@Param("userId") Long userId);

    // Calculate total target amount for a user
    @Query("SELECT COALESCE(SUM(s.targetAmount), 0) FROM Savings s WHERE s.user.id = :userId")
    BigDecimal getTotalSavingsTarget(@Param("userId") Long userId);

    // Get active (incomplete) savings goals
    @Query("SELECT s FROM Savings s WHERE s.user.id = :userId AND s.savedAmount < s.targetAmount")
    List<Savings> findActiveGoalsByUserId(@Param("userId") Long userId);

    // Get completed savings goals
    @Query("SELECT s FROM Savings s WHERE s.user.id = :userId AND s.savedAmount >= s.targetAmount")
    List<Savings> findCompletedGoalsByUserId(@Param("userId") Long userId);

    // Count total savings goals
    long countByUserId(Long userId);
}