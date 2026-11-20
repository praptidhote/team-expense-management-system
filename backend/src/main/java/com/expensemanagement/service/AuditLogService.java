package com.expensemanagement.service;

import com.expensemanagement.dto.response.AuditLogResponse;
import com.expensemanagement.dto.response.PagedResponse;
import com.expensemanagement.entity.User;
import com.expensemanagement.enums.AuditAction;
import org.springframework.data.domain.Pageable;

public interface AuditLogService {
    void log(User actor, AuditAction action, String entityName, Long entityId, String details, String ipAddress);
    void log(AuditAction action, String entityName, Long entityId, String details);
    PagedResponse<AuditLogResponse> getAuditLogs(Pageable pageable);
}
