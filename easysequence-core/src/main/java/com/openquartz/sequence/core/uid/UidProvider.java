package com.openquartz.sequence.core.uid;

/**
 * @author svnee
 **/
public interface UidProvider {

    /**
     * nextId
     *
     * @return id
     */
    long nextId();

    /**
     * nextId
     *
     * @param key biz-key
     * @return key
     */
    long nextId(String key);

    /**
     * 反解析uid
     *
     * @param uid uid
     * @return 解析结果
     */
    default String parseUid(long uid) {
        return String.valueOf(uid);
    }

    /**
     * 反解析uid
     *
     * @param key key
     * @param uid uid
     * @return 解析结果
     */
    default String parseUid(String key, long uid) {
        return String.valueOf(uid);
    }
}
