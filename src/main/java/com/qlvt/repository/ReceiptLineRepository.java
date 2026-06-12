package com.qlvt.repository;

import com.qlvt.entity.ReceiptLine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReceiptLineRepository extends JpaRepository<ReceiptLine, Long> {
}
