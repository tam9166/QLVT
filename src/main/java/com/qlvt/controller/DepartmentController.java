package com.qlvt.controller;

import com.qlvt.form.DepartmentForm;
import com.qlvt.repository.AppUserRepository;
import com.qlvt.repository.DepartmentRepository;
import com.qlvt.service.DepartmentService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/departments")
@PreAuthorize("hasRole('ADMIN')")
public class DepartmentController {
    private final DepartmentService departmentService;
    private final DepartmentRepository departmentRepository;
    private final AppUserRepository userRepository;

    public DepartmentController(DepartmentService departmentService, DepartmentRepository departmentRepository, AppUserRepository userRepository) {
        this.departmentService = departmentService;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String q, Model model) {
        model.addAttribute("departments", departmentService.search(q));
        model.addAttribute("q", q);
        model.addAttribute("userRepository", userRepository);
        return "departments/list";
    }

    @GetMapping("/new")
    public String create(Model model) {
        model.addAttribute("departmentForm", new DepartmentForm());
        return "departments/form";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("departmentForm", departmentService.toForm(departmentRepository.findById(id).orElseThrow()));
        return "departments/form";
    }

    @PostMapping
    public String save(@Valid @ModelAttribute DepartmentForm departmentForm, BindingResult bindingResult, Authentication authentication) {
        if (!departmentService.save(departmentForm, bindingResult, authentication.getName())) {
            return "departments/form";
        }
        return "redirect:/departments";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, Authentication authentication) {
        departmentService.softDelete(id, authentication.getName());
        return "redirect:/departments";
    }
}
