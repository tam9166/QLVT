package com.qlvt.enums;

public enum ExpiryAlertLevel {
    EXPIRED("Đã hết hạn", "danger"),
    EXPIRING_30("Còn 1-30 ngày", "danger"),
    EXPIRING_60("Còn 31-60 ngày", "warning"),
    EXPIRING_90("Còn 61-90 ngày", "info"),
    NORMAL("Bình thường", "success");

    private final String label;
    private final String badgeClass;

    ExpiryAlertLevel(String label, String badgeClass) {
        this.label = label;
        this.badgeClass = badgeClass;
    }

    public String getLabel() { return label; }
    public String getBadgeClass() { return badgeClass; }
}
