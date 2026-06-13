package com.qlvt.controller;

import com.qlvt.entity.Material;
import com.qlvt.entity.StockMovement;
import com.qlvt.enums.BatchStatus;
import com.qlvt.enums.MovementType;
import com.qlvt.enums.RequestStatus;
import com.qlvt.repository.*;
import com.qlvt.service.CurrentUserService;
import com.qlvt.service.StorageMonitoringService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class DashboardController {
    private final MaterialRepository materialRepository;
    private final MaterialBatchRepository batchRepository;
    private final MaterialRequestRepository requestRepository;
    private final StockMovementRepository movementRepository;
    private final NotificationRepository notificationRepository;
    private final AppUserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final WarehouseRepository warehouseRepository;
    private final DepartmentStockRepository departmentStockRepository;
    private final PriceAlertRepository priceAlertRepository;
    private final AuditLogRepository auditLogRepository;
    private final CurrentUserService currentUserService;
    private final StorageMonitoringService storageMonitoringService;

    public DashboardController(MaterialRepository materialRepository,
                               MaterialBatchRepository batchRepository,
                               MaterialRequestRepository requestRepository,
                               StockMovementRepository movementRepository,
                               NotificationRepository notificationRepository,
                               AppUserRepository userRepository,
                               DepartmentRepository departmentRepository,
                               WarehouseRepository warehouseRepository,
                               DepartmentStockRepository departmentStockRepository,
                               PriceAlertRepository priceAlertRepository,
                               AuditLogRepository auditLogRepository,
                               CurrentUserService currentUserService,
                               StorageMonitoringService storageMonitoringService) {
        this.materialRepository = materialRepository;
        this.batchRepository = batchRepository;
        this.requestRepository = requestRepository;
        this.movementRepository = movementRepository;
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.warehouseRepository = warehouseRepository;
        this.departmentStockRepository = departmentStockRepository;
        this.priceAlertRepository = priceAlertRepository;
        this.auditLogRepository = auditLogRepository;
        this.currentUserService = currentUserService;
        this.storageMonitoringService = storageMonitoringService;
    }

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        var user = currentUserService.currentUser();
        LocalDate today = LocalDate.now();
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Material> materials = materialRepository.findByDeletedFalseOrderByCodeAsc();
        var batches = batchRepository.findAll();
        var movements = movementRepository.findAll();
        var lowStocks = materials.stream().filter(m -> m.getActualQuantity() <= m.getMinStock()).toList();
        var expiringBatches30 = batches.stream()
                .filter(batch -> batch.getExpiryDate() != null)
                .filter(batch -> !batch.getExpiryDate().isBefore(today))
                .filter(batch -> !batch.getExpiryDate().isAfter(today.plusDays(30)))
                .sorted(Comparator.comparing(batch -> batch.getExpiryDate()))
                .toList();
        var expiringBatches90 = batches.stream()
                .filter(batch -> batch.getExpiryDate() != null)
                .filter(batch -> !batch.getExpiryDate().isBefore(today))
                .filter(batch -> !batch.getExpiryDate().isAfter(today.plusDays(90)))
                .sorted(Comparator.comparing(batch -> batch.getExpiryDate()))
                .toList();
        var lockedBatches = batches.stream()
                .filter(batch -> batch.getStatus() != BatchStatus.AVAILABLE
                        || (batch.getExpiryDate() != null && !batch.getExpiryDate().isAfter(today)))
                .sorted(Comparator.comparing(batch -> batch.getExpiryDate(), Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        model.addAttribute("currentUser", user);
        model.addAttribute("materialCount", materials.size());
        model.addAttribute("userCount", userRepository.count());
        model.addAttribute("departmentCount", departmentRepository.count());
        model.addAttribute("warehouseCount", warehouseRepository.count());
        model.addAttribute("requestPending", requestRepository.countByStatus(RequestStatus.SUBMITTED));
        model.addAttribute("warehousePending", requestRepository.countByStatus(RequestStatus.DEPARTMENT_APPROVED));
        model.addAttribute("myRequests", requestRepository.findTop10ByRequesterOrderByCreatedAtDesc(user.getUsername()));
        model.addAttribute("departmentStocks", user.getDepartment() == null ? java.util.List.of() : departmentStockRepository.findByDepartmentAndQuantityOnHandGreaterThanOrderByMaterial_CodeAscBatch_ExpiryDateAsc(user.getDepartment(), 0));
        model.addAttribute("priceAlertCount", priceAlertRepository.countByResolvedFalse());
        model.addAttribute("auditLogs", auditLogRepository.findTop30ByOrderByCreatedAtDesc().stream().limit(8).toList());
        model.addAttribute("lowStocks", lowStocks);
        model.addAttribute("expiringBatches", expiringBatches90.stream().limit(10).toList());
        model.addAttribute("expiringBatches30Count", expiringBatches30.size());
        model.addAttribute("movements", movements.stream()
                .sorted(Comparator.comparing(StockMovement::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(12)
                .toList());
        model.addAttribute("notifications", notificationRepository.findTop10ByOrderByCreatedAtDesc());
        model.addAttribute("totalInventoryValue", totalInventoryValue(materials));
        model.addAttribute("expiringInventoryValue", batchValue(expiringBatches90));
        model.addAttribute("lockedBatchCount", lockedBatches.size());
        model.addAttribute("lockedBatches", lockedBatches.stream().limit(10).toList());
        model.addAttribute("topIssuedMaterials", topIssuedMaterials(movements, thirtyDaysAgo));
        model.addAttribute("purchaseSuggestions", purchaseSuggestions(materials, movements, thirtyDaysAgo));
        model.addAttribute("storageRiskCount", storageMonitoringService.recentRiskCount());
        model.addAttribute("storageRiskLogs", storageMonitoringService.riskyLogs());
        return "dashboard";
    }

    private BigDecimal totalInventoryValue(List<Material> materials) {
        return materials.stream()
                .map(material -> safePrice(material).multiply(BigDecimal.valueOf(material.getActualQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal batchValue(List<com.qlvt.entity.MaterialBatch> batches) {
        return batches.stream()
                .map(batch -> safePrice(batch.getMaterial()).multiply(BigDecimal.valueOf(batch.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal safePrice(Material material) {
        return material.getEstimatedUnitPrice() == null ? BigDecimal.ZERO : material.getEstimatedUnitPrice();
    }

    private List<MaterialUsageMetric> topIssuedMaterials(List<StockMovement> movements, LocalDateTime since) {
        Map<Long, Integer> quantityByMaterial = new HashMap<>();
        Map<Long, Material> materialById = new HashMap<>();
        movements.stream()
                .filter(movement -> movement.getMovementType() == MovementType.OUT)
                .filter(movement -> movement.getCreatedAt() == null || !movement.getCreatedAt().isBefore(since))
                .forEach(movement -> {
                    Material material = movement.getMaterial();
                    if (material == null || material.getId() == null) {
                        return;
                    }
                    materialById.put(material.getId(), material);
                    quantityByMaterial.merge(material.getId(), issuedQuantity(movement), Integer::sum);
                });
        int max = quantityByMaterial.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        return quantityByMaterial.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .limit(10)
                .map(entry -> {
                    Material material = materialById.get(entry.getKey());
                    int percent = max == 0 ? 0 : (int) Math.round(entry.getValue() * 100.0 / max);
                    return new MaterialUsageMetric(material.getCode(), material.getName(), material.getUnit(), entry.getValue(), percent);
                })
                .toList();
    }

    private List<PurchaseSuggestionMetric> purchaseSuggestions(List<Material> materials, List<StockMovement> movements, LocalDateTime since) {
        Map<Long, Integer> issuedByMaterial = new HashMap<>();
        movements.stream()
                .filter(movement -> movement.getMovementType() == MovementType.OUT)
                .filter(movement -> movement.getMaterial() != null && movement.getMaterial().getId() != null)
                .filter(movement -> movement.getCreatedAt() == null || !movement.getCreatedAt().isBefore(since))
                .forEach(movement -> issuedByMaterial.merge(movement.getMaterial().getId(), issuedQuantity(movement), Integer::sum));

        return materials.stream()
                .map(material -> suggestion(material, issuedByMaterial.getOrDefault(material.getId(), 0)))
                .filter(PurchaseSuggestionMetric::isShouldShow)
                .sorted(Comparator
                        .comparing(PurchaseSuggestionMetric::priorityWeight)
                        .thenComparing(PurchaseSuggestionMetric::getEstimatedDaysLeftNumber))
                .limit(8)
                .toList();
    }

    private PurchaseSuggestionMetric suggestion(Material material, int issuedLast30Days) {
        double averageDailyUsage = issuedLast30Days / 30.0;
        int available = material.getAvailableQuantity();
        double daysLeft = averageDailyUsage <= 0 ? Double.MAX_VALUE : available / averageDailyUsage;
        boolean lowStock = available <= material.getMinStock();
        boolean nearlyOut = averageDailyUsage > 0 && daysLeft <= 14;
        int target = material.getMaxStock() > 0 ? material.getMaxStock() : Math.max(material.getMinStock() * 3, available);
        int suggestedQuantity = Math.max(0, target - available);
        String priority = lowStock ? "Cao" : nearlyOut ? "Theo dõi" : "Bình thường";
        String reason = lowStock ? "Dưới mức tồn tối thiểu" : nearlyOut ? "Ước tính còn dưới 14 ngày" : "Đủ tồn";
        return new PurchaseSuggestionMetric(
                material.getCode(),
                material.getName(),
                material.getUnit(),
                available,
                material.getMinStock(),
                decimal(averageDailyUsage),
                averageDailyUsage <= 0 ? "Chưa có dữ liệu" : decimal(daysLeft) + " ngày",
                daysLeft,
                Math.max(suggestedQuantity, material.getMinStock()),
                priority,
                reason,
                lowStock || nearlyOut
        );
    }

    private int issuedQuantity(StockMovement movement) {
        return Math.abs(movement.getQuantity());
    }

    private String decimal(double value) {
        return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }

    public static class MaterialUsageMetric {
        private final String code;
        private final String name;
        private final String unit;
        private final int quantity;
        private final int percent;

        public MaterialUsageMetric(String code, String name, String unit, int quantity, int percent) {
            this.code = code;
            this.name = name;
            this.unit = unit;
            this.quantity = quantity;
            this.percent = percent;
        }

        public String getCode() { return code; }
        public String getName() { return name; }
        public String getUnit() { return unit; }
        public int getQuantity() { return quantity; }
        public int getPercent() { return percent; }
    }

    public static class PurchaseSuggestionMetric {
        private final String code;
        private final String name;
        private final String unit;
        private final int available;
        private final int minStock;
        private final String averageDailyUsage;
        private final String estimatedDaysLeft;
        private final double estimatedDaysLeftNumber;
        private final int suggestedQuantity;
        private final String priority;
        private final String reason;
        private final boolean shouldShow;

        public PurchaseSuggestionMetric(String code, String name, String unit, int available, int minStock,
                                        String averageDailyUsage, String estimatedDaysLeft,
                                        double estimatedDaysLeftNumber, int suggestedQuantity,
                                        String priority, String reason, boolean shouldShow) {
            this.code = code;
            this.name = name;
            this.unit = unit;
            this.available = available;
            this.minStock = minStock;
            this.averageDailyUsage = averageDailyUsage;
            this.estimatedDaysLeft = estimatedDaysLeft;
            this.estimatedDaysLeftNumber = estimatedDaysLeftNumber;
            this.suggestedQuantity = suggestedQuantity;
            this.priority = priority;
            this.reason = reason;
            this.shouldShow = shouldShow;
        }

        public String getCode() { return code; }
        public String getName() { return name; }
        public String getUnit() { return unit; }
        public int getAvailable() { return available; }
        public int getMinStock() { return minStock; }
        public String getAverageDailyUsage() { return averageDailyUsage; }
        public String getEstimatedDaysLeft() { return estimatedDaysLeft; }
        public double getEstimatedDaysLeftNumber() { return estimatedDaysLeftNumber; }
        public int getSuggestedQuantity() { return suggestedQuantity; }
        public String getPriority() { return priority; }
        public String getReason() { return reason; }
        public boolean isShouldShow() { return shouldShow; }
        public int priorityWeight() { return "Cao".equals(priority) ? 0 : "Theo dõi".equals(priority) ? 1 : 2; }
    }
}
