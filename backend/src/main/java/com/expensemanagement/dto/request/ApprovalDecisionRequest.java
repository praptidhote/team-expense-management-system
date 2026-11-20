package com.expensemanagement.dto.request;

import com.expensemanagement.enums.ApprovalDecision;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApprovalDecisionRequest {

    @NotNull(message = "Decision (APPROVED or REJECTED) is required")
    private ApprovalDecision decision;

    private String comments;
}
