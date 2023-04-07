package com.openquartz.sequence.generator.common.utils;

import static com.openquartz.sequence.generator.common.utils.ParamUtils.checkNotEmpty;
import static com.openquartz.sequence.generator.common.utils.ParamUtils.checkNotNull;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;

/**
 * 时间工具类
 *
 * @author svnee
 **/
@Slf4j
public final class DateUtils {

    public static final String DAY_PATTERN = "yyyy-MM-dd";
    public static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private DateUtils() {
    }

    private static final ThreadLocal<SimpleDateFormat> DATE_FORMATTER_CONTEXT = ThreadLocal
        .withInitial(SimpleDateFormat::new);

    public static long floorAndDiffYear(LocalDate fromDate, LocalDate toDate) {
        LocalDate floorFrom = floorYear(fromDate);
        LocalDate floorTo = floorYear(toDate);
        return diffYear(floorFrom, floorTo);
    }

    /**
     * 计算年差
     */
    public static long diffYear(LocalDate fromDate, LocalDate toDate) {
        return ChronoUnit.YEARS.between(fromDate, toDate);
    }

    /**
     * 抹平年零头, 返回当年1月1号
     */
    public static LocalDate floorYear(LocalDate localDate) {
        return localDate.minusDays(localDate.getDayOfYear() - 1L);
    }

    /**
     * 抹平月零头，计算月差
     * 注：
     * 日期1 = K月最后一天
     * 日期2 = K+1月第一天
     * 日期1和日期2月差为1
     */
    public static long floorAndDiffMonth(LocalDate fromDate, LocalDate toDate) {
        LocalDate floorFrom = floorMonth(fromDate);
        LocalDate floorTo = floorMonth(toDate);
        return diffMonth(floorFrom, floorTo);
    }

    /**
     * 计算月差
     */
    public static long diffMonth(LocalDate fromDate, LocalDate toDate) {
        return ChronoUnit.MONTHS.between(fromDate, toDate);
    }


    /**
     * 抹平月零头, 返回当月1号
     */
    public static LocalDate floorMonth(LocalDate localDate) {
        return localDate.minusDays(localDate.getDayOfMonth() - 1L);
    }

    /**
     * 抹平周零头, 返回当周一
     */
    public static LocalDate floorWeek(LocalDate localDate) {
        return localDate.minusDays(localDate.getDayOfWeek().getValue() - 1L);
    }

    /**
     * 抹平周零头，计算周差
     * 注：
     * 日期1 = K周最后一天
     * 日期2 = K+1周第一天
     * 日期1和日期2周差为1
     */
    public static long floorAndDiffWeek(LocalDate fromDate, LocalDate toDate) {
        LocalDate floorFrom = floorWeek(fromDate);
        LocalDate floorTo = floorWeek(toDate);
        return diffWeek(floorFrom, floorTo);
    }

    /**
     * 计算周差
     */
    public static long diffWeek(LocalDate fromDate, LocalDate toDate) {
        return ChronoUnit.WEEKS.between(fromDate, toDate);
    }

    /**
     * 计算日差
     */
    public static long diffDay(LocalDate fromDate, LocalDate toDate) {
        return ChronoUnit.DAYS.between(fromDate, toDate);
    }

    /**
     * format date
     *
     * @param date date
     * @param pattern pattern
     * @return date str
     */
    public static String format(Date date, String pattern) {

        checkNotNull(date);
        checkNotEmpty(pattern);

        try {
            SimpleDateFormat formatter = DATE_FORMATTER_CONTEXT.get();
            formatter.applyPattern(pattern);

            return formatter.format(date);
        } finally {
            DATE_FORMATTER_CONTEXT.remove();
        }
    }

    /**
     * 解析时间字符串
     *
     * @param timeStr time str
     * @param pattern pattern  "yyyy-MM-dd"
     * @return date
     */
    public static Date parse(String timeStr, String pattern) {

        checkNotEmpty(timeStr);
        checkNotEmpty(pattern);

        try {
            SimpleDateFormat formatter = DATE_FORMATTER_CONTEXT.get();
            formatter.applyPattern(pattern);
            try {
                return formatter.parse(timeStr);
            } catch (Exception ex) {
                log.error("[DateUtils#parse] parse-error! time:{},pattern:{}", timeStr, pattern);
                throw new RuntimeException(ex);
            }
        } finally {
            DATE_FORMATTER_CONTEXT.remove();
        }
    }
}
