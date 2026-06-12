package com.qlvt.config;

import com.qlvt.entity.*;
import com.qlvt.enums.*;
import com.qlvt.repository.*;
import com.qlvt.service.WarehouseWorkflowService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Order(2)
public class DemoPageDataSeeder implements CommandLineRunner {
    private final MaterialRepository materialRepository;
    private final MaterialBatchRepository batchRepository;
    private final SupplierRepository supplierRepository;
    private final WarehouseRepository warehouseRepository;
    private final StorageLocationRepository locationRepository;
    private final MaterialRequestRepository requestRepository;
    private final ReceiptRepository receiptRepository;
    private final IssueSlipRepository issueSlipRepository;
    private final DepartmentStockRepository departmentStockRepository;
    private final DepartmentReturnRepository departmentReturnRepository;
    private final InventoryCountRepository inventoryCountRepository;
    private final NotificationRepository notificationRepository;
    private final WarehouseWorkflowService warehouseWorkflowService;

    public DemoPageDataSeeder(MaterialRepository materialRepository,
                              MaterialBatchRepository batchRepository,
                              SupplierRepository supplierRepository,
                              WarehouseRepository warehouseRepository,
                              StorageLocationRepository locationRepository,
                              MaterialRequestRepository requestRepository,
                              ReceiptRepository receiptRepository,
                              IssueSlipRepository issueSlipRepository,
                              DepartmentStockRepository departmentStockRepository,
                              DepartmentReturnRepository departmentReturnRepository,
                              InventoryCountRepository inventoryCountRepository,
                              NotificationRepository notificationRepository,
                              WarehouseWorkflowService warehouseWorkflowService) {
        this.materialRepository = materialRepository;
        this.batchRepository = batchRepository;
        this.supplierRepository = supplierRepository;
        this.warehouseRepository = warehouseRepository;
        this.locationRepository = locationRepository;
        this.requestRepository = requestRepository;
        this.receiptRepository = receiptRepository;
        this.issueSlipRepository = issueSlipRepository;
        this.departmentStockRepository = departmentStockRepository;
        this.departmentReturnRepository = departmentReturnRepository;
        this.inventoryCountRepository = inventoryCountRepository;
        this.notificationRepository = notificationRepository;
        this.warehouseWorkflowService = warehouseWorkflowService;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Warehouse warehouse = warehouseRepository.findByCode("KHO001").orElseThrow();
        StorageLocation location = locationRepository.findByCode("KHO001-A1").orElseThrow();
        Supplier supplier = supplierRepository.findByCode("NCC001").orElseThrow();

        Material gloves = material("VT002", "Găng tay y tế không bột size M", "Vật tư tiêu hao", "Hộp", 180, 40, new BigDecimal("42000"));
        Material mask = material("VT001", "Khẩu trang y tế 4 lớp", "Vật tư tiêu hao", "Hộp", 240, 30, new BigDecimal("65000"));
        Material saline = material("VT006", "Dung dịch Natri Clorid 0,9%", "Dịch truyền", "Chai", 120, 35, new BigDecimal("18000"));
        Material gauze = material("VT005", "Gạc vô khuẩn 10x10", "Vật tư tiêu hao", "Gói", 160, 45, new BigDecimal("14500"));

        MaterialBatch glovesBatch = batch(gloves, warehouse, location, supplier, "DEMO-LO-VT002-01", 160, LocalDate.now().plusMonths(10));
        MaterialBatch maskBatch = batch(mask, warehouse, location, supplier, "DEMO-LO-VT001-01", 220, LocalDate.now().plusMonths(8));
        MaterialBatch salineBatch = batch(saline, warehouse, location, supplier, "DEMO-LO-VT006-01", 100, LocalDate.now().plusMonths(6));
        MaterialBatch gauzeBatch = batch(gauze, warehouse, location, supplier, "DEMO-LO-VT005-01", 140, LocalDate.now().plusMonths(11));

        seedRequests(gloves, mask, saline);
        seedReceipt(warehouse, location, supplier, gloves, mask, saline);
        seedIssue(warehouse, location, gloves, mask, glovesBatch, maskBatch);
        seedDepartmentStock(gloves, mask, saline, glovesBatch, maskBatch, salineBatch);
        seedDepartmentReturn(warehouse, location, gloves, glovesBatch);
        seedInventoryCount(warehouse, location, gloves, mask, saline, gauze, glovesBatch, maskBatch, salineBatch, gauzeBatch);
        seedNotifications();

        warehouseWorkflowService.syncBalancesFromBatches();
    }

