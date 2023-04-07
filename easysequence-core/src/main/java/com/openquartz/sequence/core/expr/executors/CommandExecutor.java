package com.openquartz.sequence.core.expr.executors;

import com.openquartz.sequence.core.expr.cmd.Environment;
import com.openquartz.sequence.core.expr.cmd.Command;
import com.openquartz.sequence.core.expr.cmd.Result;

/**
 * @author svnee
 */
public interface CommandExecutor {

    /**
     * 执行
     */
    Result exec(Command command);

    /**
     * 校验
     */
    void validate(Command command);

    /**
     * 初始化
     */
    void init(Environment environment);
}
