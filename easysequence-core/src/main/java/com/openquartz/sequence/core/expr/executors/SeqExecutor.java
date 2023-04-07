package com.openquartz.sequence.core.expr.executors;

import com.openquartz.sequence.core.expr.persist.SequenceIncrService;
import com.openquartz.sequence.core.expr.cmd.Environment;
import com.openquartz.sequence.core.expr.cmd.AssignExtParam;
import com.openquartz.sequence.core.expr.cmd.Command;
import com.openquartz.sequence.core.expr.cmd.Result;
import com.openquartz.sequence.generator.common.exception.Asserts;
import com.openquartz.sequence.generator.common.exception.CommonErrorCode;
import com.openquartz.sequence.generator.common.utils.SpringContextUtil;
import com.openquartz.sequence.generator.common.utils.StringUtils;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * 序列号自增服务
 * 语法：{seq 序列注册码}
 *
 * @author svnee
 */
@Slf4j
public class SeqExecutor implements CommandExecutor {

    private SequenceIncrService sequenceIncrService;
    private Environment environment;

    @Override
    public Result exec(Command command) {
        if (sequenceIncrService == null) {
            this.sequenceIncrService = SpringContextUtil.getBean(SequenceIncrService.class);
        }
        String registerCode = command.getFirstParam();
        AssignExtParam param;
        if (command.getSecondParam() != null) {
            String env = command.getSecondParam();
            param = AssignExtParam.create().set("e", env);
        } else {
            param = AssignExtParam.EMPTY_PARAM;
        }
        Long assignedNumber = sequenceIncrService.getAndIncrement(registerCode, param);
        return Result.success(String.valueOf(assignedNumber));
    }

    @Override
    public void validate(Command command) {
        Asserts.notNull(command, CommonErrorCode.PARAM_ILLEGAL_ERROR);
        List<String> params = command.getParams();
        Asserts.notNull(params, CommonErrorCode.PARAM_ILLEGAL_ERROR);
        Asserts.isTrue(!params.isEmpty(), CommonErrorCode.PARAM_ILLEGAL_ERROR);
        if (command.getSecondParam() != null) {
            Asserts.isTrue(StringUtils.isNumeric(command.getSecondParam()), CommonErrorCode.PARAM_ILLEGAL_ERROR);
        }
    }

    @Override
    public void init(Environment environment) {
        try {
            this.sequenceIncrService = SpringContextUtil.getBean(SequenceIncrService.class);
        } catch (Exception ex) {
            log.error("[SeqExecutor#init] init-error!", ex);
        }
        this.environment = environment;
    }
}
