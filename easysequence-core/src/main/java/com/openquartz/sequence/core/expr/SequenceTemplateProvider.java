package com.openquartz.sequence.core.expr;

import com.openquartz.sequence.core.persist.model.SequenceTemplate;

/**
 * SequenceTemplateProvider
 *
 * @author svnee
 */
public interface SequenceTemplateProvider {

    /**
     * get sequence template
     *
     * @param registerCode register code
     * @return template
     */
    SequenceTemplate get(String registerCode);

}
