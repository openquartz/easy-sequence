package com.openquartz.sequence.core.expr.persist.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.openquartz.sequence.core.expr.SequenceTemplateProvider;
import com.openquartz.sequence.core.expr.exception.SequenceGenerateExceptionCode;
import com.openquartz.sequence.core.persist.mapper.SequenceTemplateMapper;
import com.openquartz.sequence.core.persist.model.SequenceTemplate;
import com.openquartz.sequence.generator.common.exception.Asserts;
import com.openquartz.sequence.generator.common.utils.ExceptionUtils;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

/**
 * @author svnee
 **/
@Slf4j
public class DatabaseSequenceTemplateProviderImpl implements SequenceTemplateProvider {

    private final SequenceTemplateMapper sequenceTemplateMapper;

    private final Cache<String, SequenceTemplate> localCache = CacheBuilder.newBuilder()
        .maximumSize(200)
        .concurrencyLevel(4)
        .expireAfterAccess(10, TimeUnit.MINUTES)
        .build();

    public DatabaseSequenceTemplateProviderImpl(
        SequenceTemplateMapper sequenceTemplateMapper) {
        this.sequenceTemplateMapper = sequenceTemplateMapper;
    }

    @Override
    public SequenceTemplate get(String registerCode) {
        try {
            SequenceTemplate template = localCache
                .get(registerCode, () -> sequenceTemplateMapper.selectByRegisterCode(registerCode));
            Asserts.notNull(template, SequenceGenerateExceptionCode.SEQUENCE_TEMPLATE_NOT_EXIST_ERROR);
            return template;
        } catch (Exception ex) {
            log.error("[DatabaseSequenceTemplateProviderImpl#get] occur error!registerCode:{}", registerCode, ex);
            return ExceptionUtils.rethrow(ex);
        }
    }

}
