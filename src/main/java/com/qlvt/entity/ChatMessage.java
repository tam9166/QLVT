package com.qlvt.entity;

import com.qlvt.enums.ChatIntent;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private ChatSession session;
    @Column(nullable = false, length = 20)
    private String senderType;
    @Column(columnDefinition = "nvarchar(2000)")
    private String message;
    @Enumerated(EnumType.STRING)
    private ChatIntent intent = ChatIntent.UNKNOWN;
    @Column(columnDefinition = "nvarchar(4000)")
    private String response;
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public ChatSession getSession() { return session; }
    public void setSession(ChatSession session) { this.session = session; }
    public String getSenderType() { return senderType; }
    public void setSenderType(String senderType) { this.senderType = senderType; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public ChatIntent getIntent() { return intent; }
    public void setIntent(ChatIntent intent) { this.intent = intent; }
    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
