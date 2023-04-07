package com.openquartz.sequence.core.expr.executors;

import static com.openquartz.sequence.core.expr.exception.CommandExceptionCode.COMMAND_NOT_SUPPORT_ERROR;

import com.openquartz.sequence.generator.common.exception.Asserts;
import java.util.HashMap;
import java.util.Map;

/**
 * @author svnee
 */
public class ExecutorRegistry {

    /**
     * key: command
     * value: command executor
     */
    private final Map<String, Class<? extends CommandExecutor>> cmdMap = new HashMap<>();

    public ExecutorRegistry() {
        // register command
        registerDefault();
    }

    /**
     * command mapping executor
     *
     * @param cmd command
     * @return command executor class
     */
    public Class<? extends CommandExecutor> getExecutorClass(String cmd) {
        Asserts.isTrue(cmdMap.containsKey(cmd), COMMAND_NOT_SUPPORT_ERROR, cmd);
        return cmdMap.get(cmd);
    }

    public void register(String cmdName, Class<? extends CommandExecutor> clazz) {
        cmdMap.put(cmdName, clazz);
    }

    public void register(Map<String, Class<? extends CommandExecutor>> map) {
        cmdMap.putAll(map);
    }

    public void registerDefault() {
        register("const", ConstExecutor.class);
        register("time", TimeExecutor.class);
        register("fix", com.openquartz.sequence.core.expr.executors.FixLengthExecutor.class);
        register("env", EnvExecutor.class);
        register("rand_n", RandomDigitExecutor.class);
        register("rand_c", RandomAlphaNumExecutor.class);
    }
}
