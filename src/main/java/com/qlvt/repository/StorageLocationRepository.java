package com.qlvt.repository;

import com.qlvt.entity.StorageLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface StorageLocationRepository extends JpaRepository<StorageLocation, Long> {
    Optional<StorageLocation> findByCode(String code);
    boolean existsByCode(String code);
    boolean existsByWarehouse_IdAndCodeIgnoreCaseAndIdNot(Long warehouseId, String code, Long id);
    boolean existsByWarehouse_IdAndCodeIgnoreCase(Long warehouseId, String code);
    boolean existsByWarehouse_IdAndDeletedFalse(Long warehouseId);
    boolean existsByWarehouse_IdAndDeletedFalseAndActiveTrue(Long warehouseId);
    boolean existsByParent_IdAndDeletedFalse(Long parentId);
    boolean existsByParent_IdAndDeletedFalseAndActiveTrue(Long parentId);
    List<StorageLocation> findByDeletedFalseOrderByCodeAsc();
    List<StorageLocation> findByDeletedFalseAndActiveTrueOrderByCodeAsc();
    List<StorageLocation> findByWarehouse_IdAndDeletedFalseOrderByCodeAsc(Long warehouseId);
}
