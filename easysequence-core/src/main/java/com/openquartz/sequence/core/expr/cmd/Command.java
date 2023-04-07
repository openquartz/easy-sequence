package com.openquartz.sequence.core.expr.cmd;

import com.google.common.base.Splitter;
import com.openquartz.sequence.generator.common.utils.CollectionUtils;
import java.util.List;

/**
 * @author svnee
 */
public class Command {

    private String cmd;
    private List<String> params;

    public static Command parse(String input) {
        List<String> parts = Splitter.onPattern("\\s").splitToList(input);

        if (CollectionUtils.isNotEmpty(parts)) {
            Command command = new Command();
            command.setCmd(parts.get(0));
            command.setParams(parts.subList(1, parts.size()));
            return command;
        }
        return null;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public List<String> getParams() {
        return params;
    }

    public void setParams(List<String> params) {
        this.params = params;
    }

    public String getFirstParam() {
        return indexOfParam(0);
    }

    public String getSecondParam() {
        return indexOfParam(1);
    }

    public String getThirdParam() {
        return indexOfParam(2);
    }

    public String indexOfParam(int index) {
        if (params == null) {
            return null;
        }
        if (params.size() < index + 1) {
            return null;
        }
        return params.get(index);
    }

    @Override
    public String toString() {
        return "Command{" +
            "cmd='" + cmd + '\'' +
            ", params=" + params +
            '}';
    }
}
