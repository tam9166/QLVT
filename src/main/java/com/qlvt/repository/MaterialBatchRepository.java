package com.qlvt.repository;

import com.qlvt.entity.MaterialBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MaterialBatchRepository extends JpaRepository<MaterialBatch, Long> {
    Optional<MaterialBatch> findByMaterial_IdAndBatchNumber(Long materialId, String batchNumber);

    @Query("""
            select b from MaterialBatch b
            where b.material.id = :materialId
              and b.quantity > 0
              and b.status = com.qlvt.enums.BatchStatus.AVAILABLE
              and (b.expiryDate is null or b.expiryDate > :today)
            order by case when b.expiryDate is null then 1 else 0 end,
                     b.expiryDate asc, b.receiptDate asc, b.id asc
            """)
    List<MaterialBatch> findIssuableBatchesFefo(@Param("materialId") Long materialId, @Param("today") LocalDate today);

    @Query("""
            select coalesce(sum(b.quantity), 0) from MaterialBatch b
            where b.material.id = :materialId
              and b.quantity > 0
              and b.status = com.qlvt.enums.BatchStatus.AVAILABLE
              and (b.expiryDate is null or b.expiryDate > :today)
            """)
    int sumIssuableQuantityByMaterialId(@Param("materialId") Long materialId, @Param("today") LocalDate today);

    List<MaterialBatch> findTop10ByExpiryDateBetweenOrderByExpiryDateAsc(LocalDate from, LocalDate to);
}

