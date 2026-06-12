package com.qlvt.enums;

public enum RequestStatus {
    DRAFT("Nháp"),
    SUBMITTED("Đã gửi"),
    DEPARTMENT_APPROVED("Chờ kho xử lý"),
    DEPARTMENT_REJECTED("Trưởng khoa từ chối"),
    WAREHOUSE_APPROVED("Kho đã duyệt"),
    PARTIALLY_APPROVED("Duyệt một phần"),
    WAREHOUSE_REJECTED("Kho từ chối"),
    RESERVED("Đã giữ hàng"),
    PREPARING("Đang chuẩn bị"),
    ISSUED("Đã xuất kho"),
    RECEIVED("Đã nhận"),
    CANCELLED("Đã hủy");

    private final String label;

    RequestStatus(String label) {
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
