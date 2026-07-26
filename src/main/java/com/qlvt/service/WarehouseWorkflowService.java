package com.qlvt.service;

import com.qlvt.entity.*;
import com.qlvt.enums.*;
import com.qlvt.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class WarehouseWorkflowService {
    private final MaterialRepository materialRepository;
    private final MaterialBatchRepository batchRepository;
    private final WarehouseRepository warehouseRepository;
    private final StorageLocationRepository locationRepository;
    private final SupplierRepository supplierRepository;
    private final MaterialRequestRepository requestRepository;
    private final StockBalanceRepository balanceRepository;
    private final StockReservationRepository reservationRepository;
    private final RequestApprovalLogRepository approvalLogRepository;
    private final ReceiptRepository receiptRepository;
    private final IssueSlipRepository issueSlipRepository;
    private final StockMovementRepository movementRepository;
    private final AuditService auditService;
    private final DepartmentStockService departmentStockService;
    private final PriceHistoryService priceHistoryService;
    private final InventoryAlertService inventoryAlertService;
    private final InventorySyncService inventorySyncService;

    public WarehouseWorkflowService(MaterialRepository materialRepository,
                                    MaterialBatchRepository batchRepository,
                                    WarehouseRepository warehouseRepository,
                                    StorageLocationRepository locationRepository,
                                    SupplierRepository supplierRepository,
                                    MaterialRequestRepository requestRepository,
                                    StockBalanceRepository balanceRepository,
                                    StockReservationRepository reservationRepository,
                                    RequestApprovalLogRepository approvalLogRepository,
                                    ReceiptRepository receiptRepository,
                                    IssueSlipRepository issueSlipRepository,
                                    StockMovementRepository movementRepository,
                                    AuditService auditService,
                                    DepartmentStockService departmentStockService,
                                    PriceHistoryService priceHistoryService,
                                    InventoryAlertService inventoryAlertService,
                                    InventorySyncService inventorySyncService) {
        this.materialRepository = materialRepository;
        this.batchRepository = batchRepository;
        this.warehouseRepository = warehouseRepository;
        this.locationRepository = locationRepository;
        this.supplierRepository = supplierRepository;
        this.requestRepository = requestRepository;
        this.balanceRepository = balanceRepository;
        this.reservationRepository = reservationRepository;
        this.approvalLogRepository = approvalLogRepository;
        this.receiptRepository = receiptRepository;
        this.issueSlipRepository = issueSlipRepository;
        this.movementRepository = movementRepository;
        this.auditService = auditService;
        this.departmentStockService = departmentStockService;
        this.priceHistoryService = priceHistoryService;
        this.inventoryAlertService = inventoryAlertService;
        this.inventorySyncService = inventorySyncService;
    }

    @Transactional
    public void syncBalancesFromBatches() {
        for (MaterialBatch batch : batchRepository.findAll()) {
            if (batch.getLocation() == null || batch.getWarehouse() == null || batch.getQuantity() <= 0) {
                continue;
            }
            StockBalance balance = findOrCreateBalance(batch.getMaterial(), batch, batch.getWarehouse(), batch.getLocation());
            balance.setActualQuantity(batch.getQuantity());
            balance.validate();
            balanceRepository.save(balance);
        }
    }

    @Transactional
    public Receipt createReceipt(Long materialId, Long warehouseId, Long locationId, Long supplierId,
                                 LocalDate manufactureDate, LocalDate expiryDate,
                                 int quantity, BigDecimal unitPrice, String note, String username) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng nhập phải lớn hơn 0");
        }
        Material material = materialRepository.findById(materialId).orElseThrow();
        Warehouse warehouse = warehouseRepository.findById(warehouseId).orElseThrow();
        StorageLocation location = locationRepository.findById(locationId).orElseThrow();
        Supplier supplier = supplierId == null ? null : supplierRepository.findById(supplierId).orElse(null);

        Receipt receipt = new Receipt();
        receipt.setReceiptCode(nextReceiptCode());
        receipt.setSupplier(supplier);
        receipt.setWarehouse(warehouse);
        receipt.setCreatedBy(username);
        receipt.setNote(note);

        ReceiptLine line = new ReceiptLine();
        line.setReceipt(receipt);
        line.setMaterial(material);
        line.setBatchNumber(nextBatchNumber(receipt.getReceiptCode()));
        line.setManufacturingDate(manufactureDate);
        line.setExpiryDate(expiryDate);
        line.setQuantity(quantity);
        line.setUnitPrice(unitPrice == null ? BigDecimal.ZERO : unitPrice);
        line.setLocation(location);
        line.setNote(note);
        receipt.getLines().add(line);

        receiptRepository.save(receipt);
        auditService.log(username, "CREATE_RECEIPT", "RECEIPT", receipt.getReceiptCode(), "Tạo phiếu nhập kho");
        return receipt;
    }

    @Transactional
    public Receipt updateReceiptDraft(Long receiptId, Long materialId, Long warehouseId, Long locationId, Long supplierId,
                                      LocalDate manufactureDate, LocalDate expiryDate,
                                      int quantity, BigDecimal unitPrice, String note, String username) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng nhập phải lớn hơn 0");
        }
        Receipt receipt = receiptRepository.findById(receiptId).orElseThrow();
        if (receipt.getStatus() != ReceiptStatus.DRAFT) {
            throw new IllegalStateException("Chỉ sửa phiếu nhập khi chưa xác nhận nhập kho");
        }
        Material material = materialRepository.findById(materialId).orElseThrow();
        Warehouse warehouse = warehouseRepository.findById(warehouseId).orElseThrow();
        StorageLocation location = locationRepository.findById(locationId).orElseThrow();
        Supplier supplier = supplierId == null ? null : supplierRepository.findById(supplierId).orElse(null);
        String batchNumber = receipt.getLines().isEmpty()
                ? nextBatchNumber(receipt.getReceiptCode())
                : receipt.getLines().get(0).getBatchNumber();

        receipt.setWarehouse(warehouse);
        receipt.setSupplier(supplier);
        receipt.setNote(note);
        receipt.setUpdatedAt(LocalDateTime.now());

        receipt.getLines().clear();
        ReceiptLine line = new ReceiptLine();
        line.setReceipt(receipt);
        line.setMaterial(material);
        line.setBatchNumber(batchNumber);
        line.setManufacturingDate(manufactureDate);
        line.setExpiryDate(expiryDate);
        line.setQuantity(quantity);
        line.setUnitPrice(unitPrice == null ? BigDecimal.ZERO : unitPrice);
        line.setLocation(location);
        line.setNote(note);
        receipt.getLines().add(line);

        receiptRepository.save(receipt);
        auditService.log(username, "UPDATE_RECEIPT", "RECEIPT", receipt.getReceiptCode(), "Sửa phiếu nhập kho");
        return receipt;
    }

    @Transactional
    public void confirmReceipt(Long receiptId, String username) {
        Receipt receipt = receiptRepository.findById(receiptId).orElseThrow();
        if (receipt.getStatus() != ReceiptStatus.DRAFT) {
            throw new IllegalStateException("Chỉ phiếu nháp mới được xác nhận");
        }
        for (ReceiptLine line : receipt.getLines()) {
            Material material = line.getMaterial();
            int before = inventorySyncService.syncMaterialActualQuantity(material);
            MaterialBatch batch = batchRepository.findByMaterial_IdAndBatchNumber(material.getId(), line.getBatchNumber()).orElseGet(MaterialBatch::new);
            boolean isNewBatch = batch.getId() == null;
            batch.setMaterial(material);
            batch.setWarehouse(receipt.getWarehouse());
            batch.setLocation(line.getLocation());
            batch.setSupplier(receipt.getSupplier());
            batch.setBatchNumber(line.getBatchNumber());
            batch.setManufactureDate(line.getManufacturingDate());
            batch.setExpiryDate(line.getExpiryDate());
            batch.setReceiptDate(receipt.getReceiptDate());
            if (isNewBatch) {
                batch.setInitialQuantity(line.getQuantity());
            } else {
                batch.setInitialQuantity(batch.getInitialQuantity() + line.getQuantity());
            }
            batch.setQuantity(batch.getQuantity() + line.getQuantity());
            batch.setUpdatedAt(LocalDateTime.now());
            batchRepository.save(batch);

            StockBalance balance = findOrCreateBalance(material, batch, receipt.getWarehouse(), line.getLocation());
            balance.setActualQuantity(balance.getActualQuantity() + line.getQuantity());
            balance.setUpdatedAt(LocalDateTime.now());
            balance.validate();
            balanceRepository.save(balance);

            int after = inventorySyncService.syncMaterialActualQuantity(material);
            movementRepository.save(movement(MovementType.IN, material, batch, receipt.getWarehouse(), line.getLocation(),
                    line.getQuantity(), before, after, "RECEIPT", receipt.getReceiptCode(), username));
            priceHistoryService.recordReceiptLine(receipt, line, username);
        }
        receipt.setStatus(ReceiptStatus.CONFIRMED);
        receipt.setConfirmedBy(username);
        receipt.setConfirmedAt(LocalDateTime.now());
        receipt.setUpdatedAt(LocalDateTime.now());
        receiptRepository.save(receipt);
        auditService.log(username, "CONFIRM_RECEIPT", "RECEIPT", receipt.getReceiptCode(), "Xác nhận nhập kho");
    }

    @Transactional
    public void cancelReceipt(Long receiptId, String cancellationReason, String username) {
        Receipt receipt = receiptRepository.findById(receiptId).orElseThrow();
        if (!receipt.canCancel()) {
            throw new IllegalStateException("Chỉ hủy phiếu nhập nháp");
        }
        String reason = cancellationReason == null ? "" : cancellationReason.trim();
        if (reason.isEmpty()) {
            throw new IllegalArgumentException("Cần nhập lý do hủy");
        }

        receipt.setStatus(ReceiptStatus.CANCELLED);
        receipt.setNote("Hủy: " + reason);
        receipt.setUpdatedAt(LocalDateTime.now());
        receiptRepository.save(receipt);
        auditService.log(username, "CANCEL_RECEIPT", "RECEIPT", receipt.getReceiptCode(),
                "Hủy phiếu nhập kho: " + reason);
    }

    @Transactional
    public void approveDepartment(Long requestId, String username) {
        MaterialRequest request = requestRepository.findById(requestId).orElseThrow();
        if (!request.canApproveDepartment()) {
            throw new IllegalStateException("Yêu cầu không ở trạng thái chờ trưởng khoa duyệt");
        }
        request.setStatus(RequestStatus.DEPARTMENT_APPROVED);
        request.setDepartmentApprovedBy(username);
        request.setDepartmentApprovedAt(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());
        requestRepository.save(request);
        logApproval(request, "DEPARTMENT_APPROVED", username, "Trưởng khoa duyệt yêu cầu");
        auditService.log(username, "DEPARTMENT_APPROVE", "MATERIAL_REQUEST", request.getCode(), "Trưởng khoa duyệt yêu cầu");
    }

    @Transactional
    public void reserveForRequest(Long requestId, String username) {
        MaterialRequest request = requestRepository.findById(requestId).orElseThrow();
        if (!request.canReserveStock()) {
            throw new IllegalStateException("Yêu cầu phải được điều dưỡng gửi sang kho trước khi giữ hàng");
        }
        if (!reservationRepository.findByMaterialRequest_IdAndStatus(requestId, ReservationStatus.ACTIVE).isEmpty()) {
            throw new IllegalStateException("Yêu cầu đã có tồn giữ");
        }

        boolean partial = false;
        for (MaterialRequestLine line : request.getLines()) {
            int remaining = line.getRequestedQuantity();
            int approved = 0;
            for (StockBalance balance : balanceRepository.findAvailableFefo(line.getMaterial().getId(), LocalDate.now())) {
                if (remaining == 0) {
                    break;
                }
                int take = Math.min(balance.getAvailableQuantity(), remaining);
                if (take <= 0) {
                    continue;
                }
                balance.setReservedQuantity(balance.getReservedQuantity() + take);
                balance.setUpdatedAt(LocalDateTime.now());
                balance.validate();
                balanceRepository.save(balance);

                StockReservation reservation = new StockReservation();
                reservation.setMaterialRequest(request);
                reservation.setMaterialRequestLine(line);
                reservation.setMaterial(line.getMaterial());
                reservation.setBatch(balance.getBatch());
                reservation.setStockBalance(balance);
                reservation.setWarehouse(balance.getWarehouse());
                reservation.setLocation(balance.getLocation());
                reservation.setReservedQuantity(take);
                reservationRepository.save(reservation);

                approved += take;
                remaining -= take;
            }
            line.setApprovedQuantity(approved);
            line.setStatus(approved == line.getRequestedQuantity() ? "APPROVED" : "PARTIAL");
            Material material = line.getMaterial();
            material.setReservedQuantity(material.getReservedQuantity() + approved);
            materialRepository.save(material);
            partial = partial || approved < line.getRequestedQuantity();
            if (approved == 0) {
                throw new IllegalStateException("Không đủ tồn khả dụng cho vật tư " + line.getMaterial().getCode());
            }
        }
        request.setStatus(partial ? RequestStatus.PARTIALLY_APPROVED : RequestStatus.RESERVED);
        request.setWarehouseApprovedBy(username);
        request.setWarehouseApprovedAt(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());
        requestRepository.save(request);
        logApproval(request, request.getStatus().name(), username, "Kho duyệt và giữ hàng FEFO");
        auditService.log(username, "RESERVE_REQUEST", "MATERIAL_REQUEST", request.getCode(), "Kho giữ hàng theo FEFO");
    }

    @Transactional
    public void cancelRequest(Long requestId, String username, String reason) {
        MaterialRequest request = requestRepository.findById(requestId).orElseThrow();
        if (!request.canCancel()) {
            throw new IllegalStateException("Ch\u1ec9 c\u00f3 th\u1ec3 h\u1ee7y y\u00eau c\u1ea7u tr\u01b0\u1edbc khi t\u1ea1o phi\u1ebfu xu\u1ea5t kho");
        }
        LocalDateTime now = LocalDateTime.now();
        for (StockReservation reservation : reservationRepository.findByMaterialRequest_IdAndStatus(requestId, ReservationStatus.ACTIVE)) {
            StockBalance balance = reservation.getStockBalance();
            balance.setReservedQuantity(Math.max(0, balance.getReservedQuantity() - reservation.getReservedQuantity()));
            balance.setUpdatedAt(now);
            balance.validate();
            balanceRepository.save(balance);
            Material material = reservation.getMaterial();
            material.setReservedQuantity(Math.max(0, material.getReservedQuantity() - reservation.getReservedQuantity()));
            materialRepository.save(material);
            reservation.setStatus(ReservationStatus.CANCELLED);
            reservation.setReleasedAt(now);
            reservationRepository.save(reservation);
        }
        for (MaterialRequestLine line : request.getLines()) {
            line.setApprovedQuantity(0);
            line.setStatus("CANCELLED");
        }
        request.setStatus(RequestStatus.CANCELLED);
        request.setRejectedReason(reason == null || reason.isBlank() ? "Ng\u01b0\u1eddi d\u00f9ng h\u1ee7y y\u00eau c\u1ea7u" : reason.trim());
        request.setUpdatedAt(now);
        requestRepository.save(request);
        logApproval(request, "CANCELLED", username, request.getRejectedReason());
        auditService.log(username, "CANCEL_REQUEST", "MATERIAL_REQUEST", request.getCode(), "H\u1ee7y y\u00eau c\u1ea7u v\u00e0 gi\u1ea3i ph\u00f3ng t\u1ed3n \u0111\u00e3 gi\u1eef");
    }

    @Transactional
    public List<IssueSlip> createIssueSlips(Long requestId, String username) {
        MaterialRequest request = requestRepository.findById(requestId).orElseThrow();
        if (!request.canCreateIssueSlip()) {
            throw new IllegalStateException("Chỉ tạo phiếu xuất cho yêu cầu đã giữ hàng");
        }
        List<IssueSlip> existing = issueSlipRepository.findByMaterialRequest_IdOrderByIdAsc(requestId);
        if (!existing.isEmpty()) {
            return existing;
        }
        List<StockReservation> reservations = reservationRepository.findByMaterialRequest_IdAndStatus(requestId, ReservationStatus.ACTIVE);
        List<IssueSlip> created = new java.util.ArrayList<>();
        for (MaterialRequestLine requestLine : request.getLines()) {
            IssueSlip issueSlip = new IssueSlip();
            issueSlip.setIssueCode(nextIssueCode());
            issueSlip.setMaterialRequest(request);
            issueSlip.setDepartment(request.getDepartment());
            issueSlip.setCreatedBy(username);
            issueSlip.setStatus(IssueStatus.PREPARING);
            reservations.stream()
                    .filter(reservation -> reservation.getMaterialRequestLine().getId().equals(requestLine.getId()))
                    .findFirst()
                    .ifPresent(reservation -> issueSlip.setWarehouse(reservation.getWarehouse()));

            IssueSlipLine slipLine = new IssueSlipLine();
            slipLine.setIssueSlip(issueSlip);
            slipLine.setMaterial(requestLine.getMaterial());
            slipLine.setRequestedQuantity(requestLine.getRequestedQuantity());
            slipLine.setApprovedQuantity(requestLine.getApprovedQuantity());
            issueSlip.getLines().add(slipLine);

            issueSlipRepository.save(issueSlip);
            created.add(issueSlip);
            auditService.log(username, "CREATE_ISSUE", "ISSUE_SLIP", issueSlip.getIssueCode(), "Tạo phiếu xuất từ yêu cầu");
        }
        request.setStatus(RequestStatus.PREPARING);
        request.setUpdatedAt(LocalDateTime.now());
        requestRepository.save(request);
        return created;
    }

    @Transactional
    public IssueSlip createDirectIssueSlip(Long warehouseId, Long materialId, int quantity,
                                           String department, String note, String username) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng xuất phải lớn hơn 0");
        }
        Warehouse warehouse = warehouseRepository.findById(warehouseId).orElseThrow();
        Material material = materialRepository.findById(materialId).orElseThrow();
        long available = balanceRepository.sumAvailableByMaterialIdAndWarehouseId(materialId, warehouseId, LocalDate.now());
        if (available < quantity) {
            if (available == 0) {
                inventoryAlertService.notifyOutOfStockIfNeeded(material, available, "/alerts#stock-alerts");
            }
            throw new IllegalStateException(material.getCode() + " - " + material.getName()
                    + " chỉ còn " + available + " " + (material.getUnit() == null ? "" : material.getUnit())
                    + " tại " + warehouse.getName() + ", không đủ để tạo phiếu xuất " + quantity + ".");
        }

        MaterialRequest request = new MaterialRequest();
        request.setCode(nextDirectRequestCode());
        request.setDepartment(department);
        request.setRequester(username);
        request.setPriority("BINH_THUONG");
        request.setStatus(RequestStatus.PREPARING);
        request.setNote(note);
        request.setSubmittedAt(LocalDateTime.now());
        request.setWarehouseApprovedBy(username);
        request.setWarehouseApprovedAt(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());

        MaterialRequestLine requestLine = new MaterialRequestLine();
        requestLine.setRequest(request);
        requestLine.setMaterial(material);
        requestLine.setRequestedQuantity(quantity);
        requestLine.setApprovedQuantity(quantity);
        requestLine.setIssuedQuantity(0);
        requestLine.setStatus("APPROVED");
        requestLine.setReason(note);
        request.getLines().add(requestLine);
        requestRepository.save(request);

        int remaining = quantity;
        for (StockBalance balance : balanceRepository.findAvailableFefoInWarehouse(materialId, warehouseId, LocalDate.now())) {
            if (remaining == 0) {
                break;
            }
            int take = Math.min(balance.getAvailableQuantity(), remaining);
            if (take <= 0) {
                continue;
            }
            balance.setReservedQuantity(balance.getReservedQuantity() + take);
            balance.setUpdatedAt(LocalDateTime.now());
            balance.validate();
            balanceRepository.save(balance);

            StockReservation reservation = new StockReservation();
            reservation.setMaterialRequest(request);
            reservation.setMaterialRequestLine(requestLine);
            reservation.setMaterial(material);
            reservation.setBatch(balance.getBatch());
            reservation.setStockBalance(balance);
            reservation.setWarehouse(balance.getWarehouse());
            reservation.setLocation(balance.getLocation());
            reservation.setReservedQuantity(take);
            reservationRepository.save(reservation);
            remaining -= take;
        }

        material.setReservedQuantity(material.getReservedQuantity() + quantity);
        materialRepository.save(material);

        IssueSlip issueSlip = new IssueSlip();
        issueSlip.setIssueCode(nextIssueCode());
        issueSlip.setMaterialRequest(request);
        issueSlip.setDepartment(department);
        issueSlip.setWarehouse(warehouse);
        issueSlip.setCreatedBy(username);
        issueSlip.setStatus(IssueStatus.PREPARING);
        issueSlip.setNote(note);

        IssueSlipLine line = new IssueSlipLine();
        line.setIssueSlip(issueSlip);
        line.setMaterial(material);
        line.setRequestedQuantity(quantity);
        line.setApprovedQuantity(quantity);
        line.setIssuedQuantity(0);
        line.setNote(note);
        issueSlip.getLines().add(line);
        issueSlipRepository.save(issueSlip);

        auditService.log(username, "CREATE_DIRECT_ISSUE", "ISSUE_SLIP", issueSlip.getIssueCode(), "Tạo phiếu xuất trực tiếp tại trang xuất kho");
        return issueSlip;
    }

    @Transactional
    public IssueSlip createIssueSlip(Long requestId, String username) {
        MaterialRequest request = requestRepository.findById(requestId).orElseThrow();
        if (!request.canCreateIssueSlip()) {
            throw new IllegalStateException("Chỉ tạo phiếu xuất cho yêu cầu đã giữ hàng");
        }
        IssueSlip issueSlip = issueSlipRepository.findByMaterialRequest_IdOrderByIdAsc(requestId).stream().findFirst().orElseGet(IssueSlip::new);
        if (issueSlip.getId() != null) {
            return issueSlip;
        }
        issueSlip.setIssueCode(nextIssueCode());
        issueSlip.setMaterialRequest(request);
        issueSlip.setDepartment(request.getDepartment());
        issueSlip.setCreatedBy(username);
        List<StockReservation> reservations = reservationRepository.findByMaterialRequest_IdAndStatus(requestId, ReservationStatus.ACTIVE);
        if (!reservations.isEmpty()) {
            issueSlip.setWarehouse(reservations.get(0).getWarehouse());
        }
        for (MaterialRequestLine requestLine : request.getLines()) {
            IssueSlipLine slipLine = new IssueSlipLine();
            slipLine.setIssueSlip(issueSlip);
            slipLine.setMaterial(requestLine.getMaterial());
            slipLine.setRequestedQuantity(requestLine.getRequestedQuantity());
            slipLine.setApprovedQuantity(requestLine.getApprovedQuantity());
            issueSlip.getLines().add(slipLine);
        }
        issueSlip.setStatus(IssueStatus.PREPARING);
        issueSlipRepository.save(issueSlip);
        request.setStatus(RequestStatus.PREPARING);
        request.setUpdatedAt(LocalDateTime.now());
        requestRepository.save(request);
        auditService.log(username, "CREATE_ISSUE", "ISSUE_SLIP", issueSlip.getIssueCode(), "Tạo phiếu xuất từ yêu cầu");
        return issueSlip;
    }

    @Transactional
    public IssueSlip updateIssueDraft(Long issueId, Long warehouseId, Long materialId, int quantity, String note, String username) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng xuất phải lớn hơn 0");
        }
        IssueSlip issueSlip = issueSlipRepository.findById(issueId).orElseThrow();
        if (issueSlip.getStatus() != IssueStatus.PREPARING && issueSlip.getStatus() != IssueStatus.DRAFT) {
            throw new IllegalStateException("Chỉ sửa phiếu xuất khi chưa xuất kho");
        }
        Warehouse warehouse = warehouseRepository.findById(warehouseId).orElseThrow();
        Material material = materialRepository.findById(materialId).orElseThrow();
        IssueSlipLine slipLine = issueSlip.getLines().isEmpty() ? new IssueSlipLine() : issueSlip.getLines().get(0);
        if (slipLine.getId() == null) {
            slipLine.setIssueSlip(issueSlip);
            issueSlip.getLines().add(slipLine);
        }
        Long oldMaterialId = slipLine.getMaterial() == null ? null : slipLine.getMaterial().getId();
        MaterialRequest request = issueSlip.getMaterialRequest();
        MaterialRequestLine requestLine = request.getLines().stream()
                .filter(line -> oldMaterialId != null && line.getMaterial().getId().equals(oldMaterialId))
                .findFirst()
                .orElseGet(() -> request.getLines().get(0));

        for (StockReservation reservation : reservationRepository.findByMaterialRequestLine_IdAndStatus(requestLine.getId(), ReservationStatus.ACTIVE)) {
            StockBalance balance = reservation.getStockBalance();
            balance.setReservedQuantity(Math.max(0, balance.getReservedQuantity() - reservation.getReservedQuantity()));
            balance.setUpdatedAt(LocalDateTime.now());
            balance.validate();
            balanceRepository.save(balance);

            Material oldMaterial = reservation.getMaterial();
            oldMaterial.setReservedQuantity(Math.max(0, oldMaterial.getReservedQuantity() - reservation.getReservedQuantity()));
            materialRepository.save(oldMaterial);

            reservation.setStatus(ReservationStatus.RELEASED);
            reservation.setReleasedAt(LocalDateTime.now());
            reservationRepository.save(reservation);
        }

        int approved = 0;
        for (StockBalance balance : balanceRepository.findAvailableFefoInWarehouse(materialId, warehouseId, LocalDate.now())) {
            if (approved == quantity) {
                break;
            }
            int take = Math.min(balance.getAvailableQuantity(), quantity - approved);
            if (take <= 0) {
                continue;
            }
            balance.setReservedQuantity(balance.getReservedQuantity() + take);
            balance.setUpdatedAt(LocalDateTime.now());
            balance.validate();
            balanceRepository.save(balance);

            StockReservation reservation = new StockReservation();
            reservation.setMaterialRequest(request);
            reservation.setMaterialRequestLine(requestLine);
            reservation.setMaterial(material);
            reservation.setBatch(balance.getBatch());
            reservation.setStockBalance(balance);
            reservation.setWarehouse(balance.getWarehouse());
            reservation.setLocation(balance.getLocation());
            reservation.setReservedQuantity(take);
            reservationRepository.save(reservation);
            approved += take;
        }
        if (approved < quantity) {
            throw new IllegalStateException("Không đủ tồn khả dụng trong kho đã chọn cho vật tư " + material.getCode());
        }
        material.setReservedQuantity(material.getReservedQuantity() + approved);
        materialRepository.save(material);

        requestLine.setMaterial(material);
        requestLine.setRequestedQuantity(quantity);
        requestLine.setApprovedQuantity(approved);
        requestLine.setIssuedQuantity(0);
        requestLine.setStatus("APPROVED");

        slipLine.setMaterial(material);
        slipLine.setRequestedQuantity(quantity);
        slipLine.setApprovedQuantity(approved);
        slipLine.setIssuedQuantity(0);
        slipLine.getAllocations().clear();

        issueSlip.setWarehouse(warehouse);
        issueSlip.setNote(note);
        issueSlip.setUpdatedAt(LocalDateTime.now());
        issueSlipRepository.save(issueSlip);
        request.setUpdatedAt(LocalDateTime.now());
        requestRepository.save(request);
        auditService.log(username, "UPDATE_ISSUE", "ISSUE_SLIP", issueSlip.getIssueCode(), "Sửa phiếu xuất kho");
        return issueSlip;
    }

    @Transactional
    public void issue(Long issueId, String username) {
        IssueSlip issueSlip = issueSlipRepository.findById(issueId).orElseThrow();
        if (issueSlip.getStatus() != IssueStatus.PREPARING && issueSlip.getStatus() != IssueStatus.DRAFT) {
            throw new IllegalStateException("Phiếu xuất không ở trạng thái chuẩn bị");
        }
        if (issueSlip.getLines() != null) {
            issueSingleSlip(issueSlip, username);
            return;
        }
        List<StockReservation> reservations = reservationRepository.findByMaterialRequest_IdAndStatus(issueSlip.getMaterialRequest().getId(), ReservationStatus.ACTIVE);
        if (reservations.isEmpty()) {
            throw new IllegalStateException("Không có tồn giữ để xuất");
        }
        for (StockReservation reservation : reservations) {
            StockBalance balance = reservation.getStockBalance();
            Material material = reservation.getMaterial();
            MaterialBatch batch = reservation.getBatch();
            int quantity = reservation.getReservedQuantity();
            int before = inventorySyncService.syncMaterialActualQuantity(material);

            balance.setReservedQuantity(balance.getReservedQuantity() - quantity);
            balance.setActualQuantity(balance.getActualQuantity() - quantity);
            balance.setUpdatedAt(LocalDateTime.now());
            balance.validate();
            balanceRepository.save(balance);

            batch.setQuantity(batch.getQuantity() - quantity);
            batch.setUpdatedAt(LocalDateTime.now());
            batchRepository.save(batch);

            int after = inventorySyncService.syncMaterialActualQuantity(material);
            material.setReservedQuantity(Math.max(0, material.getReservedQuantity() - quantity));
            materialRepository.save(material);
            inventoryAlertService.notifyOutOfStockIfNeeded(material, "/alerts#stock-alerts");
            movementRepository.save(movement(MovementType.OUT, material, batch, balance.getWarehouse(), balance.getLocation(),
                    -quantity, before, after, "ISSUE", issueSlip.getIssueCode(), username));

            IssueSlipLine slipLine = issueSlip.getLines().stream()
                    .filter(line -> line.getMaterial().getId().equals(material.getId()))
                    .findFirst()
                    .orElseThrow();
            slipLine.setIssuedQuantity(slipLine.getIssuedQuantity() + quantity);
            IssueBatchAllocation allocation = new IssueBatchAllocation();
            allocation.setIssueSlipLine(slipLine);
            allocation.setMaterial(material);
            allocation.setBatch(batch);
            allocation.setWarehouse(balance.getWarehouse());
            allocation.setLocation(balance.getLocation());
            allocation.setQuantity(quantity);
            slipLine.getAllocations().add(allocation);

            MaterialRequestLine requestLine = reservation.getMaterialRequestLine();
            requestLine.setIssuedQuantity(requestLine.getIssuedQuantity() + quantity);
            requestLine.setStatus("ISSUED");
            reservation.setStatus(ReservationStatus.ISSUED);
            reservation.setIssuedAt(LocalDateTime.now());
            reservationRepository.save(reservation);
        }
        completeIssueAndReceiveToDepartment(issueSlip, username);
        auditService.log(username, "ISSUE_STOCK", "ISSUE_SLIP", issueSlip.getIssueCode(), "Xuất kho theo tồn giữ FEFO");
    }

    private void issueSingleSlip(IssueSlip issueSlip, String username) {
        boolean issuedAny = false;
        for (IssueSlipLine slipLine : issueSlip.getLines()) {
            int remaining = Math.max(0, slipLine.getApprovedQuantity() - slipLine.getIssuedQuantity());
            if (remaining == 0) {
                continue;
            }
            MaterialRequestLine requestLine = issueSlip.getMaterialRequest().getLines().stream()
                    .filter(line -> line.getMaterial().getId().equals(slipLine.getMaterial().getId()))
                    .findFirst()
                    .orElseThrow();
            List<StockReservation> reservations = reservationRepository
                    .findByMaterialRequestLine_IdAndStatus(requestLine.getId(), ReservationStatus.ACTIVE);
            for (StockReservation reservation : reservations) {
                if (remaining == 0) {
                    break;
                }
                StockBalance balance = reservation.getStockBalance();
                Material material = reservation.getMaterial();
                MaterialBatch batch = reservation.getBatch();
                int quantity = Math.min(reservation.getReservedQuantity(), remaining);
                int before = inventorySyncService.syncMaterialActualQuantity(material);

                balance.setReservedQuantity(balance.getReservedQuantity() - quantity);
                balance.setActualQuantity(balance.getActualQuantity() - quantity);
                balance.setUpdatedAt(LocalDateTime.now());
                balance.validate();
                balanceRepository.save(balance);

                batch.setQuantity(batch.getQuantity() - quantity);
                batch.setUpdatedAt(LocalDateTime.now());
                batchRepository.save(batch);

                int after = inventorySyncService.syncMaterialActualQuantity(material);
                material.setReservedQuantity(Math.max(0, material.getReservedQuantity() - quantity));
                materialRepository.save(material);
                inventoryAlertService.notifyOutOfStockIfNeeded(material, "/alerts#stock-alerts");
                movementRepository.save(movement(MovementType.OUT, material, batch, balance.getWarehouse(), balance.getLocation(),
                        -quantity, before, after, "ISSUE", issueSlip.getIssueCode(), username));

                slipLine.setIssuedQuantity(slipLine.getIssuedQuantity() + quantity);
                IssueBatchAllocation allocation = new IssueBatchAllocation();
                allocation.setIssueSlipLine(slipLine);
                allocation.setMaterial(material);
                allocation.setBatch(batch);
                allocation.setWarehouse(balance.getWarehouse());
                allocation.setLocation(balance.getLocation());
                allocation.setQuantity(quantity);
                slipLine.getAllocations().add(allocation);

                requestLine.setIssuedQuantity(requestLine.getIssuedQuantity() + quantity);
                requestLine.setStatus("ISSUED");
                if (reservation.getReservedQuantity() == quantity) {
                    reservation.setStatus(ReservationStatus.ISSUED);
                    reservation.setIssuedAt(LocalDateTime.now());
                } else {
                    reservation.setReservedQuantity(reservation.getReservedQuantity() - quantity);
                }
                reservationRepository.save(reservation);
                remaining -= quantity;
                issuedAny = true;
            }
            if (remaining > 0) {
                throw new IllegalStateException("Không đủ tồn giữ để xuất vật tư " + slipLine.getMaterial().getCode());
            }
        }
        if (!issuedAny) {
            throw new IllegalStateException("Không có tồn giữ để xuất");
        }
        completeIssueAndReceiveToDepartment(issueSlip, username);
        auditService.log(username, "ISSUE_STOCK", "ISSUE_SLIP", issueSlip.getIssueCode(), "Xuất kho theo tồn giữ FEFO");
    }

    private void completeIssueAndReceiveToDepartment(IssueSlip issueSlip, String username) {
        LocalDateTime now = LocalDateTime.now();
        issueSlip.setStatus(IssueStatus.RECEIVED);
        issueSlip.setIssuedBy(username);
        issueSlip.setIssuedAt(now);
        issueSlip.setReceivedBy(username);
        issueSlip.setReceivedAt(now);
        issueSlip.setUpdatedAt(now);
        issueSlipRepository.save(issueSlip);

        MaterialRequest request = issueSlip.getMaterialRequest();
        boolean allReceived = issueSlipRepository.findByMaterialRequest_IdOrderByIdAsc(request.getId()).stream()
                .allMatch(item -> item.getStatus() == IssueStatus.RECEIVED || item.getId().equals(issueSlip.getId()));
        if (allReceived) {
            request.setStatus(RequestStatus.RECEIVED);
            request.setReceivedBy(username);
            request.setReceivedAt(now);
        } else if (reservationRepository.findByMaterialRequest_IdAndStatus(request.getId(), ReservationStatus.ACTIVE).isEmpty()) {
            request.setStatus(RequestStatus.ISSUED);
        }
        request.setUpdatedAt(now);
        requestRepository.save(request);

        departmentStockService.receiveFromIssue(issueSlip, username);
        auditService.log(username, "AUTO_RECEIVE_TO_DEPARTMENT", "ISSUE_SLIP", issueSlip.getIssueCode(), "Tự động cập nhật tồn tại khoa sau khi điều dưỡng lấy vật tư");
    }

    @Transactional
    public void receiveIssue(Long issueId, String username) {
        IssueSlip issueSlip = issueSlipRepository.findById(issueId).orElseThrow();
        if (issueSlip.getStatus() != IssueStatus.ISSUED) {
            throw new IllegalStateException("Chỉ xác nhận nhận hàng sau khi kho đã xuất");
        }
        issueSlip.setStatus(IssueStatus.RECEIVED);
        issueSlip.setReceivedBy(username);
        issueSlip.setReceivedAt(LocalDateTime.now());
        issueSlip.setUpdatedAt(LocalDateTime.now());
        issueSlipRepository.save(issueSlip);

        MaterialRequest request = issueSlip.getMaterialRequest();
        boolean allReceived = issueSlipRepository.findByMaterialRequest_IdOrderByIdAsc(request.getId()).stream()
                .allMatch(item -> item.getStatus() == IssueStatus.RECEIVED || item.getId().equals(issueSlip.getId()));
        if (allReceived) {
            request.setStatus(RequestStatus.RECEIVED);
            request.setReceivedBy(username);
            request.setReceivedAt(LocalDateTime.now());
        }
        request.setUpdatedAt(LocalDateTime.now());
        requestRepository.save(request);
        departmentStockService.receiveFromIssue(issueSlip, username);
        auditService.log(username, "RECEIVE_ISSUE", "ISSUE_SLIP", issueSlip.getIssueCode(), "Khoa xác nhận đã nhận vật tư");
    }

    @Transactional
    public void cancelIssue(Long issueId, String cancellationReason, String username) {
        IssueSlip issueSlip = issueSlipRepository.findById(issueId).orElseThrow();
        if (!issueSlip.canCancel()) {
            throw new IllegalStateException("Chỉ được hủy phiếu xuất chưa xuất kho");
        }
        String reason = cancellationReason == null ? "" : cancellationReason.trim();
        if (reason.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập lý do hủy phiếu xuất");
        }

        MaterialRequest request = issueSlip.getMaterialRequest();
        for (IssueSlipLine slipLine : issueSlip.getLines()) {
            MaterialRequestLine requestLine = request.getLines().stream()
                    .filter(line -> line.getMaterial().getId().equals(slipLine.getMaterial().getId()))
                    .findFirst()
                    .orElseThrow();
            List<StockReservation> reservations = reservationRepository
                    .findByMaterialRequestLine_IdAndStatus(requestLine.getId(), ReservationStatus.ACTIVE);
            for (StockReservation reservation : reservations) {
                int quantity = reservation.getReservedQuantity();
                StockBalance balance = reservation.getStockBalance();
                balance.setReservedQuantity(Math.max(0, balance.getReservedQuantity() - quantity));
                balance.setUpdatedAt(LocalDateTime.now());
                balance.validate();
                balanceRepository.save(balance);

                Material material = reservation.getMaterial();
                material.setReservedQuantity(Math.max(0, material.getReservedQuantity() - quantity));
                materialRepository.save(material);

                reservation.setStatus(ReservationStatus.CANCELLED);
                reservationRepository.save(reservation);
            }
            requestLine.setStatus("CANCELLED");
        }

        LocalDateTime now = LocalDateTime.now();
        issueSlip.setStatus(IssueStatus.CANCELLED);
        issueSlip.setNote(appendNote(issueSlip.getNote(), "Lý do hủy: " + reason));
        issueSlip.setUpdatedAt(now);
        issueSlipRepository.save(issueSlip);

        boolean allCancelled = issueSlipRepository.findByMaterialRequest_IdOrderByIdAsc(request.getId()).stream()
                .allMatch(item -> item.getStatus() == IssueStatus.CANCELLED || item.getId().equals(issueSlip.getId()));
        if (allCancelled) {
            request.setStatus(RequestStatus.CANCELLED);
            request.setNote(appendNote(request.getNote(), "Lý do hủy phiếu xuất: " + reason));
        }
        request.setUpdatedAt(now);
        requestRepository.save(request);
        auditService.log(username, "CANCEL_ISSUE", "ISSUE_SLIP", issueSlip.getIssueCode(),
                "Hủy phiếu xuất. Lý do: " + reason);
    }

    private String appendNote(String currentNote, String detail) {
        return currentNote == null || currentNote.isBlank() ? detail : currentNote + System.lineSeparator() + detail;
    }

    private StockBalance findOrCreateBalance(Material material, MaterialBatch batch, Warehouse warehouse, StorageLocation location) {
        return balanceRepository.findByMaterial_IdAndBatch_IdAndWarehouse_IdAndLocation_Id(
                material.getId(), batch.getId(), warehouse.getId(), location.getId()).orElseGet(() -> {
            StockBalance balance = new StockBalance();
            balance.setMaterial(material);
            balance.setBatch(batch);
            balance.setWarehouse(warehouse);
            balance.setLocation(location);
            return balance;
        });
    }

    private void logApproval(MaterialRequest request, String action, String actor, String note) {
        RequestApprovalLog log = new RequestApprovalLog();
        log.setMaterialRequest(request);
        log.setAction(action);
        log.setActor(actor);
        log.setNote(note);
        approvalLogRepository.save(log);
    }

    private StockMovement movement(MovementType type, Material material, MaterialBatch batch, Warehouse warehouse,
                                   StorageLocation location, int quantity, int before, int after,
                                   String refType, String refCode, String username) {
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

    private String nextReceiptCode() {
        int next = receiptRepository.findAll().stream()
                .map(Receipt::getReceiptCode)
                .mapToInt(code -> numericCode(code, "PN-"))
                .max()
                .orElse(0) + 1;
        String code = "PN-" + next;
        while (receiptRepository.existsByReceiptCode(code)) {
            code = "PN-" + (++next);
        }
        return code;
    }

    private String nextIssueCode() {
        int next = issueSlipRepository.findAll().stream()
                .map(IssueSlip::getIssueCode)
                .mapToInt(code -> numericCode(code, "PX-"))
                .max()
                .orElse(0) + 1;
        String code = "PX-" + next;
        while (issueSlipRepository.existsByIssueCode(code)) {
            code = "PX-" + (++next);
        }
        return code;
    }

    private String nextDirectRequestCode() {
        String code = "YC-XK-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int index = 1;
        String candidate = code;
        while (requestRepository.existsByCode(candidate)) {
            candidate = code + "-" + (++index);
        }
        return candidate;
    }

    private String nextBatchNumber(String receiptCode) {
        String suffix = receiptCode == null || !receiptCode.contains("-")
                ? String.valueOf(System.currentTimeMillis())
                : receiptCode.substring(receiptCode.indexOf('-') + 1);
        String base = "LO-" + suffix;
        String candidate = base;
        int index = 1;
        while (batchNumberExists(candidate)) {
            candidate = base + "-" + (++index);
        }
        return candidate;
    }

    private boolean batchNumberExists(String batchNumber) {
        boolean existsInBatches = batchRepository.findAll().stream()
                .anyMatch(batch -> batchNumber.equals(batch.getBatchNumber()));
        if (existsInBatches) {
            return true;
        }
        return receiptRepository.findAll().stream()
                .flatMap(receipt -> receipt.getLines().stream())
                .anyMatch(line -> batchNumber.equals(line.getBatchNumber()));
    }

    private int numericCode(String code, String prefix) {
        if (code == null || !code.startsWith(prefix)) {
            return 0;
        }
        try {
            return Integer.parseInt(code.substring(prefix.length()));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}
