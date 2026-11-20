package com.expensemanagement.dto.response;

import com.expensemanagement.enums.ExpenseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseStatusHistoryResponse {
    private Long id;
    private Long expenseId;
    private ExpenseStatus previousStatus;
    private ExpenseStatus newStatus;
    private Long changedByUserId;
    private String changedByUserName;
    private String changeReason;
    private LocalDateTime changedAt;
}
