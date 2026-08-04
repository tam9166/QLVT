package com.qlvt;

import com.qlvt.entity.StorageLocation;
import com.qlvt.entity.Warehouse;
import com.qlvt.enums.LocationType;
import com.qlvt.enums.WarehouseType;
import com.qlvt.form.StorageLocationForm;
import com.qlvt.form.WarehouseForm;
import com.qlvt.repository.StorageLocationRepository;
import com.qlvt.repository.StockBalanceRepository;
import com.qlvt.repository.WarehouseRepository;
import com.qlvt.service.AuditService;
import com.qlvt.service.WarehouseAdminService;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class WarehouseAdminServiceTest {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void warehouseFormHandlesUnknownLegacyType() {
        Fixture fixture = new Fixture();
        Warehouse warehouse = warehouse(1L);
        warehouse.setType("LEGACY_WAREHOUSE");

        WarehouseForm form = fixture.service.toForm(warehouse);

        assertNull(form.getType());
    }

    @Test
    void locationFormHandlesUnknownLegacyType() {
        Fixture fixture = new Fixture();
        StorageLocation location = location(7L, warehouse(1L));
        location.setLocationType("LEGACY_LOCATION");

        StorageLocationForm form = fixture.service.toForm(location);

        assertNull(form.getLocationType());
    }

    @Test
    void searchWarehousesTrimsKeywordBeforeQuerying() {
        Fixture fixture = new Fixture();

        fixture.service.searchWarehouses("  kho chinh  ");

        verify(fixture.warehouseRepository)
                .findByDeletedFalseAndCodeContainingIgnoreCaseOrDeletedFalseAndNameContainingIgnoreCase(
                        "kho chinh", "kho chinh");
    }

    @Test
    void searchWarehousesTreatsWhitespaceAsNoFilter() {
        Fixture fixture = new Fixture();

        fixture.service.searchWarehouses("   ");

        verify(fixture.warehouseRepository).findByDeletedFalseOrderByCodeAsc();
        verify(fixture.warehouseRepository, never())
                .findByDeletedFalseAndCodeContainingIgnoreCaseOrDeletedFalseAndNameContainingIgnoreCase(anyString(), anyString());
    }

    @Test
    void searchLocationsCombinesWarehouseAndTrimmedKeywordFilters() {
        Fixture fixture = new Fixture();
        StorageLocation matching = new StorageLocation();
        matching.setCode("KE-A01");
        matching.setName("Kệ thuốc lạnh");
        StorageLocation other = new StorageLocation();
        other.setCode("TU-B02");
        other.setName("Tủ vật tư");
        when(fixture.locationRepository.findByWarehouse_IdAndDeletedFalseOrderByCodeAsc(7L))
                .thenReturn(List.of(matching, other));

        List<StorageLocation> result = fixture.service.locations(7L, "  THUỐC LẠNH  ");

        assertEquals(List.of(matching), result);
        verify(fixture.locationRepository).findByWarehouse_IdAndDeletedFalseOrderByCodeAsc(7L);
        verify(fixture.locationRepository, never()).findByDeletedFalseOrderByCodeAsc();
    }

    @Test
    void searchLocationsTreatsWhitespaceAsNoKeywordFilter() {
        Fixture fixture = new Fixture();
        StorageLocation location = new StorageLocation();
        when(fixture.locationRepository.findByDeletedFalseOrderByCodeAsc()).thenReturn(List.of(location));

        assertEquals(List.of(location), fixture.service.locations(null, "   "));
    }

    @Test
    void searchWarehousesCombinesKeywordAndActiveStatus() {
        Fixture fixture = new Fixture();
        Warehouse active = warehouse(1L);
        Warehouse inactive = warehouse(2L);
        inactive.setActive(false);
        when(fixture.warehouseRepository
                .findByDeletedFalseAndCodeContainingIgnoreCaseOrDeletedFalseAndNameContainingIgnoreCase("kho", "kho"))
                .thenReturn(List.of(active, inactive));

        assertEquals(List.of(inactive), fixture.service.searchWarehouses(" kho ", false));
    }

    @Test
    void searchWarehousesCombinesKeywordStatusAndType() {
        Fixture fixture = new Fixture();
        Warehouse matching = warehouse(1L);
        matching.setType(WarehouseType.QUARANTINE.name());
        Warehouse wrongType = warehouse(2L);
        wrongType.setType(WarehouseType.MAIN.name());
        Warehouse inactive = warehouse(3L);
        inactive.setType(WarehouseType.QUARANTINE.name());
        inactive.setActive(false);
        when(fixture.warehouseRepository
                .findByDeletedFalseAndCodeContainingIgnoreCaseOrDeletedFalseAndNameContainingIgnoreCase("kho", "kho"))
                .thenReturn(List.of(matching, wrongType, inactive));

        assertEquals(List.of(matching),
                fixture.service.searchWarehouses(" kho ", true, WarehouseType.QUARANTINE));
    }

    @Test
    void searchLocationsCombinesWarehouseKeywordAndActiveStatus() {
        Fixture fixture = new Fixture();
        StorageLocation active = new StorageLocation();
        active.setCode("KE-A01");
        active.setName("Kệ lạnh");
        StorageLocation inactive = new StorageLocation();
        inactive.setCode("KE-A02");
        inactive.setName("Kệ lạnh dự phòng");
        inactive.setActive(false);
        when(fixture.locationRepository.findByWarehouse_IdAndDeletedFalseOrderByCodeAsc(7L))
                .thenReturn(List.of(active, inactive));

        assertEquals(List.of(inactive), fixture.service.locations(7L, " kệ ", false));
    }

    @Test
    void searchLocationsCombinesTypeWithExistingFilters() {
        Fixture fixture = new Fixture();
        StorageLocation shelf = new StorageLocation();
        shelf.setCode("KE-A01");
        shelf.setName("Kệ lạnh");
        shelf.setLocationType(LocationType.SHELF.name());
        StorageLocation cabinet = new StorageLocation();
        cabinet.setCode("TU-A01");
        cabinet.setName("Tủ lạnh");
        cabinet.setLocationType(LocationType.CABINET.name());
        when(fixture.locationRepository.findByWarehouse_IdAndDeletedFalseOrderByCodeAsc(7L))
                .thenReturn(List.of(shelf, cabinet));

        assertEquals(List.of(shelf),
                fixture.service.locations(7L, " lạnh ", true, LocationType.SHELF));
    }

    @Test
    void warehouseFormRejectsValuesLongerThanDatabaseColumns() {
        WarehouseForm form = warehouseForm(null, true);
        form.setName("N".repeat(151));
        form.setAddress("A".repeat(301));
        form.setDescription("D".repeat(501));

        var violations = validator.validate(form);

        assertTrue(violations.stream().anyMatch(v -> "name".equals(v.getPropertyPath().toString())));
        assertTrue(violations.stream().anyMatch(v -> "address".equals(v.getPropertyPath().toString())));
        assertTrue(violations.stream().anyMatch(v -> "description".equals(v.getPropertyPath().toString())));
    }

    @Test
    void storageLocationFormRejectsValuesLongerThanDatabaseColumns() {
        StorageLocationForm form = form(null, 1L, null);
        form.setName("N".repeat(151));
        form.setDescription("D".repeat(501));

        var violations = validator.validate(form);

        assertTrue(violations.stream().anyMatch(v -> "name".equals(v.getPropertyPath().toString())));
        assertTrue(violations.stream().anyMatch(v -> "description".equals(v.getPropertyPath().toString())));
    }

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
    void saveWarehouseNormalizesTextBeforeDuplicateCheckAndPersistence() {
        Fixture fixture = new Fixture();
        WarehouseForm form = warehouseForm(null, true);
        form.setCode("  kho-a  ");
        form.setName("  Kho A  ");
        form.setAddress("   ");
        form.setDescription("  Kho tổng  ");
        BindingResult errors = new BeanPropertyBindingResult(form, "warehouseForm");

        assertTrue(fixture.service.saveWarehouse(form, errors, "tester"));

        verify(fixture.warehouseRepository).existsByCodeIgnoreCase("KHO-A");
        verify(fixture.warehouseRepository).save(argThat(warehouse ->
                "KHO-A".equals(warehouse.getCode())
                        && "Kho A".equals(warehouse.getName())
                        && warehouse.getAddress() == null
                        && "Kho tổng".equals(warehouse.getDescription())));
    }

    @Test
    void saveLocationNormalizesTextBeforeDuplicateCheckAndPersistence() {
        Fixture fixture = new Fixture();
        Warehouse warehouse = warehouse(1L);
        when(fixture.warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        StorageLocationForm form = form(null, 1L, null);
        form.setCode("  a-01  ");
        form.setName("  Kệ A  ");
        form.setDescription("   ");
        BindingResult errors = errors(form);

        assertTrue(fixture.service.saveLocation(form, errors, "tester"));

        verify(fixture.locationRepository).existsByWarehouse_IdAndCodeIgnoreCase(1L, "A-01");
        verify(fixture.locationRepository).save(argThat(location ->
                "A-01".equals(location.getCode())
                        && "Kệ A".equals(location.getName())
                        && location.getDescription() == null));
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
        when(fixture.locationRepository.findByDeletedFalseOrderByCodeAsc())
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
    void saveLocationAllowsEditingInItsExistingInactiveWarehouse() {
        Fixture fixture = new Fixture();
        Warehouse warehouse = warehouse(1L);
        warehouse.setActive(false);
        StorageLocation existing = location(7L, warehouse);
        when(fixture.warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(fixture.locationRepository.findById(7L)).thenReturn(Optional.of(existing));
        StorageLocationForm form = form(7L, 1L, null);
        form.setDescription("Cập nhật mô tả");
        BindingResult errors = errors(form);

        assertTrue(fixture.service.saveLocation(form, errors, "tester"));

        assertFalse(errors.hasErrors());
        verify(fixture.locationRepository).save(existing);
    }

    @Test
    void saveLocationRejectsActivationInItsExistingInactiveWarehouse() {
        Fixture fixture = new Fixture();
        Warehouse warehouse = warehouse(1L);
        warehouse.setActive(false);
        StorageLocation existing = location(7L, warehouse);
        existing.setActive(false);
        when(fixture.warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(fixture.locationRepository.findById(7L)).thenReturn(Optional.of(existing));
        StorageLocationForm form = form(7L, 1L, null);
        form.setActive(true);
        BindingResult errors = errors(form);

        assertFalse(fixture.service.saveLocation(form, errors, "tester"));

        assertTrue(errors.hasFieldErrors("warehouseId"));
        verify(fixture.locationRepository, never()).save(any());
    }

    @Test
    void warehouseChoicesKeepSelectedInactiveWarehouseOnly() {
        Fixture fixture = new Fixture();
        Warehouse active = warehouse(1L);
        Warehouse selectedInactive = warehouse(2L);
        selectedInactive.setActive(false);
        Warehouse otherInactive = warehouse(3L);
        otherInactive.setActive(false);
        when(fixture.warehouseRepository.findByDeletedFalseOrderByCodeAsc())
                .thenReturn(List.of(active, selectedInactive, otherInactive));
        StorageLocationForm form = form(7L, 2L, null);

        assertEquals(List.of(active, selectedInactive), fixture.service.warehouseChoices(form));
    }

    @Test
    void parentChoicesKeepSelectedInactiveParentOnly() {
        Fixture fixture = new Fixture();
        Warehouse warehouse = warehouse(1L);
        StorageLocation active = location(8L, warehouse);
        StorageLocation selectedInactive = location(9L, warehouse);
        selectedInactive.setActive(false);
        StorageLocation otherInactive = location(10L, warehouse);
        otherInactive.setActive(false);
        when(fixture.locationRepository.findByDeletedFalseOrderByCodeAsc())
                .thenReturn(List.of(active, selectedInactive, otherInactive));
        StorageLocationForm form = form(7L, 1L, 9L);

        assertEquals(List.of(active, selectedInactive), fixture.service.parentLocationChoices(form));
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
    void saveLocationAllowsKeepingExistingInactiveParent() {
        Fixture fixture = new Fixture();
        Warehouse warehouse = warehouse(1L);
        StorageLocation parent = location(9L, warehouse);
        parent.setActive(false);
        StorageLocation existing = location(7L, warehouse);
        existing.setParent(parent);
        when(fixture.warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(fixture.locationRepository.findById(7L)).thenReturn(Optional.of(existing));
        when(fixture.locationRepository.findById(9L)).thenReturn(Optional.of(parent));
        StorageLocationForm form = form(7L, 1L, 9L);
        form.setDescription("Cập nhật mô tả");
        BindingResult errors = errors(form);

        assertTrue(fixture.service.saveLocation(form, errors, "tester"));

        assertFalse(errors.hasErrors());
        assertEquals(parent, existing.getParent());
        verify(fixture.locationRepository).save(existing);
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
        when(fixture.locationRepository.existsByWarehouse_IdAndDeletedFalseAndActiveTrue(1L)).thenReturn(true);
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
    void saveWarehouseAllowsDeactivationWhenAllLocationsAreInactive() {
        Fixture fixture = new Fixture();
        WarehouseForm form = warehouseForm(1L, false);
        when(fixture.warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse(1L)));
        when(fixture.locationRepository.existsByWarehouse_IdAndDeletedFalseAndActiveTrue(1L)).thenReturn(false);
        BindingResult errors = new BeanPropertyBindingResult(form, "warehouseForm");

        assertTrue(fixture.service.saveWarehouse(form, errors, "tester"));

        assertFalse(errors.hasErrors());
        verify(fixture.warehouseRepository).save(any(Warehouse.class));
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
        when(fixture.locationRepository.existsByParent_IdAndDeletedFalseAndActiveTrue(7L)).thenReturn(true);
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
    void saveLocationRejectsDeactivationWhileActiveGrandchildRemains() {
        Fixture fixture = new Fixture();
        Warehouse warehouse = warehouse(1L);
        StorageLocation existing = location(7L, warehouse);
        StorageLocation inactiveChild = location(8L, warehouse);
        inactiveChild.setActive(false);
        inactiveChild.setParent(existing);
        StorageLocation activeGrandchild = location(9L, warehouse);
        activeGrandchild.setParent(inactiveChild);
        when(fixture.warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(fixture.locationRepository.findById(7L)).thenReturn(Optional.of(existing));
        when(fixture.locationRepository.findByDeletedFalseAndActiveTrueOrderByCodeAsc())
                .thenReturn(List.of(activeGrandchild));
        StorageLocationForm form = form(7L, 1L, null);
        form.setActive(false);
        BindingResult errors = errors(form);

        assertFalse(fixture.service.saveLocation(form, errors, "tester"));

        assertTrue(errors.hasFieldErrors("active"));
        verify(fixture.locationRepository, never()).save(any());
    }

    @Test
    void saveLocationAllowsDeactivationWhenAllChildrenAreInactive() {
        Fixture fixture = new Fixture();
        Warehouse warehouse = warehouse(1L);
        StorageLocation existing = location(7L, warehouse);
        when(fixture.warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(fixture.locationRepository.findById(7L)).thenReturn(Optional.of(existing));
        when(fixture.locationRepository.existsByParent_IdAndDeletedFalseAndActiveTrue(7L)).thenReturn(false);
        StorageLocationForm form = form(7L, 1L, null);
        form.setActive(false);
        BindingResult errors = errors(form);

        assertTrue(fixture.service.saveLocation(form, errors, "tester"));

        assertFalse(errors.hasErrors());
        verify(fixture.locationRepository).save(existing);
    }

    @Test
    void saveLocationRejectsActivationUnderInactiveParent() {
        Fixture fixture = new Fixture();
        Warehouse warehouse = warehouse(1L);
        StorageLocation parent = location(6L, warehouse);
        parent.setActive(false);
        StorageLocation inactive = location(7L, warehouse);
        inactive.setActive(false);
        inactive.setParent(parent);
        when(fixture.warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(fixture.locationRepository.findById(7L)).thenReturn(Optional.of(inactive));
        when(fixture.locationRepository.findById(6L)).thenReturn(Optional.of(parent));
        StorageLocationForm form = form(7L, 1L, 6L);
        form.setActive(true);
        BindingResult errors = errors(form);

        assertFalse(fixture.service.saveLocation(form, errors, "tester"));

        assertTrue(errors.hasFieldErrors("parentId"));
        verify(fixture.locationRepository, never()).save(any());
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
