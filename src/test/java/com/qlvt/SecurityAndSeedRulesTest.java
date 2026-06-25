package com.qlvt;

import com.qlvt.entity.AppUser;
import com.qlvt.enums.UserRole;
import com.qlvt.service.UserAdminService;
import com.qlvt.util.RoleUtils;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
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

    @Test
    void userEntityDoesNotExposeVisiblePassword() {
        assertThat(Arrays.stream(AppUser.class.getDeclaredFields()).map(Field::getName))
                .doesNotContain("visiblePassword");
    }

    @Test
    void seedSqlDoesNotStoreVisiblePassword() throws Exception {
        String schema = Files.readString(Path.of("database/01_create_schema.sql"));
        String seed = Files.readString(Path.of("database/02_seed_master_data.sql"));

        assertThat(schema).doesNotContain("visible_password");
        assertThat(seed).doesNotContain("visible_password");
        assertThat(seed).doesNotContain("N'123456'");
    }

    @Test
    void resetPasswordDoesNotExposeDefaultPasswordConstant() {
        assertThat(Arrays.stream(UserAdminService.class.getDeclaredFields()).map(Field::getName))
                .doesNotContain("DEFAULT_RESET_PASSWORD");
    }

    @Test
    void userFacingVietnameseFilesDoNotContainMojibake() throws Exception {
        for (String file : new String[] {
                "src/main/resources/templates/users/list.html",
                "src/main/java/com/qlvt/service/ChatbotService.java",
                "database/03_seed_demo_data.sql",
                "README.md"
        }) {
            String content = Files.readString(Path.of(file));
            assertThat(content)
                    .as(file)
                    .doesNotContain("Ã", "Â", "Ä", "Å", "Æ", "Ð", "áº", "á»", "ï¿½", "\uFFFD");
        }
    }
}
