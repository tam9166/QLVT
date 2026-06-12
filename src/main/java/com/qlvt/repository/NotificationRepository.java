package com.qlvt.repository;

import com.qlvt.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findTop10ByOrderByCreatedAtDesc();
    List<Notification> findTop50ByOrderByCreatedAtDesc();
    long countByReadStatusFalse();
    boolean existsByTypeAndLinkAndCreatedAtAfter(String type, String link, java.time.LocalDateTime createdAt);
}
