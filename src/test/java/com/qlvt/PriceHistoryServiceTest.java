package com.qlvt;

import com.qlvt.entity.*;
import com.qlvt.repository.MaterialPriceHistoryRepository;
import com.qlvt.repository.PriceAlertRepository;
import com.qlvt.service.AuditService;
import com.qlvt.service.NotificationService;
import com.qlvt.service.PriceHistoryService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PriceHistoryServiceTest {
    @Test
    void priceIncreaseOverTwentyPercentCreatesCriticalAlert() {
        MaterialPriceHistoryRepository historyRepository = mock(MaterialPriceHistoryRepository.class);
        PriceAlertRepository alertRepository = mock(PriceAlertRepository.class);
        PriceHistoryService service = new PriceHistoryService(historyRepository, alertRepository, mock(NotificationService.class), mock(AuditService.class));
        when(historyRepository.save(any(MaterialPriceHistory.class))).thenAnswer(invocation -> {
            MaterialPriceHistory history = invocation.getArgument(0);
            history.setId(2L);
            return history;
        });
        Material material = new Material();
        material.setId(1L);
        material.setCode("VT001");
        material.setName("Găng tay y tế");
        MaterialPriceHistory previous = new MaterialPriceHistory();
        previous.setMaterial(material);
        previous.setUnitPrice(BigDecimal.valueOf(100));
        when(historyRepository.findTopByMaterial_IdAndIdNotOrderByReceivedDateDescCreatedAtDesc(1L, 2L)).thenReturn(Optional.of(previous));

        Receipt receipt = new Receipt();
        receipt.setReceiptDate(LocalDate.now());
        ReceiptLine line = new ReceiptLine();
        line.setMaterial(material);
        line.setQuantity(10);
        line.setUnitPrice(BigDecimal.valueOf(125));

        service.recordReceiptLine(receipt, line, "admin");

        verify(alertRepository).save(any(PriceAlert.class));
    }

    @Test
    void resolvingAlertRecordsActorAndAuditTrail() {
        MaterialPriceHistoryRepository historyRepository = mock(MaterialPriceHistoryRepository.class);
        PriceAlertRepository alertRepository = mock(PriceAlertRepository.class);
        AuditService auditService = mock(AuditService.class);
        PriceHistoryService service = new PriceHistoryService(
                historyRepository, alertRepository, mock(NotificationService.class), auditService);
        Material material = new Material();
        material.setCode("VT001");
        PriceAlert alert = new PriceAlert();
        alert.setMaterial(material);
        when(alertRepository.findById(7L)).thenReturn(Optional.of(alert));

        service.resolveAlert(7L, "accountant");

        assertTrue(alert.isResolved());
        assertTrue(alert.getResolvedAt().isAfter(LocalDateTime.now().minusSeconds(2)));
        verify(alertRepository).save(alert);
        verify(auditService).log("accountant", "RESOLVE_PRICE_ALERT", "PRICE_ALERT", "7",
                "Đánh dấu đã xử lý cảnh báo giá vật tư VT001");
    }

    @Test
    void resolvedAlertCannotBeResolvedAgain() {
        PriceAlertRepository alertRepository = mock(PriceAlertRepository.class);
        PriceHistoryService service = new PriceHistoryService(
                mock(MaterialPriceHistoryRepository.class), alertRepository,
                mock(NotificationService.class), mock(AuditService.class));
        PriceAlert alert = new PriceAlert();
        alert.setResolved(true);
        LocalDateTime firstResolvedAt = LocalDateTime.now().minusDays(1);
        alert.setResolvedAt(firstResolvedAt);
        when(alertRepository.findById(7L)).thenReturn(Optional.of(alert));

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> service.resolveAlert(7L, "accountant"));

        assertEquals("Cảnh báo giá đã được xử lý", error.getMessage());
        assertEquals(firstResolvedAt, alert.getResolvedAt());
        verify(alertRepository, never()).save(any());
    }
}
