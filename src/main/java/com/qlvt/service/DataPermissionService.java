package com.qlvt.service;

import com.qlvt.entity.AppUser;
import com.qlvt.entity.MaterialRequest;
import com.qlvt.enums.UserRole;
import com.qlvt.repository.MaterialRequestRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class DataPermissionService {
    private final CurrentUserService currentUserService;
    private final MaterialRequestRepository requestRepository;

    public DataPermissionService(CurrentUserService currentUserService, MaterialRequestRepository requestRepository) {
        this.currentUserService = currentUserService;
        this.requestRepository = requestRepository;
    }

    public AppUser currentUser() {
        return currentUserService.currentUser();
    }

    public boolean canViewAllRequests(AppUser user) {
        return hasAny(user, UserRole.ADMIN, UserRole.MANAGER, UserRole.ACCOUNTANT, UserRole.WAREHOUSE_STAFF);
    }

    public boolean canProcessWarehouseStock(AppUser user) {
        return hasAny(user, UserRole.ADMIN, UserRole.MANAGER, UserRole.WAREHOUSE_STAFF);
    }

    public boolean canViewDepartmentName(AppUser user, String department) {
        if (canViewAllRequests(user)) {
            return true;
        }
        return sameText(user.getDepartment(), department);
    }

    public void checkCanViewMaterialRequest(Long requestId) {
        MaterialRequest request = requestRepository.findById(requestId).orElseThrow();
        AppUser user = currentUser();
        boolean requester = sameText(user.getUsername(), request.getRequester());
        if (!canViewAllRequests(user) && !requester && !canViewDepartmentName(user, request.getDepartment())) {
            throw new AccessDeniedException("Bạn không có quyền xem phiếu yêu cầu của khoa/phòng khác");
        }
    }

    public void checkCanApproveDepartmentRequest(Long requestId) {
        MaterialRequest request = requestRepository.findById(requestId).orElseThrow();
        AppUser user = currentUser();
        if (!(hasAny(user, UserRole.ADMIN, UserRole.MANAGER) || (user.getRole() == UserRole.DEPARTMENT_HEAD && sameText(user.getDepartment(), request.getDepartment())))) {
            throw new AccessDeniedException("Bạn không có quyền duyệt yêu cầu của khoa/phòng này");
        }
    }

    public void checkCanProcessWarehouseRequest(Long requestId) {
        AppUser user = currentUser();
        if (!hasAny(user, UserRole.ADMIN, UserRole.MANAGER, UserRole.WAREHOUSE_STAFF)) {
            throw new AccessDeniedException("Bạn không có quyền xử lý xuất kho cho phiếu này");
        }
    }

    public void checkCanCancelMaterialRequest(Long requestId) {
        MaterialRequest request = requestRepository.findById(requestId).orElseThrow();
        AppUser user = currentUser();
        if (!(hasAny(user, UserRole.ADMIN, UserRole.MANAGER)
                || sameText(user.getUsername(), request.getRequester()))) {
            throw new AccessDeniedException("B\u1ea1n ch\u1ec9 c\u00f3 th\u1ec3 h\u1ee7y phi\u1ebfu y\u00eau c\u1ea7u do m\u00ecnh t\u1ea1o");
        }
    }

    public void checkCanProcessWarehouseStock() {
        if (!canProcessWarehouseStock(currentUser())) {
            throw new AccessDeniedException("Bạn không có quyền xử lý tồn kho");
        }
    }

    public boolean canUseChatbotData(Long materialId, Long warehouseId, Long departmentId) {
        AppUser user = currentUser();
        return hasAny(user, UserRole.ADMIN, UserRole.MANAGER, UserRole.ACCOUNTANT, UserRole.WAREHOUSE_STAFF, UserRole.PROCUREMENT)
                || departmentId == null;
    }

    private boolean hasAny(AppUser user, UserRole... roles) {
        for (UserRole role : roles) {
            if (user.getRole() == role) {
                return true;
            }
        }
        return false;
    }

    private boolean sameText(String left, String right) {
        return Objects.equals(normalize(left), normalize(right));
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
