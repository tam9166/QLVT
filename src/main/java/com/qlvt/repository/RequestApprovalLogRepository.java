package com.qlvt.repository;

import com.qlvt.entity.RequestApprovalLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RequestApprovalLogRepository extends JpaRepository<RequestApprovalLog, Long> {
    List<RequestApprovalLog> findByMaterialRequest_IdOrderByCreatedAtAsc(Long requestId);
}
