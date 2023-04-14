package com.openquartz.sequence.generator.common.transaction;

/**
 * TransactionCallback
 *
 * @param <T> T
 * @author svnee
 */
@FunctionalInterface
public interface InTransactionCallback<T> {

    /**
     * 事务内執行
     *
     * @return T
     */
    T doInTransaction();

}
