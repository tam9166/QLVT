package com.qlvt.controller;

import com.qlvt.service.InventoryAlertService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/alerts")
public class AlertController {
    private final InventoryAlertService alertService;

    public AlertController(InventoryAlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("expiryAlerts", alertService.expiryAlerts());
        model.addAttribute("stockAlerts", alertService.stockAlerts());
        model.addAttribute("expiryCounts", alertService.countExpiryByLevel());
        model.addAttribute("stockCounts", alertService.countStockByLevel());
        model.addAttribute("alertService", alertService);
        return "alerts/index";
    }

    @PostMapping("/generate-notifications")
    public String generateNotifications() {
        alertService.generateDailyNotifications();
        return "redirect:/alerts";
    }
}
