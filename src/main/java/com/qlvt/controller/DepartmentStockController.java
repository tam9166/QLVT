package com.qlvt.controller;

import com.qlvt.enums.DepartmentIssueType;
import com.qlvt.repository.DepartmentStockMovementRepository;
import com.qlvt.repository.DepartmentStockRepository;
import com.qlvt.service.CurrentUserService;
import com.qlvt.service.DataPermissionService;
import com.qlvt.service.DepartmentStockService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/department-stocks")
public class DepartmentStockController {
    private final DepartmentStockRepository stockRepository;
    private final DepartmentStockMovementRepository movementRepository;
    private final DepartmentStockService stockService;
    private final CurrentUserService currentUserService;
    private final DataPermissionService dataPermissionService;

    public DepartmentStockController(DepartmentStockRepository stockRepository,
                                     DepartmentStockMovementRepository movementRepository,
                                     DepartmentStockService stockService,
                                     CurrentUserService currentUserService,
                                     DataPermissionService dataPermissionService) {
        this.stockRepository = stockRepository;
        this.movementRepository = movementRepository;
        this.stockService = stockService;
        this.currentUserService = currentUserService;
        this.dataPermissionService = dataPermissionService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String department, Model model) {
        var user = currentUserService.currentUser();
        if (dataPermissionService.canViewAllRequests(user)) {
            model.addAttribute("items", stockRepository.findByQuantityOnHandGreaterThanOrderByDepartmentAscMaterial_CodeAsc(0));
        } else {
            model.addAttribute("items", stockRepository.findByDepartmentAndQuantityOnHandGreaterThanOrderByMaterial_CodeAscBatch_ExpiryDateAsc(user.getDepartment(), 0));
        }
        return "department-stocks/list";
    }

    @GetMapping("/my-department")
    public String myDepartment(Model model) {
        var user = currentUserService.currentUser();
        model.addAttribute("items", stockRepository.findByDepartmentAndQuantityOnHandGreaterThanOrderByMaterial_CodeAscBatch_ExpiryDateAsc(user.getDepartment(), 0));
        return "department-stocks/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        var stock = stockRepository.findById(id).orElseThrow();
        if (!dataPermissionService.canViewDepartmentName(currentUserService.currentUser(), stock.getDepartment())) {
            throw new AccessDeniedException("Bạn không có quyền xem tồn của khoa/phòng này");
        }
        model.addAttribute("item", stock);
        model.addAttribute("movements", movementRepository.findTop50ByDepartmentAndMaterial_IdAndBatch_IdOrderByCreatedAtDesc(stock.getDepartment(), stock.getMaterial().getId(), stock.getBatch().getId()));
        return "department-stocks/detail";
    }

    @GetMapping("/use")
    public String useForm(Model model) {
        model.addAttribute("stocks", visibleStocks());
        return "department-stocks/use";
    }

    @PostMapping("/use")
    public String use(@RequestParam Long stockId, @RequestParam int quantity, @RequestParam(required = false) String note, Authentication authentication) {
        stockService.useStock(stockId, quantity, note, authentication.getName());
        return "redirect:/department-stocks/" + stockId;
    }

    @GetMapping("/report-issue")
    public String issueForm(Model model) {
        model.addAttribute("stocks", visibleStocks());
        model.addAttribute("issueTypes", DepartmentIssueType.values());
        return "department-stocks/report-issue";
    }

    @PostMapping("/report-issue")
    public String issue(@RequestParam Long stockId, @RequestParam DepartmentIssueType issueType,
                        @RequestParam int quantity, @RequestParam(required = false) String note, Authentication authentication) {
        stockService.reportIssue(stockId, issueType, quantity, note, authentication.getName());
        return "redirect:/department-stocks/" + stockId;
    }

    private Object visibleStocks() {
        var user = currentUserService.currentUser();
        return dataPermissionService.canViewAllRequests(user)
                ? stockRepository.findByQuantityOnHandGreaterThanOrderByDepartmentAscMaterial_CodeAsc(0)
                : stockRepository.findByDepartmentAndQuantityOnHandGreaterThanOrderByMaterial_CodeAscBatch_ExpiryDateAsc(user.getDepartment(), 0);
    }
}
