package com.openquartz.sequence.core.expr.persist.model;

import com.openquartz.sequence.core.expr.exception.SequenceGenerateExceptionCode;
import com.openquartz.sequence.generator.common.exception.Asserts;
import com.openquartz.sequence.generator.common.exception.EasySequenceException;
import com.openquartz.sequence.generator.common.utils.CollectionUtils;
import java.util.concurrent.ConcurrentLinkedDeque;
import lombok.Data;

/**
 * Sequence Pool
 *
 * @author svnee
 */
@Data
public class SequencePool {

    /**
     * pool-name
     */
    private String poolName;

    /**
     * Low water threshold
     */
    private int lowWaterLevelThreshold;

    /**
     * A collection of sequence number sequence bucket
     */
    private ConcurrentLinkedDeque<SequenceBucket> sequenceBuckets;

    public SequencePool(String poolName) {
        this.poolName = poolName;
        sequenceBuckets = new ConcurrentLinkedDeque<>();
    }

    public synchronized boolean isLowWaterLevel() {
        if (CollectionUtils.isEmpty(sequenceBuckets)) {
            return true;
        }
        SequenceBucket lastBucket = sequenceBuckets.getLast();
        return lastBucket.getEndSeq() - lastBucket.getCurrentSeq() <= lowWaterLevelThreshold;
    }

    public synchronized void addSequenceBucket(SequenceBucket sequenceBucket) {
        sequenceBuckets.add(sequenceBucket);
    }

    public synchronized Long getAndIncrement() {
        ensureFirstElementAvailable();
        SequenceBucket sequenceBucket = sequenceBuckets.peek();
        Asserts.notNull(sequenceBucket, SequenceGenerateExceptionCode.SEQUENCE_BUCKET_NOT_EXIST_ERROR, poolName);
        return sequenceBucket.getAndIncrement();
    }

    /**
     * ensure first bucket is available
     */
    private void ensureFirstElementAvailable() {
        clearUseUpAndExpired();
        SequenceBucket sequenceBucket = sequenceBuckets.peek();
        if (sequenceBucket == null) {
            throw EasySequenceException
                .replacePlaceHold(SequenceGenerateExceptionCode.SEQUENCE_BUCKET_USE_UP_ERROR, poolName);
        }
    }

    /**
     * clean bucket if use up or expire
     */
    private void clearUseUpAndExpired() {
        while (true) {
            SequenceBucket sequenceBucket = sequenceBuckets.peek();
            if (sequenceBucket == null || sequenceBucket.isAvailable()) {
                break;
            }
            sequenceBuckets.poll();
        }
    }

    /**
     * clean and check enough
     *
     * @return is enough
     */
    public synchronized boolean clearAndCheckEnough() {
        clearUseUpAndExpired();
        return !sequenceBuckets.isEmpty();
    }

}
