package com.expensemanagement.service.impl;

import com.expensemanagement.dto.response.NotificationResponse;
import com.expensemanagement.dto.response.PagedResponse;
import com.expensemanagement.entity.Notification;
import com.expensemanagement.entity.User;
import com.expensemanagement.enums.NotificationType;
import com.expensemanagement.exception.ForbiddenException;
import com.expensemanagement.exception.ResourceNotFoundException;
import com.expensemanagement.repository.NotificationRepository;
import com.expensemanagement.security.SecurityUtils;
import com.expensemanagement.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    public void createNotification(User recipient, String title, String message, NotificationType type, Long referenceId) {
        if (recipient == null) return;
        try {
            Notification notification = Notification.builder()
                    .recipient(recipient)
                    .title(title)
                    .message(message)
                    .type(type)
                    .referenceId(referenceId)
                    .read(false)
                    .build();

            notificationRepository.save(notification);
        } catch (Exception ex) {
            log.error("Failed to create notification: {}", ex.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<NotificationResponse> getUserNotifications(Pageable pageable) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        Page<Notification> page = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(currentUserId, pageable);
        return PagedResponse.fromPage(page.map(this::mapToResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getRecentNotifications() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        return notificationRepository.findTop10ByRecipientIdOrderByCreatedAtDesc(currentUserId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        return notificationRepository.countByRecipientIdAndReadFalse(currentUserId);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));

        if (!notification.getRecipient().getId().equals(currentUserId)) {
            throw new ForbiddenException("Cannot modify notification of another user");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        notificationRepository.markAllAsRead(currentUserId);
    }

    private NotificationResponse mapToResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .recipientId(n.getRecipient().getId())
                .title(n.getTitle())
                .message(n.getMessage())
                .type(n.getType())
                .referenceId(n.getReferenceId())
                .read(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
