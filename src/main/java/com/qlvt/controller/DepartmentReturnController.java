package com.qlvt.controller;

import com.qlvt.repository.*;
import com.qlvt.enums.AttachmentReferenceType;
import com.qlvt.service.AttachmentService;
import com.qlvt.service.CurrentUserService;
import com.qlvt.service.DataPermissionService;
import com.qlvt.service.DepartmentStockService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/department-returns")
public class DepartmentReturnController {
    private final DepartmentReturnRepository returnRepository;
    private final DepartmentStockRepository stockRepository;
    private final WarehouseRepository warehouseRepository;
    private final StorageLocationRepository locationRepository;
    private final DepartmentStockService stockService;
    private final CurrentUserService currentUserService;
    private final DataPermissionService dataPermissionService;
    private final AttachmentService attachmentService;

    public DepartmentReturnController(DepartmentReturnRepository returnRepository,
                                      DepartmentStockRepository stockRepository,
                                      WarehouseRepository warehouseRepository,
                                      StorageLocationRepository locationRepository,
                                      DepartmentStockService stockService,
                                      CurrentUserService currentUserService,
                                      DataPermissionService dataPermissionService,
                                      AttachmentService attachmentService) {
        this.returnRepository = returnRepository;
        this.stockRepository = stockRepository;
        this.warehouseRepository = warehouseRepository;
        this.locationRepository = locationRepository;
        this.stockService = stockService;
        this.currentUserService = currentUserService;
        this.dataPermissionService = dataPermissionService;
        this.attachmentService = attachmentService;
    }

    @GetMapping
    public String list(Model model) {
        var user = currentUserService.currentUser();
        model.addAttribute("items", dataPermissionService.canViewAllRequests(user) ? returnRepository.findTop30ByOrderByCreatedAtDesc() : returnRepository.findTop30ByDepartmentOrderByCreatedAtDesc(user.getDepartment()));
        return "department-returns/list";
    }

    @GetMapping("/new")
    public String form(Model model) {
        var user = currentUserService.currentUser();
        model.addAttribute("stocks", dataPermissionService.canViewAllRequests(user) ? stockRepository.findByQuantityOnHandGreaterThanOrderByDepartmentAscMaterial_CodeAsc(0) : stockRepository.findByDepartmentAndQuantityOnHandGreaterThanOrderByMaterial_CodeAscBatch_ExpiryDateAsc(user.getDepartment(), 0));
        model.addAttribute("warehouses", warehouseRepository.findAll());
        model.addAttribute("locations", locationRepository.findAll());
        return "department-returns/form";
    }

    @PostMapping
    public String create(@RequestParam Long stockId, @RequestParam Long warehouseId, @RequestParam Long locationId,
                         @RequestParam int quantity, @RequestParam(required = false) String reason, Authentication authentication) {
        var stock = stockRepository.findById(stockId).orElseThrow();
        if (!dataPermissionService.canViewDepartmentName(currentUserService.currentUser(), stock.getDepartment())) {
            throw new AccessDeniedException("Bạn không có quyền tạo phiếu trả cho khoa/phòng khác");
        }
        var item = stockService.createReturn(stockId, warehouseId, locationId, quantity, reason, authentication.getName());
        return "redirect:/department-returns/" + item.getId();
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        var item = returnRepository.findWithLinesById(id).orElseThrow();
        if (!dataPermissionService.canViewDepartmentName(currentUserService.currentUser(), item.getDepartment())) {
            throw new AccessDeniedException("Bạn không có quyền xem phiếu trả của khoa/phòng khác");
        }
        model.addAttribute("item", item);
        model.addAttribute("attachments", attachmentService.list(AttachmentReferenceType.DEPARTMENT_RETURN, id));
        model.addAttribute("attachmentType", AttachmentReferenceType.DEPARTMENT_RETURN);
        return "department-returns/detail";
    }

    @PostMapping("/{id}/receive")
    public String receive(@PathVariable Long id, Authentication authentication) {
        dataPermissionService.checkCanProcessWarehouseStock();
        stockService.receiveReturn(id, authentication.getName());
        return "redirect:/department-returns/" + id;
    }
}
