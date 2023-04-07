package com.openquartz.sequence.starter.spring.boot.autoconfig.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * EasySequenceLeafProperties
 *
 * @author svnee
 **/
@ConfigurationProperties(prefix = EasySequenceLeafProperties.PREFIX)
public class EasySequenceLeafProperties {

    public static final String PREFIX = "easysequence.uid.leaf";

    /**
     * 是否开启
     */
    private boolean enabled = false;

    /**
     * defaultGroup
     * 默认组
     */
    private String defaultKey = "default";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getDefaultKey() {
        return defaultKey;
    }

    public void setDefaultKey(String defaultKey) {
        this.defaultKey = defaultKey;
    }
}
