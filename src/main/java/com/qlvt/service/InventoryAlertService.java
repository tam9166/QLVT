package com.qlvt.service;

import com.qlvt.entity.Material;
import com.qlvt.entity.MaterialBatch;
import com.qlvt.entity.Notification;
import com.qlvt.enums.ExpiryAlertLevel;
import com.qlvt.enums.StockAlertLevel;
import com.qlvt.repository.MaterialBatchRepository;
import com.qlvt.repository.MaterialRepository;
import com.qlvt.repository.NotificationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class InventoryAlertService {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final MaterialRepository materialRepository;
    private final MaterialBatchRepository batchRepository;
    private final NotificationRepository notificationRepository;

    public InventoryAlertService(MaterialRepository materialRepository,
                                 MaterialBatchRepository batchRepository,
                                 NotificationRepository notificationRepository) {
        this.materialRepository = materialRepository;
        this.batchRepository = batchRepository;
        this.notificationRepository = notificationRepository;
    }

    public ExpiryAlertLevel expiryLevel(MaterialBatch batch) {
        if (batch.getExpiryDate() == null) return ExpiryAlertLevel.NORMAL;
        long days = ChronoUnit.DAYS.between(LocalDate.now(), batch.getExpiryDate());
        if (days < 0) return ExpiryAlertLevel.EXPIRED;
        if (days <= 30) return ExpiryAlertLevel.EXPIRING_30;
        if (days <= 60) return ExpiryAlertLevel.EXPIRING_60;
        if (days <= 90) return ExpiryAlertLevel.EXPIRING_90;
        return ExpiryAlertLevel.NORMAL;
    }

    public StockAlertLevel stockLevel(Material material) {
        int available = material.getAvailableQuantity();
        if (available == 0) return StockAlertLevel.OUT_OF_STOCK;
        if (material.getMinStock() > 0 && available < Math.ceil(material.getMinStock() * 0.3)) return StockAlertLevel.CRITICAL_LOW;
        if (material.getMinStock() > 0 && available < material.getMinStock()) return StockAlertLevel.LOW;
        if (material.getMaxStock() > 0 && available > material.getMaxStock()) return StockAlertLevel.OVER_STOCK;
        return StockAlertLevel.NORMAL;
    }

    public long daysToExpiry(MaterialBatch batch) {
        return batch.getExpiryDate() == null ? Long.MAX_VALUE : ChronoUnit.DAYS.between(LocalDate.now(), batch.getExpiryDate());
    }

    public List<MaterialBatch> expiryAlerts() {
        return batchRepository.findAll().stream()
                .filter(batch -> expiryLevel(batch) != ExpiryAlertLevel.NORMAL)
                .sorted(Comparator.comparing(MaterialBatch::getExpiryDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    public List<Material> stockAlerts() {
        return materialRepository.findByDeletedFalseOrderByCodeAsc().stream()
                .filter(material -> stockLevel(material) != StockAlertLevel.NORMAL)
                .toList();
    }

    public Map<ExpiryAlertLevel, Long> countExpiryByLevel() {
        return batchRepository.findAll().stream().collect(Collectors.groupingBy(this::expiryLevel, Collectors.counting()));
    }

    public Map<StockAlertLevel, Long> countStockByLevel() {
        return materialRepository.findByDeletedFalseOrderByCodeAsc().stream().collect(Collectors.groupingBy(this::stockLevel, Collectors.counting()));
    }

    @Scheduled(cron = "0 0 7 * * *", zone = "Asia/Ho_Chi_Minh")
    @Transactional
    public void generateDailyNotificationsBySchedule() {
        generateDailyNotifications();
    }

    @Transactional
    public void generateDailyNotifications() {
        normalizeExistingNotifications();
        LocalDateTime today = LocalDate.now().atStartOfDay();

        for (MaterialBatch batch : expiryAlerts()) {
            ExpiryAlertLevel level = expiryLevel(batch);
            String type = "EXPIRY_" + level.name();
            String link = "/batches/" + batch.getId();
            if (!notificationRepository.existsByTypeAndLinkAndCreatedAtAfter(type, link, today)) {
                saveNotification("Cảnh báo hạn sử dụng", expiryContent(batch, level), type, "WAREHOUSE_STAFF", link);
            }
        }

        for (Material material : stockAlerts()) {
            StockAlertLevel level = stockLevel(material);
            String type = "STOCK_" + level.name() + "_" + material.getCode();
            String link = "/alerts#stock-alerts";
            if (!notificationRepository.existsByTypeAndLinkAndCreatedAtAfter(type, link, today)) {
                saveNotification("Cảnh báo tồn kho", stockContent(material, level), type, "WAREHOUSE_STAFF", link);
            }
        }
    }

    @Transactional
    public void notifyOutOfStockIfNeeded(Material material, String link) {
        notifyOutOfStockIfNeeded(material, material == null ? 0 : material.getAvailableQuantity(), link);
    }

    @Transactional
    public void notifyOutOfStockIfNeeded(Material material, long effectiveAvailable, String link) {
        if (material == null || effectiveAvailable > 0) {
            return;
        }
        LocalDateTime today = LocalDate.now().atStartOfDay();
        String targetLink = link == null || link.isBlank() ? "/alerts#stock-alerts" : link;
        String type = "STOCK_OUT_OF_STOCK_" + material.getCode();
        if (!notificationRepository.existsByTypeAndLinkAndCreatedAtAfter(type, targetLink, today)) {
            saveNotification("Cảnh báo hết kho", outOfStockContent(material), type, "WAREHOUSE_STAFF", targetLink);
        }
    }

    @Transactional
    public void normalizeExistingNotifications() {
        List<Notification> notifications = notificationRepository.findAll();
        for (Notification notification : notifications) {
            String type = notification.getType();
            if (type == null) {
                continue;
            }
            if (type.startsWith("EXPIRY_")) {
                notification.setTitle("Cảnh báo hạn sử dụng");
                parseBatchId(notification.getLink()).flatMap(batchRepository::findById)
                        .ifPresent(batch -> notification.setContent(expiryContent(batch, expiryLevel(batch))));
            } else if (type.startsWith("STOCK_OUT_OF_STOCK_")) {
                notification.setTitle("Cảnh báo hết kho");
            } else if (type.startsWith("STOCK_")) {
                notification.setTitle("Cảnh báo tồn kho");
            } else if (type.startsWith("PRICE_")) {
                notification.setTitle("Cảnh báo giá");
            } else if ("REQUEST".equals(type)) {
                notification.setTitle("Yêu cầu mới cần xử lý");
                notification.setContent("Có yêu cầu cấp vật tư mới cần kho xử lý.");
            } else if ("DEPARTMENT_RETURN".equals(type)) {
                notification.setTitle("Có phiếu trả từ khoa");
                notification.setContent("Có phiếu trả vật tư từ khoa cần kiểm tra.");
            } else if ("INVENTORY_COUNT".equals(type)) {
                notification.setTitle("Kiểm kê đã hoàn tất");
                notification.setContent("Phiếu kiểm kê đã hoàn tất, vui lòng xem chênh lệch nếu có.");
            }
        }
        notificationRepository.saveAll(notifications);
    }

    private String expiryContent(MaterialBatch batch, ExpiryAlertLevel level) {
        long days = daysToExpiry(batch);
        String remaining;
        if (days < 0) {
            remaining = "đã quá hạn " + Math.abs(days) + " ngày";
        } else if (days == 0) {
            remaining = "hết hạn hôm nay";
        } else {
            remaining = "còn " + days + " ngày";
        }
        return batch.getMaterial().getCode() + " - " + batch.getMaterial().getName()
                + ", lô " + batch.getBatchNumber()
                + ", HSD " + (batch.getExpiryDate() == null ? "-" : batch.getExpiryDate().format(DATE))
                + ", số lượng " + batch.getQuantity()
                + ", mức " + level.getLabel()
                + " (" + remaining + ").";
    }

    private String stockContent(Material material, StockAlertLevel level) {
        return material.getCode() + " - " + material.getName()
                + ": " + level.getLabel()
                + ", tồn thực tế " + material.getActualQuantity()
                + ", khả dụng " + material.getAvailableQuantity()
                + ", tối thiểu " + material.getMinStock() + ".";
    }

    private String outOfStockContent(Material material) {
        return material.getCode() + " - " + material.getName()
                + " đã hết tồn khả dụng trong kho. Tồn thực tế "
                + material.getActualQuantity()
                + ", đang giữ/chờ xuất "
                + (material.getReservedQuantity() + material.getPendingIssueQuantity())
                + ".";
    }

    private java.util.Optional<Long> parseBatchId(String link) {
        if (link == null || !link.startsWith("/batches/")) {
            return java.util.Optional.empty();
        }
        try {
            return java.util.Optional.of(Long.parseLong(link.substring("/batches/".length())));
        } catch (NumberFormatException ex) {
            return java.util.Optional.empty();
        }
    }

    private void saveNotification(String title, String content, String type, String receiver, String link) {
        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setReceiver(receiver);
        notification.setLink(link);
        notificationRepository.save(notification);
    }
}
