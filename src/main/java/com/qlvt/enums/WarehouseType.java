package com.qlvt.enums;

public enum WarehouseType {
    MAIN("Kho chính"),
    EMERGENCY("Kho cấp cứu"),
    DEPARTMENT("Kho khoa/phòng"),
    BACKUP("Kho dự phòng"),
    QUARANTINE("Kho cách ly");

    private final String label;

    WarehouseType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static String labelOf(String persistedName) {
        if (persistedName == null || persistedName.isBlank()) {
            return "-";
        }
        WarehouseType type = fromPersistedName(persistedName);
        return type == null ? persistedName : type.getLabel();
    }

    public static WarehouseType fromPersistedName(String persistedName) {
        if (persistedName == null || persistedName.isBlank()) {
            return null;
        }
        try {
            return valueOf(persistedName);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    @Override
    public String toString() {
        return label;
    }
}
