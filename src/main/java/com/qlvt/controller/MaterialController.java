package com.qlvt.controller;

import com.qlvt.entity.Material;
import com.qlvt.exception.ResourceNotFoundException;
import com.qlvt.repository.MaterialRepository;
import com.qlvt.service.AuditService;
import com.qlvt.service.InventoryAlertService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/materials")
public class MaterialController {
    private final MaterialRepository materialRepository;
    private final InventoryAlertService alertService;
    private final AuditService auditService;

    public MaterialController(MaterialRepository materialRepository, InventoryAlertService alertService, AuditService auditService) {
        this.materialRepository = materialRepository;
        this.alertService = alertService;
        this.auditService = auditService;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String q, Model model) {
        model.addAttribute("materials", q.isBlank()
                ? materialRepository.findByDeletedFalseOrderByCodeAsc()
                : materialRepository.findByDeletedFalseAndNameContainingIgnoreCaseOrDeletedFalseAndCodeContainingIgnoreCase(q, q));
        model.addAttribute("q", q);
        model.addAttribute("alertService", alertService);
        return "materials/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_STAFF')")
    public String createForm(Model model) {
        model.addAttribute("material", new Material());
        return "materials/form";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_STAFF')")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("material", materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vật tư cần sửa.")));
        return "materials/form";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_STAFF')")
    public String save(@Valid @ModelAttribute Material material, BindingResult bindingResult, Authentication authentication) {
        if (bindingResult.hasErrors()) {
            return "materials/form";
        }
        boolean creating = material.getId() == null;
        Material target = creating ? new Material() : materialRepository.findById(material.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vật tư cần lưu."));
        copyEditableFields(material, target);
        target.setUpdatedAt(LocalDateTime.now());
        materialRepository.save(target);
        auditService.log(actor(authentication), creating ? "CREATE_MATERIAL" : "UPDATE_MATERIAL",
                "MATERIAL", target.getCode(), creating ? "Tạo vật tư mới" : "Cập nhật thông tin vật tư");
        return "redirect:/materials";
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable Long id, Authentication authentication) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vật tư cần xóa."));
        material.setDeleted(true);
        material.setUpdatedAt(LocalDateTime.now());
        materialRepository.save(material);
        auditService.log(actor(authentication), "DELETE_MATERIAL", "MATERIAL", material.getCode(), "Xóa mềm vật tư");
        return "redirect:/materials";
    }

    private void copyEditableFields(Material source, Material target) {
        target.setCode(source.getCode());
        target.setName(source.getName());
        target.setAliasText(source.getAliasText());
        target.setCategory(source.getCategory());
        target.setUnit(source.getUnit());
        target.setPackageSpec(source.getPackageSpec());
        target.setMinStock(source.getMinStock());
        target.setMaxStock(source.getMaxStock());
        target.setEstimatedUnitPrice(source.getEstimatedUnitPrice() == null ? BigDecimal.ZERO : source.getEstimatedUnitPrice());
        target.setStatus(source.getStatus());
        target.setStorageCondition(source.getStorageCondition());
        target.setSpecialControl(source.isSpecialControl());
    }

    private String actor(Authentication authentication) {
        return authentication == null ? "system" : authentication.getName();
    }
}
