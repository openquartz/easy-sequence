package com.openquartz.sequence.core.uid.snowflake;

import com.openquartz.sequence.core.uid.UidProvider;
import com.openquartz.sequence.core.uid.snowflake.worker.WorkerIdAssigner;
import com.openquartz.sequence.generator.common.utils.NetUtils;
import com.openquartz.sequence.generator.common.utils.StringUtils;
import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;

/**
 * 雪花算法ID 生成器
 *
 * @author svnee
 */
@Slf4j
public class SnowflakeIdProvider implements UidProvider {

    /**
     * 生成器集合
     */
    private static final Map<String, SnowflakeIdWorker> GENERATOR_MAP = new ConcurrentHashMap<>();

    /**
     * 机器ID
     */
    private Long workerId;

    /**
     * 数据中心id
     */
    private Long datacenterId;

    /**
     * work-assigner
     */
    protected WorkerIdAssigner assigner;

    /**
     * default-group
     */
    private String defaultGroup;

    /**
     * 获取ID
     *
     * @return UID
     */
    @Override
    public long nextId() {
        return nextId(defaultGroup);
    }

    /**
     * 反解析uid
     *
     * @param uid uid
     * @return 解析结果
     */
    @Override
    public String parseUid(long uid) {
        return parseUid(defaultGroup, uid);
    }

    /**
     * getUID
     *
     * @param key group
     * @return uid
     */
    @Override
    public long nextId(String key) {
        return getSnowflakeId(key).nextId();
    }

    /**
     * uid
     *
     * @param uid uid
     * @param key group
     * @return parse result
     */
    @Override
    public String parseUid(String key, long uid) {
        return getSnowflakeId(key).parseUID(uid);
    }

    /**
     * 获取uid生成器
     *
     * @param prefix 前缀
     * @return uid生成器
     */
    public SnowflakeIdWorker getSnowflakeId(String prefix) {
        SnowflakeIdWorker snowflakeIdWorker = GENERATOR_MAP.get(prefix);
        if (null == snowflakeIdWorker) {
            synchronized (GENERATOR_MAP) {
                // 数据中心id--默认取机器码
                long realDid = null == datacenterId ? getMachineNum(31) : datacenterId;
                // 机器id--默认取进程id
                long realWid;
                if (null != assigner) {
                    realWid = assigner.assignWorkerId();
                } else if (null != workerId) {
                    realWid = workerId;
                } else {
                    realWid = getProcessNum(realDid, 31);
                }
                snowflakeIdWorker = new SnowflakeIdWorker(realWid, realDid);
                snowflakeIdWorker.setClock(true);
                GENERATOR_MAP.put(prefix, snowflakeIdWorker);
            }
        }
        return snowflakeIdWorker;
    }

    /**
     * 获取机器码
     *
     * @param maxId 最大值
     */
    public static long getMachineNum(long maxId) {
        byte[] mac = NetUtils.getMachineNum();
        long id;
        if (mac == null) {
            id = 1L;
        } else {
            id = ((0x000000FF & (long) mac[mac.length - 1]) | (0x0000FF00 & (((long) mac[mac.length - 2]) << 8))) >> 6;
            id = id % (maxId + 1);
        }
        return id;
    }

    /**
     * 获取 进程id
     *
     * @param dataCenterId 数据中心id
     * @param maxWorkerId 最大机器id
     */
    public static long getProcessNum(long dataCenterId, long maxWorkerId) {
        StringBuilder mpid = new StringBuilder();
        mpid.append(dataCenterId);
        String name = ManagementFactory.getRuntimeMXBean().getName();
        if (StringUtils.isNotBlank(name)) {
            // 获取 jvm Pid
            mpid.append(name.split("@")[0]);
        }
        // dataCenterId + PID 的 hashcode 获取16个低位
        return (mpid.toString().hashCode() & 0xffff) % (maxWorkerId + 1);
    }

    public Long getWorkerId() {
        return workerId;
    }

    public void setWorkerId(Long workerId) {
        this.workerId = workerId;
    }

    public Long getDatacenterId() {
        return datacenterId;
    }

    public void setDatacenterId(Long datacenterId) {
        this.datacenterId = datacenterId;
    }

    public void setAssigner(WorkerIdAssigner assigner) {
        this.assigner = assigner;
    }

    public String getDefaultGroup() {
        return defaultGroup;
    }

    public void setDefaultGroup(String defaultGroup) {
        this.defaultGroup = defaultGroup;
    }
}
