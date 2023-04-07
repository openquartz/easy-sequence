package com.openquartz.sequence.starter.spring.boot.autoconfig.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * EasySequenceLocalProperties
 *
 * @author svnee
 **/
@Getter
@Setter
@ConfigurationProperties(prefix = EasySequenceDatasourceProperties.PREFIX)
public class EasySequenceDatasourceProperties {

    public static final String PREFIX = "easysequence.datasource";

    /**
     * type
     */
    private String type;

    /**
     * jdbcUrl
     */
    private String url;

    /**
     * driver-class-name
     */
    private String driverClassName;

    /**
     * username
     */
    private String username;

    /**
     * password
     */
    private String password;


}
