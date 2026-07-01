package com.qlvt.service;

import com.qlvt.entity.AppUser;
import com.qlvt.repository.AppUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PasswordResetRequestService {
    private final AppUserRepository userRepository;
    private final AuditService auditService;
    private final NotificationService notificationService;

    public PasswordResetRequestService(AppUserRepository userRepository,
                                       AuditService auditService,
                                       NotificationService notificationService) {
        this.userRepository = userRepository;
        this.auditService = auditService;
        this.notificationService = notificationService;
    }

    @Transactional
    public void requestReset(String lookup, String remoteAddress) {
        String normalizedLookup = normalizeLookup(lookup);
        if (normalizedLookup.isBlank()) {
            return;
        }

        userRepository.findActiveByUsernameOrEmail(normalizedLookup)
                .ifPresent(user -> recordRequest(user, remoteAddress));
    }

    private void recordRequest(AppUser user, String remoteAddress) {
        String detail = "Password reset requested from " + safe(remoteAddress)
                + "; admin must issue a temporary password manually.";
        auditService.log("anonymous", "REQUEST_PASSWORD_RESET", "AppUser", user.getUsername(), detail);
        notificationService.notify(
                "Yêu cầu reset mật khẩu",
                user.getFullName() + " (" + user.getUsername() + ") cần cấp mật khẩu tạm.",
                "PASSWORD_RESET_REQUEST",
                "ADMIN",
                "/users?q=" + user.getUsername()
        );
    }

    private String normalizeLookup(String lookup) {
        if (lookup == null) {
            return "";
        }
        String trimmed = lookup.trim();
        return trimmed.length() > 160 ? trimmed.substring(0, 160) : trimmed;
    }

    private String safe(String value) {
        if (value == null || value.isBlank()) {
            return "unknown";
        }
        return value.length() > 80 ? value.substring(0, 80) : value;
    }
}
