package com.qlvt.enums;

public enum StockAdjustmentStatus {
    DRAFT("Nháp"),
    SUBMITTED("Đã gửi duyệt"),
    APPROVED_BY_MANAGER("Quản lý đã duyệt"),
    APPROVED_BY_ACCOUNTANT("Kế toán đã duyệt"),
    APPROVED("Đã duyệt"),
    REJECTED("Đã từ chối"),
    COMPLETED("Đã hoàn tất"),
    CANCELLED("Đã hủy");

    private final String label;

    StockAdjustmentStatus(String label) {
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
