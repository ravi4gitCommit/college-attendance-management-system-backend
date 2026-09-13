
package com.attendance.system.service;

import com.attendance.system.entity.Notification;
import com.attendance.system.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public List<Notification> getMyNotifications(UUID userId) {
        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Notification> getMyUnreadNotifications(UUID userId) {
        return notificationRepository
                .findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }

    public Notification markAsRead(Long notificationId, UUID userId) {

        Notification notification = notificationRepository
                .findById(notificationId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Notification not found"));

        // User can only modify their own notification
        if (!notification.getUserId().equals(userId)) {
            throw new IllegalArgumentException(
                    "You cannot modify this notification"
            );
        }

        notification.setIsRead(true);

        return notificationRepository.save(notification);
    }
}