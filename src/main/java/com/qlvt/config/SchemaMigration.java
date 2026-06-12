package com.qlvt.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(0)
public class SchemaMigration implements CommandLineRunner {
    private final JdbcTemplate jdbcTemplate;

    public SchemaMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        addBit("users", "locked", "0");
        addBit("users", "deleted", "0");
        addDateTime("users", "created_at");
        addDateTime("users", "updated_at");
        ensureNvarchar("users", "full_name", 150);
        ensureNvarchar("users", "department", 120);
        addNvarchar("users", "visible_password", 120);
        if (columnExists("users", "visible_password")) {
            jdbcTemplate.execute("UPDATE users SET visible_password = '123456' WHERE visible_password IS NULL OR visible_password = ''");
        }
        addNvarchar("users", "email", 160);
        addVarchar("users", "phone", 30);

        ensureNvarchar("departments", "name", 160);
        ensureNvarchar("departments", "description", 500);

        addBit("warehouses", "deleted", "0");
        addDateTime("warehouses", "created_at");
        addDateTime("warehouses", "updated_at");
        ensureNvarchar("warehouses", "name", 160);
        addNvarchar("warehouses", "description", 500);

        addBit("storage_locations", "deleted", "0");
        addDateTime("storage_locations", "created_at");
        addDateTime("storage_locations", "updated_at");
        ensureNvarchar("storage_locations", "name", 160);
        addNvarchar("storage_locations", "description", 500);

        ensureNvarchar("materials", "name", 220);
        ensureNvarchar("materials", "category", 120);
        ensureNvarchar("materials", "unit", 60);
        ensureNvarchar("materials", "specification", 500);
        ensureNvarchar("materials", "storage_condition", 500);

        ensureNvarchar("material_requests", "department", 160);
        ensureNvarchar("material_requests", "requester", 120);
        ensureNvarchar("material_requests", "priority", 80);
        ensureNvarchar("material_requests", "note", 1000);
        ensureNvarchar("material_requests", "department_approved_by", 120);
        ensureNvarchar("material_requests", "warehouse_approved_by", 120);
        ensureNvarchar("material_requests", "received_by", 120);

        ensureNvarchar("issue_slips", "department", 160);
        ensureNvarchar("issue_slips", "received_by", 120);
        ensureNvarchar("department_stocks", "department", 160);
        ensureNvarchar("department_returns", "department", 160);
        ensureNvarchar("recall_order_lines", "department", 160);
        ensureNvarchar("recall_department_responses", "department", 160);

        addNvarchar("audit_logs", "actor_username", 255);
        addNvarchar("audit_logs", "entity_name", 255);
        addNvarchar("audit_logs", "entity_id", 255);
        addNvarchar("audit_logs", "old_value", 1500);
        addNvarchar("audit_logs", "new_value", 1500);
        addNvarchar("audit_logs", "ip_address", 80);

