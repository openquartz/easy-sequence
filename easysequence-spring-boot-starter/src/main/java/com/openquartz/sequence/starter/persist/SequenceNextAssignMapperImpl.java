package com.openquartz.sequence.starter.persist;

import com.openquartz.sequence.core.dictionary.CycleUnit;
import com.openquartz.sequence.core.persist.mapper.SequenceNextAssignMapper;
import com.openquartz.sequence.core.persist.model.SequenceNextAssign;
import com.openquartz.sequence.generator.common.utils.CollectionUtils;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

/**
 * 序列号
 *
 * @author svnee
 **/
public class SequenceNextAssignMapperImpl implements SequenceNextAssignMapper {

    private final JdbcTemplate jdbcTemplate;

    public SequenceNextAssignMapperImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final String NEXT_ASSIGNER_INSERT_SQL =
        "INSERT INTO es_sequence_next_assign(unique_key, next_value, last_assign_time,`cycle`,cycle_unit, init_value) VALUES (?,?,?,?,?,?)";
    private static final String NEXT_ASSIGN_QUERY_SQL =
        "SELECT id,unique_key, next_value, last_assign_time,`cycle`,cycle_unit,init_value FROM es_sequence_next_assign WHERE unique_key=?";
    private static final String RESET_NEXT_VALUE_SQL =
        "UPDATE es_sequence_next_assign SET next_value = next_value + 1 WHERE unique_key = ? AND next_value= ?";
    private static final String ASSIGN_INCREAMENT_SQL =
        "UPDATE es_sequence_next_assign SET next_value = next_value + ?, last_assign_time = now() WHERE unique_key = ? AND next_value = ?";

    @Override
    public int insert(SequenceNextAssign assigner) {

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {

            PreparedStatement ps = connection
                .prepareStatement(NEXT_ASSIGNER_INSERT_SQL, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, assigner.getUniqueKey());
            ps.setLong(2, assigner.getNextValue());
            ps.setDate(3, Date.valueOf(assigner.getLastAssignTime()));
            ps.setInt(4, assigner.getCycle());
            ps.setString(5, assigner.getCycleUnit().getCode());
            ps.setLong(6, assigner.getNextValue());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (Objects.nonNull(key)) {
            assigner.setId(key.longValue());
            return 1;
        }
        return 0;
    }

    @Override
    public SequenceNextAssign selectByKey(String key) {
        List<SequenceNextAssign> assignList = jdbcTemplate
            .query(NEXT_ASSIGN_QUERY_SQL, new Object[]{key}, new SequenceNextAssignRowMapper());
        if (CollectionUtils.isNotEmpty(assignList)) {
            return assignList.get(0);
        }
        return null;
    }

    private static class SequenceNextAssignRowMapper implements RowMapper<SequenceNextAssign> {

        @Override
        public SequenceNextAssign mapRow(ResultSet rs, int rowNum) throws SQLException {

            SequenceNextAssign nextAssign = new SequenceNextAssign();
            nextAssign.setId(rs.getLong("id"));
            nextAssign.setUniqueKey(rs.getString("unique_key"));
            nextAssign.setNextValue(rs.getLong("next_value"));
            nextAssign.setCycle(rs.getInt("cycle"));
            nextAssign.setCycleUnit(CycleUnit.fromCode(rs.getString("cycle_unit")));
            nextAssign.setInitValue(rs.getLong("init_value"));
            Timestamp timestamp = rs.getTimestamp("last_assign_time");
            nextAssign.setLastAssignTime(timestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            return nextAssign;
        }
    }


    @Override
    public int resetNextValue(String registerCode, Long preValue) {
        return jdbcTemplate.update(RESET_NEXT_VALUE_SQL, registerCode, preValue);
    }

    @Override
    public int incrementBy(String key, Long step, Long nextValue) {
        return jdbcTemplate.update(ASSIGN_INCREAMENT_SQL, step, key, nextValue);
    }
}
