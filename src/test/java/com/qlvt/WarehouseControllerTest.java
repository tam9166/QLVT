package com.qlvt;

import com.qlvt.controller.WarehouseController;
import com.qlvt.entity.StorageLocation;
import com.qlvt.entity.Warehouse;
import com.qlvt.exception.ResourceNotFoundException;
import com.qlvt.repository.StorageLocationRepository;
import com.qlvt.repository.WarehouseRepository;
import com.qlvt.service.WarehouseAdminService;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

class WarehouseControllerTest {

    @Test
    void editWarehouseRejectsSoftDeletedRecord() {
        WarehouseAdminService service = mock(WarehouseAdminService.class);
        WarehouseRepository warehouseRepository = mock(WarehouseRepository.class);
        StorageLocationRepository locationRepository = mock(StorageLocationRepository.class);
        Warehouse deleted = new Warehouse();
        deleted.setDeleted(true);
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(deleted));
        WarehouseController controller = new WarehouseController(service, warehouseRepository, locationRepository);

        assertThrows(ResourceNotFoundException.class,
                () -> controller.editWarehouse(1L, new ExtendedModelMap()));
    }

    @Test
    void editLocationRejectsSoftDeletedRecord() {
        WarehouseAdminService service = mock(WarehouseAdminService.class);
        WarehouseRepository warehouseRepository = mock(WarehouseRepository.class);
        StorageLocationRepository locationRepository = mock(StorageLocationRepository.class);
        StorageLocation deleted = new StorageLocation();
        deleted.setDeleted(true);
        when(locationRepository.findById(1L)).thenReturn(Optional.of(deleted));
        WarehouseController controller = new WarehouseController(service, warehouseRepository, locationRepository);

        assertThrows(ResourceNotFoundException.class,
                () -> controller.editLocation(1L, new ExtendedModelMap()));
    }

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
