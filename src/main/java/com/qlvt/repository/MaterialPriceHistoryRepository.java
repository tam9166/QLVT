package com.qlvt.repository;

import com.qlvt.entity.MaterialPriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface MaterialPriceHistoryRepository extends JpaRepository<MaterialPriceHistory, Long> {
    List<MaterialPriceHistory> findTop50ByOrderByReceivedDateDescCreatedAtDesc();
    List<MaterialPriceHistory> findByMaterial_IdOrderByReceivedDateDescCreatedAtDesc(Long materialId);
    List<MaterialPriceHistory> findByReceipt_Id(Long receiptId);
    Optional<MaterialPriceHistory> findTopByMaterial_IdAndIdNotOrderByReceivedDateDescCreatedAtDesc(Long materialId, Long id);

    @Query("select avg(h.unitPrice) from MaterialPriceHistory h where h.material.id = :materialId")
    BigDecimal averagePrice(@Param("materialId") Long materialId);

    @Query("select min(h.unitPrice) from MaterialPriceHistory h where h.material.id = :materialId")
    BigDecimal minPrice(@Param("materialId") Long materialId);

    @Query("select max(h.unitPrice) from MaterialPriceHistory h where h.material.id = :materialId")
    BigDecimal maxPrice(@Param("materialId") Long materialId);
}
