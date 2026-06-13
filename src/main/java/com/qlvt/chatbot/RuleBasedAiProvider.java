package com.qlvt.chatbot;

import com.qlvt.enums.ChatIntent;
import com.qlvt.util.VietnameseTextNormalizer;
import org.springframework.stereotype.Component;

@Component
public class RuleBasedAiProvider implements AiProvider {
    @Override
    public ChatIntent detectIntent(String message) {
        String text = VietnameseTextNormalizer.normalizeSearchText(message);
        if (text.isBlank() || VietnameseTextNormalizer.containsAnyKeyword(text, "help", "huong dan", "tro giup", "ban lam duoc gi", "hoi nhu the nao")) return ChatIntent.HELP;
        if (VietnameseTextNormalizer.containsAnyKeyword(text, "lieu dung", "dieu tri", "uong bao nhieu", "tiem bao nhieu", "chi dinh", "chan doan", "thuoc gi")) return ChatIntent.UNKNOWN;
        if (VietnameseTextNormalizer.containsAnyKeyword(text, "tao phieu", "tao phieu xuat", "xin", "cap phieu", "lap phieu", "lay thuoc", "linh thuoc", "xuat kho cho khoa")) return ChatIntent.CREATE_REQUEST_DRAFT;
        if (VietnameseTextNormalizer.containsAnyKeyword(text, "phieu cua toi", "yeu cau cua toi", "trang thai phieu", "da duyet chua", "phieu den dau", "phieu xu ly chua")) return ChatIntent.CHECK_REQUEST_STATUS;
        if (VietnameseTextNormalizer.containsAnyKeyword(text, "da nhan", "lich su nhan", "nhan gan day", "khoa vua nhan gi")) return ChatIntent.CHECK_RECEIVED_HISTORY;
        if (VietnameseTextNormalizer.containsAnyKeyword(text, "ton tai khoa", "ton khoa", "khoa con bao nhieu", "vat tu tai khoa", "trong khoa con gi", "khoa dang giu")) return ChatIntent.CHECK_DEPARTMENT_STOCK;
        if (VietnameseTextNormalizer.containsAnyKeyword(text, "sap het han tai khoa", "sap het han tai kho", "canh bao het han", "lo nao sap het han", "hang nao sap het han")) return ChatIntent.CHECK_DEPARTMENT_EXPIRING_MATERIALS;
        if (VietnameseTextNormalizer.containsAnyKeyword(text, "nha cung cap", "supplier", "cong ty cung cap")) return ChatIntent.CHECK_SUPPLIER;
        if (VietnameseTextNormalizer.containsAnyKeyword(text, "lo ", "so lo", "batch")) return ChatIntent.CHECK_BATCH;
        if (VietnameseTextNormalizer.containsAnyKeyword(text, "han dung", "het han", "hsd", "expiry")) return ChatIntent.CHECK_EXPIRY;
        if (VietnameseTextNormalizer.containsAnyKeyword(text, "o dau", "nam o dau", "vi tri", "ke", "tu do", "tu thuoc", "ngan tu", "ngan")) return ChatIntent.CHECK_LOCATION;
        if (VietnameseTextNormalizer.containsAnyKeyword(text, "con khong", "con bao nhieu", "con hang khong", "het hang chua", "het kho chua", "ton", "kha dung", "stock")) return ChatIntent.CHECK_STOCK;
        if (VietnameseTextNormalizer.containsAnyKeyword(text, "thay the", "tuong duong")) return ChatIntent.SUGGEST_ALTERNATIVE;
        return ChatIntent.SEARCH_MATERIAL;
    }
}
