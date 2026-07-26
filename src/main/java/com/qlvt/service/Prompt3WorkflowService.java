package com.qlvt.service;

import com.qlvt.entity.*;
import com.qlvt.enums.*;
import com.qlvt.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class Prompt3WorkflowService {
    private final InventoryCountRepository inventoryCountRepository;
    private final InventoryCountLineRepository inventoryCountLineRepository;
    private final StockAdjustmentRepository stockAdjustmentRepository;
    private final StockTransferRepository stockTransferRepository;
    private final RecallOrderRepository recallOrderRepository;
    private final RecallDepartmentResponseRepository recallResponseRepository;
    private final DestructionSlipRepository destructionSlipRepository;
    private final PurchaseRequestRepository purchaseRequestRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final ReceiptRepository receiptRepository;
    private final MaterialRepository materialRepository;
    private final MaterialBatchRepository batchRepository;
    private final WarehouseRepository warehouseRepository;
    private final StorageLocationRepository locationRepository;
    private final SupplierRepository supplierRepository;
    private final StockBalanceRepository balanceRepository;
    private final StockMovementRepository movementRepository;
    private final IssueBatchAllocationRepository allocationRepository;
    private final AppUserRepository userRepository;
    private final AuditService auditService;
    private final NotificationService notificationService;
    private final InventorySyncService inventorySyncService;
    private final DepartmentStockService departmentStockService;

    public Prompt3WorkflowService(InventoryCountRepository inventoryCountRepository,
                                  InventoryCountLineRepository inventoryCountLineRepository,
                                  StockAdjustmentRepository stockAdjustmentRepository,
                                  StockTransferRepository stockTransferRepository,
                                  RecallOrderRepository recallOrderRepository,
                                  RecallDepartmentResponseRepository recallResponseRepository,
                                  DestructionSlipRepository destructionSlipRepository,
                                  PurchaseRequestRepository purchaseRequestRepository,
                                  PurchaseOrderRepository purchaseOrderRepository,
                                  ReceiptRepository receiptRepository,
                                  MaterialRepository materialRepository,
                                  MaterialBatchRepository batchRepository,
                                  WarehouseRepository warehouseRepository,
                                  StorageLocationRepository locationRepository,
                                  SupplierRepository supplierRepository,
                                  StockBalanceRepository balanceRepository,
                                  StockMovementRepository movementRepository,
                                  IssueBatchAllocationRepository allocationRepository,
                                  AppUserRepository userRepository,
                                  AuditService auditService,
                                  NotificationService notificationService,
                                  InventorySyncService inventorySyncService,
                                  DepartmentStockService departmentStockService) {
        this.inventoryCountRepository = inventoryCountRepository;
        this.inventoryCountLineRepository = inventoryCountLineRepository;
        this.stockAdjustmentRepository = stockAdjustmentRepository;
        this.stockTransferRepository = stockTransferRepository;
        this.recallOrderRepository = recallOrderRepository;
        this.recallResponseRepository = recallResponseRepository;
        this.destructionSlipRepository = destructionSlipRepository;
        this.purchaseRequestRepository = purchaseRequestRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.receiptRepository = receiptRepository;
        this.materialRepository = materialRepository;
        this.batchRepository = batchRepository;
        this.warehouseRepository = warehouseRepository;
        this.locationRepository = locationRepository;
        this.supplierRepository = supplierRepository;
        this.balanceRepository = balanceRepository;
        this.movementRepository = movementRepository;
        this.allocationRepository = allocationRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
        this.notificationService = notificationService;
        this.inventorySyncService = inventorySyncService;
        this.departmentStockService = departmentStockService;
    }

    @Transactional
    public InventoryCount createInventoryCount(Long warehouseId, String note, String username) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId).orElseThrow();
        InventoryCount count = new InventoryCount();
        count.setCountCode(nextCode("KK", code -> inventoryCountRepository.existsByCountCode(code)));
        count.setWarehouse(warehouse);
        count.setStatus(InventoryCountStatus.COUNTING);
        count.setStartedBy(username);
        count.setStartedAt(LocalDateTime.now());
        count.setNote(note);
        for (StockBalance balance : balanceRepository.findByWarehouse_IdOrderByMaterial_CodeAscBatch_ExpiryDateAsc(warehouseId)) {
            InventoryCountLine line = new InventoryCountLine();
            line.setInventoryCount(count);
            line.setMaterial(balance.getMaterial());
            line.setBatch(balance.getBatch());
            line.setLocation(balance.getLocation());
            line.setSystemQuantity(balance.getActualQuantity());
            line.setActualQuantity(balance.getActualQuantity());
            line.setNote("");
            count.getLines().add(line);
        }
        inventoryCountRepository.save(count);
        auditService.log(username, "CREATE_INVENTORY_COUNT", "INVENTORY_COUNT", count.getCountCode(), "Tạo đợt kiểm kê");
        notificationService.notify("Kiểm kê cần xử lý", "Đợt kiểm kê " + count.getCountCode() + " đã được tạo.", "INVENTORY_COUNT", "WAREHOUSE_STAFF", "/inventory-counts/" + count.getId());
        return count;
    }

    @Transactional
    public void updateInventoryCountLines(Long countId, Map<Long, Integer> actualQuantities, String username) {
        InventoryCount count = inventoryCountRepository.findById(countId).orElseThrow();
        ensure(count.getStatus() == InventoryCountStatus.COUNTING || count.getStatus() == InventoryCountStatus.DRAFT, "Chỉ cập nhật đợt đang kiểm kê");
        for (InventoryCountLine line : count.getLines()) {
            Integer actual = actualQuantities.get(line.getId());
            if (actual != null) {
                ensure(actual >= 0, "Số lượng kiểm kê không được âm");
                line.setActualQuantity(actual);
            }
        }
        count.setUpdatedAt(LocalDateTime.now());
        inventoryCountRepository.save(count);
        auditService.log(username, "UPDATE_INVENTORY_COUNT", "INVENTORY_COUNT", count.getCountCode(), "Cập nhật số lượng kiểm kê");
    }

    @Transactional
    public void completeInventoryCount(Long countId, String username) {
        InventoryCount count = inventoryCountRepository.findById(countId).orElseThrow();
        ensure(count.getStatus() == InventoryCountStatus.COUNTING || count.getStatus() == InventoryCountStatus.DRAFT, "Kiểm kê đã hoàn tất hoặc đã hủy");
        count.setStatus(InventoryCountStatus.COMPLETED);
        count.setCompletedBy(username);
        count.setCompletedAt(LocalDateTime.now());
        count.setUpdatedAt(LocalDateTime.now());
        inventoryCountRepository.save(count);
        auditService.log(username, "COMPLETE_INVENTORY_COUNT", "INVENTORY_COUNT", count.getCountCode(), "Hoàn tất kiểm kê, chưa tự điều chỉnh tồn");
    }

    @Transactional
    public void cancelInventoryCount(Long countId, String reason, String username) {
        InventoryCount count = inventoryCountRepository.findById(countId).orElseThrow();
        ensure(count.canCancel(), "Chỉ được hủy đợt kiểm kê đang thực hiện");
        ensure(reason != null && !reason.isBlank(), "Phải nhập lý do hủy");
        String cancellationReason = reason.trim();
        count.setStatus(InventoryCountStatus.CANCELLED);
        count.setNote(count.getNote() == null || count.getNote().isBlank()
                ? "Lý do hủy: " + cancellationReason
                : count.getNote().trim() + "\nLý do hủy: " + cancellationReason);
        count.setUpdatedAt(LocalDateTime.now());
        inventoryCountRepository.save(count);
        auditService.log(username, "CANCEL_INVENTORY_COUNT", "INVENTORY_COUNT", count.getCountCode(),
                "Hủy đợt kiểm kê: " + cancellationReason);
    }

    @Transactional
    public StockAdjustment createAdjustmentFromCount(Long countId, String username) {
        InventoryCount count = inventoryCountRepository.findById(countId).orElseThrow();
        ensure(count.getStatus() == InventoryCountStatus.COMPLETED, "Chỉ tạo điều chỉnh từ kiểm kê đã hoàn tất");
        ensure(!stockAdjustmentRepository.existsByInventoryCount_Id(countId),
                "Đợt kiểm kê này đã có phiếu điều chỉnh");
        StockAdjustment adjustment = new StockAdjustment();
        adjustment.setAdjustmentCode(nextCode("DC", code -> stockAdjustmentRepository.existsByAdjustmentCode(code)));
        adjustment.setInventoryCount(count);
        adjustment.setWarehouse(count.getWarehouse());
        adjustment.setReason("Điều chỉnh từ kiểm kê " + count.getCountCode());
        adjustment.setCreatedBy(username);
        for (InventoryCountLine countLine : count.getLines()) {
            if (countLine.getDifferenceQuantity() == 0) {
                continue;
            }
            StockAdjustmentLine line = new StockAdjustmentLine();
            line.setStockAdjustment(adjustment);
            line.setMaterial(countLine.getMaterial());
            line.setBatch(countLine.getBatch());
            line.setLocation(countLine.getLocation());
            line.setSystemQuantity(countLine.getSystemQuantity());
            line.setActualQuantity(countLine.getActualQuantity() == null ? countLine.getSystemQuantity() : countLine.getActualQuantity());
            line.setAdjustmentQuantity(countLine.getDifferenceQuantity());
            adjustment.getLines().add(line);
        }
        ensure(!adjustment.getLines().isEmpty(), "Kiểm kê không có chênh lệch để điều chỉnh");
        stockAdjustmentRepository.save(adjustment);
        notificationService.notify("Phiếu điều chỉnh cần duyệt", adjustment.getAdjustmentCode() + " đang chờ duyệt.", "ADJUSTMENT", "MANAGER", "/stock-adjustments/" + adjustment.getId());
        return adjustment;
    }

    @Transactional
    public StockAdjustment createManualAdjustment(Long balanceId, int actualQuantity, String reason, String username) {
        ensure(actualQuantity >= 0, "Tồn thực tế không được âm");
        StockBalance balance = balanceRepository.findById(balanceId).orElseThrow();
        StockAdjustment adjustment = new StockAdjustment();
        adjustment.setAdjustmentCode(nextCode("DC", code -> stockAdjustmentRepository.existsByAdjustmentCode(code)));
        adjustment.setWarehouse(balance.getWarehouse());
        adjustment.setReason(reason);
        adjustment.setCreatedBy(username);
        StockAdjustmentLine line = new StockAdjustmentLine();
        line.setStockAdjustment(adjustment);
        line.setMaterial(balance.getMaterial());
        line.setBatch(balance.getBatch());
        line.setLocation(balance.getLocation());
        line.setSystemQuantity(balance.getActualQuantity());
        line.setActualQuantity(actualQuantity);
        line.setAdjustmentQuantity(actualQuantity - balance.getActualQuantity());
        adjustment.getLines().add(line);
        stockAdjustmentRepository.save(adjustment);
        return adjustment;
    }

    @Transactional
    public void submitAdjustment(Long adjustmentId, String username) {
        StockAdjustment adjustment = stockAdjustmentRepository.findById(adjustmentId).orElseThrow();
        ensure(adjustment.getStatus() == StockAdjustmentStatus.DRAFT, "Chỉ gửi duyệt phiếu nháp");
        adjustment.setStatus(StockAdjustmentStatus.SUBMITTED);
        adjustment.setUpdatedAt(LocalDateTime.now());
        stockAdjustmentRepository.save(adjustment);
        notificationService.notify("Phiếu điều chỉnh cần duyệt", adjustment.getAdjustmentCode() + " đang chờ duyệt.", "ADJUSTMENT", "MANAGER", "/stock-adjustments/" + adjustmentId);
        auditService.log(username, "SUBMIT_ADJUSTMENT", "STOCK_ADJUSTMENT", adjustment.getAdjustmentCode(), "Gửi duyệt điều chỉnh tồn");
    }

    @Transactional
    public void approveAdjustment(Long adjustmentId, String username) {
        StockAdjustment adjustment = stockAdjustmentRepository.findById(adjustmentId).orElseThrow();
        ensure(adjustment.getStatus() == StockAdjustmentStatus.SUBMITTED, "Phiếu không ở trạng thái chờ quản lý duyệt");
        ensureCanApprove(username, adjustment.getCreatedBy());
        LocalDateTime now = LocalDateTime.now();
        adjustment.setManagerApprovedBy(username);
        adjustment.setManagerApprovedAt(now);
        adjustment.setApprovedBy(username);
        adjustment.setApprovedAt(now);
        if (requiresAccountant(adjustment)) {
            adjustment.setStatus(StockAdjustmentStatus.APPROVED_BY_MANAGER);
            notificationService.notify("Phiếu điều chỉnh chờ kế toán duyệt", adjustment.getAdjustmentCode() + " đã được quản lý duyệt và cần kế toán duyệt bước 2.", "ADJUSTMENT_ACCOUNTANT", "ACCOUNTANT", "/stock-adjustments/" + adjustmentId);
        } else {
            applyAdjustment(adjustment, username);
            adjustment.setStatus(StockAdjustmentStatus.COMPLETED);
        }
        adjustment.setUpdatedAt(now);
        stockAdjustmentRepository.save(adjustment);
        auditService.log(username, "APPROVE_ADJUSTMENT", "STOCK_ADJUSTMENT", adjustment.getAdjustmentCode(), "Duyệt và áp dụng điều chỉnh tồn");
    }

    @Transactional
    public void approveAdjustmentByAccountant(Long adjustmentId, String username) {
        StockAdjustment adjustment = stockAdjustmentRepository.findById(adjustmentId).orElseThrow();
        ensure(adjustment.getStatus() == StockAdjustmentStatus.APPROVED_BY_MANAGER, "Phiếu chưa được quản lý duyệt hoặc không cần kế toán duyệt");
        ensureAccountant(username);
        ensureCanApprove(username, adjustment.getCreatedBy());
        applyAdjustment(adjustment, username);
        LocalDateTime now = LocalDateTime.now();
        adjustment.setAccountantApprovedBy(username);
        adjustment.setAccountantApprovedAt(now);
        adjustment.setStatus(StockAdjustmentStatus.COMPLETED);
        adjustment.setUpdatedAt(now);
        stockAdjustmentRepository.save(adjustment);
        auditService.log(username, "APPROVE_ADJUSTMENT_ACCOUNTANT", "STOCK_ADJUSTMENT", adjustment.getAdjustmentCode(), "Kế toán duyệt bước 2 và áp dụng điều chỉnh tồn");
    }

    @Transactional
    public void rejectAdjustment(Long adjustmentId, String reason, String username) {
        StockAdjustment adjustment = stockAdjustmentRepository.findById(adjustmentId).orElseThrow();
        ensure(adjustment.canReject(), "Chỉ từ chối phiếu điều chỉnh đang chờ duyệt");
        String decisionReason = requireDecisionReason(reason, "Phải nhập lý do từ chối");
        LocalDateTime now = LocalDateTime.now();
        adjustment.setStatus(StockAdjustmentStatus.REJECTED);
        adjustment.setRejectedBy(username);
        adjustment.setRejectedAt(now);
        adjustment.setRejectedReason(decisionReason);
        adjustment.setUpdatedAt(now);
        stockAdjustmentRepository.save(adjustment);
        auditService.log(username, "REJECT_ADJUSTMENT", "STOCK_ADJUSTMENT", adjustment.getAdjustmentCode(),
                "Từ chối phiếu điều chỉnh tồn: " + decisionReason);
    }

    @Transactional
    public void cancelAdjustment(Long adjustmentId, String reason, String username) {
        StockAdjustment adjustment = stockAdjustmentRepository.findById(adjustmentId).orElseThrow();
        ensure(adjustment.canCancel(), "Chỉ hủy phiếu điều chỉnh đang nháp");
        String decisionReason = requireDecisionReason(reason, "Phải nhập lý do hủy");
        adjustment.setStatus(StockAdjustmentStatus.CANCELLED);
        adjustment.setReason(appendDecision(adjustment.getReason(), "Đã hủy phiếu", decisionReason));
        adjustment.setUpdatedAt(LocalDateTime.now());
        stockAdjustmentRepository.save(adjustment);
        auditService.log(username, "CANCEL_ADJUSTMENT", "STOCK_ADJUSTMENT", adjustment.getAdjustmentCode(),
                "Hủy phiếu điều chỉnh tồn: " + decisionReason);
    }

    @Transactional
    public StockTransfer createTransfer(Long fromWarehouseId, Long toWarehouseId, Long balanceId, Long toLocationId, int quantity, String reason, String username) {
        ensure(quantity > 0, "Số lượng chuyển phải lớn hơn 0");
        ensure(!fromWarehouseId.equals(toWarehouseId), "Kho đi và kho đến phải khác nhau");
        StockBalance balance = balanceRepository.findById(balanceId).orElseThrow();
        ensure(balance.getWarehouse().getId().equals(fromWarehouseId), "Tồn chọn không thuộc kho đi");
        ensure(balance.getAvailableQuantity() >= quantity, "Không đủ tồn khả dụng để chuyển");
        ensure(balance.getBatch().canIssue(LocalDate.now()), "Không cho chuyển lô hết hạn hoặc bị khóa");
        StockTransfer transfer = new StockTransfer();
        transfer.setTransferCode(nextCode("CK", code -> stockTransferRepository.existsByTransferCode(code)));
        transfer.setFromWarehouse(warehouseRepository.findById(fromWarehouseId).orElseThrow());
        transfer.setToWarehouse(warehouseRepository.findById(toWarehouseId).orElseThrow());
        transfer.setReason(reason);
        transfer.setCreatedBy(username);
        StockTransferLine line = new StockTransferLine();
        line.setStockTransfer(transfer);
        line.setMaterial(balance.getMaterial());
        line.setBatch(balance.getBatch());
        line.setFromLocation(balance.getLocation());
        line.setToLocation(locationRepository.findById(toLocationId).orElseThrow());
        line.setQuantity(quantity);
        transfer.getLines().add(line);
        stockTransferRepository.save(transfer);
        return transfer;
    }

    @Transactional
    public void submitTransfer(Long id, String username) {
        StockTransfer transfer = stockTransferRepository.findById(id).orElseThrow();
        ensure(transfer.getStatus() == StockTransferStatus.DRAFT, "Chỉ gửi duyệt phiếu nháp");
        transfer.setStatus(StockTransferStatus.SUBMITTED);
        transfer.setUpdatedAt(LocalDateTime.now());
        stockTransferRepository.save(transfer);
        notificationService.notify("Phiếu chuyển kho cần duyệt", transfer.getTransferCode() + " đang chờ duyệt.", "TRANSFER", "WAREHOUSE_STAFF", "/stock-transfers/" + id);
        auditService.log(username, "SUBMIT_TRANSFER", "STOCK_TRANSFER", transfer.getTransferCode(), "Gửi duyệt chuyển kho");
    }

    @Transactional
    public void approveTransfer(Long id, String username) {
        StockTransfer transfer = stockTransferRepository.findById(id).orElseThrow();
        ensure(transfer.getStatus() == StockTransferStatus.SUBMITTED || transfer.getStatus() == StockTransferStatus.DRAFT, "Phiếu không ở trạng thái chờ duyệt");
        transfer.setStatus(StockTransferStatus.APPROVED);
        transfer.setApprovedBy(username);
        transfer.setApprovedAt(LocalDateTime.now());
        transfer.setUpdatedAt(LocalDateTime.now());
        stockTransferRepository.save(transfer);
        auditService.log(username, "APPROVE_TRANSFER", "STOCK_TRANSFER", transfer.getTransferCode(), "Duyệt chuyển kho");
    }

    @Transactional
    public void rejectTransfer(Long id, String rejectionReason, String username) {
        StockTransfer transfer = stockTransferRepository.findById(id).orElseThrow();
        ensure(transfer.getStatus() == StockTransferStatus.SUBMITTED, "Chỉ từ chối phiếu đang chờ duyệt");
        String reason = rejectionReason == null ? "" : rejectionReason.trim();
        ensure(!reason.isEmpty(), "Cần nhập lý do từ chối");

        transfer.setStatus(StockTransferStatus.REJECTED);
        transfer.setNote("Từ chối: " + reason);
        transfer.setUpdatedAt(LocalDateTime.now());
        stockTransferRepository.save(transfer);
        auditService.log(username, "REJECT_TRANSFER", "STOCK_TRANSFER", transfer.getTransferCode(), "Từ chối chuyển kho: " + reason);
    }

    @Transactional
    public void cancelTransfer(Long id, String cancellationReason, String username) {
        StockTransfer transfer = stockTransferRepository.findById(id).orElseThrow();
        ensure(transfer.canCancel(), "Chỉ hủy phiếu chuyển kho nháp");
        String reason = cancellationReason == null ? "" : cancellationReason.trim();
        ensure(!reason.isEmpty(), "Cần nhập lý do hủy");

        transfer.setStatus(StockTransferStatus.CANCELLED);
        transfer.setNote("Hủy: " + reason);
        transfer.setUpdatedAt(LocalDateTime.now());
        stockTransferRepository.save(transfer);
        auditService.log(username, "CANCEL_TRANSFER", "STOCK_TRANSFER", transfer.getTransferCode(),
                "Hủy phiếu chuyển kho: " + reason);
    }

    @Transactional
    public void executeTransfer(Long id, String username) {
        StockTransfer transfer = stockTransferRepository.findById(id).orElseThrow();
        ensure(transfer.getStatus() == StockTransferStatus.APPROVED, "Phiếu phải được duyệt trước khi chuyển");
        for (StockTransferLine line : transfer.getLines()) {
            moveBetweenBalances(line.getMaterial(), line.getBatch(), transfer.getFromWarehouse(), transfer.getToWarehouse(),
                    line.getFromLocation(), line.getToLocation(), line.getQuantity(), username, transfer.getTransferCode());
        }
        transfer.setStatus(StockTransferStatus.TRANSFERRED);
        transfer.setTransferredBy(username);
        transfer.setTransferredAt(LocalDateTime.now());
        transfer.setUpdatedAt(LocalDateTime.now());
        stockTransferRepository.save(transfer);
        auditService.log(username, "EXECUTE_TRANSFER", "STOCK_TRANSFER", transfer.getTransferCode(), "Thực hiện chuyển kho");
    }

    @Transactional
    public void receiveTransfer(Long id, String username) {
        StockTransfer transfer = stockTransferRepository.findById(id).orElseThrow();
        ensure(transfer.getStatus() == StockTransferStatus.TRANSFERRED, "Chỉ xác nhận nhận sau khi đã chuyển");
        transfer.setStatus(StockTransferStatus.RECEIVED);
        transfer.setReceivedBy(username);
        transfer.setReceivedAt(LocalDateTime.now());
        transfer.setUpdatedAt(LocalDateTime.now());
        stockTransferRepository.save(transfer);
        auditService.log(username, "RECEIVE_TRANSFER", "STOCK_TRANSFER", transfer.getTransferCode(), "Kho nhận xác nhận chuyển kho");
    }

    @Transactional
    public RecallOrder createRecall(Long batchId, String reason, String username) {
        MaterialBatch batch = batchRepository.findById(batchId).orElseThrow();
        RecallOrder recall = new RecallOrder();
        recall.setRecallCode(nextCode("TH", code -> recallOrderRepository.existsByRecallCode(code)));
        recall.setBatch(batch);
        recall.setMaterial(batch.getMaterial());
        recall.setReason(reason);
        recall.setCreatedBy(username);
        Map<String, Integer> issuedByDepartment = new LinkedHashMap<>();
        for (IssueBatchAllocation allocation : allocationRepository.findByBatch_Id(batchId)) {
            String department = allocation.getIssueSlipLine().getIssueSlip().getDepartment();
            issuedByDepartment.merge(department == null ? "Không rõ" : department, allocation.getQuantity(), Integer::sum);
        }
        issuedByDepartment.forEach((department, quantity) -> {
            RecallOrderLine line = new RecallOrderLine();
            line.setRecallOrder(recall);
            line.setDepartment(department);
            line.setIssuedQuantity(quantity);
            line.setRemainingQuantity(quantity);
            recall.getLines().add(line);
        });
        recallOrderRepository.save(recall);
        return recall;
    }

    @Transactional
    public void activateRecall(Long id, String username) {
        RecallOrder recall = recallOrderRepository.findById(id).orElseThrow();
        ensure(recall.canActivate(), "Chỉ kích hoạt lệnh thu hồi nháp");
        ensureCanApprove(username, recall.getCreatedBy());
        recall.setStatus(RecallStatus.ACTIVE);
        recall.setApprovedBy(username);
        recall.setUpdatedAt(LocalDateTime.now());
        recall.getBatch().setStatus(BatchStatus.RECALLED);
        batchRepository.save(recall.getBatch());
        recallOrderRepository.save(recall);
        notificationService.notify("Lô bị thu hồi", "Lô " + recall.getBatch().getBatchNumber() + " đã bị thu hồi.", "RECALL", "DEPARTMENT_STAFF", "/recalls/" + id);
        auditService.log(username, "ACTIVATE_RECALL", "RECALL_ORDER", recall.getRecallCode(), "Kích hoạt thu hồi lô");
    }

    @Transactional
    public void cancelRecall(Long id, String reason, String username) {
        RecallOrder recall = recallOrderRepository.findById(id).orElseThrow();
        ensure(recall.canCancel(), "Chỉ được hủy lệnh thu hồi nháp");
        ensure(reason != null && !reason.isBlank(), "Phải nhập lý do hủy");
        recall.setStatus(RecallStatus.CANCELLED);
        recall.setNote(reason.trim());
        recall.setUpdatedAt(LocalDateTime.now());
        recallOrderRepository.save(recall);
        auditService.log(username, "CANCEL_RECALL", "RECALL_ORDER", recall.getRecallCode(),
                "Hủy lệnh thu hồi: " + reason.trim());
    }

    @Transactional
    public void respondRecall(Long recallId, String department, int remaining, int used, int returned, String note, String username) {
        ensure(remaining >= 0 && used >= 0 && returned >= 0, "Số lượng phản hồi không được âm");
        RecallOrder recall = recallOrderRepository.findById(recallId).orElseThrow();
        ensure(recall.getStatus() == RecallStatus.ACTIVE, "Lệnh thu hồi chưa active");
        RecallOrderLine recallLine = recall.getLines().stream()
                .filter(line -> line.getDepartment().equals(department))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Khoa " + department + " không nằm trong lệnh thu hồi này"));
        int cumulativeReturned = recallLine.getReturnedQuantity() + returned;
        ensure(remaining + used + cumulativeReturned <= recallLine.getIssuedQuantity(),
                "Tổng số còn lại, đã dùng và trả về không được vượt số lượng đã cấp");
        RecallDepartmentResponse response = new RecallDepartmentResponse();
        response.setRecallOrder(recall);
        response.setDepartment(department);
        response.setRespondedBy(username);
        response.setRemainingQuantity(remaining);
        response.setUsedQuantity(used);
        response.setReturnedQuantity(returned);
        response.setNote(note);
        recallResponseRepository.save(response);
        recallLine.setRemainingQuantity(remaining);
        recallLine.setReturnedQuantity(cumulativeReturned);
        recallLine.setStatus("RESPONDED");
        if (returned > 0) {
            MaterialBatch batch = recall.getBatch();
            departmentStockService.recordRecallReturn(recallId, recall.getRecallCode(), department, recall.getMaterial(), batch,
                    returned, note, username);
            applyQuantityDelta(recall.getMaterial(), batch, batch.getWarehouse(), batch.getLocation(), returned, username,
                    MovementType.RECALL, "RECALL", recall.getRecallCode());
        }
        recallOrderRepository.save(recall);
        auditService.log(username, "RESPOND_RECALL", "RECALL_ORDER", recall.getRecallCode(), "Khoa phản hồi thu hồi");
    }

    @Transactional
    public void completeRecall(Long id, String username) {
        RecallOrder recall = recallOrderRepository.findById(id).orElseThrow();
        ensure(recall.canComplete(), "Chỉ hoàn tất lệnh đang thu hồi sau khi tất cả khoa đã phản hồi");
        recall.setStatus(RecallStatus.COMPLETED);
        recall.setUpdatedAt(LocalDateTime.now());
        recallOrderRepository.save(recall);
        auditService.log(username, "COMPLETE_RECALL", "RECALL_ORDER", recall.getRecallCode(), "Hoàn tất thu hồi");
    }

    @Transactional
    public DestructionSlip createDestruction(Long balanceId, int quantity, DestructionReason reason, String note, String username) {
        ensure(quantity > 0, "Số lượng hủy phải lớn hơn 0");
        StockBalance balance = balanceRepository.findById(balanceId).orElseThrow();
        ensure(balance.getAvailableQuantity() >= quantity, "Không được hủy vượt tồn khả dụng");
        DestructionSlip slip = new DestructionSlip();
        slip.setDestructionCode(nextCode("HUY", code -> destructionSlipRepository.existsByDestructionCode(code)));
        slip.setReason(reason.name());
        slip.setNote(note);
        slip.setCreatedBy(username);
        DestructionSlipLine line = new DestructionSlipLine();
        line.setDestructionSlip(slip);
        line.setMaterial(balance.getMaterial());
        line.setBatch(balance.getBatch());
        line.setWarehouse(balance.getWarehouse());
        line.setLocation(balance.getLocation());
        line.setQuantity(quantity);
        line.setReason(reason);
        line.setNote(note);
        slip.getLines().add(line);
        destructionSlipRepository.save(slip);
        return slip;
    }

    @Transactional
    public void submitDestruction(Long id, String username) {
        DestructionSlip slip = destructionSlipRepository.findById(id).orElseThrow();
        ensure(slip.getStatus() == DestructionStatus.DRAFT, "Chỉ gửi duyệt phiếu nháp");
        slip.setStatus(DestructionStatus.SUBMITTED);
        slip.setUpdatedAt(LocalDateTime.now());
        destructionSlipRepository.save(slip);
        notificationService.notify("Phiếu hủy cần duyệt", slip.getDestructionCode() + " đang chờ duyệt.", "DESTRUCTION", "MANAGER", "/destructions/" + id);
        auditService.log(username, "SUBMIT_DESTRUCTION", "DESTRUCTION", slip.getDestructionCode(), "Gửi duyệt phiếu hủy");
    }

    @Transactional
    public void approveDestruction(Long id, String username) {
        DestructionSlip slip = destructionSlipRepository.findById(id).orElseThrow();
        ensure(slip.getStatus() == DestructionStatus.SUBMITTED, "Phiếu không ở trạng thái chờ quản lý duyệt");
        ensureCanApprove(username, slip.getCreatedBy());
        LocalDateTime now = LocalDateTime.now();
        slip.setManagerApprovedBy(username);
        slip.setManagerApprovedAt(now);
        slip.setApprovedBy(username);
        slip.setApprovedAt(now);
        if (requiresAccountant(slip)) {
            slip.setStatus(DestructionStatus.APPROVED_BY_MANAGER);
            notificationService.notify("Phiếu hủy chờ kế toán duyệt", slip.getDestructionCode() + " đã được quản lý duyệt và cần kế toán duyệt bước 2.", "DESTRUCTION_ACCOUNTANT", "ACCOUNTANT", "/destructions/" + id);
        } else {
            slip.setStatus(DestructionStatus.APPROVED);
        }
        slip.setUpdatedAt(now);
        destructionSlipRepository.save(slip);
        auditService.log(username, "APPROVE_DESTRUCTION", "DESTRUCTION", slip.getDestructionCode(), "Duyệt phiếu hủy");
    }

    @Transactional
    public void approveDestructionByAccountant(Long id, String username) {
        DestructionSlip slip = destructionSlipRepository.findById(id).orElseThrow();
        ensure(slip.getStatus() == DestructionStatus.APPROVED_BY_MANAGER, "Phiếu chưa được quản lý duyệt hoặc không cần kế toán duyệt");
        ensureAccountant(username);
        ensureCanApprove(username, slip.getCreatedBy());
        LocalDateTime now = LocalDateTime.now();
        slip.setAccountantApprovedBy(username);
        slip.setAccountantApprovedAt(now);
        slip.setStatus(DestructionStatus.APPROVED_BY_ACCOUNTANT);
        slip.setUpdatedAt(now);
        destructionSlipRepository.save(slip);
        auditService.log(username, "APPROVE_DESTRUCTION_ACCOUNTANT", "DESTRUCTION", slip.getDestructionCode(), "Kế toán duyệt bước 2 phiếu hủy");
    }

    @Transactional
    public void rejectDestruction(Long id, String reason, String username) {
        DestructionSlip slip = destructionSlipRepository.findById(id).orElseThrow();
        ensure(slip.canReject(), "Chỉ từ chối phiếu đang chờ duyệt");
        String normalizedReason = requireDecisionReason(reason, "Phải nhập lý do từ chối");
        LocalDateTime now = LocalDateTime.now();
        slip.setStatus(DestructionStatus.REJECTED);
        slip.setRejectedBy(username);
        slip.setRejectedAt(now);
        slip.setRejectedReason(normalizedReason);
        slip.setUpdatedAt(now);
        destructionSlipRepository.save(slip);
        auditService.log(username, "REJECT_DESTRUCTION", "DESTRUCTION", slip.getDestructionCode(),
                "Từ chối phiếu hủy: " + normalizedReason);
    }

    @Transactional
    public void cancelDestruction(Long id, String reason, String username) {
        DestructionSlip slip = destructionSlipRepository.findById(id).orElseThrow();
        ensure(slip.canCancel(), "Chỉ hủy phiếu hủy vật tư ở trạng thái nháp");
        String normalizedReason = requireDecisionReason(reason, "Phải nhập lý do hủy phiếu");
        slip.setStatus(DestructionStatus.CANCELLED);
        slip.setNote(appendDecision(slip.getNote(), "Đã hủy phiếu", normalizedReason));
        slip.setUpdatedAt(LocalDateTime.now());
        destructionSlipRepository.save(slip);
        auditService.log(username, "CANCEL_DESTRUCTION", "DESTRUCTION", slip.getDestructionCode(),
                "Hủy phiếu hủy vật tư: " + normalizedReason);
    }

    @Transactional
    public void destroy(Long id, String username) {
        DestructionSlip slip = destructionSlipRepository.findById(id).orElseThrow();
        ensure(slip.getStatus() == DestructionStatus.APPROVED_BY_ACCOUNTANT || slip.getStatus() == DestructionStatus.APPROVED, "Phiếu phải hoàn tất duyệt hai bước trước khi hủy");
        for (DestructionSlipLine line : slip.getLines()) {
            applyQuantityDelta(line.getMaterial(), line.getBatch(), line.getWarehouse(), line.getLocation(), -line.getQuantity(), username,
                    MovementType.DESTROY, "DESTRUCTION", slip.getDestructionCode());
            if (line.getBatch().getQuantity() == 0) {
                line.getBatch().setStatus(BatchStatus.DESTROYED);
                batchRepository.save(line.getBatch());
            }
        }
        slip.setStatus(DestructionStatus.DESTROYED);
        slip.setDestroyedAt(LocalDateTime.now());
        slip.setUpdatedAt(LocalDateTime.now());
        destructionSlipRepository.save(slip);
        auditService.log(username, "DESTROY_STOCK", "DESTRUCTION", slip.getDestructionCode(), "Thực hiện hủy vật tư");
    }

    @Transactional
    public PurchaseRequest createPurchaseRequestFromLowStock(String username) {
        PurchaseRequest request = new PurchaseRequest();
        request.setRequestCode(nextCode("MH", code -> purchaseRequestRepository.existsByRequestCode(code)));
        request.setReason("Tự động dự trù vật tư dưới tồn tối thiểu");
        request.setCreatedBy(username);
        for (Material material : materialRepository.findByDeletedFalseOrderByCodeAsc()) {
            if (material.getActualQuantity() < material.getMinStock()) {
                PurchaseRequestLine line = new PurchaseRequestLine();
                line.setPurchaseRequest(request);
                line.setMaterial(material);
                int suggested = Math.max(material.getMinStock() * 2 - material.getActualQuantity(), material.getMinStock());
                line.setSuggestedQuantity(suggested);
                line.setRequestedQuantity(suggested);
                request.getLines().add(line);
            }
        }
        ensure(!request.getLines().isEmpty(), "Không có vật tư dưới tồn tối thiểu");
        purchaseRequestRepository.save(request);
        notificationService.notify("Đề nghị mua cần duyệt", request.getRequestCode() + " được tạo từ cảnh báo tồn thấp.", "PURCHASE", "MANAGER", "/purchases/requests/" + request.getId());
        return request;
    }

    @Transactional
    public void approvePurchaseRequest(Long id, String username) {
        PurchaseRequest request = purchaseRequestRepository.findById(id).orElseThrow();
        ensure(request.canApprove(), "Chỉ duyệt được đề nghị mua đang nháp hoặc đã gửi");
        ensureCanApprove(username, request.getCreatedBy());
        request.setStatus(PurchaseRequestStatus.APPROVED);
        request.setApprovedBy(username);
        request.setApprovedAt(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());
        purchaseRequestRepository.save(request);
        auditService.log(username, "APPROVE_PURCHASE_REQUEST", "PURCHASE_REQUEST", request.getRequestCode(), "Duyệt đề nghị mua");
    }

    @Transactional
    public void cancelPurchaseRequest(Long id, String username, String reason) {
        PurchaseRequest request = purchaseRequestRepository.findById(id).orElseThrow();
        ensure(request.canCancel(), "Chỉ hủy được đề nghị mua chưa duyệt");
        String cancellationReason = requireDecisionReason(reason, "Phải nhập lý do hủy đề nghị mua");
        request.setStatus(PurchaseRequestStatus.CANCELLED);
        request.setReason(appendDecision(request.getReason(), "Đã hủy đề nghị", cancellationReason));
        request.setUpdatedAt(LocalDateTime.now());
        purchaseRequestRepository.save(request);
        auditService.log(username, "CANCEL_PURCHASE_REQUEST", "PURCHASE_REQUEST", request.getRequestCode(),
                "Hủy đề nghị mua: " + cancellationReason);
    }

    @Transactional
    public void rejectPurchaseRequest(Long id, String username, String reason) {
        PurchaseRequest request = purchaseRequestRepository.findById(id).orElseThrow();
        ensure(request.canReject(), "Chỉ từ chối được đề nghị mua chưa duyệt");
        String decisionReason = requireDecisionReason(reason, "Lý do từ chối là bắt buộc");
        ensureCanApprove(username, request.getCreatedBy());
        request.setStatus(PurchaseRequestStatus.REJECTED);
        request.setApprovedBy(username);
        request.setApprovedAt(LocalDateTime.now());
        request.setReason(appendDecision(request.getReason(), "Từ chối", decisionReason));
        request.setUpdatedAt(LocalDateTime.now());
        purchaseRequestRepository.save(request);
        auditService.log(username, "REJECT_PURCHASE_REQUEST", "PURCHASE_REQUEST", request.getRequestCode(),
                "Từ chối đề nghị mua: " + decisionReason);
    }

    @Transactional
    public PurchaseOrder createPurchaseOrder(Long purchaseRequestId, Long supplierId, LocalDate expectedDate, String username) {
        PurchaseRequest request = purchaseRequestRepository.findById(purchaseRequestId).orElseThrow();
        ensure(request.getStatus() == PurchaseRequestStatus.APPROVED, "Đề nghị mua phải được duyệt");
        PurchaseOrder order = new PurchaseOrder();
        order.setOrderCode(nextCode("DH", code -> purchaseOrderRepository.existsByOrderCode(code)));
        order.setSupplier(supplierRepository.findById(supplierId).orElseThrow());
        order.setExpectedDeliveryDate(expectedDate);
        order.setCreatedBy(username);
        for (PurchaseRequestLine requestLine : request.getLines()) {
            PurchaseOrderLine line = new PurchaseOrderLine();
            line.setPurchaseOrder(order);
            line.setMaterial(requestLine.getMaterial());
            line.setOrderedQuantity(requestLine.getRequestedQuantity());
            line.setUnitPrice(requestLine.getMaterial().getEstimatedUnitPrice());
            order.getLines().add(line);
        }
        purchaseOrderRepository.save(order);
        return order;
    }

    @Transactional
    public void sendPurchaseOrder(Long id, String username) {
        PurchaseOrder order = purchaseOrderRepository.findWithLinesById(id).orElseThrow();
        ensure(order.canSend(), "Chỉ gửi được đơn đang nháp");
        ensure(!order.getLines().isEmpty(), "Đơn đặt hàng phải có ít nhất một dòng");
        order.setStatus(PurchaseOrderStatus.SENT);
        purchaseOrderRepository.save(order);
        auditService.log(username, "SEND_PURCHASE_ORDER", "PURCHASE_ORDER", order.getOrderCode(), "Gửi đơn đặt hàng cho nhà cung cấp");
    }

    @Transactional
    public void cancelPurchaseOrder(Long id, String username, String reason) {
        PurchaseOrder order = purchaseOrderRepository.findWithLinesById(id).orElseThrow();
        ensure(order.canCancel(), "Chỉ hủy được đơn nháp hoặc đơn chưa nhận hàng");
        String cancellationReason = requireDecisionReason(reason, "Phải nhập lý do hủy đơn đặt hàng");
        order.setStatus(PurchaseOrderStatus.CANCELLED);
        order.setNote(appendDecision(order.getNote(), "Đã hủy đơn", cancellationReason));
        purchaseOrderRepository.save(order);
        auditService.log(username, "CANCEL_PURCHASE_ORDER", "PURCHASE_ORDER", order.getOrderCode(),
                "Hủy đơn đặt hàng: " + cancellationReason);
    }

    @Transactional
    public Receipt recordPurchaseOrderReceipt(Long id, Long warehouseId, Long locationId, Map<String, String> parameters, String username) {
        PurchaseOrder order = purchaseOrderRepository.findWithLinesById(id).orElseThrow();
        ensure(order.canReceive(),
                "Chỉ ghi nhận nhận hàng cho đơn đã gửi hoặc đã nhận một phần");
        Warehouse warehouse = warehouseRepository.findById(warehouseId).orElseThrow();
        StorageLocation location = locationRepository.findById(locationId).orElseThrow();
        ensure(location.getWarehouse() != null && location.getWarehouse().getId().equals(warehouse.getId()),
                "Vị trí nhập phải thuộc kho đã chọn");

        Receipt receipt = new Receipt();
        receipt.setReceiptCode(nextCode("PN", code -> receiptRepository.existsByReceiptCode(code)));
        receipt.setSupplier(order.getSupplier());
        receipt.setWarehouse(warehouse);
        receipt.setCreatedBy(username);
        receipt.setNote("Nhận hàng từ đơn " + order.getOrderCode());

        boolean changed = false;
        for (PurchaseOrderLine line : order.getLines()) {
            String value = parameters.get("received_" + line.getId());
            if (value == null || value.isBlank()) {
                continue;
            }
            int receivedNow = Integer.parseInt(value);
            ensure(receivedNow >= 0, "Số lượng nhận không được âm");
            if (receivedNow == 0) {
                continue;
            }
            int remaining = line.getOrderedQuantity() - line.getReceivedQuantity();
            ensure(receivedNow <= remaining, "Số lượng nhận không được vượt số còn lại của đơn");
            line.setReceivedQuantity(line.getReceivedQuantity() + receivedNow);

            ReceiptLine receiptLine = new ReceiptLine();
            receiptLine.setReceipt(receipt);
            receiptLine.setMaterial(line.getMaterial());
            receiptLine.setBatchNumber(nextReceiptBatchNumber(order, line, receipt.getLines().size() + 1));
            receiptLine.setQuantity(receivedNow);
            receiptLine.setUnitPrice(line.getUnitPrice());
            receiptLine.setLocation(location);
            receiptLine.setNote("Từ đơn " + order.getOrderCode());
            receipt.getLines().add(receiptLine);
            changed = true;
        }
        ensure(changed, "Phải nhập số lượng nhận lớn hơn 0");
        int orderedTotal = order.getLines().stream().mapToInt(PurchaseOrderLine::getOrderedQuantity).sum();
        int receivedTotal = order.getLines().stream().mapToInt(PurchaseOrderLine::getReceivedQuantity).sum();
        order.setStatus(receivedTotal >= orderedTotal ? PurchaseOrderStatus.RECEIVED : PurchaseOrderStatus.PARTIALLY_RECEIVED);
        if (order.getStatus() == PurchaseOrderStatus.RECEIVED) {
            order.setReceivedAt(LocalDateTime.now());
        }
        purchaseOrderRepository.save(order);
        receiptRepository.save(receipt);
        auditService.log(username, "RECEIVE_PURCHASE_ORDER", "PURCHASE_ORDER", order.getOrderCode(), "Ghi nhận nhà cung cấp giao hàng");
        auditService.log(username, "CREATE_RECEIPT_FROM_PURCHASE", "RECEIPT", receipt.getReceiptCode(), "Tạo phiếu nhập nháp từ đơn " + order.getOrderCode());
        return receipt;
    }

    private String nextReceiptBatchNumber(PurchaseOrder order, PurchaseOrderLine line, int sequence) {
        String code = order.getOrderCode() == null ? "DH" : order.getOrderCode().replaceAll("[^A-Za-z0-9-]", "");
        String material = line.getMaterial() == null ? "VT" : line.getMaterial().getCode().replaceAll("[^A-Za-z0-9-]", "");
        String base = ("LO-" + code + "-" + material + "-" + sequence);
        if (base.length() > 70) {
            base = base.substring(0, 70);
        }
        String candidate = base;
        int index = 1;
        while (batchNumberExists(line.getMaterial().getId(), candidate)
                || receiptBatchNumberExists(candidate)) {
            candidate = base + "-" + (++index);
        }
        return candidate;
    }

    private boolean batchNumberExists(Long materialId, String batchNumber) {
        return batchRepository.findByMaterial_IdAndBatchNumber(materialId, batchNumber).isPresent();
    }

    private boolean receiptBatchNumberExists(String batchNumber) {
        return receiptRepository.findAll().stream()
                .flatMap(receipt -> receipt.getLines().stream())
                .anyMatch(receiptLine -> batchNumber.equals(receiptLine.getBatchNumber()));
    }

    private void moveBetweenBalances(Material material, MaterialBatch batch, Warehouse fromWarehouse, Warehouse toWarehouse,
                                     StorageLocation fromLocation, StorageLocation toLocation, int quantity, String username, String code) {
        applyBalanceOnly(material, batch, fromWarehouse, fromLocation, -quantity);
        applyBalanceOnly(material, batch, toWarehouse, toLocation, quantity);
        movementRepository.save(movement(MovementType.TRANSFER_OUT, material, batch, fromWarehouse, fromLocation, -quantity, material.getActualQuantity(), material.getActualQuantity(), "TRANSFER", code, username));
        movementRepository.save(movement(MovementType.TRANSFER_IN, material, batch, toWarehouse, toLocation, quantity, material.getActualQuantity(), material.getActualQuantity(), "TRANSFER", code, username));
    }

    private void applyQuantityDelta(Material material, MaterialBatch batch, Warehouse warehouse, StorageLocation location, int delta,
                                    String username, MovementType type, String refType, String refCode) {
        int before = inventorySyncService.syncMaterialActualQuantity(material);
        int projectedAfter = before + delta;
        ensure(projectedAfter >= 0, "Không cho tồn vật tư âm");
        applyBalanceOnly(material, batch, warehouse, location, delta);
        int batchAfter = batch.getQuantity() + delta;
        ensure(batchAfter >= 0, "Không cho tồn lô âm");
        batch.setQuantity(batchAfter);
        batch.setUpdatedAt(LocalDateTime.now());
        batchRepository.save(batch);
        int after = inventorySyncService.syncMaterialActualQuantity(material);
        movementRepository.save(movement(type, material, batch, warehouse, location, delta, before, after, refType, refCode, username));
    }

    private void applyAdjustment(StockAdjustment adjustment, String username) {
        for (StockAdjustmentLine line : adjustment.getLines()) {
            applyQuantityDelta(line.getMaterial(), line.getBatch(), adjustment.getWarehouse(), line.getLocation(), line.getAdjustmentQuantity(), username,
                    line.getAdjustmentQuantity() >= 0 ? MovementType.ADJUSTMENT_IN : MovementType.ADJUSTMENT_OUT, "ADJUSTMENT", adjustment.getAdjustmentCode());
        }
    }

    private boolean requiresAccountant(StockAdjustment adjustment) {
        return adjustment.getLines().stream()
                .anyMatch(line -> line.getAdjustmentQuantity() < 0 || estimatedLineValue(line.getMaterial(), Math.abs(line.getAdjustmentQuantity())) >= 1_000_000L);
    }

    private boolean requiresAccountant(DestructionSlip slip) {
        return slip.getLines().stream()
                .anyMatch(line -> estimatedLineValue(line.getMaterial(), line.getQuantity()) >= 1_000_000L);
    }

    private long estimatedLineValue(Material material, int quantity) {
        if (material.getEstimatedUnitPrice() == null) {
            return 0L;
        }
        return material.getEstimatedUnitPrice().multiply(java.math.BigDecimal.valueOf(quantity)).longValue();
    }

    private void ensureCanApprove(String approver, String creator) {
        if (creator != null && creator.equalsIgnoreCase(approver) && !isAdmin(approver)) {
            throw new IllegalStateException("Người lập phiếu không được tự duyệt phiếu của chính mình");
        }
    }

    private void ensureAccountant(String username) {
        AppUser user = userRepository.findByUsername(username).orElseThrow();
        ensure(user.getRole() == UserRole.ACCOUNTANT || user.getRole() == UserRole.ADMIN, "Chỉ kế toán hoặc quản trị viên được duyệt bước 2");
    }

    private boolean isAdmin(String username) {
        return userRepository.findByUsername(username)
                .map(user -> user.getRole() == UserRole.ADMIN)
                .orElse(false);
    }

    private void applyBalanceOnly(Material material, MaterialBatch batch, Warehouse warehouse, StorageLocation location, int delta) {
        StockBalance balance = balanceRepository.findByMaterial_IdAndBatch_IdAndWarehouse_IdAndLocation_Id(
                material.getId(), batch.getId(), warehouse.getId(), location.getId()).orElseGet(() -> {
            StockBalance created = new StockBalance();
            created.setMaterial(material);
            created.setBatch(batch);
            created.setWarehouse(warehouse);
            created.setLocation(location);
            return created;
        });
        balance.setActualQuantity(balance.getActualQuantity() + delta);
        balance.setUpdatedAt(LocalDateTime.now());
        balance.validate();
        balanceRepository.save(balance);
    }

    private StockMovement movement(MovementType type, Material material, MaterialBatch batch, Warehouse warehouse, StorageLocation location,
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

    private String nextCode(String prefix, java.util.function.Predicate<String> exists) {
        String code = prefix + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return exists.test(code) ? code + "-1" : code;
    }

    private String requireDecisionReason(String reason, String message) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return reason.trim();
    }

    private String appendDecision(String current, String action, String reason) {
        String decision = action + ": " + reason;
        return current == null || current.isBlank() ? decision : current + System.lineSeparator() + decision;
    }

    private void ensure(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}

