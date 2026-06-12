package com.qlvt.repository;

import com.qlvt.entity.IssueSlip;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface IssueSlipRepository extends JpaRepository<IssueSlip, Long> {
    boolean existsByIssueCode(String issueCode);
    Optional<IssueSlip> findByMaterialRequest_Id(Long requestId);
    List<IssueSlip> findByMaterialRequest_IdOrderByIdAsc(Long requestId);
    List<IssueSlip> findTop30ByOrderByCreatedAtDesc();
    List<IssueSlip> findTop10ByMaterialRequest_RequesterAndStatusOrderByCreatedAtDesc(String requester, com.qlvt.enums.IssueStatus status);
    List<IssueSlip> findTop10ByDepartmentAndStatusOrderByCreatedAtDesc(String department, com.qlvt.enums.IssueStatus status);
}
