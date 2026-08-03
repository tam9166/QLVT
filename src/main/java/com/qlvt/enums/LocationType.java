package com.qlvt.enums;

public enum LocationType {
    ROOM("Phòng"),
    SHELF("Kệ"),
    CABINET("Tủ"),
    DRAWER("Ngăn kéo"),
    BIN("Thùng");

    private final String label;

    LocationType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static String labelOf(String persistedName) {
        if (persistedName == null || persistedName.isBlank()) {
            return "-";
        }
        try {
            return valueOf(persistedName).getLabel();
        } catch (IllegalArgumentException exception) {
            return persistedName;
        }
    }

    public static LocationType fromPersistedName(String persistedName) {
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
