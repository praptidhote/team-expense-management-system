package com.expensemanagement.dto.request;

import lombok.Data;

@Data
public class AssignManagerRequest {
    // Nullable to allow unassigning
    private Long managerId;
}
