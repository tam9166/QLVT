package com.qlvt.repository;

import com.qlvt.entity.MaterialRequest;
import com.qlvt.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MaterialRequestRepository extends JpaRepository<MaterialRequest, Long> {
    boolean existsByCode(String code);
    List<MaterialRequest> findTop20ByOrderByCreatedAtDesc();
    long countByStatus(RequestStatus status);
    List<MaterialRequest> findTop20ByRequesterOrderByCreatedAtDesc(String requester);
    List<MaterialRequest> findTop20ByDepartmentOrderByCreatedAtDesc(String department);
    List<MaterialRequest> findTop10ByRequesterOrderByCreatedAtDesc(String requester);
    List<MaterialRequest> findTop10ByDepartmentOrderByCreatedAtDesc(String department);
}
