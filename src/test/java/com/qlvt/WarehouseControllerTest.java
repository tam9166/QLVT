package com.qlvt;

import com.qlvt.controller.WarehouseController;
import com.qlvt.repository.StorageLocationRepository;
import com.qlvt.repository.WarehouseRepository;
import com.qlvt.service.WarehouseAdminService;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

class WarehouseControllerTest {

    @Test
    void createLocationOnlyOffersActiveWarehousesAndParents() {
        WarehouseAdminService service = mock(WarehouseAdminService.class);
        WarehouseRepository warehouseRepository = mock(WarehouseRepository.class);
        StorageLocationRepository locationRepository = mock(StorageLocationRepository.class);
        when(warehouseRepository.findByDeletedFalseAndActiveTrueOrderByCodeAsc()).thenReturn(List.of());
        when(service.parentLocationChoices(any())).thenReturn(List.of());
        WarehouseController controller = new WarehouseController(service, warehouseRepository, locationRepository);
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals("locations/form", controller.createLocation(model));

        verify(warehouseRepository).findByDeletedFalseAndActiveTrueOrderByCodeAsc();
        verify(service).parentLocationChoices(any());
        assertEquals(List.of(), model.get("warehouses"));
        assertEquals(List.of(), model.get("parents"));
    }
}
