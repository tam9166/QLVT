package com.qlvt.config;

import com.qlvt.service.InventoryAlertService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(10)
public class AlertNotificationStartupRunner implements CommandLineRunner {
    private final InventoryAlertService alertService;

    public AlertNotificationStartupRunner(InventoryAlertService alertService) {
        this.alertService = alertService;
    }

    @Override
    public void run(String... args) {
        alertService.generateDailyNotifications();
    }
}
