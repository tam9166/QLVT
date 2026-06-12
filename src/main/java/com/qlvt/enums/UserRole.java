package com.qlvt.enums;

public enum UserRole {
    ADMIN("Quản trị viên"),
    DEPARTMENT_STAFF("Nhân viên khoa"),
    DEPARTMENT_HEAD("Trưởng khoa"),
    WAREHOUSE_STAFF("Nhân viên kho"),
    PROCUREMENT("Mua sắm"),
    ACCOUNTANT("Kế toán"),
    MANAGER("Lãnh đạo");

    private final String label;

    UserRole(String label) {
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
