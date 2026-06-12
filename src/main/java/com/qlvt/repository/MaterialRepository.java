package com.qlvt.repository;

import com.qlvt.entity.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MaterialRepository extends JpaRepository<Material, Long> {
    Optional<Material> findByCode(String code);
    boolean existsByCode(String code);
    List<Material> findByDeletedFalseOrderByCodeAsc();
    List<Material> findByDeletedFalseAndNameContainingIgnoreCaseOrDeletedFalseAndCodeContainingIgnoreCase(String name, String code);
}
