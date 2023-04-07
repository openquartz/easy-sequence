package com.openquartz.sequence.core.expr.exception;

import com.openquartz.sequence.generator.common.exception.EasySequenceErrorCode;

/**
 * CommandExceptionCode
 *
 * @author svnee
 **/
public enum CommandExceptionCode implements EasySequenceErrorCode {

    COMMAND_NOT_SUPPORT_ERROR("01", "command:{0} not support error!", true),
    COMMAND_TEMPLATE_ILLEGAL_ERROR("02", "command template illegal error!"),
    COMMAND_TYPE_NOT_SUPPORT_ADD_CHILD_ERROR("03", "Command type not support add child command error!"),
    COMMAND_RUN_ERROR("04", "Command: {0} run error!param: {1},errorInfo:{2}", true),
    ;

    private final String errorCode;
    private final String errorMsg;
    private final boolean replacePlaceHold;

    private static final String PREFIX_BASE_CODE = "CommandError-";

    CommandExceptionCode(String errorCode, String errorMsg) {
        this(errorCode, errorMsg, false);
    }

    CommandExceptionCode(String errorCode, String errorMsg, boolean replacePlaceHold) {
        this.errorCode = PREFIX_BASE_CODE + errorCode;
        this.errorMsg = errorMsg;
        this.replacePlaceHold = replacePlaceHold;
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String getErrorMsg() {
        return errorMsg;
    }

    @Override
    public boolean isReplacePlaceHold() {
        return replacePlaceHold;
    }
}
