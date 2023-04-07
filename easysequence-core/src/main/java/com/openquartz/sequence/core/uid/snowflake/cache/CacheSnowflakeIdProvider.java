package com.openquartz.sequence.core.uid.snowflake.cache;

import static com.openquartz.sequence.generator.common.utils.ParamUtils.checkNotEmpty;
import static com.openquartz.sequence.generator.common.utils.ParamUtils.checkNotNull;

import com.openquartz.sequence.core.uid.UidProvider;
import com.openquartz.sequence.core.uid.snowflake.worker.WorkerIdAssigner;
import com.openquartz.sequence.generator.common.bean.LifestyleBean;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author svnee
 **/
public class CacheSnowflakeIdProvider implements UidProvider, LifestyleBean {

    /**
     * 生成器集合
     */
    private static final Map<String, CachedUidGenerator> GENERATOR_MAP = new ConcurrentHashMap<>();

    /**
     * workerIdAssigner
     */
    private final WorkerIdAssigner workerIdAssigner;

    /**
     * default-group
     */
    private final String defaultGroup;

    /**
     * workerId
     */
    private long workerId;

    /**
     * CacheSnowflakeIdProvider
     *
     * @param workerIdAssigner workerIdAssigner
     */
    public CacheSnowflakeIdProvider(WorkerIdAssigner workerIdAssigner, String defaultGroup) {

        // check not empty
        checkNotNull(workerIdAssigner);
        checkNotEmpty(defaultGroup);

        this.workerIdAssigner = workerIdAssigner;
        this.defaultGroup = defaultGroup;
    }

    /**
     * 获取uid生成器
     *
     * @param prefix 前缀
     * @return uid生成器
     */
    public CachedUidGenerator getUidGenerator(String prefix) {
        CachedUidGenerator snowflakeIdWorker = GENERATOR_MAP.get(prefix);
        if (null == snowflakeIdWorker) {
            synchronized (GENERATOR_MAP) {
                snowflakeIdWorker = new CachedUidGenerator(workerId);
                GENERATOR_MAP.put(prefix, snowflakeIdWorker);
            }
        }
        return snowflakeIdWorker;
    }

    @Override
    public long nextId() {
        return getUidGenerator(defaultGroup).nextId();
    }

    @Override
    public long nextId(String key) {
        return getUidGenerator(key).nextId();
    }

    @Override
    public String parseUid(long uid) {
        return getUidGenerator(defaultGroup).parseUid(uid);
    }

    @Override
    public String parseUid(String key, long uid) {
        return getUidGenerator(key).parseUid(uid);
    }

    @Override
    public void init() {

        LifestyleBean.super.init();

        this.workerId = workerIdAssigner.assignWorkerId();
    }

    @Override
    public void destroy() {

        LifestyleBean.super.destroy();

        synchronized (GENERATOR_MAP) {
            for (CachedUidGenerator generator : GENERATOR_MAP.values()) {
                generator.destroy();
            }
        }
    }
}
