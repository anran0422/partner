-- 用户表
create table user
(
    id           bigint auto_increment comment 'id'
        primary key,
    username     varchar(256) null comment '用户昵称',
    userAccount  varchar(256) null comment '账号',
    avatarUrl    varchar(1024) null comment '用户头像',
    gender       tinyint null comment '性别',
    userPassword varchar(512)       not null comment '密码',
    phone        varchar(128) null comment '电话',
    email        varchar(512) null comment '邮箱',
    userStatus   int      default 0 not null comment '用户状态 0 正常',
    createTime   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTme    datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint  default 0 not null comment '是否删除',
    userRole     int      default 0 not null comment '用户角色：0-普通用户 1-管理员',
    planetCode   varchar(512) null comment '星球编号',
    tags         varchar(1024) null comment '标签JSON列表'
) comment '用户表';

-- 队伍表
create table team
(
    id          bigint auto_increment comment 'id'
        primary key,
    name        varchar(256) null comment '队伍名称',
    description varchar(256) comment '描述',
    maxNum      int      default 1 not null comment '最大人数',
    expireTime  datetime null comment '过期时间',
    userId      bigint comment '用户id（队长 id）',
    status      int      default 0 not null comment '0-公开 1-私有 2-加密',
    password    varchar(512) null comment '密码',

    createTime  datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTme   datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint  default 0 not null comment '是否删除'
) comment '队伍表';

-- 用户队伍关系表
create table user_team
(
    id         bigint auto_increment comment 'id'
        primary key,
    userId     bigint comment '用户id',
    teamId     bigint comment '队伍id',
    joinTime   datetime null comment '加入时间',

    createTime datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTme  datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0 not null comment '是否删除'
) comment '用户队伍关系表';

-- 标签表
create table tag
(
    id         bigint auto_increment comment 'id'
        primary key,
    tagName    varchar(256) null comment '标签名称',
    userId     bigint null comment '用户 id',
    parentId   bigint null comment '父标签 id',
    idParent   tinyint null comment '0 - 不是，1 - 是父标签',
    createTime datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTme  datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0 not null comment '是否删除',
    constraint unique_tagName
        unique (tagName)
) comment '标签表';

create index idx_userId
    on tag (userId);

