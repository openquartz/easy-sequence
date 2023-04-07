package com.openquartz.sequence.generator.common.exception;

import java.text.MessageFormat;

/**
 * 异步文件异常
 *
 * @author svnee
 */
public class EasySequenceException extends RuntimeException {

    private final transient EasySequenceErrorCode errorCode;

    public EasySequenceException(EasySequenceErrorCode errorCode) {
        super(errorCode.getErrorMsg());
        this.errorCode = errorCode;
    }

    public EasySequenceException(EasySequenceErrorCode errorCode, String errorMsg) {
        super(errorMsg);
        this.errorCode = errorCode;
    }

    public EasySequenceErrorCode getErrorCode() {
        return errorCode;
    }

    /**
     * 替换占位符号
     *
     * @param placeHold 占位
     * @return 异常
     */
    public static EasySequenceException replacePlaceHold(EasySequenceErrorCode errorCode, Object... placeHold) {
        throw new EasySequenceException(errorCode, MessageFormat.format(errorCode.getErrorMsg(), placeHold));
    }

}
