package com.openquartz.sequence.core.uid.snowflake.cache;

/**
 * Strategy for assign workerId
 *
 * @author svnee
 */
public enum WorkerIdAssignerStrategy {

    DISPOSABLE(0, "disposable"),
    LOOP(1, "loop");

    private final Integer value;
    private final String name;

    public Integer getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    WorkerIdAssignerStrategy(Integer value, String name) {
        this.value = value;
        this.name = name;
    }

    /**
     * get WorkerIdAssignerStrategy by value. Default is DISPOSABLE
     */
    public static WorkerIdAssignerStrategy valueOf(Integer value) {
        for (WorkerIdAssignerStrategy item : values()) {
            if (item.value.equals(value)) {
                return item;
            }
        }
        return DISPOSABLE;
    }
}
