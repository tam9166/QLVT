package com.qlvt.repository;

import com.qlvt.entity.PriceAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PriceAlertRepository extends JpaRepository<PriceAlert, Long> {
    List<PriceAlert> findTop50ByOrderByCreatedAtDesc();
    List<PriceAlert> findByReceipt_Id(Long receiptId);
    long countByResolvedFalse();
}
