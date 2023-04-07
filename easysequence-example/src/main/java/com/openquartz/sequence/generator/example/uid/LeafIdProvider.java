package com.openquartz.sequence.generator.example.uid;

import com.openquartz.sequence.core.uid.leaf.LeafIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author svnee
 **/
@Component
@RequiredArgsConstructor
public class LeafIdProvider {

    private final LeafIdGenerator leafIdGenerator;

    public Long generateId() {
        return leafIdGenerator.nextId();
    }


}
