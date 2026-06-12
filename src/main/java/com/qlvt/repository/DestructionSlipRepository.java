package com.qlvt.repository;

import com.qlvt.entity.DestructionSlip;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DestructionSlipRepository extends JpaRepository<DestructionSlip, Long> {
    boolean existsByDestructionCode(String destructionCode);
    List<DestructionSlip> findTop30ByOrderByCreatedAtDesc();
}
