package com.openquartz.sequence.core.expr.executors;

import com.google.common.collect.Sets;
import com.openquartz.sequence.generator.common.exception.Asserts;
import com.openquartz.sequence.generator.common.exception.CommonErrorCode;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Set;

public abstract class TimeFormatExecutor implements CommandExecutor {

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


    public String formatTime(LocalDateTime localTime, String timestampPattern) {
        String formatResult = formatTimestamp(localTime, timestampPattern);
        if (Objects.nonNull(formatResult)) {
            return formatResult;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(timestampPattern);
        return formatter.format(localTime);
    }

    private String formatTimestamp(LocalDateTime localTime, String timestampPattern) {

        switch (timestampPattern) {
            case "Timestamp":
                return String.valueOf(localTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            case "Timestamp-s":
                return String.valueOf(localTime.atZone(ZoneId.systemDefault()).toInstant().getEpochSecond());
            default:
                return null;
        }
    }

    public void checkTimePattern(String timePattern){
        Asserts.isTrue(AVAILABLE_TIME_FORMATS.contains(timePattern), CommonErrorCode.PARAM_ILLEGAL_ERROR);
    }

}
