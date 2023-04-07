package com.openquartz.sequence.core.uid.snowflake.cache;

import com.openquartz.sequence.core.uid.snowflake.exception.SnowflakeExceptionCode;
import com.openquartz.sequence.generator.common.bean.LifestyleBean;
import com.openquartz.sequence.generator.common.constant.Constants;
import com.openquartz.sequence.generator.common.exception.Asserts;
import com.openquartz.sequence.generator.common.utils.DateUtils;
import com.openquartz.sequence.generator.common.utils.StringUtils;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

/**
 * The unique id has 64bits (long), default allocated as blow:<br>
 * <li>sign: The highest bit is 0
 * <li>delta seconds: The next 28 bits, represents delta seconds since a customer epoch(2016-05-20 00:00:00.000).
 * Supports about 8.7 years until to 2024-11-20 21:24:16
 * <li>worker id: The next 22 bits, represents the worker's id which assigns based on database, max id is about 420W
 * <li>sequence: The next 13 bits, represents a sequence within the same second, max for 8192/s<br><br>
 *
 * The {@link DefaultUidGenerator#parseUid(long)} is a tool method to parse the bits
 *
 * <pre>{@code
 * +------+----------------------+----------------+-----------+
 * | sign |     delta seconds    | worker node id | sequence  |
 * +------+----------------------+----------------+-----------+
 *   1bit          40bits              10bits         13bits
 * }</pre>
 *
 * You can also specified the bits by Spring property setting.
 * <li>timeBits: default as 40
 * <li>workerBits: default as 10
 * <li>seqBits: default as 13
 * <li>epochStr: Epoch date string format 'yyyy-MM-dd'. Default as '2016-05-20'<p>
 *
 * <b>Note that:</b> The total bits must be 64 -1
 *
 * @author svnee
 */
@Slf4j
public abstract class DefaultUidGenerator implements LifestyleBean {

    /** Bits allocate */
    protected int timeBits = UidConstants.TIME_BITS;
    protected int workerBits = UidConstants.WORKER_BITS;
    protected int seqBits = UidConstants.SEQ_BITS;

    /** Customer epoch, unit as second. For example 2016-05-20 (ms: 1463673600000) */
    protected String epochStr = "2016-05-20";
    protected long epochSeconds = TimeUnit.MILLISECONDS.toSeconds(1463673600000L);

    /** Stable fields after spring bean initializing */
    protected BitsAllocator bitsAllocator;
    protected long workerId;

    @Override
    public void init() {
        // initialize bits allocator
        bitsAllocator = new BitsAllocator(timeBits, workerBits, seqBits);

        Asserts.isTrueIfLog(workerId <= bitsAllocator.getMaxWorkerId(),
            () -> log.error("[DefaultUidGenerator#afterPropertiesSet] Worker id: {} exceeds the max: {}", workerId,
                bitsAllocator.getMaxWorkerId()),
            SnowflakeExceptionCode.WORKER_ID_OVER_FLOW_ERROR);

        log.info("Initialized bits(1, {}, {}, {}) for workerID:{}", timeBits, workerBits, seqBits, workerId);
    }

    public String parseUid(long uid) {
        long totalBits = BitsAllocator.TOTAL_BITS;
        long signBits = bitsAllocator.getSignBits();
        long timestampBits = bitsAllocator.getTimestampBits();
        long workerIdBits = bitsAllocator.getWorkerIdBits();
        long sequenceBits = bitsAllocator.getSequenceBits();

        // parse UID
        long sequence = (uid << (totalBits - sequenceBits)) >>> (totalBits - sequenceBits);
        long workerId = (uid << (timestampBits + signBits)) >>> (totalBits - workerIdBits);
        long deltaSeconds = uid >>> (workerIdBits + sequenceBits);

        Date thatTime = new Date(TimeUnit.SECONDS.toMillis(epochSeconds + deltaSeconds));
        String thatTimeStr = DateUtils.format(thatTime, Constants.DATE_PATTERN_DEFAULT);

        // format as string
        return String.format("{\"UID\":\"%d\",\"timestamp\":\"%s\",\"workerId\":\"%d\",\"sequence\":\"%d\"}",
            uid, thatTimeStr, workerId, sequence);
    }

    /**
     * Setters for spring property
     */
    public void setWorkerId(long workerId) {
        this.workerId = workerId;
    }

    public void setTimeBits(int timeBits) {
        if (timeBits > 0) {
            this.timeBits = timeBits;
        }
    }

    public void setWorkerBits(int workerBits) {
        if (workerBits > 0) {
            this.workerBits = workerBits;
        }
    }

    public void setSeqBits(int seqBits) {
        if (seqBits > 0) {
            this.seqBits = seqBits;
        }
    }

    public void setEpochStr(String epochStr) {
        if (StringUtils.isNotBlank(epochStr)) {
            this.epochStr = epochStr;
            this.epochSeconds = TimeUnit.MILLISECONDS
                .toSeconds(DateUtils.parse(epochStr, DateUtils.DAY_PATTERN).getTime());
        }
    }

    public int getWorkerBits() {
        return workerBits;
    }
}
