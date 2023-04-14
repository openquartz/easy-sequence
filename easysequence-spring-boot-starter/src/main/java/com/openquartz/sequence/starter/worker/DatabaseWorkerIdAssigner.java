package com.openquartz.sequence.starter.worker;

import static com.openquartz.sequence.core.uid.snowflake.exception.SnowflakeExceptionCode.GROUP_NOT_EXIST_ERROR;

import com.openquartz.sequence.core.uid.snowflake.exception.SnowflakeExceptionCode;
import com.openquartz.sequence.core.uid.snowflake.worker.WorkerIdAssigner;
import com.openquartz.sequence.core.uid.snowflake.worker.WorkerNode;
import com.openquartz.sequence.generator.common.exception.Asserts;
import com.openquartz.sequence.generator.common.exception.EasySequenceException;
import com.openquartz.sequence.generator.common.transaction.TransactionSupport;
import com.openquartz.sequence.generator.common.utils.NetUtils;
import com.openquartz.sequence.generator.common.utils.Pair;
import com.openquartz.sequence.generator.common.utils.RandomUtils;
import com.openquartz.sequence.generator.common.utils.StringUtils;
import com.openquartz.sequence.starter.persist.WorkerNodeDAO;
import java.lang.management.ManagementFactory;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;

/**
 * DB编号分配器(利用数据库来管理)
 *
 * @author svnee
 */
@Slf4j
public class DatabaseWorkerIdAssigner implements WorkerIdAssigner {

    /**
     * worker node
     */
    private final WorkerNodeDAO workerNodeDAO;

    /**
     * transaction support
     */
    private final TransactionSupport transactionSupport;

    /**
     * 本次启动的唯一key
     */
    private String uidKey;

    /**
     * current instance processId
     */
    private String processId;

    /**
     * current instance ip:port
     */
    private String ip;

    /**
     * worker_x 节点信息
     */
    private ScheduledThreadPoolExecutor scheduler;

    /**
     * current instance work node
     */
    private WorkerNode workerNode;

    /**
     * database property
     */
    private final WorkerIdAssignerProperty property;

    public DatabaseWorkerIdAssigner(WorkerIdAssignerProperty property,
        WorkerNodeDAO workerNodeDAO,
        TransactionSupport transactionSupport,
        String port) {
        this.workerNodeDAO = workerNodeDAO;
        this.property = property;
        this.transactionSupport = transactionSupport;
        init(port);
    }

    private void init(String port) {

        checkGroup(property.getGroup());

        // 配置基本信息
        initBaseInfo(port);

        // 分配worker
        allocateWorker();

        // 初始化心跳上报
        initHeartBeatReport();

        // 添加进程退出钩子
        addShutdownHook();
    }

