package com.openquartz.sequence.generator.common.utils;

import com.openquartz.sequence.generator.common.exception.Asserts;
import com.openquartz.sequence.generator.common.exception.CommonErrorCode;
import java.util.Random;

/**
 * @author svnee
 **/
public final class RandomUtils {

    private RandomUtils() {
    }

    private static final Random RANDOM = new Random();

    public static int nextInt(int start, int end) {

        Asserts.isTrue(end > start, CommonErrorCode.PARAM_ILLEGAL_ERROR);
        return RANDOM.nextInt(end - start) + start;
    }

}
