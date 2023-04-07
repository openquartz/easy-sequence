package com.openquartz.sequence.core.expr;

import com.openquartz.sequence.core.expr.cmd.AssignExtParam;
import java.util.List;

/**
 * 序列生成服务
 *
 * @author svnee
 */
public interface SequenceGenerateService {

    /**
     * 生成编码
     *
     * @param registerCode 注册码
     * @return 返回生成后的编码服务
     */
    String generateCode(String registerCode);

    /**
     * 编码服务
     * 区分环境参数使用
     *
     * @param registerCode 注册码
     * @param assignExtParam 额外参数
     * @return 生成的序列码
     */
    String generateCode(String registerCode, AssignExtParam assignExtParam);

    /**
     * 批量获取
     *
     * @param registerCode 注册码
     * @param qty 数量
     * @return 获取后的序列码
     */
    List<String> batchGenerateCode(String registerCode, int qty);

    /**
     * 批量获取
     * 分环境获取使用
     *
     * @param registerCode 注册码
     * @param param 额外参数
     * @param qty 数量
     * @return 生成的序列号
     */
    List<String> batchGenerateCode(String registerCode, int qty, AssignExtParam param);

}
