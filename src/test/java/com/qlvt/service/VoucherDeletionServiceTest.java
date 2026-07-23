package com.qlvt.service;

import com.qlvt.entity.Material;
import com.qlvt.entity.MaterialBatch;
import com.qlvt.entity.StockMovement;
import com.qlvt.entity.StorageLocation;
import com.qlvt.entity.Warehouse;
import com.qlvt.enums.MovementType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class VoucherDeletionServiceTest {

    @Test
    void receiptDeletionCreatesTraceableOutboundReversal() {
        Material material = new Material();
        MaterialBatch batch = new MaterialBatch();
        Warehouse warehouse = new Warehouse();
        StorageLocation location = new StorageLocation();

        StockMovement movement = VoucherDeletionService.reversalMovement(
                MovementType.OUT, material, batch, warehouse, location,
                -7, 20, 13, "DELETE_RECEIPT", "PN-001",
                "Đảo tồn do xóa phiếu nhập", "admin");

        assertEquals(MovementType.OUT, movement.getMovementType());
        assertEquals(-7, movement.getQuantity());
        assertEquals(20, movement.getBeforeQuantity());
        assertEquals(13, movement.getAfterQuantity());
        assertEquals("DELETE_RECEIPT", movement.getReferenceType());
        assertEquals("PN-001", movement.getReferenceCode());
        assertEquals("admin", movement.getCreatedBy());
        assertSame(material, movement.getMaterial());
        assertSame(batch, movement.getBatch());
        assertSame(warehouse, movement.getWarehouse());
        assertSame(location, movement.getLocation());
    }

    @Test
    void issueDeletionCreatesTraceableInboundReversal() {
        StockMovement movement = VoucherDeletionService.reversalMovement(
                MovementType.IN, new Material(), new MaterialBatch(), new Warehouse(), new StorageLocation(),
                4, 9, 13, "DELETE_ISSUE", "PX-001",
                "Hoàn tồn do xóa phiếu xuất", "admin");

        assertEquals(MovementType.IN, movement.getMovementType());
        assertEquals(4, movement.getQuantity());
        assertEquals(9, movement.getBeforeQuantity());
        assertEquals(13, movement.getAfterQuantity());
        assertEquals("DELETE_ISSUE", movement.getReferenceType());
        assertEquals("PX-001", movement.getReferenceCode());
    }
}
