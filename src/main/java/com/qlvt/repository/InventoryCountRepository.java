package com.qlvt.repository;

import com.qlvt.entity.InventoryCount;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface InventoryCountRepository extends JpaRepository<InventoryCount, Long> {
    boolean existsByCountCode(String countCode);
    List<InventoryCount> findTop30ByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"warehouse", "lines", "lines.material", "lines.batch", "lines.location"})
    Optional<InventoryCount> findWithLinesById(Long id);
}
