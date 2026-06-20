package com.qlvt.service;

import com.qlvt.entity.Material;
import com.qlvt.repository.MaterialRepository;
import com.qlvt.util.VietnameseTextNormalizer;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class MaterialSearchService {
    private static final Map<String, List<String>> MEDICAL_ALIASES = Map.ofEntries(
            Map.entry("khau trang", List.of("mask", "khau trang y te", "kt y te")),
            Map.entry("gang tay", List.of("bao tay", "glove", "gang tay y te")),
            Map.entry("day truyen dich", List.of("bo dich truyen", "bo day truyen", "day truyen", "dich truyen")),
            Map.entry("kim tiem", List.of("bom kim tiem", "needle")),
            Map.entry("bom tiem", List.of("syringe", "bom tiem 5ml", "bom tiem 10ml")),
            Map.entry("bong gac", List.of("gac y te", "gauze", "bong bang")),
            Map.entry("con sat trung", List.of("con", "alcohol", "dung dich sat trung")),
            Map.entry("nuoc muoi sinh ly", List.of("nacl", "natri clorid", "saline")),
            Map.entry("bang cuon", List.of("bang y te", "bandage")),
            Map.entry("nhiet ke", List.of("thermometer")),
            Map.entry("que thu duong huyet", List.of("que duong huyet", "test strip"))
    );

    private static final Set<String> STOP_WORDS = Set.of(
            "con", "bao", "nhieu", "may", "cai", "bo", "hop", "goi", "o", "dau", "nam", "ke",
            "kho", "nao", "cho", "toi", "minh", "em", "anh", "chi", "vat", "tu", "hang",
            "khong", "co", "can", "lay", "xem", "ngay", "nhap", "han", "dung", "hsd",
            "sap", "het", "trong", "gan", "nhat", "la", "lo", "ma", "so", "va"
    );

    private final MaterialRepository materialRepository;

    public MaterialSearchService(MaterialRepository materialRepository) {
        this.materialRepository = materialRepository;
    }

    public List<Material> search(String query, int limit) {
        return rankedMatches(query, limit).stream()
                .map(MaterialMatch::material)
                .toList();
    }

    public List<MaterialMatch> rankedMatches(String query, int limit) {
        String normalized = expandAliases(VietnameseTextNormalizer.normalizeSearchText(query));
        if (normalized.isBlank()) {
            return List.of();
        }
        return materialRepository.findByDeletedFalseOrderByCodeAsc().stream()
                .map(material -> new MaterialMatch(material, score(material, normalized)))
                .filter(match -> match.score() >= 24)
                .sorted(Comparator.comparingInt(MaterialMatch::score).reversed()
                        .thenComparing(match -> match.material().getCode()))
                .limit(limit)
                .toList();
    }

    public List<Material> alternatives(Material material, int limit) {
        String category = VietnameseTextNormalizer.normalizeSearchText(material.getCategory());
        return materialRepository.findByDeletedFalseOrderByCodeAsc().stream()
                .filter(candidate -> !candidate.getId().equals(material.getId()))
                .filter(candidate -> candidate.getAvailableQuantity() > 0)
                .filter(candidate -> VietnameseTextNormalizer.normalizeSearchText(candidate.getCategory()).equals(category)
                        || VietnameseTextNormalizer.fuzzyMatch(candidate.getName(), material.getName()))
                .limit(limit)
                .toList();
    }

    private int score(Material material, String normalizedQuery) {
        String code = VietnameseTextNormalizer.normalizeSearchText(material.getCode());
        String name = expandAliases(VietnameseTextNormalizer.normalizeSearchText(material.getName()));
        String rawAlias = VietnameseTextNormalizer.normalizeSearchText(material.getAliasText());
        String alias = expandAliases(rawAlias);
        String category = expandAliases(VietnameseTextNormalizer.normalizeSearchText(material.getCategory()));
        String searchable = (code + " " + name + " " + alias + " " + category).trim();

        int score = 0;
        if (!code.isBlank() && normalizedQuery.contains(code)) {
            score += 120;
        }
        if (!name.isBlank() && normalizedQuery.contains(name)) {
            score += 100;
        }
        if (!name.isBlank() && name.contains(normalizedQuery)) {
            score += 80;
        }
        if (!alias.isBlank() && containsPhraseOverlap(normalizedQuery, alias)) {
            score += 72;
        }
        if (!rawAlias.isBlank() && containsPhraseOverlap(normalizedQuery, rawAlias)) {
            score += 55;
        }
        if (!category.isBlank() && containsPhraseOverlap(normalizedQuery, category)) {
            score += 25;
        }

        Set<String> queryTokens = contentTokens(normalizedQuery);
        Set<String> materialTokens = contentTokens(searchable);
        if (!queryTokens.isEmpty() && !materialTokens.isEmpty()) {
            int overlap = 0;
            for (String token : queryTokens) {
                if (materialTokens.contains(token)) {
                    overlap++;
                } else if (materialTokens.stream().anyMatch(materialToken -> tokenClose(token, materialToken))) {
                    overlap++;
                }
            }
            score += (int) Math.round(60.0 * overlap / Math.max(1, Math.min(queryTokens.size(), materialTokens.size())));
        }

        if (VietnameseTextNormalizer.fuzzyMatch(normalizedQuery, name)) {
            score += 35;
        }
        return score;
    }

    private boolean containsPhraseOverlap(String query, String phraseText) {
        for (String phrase : phraseText.split("\\s*,\\s*|\\s*;\\s*")) {
            String normalizedPhrase = VietnameseTextNormalizer.normalizeSearchText(phrase);
            if (!normalizedPhrase.isBlank() && (query.contains(normalizedPhrase) || normalizedPhrase.contains(query))) {
                return true;
            }
        }
        return false;
    }

    private Set<String> contentTokens(String text) {
        Set<String> tokens = new LinkedHashSet<>();
        for (String token : VietnameseTextNormalizer.normalizeSearchText(text).split(" ")) {
            if (token.length() >= 2 && !STOP_WORDS.contains(token)) {
                tokens.add(token);
            }
        }
        return tokens;
    }

    public static String expandAliases(String normalizedText) {
        if (normalizedText == null || normalizedText.isBlank()) {
            return "";
        }
        List<String> extra = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : MEDICAL_ALIASES.entrySet()) {
            String canonical = entry.getKey();
            if (normalizedText.contains(canonical) || containsFuzzyPhrase(normalizedText, canonical)) {
                extra.addAll(entry.getValue());
                continue;
            }
            for (String alias : entry.getValue()) {
                String normalizedAlias = VietnameseTextNormalizer.normalizeSearchText(alias);
                if (normalizedText.contains(normalizedAlias) || containsFuzzyPhrase(normalizedText, normalizedAlias)) {
                    extra.add(canonical);
                    extra.addAll(entry.getValue());
                    break;
                }
            }
        }
        if (extra.isEmpty()) {
            return normalizedText;
        }
        return (normalizedText + " " + String.join(" ", extra)).replaceAll("\\s+", " ").trim();
    }

    private static boolean containsFuzzyPhrase(String text, String phrase) {
        Set<String> textTokens = new LinkedHashSet<>(List.of(VietnameseTextNormalizer.normalizeSearchText(text).split(" ")));
        Set<String> phraseTokens = new LinkedHashSet<>(List.of(VietnameseTextNormalizer.normalizeSearchText(phrase).split(" ")));
        phraseTokens.removeIf(String::isBlank);
        if (phraseTokens.isEmpty()) {
            return false;
        }
        int matched = 0;
        for (String phraseToken : phraseTokens) {
            if (textTokens.stream().anyMatch(textToken -> textToken.equals(phraseToken) || tokenClose(textToken, phraseToken))) {
                matched++;
            }
        }
        return matched >= phraseTokens.size();
    }

    private static boolean tokenClose(String left, String right) {
        String a = VietnameseTextNormalizer.normalizeSearchText(left);
        String b = VietnameseTextNormalizer.normalizeSearchText(right);
        if (a.equals(b)) {
            return true;
        }
        int min = Math.min(a.length(), b.length());
        if (min <= 3) {
            return false;
        }
        int distance = levenshtein(a, b);
        return distance <= (min <= 5 ? 1 : 2);
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

    public record MaterialMatch(Material material, int score) {
    }
}
