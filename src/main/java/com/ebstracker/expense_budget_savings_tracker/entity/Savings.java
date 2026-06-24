package com.ebstracker.expense_budget_savings_tracker.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "savings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Savings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    @NotBlank(message = "Goal name is required")
    @Column(name = "goal_name", nullable = false, length = 100)
    private String goalName;

    @Column(length = 255)
    private String description;

    @NotNull(message = "Target amount is required")
    @Positive(message = "Target amount must be greater than zero")
    @Column(name = "target_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal targetAmount;

    @NotNull(message = "Saved amount is required")
    @PositiveOrZero(message = "Saved amount cannot be negative")
    @Column(name = "saved_amount", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal savedAmount = BigDecimal.ZERO;

    @NotNull(message = "Date is required")
    @Column(name = "savings_date", nullable = false)
    private LocalDate savingsDate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Helper method to calculate completion percentage
    public BigDecimal getCompletionPercentage() {
        if (targetAmount == null || targetAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return savedAmount
                .multiply(BigDecimal.valueOf(100))
                .divide(targetAmount, 2, RoundingMode.HALF_UP);
    }

    // Helper method to calculate remaining amount
    public BigDecimal getRemainingAmount() {
        if (targetAmount == null || savedAmount == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal remaining = targetAmount.subtract(savedAmount);
        return remaining.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : remaining;
    }

    // Helper to check if goal is achieved
    public boolean isGoalAchieved() {
        return savedAmount != null && targetAmount != null
                && savedAmount.compareTo(targetAmount) >= 0;
    }
}