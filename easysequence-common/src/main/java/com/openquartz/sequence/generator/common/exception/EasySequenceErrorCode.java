package com.openquartz.sequence.generator.common.exception;

/**
 * 异常Code
 *
 * @author svnee
 **/
public interface EasySequenceErrorCode {

    /**
     * 异常Code
     *
     * @return 异常code码
     */
    String getErrorCode();

    /**
     * 异常信息
     *
     * @return 异常信息描述
     */
    String getErrorMsg();

    /**
     * 是否替换占位
     *
     * @return 是否可以占位替换
     */
    default boolean isReplacePlaceHold() {
        return false;
    }

}
