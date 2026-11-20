package com.expensemanagement.service;

import com.expensemanagement.dto.response.NotificationResponse;
import com.expensemanagement.dto.response.PagedResponse;
import com.expensemanagement.entity.User;
import com.expensemanagement.enums.NotificationType;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotificationService {
    void createNotification(User recipient, String title, String message, NotificationType type, Long referenceId);
    PagedResponse<NotificationResponse> getUserNotifications(Pageable pageable);
    List<NotificationResponse> getRecentNotifications();
    long getUnreadCount();
    void markAsRead(Long notificationId);
    void markAllAsRead();
}
