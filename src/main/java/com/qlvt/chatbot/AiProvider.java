package com.qlvt.chatbot;

import com.qlvt.enums.ChatIntent;

public interface AiProvider {
    ChatIntent detectIntent(String message);
}
