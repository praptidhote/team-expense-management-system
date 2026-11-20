package com.expensemanagement.dto.response;

import com.expensemanagement.enums.ApprovalDecision;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseApprovalResponse {
    private Long id;
    private Long expenseId;
    private Long reviewerId;
    private String reviewerName;
    private ApprovalDecision decision;
    private String comments;
    private LocalDateTime reviewedAt;
}
