package com.qlvt.enums;

public enum DestructionStatus {
    DRAFT("Nháp"),
    SUBMITTED("Đã gửi duyệt"),
    APPROVED_BY_MANAGER("Quản lý đã duyệt"),
    APPROVED_BY_ACCOUNTANT("Kế toán đã duyệt"),
    APPROVED("Đã duyệt"),
    REJECTED("Đã từ chối"),
    DESTROYED("Đã hủy vật tư"),
    CANCELLED("Đã hủy phiếu");

    private final String label;

    DestructionStatus(String label) {
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
