package com.qlvt.repository;

import com.qlvt.entity.DepartmentReturn;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DepartmentReturnRepository extends JpaRepository<DepartmentReturn, Long> {
    boolean existsByReturnCode(String returnCode);
    List<DepartmentReturn> findTop30ByOrderByCreatedAtDesc();
    List<DepartmentReturn> findTop30ByDepartmentOrderByCreatedAtDesc(String department);

    @EntityGraph(attributePaths = {"warehouse", "lines", "lines.material", "lines.batch", "lines.location"})
    Optional<DepartmentReturn> findWithLinesById(Long id);
}
