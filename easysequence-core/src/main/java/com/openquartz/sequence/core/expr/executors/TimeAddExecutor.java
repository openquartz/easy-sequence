package com.openquartz.sequence.core.expr.executors;


import com.google.common.collect.Sets;
import com.openquartz.sequence.core.expr.cmd.Command;
import com.openquartz.sequence.core.expr.cmd.Environment;
import com.openquartz.sequence.core.expr.cmd.Result;
import com.openquartz.sequence.generator.common.exception.Asserts;
import com.openquartz.sequence.generator.common.exception.CommonErrorCode;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;


/**
 * 功能：时间增加
 * 格式为：time_add 数值 单位 输出格式
 * 例如：time_add 1 Y yyyy-MM-dd
 */
public class TimeAddExecutor extends TimeFormatExecutor {

    /**
     * 时间单位
     */
    private static final Set<String> TIME_UNIT = Sets.newHashSet("Y", "M", "D", "H", "m", "s");

    @Override
    public Result exec(Command command) {

        LocalDateTime localTime = LocalDateTime.now();

        // 增加时间操作
        localTime = addTimeOperate(command, localTime);
        return Result.success(formatTime(localTime, command.getParams().get(2)));
    }

    private static LocalDateTime addTimeOperate(Command command, LocalDateTime localTime) {
        switch (command.getParams().get(1)) {
            case "Y":
                localTime = localTime.plusYears(Long.parseLong(command.getParams().get(1)));
                break;
            case "M":
                localTime = localTime.plusMonths(Long.parseLong(command.getParams().get(1)));
                break;
            case "D":
                localTime = localTime.plusDays(Long.parseLong(command.getParams().get(1)));
                break;
            case "H":
                localTime = localTime.plusHours(Long.parseLong(command.getParams().get(1)));
                break;
            case "m":
                localTime = localTime.plusMinutes(Long.parseLong(command.getParams().get(1)));
                break;
            case "s":
                localTime = localTime.plusSeconds(Long.parseLong(command.getParams().get(1)));
                break;
            default:
                throw new IllegalArgumentException("time unit error");
        }
        return localTime;
    }

    @Override
    public void validate(Command command) {
        Asserts.notNull(command, CommonErrorCode.PARAM_ILLEGAL_ERROR);
        Asserts.notNull(command.getParams(), CommonErrorCode.PARAM_ILLEGAL_ERROR);
        Asserts.isTrue(!command.getParams().isEmpty(), CommonErrorCode.PARAM_ILLEGAL_ERROR);
        Asserts.isTrue(Objects.nonNull(command.getParams().get(0)), CommonErrorCode.PARAM_ILLEGAL_ERROR);
        Asserts.isTrue(TIME_UNIT.contains(command.getParams().get(1)), CommonErrorCode.PARAM_ILLEGAL_ERROR);
        checkTimePattern(command.getParams().get(2));
    }

    @Override
    public void init(Environment environment) {

    }

}
