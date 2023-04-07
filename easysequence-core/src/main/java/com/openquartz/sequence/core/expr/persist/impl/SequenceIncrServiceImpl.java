package com.openquartz.sequence.core.expr.persist.impl;

import com.openquartz.sequence.core.dictionary.CycleUnit;
import com.openquartz.sequence.core.expr.cmd.AssignExtParam;
import com.openquartz.sequence.core.expr.exception.SequenceGenerateExceptionCode;
import com.openquartz.sequence.core.expr.persist.SequenceIncrService;
import com.openquartz.sequence.core.expr.persist.model.FetchSequenceRequestBuilder;
import com.openquartz.sequence.core.expr.persist.model.FetchSequenceRequestBuilder.FetchSequenceRequest;
import com.openquartz.sequence.core.expr.persist.model.SequenceBucket;
import com.openquartz.sequence.core.expr.persist.model.SequencePool;
import com.openquartz.sequence.core.expr.persist.model.SequencePoolProperties;
import com.openquartz.sequence.core.persist.mapper.SequenceAssignRegisterMapper;
import com.openquartz.sequence.core.persist.mapper.SequenceNextAssignMapper;
import com.openquartz.sequence.core.persist.model.SequenceAssignRegister;
import com.openquartz.sequence.core.persist.model.SequenceNextAssign;
import com.openquartz.sequence.generator.common.concurrent.TraceThreadPoolExecutor;
import com.openquartz.sequence.generator.common.constant.Constants;
import com.openquartz.sequence.generator.common.exception.Asserts;
import com.openquartz.sequence.generator.common.exception.DataErrorCode;
import com.openquartz.sequence.generator.common.utils.DateUtils;
import com.openquartz.sequence.generator.common.utils.RandomUtils;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.DiscardPolicy;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 序列自增服务
 *
 * @author svnee
 **/
@Slf4j
public class SequenceIncrServiceImpl implements SequenceIncrService {

    private final SequenceNextAssignMapper sequenceNextAssignMapper;
    private final SequenceAssignRegisterMapper sequenceAssignRegisterMapper;
    private final SequencePoolProperties sequencePoolProperties;
    private final ThreadPoolExecutor executorService;
    private final Map<String, AtomicInteger> refreshStateMap = new ConcurrentHashMap<>();
    private final Map<String, SequencePool> sequencePoolMap = new ConcurrentHashMap<>();

    public SequenceIncrServiceImpl(SequenceNextAssignMapper sequenceNextAssignMapper,
        SequenceAssignRegisterMapper sequenceAssignRegisterMapper,
        SequencePoolProperties sequencePoolProperties) {

        this.sequenceNextAssignMapper = sequenceNextAssignMapper;
        this.sequenceAssignRegisterMapper = sequenceAssignRegisterMapper;
        this.sequencePoolProperties = sequencePoolProperties;
        this.executorService = new TraceThreadPoolExecutor(2, 5, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(100),
            new DiscardPolicy());
        executorService.allowCoreThreadTimeOut(true);
    }

