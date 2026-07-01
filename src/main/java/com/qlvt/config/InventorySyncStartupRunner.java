package com.qlvt.config;

import com.qlvt.service.InventorySyncService;
import com.qlvt.service.WarehouseWorkflowService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(9)
public class InventorySyncStartupRunner implements CommandLineRunner {
    private final InventorySyncService inventorySyncService;
    private final WarehouseWorkflowService warehouseWorkflowService;

    public InventorySyncStartupRunner(InventorySyncService inventorySyncService,
                                      WarehouseWorkflowService warehouseWorkflowService) {
        this.inventorySyncService = inventorySyncService;
        this.warehouseWorkflowService = warehouseWorkflowService;
    }

    @Override
    public void run(String... args) {
        warehouseWorkflowService.syncBalancesFromBatches();
        inventorySyncService.syncAllActiveMaterials();
    }
}
