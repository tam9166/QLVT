package com.qlvt.controller;

import com.qlvt.form.StorageLocationForm;
import com.qlvt.form.WarehouseForm;
import com.qlvt.exception.ResourceNotFoundException;
import com.qlvt.repository.StorageLocationRepository;
import com.qlvt.repository.WarehouseRepository;
import com.qlvt.service.WarehouseAdminService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class WarehouseController {
    private final WarehouseAdminService warehouseAdminService;
    private final WarehouseRepository warehouseRepository;
    private final StorageLocationRepository locationRepository;

    public WarehouseController(WarehouseAdminService warehouseAdminService, WarehouseRepository warehouseRepository, StorageLocationRepository locationRepository) {
        this.warehouseAdminService = warehouseAdminService;
        this.warehouseRepository = warehouseRepository;
        this.locationRepository = locationRepository;
    }

    @GetMapping("/warehouses")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_STAFF')")
    public String warehouses(@RequestParam(defaultValue = "") String q, Model model) {
        model.addAttribute("warehouses", warehouseAdminService.searchWarehouses(q));
        model.addAttribute("q", q);
        return "warehouses/list";
    }

    @GetMapping("/warehouses/new")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_STAFF')")
    public String createWarehouse(Model model) {
        model.addAttribute("warehouseForm", new WarehouseForm());
        model.addAttribute("types", warehouseAdminService.warehouseTypes());
        return "warehouses/form";
    }

    @GetMapping("/warehouses/{id}/edit")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_STAFF')")
    public String editWarehouse(@PathVariable Long id, Model model) {
        var warehouse = warehouseRepository.findById(id)
                .filter(candidate -> !candidate.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Kho không tồn tại hoặc đã bị xóa"));
        model.addAttribute("warehouseForm", warehouseAdminService.toForm(warehouse));
        model.addAttribute("types", warehouseAdminService.warehouseTypes());
        return "warehouses/form";
    }

    @PostMapping("/warehouses")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_STAFF')")
    public String saveWarehouse(@Valid @ModelAttribute WarehouseForm warehouseForm, BindingResult bindingResult,
                                Model model, Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        boolean creating = warehouseForm.getId() == null;
        if (!warehouseAdminService.saveWarehouse(warehouseForm, bindingResult, authentication.getName())) {
            model.addAttribute("types", warehouseAdminService.warehouseTypes());
            return "warehouses/form";
        }
        redirectAttributes.addFlashAttribute("successMessage",
                creating ? "Đã thêm kho" : "Đã cập nhật kho");
        return "redirect:/warehouses";
    }

    @PostMapping("/warehouses/{id}/delete")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_STAFF')")
    public String deleteWarehouse(@PathVariable Long id, Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
        try {
            warehouseAdminService.deleteWarehouse(id, authentication.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa kho");
        } catch (IllegalStateException | ResourceNotFoundException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/warehouses";
    }

    @GetMapping("/locations")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_STAFF')")
    public String locations(@RequestParam(required = false) Long warehouseId, Model model) {
        model.addAttribute("locations", warehouseAdminService.locations(warehouseId));
        model.addAttribute("warehouses", warehouseRepository.findByDeletedFalseOrderByCodeAsc());
        model.addAttribute("warehouseId", warehouseId);
        return "locations/list";
    }

    @GetMapping("/locations/new")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_STAFF')")
    public String createLocation(Model model) {
        model.addAttribute("locationForm", new StorageLocationForm());
        locationFormData(model);
        return "locations/form";
    }

    @GetMapping("/locations/{id}/edit")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_STAFF')")
    public String editLocation(@PathVariable Long id, Model model) {
        var location = locationRepository.findById(id)
                .filter(candidate -> !candidate.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Vị trí không tồn tại hoặc đã bị xóa"));
        model.addAttribute("locationForm", warehouseAdminService.toForm(location));
        locationFormData(model);
        return "locations/form";
    }

    @PostMapping("/locations")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_STAFF')")
    public String saveLocation(@Valid @ModelAttribute StorageLocationForm locationForm, BindingResult bindingResult,
                               Model model, Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        boolean creating = locationForm.getId() == null;
        if (!warehouseAdminService.saveLocation(locationForm, bindingResult, authentication.getName())) {
            locationFormData(model);
            return "locations/form";
        }
        redirectAttributes.addFlashAttribute("successMessage",
                creating ? "Đã thêm vị trí" : "Đã cập nhật vị trí");
        return "redirect:/locations";
    }

    @PostMapping("/locations/{id}/delete")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_STAFF')")
    public String deleteLocation(@PathVariable Long id, Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        try {
            warehouseAdminService.deleteLocation(id, authentication.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa vị trí");
        } catch (IllegalStateException | ResourceNotFoundException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/locations";
    }

    private void locationFormData(Model model) {
        model.addAttribute("warehouses", warehouseRepository.findByDeletedFalseAndActiveTrueOrderByCodeAsc());
        StorageLocationForm form = (StorageLocationForm) model.getAttribute("locationForm");
        model.addAttribute("parents", warehouseAdminService.parentLocationChoices(form));
        model.addAttribute("types", warehouseAdminService.locationTypes());
    }
}
