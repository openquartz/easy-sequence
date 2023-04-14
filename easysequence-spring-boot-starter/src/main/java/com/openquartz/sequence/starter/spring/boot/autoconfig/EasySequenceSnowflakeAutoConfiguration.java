package com.openquartz.sequence.starter.spring.boot.autoconfig;

import static com.openquartz.sequence.core.uid.snowflake.exception.SnowflakeExceptionCode.SNOWFLAKE_DEFAULT_WORKER_ID_OVER_ERROR;
import static com.openquartz.sequence.core.uid.snowflake.exception.SnowflakeExceptionCode.SNOWFLAKE_WORKER_ID_OVER_ERROR;

import com.openquartz.sequence.core.uid.snowflake.SnowflakeIdProvider;
import com.openquartz.sequence.core.uid.snowflake.cache.CacheSnowflakeIdProvider;
import com.openquartz.sequence.core.uid.snowflake.worker.WorkerIdAssigner;
import com.openquartz.sequence.generator.common.exception.Asserts;
import com.openquartz.sequence.generator.common.transaction.TransactionSupport;
import com.openquartz.sequence.starter.persist.WorkerNodeDAO;
import com.openquartz.sequence.starter.spring.boot.autoconfig.property.EasySequenceSnowflakeProperties;
import com.openquartz.sequence.starter.spring.boot.autoconfig.property.EasySequenceSnowflakeProperties.WorkerId;
import com.openquartz.sequence.starter.spring.boot.autoconfig.property.SnowflakeType;
import com.openquartz.sequence.starter.spring.boot.autoconfig.property.WorkerIdConstants;
import com.openquartz.sequence.starter.worker.WorkerIdAssignerProperty;
import com.openquartz.sequence.starter.worker.DatabaseWorkerIdAssigner;
import com.openquartz.sequence.starter.worker.RandomWorkerIdAssigner;
import com.openquartz.sequence.starter.worker.ZkWorkerIdAssigner;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
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
@EnableConfigurationProperties(EasySequenceSnowflakeProperties.class)
@ConditionalOnProperty(prefix = EasySequenceSnowflakeProperties.PREFIX, value = "enabled", havingValue = "true")
public class EasySequenceSnowflakeAutoConfiguration {

    @Value("${server.port}")
    private String port;

