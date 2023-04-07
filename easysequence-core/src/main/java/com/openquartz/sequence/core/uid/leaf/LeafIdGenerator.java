package com.openquartz.sequence.core.uid.leaf;

import com.openquartz.sequence.core.expr.exception.LeafIdExceptionCode;
import com.openquartz.sequence.core.uid.UidProvider;
import com.openquartz.sequence.core.uid.leaf.property.LeafProperty;
import com.openquartz.sequence.generator.common.bean.LifestyleBean;
import com.openquartz.sequence.generator.common.exception.Asserts;
import com.openquartz.sequence.generator.common.exception.EasySequenceException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Leaf Id Generator
 *
 * @author svnee
 */
public class LeafIdGenerator implements UidProvider, LifestyleBean {

    private static final Logger logger = LoggerFactory.getLogger(LeafIdGenerator.class);

    private final LeafProperty leafProperty;

    /**
     * 最大步长不超过100,0000
     */
    private static final int MAX_STEP = 1000000;
    /**
     * 一个Segment维持时间为15分钟
     */
    private static final long SEGMENT_DURATION = 15 * 60 * 1000L;

    private final ExecutorService service = new ThreadPoolExecutor(5, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS,
        new SynchronousQueue<>(), new UpdateThreadFactory());
    private volatile boolean initSuccess = false;
    private final Map<String, SegmentBuffer> cache = new ConcurrentHashMap<>();
    private final LeafIdAllocDAO dao;

    public LeafIdGenerator(LeafIdAllocDAO dao, LeafProperty leafProperty) {
        this.dao = dao;
        this.leafProperty = leafProperty;
    }

    public static class UpdateThreadFactory implements ThreadFactory {

        private static int threadInitNumber = 0;

