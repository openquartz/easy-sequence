# EasySequence 序列生成器

-----

## 分布式ID

----

### 雪花ID

雪花算法分布式ID生成。入口：`com.openquartz.sequence.core.uid.snowflake.SnowflakeIdProvider.nextId()`
如果开启了雪花算法，可以直接从工厂中取出。

开启雪花算法`easysequence.uid.snowflake.enabled=true`

```properties
easysequence.uid.snowflake.enabled=true
easysequence.uid.snowflake.type=cache
easysequence.uid.snowflake.worker-id.min=0
easysequence.uid.snowflake.worker-id.max=32
easysequence.uid.snowflake.worker-id.default-val=0
easysequence.uid.snowflake.default-group=default
```

雪花算法目前支持两种算法实现。

1、**标准版本**;

2、**Cache模式** 采用百度的 [UidGenerator](https://github.com/baidu/uid-generator) 实现

> 相较于标准版本的实现CacheUidGenerator 性能更高。 具体的选择可以自己根据实际场景进行切换选择。用户可以实现一键配置`easysequence.uid.snowflake.type`自定义切换。
`default`: 表示标准版本;`cache`: 表示百度的CacheUidGenerator实现。

- 1、**标准模式** 使用dataCenterId+workerId 模式构成。所以最大workerId范围在[0,32)。用户也可在这个范围内进行缩小
- 2、**Cache模式** 的workerId 使用 workerId模式构成。workerId最大范围在[0,1024)

目前雪花算法的`workerId` 支持三种分配方式：**随机分配**、**Zookeeper分配**、**DB分配**。

#### 随机分配

设置配置：`easysequence.uid.snowflake.worker-assigner-type=random`

不支持不重复

#### Zookeeper分配

需要设置配置 `easysequence.uid.snowflake.worker-assigner-type=zookeeper` 同时 需要依赖`Zookeeper`组件.需要引入相关依赖

```properties
easysequence.uid.snowflake.zookeeper.url=
easysequence.uid.snowflake.zookeeper.worker-heartbeat-interval=3000
easysequence.uid.snowflake.zookeeper.worker-pid-port=-1
easysequence.uid.snowflake.zookeeper.worker-pid-home=/data/pids/
```

#### DB 分配(推荐)

支持使用Jdbc等的关系型数据库。

DB分配实现基于LOOP模式的下的分配。以当前机器IP下优先准则,其次依次LOOP进行分配workerId

需要设置`easysequence.uid.snowflake.worker-assigner-type=db`
配置

```properties
easysequence.uid.snowflake.db.worker-expire-interval=5000
easysequence.uid.snowflake.db.worker-heartbeat-interval=50000
```

同时执行SQL:

```sql
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
```

### LeafID

基于Meituan的LeafId的实现。可是自动装配。更好的集成到项目中。默认不开启。 如需开启可以启用配置

```properties
easysequence.uid.leaf.enabled=true
easysequence.uid.leaf.default-key=leaf-segment-test
```

并且设置默认的key.

需要执行SQL

```sql
CREATE TABLE `es_leaf_alloc`
(
    `biz_tag`     varchar(128) NOT NULL DEFAULT '',
    `max_id`      bigint(20) NOT NULL DEFAULT 1,
    `step`        int(11) NOT NULL,
    `description` varchar(256)          DEFAULT NULL,
    `update_time` timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`biz_tag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='LeafId分配';
```

并且配置默认的分组key.

使用LeafId入口方法为`com.openquartz.sequence.core.uid.leaf.LeafIdGenerator`

## 自定义序列号

----

### 环境配置

#### 1、执行SQL脚本

```sql
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
```

#### 2、配置数据源链接

```yaml
easysequence:
    datasource:
        type: com.zaxxer.hikari.HikariDataSource
        driver-class-name: com.mysql.cj.jdbc.Driver
        url: jdbc:mysql://localhost:3306/easysequence?characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&transformedBitIsBoolean=true&serverTimezone=GMT%2B8
        username: root
        password: 123456
```

### 使用

#### 1、配置表达式

表达式支持串接、嵌套。 多个表达式使用`{ }`进行分隔

##### 表达式分类

###### 常量

语法：`{const 固定常量前缀}`

###### 时间

语法：`{time 时间格式}`

时间格式目前支持：`yyyyMMddHHmmss`

###### 序列

语法：`{seq 序列号标识}`
需要配置到：`es_sequence_assign_register`中

###### 长度补齐

语法：`{fix 字符串 补齐长度 填充字符}`

###### 自定义环境参数传递

语法：`{env 环境标识}`

例如：`{const DEMO}{fix {env w} 4}{time yyyyMMdd}{fix {seq DEMO_GENERATOR {env w}} 5}`

###### 随机字母字符串

语法：`{rand_c 长度 随机范围类型}`
随机范围类型可选值为：`type1`,`type2`,`type3`,`type4`

**`type1`**: `'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
'Y', 'Z'`
`

**`type2`**: `'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
'J', 'K', 'L', 'M', 'N', 'O', 'P',
'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
'Y', 'Z'`

**`type3`**: ` 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h',
'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p',
'q', 'r', 's', 't', 'u', 'v', 'w', 'x',
'y', 'z'`

**`type4`**: ` '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'`

###### 随机数字字符串

语法：`{rand_n 长度}`

#### 2、使用表达式

使用序列号生成代码入口:`com.openquartz.sequence.core.expr.SequenceGenerateService`

#### 3、启用序列池

由于性能考虑不会每次都从DB中加载，做了池化缓存一定的序列。可以配置启用或关闭。 针对不同的序列的注册码的缓存的数量可以自定义配置。也可以直接配置对应的默认配置

```properties
###sequence-pool
easysequence.sequence.pool.enable=true
easysequence.sequence.pool.wait-fetch-timeout=5000
easysequence.sequence.pool.default-property.pre-count=50
easysequence.sequence.pool.default-property.water-level-threshold=5
easysequence.sequence.pool.custom-property.TEST1.pre-count=50
easysequence.sequence.pool.custom-property.TEST1.water-level-threshold=5
```
