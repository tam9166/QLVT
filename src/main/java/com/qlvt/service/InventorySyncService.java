package com.qlvt.service;

import com.qlvt.entity.Material;
import com.qlvt.repository.MaterialBatchRepository;
import com.qlvt.repository.MaterialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class InventorySyncService {
    private final MaterialRepository materialRepository;
    private final MaterialBatchRepository batchRepository;

    public InventorySyncService(MaterialRepository materialRepository, MaterialBatchRepository batchRepository) {
        this.materialRepository = materialRepository;
        this.batchRepository = batchRepository;
    }

    @Transactional(readOnly = true)
    public int calculateActualQuantity(Long materialId) {
        return batchRepository.sumIssuableQuantityByMaterialId(materialId, LocalDate.now());
    }

    @Transactional
    public int syncMaterialActualQuantity(Material material) {
        int syncedQuantity = calculateActualQuantity(material.getId());
        material.setActualQuantity(syncedQuantity);
        materialRepository.save(material);
        return syncedQuantity;
    }

    @Transactional
    public void syncAllActiveMaterials() {
        for (Material material : materialRepository.findByDeletedFalseOrderByCodeAsc()) {
            syncMaterialActualQuantity(material);
        }
    }
}
