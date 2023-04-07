package com.openquartz.sequence.core.expr.executors;

import com.openquartz.sequence.core.expr.cmd.Environment;
import com.openquartz.sequence.core.expr.cmd.Command;
import com.openquartz.sequence.core.expr.cmd.Result;
import com.openquartz.sequence.generator.common.exception.Asserts;
import com.openquartz.sequence.generator.common.exception.CommonErrorCode;

/**
 * @author svnee
 */
public class ConstExecutor implements CommandExecutor {

    @Override
    public Result exec(Command command) {
        return Result.success(command.getParams().get(0));
    }

    @Override
    public void validate(Command command) {
        Asserts.notNull(command, CommonErrorCode.PARAM_ILLEGAL_ERROR);
        Asserts.notNull(command.getParams(), CommonErrorCode.PARAM_ILLEGAL_ERROR);
        Asserts.isTrue(!command.getParams().isEmpty(), CommonErrorCode.PARAM_ILLEGAL_ERROR);
    }

    @Override
    public void init(Environment environment) {

    }

}
