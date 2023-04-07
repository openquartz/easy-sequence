package com.openquartz.sequence.core.persist.model;

import com.openquartz.sequence.core.dictionary.CycleUnit;
import java.time.LocalDate;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author svnee
 */
@Data
@Accessors(chain = true)
public class SequenceNextAssign {

    private Long id;

    private String uniqueKey;

    private Long nextValue;

    private LocalDate lastAssignTime;

    private Integer cycle;

    private CycleUnit cycleUnit;

    private Long initValue;
}
