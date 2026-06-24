package com.ebstracker.expense_budget_savings_tracker.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "expenses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    @NotBlank(message = "Category is required")
    @Column(nullable = false, length = 50)
    private String category;

    @NotBlank(message = "Description is required")
    @Column(nullable = false, length = 255)
    private String description;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than zero")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @NotNull(message = "Date is required")
    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}