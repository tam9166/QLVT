package com.qlvt.util;

import com.qlvt.enums.UserRole;

public final class RoleUtils {
    private RoleUtils() {
    }

    public static boolean requiresDepartment(UserRole role) {
        return role == UserRole.DEPARTMENT_STAFF || role == UserRole.DEPARTMENT_HEAD;
    }
}
