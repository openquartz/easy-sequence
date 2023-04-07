package com.openquartz.sequence.core.expr.cmd;

/**
 * @author svnee
 */
public class Result {

    public static final int SUCCESS = 200;
    public static final int PARAM_ERROR = 400;
    public static final int SYSTEM_ERROR = 500;

    /**
     * 缓存失败结果
     */
    private static final Result SYSTEM_ERROR_RESULT;
    private static final Result PARAM_ERROR_RESULT;
    private static final Result EMPTY_RETURN_SUCCESS_RESULT;

    static {
        SYSTEM_ERROR_RESULT = new Result();
        SYSTEM_ERROR_RESULT.code = SYSTEM_ERROR;
        PARAM_ERROR_RESULT = new Result();
        PARAM_ERROR_RESULT.code = PARAM_ERROR;
        EMPTY_RETURN_SUCCESS_RESULT = new Result();
        EMPTY_RETURN_SUCCESS_RESULT.code = SUCCESS;
    }

    /**
     * 200-成功
     * 400-参数错误
     * 500-系统异常
     */
    private int code;

    private String output;

    private String errorMsg;

    public boolean isSuccess() {
        return code == SUCCESS;
    }

    public static Result success(String output) {
        Result result = new Result();
        result.setCode(SUCCESS);
        result.setOutput(output);
        return result;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public String getOutput() {
        return output;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public static Result success() {
        return EMPTY_RETURN_SUCCESS_RESULT;
    }

    public static Result paramError() {
        return PARAM_ERROR_RESULT;
    }

    public static Result systemError() {
        return SYSTEM_ERROR_RESULT;
    }

    public static Result systemError(String msg) {
        Result result = new Result();
        result.errorMsg = msg;
        result.setCode(SYSTEM_ERROR);
        return result;
    }

    @Override
    public String toString() {
        return "Result{" +
            "code=" + code +
            ", output='" + output + '\'' +
            ", errorMsg='" + errorMsg + '\'' +
            '}';
    }
}
