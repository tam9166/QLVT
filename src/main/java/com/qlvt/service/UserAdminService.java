package com.qlvt.service;

import com.qlvt.entity.AppUser;
import com.qlvt.enums.UserRole;
import com.qlvt.form.UserForm;
import com.qlvt.repository.AppUserRepository;
import com.qlvt.util.RoleUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserAdminService {
    public static final String DEFAULT_RESET_PASSWORD = "123456";

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public UserAdminService(AppUserRepository userRepository, PasswordEncoder passwordEncoder, AuditService auditService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    public List<AppUser> search(String q) {
        if (q == null || q.isBlank()) {
            return userRepository.findByDeletedFalseOrderByUsernameAsc();
        }
        return userRepository.findByDeletedFalseAndUsernameContainingIgnoreCaseOrDeletedFalseAndFullNameContainingIgnoreCaseOrDeletedFalseAndEmailContainingIgnoreCase(q, q, q);
    }

    public UserForm toForm(AppUser user) {
        UserForm form = new UserForm();
        form.setId(user.getId());
        form.setUsername(user.getUsername());
        form.setFullName(user.getFullName());
        form.setEmail(user.getEmail());
        form.setPhone(user.getPhone());
        form.setRole(user.getRole());
        form.setDepartment(user.getDepartment());
        form.setEnabled(user.isEnabled());
        return form;
    }

    @Transactional
    public boolean save(UserForm form, BindingResult bindingResult, String actor) {
        Long id = form.getId() == null ? -1L : form.getId();
        if ((form.getId() == null && userRepository.existsByUsername(form.getUsername()))
                || (form.getId() != null && userRepository.existsByUsernameAndIdNot(form.getUsername(), id))) {
            bindingResult.rejectValue("username", "duplicate", "Username đã tồn tại");
        }
        if (form.getId() == null && (form.getPassword() == null || form.getPassword().isBlank())) {
            bindingResult.rejectValue("password", "required", "Mật khẩu là bắt buộc khi tạo mới");
        }
        if (RoleUtils.requiresDepartment(form.getRole()) && (form.getDepartment() == null || form.getDepartment().isBlank())) {
            bindingResult.rejectValue("department", "required", "Khoa/phòng là bắt buộc với vai trò khoa/phòng");
        }
        if (bindingResult.hasErrors()) {
            return false;
        }

        AppUser user = form.getId() == null ? new AppUser() : userRepository.findById(form.getId()).orElseThrow();
        String oldValue = user.getId() == null ? "" : user.getUsername() + "|" + user.getRole();
        user.setUsername(form.getUsername());
        if (form.getPassword() != null && !form.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(form.getPassword()));
            user.setVisiblePassword(form.getPassword());
        }
        user.setFullName(form.getFullName());
        user.setEmail(form.getEmail());
        user.setPhone(form.getPhone());
        user.setRole(form.getRole());
        user.setDepartment(form.getDepartment());
        user.setEnabled(form.isEnabled());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        auditService.logChange(actor, form.getId() == null ? "CREATE_USER" : "UPDATE_USER", "AppUser", user.getUsername(),
                oldValue, user.getUsername() + "|" + user.getRole(), "Lưu tài khoản " + user.getUsername());
        return true;
    }

    @Transactional
    public void lock(Long id, String actor) {
        AppUser user = userRepository.findById(id).orElseThrow();
        user.setLocked(true);
        user.setUpdatedAt(LocalDateTime.now());
        auditService.log(actor, "LOCK_USER", "AppUser", user.getUsername(), "Khóa tài khoản");
    }

    @Transactional
    public void unlock(Long id, String actor) {
        AppUser user = userRepository.findById(id).orElseThrow();
        user.setLocked(false);
        user.setUpdatedAt(LocalDateTime.now());
        auditService.log(actor, "UNLOCK_USER", "AppUser", user.getUsername(), "Mở khóa tài khoản");
    }

    @Transactional
    public void resetPassword(Long id, String actor) {
        AppUser user = userRepository.findById(id).orElseThrow();
        user.setPassword(passwordEncoder.encode(DEFAULT_RESET_PASSWORD));
        user.setVisiblePassword(DEFAULT_RESET_PASSWORD);
        user.setUpdatedAt(LocalDateTime.now());
        auditService.log(actor, "RESET_PASSWORD", "AppUser", user.getUsername(), "Reset mật khẩu về " + DEFAULT_RESET_PASSWORD);
    }

    @Transactional
    public void softDelete(Long id, String actor) {
        AppUser user = userRepository.findById(id).orElseThrow();
        user.setDeleted(true);
        user.setEnabled(false);
        user.setUpdatedAt(LocalDateTime.now());
        auditService.log(actor, "DELETE_USER", "AppUser", user.getUsername(), "Xóa mềm tài khoản");
    }

    public UserRole[] roles() {
        return UserRole.values();
    }
}
