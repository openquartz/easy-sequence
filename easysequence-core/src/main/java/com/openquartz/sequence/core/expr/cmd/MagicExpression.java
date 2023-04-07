package com.openquartz.sequence.core.expr.cmd;

import com.openquartz.sequence.core.expr.executors.CommandExecutor;
import com.openquartz.sequence.core.expr.executors.ExecutorFactory;
import com.openquartz.sequence.core.expr.executors.ExecutorRegistry;
import com.openquartz.sequence.core.expr.strategy.ExprExecuteStrategy;
import com.openquartz.sequence.core.expr.strategy.ExprExecuteStrategyFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * 例如：
 * 表达式： {const DEMO}{fix {env w} 4}{time yyyyMMdd}{fix {seq DEMO_GENERATOR {env w}} 5}
 *
 * @author svnee
 */
public class MagicExpression {

    private ExprExecuteStrategy strategy;
    private Environment environment;

    public String execute(String expr) {
        return execute(expr, null);
    }

    public String execute(String expr, Map<String, String> localContextParams) {
        expr = wrapAsConstCmd(expr);
        if (localContextParams != null) {
            registerLocalContextParams(localContextParams);
        }
        String result = strategy.exec(expr, environment);
        clearLocalContextParams();
        return result;
    }

    private void registerLocalContextParams(Map<String, String> localContextParams) {
        environment.registerLocalContextParam(localContextParams);
    }

    private void clearLocalContextParams() {
        environment.clearLocalContext();
    }

    private static String wrapAsConstCmd(String expr) {
        return String.format("{const %s}", expr);
    }

    private MagicExpression() {

    }

    public static MagicExpressionBuilder builder() {
        return new MagicExpressionBuilder();
    }

    public static class MagicExpressionBuilder {

        private ExprExecuteStrategy strategy = ExprExecuteStrategyFactory.getDefaultStrategy();
        private final Map<String, Class<? extends CommandExecutor>> registerMap = new HashMap<>();
        /**
         * 全局上下文
         */
        private final Map<String, String> globalContextInfo = new HashMap<>();

        public MagicExpressionBuilder setStrategyAsDefault() {
            this.strategy = ExprExecuteStrategyFactory.getDefaultStrategy();
            return this;
        }

        public MagicExpressionBuilder setStrategy(ExprExecuteStrategy strategy) {
            this.strategy = strategy;
            return this;
        }

        public MagicExpressionBuilder registerExecutor(String cmdName, Class<? extends CommandExecutor> clazz) {
            this.registerMap.put(cmdName, clazz);
            return this;
        }

        public MagicExpressionBuilder registerGlobalContextInfos(Map<String, String> globalContextInfo) {
            if (globalContextInfo != null) {
                this.globalContextInfo.putAll(globalContextInfo);
            }
            return this;
        }

        public MagicExpressionBuilder registerGlobalContextInfo(String paramName, String value) {
            if (paramName != null && value != null) {
                this.globalContextInfo.put(paramName, value);
            }
            return this;
        }

        public MagicExpression build() {
            MagicExpression magicExpression = new MagicExpression();
            Environment environment = new Environment();
            environment.setExecutorFactory(new ExecutorFactory(new ExecutorRegistry()));
            if (registerMap.size() > 0) {
                environment.registerExecutor(registerMap);
            }
            if (globalContextInfo.size() > 0) {
                environment.registerGlobalContextParam(globalContextInfo);
            }
            magicExpression.environment = environment;
            if (this.strategy == null) {
                magicExpression.strategy = ExprExecuteStrategyFactory.getDefaultStrategy();
            } else {
                magicExpression.strategy = this.strategy;
            }
            return magicExpression;
        }
    }
}
