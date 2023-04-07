package com.openquartz.sequence.generator.common.utils;

import com.openquartz.sequence.generator.common.exception.Asserts;
import com.openquartz.sequence.generator.common.exception.CommonErrorCode;

/**
 * param utils
 *
 * @author svnee
 **/
public final class ParamUtils {

    private ParamUtils() {
    }

    public static void checkNotNull(Object obj) {
        Asserts.notNull(obj, CommonErrorCode.PARAM_ILLEGAL_ERROR);
    }

    public static void checkNotEmpty(String str) {
        Asserts.isTrue(StringUtils.isNotBlank(str), CommonErrorCode.PARAM_ILLEGAL_ERROR);
    }

    public static void checkArgument(boolean b) {
        Asserts.isTrue(b, CommonErrorCode.PARAM_ILLEGAL_ERROR);
    }

}
