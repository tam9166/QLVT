package com.qlvt.repository;

import com.qlvt.entity.StockTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StockTransferRepository extends JpaRepository<StockTransfer, Long> {
    boolean existsByTransferCode(String transferCode);
    List<StockTransfer> findTop30ByOrderByCreatedAtDesc();
}
