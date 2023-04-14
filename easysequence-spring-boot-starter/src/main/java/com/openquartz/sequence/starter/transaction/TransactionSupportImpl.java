package com.openquartz.sequence.starter.transaction;

import static org.springframework.transaction.TransactionDefinition.ISOLATION_DEFAULT;

import com.openquartz.sequence.generator.common.transaction.AfterTransactionCallback;
import com.openquartz.sequence.generator.common.transaction.InTransactionCallback;
import com.openquartz.sequence.generator.common.transaction.TransactionSupport;
import com.openquartz.sequence.generator.common.utils.ExceptionUtils;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * TransactionSupport
 *
 * @author svnee
 **/
public class TransactionSupportImpl implements TransactionSupport {

    private final TransactionTemplate transactionTemplate;

    public TransactionSupportImpl(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public <T> T execute(InTransactionCallback<T> callback) {

        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        transactionTemplate.setIsolationLevel(ISOLATION_DEFAULT);

        return transactionTemplate.execute(action -> {
            try {
                return callback.doInTransaction();
            } catch (Exception exception) {
                return ExceptionUtils.rethrow(exception);
            }
        });
    }

    @Override
    public <T> T executeInNewTransaction(InTransactionCallback<T> callback) {

        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        return transactionTemplate.execute(action -> {
            try {
                return callback.doInTransaction();
            } catch (Exception exception) {
                return ExceptionUtils.rethrow(exception);
            }
        });
    }

    @Override
    public void executeAfterCommit(AfterTransactionCallback callback) {

        // current is active
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    callback.doAfterCommit();
                }
            });
            return;
        }
        // direct-execute
        callback.doAfterCommit();
    }
}
