package com.qlvt.repository;

import com.qlvt.entity.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
    boolean existsByReceiptCode(String receiptCode);
    List<Receipt> findTop30ByOrderByCreatedAtDesc();
}