        normalizeVietnameseDepartmentNames();
        normalizeVersionColumns();
        createStorageMonitoringTables();
    }

    private void addBit(String table, String column, String defaultValue) {
        executeIfMissing(table, column, "ALTER TABLE " + table + " ADD " + column + " bit NOT NULL CONSTRAINT DF_" + table + "_" + column + " DEFAULT " + defaultValue);
    }

    private void addDateTime(String table, String column) {
        executeIfMissing(table, column, "ALTER TABLE " + table + " ADD " + column + " datetime2 NULL");
        jdbcTemplate.execute("UPDATE " + table + " SET " + column + " = SYSUTCDATETIME() WHERE " + column + " IS NULL");
    }

    private void addNvarchar(String table, String column, int length) {
        executeIfMissing(table, column, "ALTER TABLE " + table + " ADD " + column + " nvarchar(" + length + ") NULL");
    }

    private void addVarchar(String table, String column, int length) {
        executeIfMissing(table, column, "ALTER TABLE " + table + " ADD " + column + " varchar(" + length + ") NULL");
    }

    private void ensureNvarchar(String table, String column, int length) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM sys.columns c
                JOIN sys.types t ON c.user_type_id = t.user_type_id
                WHERE c.object_id = OBJECT_ID(?)
                  AND c.name = ?
                  AND t.name IN ('varchar', 'char', 'text')
                """, Integer.class, table, column);
        if (count != null && count > 0) {
            jdbcTemplate.execute("ALTER TABLE " + table + " ALTER COLUMN " + column + " nvarchar(" + length + ") NULL");
        }
    }

    private void normalizeVietnameseDepartmentNames() {
        String[][] values = {
                {"Khoa C?p c?u", "Khoa Cấp cứu"},
                {"Khoa C?p cứu", "Khoa Cấp cứu"},
                {"Khoa Cap cuu", "Khoa Cấp cứu"},
                {"Khoa Cấp cứu", "Khoa Cấp cứu"},
                {"Khoa N?i t?ng h?p", "Khoa Nội tổng hợp"},
                {"Khoa Noi tong hop", "Khoa Nội tổng hợp"},
                {"Khoa X?t nghi?m", "Khoa Xét nghiệm"},
                {"Khoa Xet nghiem", "Khoa Xét nghiệm"},
                {"Khoa Ngo?i", "Khoa Ngoại"},
                {"Khoa Ngoai", "Khoa Ngoại"},
                {"Khoa Ph?u thu?t", "Khoa Phẫu thuật"},
                {"Khoa Phau thuat", "Khoa Phẫu thuật"},
                {"Khoa H?i s?c tich c?c", "Khoa Hồi sức tích cực"},
                {"Khoa Hoi suc tich cuc", "Khoa Hồi sức tích cực"},
                {"Khoa Nhi", "Khoa Nhi"},
                {"Khoa D??c", "Khoa Dược"},
                {"Khoa Duoc", "Khoa Dược"}
        };
        for (String[] value : values) {
            normalizeValue("departments", "name", value[0], value[1]);
            normalizeValue("users", "department", value[0], value[1]);
            normalizeValue("material_requests", "department", value[0], value[1]);
            normalizeValue("issue_slips", "department", value[0], value[1]);
            normalizeValue("department_stocks", "department", value[0], value[1]);
            normalizeValue("department_returns", "department", value[0], value[1]);
            normalizeValue("recall_order_lines", "department", value[0], value[1]);
            normalizeValue("recall_department_responses", "department", value[0], value[1]);
        }
    }

    private void normalizeValue(String table, String column, String badValue, String goodValue) {
        if (columnExists(table, column)) {
            jdbcTemplate.update("UPDATE " + table + " SET " + column + " = ? WHERE " + column + " = ?", goodValue, badValue);
        }
    }

    private void normalizeVersionColumns() {
        setNullVersionToZero("materials");
        setNullVersionToZero("material_batches");
        setNullVersionToZero("department_stocks");
        setNullVersionToZero("stock_balances");
        setNullVersionToZero("stock_reservations");
    }

    private void setNullVersionToZero(String table) {
        if (columnExists(table, "version")) {
            jdbcTemplate.execute("UPDATE " + table + " SET version = 0 WHERE version IS NULL");
        }
    }

    private void createStorageMonitoringTables() {
        if (!tableExists("storage_conditions")) {
            jdbcTemplate.execute("""
                    CREATE TABLE storage_conditions (
                        id bigint IDENTITY(1,1) PRIMARY KEY,
                        material_id bigint NOT NULL,
                        min_temperature decimal(10,2) NULL,
                        max_temperature decimal(10,2) NULL,
                        min_humidity decimal(10,2) NULL,
                        max_humidity decimal(10,2) NULL,
                        light_sensitive bit NOT NULL CONSTRAINT DF_storage_conditions_light_sensitive DEFAULT 0,
                        cold_chain_required bit NOT NULL CONSTRAINT DF_storage_conditions_cold_chain DEFAULT 0,
                        note nvarchar(700) NULL,
                        created_at datetime2 NULL,
                        updated_at datetime2 NULL
                    )
                    """);
            jdbcTemplate.execute("CREATE UNIQUE INDEX uk_storage_condition_material ON storage_conditions(material_id)");
        }
        if (!tableExists("temperature_logs")) {
            jdbcTemplate.execute("""
                    CREATE TABLE temperature_logs (
                        id bigint IDENTITY(1,1) PRIMARY KEY,
                        warehouse_id bigint NOT NULL,
                        recorded_at datetime2 NULL,
                        temperature decimal(10,2) NULL,
                        humidity decimal(10,2) NULL,
                        recorded_by nvarchar(120) NULL,
                        status varchar(30) NULL,
                        note nvarchar(700) NULL
                    )
                    """);
            jdbcTemplate.execute("CREATE INDEX idx_temperature_logs_warehouse ON temperature_logs(warehouse_id)");
            jdbcTemplate.execute("CREATE INDEX idx_temperature_logs_recorded ON temperature_logs(recorded_at)");
        }
    }

    private void executeIfMissing(String table, String column, String ddl) {
        if (!tableExists(table)) {
            return;
        }
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM sys.columns
                WHERE object_id = OBJECT_ID(?)
                  AND name = ?
                """, Integer.class, table, column);
        if (count == null || count == 0) {
            jdbcTemplate.execute(ddl);
        }
    }

    private boolean tableExists(String table) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys.objects WHERE object_id = OBJECT_ID(?) AND type = 'U'", Integer.class, table);
        return count != null && count > 0;
    }

    private boolean columnExists(String table, String column) {
        if (!tableExists(table)) {
            return false;
        }
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM sys.columns
                WHERE object_id = OBJECT_ID(?)
                  AND name = ?
                """, Integer.class, table, column);
        return count != null && count > 0;
    }
}
