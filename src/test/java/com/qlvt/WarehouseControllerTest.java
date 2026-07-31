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
import org.springframework.security.core.Authentication;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

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

    @Test
    void deleteWarehouseRedirectsWithBusinessError() {
        WarehouseAdminService service = mock(WarehouseAdminService.class);
        WarehouseController controller = new WarehouseController(
                service, mock(WarehouseRepository.class), mock(StorageLocationRepository.class));
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("admin");
        doThrow(new IllegalStateException("Kho đang còn tồn kho"))
                .when(service).deleteWarehouse(1L, "admin");
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        assertEquals("redirect:/warehouses",
                controller.deleteWarehouse(1L, authentication, redirectAttributes));
        assertEquals("Kho đang còn tồn kho",
                redirectAttributes.getFlashAttributes().get("errorMessage"));
    }

    @Test
    void deleteLocationRedirectsWithSuccessMessage() {
        WarehouseAdminService service = mock(WarehouseAdminService.class);
        WarehouseController controller = new WarehouseController(
                service, mock(WarehouseRepository.class), mock(StorageLocationRepository.class));
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("admin");
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        assertEquals("redirect:/locations",
                controller.deleteLocation(2L, authentication, redirectAttributes));
        verify(service).deleteLocation(2L, "admin");
        assertEquals("Đã xóa vị trí",
                redirectAttributes.getFlashAttributes().get("successMessage"));
    }
}
