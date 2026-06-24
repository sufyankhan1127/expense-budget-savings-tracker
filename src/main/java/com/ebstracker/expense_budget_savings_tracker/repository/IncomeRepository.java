package com.ebstracker.expense_budget_savings_tracker.repository;

import com.ebstracker.expense_budget_savings_tracker.entity.Income;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface IncomeRepository extends JpaRepository<Income, Long> {

    // Get all income for a specific user
    List<Income> findByUserIdOrderByIncomeDateDesc(Long userId);

    // Get income by source
    List<Income> findByUserIdAndSource(Long userId, String source);

    // Get income within date range
    List<Income> findByUserIdAndIncomeDateBetween(
            Long userId, LocalDate startDate, LocalDate endDate);

    // Calculate total income for current month
    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Income i " +
            "WHERE i.user.id = :userId " +
            "AND MONTH(i.incomeDate) = MONTH(CURRENT_DATE) " +
            "AND YEAR(i.incomeDate) = YEAR(CURRENT_DATE)")
    BigDecimal getTotalIncomeForCurrentMonth(@Param("userId") Long userId);

    // Calculate total income for a user (all time)
    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Income i WHERE i.user.id = :userId")
    BigDecimal getTotalIncome(@Param("userId") Long userId);

    // Get recent income (last 5)
    List<Income> findTop5ByUserIdOrderByIncomeDateDesc(Long userId);

    // Get monthly income data for chart (last 6 months)
    @Query("SELECT MONTH(i.incomeDate), SUM(i.amount) FROM Income i " +
            "WHERE i.user.id = :userId " +
            "AND i.incomeDate >= :startDate " +
            "GROUP BY MONTH(i.incomeDate) " +
            "ORDER BY MONTH(i.incomeDate)")
    List<Object[]> getMonthlyIncomeData(@Param("userId") Long userId,
                                        @Param("startDate") LocalDate startDate);
}