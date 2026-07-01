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
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserAdminService {
    private static final String TEMP_PASSWORD_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

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
            bindingResult.rejectValue("username", "duplicate", "Username da ton tai");
        }
        if (form.getId() == null && (form.getPassword() == null || form.getPassword().isBlank())) {
            bindingResult.rejectValue("password", "required", "Mat khau la bat buoc khi tao moi");
        }
        if (RoleUtils.requiresDepartment(form.getRole()) && (form.getDepartment() == null || form.getDepartment().isBlank())) {
            bindingResult.rejectValue("department", "required", "Khoa/phong la bat buoc voi vai tro khoa/phong");
        }
        if (bindingResult.hasErrors()) {
            return false;
        }

        AppUser user = form.getId() == null ? new AppUser() : userRepository.findById(form.getId()).orElseThrow();
        String oldValue = user.getId() == null ? "" : user.getUsername() + "|" + user.getRole();
        user.setUsername(form.getUsername());
        if (form.getPassword() != null && !form.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(form.getPassword()));
            user.setMustChangePassword(true);
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
                oldValue, user.getUsername() + "|" + user.getRole(), "Save user " + user.getUsername());
        return true;
    }

    @Transactional
    public void lock(Long id, String actor) {
        AppUser user = userRepository.findById(id).orElseThrow();
        user.setLocked(true);
        user.setUpdatedAt(LocalDateTime.now());
        auditService.log(actor, "LOCK_USER", "AppUser", user.getUsername(), "Lock user");
    }

    @Transactional
    public void unlock(Long id, String actor) {
        AppUser user = userRepository.findById(id).orElseThrow();
        user.setLocked(false);
        user.setUpdatedAt(LocalDateTime.now());
        auditService.log(actor, "UNLOCK_USER", "AppUser", user.getUsername(), "Unlock user");
    }

    @Transactional
    public String resetPassword(Long id, String actor) {
        AppUser user = userRepository.findById(id).orElseThrow();
        String temporaryPassword = generateTemporaryPassword();
        user.setPassword(passwordEncoder.encode(temporaryPassword));
        user.setMustChangePassword(true);
        user.setUpdatedAt(LocalDateTime.now());
        auditService.log(actor, "RESET_PASSWORD", "AppUser", user.getUsername(), "Reset temporary password without logging the secret");
        return temporaryPassword;
    }

    private String generateTemporaryPassword() {
        StringBuilder builder = new StringBuilder("Tmp-");
        for (int i = 0; i < 12; i++) {
            builder.append(TEMP_PASSWORD_ALPHABET.charAt(SECURE_RANDOM.nextInt(TEMP_PASSWORD_ALPHABET.length())));
        }
        return builder.toString();
    }

    @Transactional
    public void softDelete(Long id, String actor) {
        AppUser user = userRepository.findById(id).orElseThrow();
        user.setDeleted(true);
        user.setEnabled(false);
        user.setUpdatedAt(LocalDateTime.now());
        auditService.log(actor, "DELETE_USER", "AppUser", user.getUsername(), "Soft delete user");
    }

    public UserRole[] roles() {
        return UserRole.values();
    }
}
