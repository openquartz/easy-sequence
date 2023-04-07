package com.openquartz.sequence.starter.spring.boot.autoconfig;

import com.openquartz.sequence.core.uid.leaf.LeafIdAllocDAO;
import com.openquartz.sequence.core.uid.leaf.LeafIdGenerator;
import com.openquartz.sequence.core.uid.leaf.property.LeafProperty;
import com.openquartz.sequence.starter.persist.LeafIdAllocDAOImpl;
import com.openquartz.sequence.starter.spring.boot.autoconfig.property.EasySequenceLeafProperties;
import com.openquartz.sequence.starter.transaction.TransactionSupport;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author svnee
 **/
@Configuration
@EnableConfigurationProperties(EasySequenceLeafProperties.class)
@ConditionalOnProperty(prefix = EasySequenceLeafProperties.PREFIX, value = "enabled", havingValue = "true")
public class EasySequenceLeafAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(LeafIdAllocDAO.class)
    public LeafIdAllocDAO leafIdAllocDAO(@Qualifier("sequenceJdbcTemplate") JdbcTemplate jdbcTemplate,
        TransactionSupport transactionSupport) {
        return new LeafIdAllocDAOImpl(jdbcTemplate, transactionSupport);
    }

    @Bean(initMethod = "init", destroyMethod = "destroy")
    @ConditionalOnMissingBean(LeafIdGenerator.class)
    public LeafIdGenerator leafIdGenerator(LeafIdAllocDAO leafIdAllocDAO,
        EasySequenceLeafProperties easySequenceLeafProperties) {
        LeafProperty property = new LeafProperty();
        property.setDefaultKey(easySequenceLeafProperties.getDefaultKey());
        return new LeafIdGenerator(leafIdAllocDAO, property);
    }

}
