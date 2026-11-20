package com.expensemanagement.util;

import com.expensemanagement.dto.response.ExpenseResponse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.List;

public class CsvGeneratorUtil {

    public static ByteArrayInputStream expensesToCsv(List<ExpenseResponse> expenses) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(out)) {
            // Write CSV Header
            writer.println("Expense ID,Submitter Name,Submitter Email,Category,Title,Description,Amount,Currency,Expense Date,Payment Method,Status,Created At");

            // Write Data Rows
            for (ExpenseResponse exp : expenses) {
                writer.printf("\"%d\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%.2f\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                        exp.getId(),
                        escapeSpecialChars(exp.getSubmitterName()),
                        escapeSpecialChars(exp.getSubmitterEmail()),
                        escapeSpecialChars(exp.getCategoryName()),
                        escapeSpecialChars(exp.getTitle()),
                        escapeSpecialChars(exp.getDescription()),
                        exp.getAmount(),
                        exp.getCurrency(),
                        exp.getExpenseDate(),
                        exp.getPaymentMethod(),
                        exp.getStatus(),
                        exp.getCreatedAt()
                );
            }
            writer.flush();
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    private static String escapeSpecialChars(String data) {
        if (data == null) return "";
        return data.replace("\"", "\"\"").replace("\n", " ").replace("\r", " ");
    }
}
