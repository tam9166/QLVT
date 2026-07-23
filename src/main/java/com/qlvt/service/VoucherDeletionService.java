package com.qlvt.service;

import com.qlvt.entity.DepartmentStock;
import com.qlvt.entity.IssueBatchAllocation;
import com.qlvt.entity.IssueSlip;
import com.qlvt.entity.IssueSlipLine;
import com.qlvt.entity.Material;
import com.qlvt.entity.MaterialBatch;
import com.qlvt.entity.MaterialRequest;
import com.qlvt.entity.MaterialRequestLine;
import com.qlvt.entity.Receipt;
import com.qlvt.entity.ReceiptLine;
import com.qlvt.entity.StockBalance;
import com.qlvt.entity.StockMovement;
import com.qlvt.entity.StockReservation;
import com.qlvt.enums.IssueStatus;
import com.qlvt.enums.MovementType;
import com.qlvt.enums.ReceiptStatus;
import com.qlvt.enums.RequestStatus;
import com.qlvt.enums.ReservationStatus;
import com.qlvt.repository.DepartmentStockRepository;
import com.qlvt.repository.IssueSlipRepository;
import com.qlvt.repository.MaterialBatchRepository;
import com.qlvt.repository.MaterialPriceHistoryRepository;
import com.qlvt.repository.MaterialRepository;
import com.qlvt.repository.PriceAlertRepository;
import com.qlvt.repository.ReceiptRepository;
import com.qlvt.repository.StockBalanceRepository;
import com.qlvt.repository.StockMovementRepository;
import com.qlvt.repository.StockReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class VoucherDeletionService {
    private final ReceiptRepository receiptRepository;
    private final IssueSlipRepository issueSlipRepository;
    private final MaterialRepository materialRepository;
    private final MaterialBatchRepository batchRepository;
    private final StockBalanceRepository balanceRepository;
    private final StockMovementRepository movementRepository;
    private final StockReservationRepository reservationRepository;
    private final DepartmentStockRepository departmentStockRepository;
    private final MaterialPriceHistoryRepository priceHistoryRepository;
    private final PriceAlertRepository priceAlertRepository;
    private final AuditService auditService;
    private final InventorySyncService inventorySyncService;

    public VoucherDeletionService(ReceiptRepository receiptRepository,
                                  IssueSlipRepository issueSlipRepository,
                                  MaterialRepository materialRepository,
                                  MaterialBatchRepository batchRepository,
                                  StockBalanceRepository balanceRepository,
                                  StockMovementRepository movementRepository,
                                  StockReservationRepository reservationRepository,
                                  DepartmentStockRepository departmentStockRepository,
                                  MaterialPriceHistoryRepository priceHistoryRepository,
                                  PriceAlertRepository priceAlertRepository,
                                  AuditService auditService,
                                  InventorySyncService inventorySyncService) {
        this.receiptRepository = receiptRepository;
        this.issueSlipRepository = issueSlipRepository;
        this.materialRepository = materialRepository;
        this.batchRepository = batchRepository;
        this.balanceRepository = balanceRepository;
        this.movementRepository = movementRepository;
        this.reservationRepository = reservationRepository;
        this.departmentStockRepository = departmentStockRepository;
        this.priceHistoryRepository = priceHistoryRepository;
        this.priceAlertRepository = priceAlertRepository;
        this.auditService = auditService;
        this.inventorySyncService = inventorySyncService;
    }

    @Transactional
    public void deleteReceipt(Long receiptId, String username) {
        Receipt receipt = receiptRepository.findById(receiptId).orElseThrow();
        List<ReceiptLine> lines = receipt.getLines().stream().toList();
        if (receipt.getStatus() == ReceiptStatus.CONFIRMED) {
            reverseConfirmedReceipt(receipt, lines, username);
        }

        priceHistoryRepository.deleteAll(priceHistoryRepository.findByReceipt_Id(receiptId));
        priceAlertRepository.findByReceipt_Id(receiptId).forEach(alert -> {
            alert.setReceipt(null);
            priceAlertRepository.save(alert);
        });
        auditService.log(username, "DELETE_RECEIPT", "RECEIPT", receipt.getReceiptCode(), "Xóa phiếu nhập kho");
        receiptRepository.delete(receipt);
    }

    @Transactional
    public void deleteIssue(Long issueId, String username) {
        IssueSlip issue = issueSlipRepository.findById(issueId).orElseThrow();
        if (issue.getStatus() == IssueStatus.ISSUED || issue.getStatus() == IssueStatus.RECEIVED) {
            reverseIssuedSlip(issue, username);
        }
        MaterialRequest request = issue.getMaterialRequest();
        String code = issue.getIssueCode();
        issueSlipRepository.delete(issue);
        restoreRequestStatus(request);
        auditService.log(username, "DELETE_ISSUE", "ISSUE_SLIP", code, "Xóa phiếu xuất kho");
    }

    private void reverseConfirmedReceipt(Receipt receipt, List<ReceiptLine> lines, String username) {
        for (ReceiptLine line : lines) {
            Material material = line.getMaterial();
            int before = material.getActualQuantity();
            MaterialBatch batch = batchRepository.findByMaterial_IdAndBatchNumber(material.getId(), line.getBatchNumber()).orElseThrow();
            if (batch.getQuantity() < line.getQuantity()) {
                throw new IllegalStateException("Không thể xóa phiếu nhập vì lô " + batch.getBatchNumber() + " đã được xuất hoặc không đủ tồn để đảo.");
            }
            StockBalance balance = balanceRepository.findByMaterial_IdAndBatch_IdAndWarehouse_IdAndLocation_Id(
                    material.getId(), batch.getId(), receipt.getWarehouse().getId(), line.getLocation().getId()).orElseThrow();
            if (balance.getActualQuantity() < line.getQuantity()) {
                throw new IllegalStateException("Không thể xóa phiếu nhập vì tồn vị trí không đủ để đảo.");
            }
            if (inventorySyncService.calculateActualQuantity(material.getId()) < line.getQuantity()) {
                throw new IllegalStateException("Không thể xóa phiếu nhập vì tồn vật tư không đủ để đảo.");
            }

            balance.setActualQuantity(balance.getActualQuantity() - line.getQuantity());
            balance.setUpdatedAt(LocalDateTime.now());
            balance.validate();
            balanceRepository.save(balance);

            batch.setQuantity(batch.getQuantity() - line.getQuantity());
            batch.setInitialQuantity(Math.max(0, batch.getInitialQuantity() - line.getQuantity()));
            batch.setUpdatedAt(LocalDateTime.now());
            batchRepository.save(batch);

            int after = inventorySyncService.syncMaterialActualQuantity(material);
            movementRepository.save(reversalMovement(MovementType.OUT, material, batch, receipt.getWarehouse(),
                    line.getLocation(), -line.getQuantity(), before, after, "DELETE_RECEIPT", receipt.getReceiptCode(),
                    "Đảo tồn do xóa phiếu nhập", username));
        }
    }

    private void reverseIssuedSlip(IssueSlip issue, String username) {
        for (IssueSlipLine line : issue.getLines()) {
            MaterialRequestLine requestLine = issue.getMaterialRequest().getLines().stream()
                    .filter(item -> item.getMaterial().getId().equals(line.getMaterial().getId()))
                    .findFirst()
                    .orElse(null);
            for (IssueBatchAllocation allocation : line.getAllocations()) {
                if (issue.getStatus() == IssueStatus.RECEIVED) {
                    reverseDepartmentStock(issue, allocation);
                }
                Material material = allocation.getMaterial();
                int before = material.getActualQuantity();
                MaterialBatch batch = allocation.getBatch();
                StockBalance balance = balanceRepository.findByMaterial_IdAndBatch_IdAndWarehouse_IdAndLocation_Id(
                        material.getId(), batch.getId(), allocation.getWarehouse().getId(), allocation.getLocation().getId()).orElseThrow();

                balance.setActualQuantity(balance.getActualQuantity() + allocation.getQuantity());
                balance.setReservedQuantity(balance.getReservedQuantity() + allocation.getQuantity());
                balance.setUpdatedAt(LocalDateTime.now());
                balance.validate();
                balanceRepository.save(balance);

                batch.setQuantity(batch.getQuantity() + allocation.getQuantity());
                batch.setUpdatedAt(LocalDateTime.now());
                batchRepository.save(batch);

                int after = inventorySyncService.syncMaterialActualQuantity(material);
                movementRepository.save(reversalMovement(MovementType.IN, material, batch, allocation.getWarehouse(),
                        allocation.getLocation(), allocation.getQuantity(), before, after, "DELETE_ISSUE", issue.getIssueCode(),
                        "Hoàn tồn do xóa phiếu xuất", username));
                material.setReservedQuantity(material.getReservedQuantity() + allocation.getQuantity());
                materialRepository.save(material);

                if (requestLine != null) {
                    requestLine.setIssuedQuantity(Math.max(0, requestLine.getIssuedQuantity() - allocation.getQuantity()));
                    requestLine.setStatus("APPROVED");
                    reactivateReservation(requestLine, allocation);
                }
            }
            line.setIssuedQuantity(0);
        }
    }

    private void reverseDepartmentStock(IssueSlip issue, IssueBatchAllocation allocation) {
        DepartmentStock stock = departmentStockRepository
                .findByDepartmentAndMaterial_IdAndBatch_Id(issue.getDepartment(), allocation.getMaterial().getId(), allocation.getBatch().getId())
                .orElseThrow();
        if (stock.getQuantityOnHand() < allocation.getQuantity()) {
            throw new IllegalStateException("Không thể xóa phiếu xuất vì tồn tại khoa không đủ để đảo.");
        }
        stock.setQuantityOnHand(stock.getQuantityOnHand() - allocation.getQuantity());
        stock.setUpdatedAt(LocalDateTime.now());
        stock.validate();
        departmentStockRepository.save(stock);
    }

    private void reactivateReservation(MaterialRequestLine requestLine, IssueBatchAllocation allocation) {
        List<StockReservation> issuedReservations = reservationRepository
                .findByMaterialRequestLine_IdAndStatus(requestLine.getId(), ReservationStatus.ISSUED);
        StockReservation reservation = issuedReservations.stream()
                .filter(item -> item.getBatch().getId().equals(allocation.getBatch().getId())
                        && item.getLocation().getId().equals(allocation.getLocation().getId()))
                .findFirst()
                .orElse(null);
        if (reservation != null) {
            reservation.setStatus(ReservationStatus.ACTIVE);
            reservation.setIssuedAt(null);
            reservationRepository.save(reservation);
            return;
        }

        StockReservation activeReservation = reservationRepository
                .findByMaterialRequestLine_IdAndStatus(requestLine.getId(), ReservationStatus.ACTIVE)
                .stream()
                .filter(item -> item.getBatch().getId().equals(allocation.getBatch().getId())
                        && item.getLocation().getId().equals(allocation.getLocation().getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy tồn giữ để đảo phiếu xuất."));
        activeReservation.setReservedQuantity(activeReservation.getReservedQuantity() + allocation.getQuantity());
        reservationRepository.save(activeReservation);
    }

    private void restoreRequestStatus(MaterialRequest request) {
        if (request == null) {
            return;
        }
        List<IssueSlip> remainingIssues = issueSlipRepository.findByMaterialRequest_IdOrderByIdAsc(request.getId());
        if (!remainingIssues.isEmpty()) {
            request.setStatus(RequestStatus.PREPARING);
            request.setUpdatedAt(LocalDateTime.now());
            return;
        }
        boolean hasReservations = !reservationRepository.findByMaterialRequest_IdAndStatus(request.getId(), ReservationStatus.ACTIVE).isEmpty();
        if (hasReservations) {
            boolean full = request.getLines().stream().allMatch(line -> line.getApprovedQuantity() >= line.getRequestedQuantity());
            request.setStatus(full ? RequestStatus.RESERVED : RequestStatus.PARTIALLY_APPROVED);
        } else {
            request.setStatus(RequestStatus.DEPARTMENT_APPROVED);
        }
        request.setUpdatedAt(LocalDateTime.now());
    }

    static StockMovement reversalMovement(MovementType type, Material material, MaterialBatch batch,
                                          com.qlvt.entity.Warehouse warehouse,
                                          com.qlvt.entity.StorageLocation location,
                                          int quantity, int before, int after,
                                          String referenceType, String referenceCode,
                                          String note, String createdBy) {
        StockMovement movement = new StockMovement();
        movement.setMovementType(type);
        movement.setMaterial(material);
        movement.setBatch(batch);
        movement.setWarehouse(warehouse);
        movement.setLocation(location);
        movement.setQuantity(quantity);
        movement.setBeforeQuantity(before);
        movement.setAfterQuantity(after);
        movement.setReferenceType(referenceType);
        movement.setReferenceCode(referenceCode);
        movement.setNote(note);
        movement.setCreatedBy(createdBy);
        return movement;
    }
}