    private Material material(String code, String name, String category, String unit, int actualQuantity, int minStock, BigDecimal price) {
        Material material = materialRepository.findByCode(code).orElseGet(Material::new);
        material.setCode(code);
        material.setName(name);
        material.setAliasText(name.toLowerCase());
        material.setCategory(category);
        material.setUnit(unit);
        material.setPackageSpec("Dữ liệu mẫu");
        material.setStorageCondition("Bảo quản nơi khô ráo, tránh ánh nắng trực tiếp");
        material.setActualQuantity(Math.max(material.getActualQuantity(), actualQuantity));
        material.setMinStock(minStock);
        material.setMaxStock(actualQuantity * 3);
        material.setEstimatedUnitPrice(price);
        material.setStatus("ACTIVE");
        material.setDeleted(false);
        return materialRepository.save(material);
    }

    private MaterialBatch batch(Material material, Warehouse warehouse, StorageLocation location, Supplier supplier,
                                String batchNumber, int quantity, LocalDate expiryDate) {
        MaterialBatch batch = batchRepository.findByMaterial_IdAndBatchNumber(material.getId(), batchNumber).orElseGet(MaterialBatch::new);
        batch.setMaterial(material);
        batch.setWarehouse(warehouse);
        batch.setLocation(location);
        batch.setSupplier(supplier);
        batch.setBatchNumber(batchNumber);
        batch.setManufactureDate(LocalDate.now().minusMonths(2));
        batch.setExpiryDate(expiryDate);
        batch.setReceiptDate(LocalDate.now().minusDays(7));
        batch.setInitialQuantity(quantity);
        batch.setQuantity(quantity);
        batch.setStatus(BatchStatus.AVAILABLE);
        return batchRepository.save(batch);
    }

    private void seedRequests(Material gloves, Material mask, Material saline) {
        if (!requestRepository.existsByCode("YC-DEMO-001")) {
            MaterialRequest request = request("YC-DEMO-001", "Khoa Cấp cứu", "admin", RequestStatus.SUBMITTED,
                    "Cấp bổ sung vật tư trực cấp cứu cuối tuần", LocalDateTime.now().minusHours(3));
            request.getLines().add(requestLine(request, gloves, 12, 0, "Bổ sung tủ trực cấp cứu"));
            request.getLines().add(requestLine(request, mask, 8, 0, "Phát cho khu tiếp nhận"));
            requestRepository.save(request);
        }
        if (!requestRepository.existsByCode("YC-DEMO-002")) {
            MaterialRequest request = request("YC-DEMO-002", "Khoa Nội tổng hợp", "nhanvien2", RequestStatus.DEPARTMENT_APPROVED,
                    "Trưởng khoa đã duyệt, chờ kho xử lý", LocalDateTime.now().minusDays(1));
            request.setDepartmentApprovedBy("truongkhoa");
            request.setDepartmentApprovedAt(LocalDateTime.now().minusHours(20));
            request.getLines().add(requestLine(request, saline, 20, 20, "Bổ sung xe tiêm truyền"));
            requestRepository.save(request);
        }
    }

    private MaterialRequest request(String code, String department, String requester, RequestStatus status, String note, LocalDateTime createdAt) {
        MaterialRequest request = new MaterialRequest();
        request.setCode(code);
        request.setDepartment(department);
        request.setRequester(requester);
        request.setPriority("Ưu tiên vừa");
        request.setStatus(status);
        request.setNote(note);
        request.setCreatedAt(createdAt);
        request.setUpdatedAt(LocalDateTime.now());
        request.setSubmittedAt(createdAt.plusMinutes(5));
        return request;
    }

    private MaterialRequestLine requestLine(MaterialRequest request, Material material, int requested, int approved, String reason) {
        MaterialRequestLine line = new MaterialRequestLine();
        line.setRequest(request);
        line.setMaterial(material);
        line.setRequestedQuantity(requested);
        line.setApprovedQuantity(approved);
        line.setIssuedQuantity(0);
        line.setReason(reason);
        line.setStatus(approved > 0 ? "APPROVED" : "PENDING");
        line.setNote("Dữ liệu mẫu");
        return line;
    }

