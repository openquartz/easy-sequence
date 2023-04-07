package com.openquartz.sequence.core.uid.snowflake.worker;

import java.sql.Timestamp;
import lombok.ToString;

/**
 * Entity for M_WORKER_NODE
 *
 * @author svnee
 */
@ToString
public class WorkerNode {

    /**
     * Entity unique id (table unique)
     */
    private Long id;

    /**
     * group
     */
    private String group;

    /**
     * workerId
     */
    private Integer workerId;

    /**
     * 本次启动唯一id
     */
    private String uid;

    /**
     * 进程ID
     */
    private String processId;

    /**
     * ip address
     */
    private String ip;

    /**
     * 下次失效时间
     */
    private Timestamp lastExpireTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public Integer getWorkerId() {
        return workerId;
    }

    public void setWorkerId(Integer workerId) {
        this.workerId = workerId;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Timestamp getLastExpireTime() {
        return lastExpireTime;
    }

    public void setLastExpireTime(Timestamp lastExpireTime) {
        this.lastExpireTime = lastExpireTime;
    }
}
