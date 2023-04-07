package com.openquartz.sequence.generator.common.exception;

import com.openquartz.sequence.generator.common.utils.CollectionUtils;
import com.openquartz.sequence.generator.common.utils.StringUtils;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

/**
 * 断言工具类
 *
 * @author svnee
 */
@Slf4j
public final class Asserts {

    private Asserts() {
    }

    /**
     * 断言是否为true
     *
     * @param expression 表达式
     * @param code 异常码
     */
    public static void isTrue(boolean expression, EasySequenceErrorCode code) {
        isTrueIfLog(expression, null, code);
    }

    /**
     * 断言是否为true
     *
     * @param expression 表达式
     * @param code 异常码
     */
    public static void isTrueIfLog(boolean expression, LogCallback logCallback, EasySequenceErrorCode code) {
        if (!expression) {
            if (Objects.nonNull(logCallback)) {
                logCallback.log();
            }
            throw new EasySequenceException(code);
        }
    }

    /**
     * 断言是否为true
     *
     * @param expression 表达式
     * @param code 异常码
     */
    public static void isTrueIfLog(boolean expression, LogCallback logCallback, EasySequenceErrorCode code,
        Object... placeHold) {
        if (!expression) {
            if (Objects.nonNull(logCallback)) {
                logCallback.log();
            }
            throw EasySequenceException.replacePlaceHold(code, placeHold);
        }
    }

    /**
     * 断言是否为true
     *
     * @param expression 表达式
     * @param code 异常码
     */
    public static void isTrue(boolean expression, EasySequenceErrorCode code, Object... placeHold) {
        isTrueIfLog(expression, null, code, placeHold);
    }

    /**
     * 断言是否为true
     *
     * @param expression 表达式
     * @param errorCode 异常码
     * @param exceptionClazz 异常类
     * @param <T> T 异常码
     * @param <E> E 异常
     */
    public static <T extends EasySequenceErrorCode, E extends EasySequenceException> void isTrue(boolean expression,
        T errorCode, Class<E> exceptionClazz) {
        isTrueIfLog(expression, null, errorCode, exceptionClazz);
    }

    /**
     * 断言非空
     *
     * @param obj obj
     * @param code code
     */
    public static void notNull(Object obj, EasySequenceErrorCode code) {
        isTrue(Objects.nonNull(obj), code);
    }

    /**
     * 断言非空
     *
     * @param obj obj
     * @param logCallback logCallback
     * @param code code
     */
    public static void notNullIfLog(Object obj, LogCallback logCallback, EasySequenceErrorCode code,
        Object... placeHold) {
        isTrueIfLog(Objects.nonNull(obj), logCallback, code, placeHold);
    }

    /**
     * 断言非空
     *
     * @param obj obj
     * @param code code
     */
    public static void notNull(Object obj, EasySequenceErrorCode code, Object... placeHold) {
        isTrue(Objects.nonNull(obj), code, placeHold);
    }

    /**
     * 断言为空
     *
     * @param obj obj
     * @param code code
     */
    public static void isNull(Object obj, EasySequenceErrorCode code) {
        isTrue(Objects.isNull(obj), code);
    }

    /**
     * 断言为空
     *
     * @param obj obj
     * @param code code
     */
    public static void isNull(Object obj, EasySequenceErrorCode code, Object... placeHold) {
        isTrue(Objects.isNull(obj), code, placeHold);
    }

    /**
     * 断言为空
     *
     * @param obj obj
     * @param code code
     */
    public static void isNullIfLog(Object obj, LogCallback logCallback, EasySequenceErrorCode code,
        Object... placeHold) {
        isTrueIfLog(Objects.isNull(obj), logCallback, code, placeHold);
    }

    /**
     * assert string not empty
     *
     * @param obj obj string
     * @param code code
     * @param placeHold place-hold
     */
    public static void notEmpty(String obj, EasySequenceErrorCode code, Object... placeHold) {
        isTrue(StringUtils.isNotBlank(obj), code, placeHold);
    }

    /**
     * assert string not empty
     *
     * @param obj obj string
     * @param logCallback log
     * @param code code
     * @param placeHold place-hold
     */
    public static void notEmptyIfLog(String obj, LogCallback logCallback, EasySequenceErrorCode code,
        Object... placeHold) {
        isTrueIfLog(StringUtils.isNotBlank(obj), logCallback, code, placeHold);
    }

    /**
     * assert collection not empty
     *
     * @param obj obj collection
     * @param code error code
     * @param placeHold place-hold
     */
    public static void notEmpty(Collection<?> obj, EasySequenceErrorCode code, Object... placeHold) {
        isTrue(CollectionUtils.isNotEmpty(obj), code, placeHold);
    }

    /**
     * assert map not empty
     *
     * @param obj obj collection
     * @param code error code
     * @param placeHold place-hold
     */
    public static void notEmpty(Map<?, ?> obj, EasySequenceErrorCode code, Object... placeHold) {
        isTrue(obj != null && !obj.isEmpty(), code, placeHold);
    }

    /**
     * assert map not empty
     *
     * @param obj obj collection
     * @param code error code
     * @param placeHold place-hold
     */
    public static void notEmptyIfLog(Map<?, ?> obj, LogCallback logCallback, EasySequenceErrorCode code,
        Object... placeHold) {
        isTrueIfLog(obj != null && !obj.isEmpty(), logCallback, code, placeHold);
    }

    /**
     * assert collection not empty
     *
     * @param obj obj collection
     * @param logCallback callback
     * @param code error code
     * @param placeHold place-hold
     */
    public static void notEmptyIfLog(Collection<?> obj, LogCallback logCallback, EasySequenceErrorCode code,
        Object... placeHold) {
        isTrueIfLog(CollectionUtils.isNotEmpty(obj), logCallback, code, placeHold);
    }

}