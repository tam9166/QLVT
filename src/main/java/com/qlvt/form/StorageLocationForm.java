package com.qlvt.form;

import com.qlvt.enums.LocationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class StorageLocationForm {
    private Long id;

    @NotNull(message = "Kho là bắt buộc")
    private Long warehouseId;
    private Long parentId;

    @NotBlank(message = "Mã vị trí không được để trống")
    @Size(max = 50, message = "Mã vị trí tối đa 50 ký tự")
    private String code;

    @NotBlank(message = "Tên vị trí không được để trống")
    @Size(max = 150, message = "Tên vị trí tối đa 150 ký tự")
    private String name;

    @NotNull(message = "Loại vị trí là bắt buộc")
    private LocationType locationType;
    @Size(max = 500, message = "Mô tả tối đa 500 ký tự")
    private String description;
    private boolean active = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocationType getLocationType() { return locationType; }
    public void setLocationType(LocationType locationType) { this.locationType = locationType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
