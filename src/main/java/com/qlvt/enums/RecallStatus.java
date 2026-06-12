package com.qlvt.enums;

public enum RecallStatus {
    DRAFT("Nháp"),
    ACTIVE("Đang thu hồi"),
    COMPLETED("Đã hoàn tất"),
    CANCELLED("Đã hủy");

    private final String label;

    RecallStatus(String label) {
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
