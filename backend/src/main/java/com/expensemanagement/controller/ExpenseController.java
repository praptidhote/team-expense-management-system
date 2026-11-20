package com.expensemanagement.controller;

import com.expensemanagement.dto.request.ExpenseCreateRequest;
import com.expensemanagement.dto.request.ExpenseUpdateRequest;
import com.expensemanagement.dto.response.ApiResponse;
import com.expensemanagement.dto.response.ExpenseResponse;
import com.expensemanagement.dto.response.ExpenseStatusHistoryResponse;
import com.expensemanagement.dto.response.PagedResponse;
import com.expensemanagement.enums.ExpenseStatus;
import com.expensemanagement.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ExpenseResponse>> createExpense(
            @Valid @ModelAttribute ExpenseCreateRequest request,
            @RequestPart(value = "receipt", required = false) MultipartFile receipt
    ) {
        ExpenseResponse response = expenseService.createExpense(request, receipt);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response, "Expense submitted successfully"));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<PagedResponse<ExpenseResponse>>> getMyExpenses(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) ExpenseStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PagedResponse<ExpenseResponse> response = expenseService.getMyExpenses(
                categoryId, status, startDate, endDate, search, pageable
        );
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ExpenseResponse>> getExpenseById(@PathVariable Long id) {
        ExpenseResponse response = expenseService.getExpenseById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<ApiResponse<List<ExpenseStatusHistoryResponse>>> getExpenseHistory(@PathVariable Long id) {
        List<ExpenseStatusHistoryResponse> history = expenseService.getExpenseHistory(id);
        return ResponseEntity.ok(ApiResponse.ok(history));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ExpenseResponse>> updateExpense(
            @PathVariable Long id,
            @Valid @ModelAttribute ExpenseUpdateRequest request,
            @RequestPart(value = "receipt", required = false) MultipartFile receipt
    ) {
        ExpenseResponse response = expenseService.updateExpense(id, request, receipt);
        return ResponseEntity.ok(ApiResponse.ok(response, "Expense updated successfully"));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<ExpenseResponse>> cancelExpense(@PathVariable Long id) {
        ExpenseResponse response = expenseService.cancelExpense(id);
        return ResponseEntity.ok(ApiResponse.ok(response, "Expense cancelled successfully"));
    }

    @GetMapping("/{id}/receipt")
    public ResponseEntity<Resource> downloadReceipt(@PathVariable Long id) throws IOException {
        Resource resource = expenseService.getReceiptResource(id);
        String contentType = Files.probeContentType(resource.getFile().toPath());
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
