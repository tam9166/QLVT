package com.qlvt;

import com.qlvt.entity.Material;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class InventoryFormulaTest {
    @Test
    void availableQuantityEqualsActualMinusReservedMinusPendingIssue() {
        Material material = new Material();
        material.setActualQuantity(100);
        material.setReservedQuantity(25);
        material.setPendingIssueQuantity(10);

        assertThat(material.getAvailableQuantity()).isEqualTo(65);
    }
}
