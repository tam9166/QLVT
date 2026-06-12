package com.qlvt.controller;

import com.qlvt.repository.NotificationRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/notifications")
public class NotificationController {
    private final NotificationRepository repository;

    public NotificationController(NotificationRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("items", repository.findTop50ByOrderByCreatedAtDesc());
        return "notifications/list";
    }

    @PostMapping("/{id}/read")
    public String read(@PathVariable Long id) {
        var notification = repository.findById(id).orElseThrow();
        notification.setReadStatus(true);
        repository.save(notification);
        return "redirect:/notifications";
    }

    @PostMapping("/read-all")
    public String readAll() {
        var items = repository.findTop50ByOrderByCreatedAtDesc();
        items.forEach(item -> item.setReadStatus(true));
        repository.saveAll(items);
        return "redirect:/notifications";
    }
}
