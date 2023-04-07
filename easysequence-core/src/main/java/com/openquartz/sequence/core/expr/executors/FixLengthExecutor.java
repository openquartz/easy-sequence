package com.openquartz.sequence.core.expr.executors;

import com.openquartz.sequence.core.expr.cmd.Environment;
import com.openquartz.sequence.core.expr.cmd.Command;
import com.openquartz.sequence.core.expr.cmd.Result;
import com.openquartz.sequence.generator.common.exception.Asserts;
import com.openquartz.sequence.generator.common.exception.CommonErrorCode;
import com.openquartz.sequence.generator.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 固定编码长度, 输入字符串大于指定长度则不处理, 小于指定长度则前缀位置填充指定字符
 * command 参数： 1.字符串 2.长度  3.填充字符
 *
 * @author svnee
 */
@Slf4j
public class FixLengthExecutor implements CommandExecutor {

    @Override
    public Result exec(Command command) {
        int specificLen = Integer.parseInt(command.getSecondParam());
        String fillString = "0";
        boolean fillLeft = false;
        if (command.getThirdParam() != null) {
            fillLeft = "l".equals(command.getThirdParam());
        }
        if (command.indexOfParam(4) != null) {
            fillString = command.indexOfParam(4);
        }
        String str = generateFixedLenStr(command.getFirstParam(), specificLen, fillLeft, fillString);
        return Result.success(str);
    }

    @Override
    public void validate(Command command) {

        Asserts.notNull(command, CommonErrorCode.PARAM_ILLEGAL_ERROR);
        Asserts.notNull(command.getSecondParam(), CommonErrorCode.PARAM_ILLEGAL_ERROR);
        Asserts.isTrueIfLog(StringUtils.isNumeric(command.getSecondParam()),
            () -> log.error("[FixLengthExecutor#validate] command second param not illegal!command:{},secondParam:{}",
                command, command.getSecondParam()),
            CommonErrorCode.PARAM_ILLEGAL_ERROR);
    }

    @Override
    public void init(Environment environment) {

    }

    private static String generateFixedLenStr(String str, int length, boolean fillLeft, String fillStr) {
        if (str.length() < length) {
            int lengthOfFill = length - str.length();
            StringBuilder fillContent = new StringBuilder();
            while (fillContent.length() < lengthOfFill) {
                fillContent.append(fillStr);
            }
            String fillContentStr = fillContent.toString();
            if (fillContentStr.length() > lengthOfFill) {
                fillContentStr = fillContentStr.substring(fillContentStr.length() - lengthOfFill);
            }
            if (!fillLeft) {
                return fillContentStr + str;
            } else {
                return str + fillContentStr;
            }
        }
        return str;
    }

}
