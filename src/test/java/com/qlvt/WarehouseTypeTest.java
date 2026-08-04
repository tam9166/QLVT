package com.qlvt;

import com.qlvt.enums.WarehouseType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WarehouseTypeTest {

    @Test
    void exposesFriendlyLabelsWithoutChangingPersistedEnumNames() {
        assertThat(WarehouseType.MAIN.name()).isEqualTo("MAIN");
        assertThat(WarehouseType.labelOf("MAIN")).isEqualTo(WarehouseType.MAIN.getLabel());
        assertThat(WarehouseType.labelOf(null)).isEqualTo("-");
        assertThat(WarehouseType.labelOf("LEGACY_TYPE")).isEqualTo("LEGACY_TYPE");
        assertThat(WarehouseType.MAIN.getLabel()).isEqualTo("Kho chính");
        assertThat(WarehouseType.QUARANTINE.toString()).isEqualTo("Kho cách ly");
    }
}
