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

    @Override
    public String toString() {
        return label;
    }
}
