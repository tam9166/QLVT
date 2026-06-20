package com.qlvt.controller;

import com.qlvt.service.ChatbotService;
import com.qlvt.repository.AppUserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {
    private final ChatbotService chatbotService;
    private final AppUserRepository userRepository;

    public ChatbotController(ChatbotService chatbotService, AppUserRepository userRepository) {
        this.chatbotService = chatbotService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ChatbotService.ChatResponse ask(@RequestBody ChatbotRequest payload, Authentication authentication) {
        return answer(payload, authentication);
    }

    @PostMapping("/message")
    public ChatbotService.ChatResponse message(@RequestBody ChatbotRequest payload, Authentication authentication) {
        return answer(payload, authentication);
    }

    private ChatbotService.ChatResponse answer(ChatbotRequest payload, Authentication authentication) {
        String username = authentication == null ? "anonymous" : authentication.getName();
        String department = userRepository.findByUsername(username).map(user -> user.getDepartment()).orElse(null);
        return chatbotService.answer(payload == null ? "" : payload.message(), username, department);
    }

    @GetMapping("/history")
    public List<Map<String, String>> history(Authentication authentication) {
        String username = authentication == null ? "anonymous" : authentication.getName();
        return chatbotService.recentHistory(username);
    }

    @DeleteMapping("/history")
    public void clear(Authentication authentication) {
        String username = authentication == null ? "anonymous" : authentication.getName();
        chatbotService.clearHistory(username);
    }

    public record ChatbotRequest(String message, Map<String, Object> context) {
    }
}
