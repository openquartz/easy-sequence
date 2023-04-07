package com.openquartz.sequence.core.expr.persist.model;

import java.util.HashMap;
import java.util.Map;

/**
 * SequencePoolProperties
 *
 * @author svnee
 **/
public class SequencePoolProperties {

    /**
     * enable
     */
    private boolean enable = true;

    /**
     * max try count
     * while get
     */
    private int maxTryCount = 5;

    /**
     * 同步等待获取超时时间
     * 单位：毫秒
     */
    private long waitFetchTimeout = 5000;

    /**
     * default 配置
     */
    private SequencePoolProperty defaultProperty;

    /**
     * registerCode --> property
     */
    private Map<String, SequencePoolProperty> registerCode2Property = new HashMap<>();

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public int getMaxTryCount() {
        return maxTryCount;
    }

    public void setMaxTryCount(int maxTryCount) {
        this.maxTryCount = maxTryCount;
    }

    public long getWaitFetchTimeout() {
        return waitFetchTimeout;
    }

    public void setWaitFetchTimeout(long waitFetchTimeout) {
        this.waitFetchTimeout = waitFetchTimeout;
    }

    public void setRegisterCode2Property(Map<String, SequencePoolProperty> registerCode2Property) {
        this.registerCode2Property = registerCode2Property;
    }

    public void setDefaultProperty(SequencePoolProperty defaultProperty) {
        this.defaultProperty = defaultProperty;
    }

    public long getPreCount(String registerCode) {
        return registerCode2Property.getOrDefault(registerCode, defaultProperty).getPreCount();
    }

    public int getWaterLevelThreshold(String registerCode) {
        return registerCode2Property.getOrDefault(registerCode, defaultProperty).getWaterLevelThreshold();
    }

    @Override
    public String toString() {
        return "SequencePoolProperties{" +
            "enable=" + enable +
            ", maxTryCount=" + maxTryCount +
            ", waitFetchTimeout=" + waitFetchTimeout +
            ", defaultProperty=" + defaultProperty +
            ", registerCode2Property=" + registerCode2Property +
            '}';
    }

    /**
     * SequencePoolProperty
     *
     * @author svnee
     */
    public static class SequencePoolProperty {

        /**
         * every get seq pre count
         */
        private long preCount = 50;

        /**
         * 水位保持线
         */
        private int waterLevelThreshold = 5;

        public long getPreCount() {
            return preCount;
        }

        public void setPreCount(long preCount) {
            this.preCount = preCount;
        }

        public int getWaterLevelThreshold() {
            return waterLevelThreshold;
        }

        public void setWaterLevelThreshold(int waterLevelThreshold) {
            this.waterLevelThreshold = waterLevelThreshold;
        }

        @Override
        public String toString() {
            return "SequencePoolProperty{" +
                "preCount=" + preCount +
                ", waterLevelThreshold=" + waterLevelThreshold +
                '}';
        }
    }
}
