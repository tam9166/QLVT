package com.qlvt;

import com.qlvt.entity.InventoryCountLine;
import com.qlvt.entity.StockBalance;
import com.qlvt.entity.StockTransfer;
import com.qlvt.enums.StockTransferStatus;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class Prompt3BusinessRuleTest {
    @Test
    void inventoryCountLineCalculatesDifference() {
        InventoryCountLine line = new InventoryCountLine();
        line.setSystemQuantity(20);
        line.setActualQuantity(17);

        assertEquals(-3, line.getDifferenceQuantity());
    }

    @Test
    void stockBalanceRejectsNegativeAfterDestructionOrTransfer() {
        StockBalance balance = new StockBalance();
        balance.setActualQuantity(-1);

        assertThrows(IllegalStateException.class, balance::validate);
    }

    @Test
    void stockBalancePreventsDestroyingReservedQuantity() {
        StockBalance balance = new StockBalance();
        balance.setActualQuantity(10);
        balance.setReservedQuantity(8);
        balance.setPendingIssueQuantity(3);

        assertThrows(IllegalStateException.class, balance::validate);
    }

    @Test
    void stockTransferActionsFollowWorkflowState() {
        StockTransfer transfer = new StockTransfer();

        assertTrue(transfer.canSubmit());
        assertTrue(transfer.canApprove());
        assertFalse(transfer.canExecuteTransfer());
        assertFalse(transfer.canReceive());

        transfer.setStatus(StockTransferStatus.APPROVED);

        assertFalse(transfer.canSubmit());
        assertFalse(transfer.canApprove());
        assertTrue(transfer.canExecuteTransfer());
        assertFalse(transfer.canReceive());

        transfer.setStatus(StockTransferStatus.TRANSFERRED);

        assertFalse(transfer.canExecuteTransfer());
        assertTrue(transfer.canReceive());
    }
}
