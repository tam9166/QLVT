package com.qlvt.enums;

public enum StockAlertLevel {
    OUT_OF_STOCK("Hết hàng", "danger"),
    CRITICAL_LOW("Tồn rất thấp", "danger"),
    LOW("Tồn thấp", "warning"),
    NORMAL("Bình thường", "success"),
    OVER_STOCK("Vượt tồn tối đa", "info");

    private final String label;
    private final String badgeClass;

    StockAlertLevel(String label, String badgeClass) {
        this.label = label;
        this.badgeClass = badgeClass;
    }

    public String getLabel() { return label; }
    public String getBadgeClass() { return badgeClass; }
}
