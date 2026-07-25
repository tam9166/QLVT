package com.qlvt;

import com.qlvt.entity.DestructionSlip;
import com.qlvt.entity.InventoryCountLine;
import com.qlvt.entity.InventoryCount;
import com.qlvt.entity.IssueSlip;
import com.qlvt.entity.Receipt;
import com.qlvt.entity.StockBalance;
import com.qlvt.entity.StockAdjustment;
import com.qlvt.entity.StockTransfer;
import com.qlvt.entity.PurchaseOrder;
import com.qlvt.entity.PurchaseRequest;
import com.qlvt.entity.RecallOrder;
import com.qlvt.entity.RecallOrderLine;
import com.qlvt.entity.MaterialRequest;
import com.qlvt.enums.DestructionStatus;
import com.qlvt.enums.PurchaseOrderStatus;
import com.qlvt.enums.PurchaseRequestStatus;
import com.qlvt.enums.StockTransferStatus;
import com.qlvt.enums.StockAdjustmentStatus;
import com.qlvt.enums.InventoryCountStatus;
import com.qlvt.enums.IssueStatus;
import com.qlvt.enums.ReceiptStatus;
import com.qlvt.enums.RecallStatus;
import com.qlvt.enums.RequestStatus;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class Prompt3BusinessRuleTest {
    @Test
    void materialRequestActionsFollowWorkflowState() {
        MaterialRequest request = new MaterialRequest();
        assertTrue(request.canApproveDepartment());
        assertFalse(request.canReserveStock());
        assertFalse(request.canCreateIssueSlip());
        assertTrue(request.canCancel());

        request.setStatus(RequestStatus.DEPARTMENT_APPROVED);
        assertFalse(request.canApproveDepartment());
        assertTrue(request.canReserveStock());
        assertFalse(request.canCreateIssueSlip());
        assertTrue(request.canCancel());

        request.setStatus(RequestStatus.PARTIALLY_APPROVED);
        assertFalse(request.canReserveStock());
        assertTrue(request.canCreateIssueSlip());
        assertTrue(request.canCancel());

        request.setStatus(RequestStatus.PREPARING);
        assertFalse(request.canCreateIssueSlip());
        assertFalse(request.canCancel());
    }

    @Test
    void warehouseDocumentActionsFollowWorkflowState() {
        Receipt receipt = new Receipt();
        assertTrue(receipt.canEdit());
        assertTrue(receipt.canConfirm());
        receipt.setStatus(ReceiptStatus.CONFIRMED);
        assertFalse(receipt.canEdit());
        assertFalse(receipt.canConfirm());

        IssueSlip issue = new IssueSlip();
        assertTrue(issue.canEdit());
        assertTrue(issue.canIssue());
        assertFalse(issue.canReceive());
        issue.setStatus(IssueStatus.ISSUED);
        assertFalse(issue.canEdit());
        assertFalse(issue.canIssue());
        assertTrue(issue.canReceive());
        issue.setStatus(IssueStatus.RECEIVED);
        assertFalse(issue.canReceive());
    }

    @Test
    void inventoryCountLineCalculatesDifference() {
        InventoryCountLine line = new InventoryCountLine();
        line.setSystemQuantity(20);
        line.setActualQuantity(17);

        assertEquals(-3, line.getDifferenceQuantity());
    }

    @Test
    void inventoryCountActionsFollowWorkflowState() {
        InventoryCount count = new InventoryCount();

        assertTrue(count.canEditLines());
        assertTrue(count.canComplete());
        assertFalse(count.canCreateAdjustment());

        count.setStatus(InventoryCountStatus.COMPLETED);
        assertFalse(count.canEditLines());
        assertFalse(count.canComplete());
        assertTrue(count.canCreateAdjustment());

        count.setStatus(InventoryCountStatus.CANCELLED);
        assertFalse(count.canEditLines());
        assertFalse(count.canComplete());
        assertFalse(count.canCreateAdjustment());
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
        assertFalse(transfer.canReject());
        assertFalse(transfer.canExecuteTransfer());
        assertFalse(transfer.canReceive());

        transfer.setStatus(StockTransferStatus.SUBMITTED);
        assertTrue(transfer.canApprove());
        assertTrue(transfer.canReject());

        transfer.setStatus(StockTransferStatus.APPROVED);

        assertFalse(transfer.canSubmit());
        assertFalse(transfer.canApprove());
        assertFalse(transfer.canReject());
        assertTrue(transfer.canExecuteTransfer());
        assertFalse(transfer.canReceive());

        transfer.setStatus(StockTransferStatus.TRANSFERRED);

        assertFalse(transfer.canExecuteTransfer());
        assertTrue(transfer.canReceive());
    }

    @Test
    void destructionActionsFollowWorkflowState() {
        DestructionSlip slip = new DestructionSlip();

        assertTrue(slip.canSubmit());
        assertFalse(slip.canApproveManager());
        assertFalse(slip.canApproveAccountant());
        assertFalse(slip.canDestroy());

        slip.setStatus(DestructionStatus.SUBMITTED);

        assertFalse(slip.canSubmit());
        assertTrue(slip.canApproveManager());
        assertFalse(slip.canApproveAccountant());
        assertFalse(slip.canDestroy());

        slip.setStatus(DestructionStatus.APPROVED_BY_MANAGER);

        assertFalse(slip.canApproveManager());
        assertTrue(slip.canApproveAccountant());
        assertFalse(slip.canDestroy());

        slip.setStatus(DestructionStatus.APPROVED);

        assertFalse(slip.canApproveAccountant());
        assertTrue(slip.canDestroy());
        assertFalse(slip.canReject());
        assertFalse(slip.canCancel());

        slip.setStatus(DestructionStatus.DRAFT);
        assertTrue(slip.canCancel());
        assertFalse(slip.canReject());

        slip.setStatus(DestructionStatus.SUBMITTED);
        assertFalse(slip.canCancel());
        assertTrue(slip.canReject());

        slip.setStatus(DestructionStatus.APPROVED_BY_MANAGER);
        assertTrue(slip.canReject());

        slip.setStatus(DestructionStatus.REJECTED);
        assertFalse(slip.canReject());
        assertFalse(slip.canCancel());
    }

    @Test
    void stockAdjustmentActionsFollowWorkflowState() {
        StockAdjustment adjustment = new StockAdjustment();

        assertTrue(adjustment.canSubmit());
        assertFalse(adjustment.canApproveManager());
        assertFalse(adjustment.canApproveAccountant());

        adjustment.setStatus(StockAdjustmentStatus.SUBMITTED);
        assertFalse(adjustment.canSubmit());
        assertTrue(adjustment.canApproveManager());
        assertFalse(adjustment.canApproveAccountant());

        adjustment.setStatus(StockAdjustmentStatus.APPROVED_BY_MANAGER);
        assertFalse(adjustment.canApproveManager());
        assertTrue(adjustment.canApproveAccountant());

        adjustment.setStatus(StockAdjustmentStatus.COMPLETED);
        assertFalse(adjustment.canSubmit());
        assertFalse(adjustment.canApproveManager());
        assertFalse(adjustment.canApproveAccountant());
    }

    @Test
    void purchaseActionsFollowWorkflowState() {
        PurchaseRequest request = new PurchaseRequest();
        assertTrue(request.canApprove());
        assertTrue(request.canCancel());
        assertFalse(request.canCreateOrder());
        request.setStatus(PurchaseRequestStatus.APPROVED);
        assertFalse(request.canApprove());
        assertFalse(request.canCancel());
        assertTrue(request.canCreateOrder());

        PurchaseOrder order = new PurchaseOrder();
        assertTrue(order.canSend());
        assertTrue(order.canCancel());
        assertFalse(order.canReceive());
        order.setStatus(PurchaseOrderStatus.SENT);
        assertFalse(order.canSend());
        assertTrue(order.canCancel());
        assertTrue(order.canReceive());
        order.setStatus(PurchaseOrderStatus.PARTIALLY_RECEIVED);
        assertFalse(order.canCancel());
        assertTrue(order.canReceive());
    }

    @Test
    void recallActionsRequireEveryDepartmentResponse() {
        RecallOrder recall = new RecallOrder();
        RecallOrderLine line = new RecallOrderLine();
        recall.getLines().add(line);

        assertTrue(recall.canActivate());
        assertTrue(recall.canCancel());
        assertFalse(recall.canRespond());
        assertFalse(recall.canComplete());

        recall.setStatus(RecallStatus.ACTIVE);
        assertFalse(recall.canActivate());
        assertFalse(recall.canCancel());
        assertTrue(recall.canRespond());
        assertTrue(recall.hasPendingResponses());
        assertFalse(recall.canComplete());

        line.setStatus("RESPONDED");
        assertFalse(recall.hasPendingResponses());
        assertTrue(recall.canComplete());

        recall.setStatus(RecallStatus.COMPLETED);
        assertFalse(recall.canRespond());
        assertFalse(recall.canComplete());
    }
}
