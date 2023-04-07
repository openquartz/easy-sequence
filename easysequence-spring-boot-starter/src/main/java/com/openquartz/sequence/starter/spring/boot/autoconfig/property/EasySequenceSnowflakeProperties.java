package com.openquartz.sequence.starter.spring.boot.autoconfig.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 雪花算法ID配置
 *
 * @author svnee
 **/
@ConfigurationProperties(prefix = EasySequenceSnowflakeProperties.PREFIX)
public class EasySequenceSnowflakeProperties {

    public static final String PREFIX = "easysequence.uid.snowflake";

    /**
     * 是否开启
     */
    private boolean enabled = false;

    /**
     * 雪花算法类型
     * 默认是 标准版本实现
     * 可选用 百度实现的cache uid.性能更强。
     */
    private SnowflakeType type = SnowflakeType.DEFAULT;

    /**
     * defaultGroup
     * 默认组
     */
    private String defaultGroup = "default";

    /**
     * 数据中心ID
     */
    private long datacenterId = 0;

    /**
     * workerId
     */
    private WorkerId workerId = new WorkerId();

    /**
     * 机器工作类型
     * worker-assigner-type
     * 默认是 random类型、支持使用db、zk
     */
    private WorkerAssignType workerAssignerType = WorkerAssignType.RANDOM;

    /**
     * zookeeper property
     */
    private ZookeeperProperty zookeeper = new ZookeeperProperty();

    /**
     * database property
     */
    private DatabaseProperty db = new DatabaseProperty();

    public static class WorkerId {

        private long defaultVal = 0;
        private long min = 0;
        private long max = 32;

        public long getDefaultVal() {
            return defaultVal;
        }

        public void setDefaultVal(long defaultVal) {
            this.defaultVal = defaultVal;
        }

        public long getMin() {
            return min;
        }

        public void setMin(long min) {
            this.min = min;
        }

        public long getMax() {
            return max;
        }

        public void setMax(long max) {
            this.max = max;
        }

        @Override
        public String toString() {
            return "WorkerId{" +
                "defaultVal=" + defaultVal +
                ", min=" + min +
                ", max=" + max +
                '}';
        }
    }

    public static class ZookeeperProperty {

        /**
         * 地址
         * 如使用zk 配置zk的连接地址
         */
        private String url = "";

        /**
         * 心跳 间隔时间
         * 单位：毫秒
         */
        private long workerHeartbeatInterval = 3000;

        /**
         * 使用端口(同机多uid应用时区分端口)
         */
        private int workerPidPort = -1;

        /**
         * workerID 文件存储路径
         */
        private String workerPidHome = "/data/pids/";

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public long getWorkerHeartbeatInterval() {
            return workerHeartbeatInterval;
        }

        public void setWorkerHeartbeatInterval(long workerHeartbeatInterval) {
            this.workerHeartbeatInterval = workerHeartbeatInterval;
        }

        public int getWorkerPidPort() {
            return workerPidPort;
        }

        public void setWorkerPidPort(int workerPidPort) {
            this.workerPidPort = workerPidPort;
        }

        public String getWorkerPidHome() {
            return workerPidHome;
        }

        public void setWorkerPidHome(String workerPidHome) {
            this.workerPidHome = workerPidHome;
        }

        @Override
        public String toString() {
            return "ZookeeperProperty{" +
                "url='" + url + '\'' +
                ", workerHeartbeatInterval=" + workerHeartbeatInterval +
                ", workerPidPort=" + workerPidPort +
                ", workerPidHome='" + workerPidHome + '\'' +
                '}';
        }
    }

    public static class DatabaseProperty {

        /**
         * 心跳 间隔时间
         * 单位：毫秒
         */
        private long workerHeartbeatInterval = 3000;

        /**
         * worker 过期时间
         * 单位：毫秒
         */
        private long workerExpireInterval = 500000;

        public long getWorkerHeartbeatInterval() {
            return workerHeartbeatInterval;
        }

        public void setWorkerHeartbeatInterval(long workerHeartbeatInterval) {
            this.workerHeartbeatInterval = workerHeartbeatInterval;
        }

        public long getWorkerExpireInterval() {
            return workerExpireInterval;
        }

        public void setWorkerExpireInterval(long workerExpireInterval) {
            this.workerExpireInterval = workerExpireInterval;
        }

        @Override
        public String toString() {
            return "DatabaseProperty{" +
                "workerHeartbeatInterval=" + workerHeartbeatInterval +
                ", workerExpireInterval=" + workerExpireInterval +
                '}';
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public SnowflakeType getType() {
        return type;
    }

    public void setType(SnowflakeType type) {
        this.type = type;
    }

    public String getDefaultGroup() {
        return defaultGroup;
    }

    public void setDefaultGroup(String defaultGroup) {
        this.defaultGroup = defaultGroup;
    }

    public long getDatacenterId() {
        return datacenterId;
    }

    public void setDatacenterId(long datacenterId) {
        this.datacenterId = datacenterId;
    }

    public WorkerAssignType getWorkerAssignerType() {
        return workerAssignerType;
    }

    public void setWorkerAssignerType(WorkerAssignType workerAssignerType) {
        this.workerAssignerType = workerAssignerType;
    }

    public ZookeeperProperty getZookeeper() {
        return zookeeper;
    }

    public void setZookeeper(
        ZookeeperProperty zookeeper) {
        this.zookeeper = zookeeper;
    }

    public DatabaseProperty getDb() {
        return db;
    }

    public void setDb(
        DatabaseProperty db) {
        this.db = db;
    }

    public WorkerId getWorkerId() {
        return workerId;
    }

    public void setWorkerId(
        WorkerId workerId) {
        this.workerId = workerId;
    }

    @Override
    public String toString() {
        return "EasySequenceSnowflakeProperties{" +
            "enabled=" + enabled +
            ", type=" + type +
            ", defaultGroup='" + defaultGroup + '\'' +
            ", datacenterId=" + datacenterId +
            ", workerId=" + workerId +
            ", workerAssignerType=" + workerAssignerType +
            ", zookeeper=" + zookeeper +
            ", db=" + db +
            '}';
    }
}
