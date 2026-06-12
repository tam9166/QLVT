package com.qlvt.enums;

public enum InventoryCountStatus {
    DRAFT("Nháp"),
    COUNTING("Đang kiểm kê"),
    COMPLETED("Đã hoàn tất"),
    CANCELLED("Đã hủy");

    private final String label;

    InventoryCountStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
