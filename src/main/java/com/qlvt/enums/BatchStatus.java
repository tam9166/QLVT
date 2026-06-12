package com.qlvt.enums;

public enum BatchStatus {
    AVAILABLE("Có thể cấp phát"),
    QUARANTINED("Cách ly"),
    EXPIRED("Hết hạn"),
    RECALLED("Đã thu hồi"),
    DESTROYED("Đã hủy");

    private final String label;

    BatchStatus(String label) {
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
