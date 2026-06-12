package com.qlvt.repository;

import com.qlvt.entity.RecallDepartmentResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RecallDepartmentResponseRepository extends JpaRepository<RecallDepartmentResponse, Long> {
    List<RecallDepartmentResponse> findByRecallOrder_IdOrderByRespondedAtDesc(Long recallOrderId);
}
