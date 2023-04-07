package com.openquartz.sequence.core.expr.exception;

import com.openquartz.sequence.generator.common.exception.EasySequenceErrorCode;
import lombok.Getter;

/**
 * 序列号生成器异常码
 *
 * @author svnee
 */
@Getter
public enum SequenceGenerateExceptionCode implements EasySequenceErrorCode {
    SEQUENCE_TEMPLATE_NOT_EXIST_ERROR("01", "sequence-template not exist!"),
    SEQUENCE_EXECUTOR_REGISTERED_ERROR("02", "executor not registered for cmd:{0}", true),
    SEQUENCE_REGISTER_CODE_NOT_EXIST_ERROR("03", "register code not exist!{0}", true),
    SEQUENCE_BUCKET_USE_UP_ERROR("04", "sequence bucket used up!bucket:{0},startSeq:{1},endSeq:{2}", true),
    SEQUENCE_INCR_ERROR("05", "sequence incr error!"),
    SEQUENCE_BUCKET_NOT_EXIST_ERROR("06", "sequence bucket:{0} Not exist!", true),
    ;

    private final String errorCode;
    private final String errorMsg;
    private final boolean replacePlaceHold;

    private static final String PREFIX_BASE_CODE = "SequenceGenerateError-";

    SequenceGenerateExceptionCode(String errorCode, String errorMsg) {
        this(errorCode, errorMsg, false);
    }

    SequenceGenerateExceptionCode(String errorCode, String errorMsg, boolean replacePlaceHold) {
        this.errorCode = PREFIX_BASE_CODE + errorCode;
        this.errorMsg = errorMsg;
        this.replacePlaceHold = replacePlaceHold;
    }

}
