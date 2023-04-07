package com.openquartz.sequence.core.expr.executors;

import com.google.common.collect.Lists;
import com.openquartz.sequence.core.expr.cmd.Environment;
import com.openquartz.sequence.core.expr.cmd.Command;
import com.openquartz.sequence.core.expr.cmd.Result;
import com.openquartz.sequence.generator.common.exception.Asserts;
import com.openquartz.sequence.generator.common.exception.CommonErrorCode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @author svnee
 */
public class TimeExecutor implements CommandExecutor {

    private static final List<String> AVAILABLE_TIME_FORMATS = Lists.newArrayList(
        "yyyyMMdd",
        "yyyyMMddHHmmss",
        "yyyy-MM-dd",
        "yyyyMMddHHmmssSSS",
        "yyMMddHHmmss",
        "yyMMdd"
    );

    @Override
    public Result exec(Command command) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(command.getParams().get(0));
        String formatResult = formatter.format(LocalDateTime.now());
        return Result.success(formatResult);
    }

    @Override
    public void validate(Command command) {
        Asserts.notNull(command, CommonErrorCode.PARAM_ILLEGAL_ERROR);
        Asserts.notNull(command.getParams(), CommonErrorCode.PARAM_ILLEGAL_ERROR);
        Asserts.isTrue(!command.getParams().isEmpty(), CommonErrorCode.PARAM_ILLEGAL_ERROR);
        Asserts
            .isTrue(AVAILABLE_TIME_FORMATS.contains(command.getParams().get(0)), CommonErrorCode.PARAM_ILLEGAL_ERROR);
    }

    @Override
    public void init(Environment environment) {

    }

}
