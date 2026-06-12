package com.qlvt.repository;

import com.qlvt.entity.DepartmentStock;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DepartmentStockRepository extends JpaRepository<DepartmentStock, Long> {
    Optional<DepartmentStock> findByDepartmentAndMaterial_IdAndBatch_Id(String department, Long materialId, Long batchId);
    List<DepartmentStock> findByDepartmentOrderByMaterial_CodeAscBatch_ExpiryDateAsc(String department);
    List<DepartmentStock> findByQuantityOnHandGreaterThanOrderByDepartmentAscMaterial_CodeAsc(int quantity);
    List<DepartmentStock> findByDepartmentAndQuantityOnHandGreaterThanOrderByMaterial_CodeAscBatch_ExpiryDateAsc(String department, int quantity);
}
