package com.qlvt.repository;

import com.qlvt.entity.InventoryCount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InventoryCountRepository extends JpaRepository<InventoryCount, Long> {
    boolean existsByCountCode(String countCode);
    List<InventoryCount> findTop30ByOrderByCreatedAtDesc();
}
