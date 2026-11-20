package com.expensemanagement.controller;

import com.expensemanagement.dto.response.ApiResponse;
import com.expensemanagement.dto.response.ExpenseResponse;
import com.expensemanagement.dto.response.PagedResponse;
import com.expensemanagement.enums.ExpenseStatus;
import com.expensemanagement.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/expenses")
    public ResponseEntity<ApiResponse<PagedResponse<ExpenseResponse>>> getReportExpenses(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) ExpenseStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 20, sort = "expenseDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PagedResponse<ExpenseResponse> response = reportService.getReportExpenses(
                employeeId, categoryId, status, startDate, endDate, pageable
        );
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getReportSummary(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) ExpenseStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        Map<String, Object> summary = reportService.getReportSummary(
                employeeId, categoryId, status, startDate, endDate
        );
        return ResponseEntity.ok(ApiResponse.ok(summary));
    }

    @GetMapping("/export/csv")
    public ResponseEntity<InputStreamResource> exportReportCsv(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) ExpenseStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        InputStreamResource csvResource = reportService.exportReportCsv(
                employeeId, categoryId, status, startDate, endDate
        );

        String filename = "expense-report-" + LocalDate.now() + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvResource);
    }
}
