package com.qlvt.form;

import com.qlvt.enums.WarehouseType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class WarehouseForm {
    private Long id;

    @NotBlank(message = "Mã kho không được để trống")
    @Size(max = 40, message = "Mã kho tối đa 40 ký tự")
    private String code;

    @NotBlank(message = "Tên kho không được để trống")
    private String name;

    @NotNull(message = "Loại kho là bắt buộc")
    private WarehouseType type;
    private String address;
    private String description;
    private boolean active = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public WarehouseType getType() { return type; }
    public void setType(WarehouseType type) { this.type = type; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
