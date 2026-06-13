package com.qlvt.chatbot;

import com.qlvt.enums.ChatIntent;
import com.qlvt.util.VietnameseTextNormalizer;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class RuleBasedAiProvider implements AiProvider {
    private static final Pattern BATCH_CODE_PATTERN = Pattern.compile("(^|\\s)(lo|lot)[-\\s]?[a-z0-9]+");

    @Override
    public ChatIntent detectIntent(String message) {
        String text = VietnameseTextNormalizer.normalizeSearchText(message);
        if (text.isBlank() || has(text,
                "help", "huong dan", "tro giup", "ban lam duoc gi", "hoi nhu the nao",
                "cach hoi", "goi y", "chatbot", "tro ly", "minh hoi gi duoc", "ban ho tro gi")) {
            return ChatIntent.HELP;
        }
        if (has(text,
                "lieu dung", "cach dung thuoc", "dieu tri", "uong bao nhieu", "uong the nao",
                "uong thuoc", "thuoc nay", "tiem bao nhieu", "tiem thuoc", "bao nhieu lan",
                "tac dung phu", "chi dinh", "chan doan", "thuoc gi", "dung cho benh")) {
            return ChatIntent.UNKNOWN;
        }
        if (has(text,
                "tao phieu", "tao phieu xuat", "tao yeu cau", "lap phieu", "lap yeu cau",
                "can xin", "muon xin", "xin cap", "de nghi cap", "cap them", "lay them",
                "lay vat tu", "lay thuoc", "linh thuoc", "xuat kho cho khoa")) {
            return ChatIntent.CREATE_REQUEST_DRAFT;
        }
        if (has(text,
                "phieu cua toi", "phieu cua em", "don cua toi", "yeu cau cua toi", "yeu cau toi dau",
                "trang thai phieu", "da duyet chua", "da xuat chua", "da nhan chua",
                "kho duyet chua", "truong khoa duyet chua", "phieu den dau", "phieu xu ly chua")) {
            return ChatIntent.CHECK_REQUEST_STATUS;
        }
        if (has(text,
                "da nhan", "da cap phat", "lich su nhan", "lich su cap phat",
                "nhan gan day", "khoa vua nhan gi", "da nhan nhung gi", "vat tu da cap")) {
            return ChatIntent.CHECK_RECEIVED_HISTORY;
        }
        if (has(text,
                "ton tai khoa", "ton o khoa", "ton khoa", "khoa con bao nhieu", "vat tu tai khoa",
                "vat tu khoa toi", "trong khoa con gi", "khoa con gi", "khoa dang giu")) {
            return ChatIntent.CHECK_DEPARTMENT_STOCK;
        }
        if (has(text,
                "sap het han tai khoa", "sap het han tai kho", "canh bao het han", "lo nao sap het han",
                "hang nao sap het han", "gan het han", "can date", "sap het date", "gan het date", "lo can uu tien")) {
            return ChatIntent.CHECK_DEPARTMENT_EXPIRING_MATERIALS;
        }
        if (has(text, "nha cung cap", "supplier", "cong ty cung cap", "ai cung cap", "mua o dau", "nha thau", "don vi cung cap")) {
            return ChatIntent.CHECK_SUPPLIER;
        }
        if (has(text, "so lo", "ma lo", "batch", "lot") || BATCH_CODE_PATTERN.matcher(text).find()) {
            return ChatIntent.CHECK_BATCH;
        }
        if (has(text, "han dung", "het han", "hsd", "expiry", "date", "ngay het han", "con han khong", "han khi nao")) {
            return ChatIntent.CHECK_EXPIRY;
        }
        if (has(text,
                "o dau", "de dau", "nam o dau", "vi tri", "vi tri nao", "ke nao",
                "tu do", "tu thuoc", "ngan tu", "ngan nao", "o kho nao", "kho nao")) {
            return ChatIntent.CHECK_LOCATION;
        }
        if (has(text,
                "con khong", "con bao nhieu", "con nhieu khong", "con may", "con nhieu",
                "con cap duoc", "con cap duoc bao nhieu", "cap duoc bao nhieu",
                "con hang khong", "con du khong", "co san khong", "cap duoc khong",
                "het hang chua", "het kho chua", "ton kho", "ton", "kha dung", "stock", "so luong con")) {
            return ChatIntent.CHECK_STOCK;
        }
        if (has(text, "thay the", "tuong duong", "doi sang", "loai khac", "het thi dung gi", "vat tu tuong tu")) {
            return ChatIntent.SUGGEST_ALTERNATIVE;
        }
        return ChatIntent.SEARCH_MATERIAL;
    }

    private boolean has(String text, String... keywords) {
        return VietnameseTextNormalizer.containsAnyKeyword(text, keywords);
    }
}
