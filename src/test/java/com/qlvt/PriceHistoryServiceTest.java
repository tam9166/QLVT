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
import java.util.Optional;

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
}
