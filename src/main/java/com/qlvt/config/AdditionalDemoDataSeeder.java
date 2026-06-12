package com.qlvt.config;

import com.qlvt.entity.DestructionSlip;
import com.qlvt.entity.DestructionSlipLine;
import com.qlvt.entity.Material;
import com.qlvt.entity.MaterialBatch;
import com.qlvt.entity.MaterialPriceHistory;
import com.qlvt.entity.PriceAlert;
import com.qlvt.entity.PurchaseRequest;
import com.qlvt.entity.PurchaseRequestLine;
import com.qlvt.entity.RecallOrder;
import com.qlvt.entity.RecallOrderLine;
import com.qlvt.entity.Receipt;
import com.qlvt.entity.StockAdjustment;
import com.qlvt.entity.StockAdjustmentLine;
import com.qlvt.entity.StockTransfer;
import com.qlvt.entity.StockTransferLine;
import com.qlvt.entity.StorageCondition;
import com.qlvt.entity.StorageLocation;
import com.qlvt.entity.Supplier;
import com.qlvt.entity.TemperatureLog;
import com.qlvt.entity.Warehouse;
import com.qlvt.enums.BatchStatus;
import com.qlvt.enums.DestructionReason;
import com.qlvt.enums.DestructionStatus;
import com.qlvt.enums.PriceAlertLevel;
import com.qlvt.enums.PurchaseRequestStatus;
import com.qlvt.enums.RecallStatus;
import com.qlvt.enums.StockAdjustmentStatus;
import com.qlvt.enums.StockTransferStatus;
import com.qlvt.repository.DestructionSlipRepository;
import com.qlvt.repository.MaterialBatchRepository;
import com.qlvt.repository.MaterialPriceHistoryRepository;
import com.qlvt.repository.MaterialRepository;
import com.qlvt.repository.PriceAlertRepository;
import com.qlvt.repository.PurchaseRequestRepository;
import com.qlvt.repository.RecallOrderRepository;
import com.qlvt.repository.ReceiptRepository;
import com.qlvt.repository.StockAdjustmentRepository;
import com.qlvt.repository.StockTransferRepository;
import com.qlvt.repository.StorageConditionRepository;
import com.qlvt.repository.StorageLocationRepository;
import com.qlvt.repository.SupplierRepository;
import com.qlvt.repository.TemperatureLogRepository;
import com.qlvt.repository.WarehouseRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@Order(3)
public class AdditionalDemoDataSeeder implements CommandLineRunner {
    private final MaterialRepository materialRepository;
    private final MaterialBatchRepository batchRepository;
    private final SupplierRepository supplierRepository;
    private final WarehouseRepository warehouseRepository;
    private final StorageLocationRepository locationRepository;
    private final ReceiptRepository receiptRepository;
    private final StockAdjustmentRepository adjustmentRepository;
    private final StockTransferRepository transferRepository;
    private final RecallOrderRepository recallRepository;
    private final DestructionSlipRepository destructionRepository;
    private final PurchaseRequestRepository purchaseRequestRepository;
    private final MaterialPriceHistoryRepository priceHistoryRepository;
    private final PriceAlertRepository priceAlertRepository;
    private final StorageConditionRepository storageConditionRepository;
    private final TemperatureLogRepository temperatureLogRepository;

