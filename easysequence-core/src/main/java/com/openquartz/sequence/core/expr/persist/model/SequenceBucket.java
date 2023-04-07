package com.openquartz.sequence.core.expr.persist.model;

import com.openquartz.sequence.core.expr.exception.SequenceGenerateExceptionCode;
import com.openquartz.sequence.generator.common.exception.EasySequenceException;

/**
 * SequenceBucket
 * bucket: storage sequence from (startSeq,endSeq).
 *
 * @author svnee
 */
public class SequenceBucket {

    /**
     * bucket name
     */
    private String name;

    /**
     * 开始
     */
    private long startSeq;

    /**
     * 结束
     */
    private long endSeq;

    /**
     * 当前序号
     */
    private long currentSeq;

    /**
     * 过期日期
     */
    private Long expireTime;

    public synchronized long getAndIncrement() {
        if (currentSeq >= endSeq) {
            throw EasySequenceException
                .replacePlaceHold(SequenceGenerateExceptionCode.SEQUENCE_BUCKET_USE_UP_ERROR, name,
                    startSeq, endSeq);
        }
        long returnValue = currentSeq;
        currentSeq++;
        return returnValue;
    }

    public boolean isExpired() {
        if (expireTime == null || expireTime <= 0) {
            return false;
        }
        return expireTime < System.currentTimeMillis();
    }

    public boolean isUseUp() {
        return currentSeq >= endSeq;
    }

    /**
     * 可用的 Bucket
     * 存在序列号段且未过期
     */
    public boolean isAvailable() {
        return !isExpired() && !isUseUp();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getStartSeq() {
        return startSeq;
    }

    public void setStartSeq(long startSeq) {
        this.startSeq = startSeq;
    }

    public long getEndSeq() {
        return endSeq;
    }

    public void setEndSeq(long endSeq) {
        this.endSeq = endSeq;
    }

    public long getCurrentSeq() {
        return currentSeq;
    }

    public void setCurrentSeq(long currentSeq) {
        this.currentSeq = currentSeq;
    }

    public Long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Long expireTime) {
        this.expireTime = expireTime;
    }

    @Override
    public String toString() {
        return "SequenceBucket{" +
            "name='" + name + '\'' +
            ", startSeq=" + startSeq +
            ", endSeq=" + endSeq +
            ", currentSeq=" + currentSeq +
            ", expireTime=" + expireTime +
            '}';
    }
}
