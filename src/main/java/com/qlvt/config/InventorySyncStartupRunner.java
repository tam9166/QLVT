package com.qlvt.config;

import com.qlvt.service.InventorySyncService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(9)
public class InventorySyncStartupRunner implements CommandLineRunner {
    private final InventorySyncService inventorySyncService;

    public InventorySyncStartupRunner(InventorySyncService inventorySyncService) {
        this.inventorySyncService = inventorySyncService;
    }

    @Override
    public void run(String... args) {
        inventorySyncService.syncAllActiveMaterials();
    }
}
