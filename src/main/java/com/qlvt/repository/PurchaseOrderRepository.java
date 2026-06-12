package com.qlvt.repository;

import com.qlvt.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    boolean existsByOrderCode(String orderCode);
    List<PurchaseOrder> findTop30ByOrderByCreatedAtDesc();
    List<PurchaseOrder> findByExpectedDeliveryDateBefore(LocalDate date);
}
