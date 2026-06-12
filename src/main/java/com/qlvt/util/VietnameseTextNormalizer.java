package com.qlvt.util;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Locale;

public final class VietnameseTextNormalizer {
    private VietnameseTextNormalizer() {
    }

    public static String removeAccent(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replace('đ', 'd')
                .replace('Đ', 'D');
        return normalized;
    }

    public static String normalizeSearchText(String value) {
        return removeAccent(value)
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    public static boolean containsAnyKeyword(String text, String... keywords) {
        String normalized = normalizeSearchText(text);
        return Arrays.stream(keywords).map(VietnameseTextNormalizer::normalizeSearchText).anyMatch(normalized::contains);
    }

    public static boolean fuzzyMatch(String text, String target) {
        String left = normalizeSearchText(text);
        String right = normalizeSearchText(target);
        if (left.isBlank() || right.isBlank()) {
            return false;
        }
        return left.contains(right) || right.contains(left) || levenshtein(left, right) <= Math.max(2, Math.min(left.length(), right.length()) / 5);
    }

    private static int levenshtein(String a, String b) {
        int[] costs = new int[b.length() + 1];
        for (int j = 0; j < costs.length; j++) {
            costs[j] = j;
        }
        for (int i = 1; i <= a.length(); i++) {
            costs[0] = i;
            int nw = i - 1;
            for (int j = 1; j <= b.length(); j++) {
                int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]), a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1);
                nw = costs[j];
                costs[j] = cj;
            }
        }
        return costs[b.length()];
    }
}
