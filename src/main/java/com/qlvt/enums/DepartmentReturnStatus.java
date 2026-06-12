package com.qlvt.enums;

public enum DepartmentReturnStatus {
    DRAFT("Nháp"),
    SUBMITTED("Đã gửi"),
    RECEIVED_BY_WAREHOUSE("Kho đã nhận"),
    REJECTED("Đã từ chối"),
    CANCELLED("Đã hủy");

    private final String label;

    DepartmentReturnStatus(String label) {
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
