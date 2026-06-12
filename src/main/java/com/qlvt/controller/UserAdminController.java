package com.qlvt.controller;

import com.qlvt.form.UserForm;
import com.qlvt.repository.DepartmentRepository;
import com.qlvt.repository.AppUserRepository;
import com.qlvt.service.UserAdminService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserAdminController {
    private final UserAdminService userAdminService;
    private final DepartmentRepository departmentRepository;
    private final AppUserRepository userRepository;

    public UserAdminController(UserAdminService userAdminService, DepartmentRepository departmentRepository, AppUserRepository userRepository) {
        this.userAdminService = userAdminService;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String q, Model model) {
        model.addAttribute("users", userAdminService.search(q));
        model.addAttribute("q", q);
        return "users/list";
    }

    @GetMapping("/new")
    public String create(Model model) {
        model.addAttribute("userForm", new UserForm());
        formData(model);
        return "users/form";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("userForm", userAdminService.toForm(userRepository.findById(id).orElseThrow()));
        formData(model);
        return "users/form";
    }

    @PostMapping
    public String save(@Valid @ModelAttribute UserForm userForm, BindingResult bindingResult, Model model, Authentication authentication) {
        if (!userAdminService.save(userForm, bindingResult, authentication.getName())) {
            formData(model);
            return "users/form";
        }
        return "redirect:/users";
    }

    @PostMapping("/{id}/lock")
    public String lock(@PathVariable Long id, Authentication authentication) {
        userAdminService.lock(id, authentication.getName());
        return "redirect:/users";
    }

    @PostMapping("/{id}/unlock")
    public String unlock(@PathVariable Long id, Authentication authentication) {
        userAdminService.unlock(id, authentication.getName());
        return "redirect:/users";
    }

    @PostMapping("/{id}/reset-password")
    public String resetPassword(@PathVariable Long id, Authentication authentication) {
        userAdminService.resetPassword(id, authentication.getName());
        return "redirect:/users";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, Authentication authentication) {
        userAdminService.softDelete(id, authentication.getName());
        return "redirect:/users";
    }

    private void formData(Model model) {
        model.addAttribute("roles", userAdminService.roles());
        model.addAttribute("departments", departmentRepository.findByDeletedFalseOrderByCodeAsc());
    }
}
