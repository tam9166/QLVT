package com.qlvt.config;

import com.qlvt.entity.*;
import com.qlvt.enums.UserRole;
import com.qlvt.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.context.annotation.Profile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.List;

@Component
@Profile("!prod")
@Order(1)
public class DataSeeder implements CommandLineRunner {
    private static final String TEMP_PASSWORD_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final AppUserRepository userRepository;
    private final MaterialRepository materialRepository;
    private final SupplierRepository supplierRepository;
    private final WarehouseRepository warehouseRepository;
    private final StorageLocationRepository locationRepository;
    private final DepartmentRepository departmentRepository;
    private final MaterialBatchRepository batchRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.qlvt.service.WarehouseWorkflowService warehouseWorkflowService;
    private final String demoDefaultPassword;

    public DataSeeder(AppUserRepository userRepository,
                      MaterialRepository materialRepository,
                      SupplierRepository supplierRepository,
                      WarehouseRepository warehouseRepository,
                      StorageLocationRepository locationRepository,
                      DepartmentRepository departmentRepository,
                      MaterialBatchRepository batchRepository,
                      NotificationRepository notificationRepository,
                      PasswordEncoder passwordEncoder,
                      com.qlvt.service.WarehouseWorkflowService warehouseWorkflowService,
                      @Value("${app.demo.default-password:}") String demoDefaultPassword) {
        this.userRepository = userRepository;
        this.materialRepository = materialRepository;
        this.supplierRepository = supplierRepository;
        this.warehouseRepository = warehouseRepository;
        this.locationRepository = locationRepository;
        this.departmentRepository = departmentRepository;
        this.batchRepository = batchRepository;
        this.notificationRepository = notificationRepository;
        this.passwordEncoder = passwordEncoder;
        this.warehouseWorkflowService = warehouseWorkflowService;
        this.demoDefaultPassword = demoDefaultPassword;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedDepartments();
        seedUsers();
        seedSuppliers();
        seedWarehouses();
        seedMaterialsAndBatches();
        warehouseWorkflowService.syncBalancesFromBatches();
        seedNotifications();
    }

    private void seedUsers() {
        createUser("admin", "Quản trị hệ thống", null, UserRole.ADMIN, "admin@qlvt.local", "0900000001");
        createUser("thukho", "Thủ kho chính", null, UserRole.WAREHOUSE_STAFF, "thukho@qlvt.local", "0900000002");
        createUser("truongkhoa", "Trưởng khoa Cấp cứu", "Khoa Cấp cứu", UserRole.DEPARTMENT_HEAD, "truongkhoa@qlvt.local", "0900000003");
        createUser("nhanvien", "Nhân viên Khoa Cấp cứu", "Khoa Cấp cứu", UserRole.DEPARTMENT_STAFF, "nhanvien@qlvt.local", "0900000004");
        createUser("nhanvien2", "Nhân viên Khoa Nội tổng hợp", "Khoa Nội tổng hợp", UserRole.DEPARTMENT_STAFF, "nhanvien2@qlvt.local", "0900000005");
        createUser("ketoan", "Kế toán vật tư", null, UserRole.ACCOUNTANT, "ketoan@qlvt.local", "0900000006");
        createUser("lanhdao", "Lãnh đạo bệnh viện", null, UserRole.MANAGER, "lanhdao@qlvt.local", "0900000007");
    }

