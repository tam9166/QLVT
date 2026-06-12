package com.qlvt.repository;

import com.qlvt.entity.InventoryCountLine;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InventoryCountLineRepository extends JpaRepository<InventoryCountLine, Long> {
    List<InventoryCountLine> findByInventoryCount_IdOrderByIdAsc(Long inventoryCountId);
}
