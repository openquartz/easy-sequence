package com.openquartz.sequence.starter.worker;

/**
 * DatabaseWorkerAssignerProperty
 *
 * @author svnee
 **/
public class WorkerIdAssignerProperty {

    /**
     * group
     */
    private String group;

    /**
     * 最小workerId
     */
    private long minWorkerId = 0;

    /**
     * 最大workerId
     */
    private long maxWorkerId = 32;

    /**
     * 时间间隔
     * 单位：毫秒
     */
    private long workerHeartbeatInterval = 5000;

    /**
     * workerId过期时间间隔
     */
    private long workerExpireInterval = 500000;

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

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

    public long getMinWorkerId() {
        return minWorkerId;
    }

    public void setMinWorkerId(long minWorkerId) {
        this.minWorkerId = minWorkerId;
    }

    public long getMaxWorkerId() {
        return maxWorkerId;
    }

    public void setMaxWorkerId(long maxWorkerId) {
        this.maxWorkerId = maxWorkerId;
    }
}