    public AdditionalDemoDataSeeder(MaterialRepository materialRepository,
                                    MaterialBatchRepository batchRepository,
                                    SupplierRepository supplierRepository,
                                    WarehouseRepository warehouseRepository,
                                    StorageLocationRepository locationRepository,
                                    ReceiptRepository receiptRepository,
                                    StockAdjustmentRepository adjustmentRepository,
                                    StockTransferRepository transferRepository,
                                    RecallOrderRepository recallRepository,
                                    DestructionSlipRepository destructionRepository,
                                    PurchaseRequestRepository purchaseRequestRepository,
                                    MaterialPriceHistoryRepository priceHistoryRepository,
                                    PriceAlertRepository priceAlertRepository,
                                    StorageConditionRepository storageConditionRepository,
                                    TemperatureLogRepository temperatureLogRepository) {
        this.materialRepository = materialRepository;
        this.batchRepository = batchRepository;
        this.supplierRepository = supplierRepository;
        this.warehouseRepository = warehouseRepository;
        this.locationRepository = locationRepository;
        this.receiptRepository = receiptRepository;
        this.adjustmentRepository = adjustmentRepository;
        this.transferRepository = transferRepository;
        this.recallRepository = recallRepository;
        this.destructionRepository = destructionRepository;
        this.purchaseRequestRepository = purchaseRequestRepository;
        this.priceHistoryRepository = priceHistoryRepository;
        this.priceAlertRepository = priceAlertRepository;
        this.storageConditionRepository = storageConditionRepository;
        this.temperatureLogRepository = temperatureLogRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Warehouse mainWarehouse = warehouseRepository.findByCode("KHO001").orElseThrow();
        Warehouse emergencyWarehouse = warehouseRepository.findByCode("KHO002").orElse(mainWarehouse);
        StorageLocation mainLocation = locationRepository.findByCode("KHO001-A1").orElseThrow();
        StorageLocation emergencyLocation = locationRepository.findByCode("KHO002-C1").orElse(mainLocation);
        Supplier supplier = supplierRepository.findByCode("NCC001").orElseThrow();

        Material gloves = materialRepository.findByCode("VT002").orElseThrow();
        Material mask = materialRepository.findByCode("VT001").orElseThrow();
        Material saline = materialRepository.findByCode("VT006").orElseThrow();
        Material lowStock = lowStockMaterial();
        MaterialBatch glovesBatch = batchRepository.findByMaterial_IdAndBatchNumber(gloves.getId(), "DEMO-LO-VT002-01").orElseThrow();
        MaterialBatch maskBatch = batchRepository.findByMaterial_IdAndBatchNumber(mask.getId(), "DEMO-LO-VT001-01").orElseThrow();
        MaterialBatch salineBatch = batchRepository.findByMaterial_IdAndBatchNumber(saline.getId(), "DEMO-LO-VT006-01").orElseThrow();
        MaterialBatch lowStockBatch = batch(lowStock, mainWarehouse, mainLocation, supplier, "DEMO-LO-CB001-01", 8, LocalDate.now().plusMonths(9));

        seedStockAdjustment(mainWarehouse, mainLocation, gloves, glovesBatch);
        seedStockTransfer(mainWarehouse, emergencyWarehouse, mainLocation, emergencyLocation, mask, maskBatch);
        seedRecall(saline, salineBatch);
        seedDestruction(mainWarehouse, mainLocation, lowStock, lowStockBatch);
        seedPurchaseRequest(lowStock, saline);
        seedPriceHistoryAndAlert(gloves, supplier);
        seedStorageMonitoring(gloves, saline, mainWarehouse, emergencyWarehouse);
    }

    private Material lowStockMaterial() {
        Material material = materialRepository.findByCode("CB001").orElseGet(Material::new);
        material.setCode("CB001");
        material.setName("Bộ dây truyền dịch trẻ em");
        material.setAliasText("bo day truyen dich tre em");
        material.setCategory("Vật tư cảnh báo tồn kho");
        material.setUnit("Bộ");
        material.setPackageSpec("1 bộ/túi");
        material.setStorageCondition("Bảo quản nơi khô ráo");
        material.setActualQuantity(8);
        material.setReservedQuantity(0);
        material.setPendingIssueQuantity(0);
        material.setMinStock(25);
        material.setMaxStock(120);
        material.setEstimatedUnitPrice(new BigDecimal("15500"));
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
        batch.setReceiptDate(LocalDate.now().minusDays(4));
        batch.setInitialQuantity(quantity);
        batch.setQuantity(quantity);
        batch.setStatus(BatchStatus.AVAILABLE);
        return batchRepository.save(batch);
    }

    private void seedStockAdjustment(Warehouse warehouse, StorageLocation location, Material material, MaterialBatch batch) {
        if (adjustmentRepository.existsByAdjustmentCode("DC-DEMO-001")) return;
        StockAdjustment adjustment = new StockAdjustment();
        adjustment.setAdjustmentCode("DC-DEMO-001");
        adjustment.setWarehouse(warehouse);
        adjustment.setReason("Điều chỉnh chênh lệch sau kiểm kê mẫu");
        adjustment.setStatus(StockAdjustmentStatus.SUBMITTED);
        adjustment.setCreatedBy("thukho");
        adjustment.setCreatedAt(LocalDateTime.now().minusDays(1));

        StockAdjustmentLine line = new StockAdjustmentLine();
        line.setStockAdjustment(adjustment);
        line.setMaterial(material);
        line.setBatch(batch);
        line.setLocation(location);
        line.setSystemQuantity(160);
        line.setActualQuantity(158);
        line.setAdjustmentQuantity(-2);
        line.setNote("Dữ liệu mẫu điều chỉnh tồn");
        adjustment.getLines().add(line);
        adjustmentRepository.save(adjustment);
    }

