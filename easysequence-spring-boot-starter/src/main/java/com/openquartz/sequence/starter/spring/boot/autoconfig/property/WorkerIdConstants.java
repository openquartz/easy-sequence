package com.openquartz.sequence.starter.spring.boot.autoconfig.property;

/**
 * WorkerIdConstants
 *
 * @author svnee
 **/
public final class WorkerIdConstants {

    private WorkerIdConstants() {
    }

    /**
     * 最小WorkerId
     */
    public static final Integer MIN_WORKER_ID = 0;

    /**
     * 标准模式下 WorkerId
     */
    public static final Integer DEFAULT_MAX_WORKER_ID = 32;

    /**
     * Cache模式下 WorkerId
     */
    public static final Integer CACHE_MAX_WORKER_ID = 1024;

}
