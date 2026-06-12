package com.qlvt;

import com.qlvt.enums.UserRole;
import com.qlvt.util.RoleUtils;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityAndSeedRulesTest {
    @Test
    void passwordIsEncodedWithBCrypt() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encoded = encoder.encode("123456");

        assertThat(encoded).isNotEqualTo("123456");
        assertThat(encoder.matches("123456", encoded)).isTrue();
    }

    @Test
    void roleEnumContainsRequiredRoles() {
        assertThat(Arrays.asList(UserRole.values()))
                .contains(UserRole.ADMIN, UserRole.DEPARTMENT_STAFF, UserRole.DEPARTMENT_HEAD,
                        UserRole.WAREHOUSE_STAFF, UserRole.PROCUREMENT, UserRole.ACCOUNTANT, UserRole.MANAGER);
    }

    @Test
    void departmentRolesRequireDepartmentAssignment() {
        assertThat(RoleUtils.requiresDepartment(UserRole.DEPARTMENT_STAFF)).isTrue();
        assertThat(RoleUtils.requiresDepartment(UserRole.DEPARTMENT_HEAD)).isTrue();
        assertThat(RoleUtils.requiresDepartment(UserRole.ADMIN)).isFalse();
    }
}