    @Override
    public void destroy() {
        if (!executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    public Long getAndIncrement(String registerCode) {
        return getAndIncrementBy(registerCode, AssignExtParam.EMPTY_PARAM, 1);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    public Long getAndIncrement(String registerCode, AssignExtParam param) {
        return getAndIncrementBy(registerCode, param, 1);
    }

    private SequenceBucket doGetAndIncrementBy(String registerCode, AssignExtParam param, Long step) {
        SequenceAssignRegister rc = sequenceAssignRegisterMapper.selectByRegisterCode(registerCode);
        Asserts.notNull(rc, SequenceGenerateExceptionCode.SEQUENCE_REGISTER_CODE_NOT_EXIST_ERROR, registerCode);

        String key = generateKey(registerCode, param);

        SequenceNextAssign assigner = sequenceNextAssignMapper.selectByKey(key);
        if (assigner == null) {
            assigner = createInitializedRecord(rc, param);
        } else {
            processCycle(assigner);
        }
        boolean success = false;
        SequenceBucket sequenceBucket = null;
        // try get more count
        for (int i = 0; i < sequencePoolProperties.getMaxTryCount(); i++) {
            int affectRowNum = sequenceNextAssignMapper.incrementBy(key, step, assigner.getNextValue());
            if (affectRowNum >= 1) {
                success = true;
                long expireTimeout = getExpireTimeStartFromNow(assigner.getCycleUnit(), assigner.getCycle());
                sequenceBucket = new SequenceBucket();
                sequenceBucket.setStartSeq(assigner.getNextValue());
                sequenceBucket.setEndSeq(assigner.getNextValue() + step);
                sequenceBucket.setCurrentSeq(assigner.getNextValue());
                sequenceBucket.setExpireTime(expireTimeout);
                sequenceBucket.setName(assigner.getUniqueKey());
                break;
            }
            try {
                // sleep thread,reduce db tps
                Thread.sleep(RandomUtils.nextInt(20, 50));
            } catch (InterruptedException ignoredException) {
                Thread.currentThread().interrupt();
            }
            assigner = getNewestNumberAssign(key);
        }
        Asserts.isTrue(success, SequenceGenerateExceptionCode.SEQUENCE_INCR_ERROR);
        Asserts.notNull(sequenceBucket, SequenceGenerateExceptionCode.SEQUENCE_INCR_ERROR);
        return sequenceBucket;
    }

    /**
     * get sequence incr
     *
     * @param registerCode registerCode
     * @param param param
     * @param step step
     * @return value
     */
    private Long doGeneralGetIncr(String registerCode, AssignExtParam param, long step) {

        String registerKey = generateKey(registerCode, param);

        if (!sequencePoolProperties.isEnable()) {
            SequenceBucket sequenceBucket = doGetAndIncrementBy(registerCode, param, step);
            Asserts.notNull(sequenceBucket, SequenceGenerateExceptionCode.SEQUENCE_BUCKET_NOT_EXIST_ERROR, registerKey);
            return sequenceBucket.getCurrentSeq();
        }

        if (!sequencePoolMap.containsKey(registerKey)) {
            // init pool
            SequencePool sequencePool = new SequencePool(registerKey);
            sequencePool.setLowWaterLevelThreshold(sequencePoolProperties.getWaterLevelThreshold(registerCode));
            sequencePoolMap.putIfAbsent(registerKey, sequencePool);

            FetchSequenceRequest request = FetchSequenceRequestBuilder.builder(registerCode)
                .param(param)
                .step(step)
                .fetchCount(1)
                .wait(true)
                .build();
            fetchSequence(request);
        } else if (!isSequenceEnough(registerKey)) {
            FetchSequenceRequest request = FetchSequenceRequestBuilder.builder(registerCode)
                .param(param)
                .step(step)
                .fetchCount(1)
                .wait(true)
                .build();
            fetchSequence(request);
        } else if (isSequenceLowWater(registerKey)) {
            FetchSequenceRequest request = FetchSequenceRequestBuilder.builder(registerCode)
                .param(param)
                .step(step)
                .fetchCount(sequencePoolProperties.getWaterLevelThreshold(registerCode))
                .wait(false)
                .build();
            executorService.submit(() -> fetchSequence(request));
        }

        SequencePool sequencePool = sequencePoolMap.get(registerKey);
        return sequencePool.getAndIncrement();
    }

    /**
     * registerKey sequence pool is enough
     *
     * @param registerKey registerKey
     * @return sequence
     */
    private boolean isSequenceEnough(String registerKey) {
        SequencePool sequencePool = sequencePoolMap.get(registerKey);
        return sequencePool.clearAndCheckEnough();
    }

    /**
     * registerKey is in low Water
     *
     * @param registerKey registerKey
     * @return low water flag
     */
    private boolean isSequenceLowWater(String registerKey) {
        SequencePool sequencePool = sequencePoolMap.get(registerKey);
        return sequencePool.isLowWaterLevel();
    }

    /**
     * sync fetch sequence
     *
     * @param request request
     */
    private void fetchSequence(FetchSequenceRequest request) {
        AtomicInteger state;
        String key = generateKey(request.getSeqCode(), request.getParam());
        if (!refreshStateMap.containsKey(key)) {
            synchronized (this) {
                if (!refreshStateMap.containsKey(key)) {
                    refreshStateMap.put(key, new AtomicInteger(Constants.IDLE));
                }
                state = refreshStateMap.get(key);
            }
        } else {
            state = refreshStateMap.get(key);
        }

        if (state.compareAndSet(Constants.IDLE, Constants.RUNNING)) {
            try {
                SequencePool sequencePool = sequencePoolMap.get(key);
                if (!sequencePool.isLowWaterLevel()) {
                    return;
                }
                for (int i = 1; i <= request.getFetchCount(); i++) {
                    SequenceBucket sequenceBucket = doGetAndIncrementBy(request.getSeqCode(), request.getParam(),
                        Math.max(request.getStep(), sequencePoolProperties.getPreCount(request.getSeqCode())));

                    log.info(
                        "[SequenceIncrServiceImpl#fetchSequence] fetch,sequence-line:{},registerCode:{},startPoint:{},param:{}",
                        sequenceBucket, request.getSeqCode(), request.getStep(), request.getParam());
                    sequencePool.addSequenceBucket(sequenceBucket);
                }
            } finally {
                state.set(Constants.IDLE);
            }
        } else {
            if (request.isWait()) {
                long startWaitTime = System.currentTimeMillis();
                int waitingCount = 0;
                while (state.intValue() == Constants.RUNNING) {
                    try {
                        if (System.currentTimeMillis() - startWaitTime > sequencePoolProperties.getWaitFetchTimeout()) {
                            log.warn(
                                "[SequenceIncrServiceImpl#fetchSequence] has waited max time of {} milli second, breakout",
                                sequencePoolProperties.getWaitFetchTimeout());
                            break;
                        }
                        log.warn(
                            "[SequenceIncrServiceImpl#fetchSequence] thread:{} WAITING Times :{}",
                            Thread.currentThread().getId(), (++waitingCount));
                        Thread.yield();
                    } catch (Exception ignored) {
                    }
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    public Long getAndIncrementBy(String registerCode, AssignExtParam param, long step) {
        return doGeneralGetIncr(registerCode, param, step);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public SequenceNextAssign getNewestNumberAssign(String key) {
        return sequenceNextAssignMapper.selectByKey(key);
    }

    private String generateKey(String registerCode, AssignExtParam param) {
        return registerCode + param.toString();
    }

    private SequenceNextAssign createInitializedRecord(SequenceAssignRegister rc, AssignExtParam param) {
        SequenceNextAssign assigner = new SequenceNextAssign();
        assigner.setUniqueKey(generateKey(rc.getRegisterCode(), param));
        assigner.setNextValue(rc.getInitValue());
        assigner.setInitValue(rc.getInitValue());
        assigner.setCycle(rc.getCycle());
        assigner.setCycleUnit(rc.getCycleUnit());
        assigner.setLastAssignTime(LocalDate.now());
        sequenceNextAssignMapper.insert(assigner);
        return assigner;
    }

    private void processCycle(SequenceNextAssign assign) {
        if (assign.getCycle() <= 0) {
            return;
        }
        if (hasExpire(assign)) {
            int count = resetNextValue(assign.getUniqueKey(), assign.getNextValue());
            SequenceNextAssign newAssign = sequenceNextAssignMapper.selectByKey(assign.getUniqueKey());
            if (count != 1) {
                // 重试一次，不成功则报错
                if (hasExpire(newAssign)) {
                    count = resetNextValue(newAssign.getUniqueKey(), newAssign.getNextValue());
                    Asserts.isTrue(count == 1, DataErrorCode.UPDATE_ERROR, 1, count);
                } else {
                    assign.setNextValue(newAssign.getNextValue());
                }
            } else {
                assign.setNextValue(newAssign.getNextValue());
            }
        }
    }

    private int resetNextValue(String registerCode, Long preValue) {
        return sequenceNextAssignMapper.resetNextValue(registerCode, preValue);
    }

    private boolean hasExpire(SequenceNextAssign assign) {
        LocalDate lastAssignTime = assign.getLastAssignTime();
        if (lastAssignTime == null) {
            return true;
        }
        LocalDate lastAssignDate = LocalDate.of(lastAssignTime.getYear(),
            lastAssignTime.getMonth(), lastAssignTime.getDayOfMonth());
        LocalDate now = LocalDate.now();
        return hasExpire(lastAssignDate, now, assign.getCycleUnit(), assign.getCycle());
    }

    private boolean hasExpire(LocalDate lastAssignDate, LocalDate now,
        CycleUnit cycleUnit, Integer cycle) {
        long difValue = 0L;
        if (CycleUnit.YEAR.equals(cycleUnit)) {
            difValue = DateUtils.floorAndDiffYear(lastAssignDate, now);
        } else if (CycleUnit.MONTH.equals(cycleUnit)) {
            difValue = DateUtils.floorAndDiffMonth(lastAssignDate, now);
        } else if (CycleUnit.WEEK.equals(cycleUnit)) {
            difValue = DateUtils.floorAndDiffWeek(lastAssignDate, now);
        } else if (CycleUnit.DAY.equals(cycleUnit)) {
            difValue = DateUtils.diffDay(lastAssignDate, now);
        }
        return difValue >= cycle;
    }

    private static long getExpireTimeStartFromNow(CycleUnit cycleUnit, Integer cycle) {
        if (cycle <= 0) {
            return 0;
        }
        LocalDate now = LocalDate.now();
        long expireTime = 0;
        if (CycleUnit.YEAR.equals(cycleUnit)) {
            expireTime = DateUtils.floorYear(now).plusYears(cycle).atStartOfDay(ZoneOffset.ofHours(8)).toInstant()
                .toEpochMilli();
        } else if (CycleUnit.MONTH.equals(cycleUnit)) {
            expireTime = DateUtils.floorMonth(now).plusMonths(cycle).atStartOfDay(ZoneOffset.ofHours(8)).toInstant()
                .toEpochMilli();
        } else if (CycleUnit.WEEK.equals(cycleUnit)) {
            expireTime = DateUtils.floorWeek(now).plusWeeks(cycle).atStartOfDay(ZoneOffset.ofHours(8)).toInstant()
                .toEpochMilli();
        } else if (CycleUnit.DAY.equals(cycleUnit)) {
            expireTime = now.plusDays(cycle).atStartOfDay(ZoneOffset.ofHours(8)).toInstant().toEpochMilli();
        }
        return expireTime;
    }

}
