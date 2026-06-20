package com.qlvt.service;

import com.qlvt.entity.*;
import com.qlvt.enums.*;
import com.qlvt.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class DepartmentStockService {
    private final DepartmentStockRepository stockRepository;
    private final DepartmentStockMovementRepository movementRepository;
    private final DepartmentReturnRepository returnRepository;
    private final StockBalanceRepository balanceRepository;
    private final StockMovementRepository stockMovementRepository;
    private final MaterialRepository materialRepository;
    private final MaterialBatchRepository batchRepository;
    private final WarehouseRepository warehouseRepository;
    private final StorageLocationRepository locationRepository;
    private final NotificationService notificationService;
    private final AuditService auditService;
    private final InventorySyncService inventorySyncService;

    public DepartmentStockService(DepartmentStockRepository stockRepository,
                                  DepartmentStockMovementRepository movementRepository,
                                  DepartmentReturnRepository returnRepository,
                                  StockBalanceRepository balanceRepository,
                                  StockMovementRepository stockMovementRepository,
                                  MaterialRepository materialRepository,
                                  MaterialBatchRepository batchRepository,
                                  WarehouseRepository warehouseRepository,
                                  StorageLocationRepository locationRepository,
                                  NotificationService notificationService,
                                  AuditService auditService,
                                  InventorySyncService inventorySyncService) {
        this.stockRepository = stockRepository;
        this.movementRepository = movementRepository;
        this.returnRepository = returnRepository;
        this.balanceRepository = balanceRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.materialRepository = materialRepository;
        this.batchRepository = batchRepository;
        this.warehouseRepository = warehouseRepository;
        this.locationRepository = locationRepository;
        this.notificationService = notificationService;
        this.auditService = auditService;
        this.inventorySyncService = inventorySyncService;
    }

    @Transactional
    public void receiveFromIssue(IssueSlip issueSlip, String username) {
        for (IssueSlipLine line : issueSlip.getLines()) {
            for (IssueBatchAllocation allocation : line.getAllocations()) {
                DepartmentStock stock = findOrCreate(issueSlip.getDepartment(), allocation.getMaterial(), allocation.getBatch());
                int before = stock.getQuantityOnHand();
                stock.setQuantityOnHand(before + allocation.getQuantity());
                stock.setLastReceivedAt(LocalDateTime.now());
                stock.setUpdatedAt(LocalDateTime.now());
                stock.validate();
                stockRepository.save(stock);
                saveMovement(DepartmentStockMovementType.RECEIVE_FROM_WAREHOUSE, issueSlip.getDepartment(), allocation.getMaterial(), allocation.getBatch(),
                        allocation.getQuantity(), before, stock.getQuantityOnHand(), "ISSUE_SLIP", issueSlip.getId(), "Khoa xác nhận đã nhận vật tư", username);
            }
        }
        auditService.log(username, "DEPARTMENT_RECEIVE_STOCK", "ISSUE_SLIP", issueSlip.getIssueCode(), "Cập nhật tồn tại khoa sau khi nhận vật tư");
    }

    @Transactional
    public void useStock(Long stockId, int quantity, String note, String username) {
        DepartmentStock stock = stockRepository.findById(stockId).orElseThrow();
        ensureQuantity(stock, quantity);
        int before = stock.getQuantityOnHand();
        stock.setQuantityOnHand(before - quantity);
        stock.setQuantityUsed(stock.getQuantityUsed() + quantity);
        stock.setUpdatedAt(LocalDateTime.now());
        stock.validate();
        stockRepository.save(stock);
        saveMovement(DepartmentStockMovementType.USE, stock.getDepartment(), stock.getMaterial(), stock.getBatch(), -quantity, before, stock.getQuantityOnHand(), "DEPARTMENT_STOCK", stockId, note, username);
        auditService.log(username, "DEPARTMENT_USE_STOCK", "DEPARTMENT_STOCK", stock.getId().toString(), "Khoa báo đã sử dụng vật tư");
    }

    @Transactional
    public void reportIssue(Long stockId, DepartmentIssueType type, int quantity, String note, String username) {
        DepartmentStock stock = stockRepository.findById(stockId).orElseThrow();
        ensureQuantity(stock, quantity);
        int before = stock.getQuantityOnHand();
        stock.setQuantityOnHand(before - quantity);
        DepartmentStockMovementType movementType = switch (type) {
            case DAMAGE -> {
                stock.setQuantityDamaged(stock.getQuantityDamaged() + quantity);
                yield DepartmentStockMovementType.DAMAGE;
            }
            case LOST -> {
                stock.setQuantityLost(stock.getQuantityLost() + quantity);
                yield DepartmentStockMovementType.LOST;
            }
            case EXPIRED_AT_DEPARTMENT -> {
                stock.setQuantityDamaged(stock.getQuantityDamaged() + quantity);
                yield DepartmentStockMovementType.EXPIRED_AT_DEPARTMENT;
            }
            case OTHER -> DepartmentStockMovementType.ADJUSTMENT;
        };
        stock.setUpdatedAt(LocalDateTime.now());
        stock.validate();
        stockRepository.save(stock);
        saveMovement(movementType, stock.getDepartment(), stock.getMaterial(), stock.getBatch(), -quantity, before, stock.getQuantityOnHand(), "DEPARTMENT_STOCK", stockId, note, username);
        notificationService.notify("Khoa báo sự cố vật tư", stock.getDepartment() + " báo " + type.getLabel() + " " + quantity + " " + stock.getMaterial().getName(), "DEPARTMENT_STOCK_ISSUE", "WAREHOUSE_STAFF", "/department-stocks/" + stockId);
        notificationService.notify("Khoa báo sự cố vật tư", stock.getDepartment() + " báo " + type.getLabel() + " " + quantity + " " + stock.getMaterial().getName(), "DEPARTMENT_STOCK_ISSUE", "MANAGER", "/department-stocks/" + stockId);
        auditService.log(username, "DEPARTMENT_REPORT_STOCK_ISSUE", "DEPARTMENT_STOCK", stock.getId().toString(), "Khoa báo sự cố vật tư");
    }

    @Transactional
    public void recordRecallReturn(Long recallId, String recallCode, String department, Material material, MaterialBatch batch,
                                   int quantity, String note, String username) {
        DepartmentStock stock = stockRepository.findByDepartmentAndMaterial_IdAndBatch_Id(department, material.getId(), batch.getId())
                .orElseThrow(() -> new IllegalStateException("Khoa " + department + " không còn tồn lô "
                        + batch.getBatchNumber() + " để trả theo lệnh thu hồi"));
        ensureQuantity(stock, quantity);
        int before = stock.getQuantityOnHand();
        stock.setQuantityOnHand(before - quantity);
        stock.setQuantityReturned(stock.getQuantityReturned() + quantity);
        stock.setUpdatedAt(LocalDateTime.now());
        stock.validate();
        stockRepository.save(stock);
        saveMovement(DepartmentStockMovementType.RECALL_RETURN, department, material, batch, -quantity,
                before, stock.getQuantityOnHand(), "RECALL", recallId, note, username);
        auditService.log(username, "DEPARTMENT_RECALL_RETURN", "RECALL_ORDER", recallCode,
                "Khoa trả " + quantity + " " + material.getUnit() + " theo lệnh thu hồi");
    }

    @Transactional
    public DepartmentReturn createReturn(Long stockId, Long warehouseId, Long locationId, int quantity, String reason, String username) {
        DepartmentStock stock = stockRepository.findById(stockId).orElseThrow();
        ensureQuantity(stock, quantity);
        DepartmentReturn departmentReturn = new DepartmentReturn();
        departmentReturn.setReturnCode(nextReturnCode());
        departmentReturn.setDepartment(stock.getDepartment());
        Warehouse warehouse = warehouseRepository.findById(warehouseId).orElseThrow();
        StorageLocation location = locationRepository.findById(locationId).orElseThrow();
        if (location.getWarehouse() == null || !location.getWarehouse().getId().equals(warehouse.getId())) {
            throw new IllegalArgumentException("Vị trí nhận lại phải thuộc kho đã chọn");
        }
        departmentReturn.setWarehouse(warehouse);
        departmentReturn.setReason(reason);
        departmentReturn.setCreatedBy(username);
        departmentReturn.setStatus(DepartmentReturnStatus.SUBMITTED);

        DepartmentReturnLine line = new DepartmentReturnLine();
        line.setDepartmentReturn(departmentReturn);
        line.setDepartmentStock(stock);
        line.setMaterial(stock.getMaterial());
        line.setBatch(stock.getBatch());
        line.setLocation(location);
        line.setQuantity(quantity);
        line.setNote(reason);
        departmentReturn.getLines().add(line);
        returnRepository.save(departmentReturn);
        notificationService.notify("Phiếu trả vật tư từ khoa", departmentReturn.getReturnCode() + " đang chờ kho xác nhận nhận lại.", "DEPARTMENT_RETURN", "WAREHOUSE_STAFF", "/department-returns/" + departmentReturn.getId());
        auditService.log(username, "CREATE_DEPARTMENT_RETURN", "DEPARTMENT_RETURN", departmentReturn.getReturnCode(), "Khoa tạo phiếu trả vật tư về kho");
        return departmentReturn;
    }

    @Transactional
    public void receiveReturn(Long returnId, String username) {
        DepartmentReturn departmentReturn = returnRepository.findById(returnId).orElseThrow();
        if (departmentReturn.getStatus() != DepartmentReturnStatus.SUBMITTED) {
            throw new IllegalStateException("Chỉ xác nhận phiếu trả đang chờ kho nhận");
        }
        for (DepartmentReturnLine line : departmentReturn.getLines()) {
            DepartmentStock departmentStock = line.getDepartmentStock();
            ensureQuantity(departmentStock, line.getQuantity());
            int beforeDepartment = departmentStock.getQuantityOnHand();
            departmentStock.setQuantityOnHand(beforeDepartment - line.getQuantity());
            departmentStock.setQuantityReturned(departmentStock.getQuantityReturned() + line.getQuantity());
            departmentStock.setUpdatedAt(LocalDateTime.now());
            departmentStock.validate();
            stockRepository.save(departmentStock);
            saveMovement(DepartmentStockMovementType.RETURN_TO_WAREHOUSE, departmentStock.getDepartment(), line.getMaterial(), line.getBatch(), -line.getQuantity(),
                    beforeDepartment, departmentStock.getQuantityOnHand(), "DEPARTMENT_RETURN", returnId, departmentReturn.getReason(), username);

            StockBalance balance = findOrCreateBalance(line.getMaterial(), line.getBatch(), departmentReturn.getWarehouse(), line.getLocation());
            balance.setActualQuantity(balance.getActualQuantity() + line.getQuantity());
            balance.setUpdatedAt(LocalDateTime.now());
            balance.validate();
            balanceRepository.save(balance);

            Material material = line.getMaterial();
            int beforeMaterial = inventorySyncService.syncMaterialActualQuantity(material);
            MaterialBatch batch = line.getBatch();
            batch.setQuantity(batch.getQuantity() + line.getQuantity());
            batch.setUpdatedAt(LocalDateTime.now());
            batchRepository.save(batch);
            int afterMaterial = inventorySyncService.syncMaterialActualQuantity(material);
            stockMovementRepository.save(stockMovement(MovementType.RETURN, material, batch, departmentReturn.getWarehouse(), line.getLocation(), line.getQuantity(), beforeMaterial, afterMaterial, "DEPARTMENT_RETURN", departmentReturn.getReturnCode(), username));
        }
        departmentReturn.setStatus(DepartmentReturnStatus.RECEIVED_BY_WAREHOUSE);
        departmentReturn.setReceivedBy(username);
        departmentReturn.setReceivedAt(LocalDateTime.now());
        departmentReturn.setUpdatedAt(LocalDateTime.now());
        returnRepository.save(departmentReturn);
        auditService.log(username, "RECEIVE_DEPARTMENT_RETURN", "DEPARTMENT_RETURN", departmentReturn.getReturnCode(), "Kho xác nhận nhận lại vật tư từ khoa");
    }

    private DepartmentStock findOrCreate(String department, Material material, MaterialBatch batch) {
        return stockRepository.findByDepartmentAndMaterial_IdAndBatch_Id(department, material.getId(), batch.getId()).orElseGet(() -> {
            DepartmentStock stock = new DepartmentStock();
            stock.setDepartment(department);
            stock.setMaterial(material);
            stock.setBatch(batch);
            return stock;
        });
    }

    private StockBalance findOrCreateBalance(Material material, MaterialBatch batch, Warehouse warehouse, StorageLocation location) {
        return balanceRepository.findByMaterial_IdAndBatch_IdAndWarehouse_IdAndLocation_Id(material.getId(), batch.getId(), warehouse.getId(), location.getId()).orElseGet(() -> {
            StockBalance balance = new StockBalance();
            balance.setMaterial(material);
            balance.setBatch(batch);
            balance.setWarehouse(warehouse);
            balance.setLocation(location);
            return balance;
        });
    }

    private void ensureQuantity(DepartmentStock stock, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
        }
        if (stock.getQuantityOnHand() < quantity) {
            throw new IllegalStateException("Không được dùng hoặc trả vượt quá tồn tại khoa");
        }
    }

    private void saveMovement(DepartmentStockMovementType type, String department, Material material, MaterialBatch batch, int quantity,
                              int before, int after, String referenceType, Long referenceId, String note, String username) {
        DepartmentStockMovement movement = new DepartmentStockMovement();
        movement.setMovementType(type);
        movement.setDepartment(department);
        movement.setMaterial(material);
        movement.setBatch(batch);
        movement.setQuantity(quantity);
        movement.setBeforeQuantity(before);
        movement.setAfterQuantity(after);
        movement.setReferenceType(referenceType);
        movement.setReferenceId(referenceId);
        movement.setNote(note);
        movement.setCreatedBy(username);
        movementRepository.save(movement);
    }

    private StockMovement stockMovement(MovementType type, Material material, MaterialBatch batch, Warehouse warehouse, StorageLocation location,
                                        int quantity, int before, int after, String refType, String refCode, String username) {
        StockMovement movement = new StockMovement();
        movement.setMovementType(type);
        movement.setMaterial(material);
        movement.setBatch(batch);
        movement.setWarehouse(warehouse);
        movement.setLocation(location);
        movement.setQuantity(quantity);
        movement.setBeforeQuantity(before);
        movement.setAfterQuantity(after);
        movement.setReferenceType(refType);
        movement.setReferenceCode(refCode);
        movement.setCreatedBy(username);
        return movement;
    }

    private String nextReturnCode() {
        String code = "TK-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return returnRepository.existsByReturnCode(code) ? code + "-1" : code;
    }
}
