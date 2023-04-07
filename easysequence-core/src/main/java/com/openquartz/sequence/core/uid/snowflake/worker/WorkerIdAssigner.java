package com.openquartz.sequence.core.uid.snowflake.worker;

/**
 * Represents a worker id assigner for
 *
 * @author svnee
 */
public interface WorkerIdAssigner {

    /**
     * Assign worker id for
     *
     * @return assigned worker id
     */
    long assignWorkerId();

}
