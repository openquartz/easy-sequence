package com.openquartz.sequence.core.expr.executors;

import com.openquartz.sequence.core.expr.cmd.Command;
import com.openquartz.sequence.core.expr.cmd.Environment;
import com.openquartz.sequence.core.expr.cmd.Result;
import com.openquartz.sequence.generator.common.exception.Asserts;
import com.openquartz.sequence.generator.common.exception.CommonErrorCode;
import lombok.extern.slf4j.Slf4j;

/**
 * {env local wh}
 * {env global wh}
 *
 * @author svnee
 */
@Slf4j
public class EnvExecutor implements CommandExecutor {

    private Environment environment;

    @Override
    public Result exec(Command command) {
        String paramName = command.getFirstParam();
        String contextType = command.getSecondParam() != null ? command.getSecondParam() : "local";
        Result result;
        Object value = null;
        if (ContextType.GLOBAL.getCode().equals(contextType)) {
            value = environment.getGlobalContextParam(paramName);
        } else if (ContextType.THREAD_LOCAL.getCode().equals(contextType)) {
            value = environment.getLocalContextParam(paramName);
        }
        if (value == null) {
            result = Result.paramError();
        } else {
            result = Result.success(String.valueOf(value));
        }
        return result;
    }

    @Override
    public void validate(Command command) {

        Asserts.notNull(command, CommonErrorCode.PARAM_ILLEGAL_ERROR);
        Asserts.notNull(command.getParams(), CommonErrorCode.PARAM_ILLEGAL_ERROR);
        Asserts.isTrue(!command.getParams().isEmpty(), CommonErrorCode.PARAM_ILLEGAL_ERROR);

        String contextType = command.getSecondParam();
        if (contextType != null) {
            log.error("[EnvExecutor#validate] context-type:{} is not correct!", contextType);
            Asserts.isTrue(ContextType.THREAD_LOCAL.getCode().equals(contextType) ||
                ContextType.GLOBAL.getCode().equals(contextType), CommonErrorCode.PARAM_ILLEGAL_ERROR);
        }
    }

    @Override
    public void init(Environment environment) {
        this.environment = environment;
    }

    enum ContextType {
        //
        THREAD_LOCAL("local", "thread local context"),
        GLOBAL("global", "global context");

        ContextType(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        private final String code;
        private final String desc;

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }
}
