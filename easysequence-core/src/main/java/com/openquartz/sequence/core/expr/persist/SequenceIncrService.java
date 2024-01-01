package com.openquartz.sequence.core.expr.persist;

import com.openquartz.sequence.core.expr.cmd.AssignExtParam;
import com.openquartz.sequence.generator.common.bean.LifestyleBean;

/**
 * 序列自增服务
 *
 * @author svnee
 **/
public interface SequenceIncrService extends LifestyleBean {

    /**
     * 获取下个值并自增
     *
     * @param registerCode 注册code
     * @return 生成序列号
     */
    Long getAndIncrement(String registerCode);

    /**
     * 区分纬度获取下个值并自增
     * example:
     * 1.AssignExtParam param = AssignExtParam.create().set("c",100)
     * 2.getAndIncrement("RG1", param)
     *
     * @return incr number
     */
    Long getAndIncrement(String registerCode, AssignExtParam param);

    /**
     * 从获取并开始下一个自增间隔一个步长
     *
     * @param registerCode 注册码
     * @param param 参数
     * @param step 步长
     * @return 自增后的值
     */
    Long getAndIncrementBy(String registerCode, AssignExtParam param, long step);

}
