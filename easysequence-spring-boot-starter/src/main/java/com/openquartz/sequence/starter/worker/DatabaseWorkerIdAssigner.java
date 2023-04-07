package com.openquartz.sequence.starter.worker;

import static com.openquartz.sequence.core.uid.snowflake.exception.SnowflakeExceptionCode.GROUP_NOT_EXIST_ERROR;

import com.openquartz.sequence.core.uid.snowflake.exception.SnowflakeExceptionCode;
import com.openquartz.sequence.core.uid.snowflake.worker.WorkerIdAssigner;
import com.openquartz.sequence.core.uid.snowflake.worker.WorkerNode;
import com.openquartz.sequence.generator.common.exception.Asserts;
import com.openquartz.sequence.generator.common.exception.EasySequenceException;
import com.openquartz.sequence.generator.common.utils.NetUtils;
import com.openquartz.sequence.generator.common.utils.Pair;
import com.openquartz.sequence.generator.common.utils.StringUtils;
import com.openquartz.sequence.starter.persist.WorkerNodeDAO;
import com.openquartz.sequence.starter.transaction.TransactionSupport;
import java.lang.management.ManagementFactory;
import java.sql.Timestamp;
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
        scheduler.scheduleWithFixedDelay(this::refreshNodeInfo, 10000, property.getWorkerHeartbeatInterval(),
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
        for (long i = property.getMinWorkerId(); i < property.getMaxWorkerId(); i++) {
            try {
                doInsertWorkerId();
                return;
            } catch (EasySequenceException ex) {
                throw ex;
            } catch (Exception ex) {
                log.warn("[DatabaseWorkerIdAssigner#insertWorkerId]group:{} allocate occur error!errorMsg:{}",
                    property.getGroup(), ex.getMessage());
            }
        }
        Asserts.notNull(workerNode, SnowflakeExceptionCode.GROUP_WORKER_ASSIGN_ERROR);
    }

    /**
     * do insert workerId
     */
    private void doInsertWorkerId() {
        Integer maxWorkerId = workerNodeDAO.getMaxWorkerId(property.getGroup());
        if (maxWorkerId == null) {
            workerNode = workerNodeDAO
                .addWorkerNode(generateWorkerNode(Long.valueOf(property.getMinWorkerId()).intValue()));
        } else {
            if (maxWorkerId + 1 < property.getMaxWorkerId()) {
                workerNode = workerNodeDAO.addWorkerNode(generateWorkerNode(maxWorkerId + 1));
            } else {
                log.error(
                    "[DatabaseWorkerIdAssigner#doInsertWorkerId] group:{} has full worker,insert workerId error!",
                    property.getGroup());
                throw EasySequenceException
                    .replacePlaceHold(SnowflakeExceptionCode.GROUP_WORKER_FULL_ERROR, property.getGroup());
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
            .call(() -> workerNodeDAO.doApplyWorkerFromExistExpire(minId, property.getGroup(), uidKey, processId, ip,
                property.getWorkerExpireInterval()));
        if (pair.getKey()) {
            workerNode = pair.getValue();
            return true;
        }
        return false;
    }

    private boolean doApplyWorkerExtendPre() {
        WorkerNode selfNode = workerNodeDAO.selectSelfWorkNode(property.getGroup(), ip);
        if (Objects.isNull(selfNode)) {
            return false;
        }
        // tx
        Pair<Boolean, WorkerNode> nodePair = transactionSupport.call(() ->
            workerNodeDAO.doApplyWorkerFromExistSelf(selfNode.getId(), property.getGroup(), uidKey, processId, ip,
                property.getWorkerExpireInterval()));
        if (nodePair.getKey()) {
            workerNode = nodePair.getValue();
            return true;
        }
        return false;
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
