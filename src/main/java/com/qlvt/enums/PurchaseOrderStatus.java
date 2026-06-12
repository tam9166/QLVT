package com.qlvt.enums;

public enum PurchaseOrderStatus {
    DRAFT("Nháp"),
    SENT("Đã gửi nhà cung cấp"),
    PARTIALLY_RECEIVED("Nhận một phần"),
    RECEIVED("Đã nhận đủ"),
    CANCELLED("Đã hủy");

    private final String label;

    PurchaseOrderStatus(String label) {
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
