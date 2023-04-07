package com.openquartz.sequence.starter.transaction;

import com.openquartz.sequence.generator.common.utils.ExceptionUtils;
import java.util.concurrent.Callable;
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
    public <T> T call(Callable<T> callable) {
        return transactionTemplate.execute(action -> {
            try {
                return callable.call();
            } catch (Exception exception) {
                return ExceptionUtils.rethrow(exception);
            }
        });
    }


}
