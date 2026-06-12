package com.qlvt.repository;

import com.qlvt.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    List<StockMovement> findTop20ByOrderByCreatedAtDesc();
}
