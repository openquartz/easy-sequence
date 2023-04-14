package com.openquartz.sequence.starter.persist;

import com.openquartz.sequence.core.uid.leaf.LeafAlloc;
import com.openquartz.sequence.core.uid.leaf.LeafIdAllocDAO;
import com.openquartz.sequence.generator.common.transaction.TransactionSupport;
import com.openquartz.sequence.generator.common.utils.CollectionUtils;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;

/**
 * LeafIdAllocDAOImpl
 *
 * @author svnee
 **/
public class LeafIdAllocDAOImpl implements LeafIdAllocDAO {

    private final JdbcTemplate jdbcTemplate;
    private final TransactionSupport transactionSupport;

    private static final String SELECT_ALL_SQL = "SELECT biz_tag, max_id, step, update_time FROM es_leaf_alloc";
    private static final String SELECT_LEAF_ALLOC_TAG_SQL = "SELECT biz_tag, max_id, step, update_time FROM es_leaf_alloc WHERE biz_tag = ?";
    private static final String UPDATE_MAX_ID_SQL = "UPDATE es_leaf_alloc SET max_id = max_id + step WHERE biz_tag = ?";
    private static final String UPDATE_MAX_ID_CUSTOM_STEP_SQL = "UPDATE es_leaf_alloc SET max_id = max_id + ? WHERE biz_tag = ?";
    private static final String GET_ALL_TAG_SQL = "SELECT biz_tag FROM es_leaf_alloc";

    public LeafIdAllocDAOImpl(JdbcTemplate jdbcTemplate,
        TransactionSupport transactionSupport) {
        this.jdbcTemplate = jdbcTemplate;
        this.transactionSupport = transactionSupport;
    }

    @Override
    public List<LeafAlloc> getAllLeafAllocs() {
        return jdbcTemplate.query(SELECT_ALL_SQL, new LeafAllocRow());
    }

    @Override
    public LeafAlloc updateMaxIdAndGetLeafAlloc(String tag) {
        return transactionSupport.execute(() -> {
            jdbcTemplate.update(UPDATE_MAX_ID_SQL, tag);
            List<LeafAlloc> allocList = jdbcTemplate.query(SELECT_LEAF_ALLOC_TAG_SQL, new LeafAllocRow(), tag);
            return CollectionUtils.isNotEmpty(allocList) ? allocList.get(0) : null;
        });
    }

    @Override
    public LeafAlloc updateMaxIdByCustomStepAndGetLeafAlloc(LeafAlloc leafAlloc) {
        return transactionSupport.execute(() -> {
            jdbcTemplate.update(UPDATE_MAX_ID_CUSTOM_STEP_SQL, leafAlloc.getStep(), leafAlloc.getKey());
            List<LeafAlloc> allocList = jdbcTemplate
                .query(SELECT_LEAF_ALLOC_TAG_SQL, new LeafAllocRow(), leafAlloc.getKey());
            return CollectionUtils.isNotEmpty(allocList) ? allocList.get(0) : null;
        });
    }

    @Override
    public List<String> getAllTags() {
        return jdbcTemplate.queryForList(GET_ALL_TAG_SQL, String.class);
    }

    public static class LeafAllocRow implements RowMapper<LeafAlloc> {

        @Override
        public LeafAlloc mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
            LeafAlloc leafAlloc = new LeafAlloc();
            leafAlloc.setKey(rs.getString("biz_tag"));
            leafAlloc.setMaxId(rs.getLong("max_id"));
            leafAlloc.setStep(rs.getInt("step"));
            leafAlloc.setUpdateTime(rs.getString("update_time"));
            return leafAlloc;
        }
    }
}


