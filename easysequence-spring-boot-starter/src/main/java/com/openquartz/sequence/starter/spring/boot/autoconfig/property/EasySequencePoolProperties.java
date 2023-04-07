package com.openquartz.sequence.starter.spring.boot.autoconfig.property;

import java.util.Map;
import java.util.TreeMap;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * EasySequenceProperties
 *
 * @author svnee
 **/
@ConfigurationProperties(prefix = EasySequencePoolProperties.PREFIX)
public class EasySequencePoolProperties {

    public static final String PREFIX = "easysequence.sequence.pool";

    /**
     * enable
     */
    private boolean enable = true;

    /**
     * 最大重试获取次数
     */
    private int maxTryCount = 5;

    /**
     * 同步等待获取超时时间
     */
    private long waitFetchTimeout = 5000;

    /**
     * default 配置
     */
    private EasySequencePoolProperty defaultProperty = new EasySequencePoolProperty();

    /**
     * registerCode --> property
     */
    private Map<String, EasySequencePoolProperty> customProperty = new TreeMap<>();

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

    public Map<String, EasySequencePoolProperty> getCustomProperty() {
        return customProperty;
    }

    public void setCustomProperty(
        Map<String, EasySequencePoolProperty> customProperty) {
        this.customProperty = customProperty;
    }

    public EasySequencePoolProperty getDefaultProperty() {
        return defaultProperty;
    }

    public void setDefaultProperty(
        EasySequencePoolProperty defaultProperty) {
        this.defaultProperty = defaultProperty;
    }

    public long getPreCount(String registerCode) {
        return customProperty.getOrDefault(registerCode, defaultProperty).getPreCount();
    }

    public int getWaterLevelThreshold(String registerCode) {
        return customProperty.getOrDefault(registerCode, defaultProperty).getWaterLevelThreshold();
    }

    public static class EasySequencePoolProperty {

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
    }

}
