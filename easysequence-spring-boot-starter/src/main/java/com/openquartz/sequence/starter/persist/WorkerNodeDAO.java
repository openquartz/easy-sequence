package com.openquartz.sequence.starter.persist;

import com.openquartz.sequence.core.uid.snowflake.worker.WorkerNode;
import com.openquartz.sequence.generator.common.utils.CollectionUtils;
import com.openquartz.sequence.generator.common.utils.Pair;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;


/**
 * DAO for M_WORKER_NODE
 *
 * @author svnee
 */
public class WorkerNodeDAO {

    private final JdbcTemplate jdbcTemplate;

    public WorkerNodeDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final String ADD_SQL = "INSERT INTO es_snowflake_worker_node(ip, `group`, `worker_id`, uid, process_id, last_expire_time) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String DELETE_SQL = "DELETE FROM es_snowflake_worker_node WHERE id = ?";
    private static final String SELECT_MIN_ID_SQL = "SELECT id FROM es_snowflake_worker_node WHERE `group` =? AND last_expire_time < ? order by last_expire_time limit 1";
    private static final String SELECT_ID_FOR_UPDATE = "SELECT id, ip, `group`, `worker_id`, uid, process_id, last_expire_time FROM es_snowflake_worker_node WHERE id = ? FOR UPDATE";
    private static final String UPDATE_BY_ID_SQL = "UPDATE es_snowflake_worker_node SET ip = ?,`group`=?,worker_id=?,uid=?,process_id=?,last_expire_time=? WHERE id = ?";
    private static final String UPDATE_LAST_EXPIRE_TIME_SQL = "UPDATE es_snowflake_worker_node SET last_expire_time=? WHERE id = ?";
    private static final String SELECT_MAX_WORKER_ID_SQL = "SELECT max(worker_id) FROM es_snowflake_worker_node WHERE `group` = ?";
    private static final String SELECT_NODE_BY_IP_GROUP_SQL = "SELECT id, ip, `group`, `worker_id`, uid, process_id, last_expire_time FROM es_snowflake_worker_node where `group`= ? and ip = ?";

    /**
     * return minId in the group
     *
     * @param group group
     * @return minId
     */
    public Long selectMinId(String group) {
        List<Long> list = jdbcTemplate.queryForList(SELECT_MIN_ID_SQL, new Object[]{group, new Date()}, Long.class);
        return CollectionUtils.isNotEmpty(list) ? list.get(0) : null;
    }

    /**
     * 查询自身的workNode节点
     *
     * @param group 分组
     * @param ip ip 地址
     * @return 分组
     */
    public WorkerNode selectSelfWorkNode(String group, String ip) {
        List<WorkerNode> nodeList = jdbcTemplate
            .query(SELECT_NODE_BY_IP_GROUP_SQL, new WorkerNodeRowMapper(), group, ip);
        return CollectionUtils.isNotEmpty(nodeList) ? nodeList.get(0) : null;
    }

    /**
     * 根据删除ID
     *
     * @param id ID
     */
    public void delete(Long id) {
        jdbcTemplate.update(DELETE_SQL, id);
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

        WorkerNode workerNode = jdbcTemplate.queryForObject(SELECT_ID_FOR_UPDATE, new WorkerNodeRowMapper(), minId);

        if (workerNode == null) {
            return Pair.of(false, null);
        }

        if (workerNode.getLastExpireTime().compareTo(new Date()) < 0) {

            // refresh work node
            WorkerNode node = refreshWorkNode(group, uidKey, processId, ip, expireInterval, workerNode);

            return Pair.of(true, node);
        }
        return new Pair<>(false, null);
    }


    /**
     * do apply worker
     *
     * @param selfId selfId
     * @param group group
     * @param uidKey uidKey
     * @param processId process
     * @param ip local ip
     * @param expireInterval worker expire interval
     * @return key: exist flag, value: work node
     */
    public Pair<Boolean, WorkerNode> doApplyWorkerFromExistSelf(Long selfId, String group, String uidKey,
        String processId, String ip, long expireInterval) {

        WorkerNode workerNode = jdbcTemplate.queryForObject(SELECT_ID_FOR_UPDATE, new WorkerNodeRowMapper(), selfId);

        if (workerNode == null) {
            return Pair.of(false, null);
        }

        // work node expire or work node belong self
        boolean canApply = workerNode.getLastExpireTime().compareTo(new Date()) < 0
            || (Objects.equals(group, workerNode.getGroup()) && Objects.equals(ip, workerNode.getIp()));
        if (canApply) {

            // refresh work nde
            WorkerNode node = refreshWorkNode(group, uidKey, processId, ip, expireInterval, workerNode);

            return Pair.of(true, node);
        }
        return new Pair<>(false, null);
    }

    private WorkerNode refreshWorkNode(String group, String uidKey, String processId, String ip, long expireInterval,
        WorkerNode workerNode) {
        WorkerNode node = new WorkerNode();
        node.setId(workerNode.getId());
        node.setWorkerId(workerNode.getWorkerId());
        node.setGroup(group);
        node.setLastExpireTime(new Timestamp(System.currentTimeMillis() + expireInterval));
        node.setUid(uidKey);
        node.setProcessId(processId);
        node.setIp(ip);

        // update work node info
        updateById(node);
        return node;
    }

    public Integer getMaxWorkerId(String group) {
        return jdbcTemplate.queryForObject(SELECT_MAX_WORKER_ID_SQL, Integer.class, group);
    }

    private static class WorkerNodeRowMapper implements RowMapper<WorkerNode> {

        @Override
        public WorkerNode mapRow(ResultSet rs, int rowNum) throws SQLException {
            final WorkerNode workerNode = new WorkerNode();
            workerNode.setId(rs.getLong("id"));
            workerNode.setIp(rs.getString("ip"));
            workerNode.setGroup(rs.getString("group"));
            workerNode.setWorkerId(rs.getInt("worker_id"));
            workerNode.setUid(rs.getString("uid"));
            workerNode.setProcessId(rs.getString("process_id"));
            workerNode.setLastExpireTime(rs.getTimestamp("last_expire_time"));
            return workerNode;
        }
    }

    /**
     * Add {@link WorkerNode}
     *
     * @param workerNode workNode
     */
    public WorkerNode addWorkerNode(WorkerNode workerNode) {

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(ADD_SQL, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, workerNode.getIp());
            ps.setString(2, workerNode.getGroup());
            ps.setInt(3, workerNode.getWorkerId());
            ps.setString(4, workerNode.getUid());
            ps.setString(5, workerNode.getProcessId());
            ps.setTimestamp(6, workerNode.getLastExpireTime());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (Objects.nonNull(key)) {
            workerNode.setId(key.longValue());
        }
        return workerNode;
    }

    /**
     * update by id
     *
     * @param workerNode workNode
     * @return affect row
     */
    public int updateById(WorkerNode workerNode) {
        return jdbcTemplate.update(UPDATE_BY_ID_SQL, workerNode.getIp(), workerNode.getGroup(),
            workerNode.getWorkerId(),
            workerNode.getUid(),
            workerNode.getProcessId(),
            workerNode.getLastExpireTime(),
            workerNode.getId());
    }

    /**
     * refresh last expire time
     *
     * @param id id
     * @param lastExpire last expire time
     * @return affect row
     */
    public int updateLastExpireTime(Long id, Timestamp lastExpire) {
        return jdbcTemplate.update(UPDATE_LAST_EXPIRE_TIME_SQL, lastExpire, id);
    }
}
