package com.openquartz.sequence.starter.spring.boot.autoconfig;

import com.openquartz.sequence.core.expr.SequenceGenerateService;
import com.openquartz.sequence.core.expr.SequenceGenerateServiceImpl;
import com.openquartz.sequence.core.expr.SequenceTemplateProvider;
import com.openquartz.sequence.core.expr.persist.SequenceIncrService;
import com.openquartz.sequence.core.expr.persist.impl.DatabaseSequenceTemplateProviderImpl;
import com.openquartz.sequence.core.expr.persist.impl.SequenceIncrServiceImpl;
import com.openquartz.sequence.core.expr.persist.model.SequencePoolProperties;
import com.openquartz.sequence.core.expr.persist.model.SequencePoolProperties.SequencePoolProperty;
import com.openquartz.sequence.core.persist.mapper.SequenceAssignRegisterMapper;
import com.openquartz.sequence.core.persist.mapper.SequenceNextAssignMapper;
import com.openquartz.sequence.core.persist.mapper.SequenceTemplateMapper;
import com.openquartz.sequence.generator.common.utils.SpringContextUtil;
import com.openquartz.sequence.generator.common.utils.StringUtils;
import com.openquartz.sequence.starter.persist.SequenceAssignRegisterMapperImpl;
import com.openquartz.sequence.starter.persist.SequenceNextAssignMapperImpl;
import com.openquartz.sequence.starter.persist.SequenceTemplateMapperImpl;
import com.openquartz.sequence.starter.spring.boot.autoconfig.property.EasySequenceDatasourceProperties;
import com.openquartz.sequence.starter.spring.boot.autoconfig.property.EasySequencePoolProperties;
import com.openquartz.sequence.starter.spring.boot.autoconfig.property.EasySequencePoolProperties.EasySequencePoolProperty;
import com.openquartz.sequence.starter.transaction.TransactionSupport;
import com.openquartz.sequence.starter.transaction.TransactionSupportImpl;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * EasyFileLocalStorageAutoConfiguration
 *
 * @author svnee
 **/
@Slf4j
@Configuration
@EnableConfigurationProperties({EasySequenceDatasourceProperties.class, EasySequencePoolProperties.class})
public class EasySequenceCreatorAutoConfiguration {

    private static final String DEFAULT_DATASOURCE_TYPE = "org.apache.tomcat.jdbc.pool.DataSource";

    @Bean
    @ConditionalOnMissingBean(type = "easySequenceLocalStorageDataSource", value = DataSource.class)
    public DataSource easySequenceLocalStorageDataSource(
        EasySequenceDatasourceProperties easySequenceDatasourceProperties,
        Environment environment) {

        Iterable<ConfigurationPropertySource> sources = ConfigurationPropertySources
            .get(environment);
        Binder binder = new Binder(sources);
        Properties properties = binder.bind(EasySequenceDatasourceProperties.PREFIX, Properties.class).get();

        DataSource dataSource = buildDataSource(easySequenceDatasourceProperties);
        buildDataSourceProperties(dataSource, properties);
        return dataSource;
    }

    @Bean
    public TransactionSupport transactionSupport(TransactionTemplate transactionTemplate) {
        return new TransactionSupportImpl(transactionTemplate);
    }

    private void buildDataSourceProperties(DataSource dataSource, Map<Object, Object> dsMap) {
        try {
            BeanUtils.copyProperties(dataSource, dsMap);
        } catch (Exception e) {
            log.error("[EasySequenceCreatorAutoConfiguration#buildDataSourceProperties] error copy properties", e);
        }
    }

    private DataSource buildDataSource(EasySequenceDatasourceProperties easySequenceDatasourceProperties) {
        String dataSourceType = easySequenceDatasourceProperties.getType();
        try {
            String className = StringUtils.isNotBlank(dataSourceType) ? dataSourceType : DEFAULT_DATASOURCE_TYPE;
            Class<? extends DataSource> type = (Class<? extends DataSource>) Class.forName(className);
            String driverClassName = easySequenceDatasourceProperties.getDriverClassName();
            String url = easySequenceDatasourceProperties.getUrl();
            String username = easySequenceDatasourceProperties.getUsername();
            String password = easySequenceDatasourceProperties.getPassword();

            return DataSourceBuilder.create()
                .driverClassName(driverClassName)
                .url(url)
                .username(username)
                .password(password)
                .type(type)
                .build();

        } catch (ClassNotFoundException e) {
            log.error("[EasySequenceCreatorAutoConfiguration#buildDataSource]buildDataSource error", e);
            throw new IllegalStateException(e);
        }
    }