    @Bean
    @ConditionalOnMissingBean(WorkerNodeDAO.class)
    public WorkerNodeDAO workerNodeDAO(@Qualifier("sequenceJdbcTemplate") JdbcTemplate jdbcTemplate) {
        return new WorkerNodeDAO(jdbcTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(WorkerIdAssigner.class)
    @ConditionalOnProperty(prefix = EasySequenceSnowflakeProperties.PREFIX, value = "worker-assigner-type", havingValue = "random", matchIfMissing = true)
    public WorkerIdAssigner randomWorIdAssigner(WorkerIdAssignerProperty workerIdAssignerProperty) {
        return new RandomWorkerIdAssigner(workerIdAssignerProperty);
    }

    @Bean
    public WorkerIdAssignerProperty workerIdAssignerProperty(
        EasySequenceSnowflakeProperties easySequenceSnowflakeProperties) {

        Asserts
            .isTrue(easySequenceSnowflakeProperties.getWorkerId().getDefaultVal() >=
                    easySequenceSnowflakeProperties.getWorkerId().getMin(),
                SNOWFLAKE_DEFAULT_WORKER_ID_OVER_ERROR,
                easySequenceSnowflakeProperties.getWorkerId().getMin(),
                easySequenceSnowflakeProperties.getWorkerId().getMax());
        Asserts
            .isTrue(easySequenceSnowflakeProperties.getWorkerId().getDefaultVal() <
                    easySequenceSnowflakeProperties.getWorkerId().getMax(),
                SNOWFLAKE_DEFAULT_WORKER_ID_OVER_ERROR,
                easySequenceSnowflakeProperties.getWorkerId().getMin(),
                easySequenceSnowflakeProperties.getWorkerId().getMax());

        if (easySequenceSnowflakeProperties.getType() == SnowflakeType.DEFAULT) {
            checkWorkerIdProperty(easySequenceSnowflakeProperties.getWorkerId(),
                WorkerIdConstants.DEFAULT_MAX_WORKER_ID);
        } else {
            checkWorkerIdProperty(easySequenceSnowflakeProperties.getWorkerId(),
                WorkerIdConstants.CACHE_MAX_WORKER_ID);
        }

        WorkerIdAssignerProperty property = new WorkerIdAssignerProperty();
        property.setGroup(easySequenceSnowflakeProperties.getDefaultGroup());
        property.setWorkerHeartbeatInterval(easySequenceSnowflakeProperties.getDb().getWorkerHeartbeatInterval());
        property.setWorkerExpireInterval(easySequenceSnowflakeProperties.getDb().getWorkerExpireInterval());
        property.setMinWorkerId(easySequenceSnowflakeProperties.getWorkerId().getMin());
        property.setMaxWorkerId(easySequenceSnowflakeProperties.getWorkerId().getMax());
        return property;
    }

    private void checkWorkerIdProperty(WorkerId workerId, Integer maxId) {
        Asserts.isTrue(workerId.getMin() >= WorkerIdConstants.MIN_WORKER_ID, SNOWFLAKE_WORKER_ID_OVER_ERROR,
            WorkerIdConstants.MIN_WORKER_ID, maxId);
        Asserts.isTrue(workerId.getMax() <= maxId, SNOWFLAKE_WORKER_ID_OVER_ERROR,
            WorkerIdConstants.MIN_WORKER_ID, maxId);
    }

    @Bean
    @ConditionalOnMissingBean(WorkerIdAssigner.class)
    @ConditionalOnProperty(prefix = EasySequenceSnowflakeProperties.PREFIX, value = "worker-assigner-type", havingValue = "db")
    public WorkerIdAssigner dbWorIdAssigner(WorkerIdAssignerProperty workerIdAssignerProperty,
        WorkerNodeDAO workerNodeDAO,
        TransactionSupport transactionSupport) {
        return new DatabaseWorkerIdAssigner(workerIdAssignerProperty, workerNodeDAO, transactionSupport, port);
    }

    @Bean
    @ConditionalOnClass(ZooKeeper.class)
    @ConditionalOnMissingBean(WorkerIdAssigner.class)
    @ConditionalOnProperty(prefix = EasySequenceSnowflakeProperties.PREFIX, value = "worker-assigner-type", havingValue = "zookeeper")
    public WorkerIdAssigner zookeeperWorIdAssigner(EasySequenceSnowflakeProperties easySequenceSnowflakeProperties) {

        ZkWorkerIdAssigner zkWorkerIdAssigner = new ZkWorkerIdAssigner();
        zkWorkerIdAssigner.setZkAddress(easySequenceSnowflakeProperties.getZookeeper().getUrl());
        zkWorkerIdAssigner.setInterval(easySequenceSnowflakeProperties.getZookeeper().getWorkerHeartbeatInterval());
        zkWorkerIdAssigner.setPidHome(easySequenceSnowflakeProperties.getZookeeper().getWorkerPidHome());
        zkWorkerIdAssigner.setPidPort(easySequenceSnowflakeProperties.getZookeeper().getWorkerPidPort());
        return zkWorkerIdAssigner;
    }

    @Bean
    @ConditionalOnMissingBean(SnowflakeIdProvider.class)
    @ConditionalOnProperty(prefix = EasySequenceSnowflakeProperties.PREFIX, value = "type", havingValue = "default")
    public SnowflakeIdProvider snowflakeIdProvider(EasySequenceSnowflakeProperties easySequenceSnowflakeProperties,
        WorkerIdAssigner workerIdAssigner) {

        SnowflakeIdProvider provider = new SnowflakeIdProvider();
        provider.setDatacenterId(easySequenceSnowflakeProperties.getDatacenterId());
        provider.setAssigner(workerIdAssigner);
        provider.setWorkerId(easySequenceSnowflakeProperties.getWorkerId().getDefaultVal());
        provider.setDefaultGroup(easySequenceSnowflakeProperties.getDefaultGroup());
        return provider;
    }

    @Bean(initMethod = "init", destroyMethod = "destroy")
    @ConditionalOnMissingBean(CacheSnowflakeIdProvider.class)
    @ConditionalOnProperty(prefix = EasySequenceSnowflakeProperties.PREFIX, value = "type", havingValue = "cache")
    public CacheSnowflakeIdProvider cacheIdProvider(EasySequenceSnowflakeProperties easySequenceSnowflakeProperties,
        WorkerIdAssigner workerIdAssigner) {
        return new CacheSnowflakeIdProvider(workerIdAssigner, easySequenceSnowflakeProperties.getDefaultGroup());
    }

}
