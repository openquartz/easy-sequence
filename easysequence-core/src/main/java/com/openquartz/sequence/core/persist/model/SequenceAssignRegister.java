package com.openquartz.sequence.core.persist.model;

import com.openquartz.sequence.core.dictionary.CycleUnit;
import java.util.Date;
import lombok.Data;

/**
 * 序列分配注册器
 *
 * @author svnee
 **/
@Data
public class SequenceAssignRegister {

    /**
     * ID
     */
    private Integer id;

    /**
     * 注册码
     */
    private String registerCode;

    /**
     * 描述
     */
    private String registerDesc;

    /**
     * 周期
     */
    private Integer cycle;

    /**
     * 周期单位
     */
    private CycleUnit cycleUnit;

    /**
     * 初始值
     */
    private Long initValue;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
