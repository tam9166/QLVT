package com.qlvt.repository;

import com.qlvt.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    Optional<ChatSession> findFirstByUserOrderByUpdatedAtDesc(String user);
    List<ChatSession> findTop10ByUserOrderByUpdatedAtDesc(String user);
}
