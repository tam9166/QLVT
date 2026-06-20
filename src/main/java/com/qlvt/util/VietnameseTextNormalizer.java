package com.qlvt.util;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

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
        return left.contains(right)
                || right.contains(left)
                || tokenFuzzyMatch(left, right)
                || levenshtein(left, right) <= Math.max(2, Math.min(left.length(), right.length()) / 5);
    }

    private static boolean tokenFuzzyMatch(String left, String right) {
        Set<String> leftTokens = tokens(left);
        Set<String> rightTokens = tokens(right);
        if (leftTokens.isEmpty() || rightTokens.isEmpty()) {
            return false;
        }
        Set<String> smaller = leftTokens.size() <= rightTokens.size() ? leftTokens : rightTokens;
        Set<String> larger = leftTokens.size() <= rightTokens.size() ? rightTokens : leftTokens;
        int matched = 0;
        for (String token : smaller) {
            if (larger.contains(token) || larger.stream().anyMatch(candidate -> levenshtein(token, candidate) <= Math.max(1, token.length() / 4))) {
                matched++;
            }
        }
        return matched >= Math.max(1, smaller.size() - 1);
    }

    private static Set<String> tokens(String text) {
        Set<String> tokens = new LinkedHashSet<>();
        for (String token : text.split(" ")) {
            if (token.length() >= 2) {
                tokens.add(token);
            }
        }
        return tokens;
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
