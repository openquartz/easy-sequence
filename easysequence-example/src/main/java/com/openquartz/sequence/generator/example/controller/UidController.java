package com.openquartz.sequence.generator.example.controller;

import com.openquartz.sequence.generator.common.utils.StringUtils;
import com.openquartz.sequence.generator.example.uid.LeafIdProvider;
import com.openquartz.sequence.generator.example.uid.SnowflakeIdProviderSupport;
import javax.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * UID
 *
 * @author svnee
 **/
@RestController
@RequestMapping("/uid")
public class UidController {

    @Resource
    private SnowflakeIdProviderSupport snowflakeIdProviderSupport;
    @Resource
    private LeafIdProvider leafIdProvider;

    @GetMapping("/snowflake/get")
    public long getSnowflakeId(@RequestParam("key") String key) {
        if (StringUtils.isBlank(key)) {
            return snowflakeIdProviderSupport.generateId();
        }
        return snowflakeIdProviderSupport.generateId(key);
    }

    @GetMapping("/snowflake/cache/get")
    public long getCacheSnowflakeId(@RequestParam("key") String key) {
        if (StringUtils.isBlank(key)) {
            return snowflakeIdProviderSupport.generateCacheId();
        }
        return snowflakeIdProviderSupport.generateCacheId(key);
    }

    @GetMapping("/leaf/get")
    public long getLeafId() {
        return leafIdProvider.generateId();
    }

}
