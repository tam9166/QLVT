package com.qlvt.repository;

import com.qlvt.entity.StockTransfer;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface StockTransferRepository extends JpaRepository<StockTransfer, Long> {
    boolean existsByTransferCode(String transferCode);
    List<StockTransfer> findTop30ByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"fromWarehouse", "toWarehouse", "lines", "lines.material", "lines.batch", "lines.fromLocation", "lines.toLocation"})
    Optional<StockTransfer> findWithLinesById(Long id);
}
