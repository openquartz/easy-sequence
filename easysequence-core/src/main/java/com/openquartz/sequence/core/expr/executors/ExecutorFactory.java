package com.openquartz.sequence.core.expr.executors;

import com.openquartz.sequence.core.expr.cmd.Environment;
import java.util.HashMap;
import java.util.Map;

/**
 * @author svnee
 */
public class ExecutorFactory {

    private final ExecutorRegistry executorRegistry;
    private Environment environment;
    private final Map<String, CommandExecutor> cache = new HashMap<>();

    public ExecutorFactory(ExecutorRegistry executorRegistry) {
        this.executorRegistry = executorRegistry;
    }

    public CommandExecutor getExecutor(String cmdName) {
        if (cache.containsKey(cmdName)) {
            return cache.get(cmdName);
        }
        Class<? extends CommandExecutor> clazz = executorRegistry.getExecutorClass(cmdName);
        CommandExecutor executor = null;
        try {
            executor = clazz.getConstructor().newInstance();
            executor.init(environment);
        } catch (Exception e) {
            if (executor != null) {
                executor = null;
            }
        }
        if (executor != null) {
            cache.put(cmdName, executor);
        }
        return executor;
    }

    public ExecutorRegistry getExecutorRegistry() {
        return executorRegistry;
    }

    public void bindEnvironment(Environment environment) {
        this.environment = environment;
    }

}