    private void createUser(String username, String fullName, String department, UserRole role, String email, String phone) {
        AppUser user = userRepository.findByUsername(username).orElseGet(AppUser::new);
        user.setUsername(username);
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(seedPassword()));
        }
        user.setMustChangePassword(false);
        user.setFullName(fullName);
        user.setDepartment(department);
        user.setRole(role);
        user.setEmail(email);
        user.setPhone(phone);
        user.setEnabled(true);
        user.setLocked(false);
        user.setDeleted(false);
        userRepository.save(user);
    }

    private String seedPassword() {
        if (demoDefaultPassword != null && !demoDefaultPassword.isBlank()) {
            return demoDefaultPassword;
        }
        StringBuilder builder = new StringBuilder("Seed-");
        for (int i = 0; i < 16; i++) {
            builder.append(TEMP_PASSWORD_ALPHABET.charAt(SECURE_RANDOM.nextInt(TEMP_PASSWORD_ALPHABET.length())));
        }
        return builder.toString();
    }

    private void seedDepartments() {
        createDepartment("KCC", "Khoa Cấp cứu", "Tiếp nhận và xử trí cấp cứu ban đầu");
        createDepartment("KNT", "Khoa Nội tổng hợp", "Điều trị nội trú tổng hợp");
        createDepartment("KXN", "Khoa Xét nghiệm", "Thực hiện xét nghiệm, sinh phẩm và chẩn đoán");
    }

    private void createDepartment(String code, String name, String description) {
        Department department = departmentRepository.findByCode(code).orElseGet(Department::new);
        department.setCode(code);
        department.setName(name);
        department.setDescription(description);
        department.setActive(true);
        department.setDeleted(false);
        departmentRepository.save(department);
    }

    private void seedSuppliers() {
        createSupplier("NCC001", "Công ty Thiết bị Y tế An Khang", "0909123456");
        createSupplier("NCC002", "Công ty Dược phẩm Minh Tâm", "0911222333");
        createSupplier("NCC003", "Công ty Vật tư Sức khỏe Việt", "0988333444");
        createSupplier("NCC004", "Công ty Thiết bị Y tế Bình An", "0977000111");
        createSupplier("NCC005", "Công ty Sinh phẩm Ánh Dương", "0966000222");
        createSupplier("NCC006", "Công ty Dụng cụ Y khoa Phương Nam", "0955000333");
        createSupplier("NCC007", "Công ty Vật tư Vô khuẩn Hòa Bình", "0944000444");
        createSupplier("NCC008", "Công ty Thiết bị Chẩn đoán An Việt", "0933000555");
        createSupplier("NCC009", "Công ty Dược phẩm và Sinh phẩm Đông Á", "0922000666");
        createSupplier("NCC010", "Công ty Kho vận Y tế Thành Công", "0911000777");
    }

    private void createSupplier(String code, String name, String phone) {
        if (supplierRepository.existsByCode(code)) {
            return;
        }
        Supplier supplier = new Supplier();
        supplier.setCode(code);
        supplier.setName(name);
        supplier.setPhone(phone);
        supplier.setEmail(code.toLowerCase() + "@demo.vn");
        supplierRepository.save(supplier);
    }

    private void seedWarehouses() {
        Warehouse main = createWarehouse("KHO001", "Kho vật tư trung tâm", "MAIN");
        Warehouse emergency = createWarehouse("KHO002", "Kho cấp cứu", "EMERGENCY");
        Warehouse cold = createWarehouse("KHO003", "Kho lạnh sinh phẩm", "COLD");
        Warehouse surgery = createWarehouse("KHO004", "Kho phòng mổ", "SURGERY");
        Warehouse lab = createWarehouse("KHO005", "Kho xét nghiệm", "LAB");

        createLocation(main, "KHO001-A1", "Kệ A1", "SHELF");
        createLocation(main, "KHO001-B1", "Tủ B1", "CABINET");
        createLocation(main, "KHO001-P1", "Phòng vật tư vô khuẩn", "ROOM");
        createLocation(emergency, "KHO002-C1", "Ngăn C1", "DRAWER");
        createLocation(emergency, "KHO002-BIN1", "Thùng cấp cứu nhanh", "BIN");
        for (int i = 1; i <= 6; i++) {
            createLocation(main, "KHO001-A" + (i + 1), "Kệ A" + (i + 1), "SHELF");
            createLocation(cold, "KHO003-L" + i, "Ngăn lạnh L" + i, "COLD_BIN");
            createLocation(surgery, "KHO004-M" + i, "Kệ phòng mổ M" + i, "SHELF");
            createLocation(lab, "KHO005-XN" + i, "Ngăn xét nghiệm XN" + i, "DRAWER");
        }
    }
    private Warehouse createWarehouse(String code, String name, String type) {
        return warehouseRepository.findByCode(code).map(existing -> {
            existing.setName(name);
            existing.setType(type);
            return warehouseRepository.save(existing);
        }).orElseGet(() -> {
            Warehouse warehouse = new Warehouse();
            warehouse.setCode(code);
            warehouse.setName(name);
            warehouse.setType(type);
            return warehouseRepository.save(warehouse);
        });
    }

    private void createLocation(Warehouse warehouse, String code, String name, String type) {
        StorageLocation existing = locationRepository.findByCode(code).orElse(null);
        if (existing != null) {
            existing.setWarehouse(warehouse);
            existing.setName(name);
            existing.setLocationType(type);
            existing.setActive(true);
            existing.setDeleted(false);
            locationRepository.save(existing);
            return;
        }
        StorageLocation location = new StorageLocation();
        location.setWarehouse(warehouse);
        location.setCode(code);
        location.setName(name);
        location.setLocationType(type);
        locationRepository.save(location);
    }

    private void seedMaterialsAndBatches() {
        List<String[]> rows = List.of(
                new String[]{"VT001", "Khẩu trang y tế 4 lớp", "Vật tư tiêu hao", "Hộp", "50 cái/hộp", "180", "30", "65000"},
                new String[]{"VT002", "Găng tay y tế không bột size M", "Vật tư tiêu hao", "Hộp", "100 cái/hộp", "75", "40", "42000"},
                new String[]{"VT003", "Bơm tiêm 5ml", "Vật tư tiêu hao", "Cái", "Túi 100 cái", "320", "100", "3500"},
                new String[]{"VT004", "Cồn sát khuẩn 70 độ", "Dung dịch sát khuẩn", "Chai", "500ml/chai", "60", "25", "28000"},
                new String[]{"VT005", "Gạc vô khuẩn 10x10", "Vật tư tiêu hao", "Gói", "10 miếng/gói", "130", "45", "14500"},
                new String[]{"VT006", "Dung dịch Natri Clorid 0,9%", "Dịch truyền", "Chai", "500ml/chai", "90", "35", "18000"},
                new String[]{"VT007", "Kim luồn tĩnh mạch", "Dụng cụ tiêm truyền", "Cái", "Hộp 50 cái", "58", "25", "11000"},
                new String[]{"VT008", "Que thử đường huyết", "Vật tư xét nghiệm", "Hộp", "50 que/hộp", "68", "20", "175000"},
                new String[]{"VT009", "Băng cuộn y tế", "Vật tư băng bó", "Cuộn", "1 cuộn/gói", "150", "40", "10000"},
                new String[]{"VT010", "Bông gòn tiệt trùng", "Vật tư băng bó", "Gói", "100g/gói", "95", "30", "12000"},
                new String[]{"VT011", "Dây truyền dịch", "Dụng cụ tiêm truyền", "Bộ", "1 bộ/túi", "140", "45", "7500"},
                new String[]{"VT012", "Kim tiêm 23G", "Dụng cụ tiêm truyền", "Cái", "Hộp 100 cái", "500", "120", "1800"},
                new String[]{"VT013", "Ống nghiệm EDTA", "Vật tư xét nghiệm", "Ống", "Khay 100 ống", "260", "80", "2200"},
                new String[]{"VT014", "Ống nghiệm sinh hóa", "Vật tư xét nghiệm", "Ống", "Khay 100 ống", "240", "80", "2000"},
                new String[]{"VT015", "Lam kính xét nghiệm", "Vật tư xét nghiệm", "Hộp", "50 lam/hộp", "70", "20", "32000"},
                new String[]{"VT016", "Lưỡi dao mổ số 11", "Dụng cụ phẫu thuật", "Cái", "Hộp 100 cái", "120", "30", "4500"},
                new String[]{"VT017", "Lưỡi dao mổ số 15", "Dụng cụ phẫu thuật", "Cái", "Hộp 100 cái", "115", "30", "4500"},
                new String[]{"VT018", "Chỉ khâu tự tiêu 3/0", "Vật tư phẫu thuật", "Sợi", "12 sợi/hộp", "48", "15", "58000"},
                new String[]{"VT019", "Chỉ khâu không tiêu 2/0", "Vật tư phẫu thuật", "Sợi", "12 sợi/hộp", "52", "15", "52000"},
                new String[]{"VT020", "Bộ test cúm nhanh", "Sinh phẩm chẩn đoán", "Bộ", "20 bộ/hộp", "35", "10", "92000"},
                new String[]{"VT021", "Bộ test COVID-19 nhanh", "Sinh phẩm chẩn đoán", "Bộ", "25 bộ/hộp", "80", "25", "68000"},
                new String[]{"VT022", "Nhiệt kế điện tử", "Thiết bị y tế nhỏ", "Cái", "1 cái/hộp", "20", "5", "250000"},
                new String[]{"VT023", "Máy đo SpO2 cầm tay", "Thiết bị y tế nhỏ", "Cái", "1 cái/hộp", "12", "4", "650000"},
                new String[]{"VT024", "Máy đo huyết áp điện tử", "Thiết bị y tế nhỏ", "Cái", "1 cái/hộp", "10", "3", "850000"},
                new String[]{"VT025", "Túi nước tiểu", "Vật tư chăm sóc người bệnh", "Cái", "Túi 10 cái", "110", "35", "9500"},
                new String[]{"VT026", "Ống thông tiểu Foley", "Vật tư chăm sóc người bệnh", "Cái", "Hộp 10 cái", "65", "20", "28000"},
                new String[]{"VT027", "Ống hút đờm", "Vật tư hô hấp", "Cái", "Túi 50 cái", "160", "50", "3200"},
                new String[]{"VT028", "Mặt nạ khí dung người lớn", "Vật tư hô hấp", "Cái", "1 cái/túi", "45", "15", "35000"},
                new String[]{"VT029", "Mặt nạ oxy người lớn", "Vật tư hô hấp", "Cái", "1 cái/túi", "55", "20", "22000"},
                new String[]{"VT030", "Dây thở oxy hai nhánh", "Vật tư hô hấp", "Cái", "1 cái/túi", "90", "30", "12000"},
                new String[]{"VT031", "Dung dịch sát khuẩn tay nhanh", "Dung dịch sát khuẩn", "Chai", "500ml/chai", "85", "25", "52000"},
                new String[]{"VT032", "Dung dịch Povidone Iodine 10%", "Dung dịch sát khuẩn", "Chai", "500ml/chai", "42", "15", "48000"},
                new String[]{"VT033", "Nước cất y tế", "Dung dịch y tế", "Chai", "500ml/chai", "95", "30", "9000"},
                new String[]{"VT034", "Dung dịch Glucose 5%", "Dịch truyền", "Chai", "500ml/chai", "88", "30", "17000"},
                new String[]{"VT035", "Dung dịch Ringer Lactate", "Dịch truyền", "Chai", "500ml/chai", "76", "25", "19000"},
                new String[]{"VT036", "Áo choàng phẫu thuật vô khuẩn", "Đồ bảo hộ y tế", "Cái", "1 cái/túi", "60", "20", "78000"},
                new String[]{"VT037", "Mũ trùm đầu y tế", "Đồ bảo hộ y tế", "Cái", "Túi 100 cái", "300", "100", "1200"},
                new String[]{"VT038", "Kính chắn giọt bắn", "Đồ bảo hộ y tế", "Cái", "1 cái/túi", "70", "20", "26000"},
                new String[]{"VT039", "Bao giày y tế", "Đồ bảo hộ y tế", "Đôi", "Túi 100 đôi", "240", "80", "1800"},
                new String[]{"VT040", "Khăn lau khử khuẩn bề mặt", "Dung dịch sát khuẩn", "Gói", "80 tờ/gói", "65", "20", "43000"},
                new String[]{"VT041", "Túi đựng rác y tế màu vàng", "Vật tư kiểm soát nhiễm khuẩn", "Cuộn", "50 túi/cuộn", "75", "25", "36000"},
                new String[]{"VT042", "Hộp an toàn đựng vật sắc nhọn", "Vật tư kiểm soát nhiễm khuẩn", "Hộp", "5 lít/hộp", "40", "12", "42000"},
                new String[]{"VT043", "Băng dính y tế lụa", "Vật tư băng bó", "Cuộn", "1,25cm x 9m", "180", "50", "8500"},
                new String[]{"VT044", "Băng dính y tế giấy", "Vật tư băng bó", "Cuộn", "2,5cm x 9m", "160", "50", "7800"},
                new String[]{"VT045", "Miếng dán điện cực ECG", "Vật tư chẩn đoán hình ảnh", "Miếng", "Túi 50 miếng", "220", "70", "2500"},
                new String[]{"VT046", "Gel siêu âm", "Vật tư chẩn đoán hình ảnh", "Chai", "250ml/chai", "36", "12", "32000"},
                new String[]{"VT047", "Giấy in điện tim", "Vật tư chẩn đoán hình ảnh", "Cuộn", "80mm x 20m", "50", "15", "28000"},
                new String[]{"VT048", "Giấy in siêu âm", "Vật tư chẩn đoán hình ảnh", "Cuộn", "110mm x 20m", "34", "10", "76000"},
                new String[]{"VT049", "Bộ đặt nội khí quản", "Vật tư hô hấp", "Bộ", "1 bộ/hộp", "18", "6", "185000"},
                new String[]{"VT050", "Canuyn miệng hầu", "Vật tư hô hấp", "Cái", "Hộp 10 cái", "44", "12", "26000"}
        );
        Warehouse warehouse = warehouseRepository.findByCode("KHO001").orElseThrow();
        StorageLocation location = locationRepository.findByCode("KHO001-A1").orElseThrow();
        Supplier supplier = supplierRepository.findByCode("NCC001").orElseThrow();
        LocalDate today = LocalDate.now();

        int index = 0;
        for (String[] row : rows) {
            Material material = materialRepository.findByCode(row[0]).orElseGet(() -> {
                Material created = new Material();
                created.setCode(row[0]);
                created.setActualQuantity(Integer.parseInt(row[5]));
                return created;
            });
            material.setName(row[1]);
            material.setAliasText(row[1].toLowerCase().replace(" ", ""));
            material.setCategory(row[2]);
            material.setUnit(row[3]);
            material.setPackageSpec(row[4]);
            material.setMinStock(Integer.parseInt(row[6]));
            material.setMaxStock(Integer.parseInt(row[5]) * 3);
            material.setEstimatedUnitPrice(new BigDecimal(row[7]));
            material.setStorageCondition("Kho mát, khô ráo, tránh ánh nắng trực tiếp");
            materialRepository.save(material);

            String batchNumber = "LO-" + row[0] + "-01";
            if (batchRepository.findByMaterial_IdAndBatchNumber(material.getId(), batchNumber).isEmpty()) {
                MaterialBatch batch = new MaterialBatch();
                batch.setMaterial(material);
                batch.setWarehouse(warehouse);
                batch.setLocation(location);
                batch.setSupplier(supplier);
                batch.setBatchNumber(batchNumber);
                batch.setManufactureDate(today.minusDays(60 + index));
                batch.setExpiryDate(today.plusDays(45 + index * 20L));
                batch.setReceiptDate(today.minusDays(10 + index));
                batch.setQuantity(material.getActualQuantity());
                batchRepository.save(batch);
            }
            index++;
        }
    }

    private void seedNotifications() {
        if (notificationRepository.count() > 0) {
            return;
        }
        Notification notification = new Notification();
        notification.setTitle("Cảnh báo tồn kho");
        notification.setContent("Một số vật tư đang gần mức tồn tối thiểu, cần theo dõi bổ sung.");
        notification.setType("LOW_STOCK");
        notification.setReceiver("WAREHOUSE_STAFF");
        notification.setLink("/materials");
        notificationRepository.save(notification);
    }
}
