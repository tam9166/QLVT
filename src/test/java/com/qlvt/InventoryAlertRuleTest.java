package com.qlvt;

import com.qlvt.entity.Material;
import com.qlvt.entity.MaterialBatch;
import com.qlvt.enums.ExpiryAlertLevel;
import com.qlvt.enums.StockAlertLevel;
import com.qlvt.service.InventoryAlertService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InventoryAlertRuleTest {
    private final InventoryAlertService service = new InventoryAlertService(null, null, null);

    @Test
    void expiryLevelSeparatesExpiredAndUpcomingWindows() {
        assertEquals(ExpiryAlertLevel.EXPIRED, service.expiryLevel(batch(LocalDate.now().minusDays(1))));
        assertEquals(ExpiryAlertLevel.EXPIRING_30, service.expiryLevel(batch(LocalDate.now().plusDays(30))));
        assertEquals(ExpiryAlertLevel.EXPIRING_60, service.expiryLevel(batch(LocalDate.now().plusDays(60))));
        assertEquals(ExpiryAlertLevel.EXPIRING_90, service.expiryLevel(batch(LocalDate.now().plusDays(90))));
    }

    @Test
    void stockLevelSeparatesOutCriticalLowAndLowStock() {
        assertEquals(StockAlertLevel.OUT_OF_STOCK, service.stockLevel(material(100, 0, 500)));
        assertEquals(StockAlertLevel.CRITICAL_LOW, service.stockLevel(material(100, 20, 500)));
        assertEquals(StockAlertLevel.LOW, service.stockLevel(material(100, 80, 500)));
        assertEquals(StockAlertLevel.OVER_STOCK, service.stockLevel(material(100, 600, 500)));
    }

    private MaterialBatch batch(LocalDate expiryDate) {
        MaterialBatch batch = new MaterialBatch();
        batch.setExpiryDate(expiryDate);
        return batch;
    }

    private Material material(int minStock, int actualQuantity, int maxStock) {
        Material material = new Material();
        material.setMinStock(minStock);
        material.setActualQuantity(actualQuantity);
        material.setMaxStock(maxStock);
        return material;
    }
}