    /**
     * 初始化数据的心跳上报
     */
    private void initHeartBeatReport() {
        scheduler = new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
            private final AtomicInteger threadNum = new AtomicInteger(0);

            @Override
            @SuppressWarnings("all")
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "DBWorkerAssign-Heart-Thread-" + threadNum.getAndIncrement());
                thread.setDaemon(true);
                return thread;
            }
        });

        // 延迟10秒上报，每5秒上报一次数据
        scheduler.scheduleWithFixedDelay(this::refreshNodeInfo, RandomUtils.nextInt(1,1000) * 100L, property.getWorkerHeartbeatInterval(),
            TimeUnit.MILLISECONDS);
    }

    /**
     * 将时间向未来延长固定的小时
     */
    private long afterHour() {
        return System.currentTimeMillis() + property.getWorkerExpireInterval();
    }

    /**
     * 刷新节点信息
     * <p>
     * 主要为更新下次失效时间
     */
    private void refreshNodeInfo() {
        long lastExpireTime = afterHour();
        Timestamp lastExpireTimestamp = new Timestamp(lastExpireTime);
        int affect = workerNodeDAO.updateLastExpireTime(workerNode.getId(), lastExpireTimestamp);
        if (affect > 0) {
            workerNode.setLastExpireTime(lastExpireTimestamp);
        }
    }

    /**
     * 进程关闭时候清理业务数据
     */
    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("[DatabaseWorkerIdAssigner#addShutdownHook] process ready to quit, clear resources of db");
            workerNodeDAO.delete(workerNode.getId());
            if (null != scheduler) {
                scheduler.shutdown();
            }
        }));
    }

    private void checkGroup(String group) {
        if (StringUtils.isBlank(group)) {
            throw new EasySequenceException(GROUP_NOT_EXIST_ERROR);
        }
    }

    private void initBaseInfo(String port) {
        uidKey = UUID.randomUUID().toString();
        processId = getProcessIdStr();
        ip = NetUtils.getLocalInetAddress().getHostAddress() + ":" + port;
    }

    private void allocateWorker() {

        // apply exist worker
        if (!applyWorkerFromExistExpire()) {

            // save worker
            insertWorker();
        }

        log.info(">>>>>>>>>>>[DatabaseWorkerIdAssigner#allocate] instance ip: {}, group: {}, current-workerId: {}",
            workerNode.getIp(), workerNode.getGroup(), workerNode.getWorkerId());
    }

    /**
     * 新增一个worker
     * <p>
     * 如果数据达到最大，则阻止进程启动
     */
    private void insertWorker() {
        Integer currentWorkerId = workerNodeDAO.getMaxWorkerId(property.getGroup());
        for (long i = property.getMinWorkerId(); i < property.getMaxWorkerId(); i++) {
            try {
                doInsertWorkerId(currentWorkerId);
                return;
            } catch (EasySequenceException ex) {
                throw ex;
            } catch (Exception ex) {
                log.warn("[DatabaseWorkerIdAssigner#insertWorkerId] group:{} allocate occur error!errorMsg:{}",
                    property.getGroup(), ex.getMessage());
                if (currentWorkerId == null) {
                    currentWorkerId = workerNodeDAO.getMaxWorkerId(property.getGroup());
                } else {
                    currentWorkerId++;
                }
            }
        }
        Asserts.notNull(workerNode, SnowflakeExceptionCode.GROUP_WORKER_ASSIGN_ERROR);
    }

    /**
     * do insert workerId
     */
    private void doInsertWorkerId(Integer maxWorkerId) {
        if (maxWorkerId == null) {
            workerNode = workerNodeDAO.addWorkerNode(generateWorkerNode((int) property.getMinWorkerId()));
        } else {
            // 做 loop 重置
            if (maxWorkerId + 1 < property.getMaxWorkerId()) {
                workerNode = workerNodeDAO.addWorkerNode(generateWorkerNode(maxWorkerId + 1));
            } else {
                // 当前loop 重置后得到的workerId
                long workerId = maxWorkerId + 1 - property.getMaxWorkerId() + property.getMinWorkerId();
                workerNode = workerNodeDAO.addWorkerNode(generateWorkerNode((int) workerId));
            }
        }
    }

    /**
     * generate new worker node
     *
     * @param workerId workerId
     * @return worker node
     */
    private WorkerNode generateWorkerNode(Integer workerId) {
        WorkerNode uuidGeneratorDO = new WorkerNode();
        uuidGeneratorDO.setWorkerId(workerId);
        uuidGeneratorDO.setGroup(property.getGroup());
        uuidGeneratorDO.setLastExpireTime(new Timestamp(afterHour()));
        uuidGeneratorDO.setUid(uidKey);
        uuidGeneratorDO.setProcessId(processId);
        uuidGeneratorDO.setIp(ip);
        return uuidGeneratorDO;
    }

    /**
     * 从已存在的worker里面查看过期的，有过期的则获取并更新数据
     *
     * @return true：分配成功，false：分配失败
     */
    private boolean applyWorkerFromExistExpire() {

        // do apply extend pre node
        if (doApplyWorkerExtendPre()) {
            return true;
        }

        Long minId = workerNodeDAO.selectMinId(property.getGroup());
        if (null == minId) {
            return false;
        }

        // tx
        Pair<Boolean, WorkerNode> pair = transactionSupport
            .execute(() -> doApplyWorkerFromExistExpire(minId, property.getGroup(), uidKey, processId, ip,
                property.getWorkerExpireInterval()));
        if (pair.getKey()) {
            workerNode = pair.getValue();
            return true;
        }
        return false;
    }

    /**
     * do apply worker
     *
     * @param selfId selfId
     * @param group group
     * @param expireInterval worker expire interval
     * @return key: exist flag, value: work node
     */
    public Pair<Boolean, WorkerNode> doApplyWorkerFromExistSelf(Long selfId, String group, long expireInterval) {

        WorkerNode existedWorkerNode = workerNodeDAO.selectForUpdate(selfId);
        if (existedWorkerNode == null) {
            return Pair.of(false, null);
        }

        // work node expire or work node belong self
        boolean canApply = existedWorkerNode.getLastExpireTime().compareTo(new Date()) < 0
            || (Objects.equals(group, existedWorkerNode.getGroup()) && Objects.equals(ip, existedWorkerNode.getIp()));
        if (canApply) {

            // refresh work node
            long workerId = existedWorkerNode.getWorkerId();
            // do loop allocate workerId for workerNode
            Pair<Boolean, WorkerNode> allocateResult = doLoopAllocateWorkerNode(group, expireInterval,
                existedWorkerNode, workerId);
            if (allocateResult != null) {
                return allocateResult;
            }
        }
        return new Pair<>(false, null);
    }

    private Pair<Boolean, WorkerNode> doLoopAllocateWorkerNode(String group,
        long expireInterval, WorkerNode existedWorkerNode, long workerId) {
        // 超出范围
        if (workerId < property.getMinWorkerId() || workerId >= property.getMaxWorkerId()) {
            workerId = property.getMinWorkerId();
        }

        for (long i = property.getMinWorkerId(); i < property.getMaxWorkerId(); i++) {
            try {
                if (workerId >= property.getMaxWorkerId()) {
                    long actualWorkerId = workerId - property.getMaxWorkerId() + property.getMinWorkerId();
                    existedWorkerNode.setWorkerId((int) actualWorkerId);
                } else {
                    existedWorkerNode.setWorkerId((int) workerId);
                }
                WorkerNode node = workerNodeDAO
                    .refreshWorkNode(group, uidKey, processId, ip, expireInterval, existedWorkerNode);
                return Pair.of(true, node);
            } catch (Exception ex) {
                log.warn("[DatabaseWorkerIdAssigner#doApplyWorkerFromExistSelf] refresh-error!", ex);
                workerId++;
            }
        }
        return null;
    }

    private boolean doApplyWorkerExtendPre() {
        WorkerNode selfNode = workerNodeDAO.selectSelfWorkNode(property.getGroup(), ip);
        if (Objects.isNull(selfNode)) {
            return false;
        }
        // tx
        Pair<Boolean, WorkerNode> nodePair = transactionSupport.execute(() ->
            doApplyWorkerFromExistSelf(selfNode.getId(), property.getGroup(), property.getWorkerExpireInterval()));
        if (nodePair.getKey()) {
            workerNode = nodePair.getValue();
            return true;
        }
        return false;
    }

    /**
     * do apply worker
     *
     * @param minId minId
     * @param group group
     * @param uidKey uidKey
     * @param processId process
     * @param ip local ip
     * @param expireInterval worker expire interval
     * @return key: exist flag, value: work node
     */
    public Pair<Boolean, WorkerNode> doApplyWorkerFromExistExpire(Long minId, String group, String uidKey,
        String processId, String ip, long expireInterval) {

        WorkerNode existedWorkerNode = workerNodeDAO.selectForUpdate(minId);

        if (existedWorkerNode == null) {
            return Pair.of(false, null);
        }

        if (existedWorkerNode.getLastExpireTime().compareTo(new Date()) < 0) {

            // do loop allocate workerId for workerNode
            long workerId = existedWorkerNode.getWorkerId();
            Pair<Boolean, WorkerNode> allocateResult = doLoopAllocateWorkerNode(group, expireInterval,
                existedWorkerNode, workerId);
            if (allocateResult != null) {
                return allocateResult;
            }
        }
        return new Pair<>(false, null);
    }


    /**
     * get current instance process Id
     *
     * @return process id
     */
    private String getProcessIdStr() {
        return ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
    }

    /**
     * Assign worker id base on database
     *
     * @return assigned worker id
     */
    @Override
    public long assignWorkerId() {
        return workerNode.getWorkerId();
    }

}
