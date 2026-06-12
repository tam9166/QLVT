package com.qlvt.enums;

public enum StockTransferStatus {
    DRAFT("Nháp"),
    SUBMITTED("Đã gửi duyệt"),
    APPROVED("Đã duyệt"),
    TRANSFERRED("Đã chuyển"),
    RECEIVED("Đã nhận"),
    REJECTED("Đã từ chối"),
    CANCELLED("Đã hủy");

    private final String label;

    StockTransferStatus(String label) {
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
