package com.openquartz.sequence.core.expr.executors;

import com.google.common.collect.Sets;
import com.openquartz.sequence.core.expr.cmd.Environment;
import com.openquartz.sequence.core.expr.cmd.Command;
import com.openquartz.sequence.core.expr.cmd.Result;
import com.openquartz.sequence.generator.common.exception.Asserts;
import com.openquartz.sequence.generator.common.exception.CommonErrorCode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Set;

/**
 * @author svnee
 */
public class TimeExecutor implements CommandExecutor {

    /**
     * 时间戳格式
     * Timestamp: 毫秒时间错
     * Timestamp-s: 秒时间戳
     */
    private static final Set<String> AVAILABLE_TIME_FORMATS = Sets.newHashSet(
        "yyyyMMdd",
        "yyyyMMddHHmmss",
        "yyyy-MM-dd",
        "yyyyMMddHHmmssSSS",
        "yyMMddHHmmss",
        "yyMMdd",
        "Timestamp",
        "Timestamp-s"
    );

    @Override
    public Result exec(Command command) {

        LocalDateTime localTime = LocalDateTime.now();
        String formatResult = parseTimeFormat(localTime, command.getParams().get(0));
        if (Objects.nonNull(formatResult)) {
            return Result.success(formatResult);
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(command.getParams().get(0));
        formatResult = formatter.format(localTime);
        return Result.success(formatResult);
    }

    private String parseTimeFormat(LocalDateTime localTime, String timestampPattern) {

        switch (timestampPattern) {
            case "Timestamp":
                return String.valueOf(localTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            case "Timestamp-s":
                return String.valueOf(localTime.atZone(ZoneId.systemDefault()).toInstant().getEpochSecond());
            default:
                return null;
        }
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
