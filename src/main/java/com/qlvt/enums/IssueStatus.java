package com.qlvt.enums;

public enum IssueStatus {
    DRAFT("Nháp"),
    PREPARING("Đang chuẩn bị"),
    ISSUED("Đã xuất kho"),
    RECEIVED("Đã nhận"),
    CANCELLED("Đã hủy");

    private final String label;

    IssueStatus(String label) {
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
