package com.openquartz.sequence.generator.example.uid;

import com.openquartz.sequence.core.uid.snowflake.SnowflakeIdProvider;
import com.openquartz.sequence.core.uid.snowflake.cache.CacheSnowflakeIdProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author svnee
 **/
@Component
public class SnowflakeIdProviderSupport {

    @Autowired(required = false)
    private SnowflakeIdProvider snowflakeIdProvider;
    @Autowired(required = false)
    private CacheSnowflakeIdProvider cacheSnowflakeIdProvider;

    public Long generateId() {
        return snowflakeIdProvider.nextId();
    }

    public Long generateId(String key) {
        return snowflakeIdProvider.nextId(key);
    }

    public Long generateCacheId() {
        return cacheSnowflakeIdProvider.nextId();
    }

    public Long generateCacheId(String key) {
        return cacheSnowflakeIdProvider.nextId(key);
    }

}
