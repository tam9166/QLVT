package com.qlvt.repository;

import com.qlvt.entity.TemperatureLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface TemperatureLogRepository extends JpaRepository<TemperatureLog, Long> {
    List<TemperatureLog> findTop50ByOrderByRecordedAtDesc();
    List<TemperatureLog> findTop10ByStatusInOrderByRecordedAtDesc(List<String> statuses);
    long countByStatusInAndRecordedAtAfter(List<String> statuses, LocalDateTime recordedAt);
}
