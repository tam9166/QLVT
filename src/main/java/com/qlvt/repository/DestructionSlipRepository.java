package com.qlvt.repository;

import com.qlvt.entity.DestructionSlip;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DestructionSlipRepository extends JpaRepository<DestructionSlip, Long> {
    boolean existsByDestructionCode(String destructionCode);
    List<DestructionSlip> findTop30ByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"lines", "lines.material", "lines.batch", "lines.warehouse", "lines.location"})
    Optional<DestructionSlip> findWithLinesById(Long id);
}
