package com.qlvt.enums;

public enum ReceiptStatus {
    DRAFT("Nháp"),
    CONFIRMED("Đã xác nhận"),
    CANCELLED("Đã hủy");

    private final String label;

    ReceiptStatus(String label) {
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
