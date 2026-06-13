package com.qlvt.repository;

import com.qlvt.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    boolean existsByOrderCode(String orderCode);
    List<PurchaseOrder> findTop30ByOrderByCreatedAtDesc();
    List<PurchaseOrder> findByExpectedDeliveryDateBefore(LocalDate date);

    @EntityGraph(attributePaths = {"supplier", "lines", "lines.material"})
    Optional<PurchaseOrder> findWithLinesById(Long id);
}
