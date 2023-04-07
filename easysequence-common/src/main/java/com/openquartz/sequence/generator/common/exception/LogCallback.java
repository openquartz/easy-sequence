package com.openquartz.sequence.generator.common.exception;

/**
 * LogConsumer
 *
 * @author svnee
 **/
@FunctionalInterface
public interface LogCallback {

    /**
     * log
     */
    void log();
}
