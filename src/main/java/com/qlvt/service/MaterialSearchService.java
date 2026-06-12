package com.qlvt.service;

import com.qlvt.entity.Material;
import com.qlvt.repository.MaterialRepository;
import com.qlvt.util.VietnameseTextNormalizer;
import org.springframework.stereotype.Service;
import java.util.Comparator;
import java.util.List;

@Service
public class MaterialSearchService {
    private final MaterialRepository materialRepository;

    public MaterialSearchService(MaterialRepository materialRepository) {
        this.materialRepository = materialRepository;
    }

    public List<Material> search(String query, int limit) {
        String normalized = VietnameseTextNormalizer.normalizeSearchText(query);
        return materialRepository.findByDeletedFalseOrderByCodeAsc().stream()
                .filter(material -> matches(material, normalized))
                .sorted(Comparator.comparing(Material::getCode))
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

    private boolean matches(Material material, String normalizedQuery) {
        if (normalizedQuery.isBlank()) {
            return false;
        }
        String code = VietnameseTextNormalizer.normalizeSearchText(material.getCode());
        String name = VietnameseTextNormalizer.normalizeSearchText(material.getName());
        String alias = VietnameseTextNormalizer.normalizeSearchText(material.getAliasText());
        return normalizedQuery.contains(code)
                || name.contains(normalizedQuery)
                || normalizedQuery.contains(name)
                || alias.contains(normalizedQuery)
                || VietnameseTextNormalizer.fuzzyMatch(normalizedQuery, name);
    }
}
