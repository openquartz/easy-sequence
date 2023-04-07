package com.openquartz.sequence.core.expr.strategy;

/**
 * ExprExecuteStrategyFactory
 *
 * @author svnee
 */
public final class ExprExecuteStrategyFactory {

    private ExprExecuteStrategyFactory() {
    }

    private static final ExprExecuteStrategy DEFAULT_STRATEGY = new ParseThenExecuteStrategy();

    public static ExprExecuteStrategy getDefaultStrategy() {
        return DEFAULT_STRATEGY;
    }
}
