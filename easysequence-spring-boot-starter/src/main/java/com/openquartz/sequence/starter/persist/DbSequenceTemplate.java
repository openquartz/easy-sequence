package com.openquartz.sequence.starter.persist;

import com.openquartz.sequence.core.persist.model.SequenceTemplate;
import lombok.Data;

/**
 * DB 实现
 *
 * @author svnee
 **/
@Data
public class DbSequenceTemplate implements SequenceTemplate {

    private Long id;

    /**
     * 注册码
     */
    private String registerCode;

    /**
     * 表达式
     */
    private String expression;

}
