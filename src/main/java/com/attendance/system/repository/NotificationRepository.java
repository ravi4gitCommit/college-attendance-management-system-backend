package com.attendance.system.repository;

import com.attendance.system.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(
            java.util.UUID userId
    );

    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(
            java.util.UUID userId
    );
}