    private void seedReceipt(Warehouse warehouse, StorageLocation location, Supplier supplier, Material gloves, Material mask, Material saline) {
        if (receiptRepository.existsByReceiptCode("PN-DEMO-001")) {
            return;
        }
        Receipt receipt = new Receipt();
        receipt.setReceiptCode("PN-DEMO-001");
        receipt.setSupplier(supplier);
        receipt.setWarehouse(warehouse);
        receipt.setStatus(ReceiptStatus.CONFIRMED);
        receipt.setReceiptDate(LocalDate.now().minusDays(2));
        receipt.setCreatedBy("thukho");
        receipt.setConfirmedBy("thukho");
        receipt.setConfirmedAt(LocalDateTime.now().minusDays(2).plusHours(1));
        receipt.setNote("Phiếu nhập mẫu từ nhà cung cấp");
        receipt.getLines().add(receiptLine(receipt, gloves, location, "DEMO-NHAP-VT002", 60, new BigDecimal("42000")));
        receipt.getLines().add(receiptLine(receipt, mask, location, "DEMO-NHAP-VT001", 80, new BigDecimal("65000")));
        receipt.getLines().add(receiptLine(receipt, saline, location, "DEMO-NHAP-VT006", 40, new BigDecimal("18000")));
        receiptRepository.save(receipt);
    }

    private ReceiptLine receiptLine(Receipt receipt, Material material, StorageLocation location, String batchNumber, int quantity, BigDecimal unitPrice) {
        ReceiptLine line = new ReceiptLine();
        line.setReceipt(receipt);
        line.setMaterial(material);
        line.setBatchNumber(batchNumber);
        line.setManufacturingDate(LocalDate.now().minusMonths(2));
        line.setExpiryDate(LocalDate.now().plusMonths(12));
        line.setQuantity(quantity);
        line.setUnitPrice(unitPrice);
        line.setLocation(location);
        line.setNote("Dòng nhập mẫu");
        return line;
    }

    private void seedIssue(Warehouse warehouse, StorageLocation location, Material gloves, Material mask,
                           MaterialBatch glovesBatch, MaterialBatch maskBatch) {
        if (issueSlipRepository.existsByIssueCode("PX-DEMO-001")) {
            return;
        }
        MaterialRequest request = requestRepository.findAll().stream()
                .filter(item -> "YC-DEMO-001".equals(item.getCode()))
                .findFirst()
                .orElse(null);
        if (request == null) {
            return;
        }
        IssueSlip issue = new IssueSlip();
        issue.setIssueCode("PX-DEMO-001");
        issue.setMaterialRequest(request);
        issue.setDepartment("Khoa Cấp cứu");
        issue.setWarehouse(warehouse);
        issue.setStatus(IssueStatus.ISSUED);
        issue.setCreatedBy("thukho");
        issue.setIssuedBy("thukho");
        issue.setIssuedAt(LocalDateTime.now().minusHours(2));
        issue.setNote("Phiếu xuất mẫu theo yêu cầu YC-DEMO-001");
        issue.getLines().add(issueLine(issue, gloves, glovesBatch, warehouse, location, 10));
        issue.getLines().add(issueLine(issue, mask, maskBatch, warehouse, location, 6));
        issueSlipRepository.save(issue);
    }

    private IssueSlipLine issueLine(IssueSlip issue, Material material, MaterialBatch batch, Warehouse warehouse,
                                    StorageLocation location, int quantity) {
        IssueSlipLine line = new IssueSlipLine();
        line.setIssueSlip(issue);
        line.setMaterial(material);
        line.setRequestedQuantity(quantity);
        line.setApprovedQuantity(quantity);
        line.setIssuedQuantity(quantity);
        line.setNote("Xuất theo FEFO từ lô demo");

        IssueBatchAllocation allocation = new IssueBatchAllocation();
        allocation.setIssueSlipLine(line);
        allocation.setMaterial(material);
        allocation.setBatch(batch);
        allocation.setWarehouse(warehouse);
        allocation.setLocation(location);
        allocation.setQuantity(quantity);
        line.getAllocations().add(allocation);
        return line;
    }

    private void seedDepartmentStock(Material gloves, Material mask, Material saline,
                                     MaterialBatch glovesBatch, MaterialBatch maskBatch, MaterialBatch salineBatch) {
        departmentStock("Khoa Cấp cứu", gloves, glovesBatch, 10, 3);
        departmentStock("Khoa Cấp cứu", mask, maskBatch, 6, 2);
        departmentStock("Khoa Nội tổng hợp", saline, salineBatch, 18, 5);
    }

    private DepartmentStock departmentStock(String department, Material material, MaterialBatch batch, int onHand, int used) {
        DepartmentStock stock = departmentStockRepository
                .findByDepartmentAndMaterial_IdAndBatch_Id(department, material.getId(), batch.getId())
                .orElseGet(DepartmentStock::new);
        stock.setDepartment(department);
        stock.setMaterial(material);
        stock.setBatch(batch);
        stock.setQuantityOnHand(onHand);
        stock.setQuantityUsed(used);
        stock.setQuantityDamaged(0);
        stock.setQuantityLost(0);
        stock.setQuantityReturned(0);
        stock.setLastReceivedAt(LocalDateTime.now().minusHours(2));
        stock.setUpdatedAt(LocalDateTime.now());
        return departmentStockRepository.save(stock);
    }

