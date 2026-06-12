package com.qlvt;

import com.qlvt.entity.DepartmentStock;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class DepartmentStockRuleTest {
    @Test
    void departmentStockCannotBeNegative() {
        DepartmentStock stock = new DepartmentStock();
        stock.setQuantityOnHand(-1);
        assertThrows(IllegalStateException.class, stock::validate);
    }
}
