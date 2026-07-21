package com.qlvt.repository;

import com.qlvt.entity.StockAdjustment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface StockAdjustmentRepository extends JpaRepository<StockAdjustment, Long> {
    boolean existsByAdjustmentCode(String adjustmentCode);
    boolean existsByInventoryCount_Id(Long inventoryCountId);
    Optional<StockAdjustment> findFirstByInventoryCount_IdOrderByCreatedAtDesc(Long inventoryCountId);
    List<StockAdjustment> findTop30ByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"warehouse", "inventoryCount", "lines", "lines.material", "lines.batch", "lines.location"})
    Optional<StockAdjustment> findWithLinesById(Long id);
}
