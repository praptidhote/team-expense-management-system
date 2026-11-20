package com.expensemanagement.dto.response;

import com.expensemanagement.enums.ExpenseStatus;
import com.expensemanagement.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseResponse {
    private Long id;
    private Long userId;
    private String submitterName;
    private String submitterEmail;
    private Long categoryId;
    private String categoryName;
    private String title;
    private String description;
    private BigDecimal amount;
    private String currency;
    private LocalDate expenseDate;
    private PaymentMethod paymentMethod;
    private ExpenseStatus status;
    private String receiptUrl;
    private String receiptOriginalName;
    private String receiptContentType;
    private Long receiptSizeBytes;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
