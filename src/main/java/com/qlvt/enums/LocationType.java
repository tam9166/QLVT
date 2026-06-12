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

    @Override
    public String toString() {
        return label;
    }
}
