package com.openquartz.sequence.generator.common.dictionary;

/**
 * BaseEnum
 *
 * @param <T> T
 * @author svnee
 */
public interface BaseEnum<T> {

    /**
     * getCode
     *
     * @return code
     */
    T getCode();

    /**
     * getDesc
     *
     * @return desc
     */
    String getDesc();
}
