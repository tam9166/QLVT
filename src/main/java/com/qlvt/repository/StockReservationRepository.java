package com.qlvt.repository;

import com.qlvt.entity.StockReservation;
import com.qlvt.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StockReservationRepository extends JpaRepository<StockReservation, Long> {
    List<StockReservation> findByMaterialRequest_IdAndStatus(Long requestId, ReservationStatus status);
    List<StockReservation> findByMaterialRequestLine_IdAndStatus(Long lineId, ReservationStatus status);
}
