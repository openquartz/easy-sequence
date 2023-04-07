package com.openquartz.sequence.starter.persist;

import com.openquartz.sequence.core.dictionary.CycleUnit;
import com.openquartz.sequence.core.persist.mapper.SequenceAssignRegisterMapper;
import com.openquartz.sequence.core.persist.model.SequenceAssignRegister;
import com.openquartz.sequence.generator.common.utils.CollectionUtils;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/**
 * @author svnee
 **/
public class SequenceAssignRegisterMapperImpl implements SequenceAssignRegisterMapper {

    private final JdbcTemplate jdbcTemplate;

    public SequenceAssignRegisterMapperImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final String SEQUENCE_ASSIGN_REGISTER_QUERY_SQL =
        "SELECT id,register_code,register_desc,`cycle`,cycle_unit,init_value,create_time,update_time FROM es_sequence_assign_register WHERE register_code=?";

    @Override
    public SequenceAssignRegister selectByRegisterCode(String registerCode) {
        List<SequenceAssignRegister> assignRegisterList = jdbcTemplate
            .query(SEQUENCE_ASSIGN_REGISTER_QUERY_SQL, new Object[]{registerCode},
                new SequenceAssignRegisterRowMapper());
        if (CollectionUtils.isNotEmpty(assignRegisterList)) {
            return assignRegisterList.get(0);
        }
        return null;
    }

    private static class SequenceAssignRegisterRowMapper implements RowMapper<SequenceAssignRegister> {

        @Override
        public SequenceAssignRegister mapRow(ResultSet rs, int rowNum) throws SQLException {
            SequenceAssignRegister assignRegister = new SequenceAssignRegister();
            assignRegister.setId(rs.getInt("id"));
            assignRegister.setRegisterCode(rs.getString("register_code"));
            assignRegister.setRegisterDesc(rs.getString("register_desc"));
            assignRegister.setCycle(rs.getInt("cycle"));
            assignRegister.setCycleUnit(CycleUnit.fromCode(rs.getString("cycle_unit")));
            assignRegister.setInitValue(rs.getLong("init_value"));
            assignRegister.setCreateTime(rs.getDate("create_time"));
            assignRegister.setUpdateTime(rs.getDate("update_time"));
            return assignRegister;
        }
    }
}
