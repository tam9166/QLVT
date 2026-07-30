package com.qlvt;

import com.qlvt.entity.StorageLocation;
import com.qlvt.entity.Warehouse;
import com.qlvt.enums.LocationType;
import com.qlvt.form.StorageLocationForm;
import com.qlvt.repository.StorageLocationRepository;
import com.qlvt.repository.StockBalanceRepository;
import com.qlvt.repository.WarehouseRepository;
import com.qlvt.service.AuditService;
import com.qlvt.service.WarehouseAdminService;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class WarehouseAdminServiceTest {

    @Test
    void saveLocationRejectsParentFromAnotherWarehouse() {
        Fixture fixture = new Fixture();
        StorageLocation parent = location(9L, warehouse(2L));
        when(fixture.locationRepository.findById(9L)).thenReturn(Optional.of(parent));
        StorageLocationForm form = form(null, 1L, 9L);
        BindingResult errors = errors(form);

        assertFalse(fixture.service.saveLocation(form, errors, "tester"));

        assertTrue(errors.hasFieldErrors("parentId"));
        verify(fixture.locationRepository, never()).save(any());
    }

    @Test
    void saveLocationRejectsHierarchyCycle() {
        Fixture fixture = new Fixture();
        Warehouse warehouse = warehouse(1L);
        StorageLocation edited = location(7L, warehouse);
        StorageLocation child = location(9L, warehouse);
        child.setParent(edited);
        when(fixture.locationRepository.findById(9L)).thenReturn(Optional.of(child));
        StorageLocationForm form = form(7L, 1L, 9L);
        BindingResult errors = errors(form);

        assertFalse(fixture.service.saveLocation(form, errors, "tester"));

        assertTrue(errors.hasFieldErrors("parentId"));
        verify(fixture.locationRepository, never()).save(any());
    }

    @Test
    void deleteLocationRejectsLocationWithStock() {
        Fixture fixture = new Fixture();
        StorageLocation location = location(7L, warehouse(1L));
        when(fixture.locationRepository.findById(7L)).thenReturn(Optional.of(location));
        when(fixture.stockBalanceRepository.existsByLocation_IdAndActualQuantityGreaterThan(7L, 0)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> fixture.service.deleteLocation(7L, "tester"));

        assertFalse(location.isDeleted());
        verify(fixture.auditService, never()).log(any(), any(), any(), any(), any());
    }

    @Test
    void deleteLocationRejectsParentWithActiveChildren() {
        Fixture fixture = new Fixture();
        StorageLocation location = location(7L, warehouse(1L));
        when(fixture.locationRepository.findById(7L)).thenReturn(Optional.of(location));
        when(fixture.locationRepository.existsByParent_IdAndDeletedFalse(7L)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> fixture.service.deleteLocation(7L, "tester"));

        assertFalse(location.isDeleted());
    }

    @Test
    void deleteWarehouseRejectsWarehouseWithLocations() {
        Fixture fixture = new Fixture();
        Warehouse warehouse = warehouse(1L);
        when(fixture.warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(fixture.locationRepository.existsByWarehouse_IdAndDeletedFalse(1L)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> fixture.service.deleteWarehouse(1L, "tester"));

        assertFalse(warehouse.isDeleted());
    }

    private static BindingResult errors(StorageLocationForm form) {
        return new BeanPropertyBindingResult(form, "locationForm");
    }

    private static StorageLocationForm form(Long id, Long warehouseId, Long parentId) {
        StorageLocationForm form = new StorageLocationForm();
        form.setId(id);
        form.setWarehouseId(warehouseId);
        form.setParentId(parentId);
        form.setCode("A-01");
        form.setName("Kệ A");
        form.setLocationType(LocationType.SHELF);
        return form;
    }

    private static Warehouse warehouse(Long id) {
        Warehouse warehouse = new Warehouse();
        warehouse.setId(id);
        return warehouse;
    }

    private static StorageLocation location(Long id, Warehouse warehouse) {
        StorageLocation location = new StorageLocation();
        location.setId(id);
        location.setWarehouse(warehouse);
        return location;
    }

    private static class Fixture {
        private final WarehouseRepository warehouseRepository = mock(WarehouseRepository.class);
        private final StorageLocationRepository locationRepository = mock(StorageLocationRepository.class);
        private final StockBalanceRepository stockBalanceRepository = mock(StockBalanceRepository.class);
        private final AuditService auditService = mock(AuditService.class);
        private final WarehouseAdminService service =
                new WarehouseAdminService(warehouseRepository, locationRepository, stockBalanceRepository, auditService);
    }
}
