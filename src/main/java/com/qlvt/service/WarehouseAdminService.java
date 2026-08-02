package com.qlvt.service;

import com.qlvt.entity.StorageLocation;
import com.qlvt.entity.Warehouse;
import com.qlvt.exception.ResourceNotFoundException;
import com.qlvt.enums.LocationType;
import com.qlvt.enums.WarehouseType;
import com.qlvt.form.StorageLocationForm;
import com.qlvt.form.WarehouseForm;
import com.qlvt.repository.StorageLocationRepository;
import com.qlvt.repository.StockBalanceRepository;
import com.qlvt.repository.WarehouseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class WarehouseAdminService {
    private final WarehouseRepository warehouseRepository;
    private final StorageLocationRepository locationRepository;
    private final StockBalanceRepository stockBalanceRepository;
    private final AuditService auditService;

    public WarehouseAdminService(WarehouseRepository warehouseRepository,
                                 StorageLocationRepository locationRepository,
                                 StockBalanceRepository stockBalanceRepository,
                                 AuditService auditService) {
        this.warehouseRepository = warehouseRepository;
        this.locationRepository = locationRepository;
        this.stockBalanceRepository = stockBalanceRepository;
        this.auditService = auditService;
    }

    public List<Warehouse> searchWarehouses(String q) {
        return searchWarehouses(q, null);
    }

    public List<Warehouse> searchWarehouses(String q, Boolean active) {
        List<Warehouse> warehouses;
        if (q == null || q.isBlank()) {
            warehouses = warehouseRepository.findByDeletedFalseOrderByCodeAsc();
        } else {
            String keyword = q.trim();
            warehouses = warehouseRepository.findByDeletedFalseAndCodeContainingIgnoreCaseOrDeletedFalseAndNameContainingIgnoreCase(
                    keyword, keyword);
        }
        return active == null ? warehouses : warehouses.stream()
                .filter(warehouse -> warehouse.isActive() == active)
                .toList();
    }

    public List<StorageLocation> locations(Long warehouseId, String q) {
        return locations(warehouseId, q, null);
    }

    public List<StorageLocation> locations(Long warehouseId, String q, Boolean active) {
        List<StorageLocation> locations = warehouseId == null
                ? locationRepository.findByDeletedFalseOrderByCodeAsc()
                : locationRepository.findByWarehouse_IdAndDeletedFalseOrderByCodeAsc(warehouseId);
        return locations.stream()
                .filter(location -> active == null || location.isActive() == active)
                .filter(location -> matchesKeyword(location, q))
                .toList();
    }

    private boolean matchesKeyword(StorageLocation location, String q) {
        if (q == null || q.isBlank()) {
            return true;
        }
        String keyword = q.trim().toLowerCase(Locale.ROOT);
        return containsIgnoreCase(location.getCode(), keyword)
                || containsIgnoreCase(location.getName(), keyword);
    }

    private boolean containsIgnoreCase(String value, String normalizedKeyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(normalizedKeyword);
    }

    public List<StorageLocation> parentLocationChoices(StorageLocationForm form) {
        return locationRepository.findByDeletedFalseAndActiveTrueOrderByCodeAsc().stream()
                .filter(candidate -> form.getWarehouseId() == null
                        || (candidate.getWarehouse() != null
                        && form.getWarehouseId().equals(candidate.getWarehouse().getId())))
                .filter(candidate -> !isSelfOrDescendant(candidate, form.getId()))
                .toList();
    }

    public List<Warehouse> warehouseChoices(StorageLocationForm form) {
        return warehouseRepository.findByDeletedFalseOrderByCodeAsc().stream()
                .filter(warehouse -> warehouse.isActive()
                        || (form != null && warehouse.getId().equals(form.getWarehouseId())))
                .toList();
    }

    private boolean isSelfOrDescendant(StorageLocation candidate, Long editedLocationId) {
        if (editedLocationId == null) {
            return false;
        }
        Set<Long> visited = new HashSet<>();
        for (StorageLocation current = candidate; current != null; current = current.getParent()) {
            if (editedLocationId.equals(current.getId())) {
                return true;
            }
            if (current.getId() != null && !visited.add(current.getId())) {
                return true;
            }
        }
        return false;
    }

    public WarehouseForm toForm(Warehouse warehouse) {
        WarehouseForm form = new WarehouseForm();
        form.setId(warehouse.getId());
        form.setCode(warehouse.getCode());
        form.setName(warehouse.getName());
        form.setAddress(warehouse.getAddress());
        form.setDescription(warehouse.getDescription());
        form.setActive(warehouse.isActive());
        form.setType(warehouse.getType() == null ? null : WarehouseType.valueOf(warehouse.getType()));
        return form;
    }

    public StorageLocationForm toForm(StorageLocation location) {
        StorageLocationForm form = new StorageLocationForm();
        form.setId(location.getId());
        form.setWarehouseId(location.getWarehouse().getId());
        form.setParentId(location.getParent() == null ? null : location.getParent().getId());
        form.setCode(location.getCode());
        form.setName(location.getName());
        form.setDescription(location.getDescription());
        form.setActive(location.isActive());
        form.setLocationType(location.getLocationType() == null ? null : LocationType.valueOf(location.getLocationType()));
        return form;
    }

    @Transactional
    public boolean saveWarehouse(WarehouseForm form, BindingResult bindingResult, String actor) {
        normalize(form);
        Long id = form.getId() == null ? -1L : form.getId();
        Warehouse existing = form.getId() == null
                ? null
                : warehouseRepository.findById(form.getId()).orElse(null);
        if (form.getId() != null && (existing == null || existing.isDeleted())) {
            bindingResult.reject("invalidWarehouse", "Kho không tồn tại hoặc đã bị xóa");
        }
        if ((form.getId() == null && warehouseRepository.existsByCodeIgnoreCase(form.getCode()))
                || (form.getId() != null && warehouseRepository.existsByCodeIgnoreCaseAndIdNot(form.getCode(), id))) {
            bindingResult.rejectValue("code", "duplicate", "Mã kho đã tồn tại");
        }
        if (existing != null && existing.isActive() && !form.isActive()) {
            if (stockBalanceRepository.existsByWarehouse_IdAndActualQuantityGreaterThan(form.getId(), 0)) {
                bindingResult.rejectValue("active", "inUse", "Không thể ngừng hoạt động kho đang còn tồn kho");
            } else if (locationRepository.existsByWarehouse_IdAndDeletedFalse(form.getId())) {
                bindingResult.rejectValue("active", "hasLocations", "Phải ngừng hoạt động hoặc xóa các vị trí trong kho trước");
            }
        }
        if (bindingResult.hasErrors()) {
            return false;
        }
        Warehouse warehouse = form.getId() == null ? new Warehouse() : existing;
        warehouse.setCode(form.getCode());
        warehouse.setName(form.getName());
        warehouse.setType(form.getType().name());
        warehouse.setAddress(form.getAddress());
        warehouse.setDescription(form.getDescription());
        warehouse.setActive(form.isActive());
        warehouse.setUpdatedAt(LocalDateTime.now());
        warehouseRepository.save(warehouse);
        auditService.log(actor, form.getId() == null ? "CREATE_WAREHOUSE" : "UPDATE_WAREHOUSE", "Warehouse", warehouse.getCode(), "Lưu kho " + warehouse.getName());
        return true;
    }

    @Transactional
    public boolean saveLocation(StorageLocationForm form, BindingResult bindingResult, String actor) {
        normalize(form);
        Long id = form.getId() == null ? -1L : form.getId();
        StorageLocation existing = form.getId() == null
                ? null
                : locationRepository.findById(form.getId()).orElse(null);
        if (form.getId() != null && (existing == null || existing.isDeleted())) {
            bindingResult.reject("invalidLocation", "Vị trí không tồn tại hoặc đã bị xóa");
        }
        Warehouse warehouse = form.getWarehouseId() == null
                ? null
                : warehouseRepository.findById(form.getWarehouseId()).orElse(null);
        boolean keepsExistingWarehouse = existing != null
                && existing.getWarehouse() != null
                && existing.getWarehouse().getId().equals(form.getWarehouseId());
        if (warehouse == null || warehouse.isDeleted() || (!warehouse.isActive() && !keepsExistingWarehouse)) {
            bindingResult.rejectValue("warehouseId", "invalid", "Kho không tồn tại hoặc không hoạt động");
        }
        if (form.getWarehouseId() != null && ((form.getId() == null && locationRepository.existsByWarehouse_IdAndCodeIgnoreCase(form.getWarehouseId(), form.getCode()))
                || (form.getId() != null && locationRepository.existsByWarehouse_IdAndCodeIgnoreCaseAndIdNot(form.getWarehouseId(), form.getCode(), id)))) {
            bindingResult.rejectValue("code", "duplicate", "Mã vị trí đã tồn tại trong kho này");
        }
        StorageLocation parent = validateParent(form, bindingResult);
        validateWarehouseChange(form, existing, bindingResult);
        if (existing != null && existing.isActive() && !form.isActive()) {
            if (stockBalanceRepository.existsByLocation_IdAndActualQuantityGreaterThan(form.getId(), 0)) {
                bindingResult.rejectValue("active", "inUse", "Không thể ngừng hoạt động vị trí đang còn tồn kho");
            } else if (locationRepository.existsByParent_IdAndDeletedFalse(form.getId())) {
                bindingResult.rejectValue("active", "hasChildren", "Phải ngừng hoạt động hoặc xóa các vị trí con trước");
            }
        }
        if (bindingResult.hasErrors()) {
            return false;
        }
        StorageLocation location = form.getId() == null ? new StorageLocation() : existing;
        location.setWarehouse(warehouse);
        location.setParent(parent);
        location.setCode(form.getCode());
        location.setName(form.getName());
        location.setLocationType(form.getLocationType().name());
        location.setDescription(form.getDescription());
        location.setActive(form.isActive());
        location.setUpdatedAt(LocalDateTime.now());
        locationRepository.save(location);
        auditService.log(actor, form.getId() == null ? "CREATE_LOCATION" : "UPDATE_LOCATION", "StorageLocation", location.getCode(), "Lưu vị trí " + location.getName());
        return true;
    }

    private void validateWarehouseChange(StorageLocationForm form, StorageLocation existing,
                                         BindingResult bindingResult) {
        if (form.getId() == null || form.getWarehouseId() == null || bindingResult.hasFieldErrors("warehouseId")) {
            return;
        }
        if (existing == null || existing.getWarehouse() == null
                || existing.getWarehouse().getId().equals(form.getWarehouseId())) {
            return;
        }
        if (stockBalanceRepository.existsByLocation_IdAndActualQuantityGreaterThan(form.getId(), 0)) {
            bindingResult.rejectValue("warehouseId", "inUse",
                    "Không thể chuyển vị trí sang kho khác khi vẫn còn tồn kho");
        } else if (locationRepository.existsByParent_IdAndDeletedFalse(form.getId())) {
            bindingResult.rejectValue("warehouseId", "hasChildren",
                    "Không thể chuyển vị trí cha sang kho khác khi vẫn còn vị trí con");
        }
    }

    private StorageLocation validateParent(StorageLocationForm form, BindingResult bindingResult) {
        if (form.getParentId() == null) {
            return null;
        }
        StorageLocation parent = locationRepository.findById(form.getParentId()).orElse(null);
        if (parent == null || parent.isDeleted() || !parent.isActive()) {
            bindingResult.rejectValue("parentId", "invalid", "Vị trí cha không tồn tại hoặc đã bị xóa");
            return null;
        }
        if (parent.getWarehouse() == null || !parent.getWarehouse().getId().equals(form.getWarehouseId())) {
            bindingResult.rejectValue("parentId", "warehouseMismatch", "Vị trí cha phải thuộc cùng kho");
        }
        Long locationId = form.getId();
        Set<Long> visited = new HashSet<>();
        for (StorageLocation current = parent; current != null; current = current.getParent()) {
            if (locationId != null && locationId.equals(current.getId())) {
                bindingResult.rejectValue("parentId", "cycle", "Vị trí cha tạo thành vòng lặp phân cấp");
                break;
            }
            if (current.getId() != null && !visited.add(current.getId())) {
                bindingResult.rejectValue("parentId", "cycle", "Cây vị trí cha đang có vòng lặp không hợp lệ");
                break;
            }
        }
        return parent;
    }

    @Transactional
    public void deleteWarehouse(Long id, String actor) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kho không tồn tại"));
        if (warehouse.isDeleted()) {
            throw new IllegalStateException("Kho đã bị xóa");
        }
        if (stockBalanceRepository.existsByWarehouse_IdAndActualQuantityGreaterThan(id, 0)) {
            throw new IllegalStateException("Không thể xóa kho đang còn tồn kho");
        }
        if (locationRepository.existsByWarehouse_IdAndDeletedFalse(id)) {
            throw new IllegalStateException("Phải xóa các vị trí trong kho trước khi xóa kho");
        }
        warehouse.setDeleted(true);
        warehouse.setActive(false);
        warehouse.setUpdatedAt(LocalDateTime.now());
        auditService.log(actor, "DELETE_WAREHOUSE", "Warehouse", warehouse.getCode(), "Xóa mềm kho");
    }

    @Transactional
    public void deleteLocation(Long id, String actor) {
        StorageLocation location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vị trí không tồn tại"));
        if (location.isDeleted()) {
            throw new IllegalStateException("Vị trí đã bị xóa");
        }
        if (stockBalanceRepository.existsByLocation_IdAndActualQuantityGreaterThan(id, 0)) {
            throw new IllegalStateException("Không thể xóa vị trí đang còn tồn kho");
        }
        if (locationRepository.existsByParent_IdAndDeletedFalse(id)) {
            throw new IllegalStateException("Phải xóa các vị trí con trước khi xóa vị trí cha");
        }
        location.setDeleted(true);
        location.setActive(false);
        location.setUpdatedAt(LocalDateTime.now());
        auditService.log(actor, "DELETE_LOCATION", "StorageLocation", location.getCode(), "Xóa mềm vị trí");
    }

    public WarehouseType[] warehouseTypes() { return WarehouseType.values(); }
    public LocationType[] locationTypes() { return LocationType.values(); }

    private void normalize(WarehouseForm form) {
        form.setCode(normalizeCode(form.getCode()));
        form.setName(trim(form.getName()));
        form.setAddress(trimToNull(form.getAddress()));
        form.setDescription(trimToNull(form.getDescription()));
    }

    private void normalize(StorageLocationForm form) {
        form.setCode(normalizeCode(form.getCode()));
        form.setName(trim(form.getName()));
        form.setDescription(trimToNull(form.getDescription()));
    }

    private String normalizeCode(String value) {
        String trimmed = trim(value);
        return trimmed == null ? null : trimmed.toUpperCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        String trimmed = trim(value);
        return trimmed == null || trimmed.isEmpty() ? null : trimmed;
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
