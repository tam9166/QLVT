package com.qlvt.repository;

import com.qlvt.entity.StorageLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface StorageLocationRepository extends JpaRepository<StorageLocation, Long> {
    Optional<StorageLocation> findByCode(String code);
    boolean existsByCode(String code);
    boolean existsByWarehouse_IdAndCodeAndIdNot(Long warehouseId, String code, Long id);
    boolean existsByWarehouse_IdAndCode(Long warehouseId, String code);
    List<StorageLocation> findByDeletedFalseOrderByCodeAsc();
    List<StorageLocation> findByWarehouse_IdAndDeletedFalseOrderByCodeAsc(Long warehouseId);
}
