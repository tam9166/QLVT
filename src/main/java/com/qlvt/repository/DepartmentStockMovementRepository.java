package com.qlvt.repository;

import com.qlvt.entity.DepartmentStockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DepartmentStockMovementRepository extends JpaRepository<DepartmentStockMovement, Long> {
    List<DepartmentStockMovement> findTop50ByDepartmentOrderByCreatedAtDesc(String department);
    List<DepartmentStockMovement> findTop50ByDepartmentAndMaterial_IdAndBatch_IdOrderByCreatedAtDesc(String department, Long materialId, Long batchId);
}
