package com.qlvt;

import com.qlvt.entity.MaterialBatch;
import com.qlvt.enums.BatchStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class BatchIssueRuleTest {
    @Test
    void availableNonExpiredBatchCanBeIssued() {
        MaterialBatch batch = new MaterialBatch();
        batch.setQuantity(10);
        batch.setStatus(BatchStatus.AVAILABLE);
        batch.setExpiryDate(LocalDate.now().plusDays(30));

        assertThat(batch.canIssue(LocalDate.now())).isTrue();
    }

    @Test
    void expiredBatchCannotBeIssued() {
        MaterialBatch batch = new MaterialBatch();
        batch.setQuantity(10);
        batch.setStatus(BatchStatus.AVAILABLE);
        batch.setExpiryDate(LocalDate.now().minusDays(1));

        assertThat(batch.canIssue(LocalDate.now())).isFalse();
    }

    @Test
    void quarantinedRecalledDestroyedOrEmptyBatchCannotBeIssued() {
        for (BatchStatus status : new BatchStatus[]{BatchStatus.QUARANTINED, BatchStatus.RECALLED, BatchStatus.DESTROYED, BatchStatus.EXPIRED}) {
            MaterialBatch batch = new MaterialBatch();
            batch.setQuantity(10);
            batch.setStatus(status);
            batch.setExpiryDate(LocalDate.now().plusDays(30));

            assertThat(batch.canIssue(LocalDate.now())).isFalse();
        }

        MaterialBatch empty = new MaterialBatch();
        empty.setQuantity(0);
        empty.setStatus(BatchStatus.AVAILABLE);
        empty.setExpiryDate(LocalDate.now().plusDays(30));

        assertThat(empty.canIssue(LocalDate.now())).isFalse();
    }
}