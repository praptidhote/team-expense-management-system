package com.expensemanagement.service.impl;

import com.expensemanagement.dto.response.AuditLogResponse;
import com.expensemanagement.dto.response.PagedResponse;
import com.expensemanagement.entity.AuditLog;
import com.expensemanagement.entity.User;
import com.expensemanagement.enums.AuditAction;
import com.expensemanagement.repository.AuditLogRepository;
import com.expensemanagement.repository.UserRepository;
import com.expensemanagement.security.SecurityUtils;
import com.expensemanagement.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void log(User actor, AuditAction action, String entityName, Long entityId, String details, String ipAddress) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .actor(actor)
                    .action(action)
                    .entityName(entityName)
                    .entityId(entityId)
                    .details(details)
                    .ipAddress(ipAddress)
                    .build();

            auditLogRepository.save(auditLog);
        } catch (Exception ex) {
            log.error("Failed to write audit log: {}", ex.getMessage());
        }
    }

    @Override
    @Transactional
    public void log(AuditAction action, String entityName, Long entityId, String details) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        User actor = currentUserId != null ? userRepository.findById(currentUserId).orElse(null) : null;
        log(actor, action, entityName, entityId, details, null);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AuditLogResponse> getAuditLogs(Pageable pageable) {
        Page<AuditLog> page = auditLogRepository.findAllByOrderByCreatedAtDesc(pageable);
        Page<AuditLogResponse> mappedPage = page.map(this::mapToResponse);
        return PagedResponse.fromPage(mappedPage);
    }

    private AuditLogResponse mapToResponse(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .actorId(log.getActor() != null ? log.getActor().getId() : null)
                .actorName(log.getActor() != null ? log.getActor().getFullName() : "System")
                .actorEmail(log.getActor() != null ? log.getActor().getEmail() : "system@company.com")
                .action(log.getAction())
                .entityName(log.getEntityName())
                .entityId(log.getEntityId())
                .details(log.getDetails())
                .ipAddress(log.getIpAddress())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
