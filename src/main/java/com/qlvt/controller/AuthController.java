package com.qlvt.controller;

import com.qlvt.service.PasswordResetRequestService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {
    private final PasswordResetRequestService passwordResetRequestService;

    public AuthController(PasswordResetRequestService passwordResetRequestService) {
        this.passwordResetRequestService = passwordResetRequestService;
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String requestPasswordReset(@RequestParam(name = "lookup", defaultValue = "") String lookup,
                                       HttpServletRequest request,
                                       Model model) {
        passwordResetRequestService.requestReset(lookup, request.getRemoteAddr());
        model.addAttribute("submitted", true);
        return "auth/forgot-password";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "auth/access-denied";
    }
}
