package com.openquartz.sequence.starter.worker;

import com.openquartz.sequence.core.uid.snowflake.worker.WorkerIdAssigner;
import com.openquartz.sequence.generator.common.utils.RandomUtils;

/**
 * 随机本地生产workerId
 *
 * @author svnee
 **/
public class RandomWorkerIdAssigner implements WorkerIdAssigner {

    private final WorkerIdAssignerProperty property;

    public RandomWorkerIdAssigner(WorkerIdAssignerProperty property) {
        this.property = property;
    }

    @Override
    public long assignWorkerId() {
        return RandomUtils.nextInt((int) property.getMinWorkerId(), (int) property.getMaxWorkerId());
    }
}
