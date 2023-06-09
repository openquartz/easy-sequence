package com.openquartz.sequence.core.uid.snowflake.cache;

import com.openquartz.sequence.core.uid.snowflake.cache.buffer.BufferPaddingExecutor;
import com.openquartz.sequence.core.uid.snowflake.cache.buffer.RejectedPutBufferHandler;
import com.openquartz.sequence.core.uid.snowflake.cache.buffer.RejectedTakeBufferHandler;
import com.openquartz.sequence.core.uid.snowflake.cache.buffer.RingBuffer;
import com.openquartz.sequence.core.uid.snowflake.exception.SnowflakeExceptionCode;
import com.openquartz.sequence.generator.common.bean.LifestyleBean;
import com.openquartz.sequence.generator.common.exception.EasySequenceException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

/**
 * from {@link DefaultUidGenerator}, based on a lock free {@link com.openquartz.sequence.core.uid.snowflake.cache.buffer.RingBuffer}<p>
 *
 * The spring properties you can specified as below:<br>
 * <li><b>boostPower:</b> RingBuffer size boost for a power of 2, Sample: boostPower is 3, it means the buffer size
 * will be <code>({@link BitsAllocator#getMaxSequence()} + 1) &lt;&lt;
 * {@link #boostPower}</code>, Default as {@value #DEFAULT_BOOST_POWER}
 * <li><b>paddingFactor:</b> Represents a percent value of (0 - 100). When the count of rest available UIDs reach the
 * threshold, it will trigger padding buffer. Default as{@link com.openquartz.sequence.core.uid.snowflake.cache.buffer.RingBuffer#DEFAULT_PADDING_PERCENT}
 * Sample: paddingFactor=20, bufferSize=1000 -> threshold=1000 * 20 /100, padding buffer will be triggered when tail-cursor<threshold
 * <li><b>scheduleInterval:</b> Padding buffer in a schedule, specify padding buffer interval, Unit as second
 * <li><b>rejectedPutBufferHandler:</b> Policy for rejected put buffer. Default as discard put request, just do logging
 * <li><b>rejectedTakeBufferHandler:</b> Policy for rejected take buffer. Default as throwing up an exception
 *
 * @author svnee
 */
@Slf4j
public class CachedUidGenerator extends DefaultUidGenerator implements LifestyleBean {

    private static final int DEFAULT_BOOST_POWER = 3;

    /** Spring properties */
    private int boostPower = DEFAULT_BOOST_POWER;
    private static final int PADDING_FACTOR = RingBuffer.DEFAULT_PADDING_PERCENT;
    private Long scheduleInterval;

    private RejectedPutBufferHandler rejectedPutBufferHandler;
    private RejectedTakeBufferHandler rejectedTakeBufferHandler;

    /** RingBuffer */
    private RingBuffer ringBuffer;
    private BufferPaddingExecutor bufferPaddingExecutor;

    public CachedUidGenerator(long workerId) {

        super.setWorkerId(workerId);

        // init
        init();
    }

    @Override
    public void init() {

        // initialize workerId & bitsAllocator
        super.init();

        // initialize RingBuffer & RingBufferPaddingExecutor
        this.initRingBuffer();

        log.info("[CachedUidGenerator#init] Initialized RingBuffer successfully.");
    }

    public long nextId() {
        try {
            long uid = ringBuffer.take();
            if (log.isDebugEnabled()) {
                String parsedInfo = this.parseUid(uid);
                log.debug("getUID! parsedInfo:{}", parsedInfo);
            }
            return uid;
        } catch (Exception e) {
            log.error("Generate unique id exception. ", e);
            throw new EasySequenceException(SnowflakeExceptionCode.SNOWFLAKE_ID_GENERATE_ERROR);
        }
    }

    public long getWorkerId() {
        return workerId;
    }

    @Override
    public String parseUid(long uid) {
        return super.parseUid(uid);
    }

    @Override
    public void destroy() {

        super.destroy();

        bufferPaddingExecutor.shutdown();
    }

    /**
     * Get the UIDs in the same specified second under the max sequence
     *
     * @return UID list, size of {@link BitsAllocator#getMaxSequence()} + 1
     */
    protected List<Long> nextIdsForOneSecond(long currentSecond) {
        // Initialize result list size of (max sequence + 1)
        int listSize = (int) bitsAllocator.getMaxSequence() + 1;
        List<Long> uidList = new ArrayList<>(listSize);

        // Allocate the first sequence of the second, the others can be calculated with the offset
        long firstSeqUid = bitsAllocator.allocate(currentSecond - epochSeconds, workerId, 0L);
        for (int offset = 0; offset < listSize; offset++) {
            uidList.add(firstSeqUid + offset);
        }

        return uidList;
    }

    /**
     * Initialize RingBuffer & RingBufferPaddingExecutor
     */
    private void initRingBuffer() {
        // initialize RingBuffer
        int bufferSize = ((int) bitsAllocator.getMaxSequence() + 1) << boostPower;
        this.ringBuffer = new RingBuffer(bufferSize, PADDING_FACTOR);
        log.info("Initialized ring buffer size:{}, paddingFactor:{}", bufferSize, PADDING_FACTOR);

        // initialize RingBufferPaddingExecutor
        boolean usingSchedule = (scheduleInterval != null);
        this.bufferPaddingExecutor = new BufferPaddingExecutor(ringBuffer, this::nextIdsForOneSecond, usingSchedule);
        if (usingSchedule) {
            bufferPaddingExecutor.setScheduleInterval(scheduleInterval);
        }

        log
            .info("Initialized BufferPaddingExecutor. Using schdule:{}, interval:{}", usingSchedule, scheduleInterval);

        // set rejected put/take handle policy
        this.ringBuffer.setBufferPaddingExecutor(bufferPaddingExecutor);
        if (rejectedPutBufferHandler != null) {
            this.ringBuffer.setRejectedPutHandler(rejectedPutBufferHandler);
        }
        if (rejectedTakeBufferHandler != null) {
            this.ringBuffer.setRejectedTakeHandler(rejectedTakeBufferHandler);
        }

        // fill in all slots of the RingBuffer
        bufferPaddingExecutor.paddingBuffer();

        // start buffer padding threads
        bufferPaddingExecutor.start();
    }

    /**
     * Setters for spring property
     */
    public void setBoostPower(int boostPower) {
        Assert.isTrue(boostPower > 0, "Boost power must be positive!");
        this.boostPower = boostPower;
    }

    public void setRejectedPutBufferHandler(RejectedPutBufferHandler rejectedPutBufferHandler) {
        Assert.notNull(rejectedPutBufferHandler, "RejectedPutBufferHandler can't be null!");
        this.rejectedPutBufferHandler = rejectedPutBufferHandler;
    }

    public void setRejectedTakeBufferHandler(RejectedTakeBufferHandler rejectedTakeBufferHandler) {
        Assert.notNull(rejectedTakeBufferHandler, "RejectedTakeBufferHandler can't be null!");
        this.rejectedTakeBufferHandler = rejectedTakeBufferHandler;
    }

    public void setScheduleInterval(long scheduleInterval) {
        Assert.isTrue(scheduleInterval > 0, "Schedule interval must positive!");
        this.scheduleInterval = scheduleInterval;
    }

}