    @Bean
    @ConditionalOnMissingBean(SequenceAssignRegisterMapper.class)
    @ConditionalOnClass(SequenceAssignRegisterMapper.class)
    public SequenceAssignRegisterMapper sequenceAssignRegisterMapper(
        @Qualifier("sequenceJdbcTemplate") JdbcTemplate jdbcTemplate) {
        return new SequenceAssignRegisterMapperImpl(jdbcTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(type = "easySequenceLocalStorageDataSource", value = JdbcTemplate.class)
    public JdbcTemplate sequenceJdbcTemplate(@Qualifier("easySequenceLocalStorageDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    @ConditionalOnMissingBean(SequenceNextAssignMapper.class)
    @ConditionalOnClass(SequenceNextAssignMapper.class)
    public SequenceNextAssignMapper sequenceNextAssignMapper(
        @Qualifier("sequenceJdbcTemplate") JdbcTemplate jdbcTemplate) {
        return new SequenceNextAssignMapperImpl(jdbcTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(SequenceTemplateMapper.class)
    @ConditionalOnClass(SequenceTemplateMapper.class)
    public SequenceTemplateMapper sequenceTemplateMapper(@Qualifier("sequenceJdbcTemplate") JdbcTemplate jdbcTemplate) {
        return new SequenceTemplateMapperImpl(jdbcTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(SequenceGenerateService.class)
    public SequenceGenerateService sequenceGenerateServiceImpl(SequenceTemplateProvider sequenceTemplateProvider) {
        return new SequenceGenerateServiceImpl(sequenceTemplateProvider);
    }

    @Bean
    @ConditionalOnMissingBean(SequenceTemplateProvider.class)
    public SequenceTemplateProvider sequenceTemplateProvider(SequenceTemplateMapper sequenceTemplateMapper) {
        return new DatabaseSequenceTemplateProviderImpl(sequenceTemplateMapper);
    }

    @Bean(destroyMethod = "destroy", initMethod = "init")
    @ConditionalOnMissingBean(SequenceIncrService.class)
    public SequenceIncrService sequenceIncrService(SequenceNextAssignMapper sequenceNextAssignMapper,
        SequenceAssignRegisterMapper sequenceAssignRegisterMapper,
        EasySequencePoolProperties easySequencePoolProperties) {

        SequencePoolProperties sequencePoolProperties = new SequencePoolProperties();
        sequencePoolProperties.setEnable(easySequencePoolProperties.isEnable());
        sequencePoolProperties.setWaitFetchTimeout(easySequencePoolProperties.getWaitFetchTimeout());
        sequencePoolProperties.setMaxTryCount(easySequencePoolProperties.getMaxTryCount());

        Map<String, SequencePoolProperty> registerCode2Property = easySequencePoolProperties.getCustomProperty()
            .entrySet()
            .stream()
            .collect(Collectors.toMap(Entry::getKey, e -> convert(e.getValue())));

        sequencePoolProperties.setRegisterCode2Property(registerCode2Property);
        sequencePoolProperties.setDefaultProperty(convert(easySequencePoolProperties.getDefaultProperty()));

        return new SequenceIncrServiceImpl(sequenceNextAssignMapper, sequenceAssignRegisterMapper,
            sequencePoolProperties);
    }

    private SequencePoolProperty convert(EasySequencePoolProperty property) {
        SequencePoolProperty sequencePoolProperty = new SequencePoolProperty();
        sequencePoolProperty.setPreCount(property.getPreCount());
        sequencePoolProperty.setWaterLevelThreshold(property.getWaterLevelThreshold());
        return sequencePoolProperty;
    }

    @Bean
    public SpringContextUtil springContextUtil() {
        return new SpringContextUtil();
    }


}
