package com.qlvt.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DepartmentForm {
    private Long id;

    @NotBlank(message = "Mã khoa/phòng không được để trống")
    @Size(max = 40, message = "Mã tối đa 40 ký tự")
    private String code;

    @NotBlank(message = "Tên khoa/phòng không được để trống")
    private String name;
    private String description;
    private boolean active = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
