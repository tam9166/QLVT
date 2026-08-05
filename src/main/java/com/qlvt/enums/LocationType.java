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
        LocationType type = fromPersistedName(persistedName);
        return type == null ? persistedName : type.getLabel();
    }

    public static LocationType fromPersistedName(String persistedName) {
        if (persistedName == null || persistedName.isBlank()) {
            return null;
        }
        for (LocationType type : values()) {
            if (type.name().equalsIgnoreCase(persistedName.trim())) {
                return type;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return label;
    }
}
