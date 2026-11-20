package com.expensemanagement.entity;

import com.expensemanagement.enums.ExpenseStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "expense_status_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id", nullable = false)
    private Expense expense;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 30)
    private ExpenseStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", length = 30, nullable = false)
    private ExpenseStatus newStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_user_id", nullable = false)
    private User changedBy;

    @Column(name = "change_reason", columnDefinition = "TEXT")
    private String changeReason;

    @CreationTimestamp
    @Column(nullable = false, updatable = false, name = "changed_at")
    private LocalDateTime changedAt;
}
