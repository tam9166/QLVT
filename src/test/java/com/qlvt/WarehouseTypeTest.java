package com.qlvt;

import com.qlvt.enums.WarehouseType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WarehouseTypeTest {

    @Test
    void exposesFriendlyLabelsWithoutChangingPersistedEnumNames() {
        assertThat(WarehouseType.MAIN.name()).isEqualTo("MAIN");
        assertThat(WarehouseType.MAIN.getLabel()).isEqualTo("Kho chính");
        assertThat(WarehouseType.QUARANTINE.toString()).isEqualTo("Kho cách ly");
    }
}
