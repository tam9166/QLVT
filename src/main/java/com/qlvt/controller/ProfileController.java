package com.qlvt.controller;

import com.qlvt.repository.AppUserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
public class ProfileController {
    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfileController(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/profile")
    public String profile(Authentication authentication, Model model) {
        String username = authentication == null ? "" : authentication.getName();
        model.addAttribute("profile", userRepository.findByUsername(username).orElseThrow());
        return "profile/index";
    }

    @PostMapping("/profile")
    public String updateProfile(Authentication authentication,
                                @RequestParam String fullName,
                                @RequestParam(required = false) String email,
                                @RequestParam(required = false) String phone,
                                RedirectAttributes redirectAttributes) {
        String username = authentication == null ? "" : authentication.getName();
        var user = userRepository.findByUsername(username).orElseThrow();
        if (fullName == null || fullName.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Họ tên không được để trống.");
            return "redirect:/profile";
        }
        user.setFullName(fullName.trim());
        user.setEmail(email == null ? null : email.trim());
        user.setPhone(phone == null ? null : phone.trim());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("success", "Đã cập nhật thông tin cá nhân.");
        return "redirect:/profile";
    }

    @PostMapping("/profile/password")
    public String updatePassword(Authentication authentication,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 RedirectAttributes redirectAttributes) {
        if (newPassword == null || newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu mới phải có ít nhất 6 ký tự.");
            return "redirect:/profile";
        }
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Xác nhận mật khẩu không khớp.");
            return "redirect:/profile";
        }
        String username = authentication == null ? "" : authentication.getName();
        var user = userRepository.findByUsername(username).orElseThrow();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setVisiblePassword(newPassword);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("success", "Đã đổi mật khẩu.");
        return "redirect:/profile";
    }
}
