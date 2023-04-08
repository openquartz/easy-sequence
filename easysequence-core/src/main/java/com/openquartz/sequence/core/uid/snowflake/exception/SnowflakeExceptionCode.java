package com.openquartz.sequence.core.uid.snowflake.exception;

import com.openquartz.sequence.generator.common.exception.EasySequenceErrorCode;
import lombok.Getter;

/**
 * 雪花算法ID异常
 *
 * @author svnee
 **/
@Getter
public enum SnowflakeExceptionCode implements EasySequenceErrorCode {
    WORKER_ID_NOT_ILLEGAL("01", "workerId不合法"),
    GROUP_NOT_EXIST_ERROR("02", "group not exist!"),
    GROUP_WORKER_FULL_ERROR("03", "group:{0} has full worker!", true),
    GROUP_WORKER_ASSIGN_ERROR("04", "group:{0} assign workerId error!", true),
    CLOCK_CALLBACK_ERROR("05", "Time callback, refusing to generate ID for more than {0} milliseconds", true),
    WORKER_ID_OVER_FLOW_ERROR("06", "WorkerId over flow error!"),
    DATA_CENTER_ID_OVER_FLOW_ERROR("07", "DataCenterId over flow error!"),
    SNOWFLAKE_ID_GENERATE_ERROR("08", "SnowflakeId generate error!"),
    SNOWFLAKE_DEFAULT_WORKER_ID_OVER_ERROR("09", "Snowflake default workerId must in range:[{0},{1})!", true),
    SNOWFLAKE_WORKER_ID_OVER_ERROR("10", "Snowflake workerId must in range:[{0},{1})!", true),
    ;

    private final String errorCode;
    private final String errorMsg;
    private final boolean replacePlaceHold;

    private static final String PREFIX_BASE_CODE = "SnowflakeIdError-";

    SnowflakeExceptionCode(String errorCode, String errorMsg) {
        this(errorCode, errorMsg, false);
    }

    SnowflakeExceptionCode(String errorCode, String errorMsg, boolean replacePlaceHold) {
        this.errorCode = PREFIX_BASE_CODE + errorCode;
        this.errorMsg = errorMsg;
        this.replacePlaceHold = replacePlaceHold;
    }
}
