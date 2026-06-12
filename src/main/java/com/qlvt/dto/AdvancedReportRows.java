package com.qlvt.dto;

import java.time.LocalDate;

public final class AdvancedReportRows {
    private AdvancedReportRows() {
    }

    public record StockBalanceRow(String materialText, String batchNumber, String warehouseName,
                                  int actualQuantity, int availableQuantity) {
    }

    public record BatchExpiryRow(String batchNumber, String materialName, LocalDate expiryDate, int quantity) {
    }

    public record InventoryCountRow(String countCode, String warehouseName, String status, int lineCount) {
    }

    public record CodeStatusRow(String code, String status) {
    }
}
