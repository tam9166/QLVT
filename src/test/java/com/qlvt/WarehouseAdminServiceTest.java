package com.qlvt;

import com.qlvt.entity.StorageLocation;
import com.qlvt.entity.Warehouse;
import com.qlvt.enums.LocationType;
import com.qlvt.form.StorageLocationForm;
import com.qlvt.form.WarehouseForm;
import com.qlvt.repository.StorageLocationRepository;
import com.qlvt.repository.StockBalanceRepository;
import com.qlvt.repository.WarehouseRepository;
import com.qlvt.service.AuditService;
import com.qlvt.service.WarehouseAdminService;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class WarehouseAdminServiceTest {
    @Test
    void storageLocationCodeIsUniqueWithinWarehouseOnly() throws NoSuchFieldException {
        Column codeColumn = StorageLocation.class.getDeclaredField("code").getAnnotation(Column.class);
        Table table = StorageLocation.class.getAnnotation(Table.class);

        assertFalse(codeColumn.unique());
        assertTrue(java.util.Arrays.stream(table.indexes())
                .anyMatch(index -> index.unique()
                        && "warehouse_id,code".equals(index.columnList())));
    }


    @Test
    void parentLocationChoicesExcludeOtherWarehousesSelfAndDescendants() {
        Fixture fixture = new Fixture();
        Warehouse selectedWarehouse = warehouse(1L);
        StorageLocation edited = location(7L, selectedWarehouse);
        StorageLocation validParent = location(8L, selectedWarehouse);
        StorageLocation child = location(9L, selectedWarehouse);
        child.setParent(edited);
        StorageLocation otherWarehouse = location(10L, warehouse(2L));
        when(fixture.locationRepository.findByDeletedFalseAndActiveTrueOrderByCodeAsc())
                .thenReturn(List.of(edited, validParent, child, otherWarehouse));
        StorageLocationForm form = form(7L, 1L, null);

        List<StorageLocation> choices = fixture.service.parentLocationChoices(form);

        assertEquals(List.of(validParent), choices);
    }

    @Test
    void saveLocationRejectsParentFromAnotherWarehouse() {
        Fixture fixture = new Fixture();
        when(fixture.warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse(1L)));
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
        when(fixture.warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
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
    void saveLocationRejectsInactiveWarehouse() {
        Fixture fixture = new Fixture();
        Warehouse warehouse = warehouse(1L);
        warehouse.setActive(false);
        when(fixture.warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        StorageLocationForm form = form(null, 1L, null);
        BindingResult errors = errors(form);

        assertFalse(fixture.service.saveLocation(form, errors, "tester"));

        assertTrue(errors.hasFieldErrors("warehouseId"));
        verify(fixture.locationRepository, never()).save(any());
    }

    @Test
    void saveLocationRejectsInactiveParent() {
        Fixture fixture = new Fixture();
        Warehouse warehouse = warehouse(1L);
        when(fixture.warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        StorageLocation parent = location(9L, warehouse);
        parent.setActive(false);
        when(fixture.locationRepository.findById(9L)).thenReturn(Optional.of(parent));
        StorageLocationForm form = form(null, 1L, 9L);
        BindingResult errors = errors(form);

        assertFalse(fixture.service.saveLocation(form, errors, "tester"));

        assertTrue(errors.hasFieldErrors("parentId"));
        verify(fixture.locationRepository, never()).save(any());
    }

    @Test
    void saveWarehouseRejectsDeactivationWhileStockRemains() {
        Fixture fixture = new Fixture();
        WarehouseForm form = warehouseForm(1L, false);
        when(fixture.warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse(1L)));
        when(fixture.stockBalanceRepository.existsByWarehouse_IdAndActualQuantityGreaterThan(1L, 0)).thenReturn(true);
        BindingResult errors = new BeanPropertyBindingResult(form, "warehouseForm");

        assertFalse(fixture.service.saveWarehouse(form, errors, "tester"));

        assertTrue(errors.hasFieldErrors("active"));
        verify(fixture.warehouseRepository, never()).save(any());
    }

    @Test
    void saveWarehouseRejectsEditingDeletedWarehouse() {
        Fixture fixture = new Fixture();
        Warehouse deleted = warehouse(1L);
        deleted.setDeleted(true);
        when(fixture.warehouseRepository.findById(1L)).thenReturn(Optional.of(deleted));
        WarehouseForm form = warehouseForm(1L, true);
        BindingResult errors = new BeanPropertyBindingResult(form, "warehouseForm");

        assertFalse(fixture.service.saveWarehouse(form, errors, "tester"));

        assertTrue(errors.hasGlobalErrors());
        verify(fixture.warehouseRepository, never()).save(any());
    }

    @Test
    void saveWarehouseRejectsDeactivationWhileLocationsRemain() {
        Fixture fixture = new Fixture();
        WarehouseForm form = warehouseForm(1L, false);
        when(fixture.warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse(1L)));
        when(fixture.locationRepository.existsByWarehouse_IdAndDeletedFalse(1L)).thenReturn(true);
        BindingResult errors = new BeanPropertyBindingResult(form, "warehouseForm");

        assertFalse(fixture.service.saveWarehouse(form, errors, "tester"));

        assertTrue(errors.hasFieldErrors("active"));
        verify(fixture.warehouseRepository, never()).save(any());
    }

    @Test
    void saveWarehouseAllowsEditingAlreadyInactiveWarehouse() {
        Fixture fixture = new Fixture();
        Warehouse inactive = warehouse(1L);
        inactive.setActive(false);
        when(fixture.warehouseRepository.findById(1L)).thenReturn(Optional.of(inactive));
        when(fixture.locationRepository.existsByWarehouse_IdAndDeletedFalse(1L)).thenReturn(true);
        WarehouseForm form = warehouseForm(1L, false);
        form.setDescription("Cập nhật mô tả");
        BindingResult errors = new BeanPropertyBindingResult(form, "warehouseForm");

        assertTrue(fixture.service.saveWarehouse(form, errors, "tester"));

        assertFalse(errors.hasErrors());
        verify(fixture.warehouseRepository).save(inactive);
        verify(fixture.locationRepository, never()).existsByWarehouse_IdAndDeletedFalse(1L);
    }

    @Test
    void saveLocationRejectsDeactivationWhileStockRemains() {
        Fixture fixture = new Fixture();
        Warehouse warehouse = warehouse(1L);
        StorageLocation existing = location(7L, warehouse);
        when(fixture.warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(fixture.locationRepository.findById(7L)).thenReturn(Optional.of(existing));
        StorageLocationForm form = form(7L, 1L, null);
        form.setActive(false);
        when(fixture.stockBalanceRepository.existsByLocation_IdAndActualQuantityGreaterThan(7L, 0)).thenReturn(true);
        BindingResult errors = errors(form);

        assertFalse(fixture.service.saveLocation(form, errors, "tester"));

        assertTrue(errors.hasFieldErrors("active"));
        verify(fixture.locationRepository, never()).save(any());
    }

    @Test
    void saveLocationRejectsEditingDeletedLocation() {
        Fixture fixture = new Fixture();
        Warehouse warehouse = warehouse(1L);
        StorageLocation deleted = location(7L, warehouse);
        deleted.setDeleted(true);
        when(fixture.warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(fixture.locationRepository.findById(7L)).thenReturn(Optional.of(deleted));
        StorageLocationForm form = form(7L, 1L, null);
        BindingResult errors = errors(form);

        assertFalse(fixture.service.saveLocation(form, errors, "tester"));

        assertTrue(errors.hasGlobalErrors());
        verify(fixture.locationRepository, never()).save(any());
    }

    @Test
    void saveLocationRejectsDeactivationWhileChildrenRemain() {
        Fixture fixture = new Fixture();
        Warehouse warehouse = warehouse(1L);
        StorageLocation existing = location(7L, warehouse);
        when(fixture.warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(fixture.locationRepository.findById(7L)).thenReturn(Optional.of(existing));
        StorageLocationForm form = form(7L, 1L, null);
        form.setActive(false);
        when(fixture.locationRepository.existsByParent_IdAndDeletedFalse(7L)).thenReturn(true);
        BindingResult errors = errors(form);

        assertFalse(fixture.service.saveLocation(form, errors, "tester"));

        assertTrue(errors.hasFieldErrors("active"));
        verify(fixture.locationRepository, never()).save(any());
    }

    @Test
    void saveLocationAllowsEditingAlreadyInactiveLocation() {
        Fixture fixture = new Fixture();
        Warehouse warehouse = warehouse(1L);
        StorageLocation inactive = location(7L, warehouse);
        inactive.setActive(false);
        when(fixture.warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(fixture.locationRepository.findById(7L)).thenReturn(Optional.of(inactive));
        when(fixture.locationRepository.existsByParent_IdAndDeletedFalse(7L)).thenReturn(true);
        StorageLocationForm form = form(7L, 1L, null);
        form.setActive(false);
        form.setDescription("Cập nhật mô tả");
        BindingResult errors = errors(form);

        assertTrue(fixture.service.saveLocation(form, errors, "tester"));

        assertFalse(errors.hasErrors());
        verify(fixture.locationRepository).save(inactive);
        verify(fixture.locationRepository, never()).existsByParent_IdAndDeletedFalse(7L);
    }

    @Test
    void saveLocationRejectsWarehouseChangeWhileStockRemains() {
        Fixture fixture = new Fixture();
        Warehouse source = warehouse(1L);
        Warehouse destination = warehouse(2L);
        StorageLocation existing = location(7L, source);
        when(fixture.warehouseRepository.findById(2L)).thenReturn(Optional.of(destination));
        when(fixture.locationRepository.findById(7L)).thenReturn(Optional.of(existing));
        when(fixture.stockBalanceRepository.existsByLocation_IdAndActualQuantityGreaterThan(7L, 0)).thenReturn(true);
        StorageLocationForm form = form(7L, 2L, null);
        BindingResult errors = errors(form);

        assertFalse(fixture.service.saveLocation(form, errors, "tester"));

        assertTrue(errors.hasFieldErrors("warehouseId"));
        verify(fixture.locationRepository, never()).save(any());
    }

    @Test
    void saveLocationRejectsWarehouseChangeWhileChildrenRemain() {
        Fixture fixture = new Fixture();
        Warehouse source = warehouse(1L);
        Warehouse destination = warehouse(2L);
        StorageLocation existing = location(7L, source);
        when(fixture.warehouseRepository.findById(2L)).thenReturn(Optional.of(destination));
        when(fixture.locationRepository.findById(7L)).thenReturn(Optional.of(existing));
        when(fixture.locationRepository.existsByParent_IdAndDeletedFalse(7L)).thenReturn(true);
        StorageLocationForm form = form(7L, 2L, null);
        BindingResult errors = errors(form);

        assertFalse(fixture.service.saveLocation(form, errors, "tester"));

        assertTrue(errors.hasFieldErrors("warehouseId"));
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

    @Test
    void deleteWarehouseRejectsAlreadyDeletedWarehouse() {
        Fixture fixture = new Fixture();
        Warehouse warehouse = warehouse(1L);
        warehouse.setDeleted(true);
        when(fixture.warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));

        assertThrows(IllegalStateException.class, () -> fixture.service.deleteWarehouse(1L, "tester"));

        verify(fixture.stockBalanceRepository, never())
                .existsByWarehouse_IdAndActualQuantityGreaterThan(anyLong(), anyInt());
        verify(fixture.auditService, never()).log(any(), any(), any(), any(), any());
    }

    @Test
    void deleteLocationRejectsAlreadyDeletedLocation() {
        Fixture fixture = new Fixture();
        StorageLocation location = location(7L, warehouse(1L));
        location.setDeleted(true);
        when(fixture.locationRepository.findById(7L)).thenReturn(Optional.of(location));

        assertThrows(IllegalStateException.class, () -> fixture.service.deleteLocation(7L, "tester"));

        verify(fixture.stockBalanceRepository, never())
                .existsByLocation_IdAndActualQuantityGreaterThan(anyLong(), anyInt());
        verify(fixture.auditService, never()).log(any(), any(), any(), any(), any());
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

    private static WarehouseForm warehouseForm(Long id, boolean active) {
        WarehouseForm form = new WarehouseForm();
        form.setId(id);
        form.setCode("KHO-01");
        form.setName("Kho chính");
        form.setActive(active);
        form.setType(com.qlvt.enums.WarehouseType.MAIN);
        return form;
    }

    private static Warehouse warehouse(Long id) {
        Warehouse warehouse = new Warehouse();
        warehouse.setId(id);
        warehouse.setActive(true);
        return warehouse;
    }

    private static StorageLocation location(Long id, Warehouse warehouse) {
        StorageLocation location = new StorageLocation();
        location.setId(id);
        location.setWarehouse(warehouse);
        location.setActive(true);
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
