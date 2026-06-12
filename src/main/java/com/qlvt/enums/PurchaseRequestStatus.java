package com.qlvt.enums;

public enum PurchaseRequestStatus {
    DRAFT("Nháp"),
    SUBMITTED("Đã gửi"),
    APPROVED("Đã duyệt"),
    REJECTED("Đã từ chối"),
    CANCELLED("Đã hủy");

    private final String label;

    PurchaseRequestStatus(String label) {
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
