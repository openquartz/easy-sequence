package com.openquartz.sequence.generator.common.concurrent;

/**
 * TraceContextParam
 *
 * @author svnee
 */
public enum TraceContextParam {

    TRACE_ID("traceId"),
    ;

    TraceContextParam(String code) {
        this.code = code;
    }

    /**
     * Code
     */
    private final String code;

    public String getCode() {
        return code;
    }
}
