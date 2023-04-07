package com.openquartz.sequence.core.persist.model;


/**
 * @author svnee
 */
public interface SequenceTemplate {

    /**
     * 注冊码
     *
     * @return 注册码
     */
    String getRegisterCode();

    /**
     * 表达式
     *
     * @return 表达式
     */
    String getExpression();
}
