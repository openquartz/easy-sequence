package com.openquartz.sequence.starter.transaction;

import java.util.concurrent.Callable;

/**
 * transaction support
 *
 * @author svnee
 */
public interface TransactionSupport {

    /**
     * function
     *
     * @param callable callable
     * @param <T> t
     * @return t
     */
    <T> T call(Callable<T> callable);

}
