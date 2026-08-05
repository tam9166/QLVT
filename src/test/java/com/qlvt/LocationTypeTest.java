package com.qlvt;

import com.qlvt.enums.LocationType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LocationTypeTest {

    @Test
    void exposesFriendlyLabelsWithoutChangingPersistedEnumNames() {
        assertThat(LocationType.SHELF.name()).isEqualTo("SHELF");
        assertThat(LocationType.SHELF.getLabel()).isEqualTo("Kệ");
        assertThat(LocationType.CABINET.toString()).isEqualTo("Tủ");
    }

    @Test
    void resolvesPersistedNamesWithoutBreakingOnLegacyValues() {
        assertThat(LocationType.labelOf("DRAWER")).isEqualTo("Ngăn kéo");
        assertThat(LocationType.labelOf(null)).isEqualTo("-");
        assertThat(LocationType.labelOf("LEGACY_TYPE")).isEqualTo("LEGACY_TYPE");
    }

    @Test
    void resolvesPersistedNamesCaseInsensitively() {
        assertThat(LocationType.fromPersistedName(" shelf ")).isEqualTo(LocationType.SHELF);
        assertThat(LocationType.labelOf("shelf")).isEqualTo(LocationType.SHELF.getLabel());
    }
}
