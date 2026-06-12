package com.qlvt.controller;

import com.qlvt.repository.NotificationRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelControllerAdvice {
    private final NotificationRepository notificationRepository;

    public GlobalModelControllerAdvice(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @ModelAttribute("unreadNotificationCount")
    public long unreadNotificationCount() {
        return notificationRepository.countByReadStatusFalse();
    }

    @ModelAttribute("currentPath")
    public String currentPath(HttpServletRequest request) {
        return request == null ? "/" : request.getRequestURI();
    }
}
