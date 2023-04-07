package com.openquartz.sequence.core.expr.cmd;

import com.openquartz.sequence.core.expr.exception.CommandExceptionCode;
import com.openquartz.sequence.generator.common.constant.Constants;
import com.openquartz.sequence.generator.common.exception.Asserts;
import java.util.ArrayList;
import java.util.List;

/**
 * @author svnee
 */
public class CommandNode {

    /**
     * command or value
     */
    private String value;

    /**
     * children command node
     */
    private List<CommandNode> children = new ArrayList<>();

    /**
     * type
     */
    private NodeType type;

    public CommandNode(NodeType type) {
        this.type = type;
    }

    public List<CommandNode> getChildren() {
        return children;
    }

    public void setChildren(List<CommandNode> children) {
        Asserts.isTrue(isCommand(), CommandExceptionCode.COMMAND_TYPE_NOT_SUPPORT_ADD_CHILD_ERROR);
        this.children = children;
    }

    public boolean isCommand() {
        return NodeType.COMMAND.equals(type);
    }

    public boolean isJoin() {
        return NodeType.JOIN.equals(type);
    }

    public boolean isParam() {
        return NodeType.PARAM.equals(type);
    }

    public NodeType getType() {
        return type;
    }

    public void setType(NodeType type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public static CommandNode fromParam(String param) {
        CommandNode node = new CommandNode(NodeType.PARAM);
        node.setValue(param);
        return node;
    }

    public static CommandNode joinNode() {
        CommandNode node = new CommandNode(NodeType.JOIN);
        node.setValue(Constants.EMPTY);
        return node;
    }

    enum NodeType {
        //结点类型
        COMMAND,
        PARAM,
        JOIN
    }
}
