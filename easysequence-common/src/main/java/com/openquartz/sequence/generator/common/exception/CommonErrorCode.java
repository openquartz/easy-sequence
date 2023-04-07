package com.openquartz.sequence.generator.common.exception;

import lombok.Getter;

/**
 * @author svnee
 **/
@Getter
public enum CommonErrorCode implements EasySequenceErrorCode {

    PARAM_ILLEGAL_ERROR("01", "Illegal Param!"),
    ;
    private final String errorCode;
    private final String errorMsg;
    private final boolean replacePlaceHold;

    private static final String SIMPLE_BASE_CODE = "CommonError-";

    CommonErrorCode(String errorCode, String errorMsg) {
        this(errorCode, errorMsg, false);
    }

    CommonErrorCode(String errorCode, String errorMsg, boolean replacePlaceHold) {
        this.errorCode = SIMPLE_BASE_CODE + errorCode;
        this.errorMsg = errorMsg;
        this.replacePlaceHold = replacePlaceHold;
    }
}
