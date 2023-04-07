package com.openquartz.sequence.core.dictionary;

import com.openquartz.sequence.generator.common.dictionary.BaseEnum;

/**
 * 循环单位
 *
 * @author svnee
 */
public enum CycleUnit implements BaseEnum<String> {

    DAY("day", "天"),
    WEEK("week", "周"),
    MONTH("month", "月"),
    YEAR("year", "年"),
    ;

    CycleUnit(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private final String code;
    private final String desc;

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getDesc() {
        return desc;
    }

    public static CycleUnit fromCode(String code) {
        for (CycleUnit cycleUnit : CycleUnit.values()) {
            if (cycleUnit.code.equals(code)) {
                return cycleUnit;
            }
        }
        return null;
    }

}
