package com.qlvt.service;

import com.qlvt.entity.*;
import com.qlvt.enums.PriceAlertLevel;
import com.qlvt.repository.MaterialPriceHistoryRepository;
import com.qlvt.repository.PriceAlertRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
public class PriceHistoryService {
    private static final BigDecimal WARNING_PERCENT = BigDecimal.valueOf(10);
    private static final BigDecimal CRITICAL_PERCENT = BigDecimal.valueOf(20);

    private final MaterialPriceHistoryRepository historyRepository;
    private final PriceAlertRepository alertRepository;
    private final NotificationService notificationService;
    private final AuditService auditService;

    public PriceHistoryService(MaterialPriceHistoryRepository historyRepository,
                               PriceAlertRepository alertRepository,
                               NotificationService notificationService,
                               AuditService auditService) {
        this.historyRepository = historyRepository;
        this.alertRepository = alertRepository;
        this.notificationService = notificationService;
        this.auditService = auditService;
    }

    @Transactional
    public void recordReceiptLine(Receipt receipt, ReceiptLine line, String username) {
        if (line.getUnitPrice() == null || line.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        MaterialPriceHistory history = new MaterialPriceHistory();
        history.setMaterial(line.getMaterial());
        history.setSupplier(receipt.getSupplier());
        history.setReceipt(receipt);
        history.setUnitPrice(line.getUnitPrice());
        history.setQuantity(line.getQuantity());
        history.setTotalAmount(line.getUnitPrice().multiply(BigDecimal.valueOf(line.getQuantity())));
        history.setReceivedDate(receipt.getReceiptDate());
        history.setCreatedBy(username);
        history.setNote(line.getNote());
        historyRepository.save(history);
        historyRepository.findTopByMaterial_IdAndIdNotOrderByReceivedDateDescCreatedAtDesc(line.getMaterial().getId(), history.getId())
                .ifPresent(previous -> createAlertIfNeeded(previous, history));
        auditService.log(username, "CREATE_PRICE_HISTORY", "MATERIAL", line.getMaterial().getCode(), "Ghi lịch sử giá nhập vật tư");
    }

    @Transactional
    public void resolveAlert(Long alertId) {
        PriceAlert alert = alertRepository.findById(alertId).orElseThrow();
        alert.setResolved(true);
        alert.setResolvedAt(LocalDateTime.now());
        alertRepository.save(alert);
    }

    private void createAlertIfNeeded(MaterialPriceHistory previous, MaterialPriceHistory current) {
        BigDecimal oldPrice = previous.getUnitPrice();
        BigDecimal newPrice = current.getUnitPrice();
        if (oldPrice.compareTo(BigDecimal.ZERO) <= 0 || newPrice.compareTo(oldPrice) <= 0) {
            return;
        }
        BigDecimal percent = newPrice.subtract(oldPrice)
                .multiply(BigDecimal.valueOf(100))
                .divide(oldPrice, 2, RoundingMode.HALF_UP);
        PriceAlertLevel level = percent.compareTo(CRITICAL_PERCENT) >= 0 ? PriceAlertLevel.CRITICAL
                : percent.compareTo(WARNING_PERCENT) >= 0 ? PriceAlertLevel.WARNING
                : PriceAlertLevel.NORMAL;
        if (level == PriceAlertLevel.NORMAL) {
            return;
        }
        PriceAlert alert = new PriceAlert();
        alert.setMaterial(current.getMaterial());
        alert.setSupplier(current.getSupplier());
        alert.setOldPrice(oldPrice);
        alert.setNewPrice(newPrice);
        alert.setDifferencePercent(percent);
        alert.setAlertLevel(level);
        alert.setReceipt(current.getReceipt());
        alert.setMessage("Giá nhập " + current.getMaterial().getName() + " tăng " + percent + "% so với lần nhập trước");
        alertRepository.save(alert);
        notificationService.notify("Cảnh báo giá nhập bất thường", alert.getMessage(), "PRICE_ALERT", "ACCOUNTANT", "/price-alerts");
        notificationService.notify("Cảnh báo giá nhập bất thường", alert.getMessage(), "PRICE_ALERT", "PROCUREMENT", "/price-alerts");
    }
}
