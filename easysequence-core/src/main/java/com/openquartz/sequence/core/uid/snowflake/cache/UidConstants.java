package com.openquartz.sequence.core.uid.snowflake.cache;


/**
 * UidConstants
 *
 * @author svnee
 */
public final class UidConstants {

    private UidConstants() {
    }

    /**
     * time bits
     */
    public static final int TIME_BITS = 40;

    /**
     * workerId bits
     */
    public static final int WORKER_BITS = 10;

    /**
     * serial number
     */
    public static final int SEQ_BITS = 13;

}
