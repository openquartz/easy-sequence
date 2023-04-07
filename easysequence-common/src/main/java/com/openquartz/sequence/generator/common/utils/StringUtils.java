package com.openquartz.sequence.generator.common.utils;

/**
 * StringUtils
 *
 * @author svnee
 **/
public final class StringUtils {

    private StringUtils() {
    }

    /**
     * 不为空
     *
     * @param cs char
     * @return 是否不为空
     */
    public static boolean isNotBlank(final CharSequence cs) {
        return !isBlank(cs);
    }

    /**
     * 是否为空
     *
     * @param cs cs
     * @return 是否为空
     */
    public static boolean isBlank(final CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Is all not empty.
     */
    public static boolean isAllNotEmpty(CharSequence... args) {
        return !hasEmpty(args);
    }

    /**
     * Has empty.
     */
    public static boolean hasEmpty(CharSequence... strList) {
        if (strList == null || strList.length == 0) {
            return true;
        }

        for (CharSequence str : strList) {
            if (isEmpty(str)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isEmpty(CharSequence str) {
        return str == null || str.length() == 0;
    }

    /**
     * split prefix
     *
     * @param source source
     * @param maxLength max length
     * @return prefix string
     */
    public static String splitPrefix(String source, int maxLength) {
        if (StringUtils.isBlank(source) || source.length() <= maxLength) {
            return source;
        }
        return source.substring(0, maxLength);
    }

    /**
     * Str is Numeric
     *
     * @param cs char
     * @return numeric
     */
    public static boolean isNumeric(final CharSequence cs) {
        if (isEmpty(cs)) {
            return false;
        }
        final int sz = cs.length();
        for (int i = 0; i < sz; i++) {
            if (!Character.isDigit(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