    private void seedStockTransfer(Warehouse fromWarehouse, Warehouse toWarehouse, StorageLocation fromLocation,
                                   StorageLocation toLocation, Material material, MaterialBatch batch) {
        if (transferRepository.existsByTransferCode("CK-DEMO-001")) return;
        StockTransfer transfer = new StockTransfer();
        transfer.setTransferCode("CK-DEMO-001");
        transfer.setFromWarehouse(fromWarehouse);
        transfer.setToWarehouse(toWarehouse);
        transfer.setStatus(StockTransferStatus.SUBMITTED);
        transfer.setReason("Bổ sung vật tư cho kho cấp cứu");
        transfer.setCreatedBy("thukho");
        transfer.setCreatedAt(LocalDateTime.now().minusHours(8));

        StockTransferLine line = new StockTransferLine();
        line.setStockTransfer(transfer);
        line.setMaterial(material);
        line.setBatch(batch);
        line.setFromLocation(fromLocation);
        line.setToLocation(toLocation);
        line.setQuantity(15);
        line.setNote("Dữ liệu mẫu chuyển kho nội bộ");
        transfer.getLines().add(line);
        transferRepository.save(transfer);
    }

    private void seedRecall(Material material, MaterialBatch batch) {
        if (recallRepository.existsByRecallCode("TH-DEMO-001")) return;
        RecallOrder recall = new RecallOrder();
        recall.setRecallCode("TH-DEMO-001");
        recall.setMaterial(material);
        recall.setBatch(batch);
        recall.setReason("Theo dõi chất lượng lô sau phản ánh từ khoa");
        recall.setStatus(RecallStatus.ACTIVE);
        recall.setCreatedBy("thukho");
        recall.setApprovedBy("lanhdao");
        recall.setCreatedAt(LocalDateTime.now().minusHours(6));

        RecallOrderLine line = new RecallOrderLine();
        line.setRecallOrder(recall);
        line.setDepartment("Khoa Nội tổng hợp");
        line.setIssuedQuantity(20);
        line.setRemainingQuantity(12);
        line.setReturnedQuantity(3);
        line.setStatus("Đang thu hồi");
        line.setNote("Dữ liệu mẫu thu hồi theo lô");
        recall.getLines().add(line);
        recallRepository.save(recall);
    }

    private void seedDestruction(Warehouse warehouse, StorageLocation location, Material material, MaterialBatch batch) {
        if (destructionRepository.existsByDestructionCode("HUY-DEMO-001")) return;
        DestructionSlip slip = new DestructionSlip();
        slip.setDestructionCode("HUY-DEMO-001");
        slip.setReason("Hủy vật tư lỗi bao bì trong quá trình kiểm tra");
        slip.setStatus(DestructionStatus.SUBMITTED);
        slip.setCreatedBy("thukho");
        slip.setCreatedAt(LocalDateTime.now().minusHours(5));

        DestructionSlipLine line = new DestructionSlipLine();
        line.setDestructionSlip(slip);
        line.setMaterial(material);
        line.setBatch(batch);
        line.setWarehouse(warehouse);
        line.setLocation(location);
        line.setQuantity(2);
        line.setReason(DestructionReason.DAMAGED);
        line.setNote("Dữ liệu mẫu hủy vật tư");
        slip.getLines().add(line);
        destructionRepository.save(slip);
    }

    private void seedPurchaseRequest(Material lowStock, Material saline) {
        if (purchaseRequestRepository.existsByRequestCode("MS-DEMO-001")) return;
        PurchaseRequest request = new PurchaseRequest();
        request.setRequestCode("MS-DEMO-001");
        request.setStatus(PurchaseRequestStatus.SUBMITTED);
        request.setReason("Bổ sung vật tư tồn thấp và dịch truyền dự phòng");
        request.setCreatedBy("muasam");
        request.setCreatedAt(LocalDateTime.now().minusHours(4));
        request.getLines().add(purchaseLine(request, lowStock, 80, 100, "Đề xuất mua do tồn dưới mức tối thiểu"));
        request.getLines().add(purchaseLine(request, saline, 60, 80, "Bổ sung tồn kho an toàn"));
        purchaseRequestRepository.save(request);
    }

    private PurchaseRequestLine purchaseLine(PurchaseRequest request, Material material, int requested, int suggested, String note) {
        PurchaseRequestLine line = new PurchaseRequestLine();
        line.setPurchaseRequest(request);
        line.setMaterial(material);
        line.setRequestedQuantity(requested);
        line.setSuggestedQuantity(suggested);
        line.setNote(note);
        return line;
    }

