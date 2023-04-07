package com.openquartz.sequence.generator.common.concurrent;

import static com.openquartz.sequence.generator.common.utils.ParamUtils.checkNotNull;

import com.openquartz.sequence.generator.common.utils.MapUtils;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * TraceContext
 *
 * @author svnee
 **/
public class TraceContext {

    private static final ThreadLocal<Map<String, Object>> CURRENT_TRACE_CONTEXT = ThreadLocal.withInitial(HashMap::new);

    private TraceContext() {
    }

    public static void setTraceContextMap(Map<String, Object> currentTraceContextMap) {
        CURRENT_TRACE_CONTEXT.set(currentTraceContextMap);
    }

    public static Map<String, Object> getTraceContextMap() {
        Map<String, Object> traceContextMap = CURRENT_TRACE_CONTEXT.get();
        return Objects.isNull(traceContextMap) ? Collections.emptyMap() : traceContextMap;
    }

    public static void clear() {
        CURRENT_TRACE_CONTEXT.remove();
    }

    public static void clear(TraceContextParam param) {
        CURRENT_TRACE_CONTEXT.get().remove(param.getCode());
    }

    private static Object get(TraceContextParam param) {

        checkNotNull(param);

        return CURRENT_TRACE_CONTEXT.get().get(param.getCode());
    }

    private static Object putIfAbsent(TraceContextParam param, Object value) {

        checkNotNull(param);
        checkNotNull(value);

        Map<String, Object> map = CURRENT_TRACE_CONTEXT.get();
        if (Objects.isNull(map)) {
            map = new HashMap<>();
        }

        return map.putIfAbsent(param.getCode(), value);
    }

    private static Object put(TraceContextParam param, Object value) {

        checkNotNull(param);
        checkNotNull(value);

        Map<String, Object> map = CURRENT_TRACE_CONTEXT.get();
        if (Objects.isNull(map)) {
            map = new HashMap<>();
        }

        return map.put(param.getCode(), value);
    }

    /**
     * 获取traceId
     *
     * @return traceId
     */
    public static String getTraceId() {
        Map<String, String> traceMap = getTrace();
        return Objects.nonNull(traceMap) ? traceMap.get("traceId") : null;
    }

    /**
     * get trace
     *
     * @return trace
     */
    public static Map<String, String> getTrace() {
        Object o = get(TraceContextParam.TRACE_ID);
        return Objects.nonNull(o) ? (Map<String, String>) o : null;
    }

    /**
     * put trace
     */
    public static void putTrace(Map<String, String> traceMap) {
        if (MapUtils.isNotEmpty(traceMap)) {
            put(TraceContextParam.TRACE_ID, traceMap);
        }
    }

}
