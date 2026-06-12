package com.qlvt.repository;

import com.qlvt.entity.StockAdjustmentLine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockAdjustmentLineRepository extends JpaRepository<StockAdjustmentLine, Long> {
}