    private void seedDepartmentReturn(Warehouse warehouse, StorageLocation location, Material gloves, MaterialBatch glovesBatch) {
        if (departmentReturnRepository.existsByReturnCode("TK-DEMO-001")) {
            return;
        }
        DepartmentStock stock = departmentStockRepository
                .findByDepartmentAndMaterial_IdAndBatch_Id("Khoa Cấp cứu", gloves.getId(), glovesBatch.getId())
                .orElse(null);
        if (stock == null) {
            return;
        }
        DepartmentReturn departmentReturn = new DepartmentReturn();
        departmentReturn.setReturnCode("TK-DEMO-001");
        departmentReturn.setDepartment("Khoa Cấp cứu");
        departmentReturn.setWarehouse(warehouse);
        departmentReturn.setStatus(DepartmentReturnStatus.SUBMITTED);
        departmentReturn.setReason("Trả vật tư dư sau ca trực");
        departmentReturn.setCreatedBy("nhanvien");
        departmentReturn.setCreatedAt(LocalDateTime.now().minusHours(1));
        departmentReturn.setUpdatedAt(LocalDateTime.now().minusHours(1));

        DepartmentReturnLine line = new DepartmentReturnLine();
        line.setDepartmentReturn(departmentReturn);
        line.setDepartmentStock(stock);
        line.setMaterial(gloves);
        line.setBatch(glovesBatch);
        line.setLocation(location);
        line.setQuantity(2);
        line.setNote("Dữ liệu mẫu trả khoa");
        departmentReturn.getLines().add(line);
        departmentReturnRepository.save(departmentReturn);
    }

    private void seedInventoryCount(Warehouse warehouse, StorageLocation location, Material gloves, Material mask,
                                    Material saline, Material gauze, MaterialBatch glovesBatch, MaterialBatch maskBatch,
                                    MaterialBatch salineBatch, MaterialBatch gauzeBatch) {
        if (inventoryCountRepository.existsByCountCode("KK-DEMO-001")) {
            return;
        }
        InventoryCount count = new InventoryCount();
        count.setCountCode("KK-DEMO-001");
        count.setWarehouse(warehouse);
        count.setStatus(InventoryCountStatus.COMPLETED);
        count.setStartedBy("thukho");
        count.setStartedAt(LocalDateTime.now().minusDays(1));
        count.setCompletedBy("thukho");
        count.setCompletedAt(LocalDateTime.now().minusHours(18));
        count.setNote("Kiểm kê mẫu cuối ngày");
        count.getLines().add(countLine(count, gloves, glovesBatch, location, 160, 158, "Lệch do đã cấp cho khoa"));
        count.getLines().add(countLine(count, mask, maskBatch, location, 220, 220, "Khớp hệ thống"));
        count.getLines().add(countLine(count, saline, salineBatch, location, 100, 101, "Thừa 1 chai sau đối chiếu"));
        count.getLines().add(countLine(count, gauze, gauzeBatch, location, 140, 140, "Khớp hệ thống"));
        inventoryCountRepository.save(count);
    }

    private InventoryCountLine countLine(InventoryCount count, Material material, MaterialBatch batch, StorageLocation location,
                                         int systemQuantity, int actualQuantity, String note) {
        InventoryCountLine line = new InventoryCountLine();
        line.setInventoryCount(count);
        line.setMaterial(material);
        line.setBatch(batch);
        line.setLocation(location);
        line.setSystemQuantity(systemQuantity);
        line.setActualQuantity(actualQuantity);
        line.setNote(note);
        return line;
    }

    private void seedNotifications() {
        if (notificationRepository.count() > 3) {
            return;
        }
        List<Notification> notifications = List.of(
                notification("Yêu cầu mới cần xử lý", "Khoa Cấp cứu vừa gửi yêu cầu cấp vật tư mẫu.", "REQUEST", "WAREHOUSE_STAFF", "/requests"),
                notification("Có phiếu trả từ khoa", "Khoa Cấp cứu gửi phiếu trả vật tư dư.", "DEPARTMENT_RETURN", "WAREHOUSE_STAFF", "/department-returns"),
                notification("Kiểm kê đã hoàn tất", "Phiếu kiểm kê KK-DEMO-001 đã có chênh lệch cần xem.", "INVENTORY_COUNT", "MANAGER", "/inventory-counts")
        );
        notificationRepository.saveAll(notifications);
    }

    private Notification notification(String title, String content, String type, String receiver, String link) {
        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setReceiver(receiver);
        notification.setLink(link);
        return notification;
    }
}
