package com.openquartz.sequence.generator.common.bean;

/**
 * LifeStyle Bean
 *
 * @author svnee
 */
public interface LifestyleBean {

    /**
     * init method
     */
    default void init() {
    }

    /**
     * destroy method
     */
    default void destroy() {
    }

}