        private static synchronized int nextThreadNum() {
            return threadInitNumber++;
        }

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "Thread-Segment-Update-" + nextThreadNum());
        }
    }

    @Override
    public void init() {
        logger.info("Init ........");
        // 确保加载到kv后才初始化成功
        updateCacheFromDb();
        initSuccess = true;
        updateCacheFromDbAtEveryMinute();
    }

    @Override
    public void destroy() {
        service.shutdown();
    }

    private void updateCacheFromDbAtEveryMinute() {
        Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r);
            t.setName("check-idCache-thread");
            t.setDaemon(true);
            return t;
        }).scheduleWithFixedDelay(this::updateCacheFromDb, 60, 60, TimeUnit.SECONDS);
    }

    private void updateCacheFromDb() {
        logger.info("update cache from db start!");
        long startTime = System.currentTimeMillis();
        try {
            List<String> dbTags = dao.getAllTags();
            if (dbTags == null || dbTags.isEmpty()) {
                return;
            }
            List<String> cacheTags = new ArrayList<>(cache.keySet());
            Set<String> insertTagsSet = new HashSet<>(dbTags);
            Set<String> removeTagsSet = new HashSet<>(cacheTags);
            //db中新加的tags灌进cache
            for (String tmp : cacheTags) {
                insertTagsSet.remove(tmp);
            }
            for (String tag : insertTagsSet) {
                SegmentBuffer buffer = new SegmentBuffer();
                buffer.setKey(tag);
                Segment segment = buffer.getCurrent();
                segment.setValue(new AtomicLong(0));
                segment.setMax(0);
                segment.setStep(0);
                cache.put(tag, buffer);
                logger.info("Add tag {} from db to IdCache, SegmentBuffer {}", tag, buffer);
            }
            //cache中已失效的tags从cache删除
            for (String tmp : dbTags) {
                removeTagsSet.remove(tmp);
            }
            for (String tag : removeTagsSet) {
                cache.remove(tag);
                logger.info("Remove tag {} from IdCache", tag);
            }
        } catch (Exception e) {
            logger.warn("update cache from db exception", e);
        } finally {
            logger.info("update cache from db end!cost:{}", System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public long nextId() {
        return nextId(leafProperty.getDefaultKey());
    }

    @Override
    public long nextId(final String key) {

        Asserts.isTrue(initSuccess, LeafIdExceptionCode.LEAF_ID_INIT_ERROR);
        Asserts.isTrue(cache.containsKey(key), LeafIdExceptionCode.LEAF_KEY_NOT_EXISTS_ERROR, key);

        SegmentBuffer buffer = cache.get(key);
        if (!buffer.isInitOk()) {
            synchronized (buffer) {
                if (!buffer.isInitOk()) {
                    try {
                        updateSegmentFromDb(key, buffer.getCurrent());
                        logger.info("Init buffer. Update leaf key {} {} from db", key, buffer.getCurrent());
                        buffer.setInitOk(true);
                    } catch (Exception e) {
                        logger.warn("Init buffer {} exception", buffer.getCurrent(), e);
                    }
                }
            }
        }
        return getIdFromSegmentBuffer(cache.get(key));
    }

    public void updateSegmentFromDb(String key, Segment segment) {
        SegmentBuffer buffer = segment.getBuffer();
        LeafAlloc leafAlloc;
        if (!buffer.isInitOk()) {
            leafAlloc = dao.updateMaxIdAndGetLeafAlloc(key);
            buffer.setStep(leafAlloc.getStep());
            //leafAlloc中的step为DB中的step
            buffer.setMinStep(leafAlloc.getStep());
        } else if (buffer.getUpdateTimestamp() == 0) {
            leafAlloc = dao.updateMaxIdAndGetLeafAlloc(key);
            buffer.setUpdateTimestamp(System.currentTimeMillis());
            buffer.setStep(leafAlloc.getStep());
            //leafAlloc中的step为DB中的step
            buffer.setMinStep(leafAlloc.getStep());
        } else {
            long duration = System.currentTimeMillis() - buffer.getUpdateTimestamp();
            int nextStep = buffer.getStep();
            if (duration < SEGMENT_DURATION && nextStep * 2 <= MAX_STEP) {
                nextStep = nextStep * 2;
            } else if (duration >= SEGMENT_DURATION * 2) {
                nextStep = nextStep / 2 >= buffer.getMinStep() ? nextStep / 2 : nextStep;
            }
            String format = String.format("%.2f", ((double) duration / (1000 * 60)));
            logger
                .info("leafKey[{}], step[{}], duration[{}mins], nextStep[{}]", key, buffer.getStep(), format, nextStep);
            LeafAlloc temp = new LeafAlloc();
            temp.setKey(key);
            temp.setStep(nextStep);
            leafAlloc = dao.updateMaxIdByCustomStepAndGetLeafAlloc(temp);
            buffer.setUpdateTimestamp(System.currentTimeMillis());
            buffer.setStep(nextStep);
            //leafAlloc的step为DB中的step
            buffer.setMinStep(leafAlloc.getStep());
        }
        // must set value before set max
        long value = leafAlloc.getMaxId() - buffer.getStep();
        segment.getValue().set(value);
        segment.setMax(leafAlloc.getMaxId());
        segment.setStep(buffer.getStep());
    }

    public long getIdFromSegmentBuffer(final SegmentBuffer buffer) {
        while (true) {
            buffer.rLock().lock();
            try {
                final Segment segment = buffer.getCurrent();
                if (!buffer.isNextReady() && (segment.getIdle() < 0.9 * segment.getStep()) && buffer.getThreadRunning()
                    .compareAndSet(false, true)) {
                    service.execute(() -> {
                        Segment next = buffer.getSegments()[buffer.nextPos()];
                        boolean updateOk = false;
                        try {
                            updateSegmentFromDb(buffer.getKey(), next);
                            updateOk = true;
                            logger.info("update segment {} from db {}", buffer.getKey(), next);
                        } catch (Exception e) {
                            logger.warn(buffer.getKey() + " updateSegmentFromDb exception", e);
                        } finally {
                            if (updateOk) {
                                buffer.wLock().lock();
                                buffer.setNextReady(true);
                                buffer.getThreadRunning().set(false);
                                buffer.wLock().unlock();
                            } else {
                                buffer.getThreadRunning().set(false);
                            }
                        }
                    });
                }
                long value = segment.getValue().getAndIncrement();
                if (value < segment.getMax()) {
                    return value;
                }
            } finally {
                buffer.rLock().unlock();
            }
            waitAndSleep(buffer);
            buffer.wLock().lock();
            try {
                final Segment segment = buffer.getCurrent();
                long value = segment.getValue().getAndIncrement();
                if (value < segment.getMax()) {
                    return value;
                }

                if (buffer.isNextReady()) {
                    buffer.switchPos();
                    buffer.setNextReady(false);
                } else {
                    logger.error("Both two segments in {} are not ready!", buffer);
                    throw new EasySequenceException(LeafIdExceptionCode.LEAF_TWO_SEGMENTS_ARE_NULL_ERROR);
                }
            } finally {
                buffer.wLock().unlock();
            }
        }
    }

    private void waitAndSleep(SegmentBuffer buffer) {
        int roll = 0;
        while (buffer.getThreadRunning().get()) {
            roll += 1;
            if (roll > 10000) {
                try {
                    TimeUnit.MILLISECONDS.sleep(10);
                    break;
                } catch (InterruptedException e) {
                    logger.warn("Thread {} Interrupted", Thread.currentThread().getName());
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
}
