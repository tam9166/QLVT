package com.qlvt.service;

import com.qlvt.entity.Department;
import com.qlvt.form.DepartmentForm;
import com.qlvt.repository.DepartmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DepartmentService {
    private final DepartmentRepository departmentRepository;
    private final AuditService auditService;

    public DepartmentService(DepartmentRepository departmentRepository, AuditService auditService) {
        this.departmentRepository = departmentRepository;
        this.auditService = auditService;
    }

    public List<Department> search(String q) {
        if (q == null || q.isBlank()) {
            return departmentRepository.findByDeletedFalseOrderByCodeAsc();
        }
        return departmentRepository.findByDeletedFalseAndCodeContainingIgnoreCaseOrDeletedFalseAndNameContainingIgnoreCase(q, q);
    }

    public DepartmentForm toForm(Department department) {
        DepartmentForm form = new DepartmentForm();
        form.setId(department.getId());
        form.setCode(department.getCode());
        form.setName(department.getName());
        form.setDescription(department.getDescription());
        form.setActive(department.isActive());
        return form;
    }

    @Transactional
    public boolean save(DepartmentForm form, BindingResult bindingResult, String actor) {
        Long id = form.getId() == null ? -1L : form.getId();
        if ((form.getId() == null && departmentRepository.existsByCode(form.getCode()))
                || (form.getId() != null && departmentRepository.existsByCodeAndIdNot(form.getCode(), id))) {
            bindingResult.rejectValue("code", "duplicate", "Mã khoa/phòng đã tồn tại");
        }
        if (bindingResult.hasErrors()) {
            return false;
        }
        Department department = form.getId() == null ? new Department() : departmentRepository.findById(form.getId()).orElseThrow();
        department.setCode(form.getCode());
        department.setName(form.getName());
        department.setDescription(form.getDescription());
        department.setActive(form.isActive());
        department.setUpdatedAt(LocalDateTime.now());
        departmentRepository.save(department);
        auditService.log(actor, form.getId() == null ? "CREATE_DEPARTMENT" : "UPDATE_DEPARTMENT", "Department", department.getCode(), "Lưu khoa/phòng " + department.getName());
        return true;
    }

    @Transactional
    public void softDelete(Long id, String actor) {
        Department department = departmentRepository.findById(id).orElseThrow();
        department.setDeleted(true);
        department.setActive(false);
        department.setUpdatedAt(LocalDateTime.now());
        auditService.log(actor, "DELETE_DEPARTMENT", "Department", department.getCode(), "Xóa mềm khoa/phòng");
    }
}
