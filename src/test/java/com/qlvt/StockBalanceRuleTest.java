package com.qlvt;

import com.qlvt.entity.StockBalance;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StockBalanceRuleTest {
    @Test
    void availableQuantitySubtractsReservedAndPendingIssue() {
        StockBalance balance = new StockBalance();
        balance.setActualQuantity(100);
        balance.setReservedQuantity(30);
        balance.setPendingIssueQuantity(10);

        assertEquals(60, balance.getAvailableQuantity());
        assertDoesNotThrow(balance::validate);
    }

    @Test
    void validateRejectsReservationGreaterThanActualQuantity() {
        StockBalance balance = new StockBalance();
        balance.setActualQuantity(20);
        balance.setReservedQuantity(21);

        assertThrows(IllegalStateException.class, balance::validate);
    }
}
