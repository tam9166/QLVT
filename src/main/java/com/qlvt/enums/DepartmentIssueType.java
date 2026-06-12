package com.qlvt.enums;

public enum DepartmentIssueType {
    DAMAGE("Hỏng"),
    LOST("Mất"),
    EXPIRED_AT_DEPARTMENT("Hết hạn tại khoa"),
    OTHER("Khác");

    private final String label;

    DepartmentIssueType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
