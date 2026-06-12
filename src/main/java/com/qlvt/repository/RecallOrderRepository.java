package com.qlvt.repository;

import com.qlvt.entity.RecallOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RecallOrderRepository extends JpaRepository<RecallOrder, Long> {
    boolean existsByRecallCode(String recallCode);
    List<RecallOrder> findTop30ByOrderByCreatedAtDesc();
}
