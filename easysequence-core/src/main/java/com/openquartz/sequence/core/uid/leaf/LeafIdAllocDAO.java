package com.openquartz.sequence.core.uid.leaf;

import java.util.List;

/**
 * LeafIdAllocDAO
 *
 * @author svnee
 */
public interface LeafIdAllocDAO {

    /**
     * get all leafAlloc
     *
     * @return LeafAlloc
     */
    List<LeafAlloc> getAllLeafAllocs();

    /**
     * updateMaxIdAndGetLeafAlloc
     *
     * @param tag tag
     * @return LeafAlloc
     */
    LeafAlloc updateMaxIdAndGetLeafAlloc(String tag);

    /**
     * updateMaxIdByCustomStepAndGetLeafAlloc
     *
     * @param leafAlloc leafAlloc
     * @return LeafAlloc
     */
    LeafAlloc updateMaxIdByCustomStepAndGetLeafAlloc(LeafAlloc leafAlloc);

    /**
     * getAllTags
     *
     * @return tags
     */
    List<String> getAllTags();
}
