CREATE TABLE es_sequence_assign_register
(
    id            INT (11) NOT NULL AUTO_INCREMENT COMMENT 'ID',
    register_code VARCHAR(50) NOT NULL DEFAULT '' COMMENT '注册码',
    register_desc VARCHAR(50) NOT NULL DEFAULT '' COMMENT '描述',
    `cycle`       INT (11) NOT NULL DEFAULT -1 COMMENT '周期',
    cycle_unit    VARCHAR(50) NOT NULL DEFAULT '' COMMENT '周期单位',
    init_value    BIGINT (20) NOT NULL DEFAULT -1 COMMENT '初始值',
    create_time   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '序列号注册器';

CREATE TABLE es_sequence_next_assign
(
    id               BIGINT (20) NOT NULL AUTO_INCREMENT COMMENT 'id',
    unique_key       VARCHAR(50) NOT NULL DEFAULT '' COMMENT '唯一key',
    next_value       BIGINT (15) NOT NULL DEFAULT -1 COMMENT '下一次值',
    last_assign_time DATETIME    NOT NULL COMMENT '下一次生成时间',
    `cycle`          INT (10) NOT NULL DEFAULT -1 COMMENT '循环',
    cycle_unit       VARCHAR(50) NOT NULL DEFAULT '' COMMENT '循环单位',
    init_value       BIGINT (20) NOT NULL DEFAULT -1 COMMENT '初始值',
    PRIMARY KEY (id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '序列下一次生成';

CREATE TABLE es_sequence_template
(
    id            BIGINT (20) NOT NULL AUTO_INCREMENT COMMENT 'id',
    register_code VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '唯一key',
    `expression`  varchar(512) NOT NULL DEFAULT '' COMMENT '表达式',
    PRIMARY KEY (id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '序列号生成模板';

CREATE TABLE `es_snowflake_worker_node`
(
    `id`               bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `group`            varchar(50) NOT NULL DEFAULT '' COMMENT 'group',
    `worker_id`        int(5) NOT NULL DEFAULT 0 COMMENT 'workerId',
    `uid`              varchar(50) NOT NULL DEFAULT '' COMMENT '本次启动唯一id',
    `process_id`       varchar(50) NOT NULL DEFAULT '' COMMENT '进程ID',
    `ip`               varchar(50) NOT NULL DEFAULT '' COMMENT 'ip address',
    `last_expire_time` timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下次失效时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uniq_group_worker` (`group`,`worker_id`),
    UNIQUE KEY `uniq_group_ip` (`group`,`ip`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='雪花算法WorkNode';

CREATE TABLE `es_leaf_alloc`
(
    `biz_tag`     varchar(128) NOT NULL DEFAULT '',
    `max_id`      bigint(20) NOT NULL DEFAULT '1',
    `step`        int(11) NOT NULL,
    `description` varchar(256)          DEFAULT NULL,
    `update_time` timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`biz_tag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='LeafId分配';