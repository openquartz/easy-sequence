package com.openquartz.sequence.starter.persist;

import com.openquartz.sequence.core.persist.mapper.SequenceTemplateMapper;
import com.openquartz.sequence.core.persist.model.SequenceTemplate;
import com.openquartz.sequence.generator.common.utils.CollectionUtils;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/**
 * SequenceTemplateMapperImpl
 *
 * @author svnee
 **/
public class SequenceTemplateMapperImpl implements SequenceTemplateMapper {

    private final JdbcTemplate jdbcTemplate;

    public SequenceTemplateMapperImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final String SEQUENCE_TEMPLATE_QUERY_SQL = "SELECT id, register_code,`expression` FROM es_sequence_template WHERE register_code = ?";

    @Override
    public SequenceTemplate selectByRegisterCode(String registerCode) {
        List<SequenceTemplate> templateList = jdbcTemplate
            .query(SEQUENCE_TEMPLATE_QUERY_SQL, new Object[]{registerCode}, new SequenceTemplateRowMapper());
        return CollectionUtils.isNotEmpty(templateList) ? templateList.get(0) : null;
    }

    private static class SequenceTemplateRowMapper implements RowMapper<SequenceTemplate> {

        @Override
        public SequenceTemplate mapRow(ResultSet rs, int rowNum) throws SQLException {
            DbSequenceTemplate template = new DbSequenceTemplate();
            template.setId(rs.getLong("id"));
            template.setRegisterCode(rs.getString("register_code"));
            template.setExpression(rs.getString("expression"));
            return template;
        }
    }

}
