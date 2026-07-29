package com.qlvt.repository;

import com.qlvt.entity.StockBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StockBalanceRepository extends JpaRepository<StockBalance, Long> {
    Optional<StockBalance> findByMaterial_IdAndBatch_IdAndWarehouse_IdAndLocation_Id(Long materialId, Long batchId, Long warehouseId, Long locationId);
    List<StockBalance> findByWarehouse_IdOrderByMaterial_CodeAscBatch_ExpiryDateAsc(Long warehouseId);
    List<StockBalance> findByMaterial_IdOrderByWarehouse_CodeAscLocation_CodeAsc(Long materialId);
    List<StockBalance> findTop20ByBatch_ExpiryDateBetweenOrderByBatch_ExpiryDateAsc(LocalDate from, LocalDate to);

    @Query("""
            select case when count(b) > 0 then true else false end
            from StockBalance b
            where b.batch.id = :batchId
              and (b.reservedQuantity > 0 or b.pendingIssueQuantity > 0)
            """)
    boolean hasCommittedQuantityForBatch(@Param("batchId") Long batchId);

    @Query("""
            select coalesce(sum(b.actualQuantity - b.reservedQuantity - b.pendingIssueQuantity), 0)
            from StockBalance b
            where b.material.id = :materialId
              and b.actualQuantity > b.reservedQuantity + b.pendingIssueQuantity
              and b.batch.status = com.qlvt.enums.BatchStatus.AVAILABLE
              and (b.batch.expiryDate is null or b.batch.expiryDate > :today)
            """)
    long sumAvailableByMaterialId(@Param("materialId") Long materialId, @Param("today") LocalDate today);

    @Query("""
            select coalesce(sum(b.actualQuantity - b.reservedQuantity - b.pendingIssueQuantity), 0)
            from StockBalance b
            where b.material.id = :materialId
              and b.warehouse.id = :warehouseId
              and b.actualQuantity > b.reservedQuantity + b.pendingIssueQuantity
              and b.batch.status = com.qlvt.enums.BatchStatus.AVAILABLE
              and (b.batch.expiryDate is null or b.batch.expiryDate > :today)
            """)
    long sumAvailableByMaterialIdAndWarehouseId(@Param("materialId") Long materialId,
                                                @Param("warehouseId") Long warehouseId,
                                                @Param("today") LocalDate today);

    @Query("""
            select b from StockBalance b
            where b.material.id = :materialId
              and b.actualQuantity > b.reservedQuantity + b.pendingIssueQuantity
              and b.batch.status = com.qlvt.enums.BatchStatus.AVAILABLE
              and (b.batch.expiryDate is null or b.batch.expiryDate > :today)
            order by case when b.batch.expiryDate is null then 1 else 0 end,
                     b.batch.expiryDate asc, b.batch.receiptDate asc, b.batch.id asc
            """)
    List<StockBalance> findAvailableFefo(@Param("materialId") Long materialId, @Param("today") LocalDate today);

    @Query("""
            select b from StockBalance b
            where b.material.id = :materialId
              and b.warehouse.id = :warehouseId
              and b.actualQuantity > b.reservedQuantity + b.pendingIssueQuantity
              and b.batch.status = com.qlvt.enums.BatchStatus.AVAILABLE
              and (b.batch.expiryDate is null or b.batch.expiryDate > :today)
            order by case when b.batch.expiryDate is null then 1 else 0 end,
                     b.batch.expiryDate asc, b.batch.receiptDate asc, b.batch.id asc
            """)
    List<StockBalance> findAvailableFefoInWarehouse(@Param("materialId") Long materialId,
                                                    @Param("warehouseId") Long warehouseId,
                                                    @Param("today") LocalDate today);
}
