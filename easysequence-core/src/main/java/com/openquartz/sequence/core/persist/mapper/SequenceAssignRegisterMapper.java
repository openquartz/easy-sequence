package com.openquartz.sequence.core.persist.mapper;

import com.openquartz.sequence.core.persist.model.SequenceAssignRegister;

/**
 * SequenceAssignRegisterMapper
 *
 * @author svnee
 */
public interface SequenceAssignRegisterMapper {

    /**
     * 根据注册码查询序列注册生成器
     *
     * @param registerCode 注册码
     * @return 序列注册器·
     */
    SequenceAssignRegister selectByRegisterCode(String registerCode);

}
