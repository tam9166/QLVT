package com.qlvt;

import com.qlvt.chatbot.RuleBasedAiProvider;
import com.qlvt.enums.ChatIntent;
import com.qlvt.util.VietnameseTextNormalizer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChatbotRuleTest {
    @Test
    void normalizerSupportsVietnameseWithAndWithoutAccent() {
        assertEquals("gang tay size m con khong", VietnameseTextNormalizer.normalizeSearchText("Găng tay size M còn không?"));
        assertTrue(VietnameseTextNormalizer.fuzzyMatch("gang tay size m", "Găng tay y tế không bột size M"));
    }

    @Test
    void ruleEngineDetectsNaturalInventoryQuestions() {
        RuleBasedAiProvider provider = new RuleBasedAiProvider();

        assertEquals(ChatIntent.CHECK_STOCK, provider.detectIntent("VT001 còn bao nhiêu"));
        assertEquals(ChatIntent.CHECK_STOCK, provider.detectIntent("kho còn hàng không"));
        assertEquals(ChatIntent.CHECK_LOCATION, provider.detectIntent("nó nằm ở đâu"));
        assertEquals(ChatIntent.CHECK_BATCH, provider.detectIntent("lô ABC123 hạn dùng khi nào"));
        assertEquals(ChatIntent.CHECK_REQUEST_STATUS, provider.detectIntent("phiếu của tôi tới đâu rồi"));
        assertEquals(ChatIntent.CHECK_DEPARTMENT_STOCK, provider.detectIntent("khoa tôi còn gì"));
    }

    @Test
    void ruleEngineRejectsMedicalAdvice() {
        RuleBasedAiProvider provider = new RuleBasedAiProvider();

        assertEquals(ChatIntent.UNKNOWN, provider.detectIntent("thuốc này liều dùng bao nhiêu"));
        assertEquals(ChatIntent.UNKNOWN, provider.detectIntent("bệnh này điều trị thế nào"));
    }
}
