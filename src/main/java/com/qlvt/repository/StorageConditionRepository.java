package com.qlvt.repository;

import com.qlvt.entity.StorageCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface StorageConditionRepository extends JpaRepository<StorageCondition, Long> {
    Optional<StorageCondition> findByMaterial_Id(Long materialId);
    List<StorageCondition> findByColdChainRequiredTrueOrderByMaterial_CodeAsc();
    List<StorageCondition> findTop50ByOrderByUpdatedAtDesc();
}
