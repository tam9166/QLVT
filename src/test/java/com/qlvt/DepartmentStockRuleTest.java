package com.qlvt;

import com.qlvt.entity.DepartmentStock;
import com.qlvt.entity.DepartmentReturn;
import com.qlvt.enums.DepartmentReturnStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DepartmentStockRuleTest {
    @Test
    void departmentStockCannotBeNegative() {
        DepartmentStock stock = new DepartmentStock();
        stock.setQuantityOnHand(-1);
        assertThrows(IllegalStateException.class, stock::validate);
    }

    @Test
    void onlySubmittedDepartmentReturnCanBeReceivedByWarehouse() {
        DepartmentReturn item = new DepartmentReturn();
        assertFalse(item.canReceiveByWarehouse());

        item.setStatus(DepartmentReturnStatus.SUBMITTED);
        assertTrue(item.canReceiveByWarehouse());

        item.setStatus(DepartmentReturnStatus.RECEIVED_BY_WAREHOUSE);
        assertFalse(item.canReceiveByWarehouse());
    }
}
