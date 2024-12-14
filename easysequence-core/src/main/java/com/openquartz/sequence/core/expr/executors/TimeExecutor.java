package com.openquartz.sequence.core.expr.executors;

import com.openquartz.sequence.core.expr.cmd.Environment;
import com.openquartz.sequence.core.expr.cmd.Command;
import com.openquartz.sequence.core.expr.cmd.Result;
import com.openquartz.sequence.generator.common.exception.Asserts;
import com.openquartz.sequence.generator.common.exception.CommonErrorCode;

import java.time.LocalDateTime;

/**
 * 功能：输出当前时间
 * 格式：time 时间格式
 *
 * @author svnee
 */
public class TimeExecutor extends TimeFormatExecutor {

    @Override
    public Result exec(Command command) {
        LocalDateTime localTime = LocalDateTime.now();
        return Result.success(formatTime(localTime, command.getParams().get(0)));
    }

    @Override
    public void validate(Command command) {
        Asserts.notNull(command, CommonErrorCode.PARAM_ILLEGAL_ERROR);
        Asserts.notNull(command.getParams(), CommonErrorCode.PARAM_ILLEGAL_ERROR);
        Asserts.isTrue(!command.getParams().isEmpty(), CommonErrorCode.PARAM_ILLEGAL_ERROR);
        checkTimePattern(command.getParams().get(0));
    }

    @Override
    public void init(Environment environment) {

    }

}
