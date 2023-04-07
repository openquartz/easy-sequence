package com.openquartz.sequence.core.expr.cmd;

import com.google.common.collect.ImmutableSortedSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author svnee
 */
public class AssignExtParam {

    private Map<String, String> params = new HashMap<>();
    public static final AssignExtParam EMPTY_PARAM = AssignExtParam.create();

    static {
        EMPTY_PARAM.params = Collections.unmodifiableMap(EMPTY_PARAM.params);
    }

    private AssignExtParam() {

    }

    public Map<String, String> getParams() {
        return params;
    }

    public AssignExtParam set(String key, String value) {
        this.params.put(key, value);
        return this;
    }

    @Override
    public String toString() {
        return ImmutableSortedSet.copyOf(params.keySet()).stream()
            .map(key -> "{" + key + "-" + params.get(key) + "}").collect(Collectors.joining());
    }

    public static AssignExtParam create() {
        return new AssignExtParam();
    }

}
