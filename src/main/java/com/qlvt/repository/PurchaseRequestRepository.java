package com.qlvt.repository;

import com.qlvt.entity.PurchaseRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PurchaseRequestRepository extends JpaRepository<PurchaseRequest, Long> {
    boolean existsByRequestCode(String requestCode);
    List<PurchaseRequest> findTop30ByOrderByCreatedAtDesc();
}
