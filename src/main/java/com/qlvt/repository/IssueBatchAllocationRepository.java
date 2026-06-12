package com.qlvt.repository;

import com.qlvt.entity.IssueBatchAllocation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IssueBatchAllocationRepository extends JpaRepository<IssueBatchAllocation, Long> {
    java.util.List<IssueBatchAllocation> findByBatch_Id(Long batchId);
}
