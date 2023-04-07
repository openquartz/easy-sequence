package com.openquartz.sequence.core.expr.cmd;

import com.openquartz.sequence.core.expr.cmd.CommandNode.NodeType;
import com.openquartz.sequence.core.expr.exception.CommandExceptionCode;
import com.openquartz.sequence.generator.common.constant.Constants;
import com.openquartz.sequence.generator.common.exception.Asserts;
import com.openquartz.sequence.generator.common.utils.Pair;
import com.openquartz.sequence.generator.common.utils.StringUtils;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

/**
 * @author svnee
 */
@Slf4j
public final class CommandTreeParser {

    private CommandTreeParser() {
    }

    /**
     * 左括号
     */
    private static final Character LEFT_BRACE = '{';

    /**
     * 右括号
     */
    private static final Character RIGHT_BRACE = '}';

    /**
     * 默认链接字符串
     */
    private static final String JOIN_MARK = Constants.EMPTY;

    public static CommandNode parseExpr(String expr) {
        validateTemplate(expr);
        return parse(expr);
    }

    private static void validateTemplate(String template) {

        Asserts.isTrue(template.startsWith(String.valueOf(LEFT_BRACE)),
            CommandExceptionCode.COMMAND_TEMPLATE_ILLEGAL_ERROR);
        Asserts.isTrue(template.endsWith(String.valueOf(RIGHT_BRACE)),
            CommandExceptionCode.COMMAND_TEMPLATE_ILLEGAL_ERROR);

        Deque<Character> st = new ArrayDeque<>();
        for (Character c : template.toCharArray()) {
            if (st.isEmpty()) {
                Asserts.isTrue(LEFT_BRACE.equals(c), CommandExceptionCode.COMMAND_TEMPLATE_ILLEGAL_ERROR);
            }
            if (LEFT_BRACE.equals(c)) {
                st.push(c);
            } else if (RIGHT_BRACE.equals(c)) {
                st.pop();
            }
        }
        Asserts.isTrue(st.isEmpty(), CommandExceptionCode.COMMAND_TEMPLATE_ILLEGAL_ERROR);
    }

    private static CommandNode parse(String expr) {
        return recursiveParse(expr);
    }

    private static CommandNode recursiveParse(String expr) {
        // remove outermost layer brace
        expr = expr.substring(1, expr.length() - 1).trim();
        if (StringUtils.isBlank(expr)) {
            return null;
        }

        String finalExpr = expr;
        Asserts.isTrueIfLog(!expr.startsWith(String.valueOf(LEFT_BRACE)),
            () -> log.error("[CommandTreeParser#recursiveParse] parse error! command template illegal! expr:{}",
                finalExpr),
            CommandExceptionCode.COMMAND_TEMPLATE_ILLEGAL_ERROR);

        CommandNode commandNode = new CommandNode(NodeType.COMMAND);

        // split cmd and param
        Pair<String, List<String>> pair = splitCmdAndParam(expr);
        commandNode.setValue(pair.getLeft());

        List<String> params = pair.getRight();
        List<CommandNode> children = params.stream()
            .map(CommandTreeParser::getCommandNode)
            .collect(Collectors.toList());
        commandNode.setChildren(children);

        return commandNode;
    }

    private static CommandNode getCommandNode(String part) {
        if (part.startsWith(String.valueOf(LEFT_BRACE))) {
            return recursiveParse(part);
        }
        return isJoinMark(part) ? CommandNode.joinNode() : CommandNode.fromParam(part);
    }

    private static Pair<String, List<String>> splitCmdAndParam(String expr) {
        List<String> parts = new ArrayList<>();
        Deque<Character> st = new ArrayDeque<>();
        StringBuilder tmp = new StringBuilder();
        Character lastCharacter = null;
        for (Character c : expr.toCharArray()) {
            if (LEFT_BRACE.equals(c)) {
                if (st.isEmpty()) {
                    String tmpValue = tmp.toString();
                    if (StringUtils.isNotBlank(tmpValue)) {
                        parts.add(tmpValue);
                        //add join mark
                        parts.add(JOIN_MARK);
                        tmp = new StringBuilder(String.valueOf(c));
                    } else {
                        if (RIGHT_BRACE.equals(lastCharacter)) {
                            //add join mark
                            parts.add(JOIN_MARK);
                        }
                        tmp.append(c);
                    }
                } else {
                    tmp.append(c);
                }
                st.push(c);
            } else if (RIGHT_BRACE.equals(c)) {
                tmp.append(c);
                st.pop();
                if (st.isEmpty()) {
                    String tmpValue = tmp.toString();
                    parts.add(tmpValue);
                    tmp = new StringBuilder();
                }
            } else if (Character.isSpaceChar(c) && st.isEmpty()) {
                String tmpValue = tmp.toString();
                if (StringUtils.isNotBlank(tmpValue)) {
                    parts.add(tmp.toString());
                    if (RIGHT_BRACE.equals(lastCharacter)) {
                        // add join mark
                        parts.add(JOIN_MARK);
                    }
                    tmp = new StringBuilder();
                }
            } else {
                tmp.append(c);
            }
            lastCharacter = c;
        }
        String tmpValue = tmp.toString();
        if (StringUtils.isNotBlank(tmpValue)) {
            parts.add(tmpValue);
        }
        return Pair.of(parts.get(0), parts.subList(1, parts.size()));
    }

    private static boolean isJoinMark(String part) {
        return JOIN_MARK.equals(part);
    }

}
