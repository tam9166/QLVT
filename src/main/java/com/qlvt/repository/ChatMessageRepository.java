package com.qlvt.repository;

import com.qlvt.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findTop30BySession_IdOrderByCreatedAtAsc(Long sessionId);
}
