package com.qlvt.repository;

import com.qlvt.entity.RecallOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface RecallOrderRepository extends JpaRepository<RecallOrder, Long> {
    boolean existsByRecallCode(String recallCode);
    List<RecallOrder> findTop30ByOrderByCreatedAtDesc();

    @Query("""
            select distinct r from RecallOrder r
            join fetch r.material
            join fetch r.batch b
            left join fetch r.lines
            where r.id = :id
            """)
    Optional<RecallOrder> findDetailById(@Param("id") Long id);
}
