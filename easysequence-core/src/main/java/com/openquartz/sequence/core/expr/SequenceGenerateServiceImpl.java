package com.openquartz.sequence.core.expr;

import com.google.common.collect.Lists;
import com.openquartz.sequence.core.expr.cmd.AssignExtParam;
import com.openquartz.sequence.core.expr.cmd.MagicExpression;
import com.openquartz.sequence.core.expr.executors.SeqExecutor;
import com.openquartz.sequence.core.persist.model.SequenceTemplate;
import java.util.List;

/**
 * @author svnee
 **/
public class SequenceGenerateServiceImpl implements SequenceGenerateService {

    private final MagicExpression magicExpression;
    private final SequenceTemplateProvider sequenceTemplateProvider;

    public SequenceGenerateServiceImpl(SequenceTemplateProvider sequenceTemplateProvider) {
        this.sequenceTemplateProvider = sequenceTemplateProvider;
        this.magicExpression = MagicExpression
            .builder()
            .registerExecutor("seq", SeqExecutor.class)
            .build();
    }

    @Override
    public String generateCode(String registerCode) {
        SequenceTemplate template = sequenceTemplateProvider.get(registerCode);
        return magicExpression.execute(template.getExpression());
    }

    @Override
    public String generateCode(String registerCode, AssignExtParam assignExtParam) {
        SequenceTemplate template = sequenceTemplateProvider.get(registerCode);
        return magicExpression.execute(template.getExpression(), assignExtParam.getParams());
    }

    @Override
    public List<String> batchGenerateCode(String registerCode, int qty) {
        SequenceTemplate template = sequenceTemplateProvider.get(registerCode);
        List<String> sequenceResultList = Lists.newArrayListWithCapacity(qty);
        for (int i = 0; i < qty; i++) {
            sequenceResultList.add(magicExpression.execute(template.getExpression()));
        }
        return sequenceResultList;
    }

    @Override
    public List<String> batchGenerateCode(String registerCode, int qty, AssignExtParam param) {
        SequenceTemplate template = sequenceTemplateProvider.get(registerCode);
        List<String> sequenceResultList = Lists.newArrayListWithCapacity(qty);
        for (int i = 0; i < qty; i++) {
            sequenceResultList.add(magicExpression.execute(template.getExpression(), param.getParams()));
        }
        return sequenceResultList;
    }

}
