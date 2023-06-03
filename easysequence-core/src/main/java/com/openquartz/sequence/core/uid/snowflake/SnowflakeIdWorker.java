package com.openquartz.sequence.core.uid.snowflake;

import static com.openquartz.sequence.core.uid.snowflake.exception.SnowflakeExceptionCode.CLOCK_CALLBACK_ERROR;

import com.openquartz.sequence.core.uid.snowflake.exception.SnowflakeExceptionCode;
import com.openquartz.sequence.generator.common.constant.Constants;
import com.openquartz.sequence.generator.common.exception.EasySequenceException;
import com.openquartz.sequence.generator.common.time.SystemClock;
import com.openquartz.sequence.generator.common.utils.ExceptionUtils;
import java.text.SimpleDateFormat;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;

/**
 * twitter Snowflake 算法，提供uid生成器
 *
 * @author svnee
 */
@Slf4j
public class SnowflakeIdWorker {

    // ============================== Constants===========================================

    private static final String MSG_UID_PARSE = "{\"UID\":\"%s\",\"timestamp\":\"%s\",\"workerId\":\"%d\",\"dataCenterId\":\"%d\",\"sequence\":\"%d\"}";

    // ==============================Fields===========================================
    /** 开始时间截 (2017-12-25)，用于用当前时间戳减去这个时间戳，算出偏移量 */
    private final long twepoch = 1514131200000L;

    /** 机器id所占的位数(表示只允许workId的范围为：0-1023) */
    private final long workerIdBits = 5;

    /** 数据标识id所占的位数 */
    private final long datacenterIdBits = 5;

    /** 支持的最大机器id，结果是31 (这个移位算法可以很快的计算出几位二进制数所能表示的最大十进制数) */
    public final long maxWorkerId = ~(-1L << workerIdBits);

    /** 序列在id中占的位数 (表示只允许sequenceId的范围为：0-4095) */
    private final long sequenceBits = 12;

    /** 工作机器ID(0~31) */
    private final long workerId;

    /** 数据中心ID(0~31) */
    private final long datacenterId;

    /** 毫秒内序列(0~4095) */
    private long sequence = 0L;

    /** 上次生成ID的时间截 */
    private long lastTimestamp = -1L;

    private boolean isClock = false;

    public void setClock(boolean clock) {
        isClock = clock;
    }

    // ==============================Constructors=====================================

    /**
     * 构造函数
     *
     * @param workerId 工作ID (0~31)
     * @param datacenterId 数据中心ID (0~31)
     */
    public SnowflakeIdWorker(long workerId, long datacenterId) {
        log.info("SnowflakeIdWorker,workerId:{},datacenterId:{}", workerId, datacenterId);
        if (workerId > maxWorkerId || workerId < 0) {
            throw new EasySequenceException(SnowflakeExceptionCode.WORKER_ID_OVER_FLOW_ERROR);
        }
        // 支持的最大数据标识id，结果是31
        long maxDatacenterId = ~(-1L << datacenterIdBits);
        if (datacenterId > maxDatacenterId || datacenterId < 0) {
            throw new EasySequenceException(SnowflakeExceptionCode.DATA_CENTER_ID_OVER_FLOW_ERROR);
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    // ==============================Methods==========================================

    /**
     * 获得下一个ID (该方法是线程安全的)
     *
     * @return SnowflakeId
     */
    public synchronized long nextId() {
        long timestamp = timeGen();
        // 闰秒：如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过这个时候应当抛出异常
        if (timestamp < lastTimestamp) {
            long offset = lastTimestamp - timestamp;
            if (offset <= 5) {
                try {
                    // 时间偏差大小小于5ms，则等待两倍时间
                    wait(offset << 1);
                    timestamp = timeGen();
                    if (timestamp < lastTimestamp) {
                        // 还是小于，抛异常并上报
                        throw EasySequenceException.replacePlaceHold(CLOCK_CALLBACK_ERROR, lastTimestamp - timestamp);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    ExceptionUtils.rethrow(e);
                }
            } else {
                throw EasySequenceException.replacePlaceHold(CLOCK_CALLBACK_ERROR, lastTimestamp - timestamp);
            }
        }

        // 解决跨毫秒生成ID序列号始终为偶数的缺陷:如果是同一时间生成的，则进行毫秒内序列
        if (lastTimestamp == timestamp) {
            // 通过位与运算保证计算的结果范围始终是 0-4095
            // 生成序列的掩码，(防止溢出:位与运算保证计算的结果范围始终是 0-4095，0b111111111111=0xfff=4095)
            long sequenceMask = ~(-1L << sequenceBits);
            sequence = (sequence + 1) & sequenceMask;
            // 毫秒内序列溢出
            if (sequence == 0) {
                // 阻塞到下一个毫秒,获得新的时间戳
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            // 时间戳改变，毫秒内序列重置
            sequence = 0L;
        }

        // 上次生成ID的时间截
        lastTimestamp = timestamp;

        /*
         * 1.左移运算是为了将数值移动到对应的段(41、5、5，12那段因为本来就在最右，因此不用左移)
         * 2.然后对每个左移后的值(la、lb、lc、sequence)做位或运算，是为了把各个短的数据合并起来，合并成一个二进制数
         * 3.最后转换成10进制，就是最终生成的id(64位的ID)
         */
        // 机器ID向左移12位
        // 数据标识id向左移17位(12+5)
        long datacenterIdShift = sequenceBits + workerIdBits;
        // 时间截向左移22位(5+5+12)
        long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;
        return ((timestamp - twepoch) << timestampLeftShift) | (datacenterId << datacenterIdShift) | (workerId
            << sequenceBits) | sequence;
    }

    public String parseUID(Long uid) {
        // 总位数
        long totalBits = 64L;
        // 标识
        long signBits = 1L;
        // 时间戳
        long timestampBits = 41L;
        // 解析Uid：标识 -- 时间戳 -- 数据中心 -- 机器码 --序列
        long sequence = (uid << (totalBits - sequenceBits)) >>> (totalBits - sequenceBits);
        long dataCenterId = (uid << (timestampBits + signBits)) >>> (totalBits - datacenterIdBits);
        long workerId = (uid << (timestampBits + signBits + datacenterIdBits)) >>> (totalBits - workerIdBits);
        long deltaSeconds = uid >>> (datacenterIdBits + workerIdBits + sequenceBits);
        // 时间处理(补上开始时间戳)
        Date thatTime = new Date(twepoch + deltaSeconds);
        String date = new SimpleDateFormat(Constants.DATE_PATTERN_DEFAULT).format(thatTime);
        // 格式化输出
        return String.format(MSG_UID_PARSE, uid, date, workerId, dataCenterId, sequence);
    }

    /**
     * 保证返回的毫秒数在参数之后(阻塞到下一个毫秒，直到获得新的时间戳)
     *
     * @param lastTimestamp 上次生成ID的时间截
     * @return 当前时间戳
     */
    protected long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    /**
     * 获得系统当前毫秒数
     *
     * @return 当前时间(毫秒)
     */
    protected long timeGen() {
        if (isClock) {
            // 解决高并发下获取时间戳的性能问题
            return SystemClock.now();
        } else {
            return System.currentTimeMillis();
        }
    }
}