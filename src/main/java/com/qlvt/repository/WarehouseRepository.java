package com.qlvt.repository;

import com.qlvt.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    Optional<Warehouse> findByCode(String code);
    boolean existsByCodeIgnoreCase(String code);
    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);
    List<Warehouse> findByDeletedFalseOrderByCodeAsc();
    List<Warehouse> findByDeletedFalseAndActiveTrueOrderByCodeAsc();
    List<Warehouse> findByDeletedFalseAndCodeContainingIgnoreCaseOrDeletedFalseAndNameContainingIgnoreCase(String code, String name);
}
