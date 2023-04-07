package com.openquartz.sequence.core.persist.mapper;

import com.openquartz.sequence.core.persist.model.SequenceNextAssign;

/**
 * @author svnee
 */
public interface SequenceNextAssignMapper {

    /**
     * Id
     *
     * @param assigner assigner
     * @return affect row
     */
    int insert(SequenceNextAssign assigner);

    /**
     * 按照key 查询下一个序列分配生成值
     *
     * @param key key
     * @return 下一个序列生成指定值
     */
    SequenceNextAssign selectByKey(String key);

    /**
     * 重新设置下一个分配值
     *
     * @param registerCode 注册码
     * @param preValue 之前的值
     * @return 影响行数
     */
    int resetNextValue(String registerCode, Long preValue);

    /**
     * 从起点值开始自增
     *
     * @param key key
     * @param step 步长
     * @param nextValue 下一个分配值
     * @return 影响行数
     */
    int incrementBy(String key, Long step, Long nextValue);
}