    private void seedStorageMonitoring(Material gloves, Material saline, Warehouse mainWarehouse, Warehouse emergencyWarehouse) {
        if (storageConditionRepository.count() == 0) {
            StorageCondition glovesCondition = new StorageCondition();
            glovesCondition.setMaterial(gloves);
            glovesCondition.setMinTemperature(new BigDecimal("15"));
            glovesCondition.setMaxTemperature(new BigDecimal("30"));
            glovesCondition.setMinHumidity(new BigDecimal("35"));
            glovesCondition.setMaxHumidity(new BigDecimal("70"));
            glovesCondition.setLightSensitive(true);
            glovesCondition.setNote("Tránh ánh nắng trực tiếp, không đặt sát nền kho.");
            storageConditionRepository.save(glovesCondition);

            StorageCondition salineCondition = new StorageCondition();
            salineCondition.setMaterial(saline);
            salineCondition.setMinTemperature(new BigDecimal("2"));
            salineCondition.setMaxTemperature(new BigDecimal("8"));
            salineCondition.setMinHumidity(new BigDecimal("35"));
            salineCondition.setMaxHumidity(new BigDecimal("65"));
            salineCondition.setColdChainRequired(true);
            salineCondition.setNote("Theo dõi nhiệt độ kho lạnh mỗi ca trực.");
            storageConditionRepository.save(salineCondition);
        }

        if (temperatureLogRepository.count() == 0) {
            TemperatureLog normal = temperatureLog(mainWarehouse, "24.5", "58", "NORMAL", "Nhiệt độ ổn định trong ca sáng");
            TemperatureLog warning = temperatureLog(emergencyWarehouse, "27.2", "68", "WARNING", "Độ ẩm tăng, cần kiểm tra điều hòa kho");
            TemperatureLog risk = temperatureLog(mainWarehouse, "31.0", "76", "RISK", "Vượt ngưỡng, cần kiểm tra và cân nhắc cách ly lô nhạy cảm");
            temperatureLogRepository.save(normal);
            temperatureLogRepository.save(warning);
            temperatureLogRepository.save(risk);
        }
    }

    private TemperatureLog temperatureLog(Warehouse warehouse, String temperature, String humidity, String status, String note) {
        TemperatureLog log = new TemperatureLog();
        log.setWarehouse(warehouse);
        log.setRecordedAt(LocalDateTime.now().minusHours(status.equals("RISK") ? 1 : 6));
        log.setTemperature(new BigDecimal(temperature));
        log.setHumidity(new BigDecimal(humidity));
        log.setStatus(status);
        log.setRecordedBy("thukho");
        log.setNote(note);
        return log;
    }

    private void seedPriceHistoryAndAlert(Material material, Supplier supplier) {
        Receipt receipt = receiptRepository.findAll().stream()
                .filter(item -> "PN-DEMO-001".equals(item.getReceiptCode()))
                .findFirst()
                .orElse(null);
        if (receipt == null) return;

        boolean hasHistory = priceHistoryRepository.findAll().stream()
                .anyMatch(item -> item.getMaterial().getCode().equals(material.getCode())
                        && item.getUnitPrice().compareTo(new BigDecimal("56000")) == 0);
        if (!hasHistory) {
            MaterialPriceHistory history = new MaterialPriceHistory();
            history.setMaterial(material);
            history.setSupplier(supplier);
            history.setReceipt(receipt);
            history.setUnitPrice(new BigDecimal("56000"));
            history.setQuantity(60);
            history.setTotalAmount(new BigDecimal("3360000"));
            history.setReceivedDate(LocalDate.now().minusDays(2));
            history.setCreatedBy("thukho");
            history.setNote("Dữ liệu mẫu lịch sử giá nhập");
            priceHistoryRepository.save(history);
        }

        boolean hasAlert = priceAlertRepository.findAll().stream()
                .anyMatch(item -> item.getMaterial().getCode().equals(material.getCode())
                        && item.getNewPrice().compareTo(new BigDecimal("56000")) == 0);
        if (!hasAlert) {
            PriceAlert alert = new PriceAlert();
            alert.setMaterial(material);
            alert.setSupplier(supplier);
            alert.setReceipt(receipt);
            alert.setOldPrice(new BigDecimal("42000"));
            alert.setNewPrice(new BigDecimal("56000"));
            alert.setDifferencePercent(new BigDecimal("33.33"));
            alert.setAlertLevel(PriceAlertLevel.WARNING);
            alert.setMessage("Giá nhập tăng 33,33% so với lần nhập trước");
            alert.setResolved(false);
            priceAlertRepository.save(alert);
        }
    }
}
