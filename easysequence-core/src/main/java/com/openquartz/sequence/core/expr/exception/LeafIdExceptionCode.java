package com.openquartz.sequence.core.expr.exception;

import com.openquartz.sequence.generator.common.exception.EasySequenceErrorCode;

/**
 * LeafId Exception
 *
 * @author svnee
 */
public enum LeafIdExceptionCode implements EasySequenceErrorCode {

    LEAF_ID_INIT_ERROR("01", "LeafId init error!"),
    LEAF_KEY_NOT_EXISTS_ERROR("02", "Leaf key:{0} not exists!", true),
    LEAF_TWO_SEGMENTS_ARE_NULL_ERROR("03", "Leaf two segment area error!"),
    ;
    private final String errorCode;
    private final String errorMsg;
    private final boolean replacePlaceHold;

    private static final String PREFIX_BASE_CODE = "LeafIdError-";

    LeafIdExceptionCode(String errorCode, String errorMsg) {
        this(errorCode, errorMsg, false);
    }

    LeafIdExceptionCode(String errorCode, String errorMsg, boolean replacePlaceHold) {
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
