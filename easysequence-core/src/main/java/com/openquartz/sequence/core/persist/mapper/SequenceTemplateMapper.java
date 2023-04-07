package com.openquartz.sequence.core.persist.mapper;

import com.openquartz.sequence.core.persist.model.SequenceTemplate;

/**
 * 序列模版Mapper
 *
 * @author svnee
 */
public interface SequenceTemplateMapper {

    /**
     * 查询注册码对应的序列号模板
     *
     * @param registerCode 注册码
     * @return 序列号模板
     */
    SequenceTemplate selectByRegisterCode(String registerCode);

}
