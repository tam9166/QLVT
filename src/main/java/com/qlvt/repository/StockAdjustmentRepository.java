package com.qlvt.repository;

import com.qlvt.entity.StockAdjustment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StockAdjustmentRepository extends JpaRepository<StockAdjustment, Long> {
    boolean existsByAdjustmentCode(String adjustmentCode);
    List<StockAdjustment> findTop30ByOrderByCreatedAtDesc();
}
