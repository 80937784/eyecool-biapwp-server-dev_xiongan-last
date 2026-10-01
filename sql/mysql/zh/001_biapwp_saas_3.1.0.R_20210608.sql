-- ----------------------------
-- 执行此SQL文件需要修改如下数据库名为实际的数据库名称
-- ----------------------------
SET @schemaName = 'eyecool_assps';

-- ----------------------------
-- 1、存储每一个已配置的 jobDetail 的详细信息
-- ----------------------------
drop table if exists QRTZ_JOB_DETAILS;
create table QRTZ_JOB_DETAILS (
    sched_name           varchar(120)    not null,
    job_name             varchar(200)    not null,
    job_group            varchar(200)    not null,
    description          varchar(250)    null,
    job_class_name       varchar(250)    not null,
    is_durable           varchar(1)      not null,
    is_nonconcurrent     varchar(1)      not null,
    is_update_data       varchar(1)      not null,
    requests_recovery    varchar(1)      not null,
    job_data             blob            null,
    primary key (sched_name,job_name,job_group)
) engine=innodb;

-- ----------------------------
-- 2、 存储已配置的 Trigger 的信息
-- ----------------------------
drop table if exists QRTZ_TRIGGERS;
create table QRTZ_TRIGGERS (
    sched_name           varchar(120)    not null,
    trigger_name         varchar(200)    not null,
    trigger_group        varchar(200)    not null,
    job_name             varchar(200)    not null,
    job_group            varchar(200)    not null,
    description          varchar(250)    null,
    next_fire_time       bigint(13)      null,
    prev_fire_time       bigint(13)      null,
    priority             integer         null,
    trigger_state        varchar(16)     not null,
    trigger_type         varchar(8)      not null,
    start_time           bigint(13)      not null,
    end_time             bigint(13)      null,
    calendar_name        varchar(200)    null,
    misfire_instr        smallint(2)     null,
    job_data             blob            null,
    primary key (sched_name,trigger_name,trigger_group),
    foreign key (sched_name,job_name,job_group) references QRTZ_JOB_DETAILS(sched_name,job_name,job_group)
) engine=innodb;

-- ----------------------------
-- 3、 存储简单的 Trigger，包括重复次数，间隔，以及已触发的次数
-- ----------------------------
drop table if exists QRTZ_SIMPLE_TRIGGERS;
create table QRTZ_SIMPLE_TRIGGERS (
    sched_name           varchar(120)    not null,
    trigger_name         varchar(200)    not null,
    trigger_group        varchar(200)    not null,
    repeat_count         bigint(7)       not null,
    repeat_interval      bigint(12)      not null,
    times_triggered      bigint(10)      not null,
    primary key (sched_name,trigger_name,trigger_group),
    foreign key (sched_name,trigger_name,trigger_group) references QRTZ_TRIGGERS(sched_name,trigger_name,trigger_group)
) engine=innodb;

-- ----------------------------
-- 4、 存储 Cron Trigger，包括 Cron 表达式和时区信息
-- ---------------------------- 
drop table if exists QRTZ_CRON_TRIGGERS;
create table QRTZ_CRON_TRIGGERS (
    sched_name           varchar(120)    not null,
    trigger_name         varchar(200)    not null,
    trigger_group        varchar(200)    not null,
    cron_expression      varchar(200)    not null,
    time_zone_id         varchar(80),
    primary key (sched_name,trigger_name,trigger_group),
    foreign key (sched_name,trigger_name,trigger_group) references QRTZ_TRIGGERS(sched_name,trigger_name,trigger_group)
) engine=innodb;

-- ----------------------------
-- 5、 Trigger 作为 Blob 类型存储(用于 Quartz 用户用 JDBC 创建他们自己定制的 Trigger 类型，JobStore 并不知道如何存储实例的时候)
-- ---------------------------- 
drop table if exists QRTZ_BLOB_TRIGGERS;
create table QRTZ_BLOB_TRIGGERS (
    sched_name           varchar(120)    not null,
    trigger_name         varchar(200)    not null,
    trigger_group        varchar(200)    not null,
    blob_data            blob            null,
    primary key (sched_name,trigger_name,trigger_group),
    foreign key (sched_name,trigger_name,trigger_group) references QRTZ_TRIGGERS(sched_name,trigger_name,trigger_group)
) engine=innodb;

-- ----------------------------
-- 6、 以 Blob 类型存储存放日历信息， quartz可配置一个日历来指定一个时间范围
-- ---------------------------- 
drop table if exists QRTZ_CALENDARS;
create table QRTZ_CALENDARS (
    sched_name           varchar(120)    not null,
    calendar_name        varchar(200)    not null,
    calendar             blob            not null,
    primary key (sched_name,calendar_name)
) engine=innodb;

-- ----------------------------
-- 7、 存储已暂停的 Trigger 组的信息
-- ---------------------------- 
drop table if exists QRTZ_PAUSED_TRIGGER_GRPS;
create table QRTZ_PAUSED_TRIGGER_GRPS (
    sched_name           varchar(120)    not null,
    trigger_group        varchar(200)    not null,
    primary key (sched_name,trigger_group)
) engine=innodb;

-- ----------------------------
-- 8、 存储与已触发的 Trigger 相关的状态信息，以及相联 Job 的执行信息
-- ---------------------------- 
drop table if exists QRTZ_FIRED_TRIGGERS;
create table QRTZ_FIRED_TRIGGERS (
    sched_name           varchar(120)    not null,
    entry_id             varchar(95)     not null,
    trigger_name         varchar(200)    not null,
    trigger_group        varchar(200)    not null,
    instance_name        varchar(200)    not null,
    fired_time           bigint(13)      not null,
    sched_time           bigint(13)      not null,
    priority             integer         not null,
    state                varchar(16)     not null,
    job_name             varchar(200)    null,
    job_group            varchar(200)    null,
    is_nonconcurrent     varchar(1)      null,
    requests_recovery    varchar(1)      null,
    primary key (sched_name,entry_id)
) engine=innodb;

-- ----------------------------
-- 9、 存储少量的有关 Scheduler 的状态信息，假如是用于集群中，可以看到其他的 Scheduler 实例
-- ---------------------------- 
drop table if exists QRTZ_SCHEDULER_STATE; 
create table QRTZ_SCHEDULER_STATE (
    sched_name           varchar(120)    not null,
    instance_name        varchar(200)    not null,
    last_checkin_time    bigint(13)      not null,
    checkin_interval     bigint(13)      not null,
    primary key (sched_name,instance_name)
) engine=innodb;

-- ----------------------------
-- 10、 存储程序的悲观锁的信息(假如使用了悲观锁)
-- ---------------------------- 
drop table if exists QRTZ_LOCKS;
create table QRTZ_LOCKS (
    sched_name           varchar(120)    not null,
    lock_name            varchar(40)     not null,
    primary key (sched_name,lock_name)
) engine=innodb;

drop table if exists QRTZ_SIMPROP_TRIGGERS;
create table QRTZ_SIMPROP_TRIGGERS (
    sched_name           varchar(120)    not null,
    trigger_name         varchar(200)    not null,
    trigger_group        varchar(200)    not null,
    str_prop_1           varchar(512)    null,
    str_prop_2           varchar(512)    null,
    str_prop_3           varchar(512)    null,
    int_prop_1           int             null,
    int_prop_2           int             null,
    long_prop_1          bigint          null,
    long_prop_2          bigint          null,
    dec_prop_1           numeric(13,4)   null,
    dec_prop_2           numeric(13,4)   null,
    bool_prop_1          varchar(1)      null,
    bool_prop_2          varchar(1)      null,
    primary key (sched_name,trigger_name,trigger_group),
    foreign key (sched_name,trigger_name,trigger_group) references QRTZ_TRIGGERS(sched_name,trigger_name,trigger_group)
) engine=innodb;

commit;


-- ----------------------------
-- 系统管理等相关表SQL
-- ----------------------------
-- ----------------------------
-- 1、部门表
-- ----------------------------
drop table if exists sys_dept;
create table sys_dept (
  dept_id           bigint(20)      not null auto_increment    comment '部门id',
  parent_id         bigint(20)      default 0                  comment '父部门id',
  ancestors         varchar(50)     default ''                 comment '祖级列表',
  dept_code         varchar(50)     default ''                 comment '部门编码',
  dept_name         varchar(30)     default ''                 comment '部门名称',
  order_num         int(4)          default 0                  comment '显示顺序',
  leader            varchar(20)     default null               comment '负责人',
  phone             varchar(11)     default null               comment '联系电话',
  email             varchar(50)     default null               comment '邮箱',
  status            char(1)         default '0'                comment '部门状态（0正常 1停用）',
  del_flag          char(1)         default '0'                comment '删除标志（0代表存在 2代表删除）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time 	    datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  primary key (dept_id)
) engine=innodb auto_increment=200 comment = '部门表';

-- ----------------------------
-- 初始化-部门表数据
-- ----------------------------
insert into sys_dept values(100,  0,   '0',     'eyecool',  '眼神科技',   0, '眼神', '15888888888', 'eyecool@eyecool.cn', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values( 99,  100, '0,100', 'default',  '默认部门',   0, '眼神', '15666666666', 'default@eyecool.cn', '0', '0', 'admin', sysdate(), '', null);

-- ----------------------------
-- 2、用户信息表
-- ----------------------------
drop table if exists sys_user;
create table sys_user (
  user_id           bigint(20)      not null auto_increment    comment '用户ID',
  dept_id           bigint(20)      default null               comment '部门ID',
  user_name         varchar(30)     not null                   comment '用户账号',
  nick_name         varchar(30)     default ''                 comment '用户昵称',
  user_type         varchar(2)      default '00'               comment '用户类型（00系统用户 01注册用户）',
  email             varchar(50)     default ''                 comment '用户邮箱',
  phonenumber       varchar(11)     default ''                 comment '手机号码',
  sex               char(1)         default '0'                comment '用户性别（0男 1女 2未知）',
  avatar            varchar(100)    default ''                 comment '头像地址',
  password          varchar(100)    default ''                 comment '密码',
  salt				varchar(20)		default '' 				   comment '盐加密',
  status            char(1)         default '0'                comment '帐号状态（0正常 1停用）',
  del_flag          char(1)         default '0'                comment '删除标志（0代表存在 2代表删除）',
  login_ip          varchar(50)     default ''                 comment '最后登录IP',
  login_date        datetime                                   comment '最后登录时间',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (user_id)
) engine=innodb auto_increment=100 comment = '用户信息表';

-- ----------------------------
-- 初始化-用户信息表数据
-- ----------------------------
insert into sys_user values(1,  99, 'admin', '眼神', '00', 'eyecool@163.com', '15888888888', '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '','0', '0', '127.0.0.1', sysdate(), 'admin', sysdate(), '', null, '管理员');
insert into sys_user values(2,  99, 'eyecool',    '眼神', '00', 'eyecool@eyecool.cn',  '15666666666', '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '', '0', '0', '127.0.0.1', sysdate(), 'admin', sysdate(), '', null, '测试员');


-- ----------------------------
-- 3、岗位信息表
-- ----------------------------
drop table if exists sys_post;
create table sys_post
(
  post_id       bigint(20)      not null auto_increment    comment '岗位ID',
  post_code     varchar(64)     not null                   comment '岗位编码',
  post_name     varchar(50)     not null                   comment '岗位名称',
  post_sort     int(4)          not null                   comment '显示顺序',
  status        char(1)         not null                   comment '状态（0正常 1停用）',
  create_by     varchar(64)     default ''                 comment '创建者',
  create_time   datetime                                   comment '创建时间',
  update_by     varchar(64)     default ''			       comment '更新者',
  update_time   datetime                                   comment '更新时间',
  remark        varchar(500)    default null               comment '备注',
  primary key (post_id)
) engine=innodb comment = '岗位信息表';

-- ----------------------------
-- 初始化-岗位信息表数据
-- ----------------------------
insert into sys_post values(1, 'ceo',  '董事长',    1, '0', 'admin', sysdate(), '', null, '');
insert into sys_post values(2, 'se',   '项目经理',  2, '0', 'admin', sysdate(), '', null, '');
insert into sys_post values(3, 'hr',   '人力资源',  3, '0', 'admin', sysdate(), '', null, '');
insert into sys_post values(4, 'user', '普通员工',  4, '0', 'admin', sysdate(), '', null, '');


-- ----------------------------
-- 4、角色信息表
-- ----------------------------
drop table if exists sys_role;
create table sys_role (
  role_id              bigint(20)      not null auto_increment    comment '角色ID',
  role_name            varchar(30)     not null                   comment '角色名称',
  role_key             varchar(100)    not null                   comment '角色权限字符串',
  role_sort            int(4)          not null                   comment '显示顺序',
  data_scope           char(1)         default '1'                comment '数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）',
  menu_check_strictly  tinyint(1)      default 1                  comment '菜单树选择项是否关联显示',
  dept_check_strictly  tinyint(1)      default 1                  comment '部门树选择项是否关联显示',
  status               char(1)         not null                   comment '角色状态（0正常 1停用）',
  del_flag             char(1)         default '0'                comment '删除标志（0代表存在 2代表删除）',
  create_by            varchar(64)     default ''                 comment '创建者',
  create_time          datetime                                   comment '创建时间',
  update_by            varchar(64)     default ''                 comment '更新者',
  update_time          datetime                                   comment '更新时间',
  remark               varchar(500)    default null               comment '备注',
  primary key (role_id)
) engine=innodb auto_increment=100 comment = '角色信息表';

-- ----------------------------
-- 初始化-角色信息表数据
-- ----------------------------
insert into sys_role values('1', '超级管理员',  'admin',  1, 1, 1, 1, '0', '0', 'admin', sysdate(), '', null, '超级管理员');
insert into sys_role values('2', '普通角色',    'common', 2, 2, 1, 1, '0', '0', 'admin', sysdate(), '', null, '普通角色');

-- ----------------------------
-- 5、菜单权限表
-- ----------------------------
drop table if exists sys_menu;
create table sys_menu (
  menu_id           bigint(20)      not null auto_increment    comment '菜单ID',
  menu_name         varchar(50)     not null                   comment '菜单名称',
  parent_id         bigint(20)      default 0                  comment '父菜单ID',
  order_num         int(4)          default 0                  comment '显示顺序',
  path              varchar(200)    default ''                 comment '路由地址',
  component         varchar(255)    default null               comment '组件路径',
  is_frame          int(1)          default 1                  comment '是否为外链（0是 1否）',
  is_cache          int(1)          default 0                  comment '是否缓存（0缓存 1不缓存）',
  menu_type         char(1)         default ''                 comment '菜单类型（M目录 C菜单 F按钮）',
  visible           char(1)         default 0                  comment '菜单状态（0显示 1隐藏）',
  status            char(1)         default 0                  comment '菜单状态（0正常 1停用）',
  perms             varchar(100)    default null               comment '权限标识',
  icon              varchar(100)    default '#'                comment '菜单图标',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default ''                 comment '备注',
  primary key (menu_id)
) engine=innodb auto_increment=2000 comment = '菜单权限表';

-- ----------------------------
-- 初始化-菜单信息表数据
-- ----------------------------
-- 一级菜单
insert into sys_menu values('1', '系统管理', '0', '1', 'system',           null,   1, 0, 'M', '0', '0', '', 'sysmgr',   'admin', sysdate(), '', null, '系统管理目录');
insert into sys_menu values('2', '系统监控', '0', '2', 'monitor',          null,   1, 0, 'M', '0', '0', '', 'sysmonitor',  'admin', sysdate(), '', null, '系统监控目录');
insert into sys_menu values('3', '系统工具', '0', '3', 'tool',             null,   1, 0, 'M', '0', '0', '', 'systool',     'admin', sysdate(), '', null, '系统工具目录');
-- 二级菜单
insert into sys_menu values('100',  '用户管理', '1',   '1', 'user',       'system/user/index',        1, 0, 'C', '0', '0', 'system:user:list',        '#',          'admin', sysdate(), '', null, '用户管理菜单');
insert into sys_menu values('101',  '角色管理', '1',   '2', 'role',       'system/role/index',        1, 0, 'C', '0', '0', 'system:role:list',        '#',       'admin', sysdate(), '', null, '角色管理菜单');
insert into sys_menu values('102',  '菜单管理', '1',   '3', 'menu',       'system/menu/index',        1, 0, 'C', '0', '0', 'system:menu:list',        '#',    'admin', sysdate(), '', null, '菜单管理菜单');
insert into sys_menu values('103',  '部门管理', '1',   '4', 'dept',       'system/dept/index',        1, 0, 'C', '0', '0', 'system:dept:list',        '#',          'admin', sysdate(), '', null, '部门管理菜单');
insert into sys_menu values('104',  '岗位管理', '1',   '5', 'post',       'system/post/index',        1, 0, 'C', '0', '0', 'system:post:list',        '#',          'admin', sysdate(), '', null, '岗位管理菜单');
insert into sys_menu values('105',  '字典管理', '1',   '6', 'dict',       'system/dict/index',        1, 0, 'C', '0', '0', 'system:dict:list',        '#',          'admin', sysdate(), '', null, '字典管理菜单');
insert into sys_menu values('106',  '参数设置', '1',   '7', 'config',     'system/config/index',      1, 0, 'C', '0', '0', 'system:config:list',      '#',          'admin', sysdate(), '', null, '参数设置菜单');
insert into sys_menu values('107',  '通知公告', '1',   '8', 'notice',     'system/notice/index',      1, 0, 'C', '0', '0', 'system:notice:list',      '#',       'admin', sysdate(), '', null, '通知公告菜单');
insert into sys_menu values('108',  '日志管理', '1',   '9', 'log',        'system/log/index',         1, 0, 'M', '0', '0', '',                        '#',           'admin', sysdate(), '', null, '日志管理菜单');
insert into sys_menu values('109',  '在线用户', '2',   '1', 'online',     'monitor/online/index',     1, 0, 'C', '0', '0', 'monitor:online:list',     '#',        'admin', sysdate(), '', null, '在线用户菜单');
insert into sys_menu values('110',  '定时任务', '2',   '2', 'job',        'monitor/job/index',        1, 0, 'C', '0', '0', 'monitor:job:list',        '#',           'admin', sysdate(), '', null, '定时任务菜单');
insert into sys_menu values('111',  '数据监控', '2',   '3', 'druid',      'monitor/druid/index',      1, 0, 'C', '0', '0', 'monitor:druid:list',      '#',         'admin', sysdate(), '', null, '数据监控菜单');
insert into sys_menu values('112',  '服务监控', '2',   '4', 'server',     'monitor/server/index',     1, 0, 'C', '0', '0', 'monitor:server:list',     '#',        'admin', sysdate(), '', null, '服务监控菜单');
insert into sys_menu values('113',  '表单构建', '3',   '1', 'build',      'tool/build/index',         1, 0, 'C', '0', '0', 'tool:build:list',         '#',         'admin', sysdate(), '', null, '表单构建菜单');
insert into sys_menu values('114',  '代码生成', '3',   '2', 'gen',        'tool/gen/index',           1, 0, 'C', '0', '0', 'tool:gen:list',           '#',          'admin', sysdate(), '', null, '代码生成菜单');
insert into sys_menu values('115',  '系统接口', '3',   '3', 'swagger',    'tool/swagger/index',       1, 0, 'C', '0', '0', 'tool:swagger:list',       '#',       'admin', sysdate(), '', null, '系统接口菜单');
-- 三级菜单
insert into sys_menu values('500',  '操作日志', '108', '1', 'operlog',    'monitor/operlog/index',    1, 0, 'C', '0', '0', 'monitor:operlog:list',    '#',          'admin', sysdate(), '', null, '操作日志菜单');
insert into sys_menu values('501',  '登录日志', '108', '2', 'logininfor', 'monitor/logininfor/index', 1, 0, 'C', '0', '0', 'monitor:logininfor:list', '#',    'admin', sysdate(), '', null, '登录日志菜单');
-- 用户管理按钮
insert into sys_menu values('1001', '用户查询', '100', '1',  '', '', 1, 0, 'F', '0', '0', 'system:user:query',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1002', '用户新增', '100', '2',  '', '', 1, 0, 'F', '0', '0', 'system:user:add',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1003', '用户修改', '100', '3',  '', '', 1, 0, 'F', '0', '0', 'system:user:edit',           '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1004', '用户删除', '100', '4',  '', '', 1, 0, 'F', '0', '0', 'system:user:remove',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1005', '用户导出', '100', '5',  '', '', 1, 0, 'F', '0', '0', 'system:user:export',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1006', '用户导入', '100', '6',  '', '', 1, 0, 'F', '0', '0', 'system:user:import',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1007', '重置密码', '100', '7',  '', '', 1, 0, 'F', '0', '0', 'system:user:resetPwd',       '#', 'admin', sysdate(), '', null, '');
-- 角色管理按钮
insert into sys_menu values('1008', '角色查询', '101', '1',  '', '', 1, 0, 'F', '0', '0', 'system:role:query',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1009', '角色新增', '101', '2',  '', '', 1, 0, 'F', '0', '0', 'system:role:add',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1010', '角色修改', '101', '3',  '', '', 1, 0, 'F', '0', '0', 'system:role:edit',           '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1011', '角色删除', '101', '4',  '', '', 1, 0, 'F', '0', '0', 'system:role:remove',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1012', '角色导出', '101', '5',  '', '', 1, 0, 'F', '0', '0', 'system:role:export',         '#', 'admin', sysdate(), '', null, '');
-- 菜单管理按钮
insert into sys_menu values('1013', '菜单查询', '102', '1',  '', '', 1, 0, 'F', '0', '0', 'system:menu:query',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1014', '菜单新增', '102', '2',  '', '', 1, 0, 'F', '0', '0', 'system:menu:add',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1015', '菜单修改', '102', '3',  '', '', 1, 0, 'F', '0', '0', 'system:menu:edit',           '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1016', '菜单删除', '102', '4',  '', '', 1, 0, 'F', '0', '0', 'system:menu:remove',         '#', 'admin', sysdate(), '', null, '');
-- 部门管理按钮
insert into sys_menu values('1017', '部门查询', '103', '1',  '', '', 1, 0, 'F', '0', '0', 'system:dept:query',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1018', '部门新增', '103', '2',  '', '', 1, 0, 'F', '0', '0', 'system:dept:add',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1019', '部门修改', '103', '3',  '', '', 1, 0, 'F', '0', '0', 'system:dept:edit',           '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1020', '部门删除', '103', '4',  '', '', 1, 0, 'F', '0', '0', 'system:dept:remove',         '#', 'admin', sysdate(), '', null, '');
-- 岗位管理按钮
insert into sys_menu values('1021', '岗位查询', '104', '1',  '', '', 1, 0, 'F', '0', '0', 'system:post:query',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1022', '岗位新增', '104', '2',  '', '', 1, 0, 'F', '0', '0', 'system:post:add',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1023', '岗位修改', '104', '3',  '', '', 1, 0, 'F', '0', '0', 'system:post:edit',           '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1024', '岗位删除', '104', '4',  '', '', 1, 0, 'F', '0', '0', 'system:post:remove',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1025', '岗位导出', '104', '5',  '', '', 1, 0, 'F', '0', '0', 'system:post:export',         '#', 'admin', sysdate(), '', null, '');
-- 字典管理按钮
insert into sys_menu values('1026', '字典查询', '105', '1', '#', '', 1, 0, 'F', '0', '0', 'system:dict:query',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1027', '字典新增', '105', '2', '#', '', 1, 0, 'F', '0', '0', 'system:dict:add',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1028', '字典修改', '105', '3', '#', '', 1, 0, 'F', '0', '0', 'system:dict:edit',           '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1029', '字典删除', '105', '4', '#', '', 1, 0, 'F', '0', '0', 'system:dict:remove',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1030', '字典导出', '105', '5', '#', '', 1, 0, 'F', '0', '0', 'system:dict:export',         '#', 'admin', sysdate(), '', null, '');
-- 参数设置按钮
insert into sys_menu values('1031', '参数查询', '106', '1', '#', '', 1, 0, 'F', '0', '0', 'system:config:query',        '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1032', '参数新增', '106', '2', '#', '', 1, 0, 'F', '0', '0', 'system:config:add',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1033', '参数修改', '106', '3', '#', '', 1, 0, 'F', '0', '0', 'system:config:edit',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1034', '参数删除', '106', '4', '#', '', 1, 0, 'F', '0', '0', 'system:config:remove',       '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1035', '参数导出', '106', '5', '#', '', 1, 0, 'F', '0', '0', 'system:config:export',       '#', 'admin', sysdate(), '', null, '');
-- 通知公告按钮
insert into sys_menu values('1036', '公告查询', '107', '1', '#', '', 1, 0, 'F', '0', '0', 'system:notice:query',        '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1037', '公告新增', '107', '2', '#', '', 1, 0, 'F', '0', '0', 'system:notice:add',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1038', '公告修改', '107', '3', '#', '', 1, 0, 'F', '0', '0', 'system:notice:edit',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1039', '公告删除', '107', '4', '#', '', 1, 0, 'F', '0', '0', 'system:notice:remove',       '#', 'admin', sysdate(), '', null, '');
-- 操作日志按钮
insert into sys_menu values('1040', '操作查询', '500', '1', '#', '', 1, 0, 'F', '0', '0', 'monitor:operlog:query',      '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1041', '操作删除', '500', '2', '#', '', 1, 0, 'F', '0', '0', 'monitor:operlog:remove',     '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1042', '日志导出', '500', '4', '#', '', 1, 0, 'F', '0', '0', 'monitor:operlog:export',     '#', 'admin', sysdate(), '', null, '');
-- 登录日志按钮
insert into sys_menu values('1043', '登录查询', '501', '1', '#', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:query',   '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1044', '登录删除', '501', '2', '#', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:remove',  '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1045', '日志导出', '501', '3', '#', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:export',  '#', 'admin', sysdate(), '', null, '');
-- 在线用户按钮
insert into sys_menu values('1046', '在线查询', '109', '1', '#', '', 1, 0, 'F', '0', '0', 'monitor:online:query',       '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1047', '批量强退', '109', '2', '#', '', 1, 0, 'F', '0', '0', 'monitor:online:batchLogout', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1048', '单条强退', '109', '3', '#', '', 1, 0, 'F', '0', '0', 'monitor:online:forceLogout', '#', 'admin', sysdate(), '', null, '');
-- 定时任务按钮
insert into sys_menu values('1049', '任务查询', '110', '1', '#', '', 1, 0, 'F', '0', '0', 'monitor:job:query',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1050', '任务新增', '110', '2', '#', '', 1, 0, 'F', '0', '0', 'monitor:job:add',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1051', '任务修改', '110', '3', '#', '', 1, 0, 'F', '0', '0', 'monitor:job:edit',           '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1052', '任务删除', '110', '4', '#', '', 1, 0, 'F', '0', '0', 'monitor:job:remove',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1053', '状态修改', '110', '5', '#', '', 1, 0, 'F', '0', '0', 'monitor:job:changeStatus',   '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1054', '任务导出', '110', '7', '#', '', 1, 0, 'F', '0', '0', 'monitor:job:export',         '#', 'admin', sysdate(), '', null, '');
-- 代码生成按钮
insert into sys_menu values('1055', '生成查询', '114', '1', '#', '', 1, 0, 'F', '0', '0', 'tool:gen:query',             '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1056', '生成修改', '114', '2', '#', '', 1, 0, 'F', '0', '0', 'tool:gen:edit',              '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1057', '生成删除', '114', '3', '#', '', 1, 0, 'F', '0', '0', 'tool:gen:remove',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1058', '导入代码', '114', '2', '#', '', 1, 0, 'F', '0', '0', 'tool:gen:import',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1059', '预览代码', '114', '4', '#', '', 1, 0, 'F', '0', '0', 'tool:gen:preview',           '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1060', '生成代码', '114', '5', '#', '', 1, 0, 'F', '0', '0', 'tool:gen:code',              '#', 'admin', sysdate(), '', null, '');


-- ----------------------------
-- 6、用户和角色关联表  用户N-1角色
-- ----------------------------
drop table if exists sys_user_role;
create table sys_user_role (
  user_id   bigint(20) not null comment '用户ID',
  role_id   bigint(20) not null comment '角色ID',
  primary key(user_id, role_id)
) engine=innodb comment = '用户和角色关联表';

-- ----------------------------
-- 初始化-用户和角色关联表数据
-- ----------------------------
insert into sys_user_role values ('1', '1');
insert into sys_user_role values ('2', '2');


-- ----------------------------
-- 7、角色和菜单关联表  角色1-N菜单
-- ----------------------------
drop table if exists sys_role_menu;
create table sys_role_menu (
  role_id   bigint(20) not null comment '角色ID',
  menu_id   bigint(20) not null comment '菜单ID',
  primary key(role_id, menu_id)
) engine=innodb comment = '角色和菜单关联表';

-- ----------------------------
-- 初始化-角色和菜单关联表数据
-- ----------------------------
insert into sys_role_menu values ('2', '1');
insert into sys_role_menu values ('2', '2');
insert into sys_role_menu values ('2', '3');
insert into sys_role_menu values ('2', '4');
insert into sys_role_menu values ('2', '100');
insert into sys_role_menu values ('2', '101');
insert into sys_role_menu values ('2', '102');
insert into sys_role_menu values ('2', '103');
insert into sys_role_menu values ('2', '104');
insert into sys_role_menu values ('2', '105');
insert into sys_role_menu values ('2', '106');
insert into sys_role_menu values ('2', '107');
insert into sys_role_menu values ('2', '108');
insert into sys_role_menu values ('2', '109');
insert into sys_role_menu values ('2', '110');
insert into sys_role_menu values ('2', '111');
insert into sys_role_menu values ('2', '112');
insert into sys_role_menu values ('2', '113');
insert into sys_role_menu values ('2', '114');
insert into sys_role_menu values ('2', '115');
insert into sys_role_menu values ('2', '500');
insert into sys_role_menu values ('2', '501');
insert into sys_role_menu values ('2', '1000');
insert into sys_role_menu values ('2', '1001');
insert into sys_role_menu values ('2', '1002');
insert into sys_role_menu values ('2', '1003');
insert into sys_role_menu values ('2', '1004');
insert into sys_role_menu values ('2', '1005');
insert into sys_role_menu values ('2', '1006');
insert into sys_role_menu values ('2', '1007');
insert into sys_role_menu values ('2', '1008');
insert into sys_role_menu values ('2', '1009');
insert into sys_role_menu values ('2', '1010');
insert into sys_role_menu values ('2', '1011');
insert into sys_role_menu values ('2', '1012');
insert into sys_role_menu values ('2', '1013');
insert into sys_role_menu values ('2', '1014');
insert into sys_role_menu values ('2', '1015');
insert into sys_role_menu values ('2', '1016');
insert into sys_role_menu values ('2', '1017');
insert into sys_role_menu values ('2', '1018');
insert into sys_role_menu values ('2', '1019');
insert into sys_role_menu values ('2', '1020');
insert into sys_role_menu values ('2', '1021');
insert into sys_role_menu values ('2', '1022');
insert into sys_role_menu values ('2', '1023');
insert into sys_role_menu values ('2', '1024');
insert into sys_role_menu values ('2', '1025');
insert into sys_role_menu values ('2', '1026');
insert into sys_role_menu values ('2', '1027');
insert into sys_role_menu values ('2', '1028');
insert into sys_role_menu values ('2', '1029');
insert into sys_role_menu values ('2', '1030');
insert into sys_role_menu values ('2', '1031');
insert into sys_role_menu values ('2', '1032');
insert into sys_role_menu values ('2', '1033');
insert into sys_role_menu values ('2', '1034');
insert into sys_role_menu values ('2', '1035');
insert into sys_role_menu values ('2', '1036');
insert into sys_role_menu values ('2', '1037');
insert into sys_role_menu values ('2', '1038');
insert into sys_role_menu values ('2', '1039');
insert into sys_role_menu values ('2', '1040');
insert into sys_role_menu values ('2', '1041');
insert into sys_role_menu values ('2', '1042');
insert into sys_role_menu values ('2', '1043');
insert into sys_role_menu values ('2', '1044');
insert into sys_role_menu values ('2', '1045');
insert into sys_role_menu values ('2', '1046');
insert into sys_role_menu values ('2', '1047');
insert into sys_role_menu values ('2', '1048');
insert into sys_role_menu values ('2', '1049');
insert into sys_role_menu values ('2', '1050');
insert into sys_role_menu values ('2', '1051');
insert into sys_role_menu values ('2', '1052');
insert into sys_role_menu values ('2', '1053');
insert into sys_role_menu values ('2', '1054');
insert into sys_role_menu values ('2', '1055');
insert into sys_role_menu values ('2', '1056');
insert into sys_role_menu values ('2', '1057');
insert into sys_role_menu values ('2', '1058');
insert into sys_role_menu values ('2', '1059');
insert into sys_role_menu values ('2', '1060');

-- ----------------------------
-- 8、角色和部门关联表  角色1-N部门
-- ----------------------------
drop table if exists sys_role_dept;
create table sys_role_dept (
  role_id   bigint(20) not null comment '角色ID',
  dept_id   bigint(20) not null comment '部门ID',
  primary key(role_id, dept_id)
) engine=innodb comment = '角色和部门关联表';

-- ----------------------------
-- 初始化-角色和部门关联表数据
-- ----------------------------
insert into sys_role_dept values ('2', '99');

-- ----------------------------
-- 9、用户与岗位关联表  用户1-N岗位
-- ----------------------------
drop table if exists sys_user_post;
create table sys_user_post
(
  user_id   bigint(20) not null comment '用户ID',
  post_id   bigint(20) not null comment '岗位ID',
  primary key (user_id, post_id)
) engine=innodb comment = '用户与岗位关联表';

-- ----------------------------
-- 初始化-用户与岗位关联表数据
-- ----------------------------
insert into sys_user_post values ('1', '1');
insert into sys_user_post values ('2', '2');

-- ----------------------------
-- 10、操作日志记录
-- ----------------------------
drop table if exists sys_oper_log;
create table sys_oper_log (
  oper_id           bigint(20)      not null auto_increment    comment '日志主键',
  title             varchar(50)     default ''                 comment '模块标题',
  business_type     int(2)          default 0                  comment '业务类型（0其它 1新增 2修改 3删除）',
  method            varchar(100)    default ''                 comment '方法名称',
  request_method    varchar(10)     default ''                 comment '请求方式',
  operator_type     int(1)          default 0                  comment '操作类别（0其它 1后台用户 2手机端用户）',
  oper_name         varchar(50)     default ''                 comment '操作人员',
  dept_name         varchar(50)     default ''                 comment '部门名称',
  oper_url          varchar(255)    default ''                 comment '请求URL',
  oper_ip           varchar(50)     default ''                 comment '主机地址',
  oper_location     varchar(255)    default ''                 comment '操作地点',
  oper_param        varchar(2000)   default ''                 comment '请求参数',
  json_result       varchar(2000)   default ''                 comment '返回参数',
  status            int(1)          default 0                  comment '操作状态（0正常 1异常）',
  error_msg         varchar(2000)   default ''                 comment '错误消息',
  oper_time         datetime                                   comment '操作时间',
  primary key (oper_id)
) engine=innodb auto_increment=100 comment = '操作日志记录';


-- ----------------------------
-- 11、字典类型表
-- ----------------------------
drop table if exists sys_dict_type;
create table sys_dict_type
(
  dict_id          bigint(20)      not null auto_increment    comment '字典主键',
  dict_name        varchar(100)    default ''                 comment '字典名称',
  dict_type        varchar(100)    default ''                 comment '字典类型',
  status           char(1)         default '0'                comment '状态（0正常 1停用）',
  create_by        varchar(64)     default ''                 comment '创建者',
  create_time      datetime                                   comment '创建时间',
  update_by        varchar(64)     default ''                 comment '更新者',
  update_time      datetime                                   comment '更新时间',
  remark           varchar(500)    default null               comment '备注',
  primary key (dict_id),
  unique (dict_type)
) engine=innodb auto_increment=100 comment = '字典类型表';

insert into sys_dict_type values(1,  '用户性别', 'sys_user_sex',        '0', 'admin', sysdate(), '', null, '用户性别列表');
insert into sys_dict_type values(2,  '菜单状态', 'sys_show_hide',       '0', 'admin', sysdate(), '', null, '菜单状态列表');
insert into sys_dict_type values(3,  '系统开关', 'sys_normal_disable',  '0', 'admin', sysdate(), '', null, '系统开关列表');
insert into sys_dict_type values(4,  '任务状态', 'sys_job_status',      '0', 'admin', sysdate(), '', null, '任务状态列表');
insert into sys_dict_type values(5,  '任务分组', 'sys_job_group',       '0', 'admin', sysdate(), '', null, '任务分组列表');
insert into sys_dict_type values(6,  '系统是否', 'sys_yes_no',          '0', 'admin', sysdate(), '', null, '系统是否列表');
insert into sys_dict_type values(7,  '通知类型', 'sys_notice_type',     '0', 'admin', sysdate(), '', null, '通知类型列表');
insert into sys_dict_type values(8,  '通知状态', 'sys_notice_status',   '0', 'admin', sysdate(), '', null, '通知状态列表');
insert into sys_dict_type values(9,  '操作类型', 'sys_oper_type',       '0', 'admin', sysdate(), '', null, '操作类型列表');
insert into sys_dict_type values(10, '系统状态', 'sys_common_status',   '0', 'admin', sysdate(), '', null, '登录状态列表');

-- ----------------------------
-- 12、字典数据表
-- ----------------------------
drop table if exists sys_dict_data;
create table sys_dict_data
(
  dict_code        bigint(20)      not null auto_increment    comment '字典编码',
  dict_sort        int(4)          default 0                  comment '字典排序',
  dict_label       varchar(100)    default ''                 comment '字典标签',
  dict_value       varchar(100)    default ''                 comment '字典键值',
  dict_type        varchar(100)    default ''                 comment '字典类型',
  css_class        varchar(100)    default null               comment '样式属性（其他样式扩展）',
  list_class       varchar(100)    default null               comment '表格回显样式',
  is_default       char(1)         default 'N'                comment '是否默认（Y是 N否）',
  status           char(1)         default '0'                comment '状态（0正常 1停用）',
  create_by        varchar(64)     default ''                 comment '创建者',
  create_time      datetime                                   comment '创建时间',
  update_by        varchar(64)     default ''                 comment '更新者',
  update_time      datetime                                   comment '更新时间',
  remark           varchar(500)    default null               comment '备注',
  primary key (dict_code)
) engine=innodb auto_increment=100 comment = '字典数据表';

insert into sys_dict_data values(1,  1,  '男',       '0',       'sys_user_sex',        '',   '',        'Y', '0', 'admin', sysdate(), '', null, '性别男');
insert into sys_dict_data values(2,  2,  '女',       '1',       'sys_user_sex',        '',   '',        'N', '0', 'admin', sysdate(), '', null, '性别女');
insert into sys_dict_data values(3,  3,  '未知',     '2',       'sys_user_sex',        '',   '',        'N', '0', 'admin', sysdate(), '', null, '性别未知');
insert into sys_dict_data values(4,  1,  '显示',     '0',       'sys_show_hide',       '',   'primary', 'Y', '0', 'admin', sysdate(), '', null, '显示菜单');
insert into sys_dict_data values(5,  2,  '隐藏',     '1',       'sys_show_hide',       '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '隐藏菜单');
insert into sys_dict_data values(6,  1,  '正常',     '0',       'sys_normal_disable',  '',   'primary', 'Y', '0', 'admin', sysdate(), '', null, '正常状态');
insert into sys_dict_data values(7,  2,  '停用',     '1',       'sys_normal_disable',  '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '停用状态');
insert into sys_dict_data values(8,  1,  '正常',     '0',       'sys_job_status',      '',   'primary', 'Y', '0', 'admin', sysdate(), '', null, '正常状态');
insert into sys_dict_data values(9,  2,  '暂停',     '1',       'sys_job_status',      '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '停用状态');
insert into sys_dict_data values(10, 1,  '默认',     'DEFAULT', 'sys_job_group',       '',   '',        'Y', '0', 'admin', sysdate(), '', null, '默认分组');
insert into sys_dict_data values(11, 2,  '系统',     'SYSTEM',  'sys_job_group',       '',   '',        'N', '0', 'admin', sysdate(), '', null, '系统分组');
insert into sys_dict_data values(12, 1,  '是',       'Y',       'sys_yes_no',          '',   'primary', 'Y', '0', 'admin', sysdate(), '', null, '系统默认是');
insert into sys_dict_data values(13, 2,  '否',       'N',       'sys_yes_no',          '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '系统默认否');
insert into sys_dict_data values(14, 1,  '通知',     '1',       'sys_notice_type',     '',   'warning', 'Y', '0', 'admin', sysdate(), '', null, '通知');
insert into sys_dict_data values(15, 2,  '公告',     '2',       'sys_notice_type',     '',   'success', 'N', '0', 'admin', sysdate(), '', null, '公告');
insert into sys_dict_data values(16, 1,  '正常',     '0',       'sys_notice_status',   '',   'primary', 'Y', '0', 'admin', sysdate(), '', null, '正常状态');
insert into sys_dict_data values(17, 2,  '关闭',     '1',       'sys_notice_status',   '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '关闭状态');
insert into sys_dict_data values(18, 1,  '新增',     '1',       'sys_oper_type',       '',   'info',    'N', '0', 'admin', sysdate(), '', null, '新增操作');
insert into sys_dict_data values(19, 2,  '修改',     '2',       'sys_oper_type',       '',   'info',    'N', '0', 'admin', sysdate(), '', null, '修改操作');
insert into sys_dict_data values(20, 3,  '删除',     '3',       'sys_oper_type',       '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '删除操作');
insert into sys_dict_data values(21, 4,  '授权',     '4',       'sys_oper_type',       '',   'primary', 'N', '0', 'admin', sysdate(), '', null, '授权操作');
insert into sys_dict_data values(22, 5,  '导出',     '5',       'sys_oper_type',       '',   'warning', 'N', '0', 'admin', sysdate(), '', null, '导出操作');
insert into sys_dict_data values(23, 6,  '导入',     '6',       'sys_oper_type',       '',   'warning', 'N', '0', 'admin', sysdate(), '', null, '导入操作');
insert into sys_dict_data values(24, 7,  '强退',     '7',       'sys_oper_type',       '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '强退操作');
insert into sys_dict_data values(25, 8,  '生成代码', '8',       'sys_oper_type',       '',   'warning', 'N', '0', 'admin', sysdate(), '', null, '生成操作');
insert into sys_dict_data values(26, 9,  '清空数据', '9',       'sys_oper_type',       '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '清空操作');
insert into sys_dict_data values(27, 1,  '成功',     '0',       'sys_common_status',   '',   'primary', 'N', '0', 'admin', sysdate(), '', null, '正常状态');
insert into sys_dict_data values(28, 2,  '失败',     '1',       'sys_common_status',   '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '停用状态');


-- ----------------------------
-- 13、参数配置表
-- ----------------------------
drop table if exists sys_config;
create table sys_config (
  config_id         int(5)          not null auto_increment    comment '参数主键',
  config_name       varchar(100)    default ''                 comment '参数名称',
  config_key        varchar(100)    default ''                 comment '参数键名',
  config_value      varchar(500)    default ''                 comment '参数键值',
  config_type       char(1)         default 'N'                comment '系统内置（Y是 N否）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (config_id)
) engine=innodb auto_increment=100 comment = '参数配置表';

insert into sys_config values(1, '主框架页-默认皮肤样式名称', 'sys.index.skinName',     'skin-blue',     'Y', 'admin', sysdate(), '', null, '蓝色 skin-blue、绿色 skin-green、紫色 skin-purple、红色 skin-red、黄色 skin-yellow' );
insert into sys_config values(2, '用户管理-账号初始密码',     'sys.user.initPassword',  '123456',        'Y', 'admin', sysdate(), '', null, '初始化密码 123456' );
insert into sys_config values(3, '主框架页-侧边栏主题',       'sys.index.sideTheme',    'theme-dark',    'Y', 'admin', sysdate(), '', null, '深色主题theme-dark，浅色主题theme-light' );


-- ----------------------------
-- 14、系统访问记录
-- ----------------------------
drop table if exists sys_logininfor;
create table sys_logininfor (
  info_id        bigint(20)     not null auto_increment   comment '访问ID',
  user_name      varchar(50)    default ''                comment '用户账号',
  ipaddr         varchar(50)    default ''                comment '登录IP地址',
  login_location varchar(255)   default ''                comment '登录地点',
  browser        varchar(50)    default ''                comment '浏览器类型',
  os             varchar(50)    default ''                comment '操作系统',
  status         char(1)        default '0'               comment '登录状态（0成功 1失败）',
  msg            varchar(255)   default ''                comment '提示消息',
  login_time     datetime                                 comment '访问时间',
  primary key (info_id)
) engine=innodb auto_increment=100 comment = '系统访问记录';


-- ----------------------------
-- 15、定时任务调度表
-- ----------------------------
drop table if exists sys_job;
create table sys_job (
  job_id              bigint(20)    not null auto_increment    comment '任务ID',
  job_name            varchar(64)   default ''                 comment '任务名称',
  job_group           varchar(64)   default 'DEFAULT'          comment '任务组名',
  invoke_target       varchar(500)  not null                   comment '调用目标字符串',
  cron_expression     varchar(255)  default ''                 comment 'cron执行表达式',
  misfire_policy      varchar(20)   default '3'                comment '计划执行错误策略（1立即执行 2执行一次 3放弃执行）',
  concurrent          char(1)       default '1'                comment '是否并发执行（0允许 1禁止）',
  status              char(1)       default '0'                comment '状态（0正常 1暂停）',
  create_by           varchar(64)   default ''                 comment '创建者',
  create_time         datetime                                 comment '创建时间',
  update_by           varchar(64)   default ''                 comment '更新者',
  update_time         datetime                                 comment '更新时间',
  remark              varchar(500)  default ''                 comment '备注信息',
  primary key (job_id, job_name, job_group)
) engine=innodb auto_increment=100 comment = '定时任务调度表';

-- ----------------------------
-- 16、定时任务调度日志表
-- ----------------------------
drop table if exists sys_job_log;
create table sys_job_log (
  job_log_id          bigint(20)     not null auto_increment    comment '任务日志ID',
  job_name            varchar(64)    not null                   comment '任务名称',
  job_group           varchar(64)    not null                   comment '任务组名',
  invoke_target       varchar(500)   not null                   comment '调用目标字符串',
  job_message         varchar(500)                              comment '日志信息',
  status              char(1)        default '0'                comment '执行状态（0正常 1失败）',
  exception_info      varchar(2000)  default ''                 comment '异常信息',
  create_time         datetime                                  comment '创建时间',
  primary key (job_log_id)
) engine=innodb comment = '定时任务调度日志表';


-- ----------------------------
-- 17、通知公告表
-- ----------------------------
drop table if exists sys_notice;
create table sys_notice (
  notice_id         int(4)          not null auto_increment    comment '公告ID',
  notice_title      varchar(50)     not null                   comment '公告标题',
  notice_type       char(1)         not null                   comment '公告类型（1通知 2公告）',
  notice_content    longblob        default null               comment '公告内容',
  status            char(1)         default '0'                comment '公告状态（0正常 1关闭）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(255)    default null               comment '备注',
  primary key (notice_id)
) engine=innodb auto_increment=10 comment = '通知公告表';

-- ----------------------------
-- 初始化-公告信息表数据
-- ----------------------------
insert into sys_notice values('1', '温馨提醒：2018-07-01 眼神新版本发布啦', '2', '新版本内容', '0', 'admin', sysdate(), '', null, '管理员');
insert into sys_notice values('2', '维护通知：2018-07-01 眼神系统凌晨维护', '1', '维护内容',   '0', 'admin', sysdate(), '', null, '管理员');


-- ----------------------------
-- 18、代码生成业务表
-- ----------------------------
drop table if exists gen_table;
create table gen_table (
  table_id          bigint(20)      not null auto_increment    comment '编号',
  table_name        varchar(200)    default ''                 comment '表名称',
  table_comment     varchar(500)    default ''                 comment '表描述',
  class_name        varchar(100)    default ''                 comment '实体类名称',
  tpl_category      varchar(200)    default 'crud'             comment '使用的模板（crud单表操作 tree树表操作）',
  package_name      varchar(100)                               comment '生成包路径',
  module_name       varchar(30)                                comment '生成模块名',
  business_name     varchar(30)                                comment '生成业务名',
  function_name     varchar(50)                                comment '生成功能名',
  function_author   varchar(50)                                comment '生成功能作者',
  gen_type          char(1)         default '0'                comment '生成代码方式（0zip压缩包 1自定义路径）',
  gen_path          varchar(200)    default '/'                comment '生成路径（不填默认项目路径）',
  options           varchar(1000)                              comment '其它生成选项',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time 	    datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (table_id)
) engine=innodb auto_increment=1 comment = '代码生成业务表';


-- ----------------------------
-- 19、代码生成业务表字段
-- ----------------------------
drop table if exists gen_table_column;
create table gen_table_column (
  column_id         bigint(20)      not null auto_increment    comment '编号',
  table_id          varchar(64)                                comment '归属表编号',
  column_name       varchar(200)                               comment '列名称',
  column_comment    varchar(500)                               comment '列描述',
  column_type       varchar(100)                               comment '列类型',
  java_type         varchar(500)                               comment 'JAVA类型',
  java_field        varchar(200)                               comment 'JAVA字段名',
  is_pk             char(1)                                    comment '是否主键（1是）',
  is_increment      char(1)                                    comment '是否自增（1是）',
  is_required       char(1)                                    comment '是否必填（1是）',
  is_insert         char(1)                                    comment '是否为插入字段（1是）',
  is_edit           char(1)                                    comment '是否编辑字段（1是）',
  is_list           char(1)                                    comment '是否列表字段（1是）',
  is_query          char(1)                                    comment '是否查询字段（1是）',
  query_type        varchar(200)    default 'EQ'               comment '查询方式（等于、不等于、大于、小于、范围）',
  html_type         varchar(200)                               comment '显示类型（文本框、文本域、下拉框、复选框、单选框、日期控件）',
  dict_type         varchar(200)    default ''                 comment '字典类型',
  sort              int                                        comment '排序',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time 	    datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  primary key (column_id)
) engine=innodb auto_increment=1 comment = '代码生成业务表字段';

-- ----------------------------
-- 单点登录终端信息管理
-- ----------------------------
CREATE TABLE sys_client_details (
  client_id varchar(255) NOT NULL COMMENT '终端编号',
  resource_ids varchar(255) DEFAULT NULL COMMENT '资源ID标识',
  client_secret varchar(255) NOT NULL COMMENT '终端安全码',
  scope varchar(255) NOT NULL COMMENT '终端授权范围',
  web_server_redirect_uri varchar(255) DEFAULT NULL COMMENT '服务器回调地址',
  authorities varchar(255) DEFAULT NULL COMMENT '访问资源所需权限',
  additional_information varchar(4096) DEFAULT NULL COMMENT '附加信息',
  origin_secret varchar(255) NOT NULL COMMENT '终端明文安全码',
  PRIMARY KEY (client_id)
) COMMENT='终端配置表';

-- ----------------------------
-- 单点登录终端管理菜单
-- ----------------------------
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2000, '终端配置', 1, 10, 'client', 'system/client/index', 1, 0, 'C', '0', '0', 'system:client:list', '#', 'admin', sysdate(), '', NULL, '终端配置菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2001, '终端配置查询', 2000, 1, '#', '', 1, 0, 'F', '0', '0', 'system:client:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2002, '终端配置新增', 2000, 2, '#', '', 1, 0, 'F', '0', '0', 'system:client:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2003, '终端配置修改', 2000, 3, '#', '', 1, 0, 'F', '0', '0', 'system:client:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2004, '终端配置删除', 2000, 4, '#', '', 1, 0, 'F', '0', '0', 'system:client:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2005, '终端配置导出', 2000, 5, '#', '', 1, 0, 'F', '0', '0', 'system:client:export', '#', 'admin', sysdate(), '', NULL, '');
-- ----------------------------
-- WEB终端
-- ----------------------------
INSERT INTO sys_client_details(client_id, resource_ids, client_secret, scope, web_server_redirect_uri, authorities, additional_information, origin_secret) VALUES ('web', NULL, '$2a$10$OsbchgUAxA6MLchR2L1jvejxfTYi5XPoPwQUNERNr.vcH.ivnb5HO', 'server', NULL, NULL, NULL, '123456');
INSERT INTO sys_client_details(client_id, resource_ids, client_secret, scope, web_server_redirect_uri, authorities, additional_information, origin_secret) VALUES ('app', NULL, '$2a$10$uKuoTb8Sa5ocmTxJpap1tu4vChbVifBe4uXqohCcwWJRbZvlqGsdC', 'server', NULL, NULL, NULL, '123456');

CREATE TABLE sys_tenant_role (
  id varchar(48) NOT NULL COMMENT '角色ID',
  role_name varchar(30) NOT NULL COMMENT '角色名称',
  role_key varchar(100) NOT NULL COMMENT '角色权限字符串',
  role_sort int(4) NOT NULL COMMENT '显示顺序',
  menu_check_strictly tinyint(1) DEFAULT '1' COMMENT '菜单树选择项是否关联显示',
  status char(1) NOT NULL COMMENT '角色状态（0正常 1停用）',
  del_flag char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  create_by varchar(64) DEFAULT '' COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT '' COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) COMMENT='租户角色信息表';

CREATE TABLE sys_tenant_role_menu (
  role_id varchar(48) NOT NULL COMMENT '角色ID',
  menu_id bigint(20) NOT NULL COMMENT '菜单ID',
  PRIMARY KEY (role_id,menu_id)
) COMMENT='租户角色和菜单关联表';
-- ----------------------------
-- 租户信息表
-- ----------------------------
CREATE TABLE sys_tenant (
  id bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  tenant_id varchar(128) NOT NULL DEFAULT '' COMMENT '租户ID',
  tenant_name varchar(128) DEFAULT '' COMMENT '租户名称',
  tenant_type varchar(18) DEFAULT 'TRIAL' COMMENT '租户类型(TRIAL: 试用租户， FORMAL: 正式租户)',
  tenant_desc varchar(255) DEFAULT NULL COMMENT '租户描述',
  tenant_state varchar(18) DEFAULT 'NORMAL' COMMENT '租户状态(NORMAL: 正常， FROZEN: 冻结)',
  contact_name varchar(255) DEFAULT NULL COMMENT '联系人姓名',
  phone varchar(20) DEFAULT NULL COMMENT '联系手机号',
  email varchar(255) DEFAULT NULL COMMENT '电子邮件',
  tenant_role_id varchar(48) DEFAULT NULL COMMENT '租户角色ID',
  create_source varchar(32) DEFAULT 'BG_CREATE' COMMENT '创建来源(REGIST: 注册，BG_CREATE: 后台创建 )',
  effective_time datetime NOT NULL COMMENT '生效时间',
  expire_time datetime NOT NULL COMMENT '失效时间',
  create_time datetime NOT NULL COMMENT '创建时间',
  update_time datetime NOT NULL COMMENT '修改时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_tenant_id (tenant_id) COMMENT 'tenantId唯一索引',
  UNIQUE KEY uk_tenant_name (tenant_name) COMMENT 'tenantName唯一索引'
) COMMENT='租户信息表';
INSERT INTO sys_tenant(id, tenant_id, tenant_name, tenant_type, tenant_desc, tenant_state, contact_name, phone, create_source, effective_time, expire_time, create_time, update_time) VALUES (1, 'super', '系统租户', 'FORMAL', '系统租户', 'NORMAL', NULL, NULL, 'BG_CREATE', '2020-11-09 00:00:00', '2099-11-30 00:00:00', sysdate(), sysdate());

-- ----------------------------2
-- 租户管理菜单
-- ----------------------------
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2006, '租户信息', 2519, 11, 'tenant', 'system/tenant/index', 1, 0, 'C', '0', '0', 'system:tenant:list', '#', 'admin', sysdate(), '', NULL, '租户信息菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2007, '租户信息查询', 2006, 1, '#', '', 1, 0, 'F', '0', '0', 'system:tenant:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2008, '租户信息新增', 2006, 2, '#', '', 1, 0, 'F', '0', '0', 'system:tenant:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2009, '租户信息修改', 2006, 3, '#', '', 1, 0, 'F', '0', '0', 'system:tenant:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2010, '租户信息删除', 2006, 4, '#', '', 1, 0, 'F', '0', '0', 'system:tenant:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2011, '租户信息导出', 2006, 5, '#', '', 1, 0, 'F', '0', '0', 'system:tenant:export', '#', 'admin', sysdate(), '', NULL, '');

-- ----------------------------
-- 系统表添加租户字段
-- ----------------------------
ALTER TABLE sys_user ADD COLUMN tenant_id varchar(255) NULL DEFAULT 'super' COMMENT '租户ID';
ALTER TABLE sys_role ADD COLUMN tenant_id varchar(255) NULL DEFAULT 'super' COMMENT '租户ID';
ALTER TABLE sys_post ADD COLUMN tenant_id varchar(255) NULL DEFAULT 'super' COMMENT '租户ID';
ALTER TABLE sys_oper_log ADD COLUMN tenant_id varchar(255) NULL DEFAULT 'super' COMMENT '租户ID';
ALTER TABLE sys_notice ADD COLUMN tenant_id varchar(255) NULL DEFAULT 'super' COMMENT '租户ID';
ALTER TABLE sys_logininfor ADD COLUMN tenant_id varchar(255) NULL DEFAULT 'super' COMMENT '租户ID';
ALTER TABLE sys_dept ADD COLUMN tenant_id varchar(255) NULL DEFAULT 'super' COMMENT '租户ID';

update sys_user set tenant_id = 'super' where tenant_id is null;
update sys_role set tenant_id = 'super' where tenant_id is null;
update sys_post set tenant_id = 'super' where tenant_id is null;
update sys_oper_log set tenant_id = 'super' where tenant_id is null;
update sys_notice set tenant_id = 'super' where tenant_id is null;
update sys_logininfor set tenant_id = 'super' where tenant_id is null;
update sys_dept set tenant_id = 'super' where tenant_id is null;

-- ----------------------------
-- 租户参数信息表
-- ----------------------------
CREATE TABLE sys_tenant_config (
  config_id int(5) NOT NULL COMMENT '参数主键',
  tenant_id varchar(255) NOT NULL COMMENT '租户ID',
  config_value varchar(500) DEFAULT '' COMMENT '参数值',
  update_by varchar(64) DEFAULT '' COMMENT '更新人',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (config_id,tenant_id)
) COMMENT='租户参数配置表';

-- ----------------------------
-- 用户表添加是否租户管理员字段
-- ----------------------------
ALTER TABLE sys_user ADD COLUMN tenant_admin char(1) DEFAULT 'N' COMMENT '是否租户管理员(Y是 N否)';




-- ----------------------------
-- 创建表分区存储过程(按天或者按月分区)
-- par_name_format:'%Y%m%d'或者'%Y%m'
-- 按月区分，day_value要传入某个月的最后一天
-- ----------------------------
DELIMITER $$
CREATE PROCEDURE sp_create_partition (day_value datetime, tb_schema varchar(128),tb_name varchar(128),par_name_format varchar(20))
BEGIN
  DECLARE par_name varchar(32);
  DECLARE par_value varchar(32);
  DECLARE _err int(1);
  DECLARE par_exist int(1);
  DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING, NOT FOUND SET _err = 1;
  START TRANSACTION;
    SET par_name = CONCAT('p', DATE_FORMAT(day_value, par_name_format));
    SELECT
      COUNT(1) INTO par_exist
    FROM information_schema.PARTITIONS
    WHERE TABLE_SCHEMA = tb_schema AND TABLE_NAME = tb_name AND PARTITION_NAME = par_name;
    IF (par_exist = 0) THEN
      SET par_value = DATE_FORMAT(day_value, '%Y-%m-%d');
      SET @alter_sql = CONCAT('alter table ', tb_name, ' add PARTITION (PARTITION ', par_name, ' VALUES LESS THAN (TO_DAYS("', par_value, '")+1))');
      PREPARE stmt1 FROM @alter_sql;
      EXECUTE stmt1;
    END IF;
  END
$$
	
	
-- ----------------------------
-- 删除表分区存储过程,用于删除过期分区(按天或者按月分区)
-- par_name_format:'%Y%m%d'或者'%Y%m'
-- ----------------------------
DELIMITER $$
CREATE PROCEDURE sp_drop_partition (day_value datetime, tb_schema varchar(128), tb_name varchar(128),par_name_format varchar(20))
BEGIN
  DECLARE str_day varchar(64);
  DECLARE _err int(1);
  DECLARE done int DEFAULT 0;
  DECLARE par_name varchar(64);
  DECLARE cur_partition_name CURSOR FOR
  SELECT
    partition_name
  FROM INFORMATION_SCHEMA.PARTITIONS
  WHERE TABLE_SCHEMA = tb_schema AND table_name = tb_name
  ORDER BY partition_ordinal_position;
  DECLARE CONTINUE HANDLER FOR SQLEXCEPTION, SQLWARNING, NOT FOUND SET _err = 1;
  DECLARE CONTINUE HANDLER FOR SQLSTATE '02000' SET done = 1;
  SET str_day = DATE_FORMAT(day_value, par_name_format);
  OPEN cur_partition_name;
  REPEAT
    FETCH cur_partition_name INTO par_name;
    IF (str_day > SUBSTRING(par_name, 2)) THEN
      SET @alter_sql = CONCAT('alter table ', tb_name, ' drop PARTITION ', par_name);
      PREPARE stmt1 FROM @alter_sql;
      EXECUTE stmt1;
    END IF;
  UNTIL done END REPEAT;
  CLOSE cur_partition_name;
END
$$

-- 系统参数添加"租户是否可以维护"控制字段 
ALTER TABLE sys_config ADD COLUMN tenant_maintain char(1) NOT NULL DEFAULT 'N' COMMENT '是否允许租户维护（Y:是，N:否）';

CREATE TABLE base_person_info (
  id varchar(48) NOT NULL COMMENT '主键',
  unique_id varchar(48) NOT NULL COMMENT '人员标识',
  name varchar(64) DEFAULT NULL COMMENT '姓名',
  sex char(1) DEFAULT NULL COMMENT '性别：0-男 1-女 2-未知',
  phone varchar(48) DEFAULT NULL COMMENT '手机',
  card_no varchar(255) DEFAULT NULL COMMENT '卡号',
  account varchar(255) DEFAULT NULL COMMENT '账号',
  email varchar(255) DEFAULT NULL COMMENT '邮箱',
  dept_id bigint(20) DEFAULT NULL COMMENT '部门ID',
  datasource varchar(48) NOT NULL DEFAULT 'INTERFACE' COMMENT '数据来源(数据字典)',
  flag char(1) DEFAULT '1' COMMENT '人员标记：1-正常 2-红名单 3-黑名单',
  status char(1) NOT NULL DEFAULT '0' COMMENT '状态：0-有效  1:无效',
  remark varchar(255) DEFAULT NULL COMMENT '备注',
  create_by varchar(64) DEFAULT NULL COMMENT '创建人',
  update_by varchar(64) DEFAULT NULL COMMENT '修改人',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_time datetime DEFAULT NULL COMMENT '修改时间',
  batch_date datetime DEFAULT NULL COMMENT '定时任务执行时间',
  tenant_id varchar(255) DEFAULT 'super' COMMENT '租户ID',
  PRIMARY KEY (id) USING BTREE,
  UNIQUE KEY uniqueIdIndex (unique_id,tenant_id)
) COMMENT='基础数据_人员基础信息';

CREATE TABLE base_person_face (
  id varchar(48) NOT NULL COMMENT '主键',
  person_id varchar(48) NOT NULL COMMENT '关联人员主键',
  unique_id varchar(48) NOT NULL COMMENT '人员标识',
  feature varchar(4000) NOT NULL COMMENT '人脸特征',
  feature_md5 varchar(255) DEFAULT NULL COMMENT '人脸特征MD5',
  quality_score double DEFAULT NULL COMMENT '图像质量得分',
  image_url varchar(255) NOT NULL COMMENT '人脸图像路径',
  vendor_code varchar(48) DEFAULT NULL COMMENT '厂商',
  algs_version varchar(48) DEFAULT NULL COMMENT '算法版本',
  encrypted char(1) DEFAULT '0' COMMENT '是否加密： 1-加密 0-不加密',
  datasource varchar(48) NOT NULL DEFAULT 'INTERFACE' COMMENT '数据来源(数据字典)',
  remark varchar(255) DEFAULT NULL COMMENT '备注',
  status char(1) NOT NULL DEFAULT '0' COMMENT '状态：0-有效  1:无效',
  create_by varchar(64) DEFAULT NULL COMMENT '创建人',
  update_by varchar(64) DEFAULT NULL COMMENT '修改人',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_time datetime DEFAULT NULL COMMENT '修改时间',
  batch_date datetime DEFAULT NULL COMMENT '定时任务执行时间',
  tenant_id varchar(255) DEFAULT 'super' COMMENT '租户ID',
  PRIMARY KEY (id) USING BTREE,
  KEY personIdIndex (person_id)
) COMMENT='基础数据_人脸图像信息';

CREATE TABLE base_person_finger (
  id varchar(48) NOT NULL COMMENT '主键',
  person_id varchar(48) NOT NULL COMMENT '关联人员主键',
  unique_id varchar(48) NOT NULL COMMENT '人员标识',
  finger_no varchar(2) NOT NULL COMMENT '手指编号',
  coercive_position varchar(2) NOT NULL DEFAULT '0' COMMENT '胁迫位',
  feature varchar(4000) NOT NULL COMMENT '指纹特征',
  feature_md5 varchar(255) DEFAULT NULL COMMENT '指纹特征MD5',
  quality_score double DEFAULT NULL COMMENT '图像质量得分',
  image_url varchar(255) DEFAULT NULL COMMENT '指纹图像路径',
  vendor_code varchar(48) DEFAULT NULL COMMENT '厂商',
  algs_version varchar(48) DEFAULT NULL COMMENT '算法版本',
  encrypted char(1) DEFAULT '0' COMMENT '是否加密： 1-加密 0-不加密',
  datasource varchar(48) NOT NULL DEFAULT 'INTERFACE' COMMENT '数据来源(数据字典)',
  remark varchar(255) DEFAULT NULL COMMENT '备注',
  status char(1) NOT NULL DEFAULT '0' COMMENT '状态：0-有效  1:无效',
  create_by varchar(64) DEFAULT NULL COMMENT '创建人',
  update_by varchar(64) DEFAULT NULL COMMENT '修改人',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_time datetime DEFAULT NULL COMMENT '修改时间',
  batch_date datetime DEFAULT NULL COMMENT '定时任务执行时间',
  tenant_id varchar(255) DEFAULT 'super' COMMENT '租户ID',
  PRIMARY KEY (id) USING BTREE,
  KEY personIdIndex (person_id)
) COMMENT='基础数据_指纹图像信息';

CREATE TABLE base_person_iris (
  id varchar(48) NOT NULL COMMENT '主键',
  person_id varchar(48) NOT NULL COMMENT '关联人员主键',
  unique_id varchar(48) NOT NULL COMMENT '人员标识',
  feature longtext NOT NULL COMMENT '虹膜特征',
  feature_md5 varchar(255) DEFAULT NULL COMMENT '虹膜特征MD5',
  quality_score double DEFAULT NULL COMMENT '图像质量得分',
  image_url varchar(255) NOT NULL COMMENT '虹膜图像路径',
  vendor_code varchar(48) DEFAULT NULL COMMENT '厂商',
  algs_version varchar(48) DEFAULT NULL COMMENT '算法版本',
  encrypted char(1) DEFAULT '0' COMMENT '是否加密： 1-加密 0-不加密',
  datasource varchar(48) NOT NULL DEFAULT 'INTERFACE' COMMENT '数据来源(数据字典)',
  remark varchar(255) DEFAULT NULL COMMENT '备注',
  status char(1) NOT NULL DEFAULT '0' COMMENT '状态：0-有效  1:无效',
  create_by varchar(64) DEFAULT NULL COMMENT '创建人',
  update_by varchar(64) DEFAULT NULL COMMENT '修改人',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_time datetime DEFAULT NULL COMMENT '修改时间',
  batch_date datetime DEFAULT NULL COMMENT '定时任务执行时间',
  tenant_id varchar(255) DEFAULT 'super' COMMENT '租户ID',
  PRIMARY KEY (id) USING BTREE,
  KEY personIdIndex (person_id)
) COMMENT='基础数据_虹膜图像信息';


CREATE TABLE base_person_iris_face (
  id varchar(48) NOT NULL COMMENT '主键',
  person_id varchar(48) NOT NULL COMMENT '关联人员主键',
  unique_id varchar(48) NOT NULL COMMENT '人员标识',
  fusion_feature longtext COMMENT '融合特征',
  face_feature longtext COMMENT '人脸特征',
  iris_feature longtext COMMENT '虹膜特征',
  fusion_feature_md5 varchar(255) DEFAULT NULL COMMENT '融合特征md5',
  face_feature_md5 varchar(255) DEFAULT NULL COMMENT '人脸特征md5',
  face_image_url varchar(255) DEFAULT NULL COMMENT '人脸照片路径',
  face_quality double DEFAULT NULL COMMENT '人脸质量分数',
  iris_feature_md5 varchar(255) DEFAULT NULL COMMENT '虹膜特征md5',
  iris_image_url varchar(255) DEFAULT NULL COMMENT '虹膜照片路径',
  iris_quality double DEFAULT NULL COMMENT '虹膜质量分数',
  encrypted char(1) DEFAULT '0' COMMENT '是否加密： 1-加密 0-不加密',
  status char(1) NOT NULL DEFAULT '0' COMMENT '状态：0-有效  1-无效',
  datasource varchar(50) DEFAULT 'INTERFACE' COMMENT '数据来源(数据字典)',
  data_describe varchar(255) DEFAULT NULL COMMENT '数据描述',
  validity_date datetime DEFAULT NULL COMMENT '有效期',
  create_by varchar(48) DEFAULT NULL COMMENT '创建人',
  update_by varchar(48) DEFAULT NULL COMMENT '修改人',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_time datetime DEFAULT NULL COMMENT '修改时间',
  remark varchar(255) DEFAULT NULL COMMENT '备注',
  tenant_id varchar(255) DEFAULT 'super' COMMENT '租户ID',
  PRIMARY KEY (id),
  KEY PERSON_UNIQUEID_INDEX (unique_id)
) COMMENT='虹膜人脸多模态图片特征表';


CREATE TABLE base_person_cert (
  id varchar(48) NOT NULL COMMENT '主键',
  unique_id varchar(48) NOT NULL COMMENT '人员标识',
  cert_type varchar(3) NOT NULL COMMENT '证件类型（字典国标）',
  cert_num varchar(255) NOT NULL COMMENT '证件号码',
  cert_name varchar(64) DEFAULT NULL COMMENT '证件姓名',
  cert_validity varchar(48) DEFAULT NULL COMMENT '证件有效期',
  gender char(1) DEFAULT NULL COMMENT '性别：0-男 1-女 2-未知',
  bth_date varchar(48) DEFAULT NULL COMMENT '出生日期',
  nation varchar(3) DEFAULT NULL COMMENT '民族',
  address varchar(255) DEFAULT NULL COMMENT '家庭住址',
  cert_authority varchar(255) DEFAULT NULL COMMENT '发证机关',
  cert_img varchar(255) DEFAULT NULL COMMENT '证件照',
  encrypted char(1) DEFAULT '0' COMMENT '是否加密： 1-加密 0-不加密',
  enterschool_img varchar(255) DEFAULT NULL COMMENT '入学照片',
  inschool_img varchar(255) DEFAULT NULL COMMENT '在校照片',
  graduate_img varchar(255) DEFAULT NULL COMMENT '毕业照片',
  remark varchar(255) DEFAULT NULL COMMENT '备注',
  create_by varchar(64) DEFAULT NULL COMMENT '创建人',
  update_by varchar(64) DEFAULT NULL COMMENT '修改人',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_time datetime DEFAULT NULL COMMENT '修改时间',
  batch_date datetime DEFAULT NULL COMMENT '定时任务执行时间',
  tenant_id varchar(255) DEFAULT 'super' COMMENT '租户ID',
  PRIMARY KEY (id) USING BTREE
) COMMENT='基础数据_人员证件信息';


CREATE TABLE channel_business  (
  id varchar(48)  NOT NULL COMMENT '主键',
  channel_id varchar(48)  NOT NULL COMMENT '渠道主键',
  person_id varchar(48)  NOT NULL COMMENT '人员主键',
  unique_id varchar(48)  NOT NULL COMMENT '人员唯一标识',
  busi_code_first varchar(255)  NULL DEFAULT NULL COMMENT '业务号1',
  busi_code_second varchar(255)  NULL DEFAULT NULL COMMENT '业务号2',
  busi_code_third varchar(255)  NULL DEFAULT NULL COMMENT '业务号3',
  face_mode varchar(1)  NOT NULL DEFAULT '1' COMMENT '开通人脸(数据字典:0-不开通 1-开通)',
  finger_mode varchar(1)  NOT NULL DEFAULT '1' COMMENT '开通指纹(数据字典:0-不开通 1-开通)',
  iris_mode varchar(1)  NOT NULL DEFAULT '1' COMMENT '开通虹膜(数据字典:0-不开通 1-开通)',
  fvein_mode varchar(1)  NOT NULL DEFAULT '1' COMMENT '开通指静脉(数据字典:0-不开通 1-开通)',
  face_iris_mode varchar(1)  NOT NULL DEFAULT '1' COMMENT '开通人脸虹膜多模态(数据字典:0-不开通 1-开通)',
  datasource varchar(48)  NOT NULL DEFAULT 'INTERFACE' COMMENT '数据来源(数据字典)',
  locked varchar(1)  NULL DEFAULT 'N' COMMENT '是否锁定(数据字典:N-否 Y-是)',
  lock_time datetime NULL DEFAULT NULL COMMENT '锁定时间',
  status char(1)  NOT NULL DEFAULT '0' COMMENT '状态(数据字典:0-有效  1:无效)',
  remark varchar(255)  NULL DEFAULT NULL COMMENT '备注',
  create_by varchar(64)  NULL DEFAULT NULL COMMENT '创建人',
  update_by varchar(64)  NULL DEFAULT NULL COMMENT '修改人',
  create_time datetime NULL DEFAULT NULL COMMENT '创建时间',
  update_time datetime NULL DEFAULT NULL COMMENT '修改时间',
  batch_date datetime NULL DEFAULT NULL COMMENT '定时任务执行时间',
  tenant_id varchar(255)  NULL DEFAULT 'super' COMMENT '租户ID',
  update_seria_num bigint(20) NULL DEFAULT NULL COMMENT '业务更新标识流水码',
  PRIMARY KEY (id) USING BTREE,
  UNIQUE INDEX channelIdPersonIdUnique(channel_id, person_id) USING BTREE
) COMMENT = '渠道管理_渠道业务表';


CREATE TABLE channel_info  (
  id varchar(48)  NOT NULL COMMENT '主键',
  channel_code varchar(255)  NOT NULL COMMENT '渠道编码',
  channel_name varchar(255)  NOT NULL COMMENT '渠道名称',
  face_mode varchar(1)  NOT NULL DEFAULT '1' COMMENT '开通人脸(数据字典:0-不开通 1-开通)',
  finger_mode varchar(1)  NOT NULL DEFAULT '1' COMMENT '开通指纹(数据字典:0-不开通 1-开通)',
  iris_mode varchar(1)  NOT NULL DEFAULT '1' COMMENT '开通虹膜(数据字典:0-不开通 1-开通)',
  fvein_mode varchar(1)  NOT NULL DEFAULT '1' COMMENT '开通指静脉(数据字典:0-不开通 1-开通)',
  face_iris_mode varchar(1)  NOT NULL DEFAULT '1' COMMENT '开通人脸虹膜多模态(数据字典:0-不开通 1-开通)',
  enable_multi_faces varchar(1)  NULL DEFAULT 'N' COMMENT '是否支持多人脸(数据字典:N-否 Y-是)',
  search_n varchar(1)  NULL DEFAULT '2' COMMENT '1-N识别方式(数据字典:1-校验分库、2-校验渠道库、3-校验全库, 4-依次校验)',
  device_num_limit int(11) NULL DEFAULT 0 COMMENT '挂载设备数量上限',
  remark varchar(255)  NULL DEFAULT NULL COMMENT '备注',
  create_by varchar(64)  NULL DEFAULT NULL COMMENT '创建人',
  update_by varchar(64)  NULL DEFAULT NULL COMMENT '修改人',
  create_time datetime NULL DEFAULT NULL COMMENT '创建时间',
  update_time datetime NULL DEFAULT NULL COMMENT '修改时间',
  batch_date datetime NULL DEFAULT NULL COMMENT '定时任务执行时间',
  tenant_id varchar(255)  NULL DEFAULT 'super' COMMENT '租户ID',
  PRIMARY KEY (id) USING BTREE
) COMMENT = '渠道管理_渠道信息表';


CREATE TABLE channel_param  (
  id varchar(48)  NOT NULL COMMENT '主键',
  channel_id varchar(48)  NOT NULL COMMENT '渠道主键',
  param_code varchar(255)  NOT NULL COMMENT '参数编码',
  param_name varchar(255)  NULL DEFAULT NULL COMMENT '参数名称',
  param_value varchar(255)  NOT NULL COMMENT '参数键值',
  bio_attest_type varchar(1)  NOT NULL COMMENT '认证类型(数据字典::0-人脸  1指纹 2虹膜 3指静脉)',
  remark varchar(255)  NULL DEFAULT NULL COMMENT '备注',
  create_by varchar(64)  NULL DEFAULT NULL COMMENT '创建人',
  update_by varchar(64)  NULL DEFAULT NULL COMMENT '修改人',
  create_time datetime NULL DEFAULT NULL COMMENT '创建时间',
  update_time datetime NULL DEFAULT NULL COMMENT '修改时间',
  batch_date datetime NULL DEFAULT NULL COMMENT '定时任务执行时间',
  tenant_id varchar(255)  NULL DEFAULT 'super' COMMENT '租户ID',
  PRIMARY KEY (id) USING BTREE
) COMMENT = '渠道管理_渠道参数表';


CREATE TABLE channel_subtreasury_busi  (
  id varchar(48)  NOT NULL COMMENT '主键',
  channel_id varchar(48)  NOT NULL COMMENT '渠道主键',
  sub_treasury_id varchar(48)  NOT NULL COMMENT '分库主键',
  person_id varchar(48)  NOT NULL COMMENT '人员主键',
  unique_id varchar(48)  NOT NULL COMMENT '人员唯一标识',
  datasource varchar(48)  NOT NULL DEFAULT 'INTERFACE' COMMENT '数据来源(数据字典)',
  status char(1)  NOT NULL DEFAULT '0' COMMENT '状态(数据字典:0-有效  1:无效)',
  remark varchar(255)  NULL DEFAULT NULL COMMENT '备注',
  create_by varchar(64)  NULL DEFAULT NULL COMMENT '创建人',
  update_by varchar(64)  NULL DEFAULT NULL COMMENT '修改人',
  create_time datetime NULL DEFAULT NULL COMMENT '创建时间',
  update_time datetime NULL DEFAULT NULL COMMENT '修改时间',
  batch_date datetime NULL DEFAULT NULL COMMENT '定时任务执行时间',
  tenant_id varchar(255)  NULL DEFAULT 'super' COMMENT '租户ID',
  update_seria_num bigint(20) NULL DEFAULT NULL COMMENT '业务更新标识流水码',
  PRIMARY KEY (id) USING BTREE,
  UNIQUE INDEX cIdSubCodePIdUnique(channel_id, sub_treasury_id, person_id) USING BTREE
) COMMENT = '渠道管理_分库业务表';


CREATE TABLE channel_subtreasury_info  (
  id varchar(48)  NOT NULL COMMENT '主键',
  channel_id varchar(48)  NOT NULL COMMENT '渠道主键',
  sub_treasury_code varchar(100)  NOT NULL COMMENT '分库号(渠道编码_分库号)',
  sub_treasury_name varchar(100)  NOT NULL COMMENT '分库名称',
  remark varchar(255)  NULL DEFAULT NULL COMMENT '备注',
  create_by varchar(64)  NULL DEFAULT NULL COMMENT '创建人',
  update_by varchar(64)  NULL DEFAULT NULL COMMENT '修改人',
  create_time datetime NULL DEFAULT NULL COMMENT '创建时间',
  update_time datetime NULL DEFAULT NULL COMMENT '修改时间',
  batch_date datetime NULL DEFAULT NULL COMMENT '定时任务执行时间',
  tenant_id varchar(255)  NULL DEFAULT 'super' COMMENT '租户ID',
  PRIMARY KEY (id) USING BTREE
) COMMENT = '分库信息表';


CREATE TABLE sys_tenant_interface (
  tenant_id varchar(255) NOT NULL COMMENT '租户ID',
  transcode varchar(255) NOT NULL COMMENT '接口交易码',
  expire_time datetime NOT NULL COMMENT '过期时间',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (tenant_id,transcode)
) COMMENT='租户和接口关联表';


CREATE TABLE area_model (
  id bigint(20) NOT NULL AUTO_INCREMENT COMMENT '区域id',
  parent_id bigint(20) DEFAULT '0' COMMENT '父区域id',
  ancestors varchar(50) DEFAULT '' COMMENT '祖级列表',
  area_name varchar(30) DEFAULT '' COMMENT '区域名称',
  order_num int(4) DEFAULT '0' COMMENT '显示顺序',
  area_type varchar(30) DEFAULT '' COMMENT '区域类型 字典',
  status char(1) DEFAULT '0' COMMENT '区域状态（0正常 1停用）',
  create_by varchar(64) DEFAULT '' COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT '' COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT '' COMMENT '备注',
  tenant_id varchar(255) DEFAULT 'super' COMMENT '租户ID',
  PRIMARY KEY (id) USING BTREE
) COMMENT='区域表';


CREATE TABLE device_model (
  id varchar(48) NOT NULL COMMENT '主键',
  model_code varchar(100) NOT NULL COMMENT '设备型号编码',
  model_name varchar(255) NOT NULL COMMENT '设备型号名称',
  exterior_image varchar(255) DEFAULT NULL COMMENT '设备外观图片',
  model_desc varchar(2000) DEFAULT NULL COMMENT '设备型号描述',
  create_by varchar(64) DEFAULT NULL COMMENT '创建人',
  update_by varchar(64) DEFAULT NULL COMMENT '修改人',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_time datetime DEFAULT NULL COMMENT '修改时间',
  tenant_id varchar(255) DEFAULT 'super' COMMENT '租户ID',
  PRIMARY KEY (id),
  UNIQUE KEY model_code_index (model_code,tenant_id)
) COMMENT='设备型号信息';


CREATE TABLE device_param_info (
  id varchar(48) NOT NULL COMMENT '主键',
  param_code varchar(100) NOT NULL COMMENT '设备参数编码',
  param_name varchar(255) NOT NULL COMMENT '设备参数名称',
  param_desc varchar(2000) DEFAULT NULL COMMENT '设备参数描述',
  create_by varchar(64) DEFAULT NULL COMMENT '创建人',
  update_by varchar(64) DEFAULT NULL COMMENT '修改人',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_time datetime DEFAULT NULL COMMENT '修改时间',
  tenant_id varchar(255) DEFAULT 'super' COMMENT '租户ID',
  PRIMARY KEY (id),
  UNIQUE KEY param_code_index (param_code,tenant_id)
) COMMENT='设备参数信息';


CREATE TABLE device_param_model_rel (
  id varchar(48) NOT NULL COMMENT '主键',
  model_code varchar(255) NOT NULL COMMENT '设备型号编码',
  param_code varchar(255) NOT NULL COMMENT '设备参数编码',
  create_by varchar(64) DEFAULT NULL COMMENT '创建人',
  update_by varchar(64) DEFAULT NULL COMMENT '修改人',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_time datetime DEFAULT NULL COMMENT '修改时间',
  tenant_id varchar(255) DEFAULT 'super' COMMENT '租户ID',
  PRIMARY KEY (id)
) COMMENT='参数型号关系';

CREATE TABLE device_info (
  id varchar(48) NOT NULL COMMENT '主键',
  device_no varchar(48) NOT NULL COMMENT '设备编号',
  device_name varchar(255) DEFAULT NULL COMMENT '设备名称',
  device_addr varchar(255) DEFAULT NULL COMMENT '安装地点',
  device_model_code varchar(48) DEFAULT NULL COMMENT '型号编码',
  device_ip varchar(48) DEFAULT NULL COMMENT '设备IP',
  device_mac varchar(48) DEFAULT NULL COMMENT '设备Mac',
  longitude double DEFAULT NULL COMMENT '设备经度',
  latitude double DEFAULT NULL COMMENT '设备纬度',
  channel_code varchar(255) DEFAULT NULL COMMENT '渠道编码',
  area_id bigint(20) DEFAULT NULL COMMENT '区域主键',
  subtreasury_code varchar(255) DEFAULT NULL COMMENT '分库编码(用,分隔)',
  import_batch_num varchar(48) DEFAULT NULL COMMENT '导入批次',
  create_method char(1) NOT NULL DEFAULT '1' COMMENT '创建方式(1: 后台添加，2：自主注册)',
  pull_all_flag char(1) DEFAULT '1' COMMENT '是否进行全量拉取（1：是，0：否）',
  primary_sub_code varchar(48) DEFAULT NULL COMMENT '主分库编码',
  duplicate_time int(11) DEFAULT NULL COMMENT '后端比对去重时长(ms)',
  device_type char(1) NOT NULL DEFAULT '1' COMMENT '设备类型(字典 1:常规 2..)',
  mqtt_pwd varchar(255) NOT NULL COMMENT 'MQTT连接密码',
  mqtt_salt varchar(255) NOT NULL COMMENT 'MQTT密码盐值',
  ext_info varchar(4000) DEFAULT NULL COMMENT '扩展信息',
  create_by varchar(64) DEFAULT NULL COMMENT '创建人',
  update_by varchar(64) DEFAULT NULL COMMENT '修改人',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_time datetime DEFAULT NULL COMMENT '修改时间',
  batch_date datetime DEFAULT NULL COMMENT '定时任务执行时间',
  tenant_id varchar(255) DEFAULT 'super' COMMENT '租户ID',
  PRIMARY KEY (id),
  UNIQUE KEY deviceno_unique_index (device_no) USING BTREE COMMENT '设备编码唯一索引'
) COMMENT='设备信息表';


CREATE TABLE device_batch_log (
  id varchar(48) NOT NULL COMMENT '主键',
  batch_num varchar(50) NOT NULL COMMENT '批次号',
  batch_desc varchar(255) NOT NULL COMMENT '批次说明',
  rollbacked char(1) NOT NULL DEFAULT '0' COMMENT '是否回滚(1:是， 0:否)',
  rollback_time datetime DEFAULT NULL COMMENT '回滚时间',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  create_by varchar(255) DEFAULT NULL COMMENT '创建人',
  update_time datetime DEFAULT NULL COMMENT '修改时间',
  update_by varchar(255) DEFAULT NULL COMMENT '修改人',
  tenant_id varchar(255) DEFAULT 'super' COMMENT '导入租户',
  PRIMARY KEY (id),
  UNIQUE KEY uk_batch_num (batch_num) USING BTREE COMMENT '批次号唯一索引'
) COMMENT='设备导入批次日志';

CREATE TABLE device_upgrade_version (
  id varchar(48) NOT NULL COMMENT '主键',
  app_name varchar(48) NOT NULL COMMENT 'APP名',
  version varchar(48) NOT NULL COMMENT '版本号',
  description varchar(255) DEFAULT NULL COMMENT '版本描述',
  path varchar(255) NOT NULL COMMENT '升级文件',
  file_size bigint(20) NOT NULL COMMENT '版本大小(B)',
  filename varchar(255) NOT NULL COMMENT '源文件名',
  md5 varchar(255) NOT NULL COMMENT '文件MD5',
  enabled char(1) DEFAULT '0' COMMENT '是否启用(1:启用 0:停用)',
  create_by varchar(64) DEFAULT NULL COMMENT '创建人',
  update_by varchar(64) DEFAULT NULL COMMENT '修改人',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_time datetime DEFAULT NULL COMMENT '修改时间',
  batch_date datetime DEFAULT NULL COMMENT '定时任务执行时间',
  tenant_id varchar(255) DEFAULT 'super' COMMENT '租户ID',
  PRIMARY KEY (id)
) COMMENT='版本信息表';

CREATE TABLE device_upgrade_task (
  id varchar(48) NOT NULL COMMENT '主键',
  version_id varchar(48) NOT NULL COMMENT '版本主键',
  device_id varchar(48) NOT NULL COMMENT '设备主键',
  upgrade_time datetime DEFAULT NULL COMMENT '更新时间(如果为空，表示立即更新)',
  upgrade_count_limit int(11) NOT NULL COMMENT '升级次数上限',
  execute_count int(11) NOT NULL DEFAULT '0' COMMENT '执行次数',
  pub_count int(11) DEFAULT '0' COMMENT '发布次数',
  task_index int(11) NOT NULL COMMENT '任务排序',
  execute_result varchar(1) NOT NULL DEFAULT '1' COMMENT '执行结果(1:待执行，2：成功，3：失败，4：跳过)',
  rollback_install char(1) NOT NULL DEFAULT '0' COMMENT '是否降级安装(1：是，0：否)',
  create_by varchar(64) DEFAULT NULL COMMENT '创建人',
  update_by varchar(64) DEFAULT NULL COMMENT '修改人',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_time datetime DEFAULT NULL COMMENT '修改时间',
  batch_date datetime DEFAULT NULL COMMENT '定时任务执行时间',
  tenant_id varchar(255) DEFAULT 'super' COMMENT '租户ID',
  PRIMARY KEY (id)
) COMMENT='升级任务表';


CREATE TABLE device_upgrade_log (
  id varchar(48) NOT NULL COMMENT '主键',
  task_id varchar(48) NOT NULL COMMENT '任务主键',
  device_no varchar(48) NOT NULL COMMENT '设备编号',
  device_name varchar(255) DEFAULT NULL COMMENT '设备名称',
  before_app_name varchar(48) DEFAULT NULL COMMENT '升级前APP名',
  after_app_name varchar(48) NOT NULL COMMENT '升级后APP名',
  before_version varchar(48) DEFAULT NULL COMMENT '升级前版本',
  after_version varchar(48) NOT NULL COMMENT '升级后版本',
  upgrade_status char(1) NOT NULL COMMENT '更新状态(1:待下载，2：待更新，3：更新成功，4：更新失败，5：跳过)',
  client_time datetime DEFAULT NULL COMMENT '升级时前端时间',
  server_time datetime DEFAULT NULL COMMENT '升级时后端时间',
  time_used int(11) DEFAULT NULL COMMENT '升级耗时(单位s)',
  fail_reason varchar(500) DEFAULT NULL COMMENT '失败原因',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_time datetime DEFAULT NULL COMMENT '创建时间',
  batch_date datetime DEFAULT NULL COMMENT '定时任务执行时间',
  tenant_id varchar(255) DEFAULT 'super' COMMENT '租户ID',
  PRIMARY KEY (id)
) COMMENT='升级日志表';


CREATE TABLE device_param_distribute_log (
  id varchar(48) NOT NULL COMMENT '主键',
  device_no varchar(48) NOT NULL COMMENT '设备编号',
  param_code varchar(255) NOT NULL COMMENT '参数编码',
  param_value varchar(255) NOT NULL COMMENT '参数值',
  distribute_result char(1) DEFAULT NULL COMMENT '下发结果(0：成功，1：失败)',
  sort_index bigint(20) NOT NULL COMMENT '下发排序',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  tenant_id varchar(255) DEFAULT 'super' COMMENT '租户ID',
  PRIMARY KEY (id)
) COMMENT='参数下发日志';


CREATE TABLE device_action_log (
  id varchar(48) NOT NULL COMMENT '主键',
  device_name varchar(255) DEFAULT NULL COMMENT '设备名称',
  device_no varchar(48) NOT NULL COMMENT '设备编号',
  channel_code varchar(48) DEFAULT NULL COMMENT '设备渠道编码',
  model_code varchar(48) DEFAULT NULL COMMENT '设备型号编码',
  action_type char(1) NOT NULL COMMENT '动作类型(1:注册 2:上线 3:下线 4:更新)',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  tenant_id varchar(255) DEFAULT 'super' COMMENT '租户ID',
  PRIMARY KEY (id)
) COMMENT='设备动作日志';

CREATE TABLE device_access_adapter (
  id varchar(48) NOT NULL COMMENT '主键',
  device_name varchar(255) DEFAULT NULL COMMENT '设备名称',
  device_sn varchar(48) NOT NULL COMMENT '设备序列号',
  seria_num varchar(100) NOT NULL COMMENT '同步序列号',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  tenant_id varchar(255) DEFAULT 'super' COMMENT '租户id',
  PRIMARY KEY (id),
  UNIQUE KEY idx_sn (device_sn)
) COMMENT='203设备接入';

CREATE TABLE sys_sdk_file (
  id varchar(48) NOT NULL COMMENT '主键',
  file_name varchar(255) NOT NULL COMMENT '上传文件名称',
  file_path varchar(255) NOT NULL COMMENT '文件存储路径',
  md5 varchar(255) NOT NULL COMMENT '文件MD5',
  sdk_type varchar(20) NOT NULL COMMENT 'SDK类型',
  create_by varchar(64) DEFAULT NULL COMMENT '创建人',
  update_by varchar(64) DEFAULT NULL COMMENT '修改人',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_time datetime DEFAULT NULL COMMENT '修改时间',
  tenant_id varchar(255) NOT NULL DEFAULT 'super' COMMENT '租户ID',
  sorted_no int(20) NOT NULL COMMENT '版本排序',
  PRIMARY KEY (id)
) COMMENT='SDK文件上传表';

CREATE TABLE app_info (
  id varchar(48) NOT NULL COMMENT '主键',
  app_key varchar(255) NOT NULL COMMENT '应用系统键值',
  app_secret varchar(255) NOT NULL COMMENT '应用系统密钥',
  app_desc varchar(255) DEFAULT NULL COMMENT '应用系统描述',
  built_in varchar(1) NOT NULL DEFAULT 'N' COMMENT '是否内置(Y：是，N：否)',
  remark varchar(255) DEFAULT NULL COMMENT '备注',
  status char(1) NOT NULL DEFAULT '0' COMMENT '状态(数据字典:0-有效  1:无效)',
  create_by varchar(64) DEFAULT NULL COMMENT '创建人',
  update_by varchar(64) DEFAULT NULL COMMENT '修改人',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_time datetime DEFAULT NULL COMMENT '修改时间',
  tenant_id varchar(255) DEFAULT 'super' COMMENT '租户ID',
  PRIMARY KEY (id)
) COMMENT='应用系统信息表';


CREATE TABLE app_interface_auth (
  id varchar(48) NOT NULL COMMENT '主键',
  app_id varchar(48) NOT NULL COMMENT '应用系统主键',
  trans_code varchar(255) NOT NULL COMMENT '接口交易码(数据字典)',
  trans_end_time datetime DEFAULT NULL COMMENT '交易有效截止时间',
  remark varchar(255) DEFAULT NULL COMMENT '备注',
  create_by varchar(64) DEFAULT NULL COMMENT '创建人',
  update_by varchar(64) DEFAULT NULL COMMENT '修改人',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_time datetime DEFAULT NULL COMMENT '修改时间',
  tenant_id varchar(255) DEFAULT 'super' COMMENT '租户ID',
  PRIMARY KEY (id)
) COMMENT='应用系统接口授权表';


CREATE TABLE msg_ding_application (
  id varchar(48) NOT NULL COMMENT '主键',
  team_id varchar(255) NOT NULL COMMENT '团队(企业)主键',
  corp_id varchar(255) NOT NULL COMMENT '团队(企业)CorpId',
  app_name varchar(255) NOT NULL COMMENT '微应用名称',
  agent_id varchar(255) NOT NULL COMMENT '微应用agentId',
  app_key varchar(255) NOT NULL COMMENT '微应用AppKey',
  app_secrect varchar(255) NOT NULL COMMENT '微应用AppSecrect',
  short_des varchar(255) DEFAULT NULL COMMENT '微应用简介',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  tenant_id varchar(255) DEFAULT 'super' COMMENT '租户ID',
  PRIMARY KEY (id)
) COMMENT='钉钉微应用';


CREATE TABLE msg_ding_team (
  id varchar(48) NOT NULL COMMENT '主键',
  corp_id varchar(255) NOT NULL COMMENT '团队(企业)CorpId',
  team_name varchar(255) NOT NULL COMMENT '团队(企业)名称',
  team_des varchar(500) DEFAULT NULL COMMENT '团队(企业)说明',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  tenant_id varchar(255) DEFAULT 'super' COMMENT '租户ID',
  PRIMARY KEY (id)
) COMMENT='钉钉团队(企业)';

CREATE TABLE msg_log (
  id varchar(48) NOT NULL COMMENT '主键',
  received_seq varchar(48) NOT NULL COMMENT '业务流水号',
  notice_method varchar(1) NOT NULL COMMENT '通知方式(1：短信，2：邮件，3：微信，4：钉钉)',
  msg_subject varchar(255) DEFAULT NULL COMMENT '消息主题',
  msg_content varchar(4000) DEFAULT NULL COMMENT '消息内容',
  has_annex varchar(1) NOT NULL DEFAULT 'N' COMMENT '是否包含附件(Y：是，N：否)',
  from_user varchar(255) DEFAULT NULL COMMENT '发信人标识',
  to_user varchar(4000) DEFAULT NULL COMMENT '收件人标识(多个使用“,”分隔)',
  offical_account_id varchar(48) DEFAULT NULL COMMENT '公众(企业)号标识',
  offical_account_name varchar(255) DEFAULT NULL COMMENT '公众(企业)号名称',
  scene_remark varchar(48) DEFAULT NULL COMMENT '场景标识',
  result_status varchar(1) NOT NULL COMMENT '发送状态(0：成功，1：失败)',
  err_msg varchar(255) DEFAULT NULL COMMENT '错误信息',
  json_response varchar(4000) DEFAULT NULL COMMENT '发送结果json',
  create_time datetime NOT NULL COMMENT '创建时间',
  tenant_id varchar(255) DEFAULT 'super' COMMENT '租户ID',
  PRIMARY KEY (id,create_time)
) PARTITION BY RANGE (TO_DAYS(create_time)) (
   PARTITION p0 VALUES LESS THAN (0)
) ;
ALTER TABLE msg_log COMMENT='消息日志';
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL - 2 DAY), @schemaName, 'msg_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL - 1 DAY), @schemaName, 'msg_log','%Y%m%d');
CALL sp_create_partition(sysdate(), @schemaName, 'msg_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 1 DAY), @schemaName, 'msg_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 2 DAY), @schemaName, 'msg_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 3 DAY), @schemaName, 'msg_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 4 DAY), @schemaName, 'msg_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 5 DAY), @schemaName, 'msg_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 6 DAY), @schemaName, 'msg_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 7 DAY), @schemaName, 'msg_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 8 DAY), @schemaName, 'msg_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 9 DAY), @schemaName, 'msg_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 10 DAY), @schemaName, 'msg_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 11 DAY), @schemaName, 'msg_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 12 DAY), @schemaName, 'msg_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 13 DAY), @schemaName, 'msg_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 14 DAY), @schemaName, 'msg_log','%Y%m%d');

CREATE TABLE msg_log_annex (
  id varchar(48) NOT NULL COMMENT '主键',
  log_id varchar(48) NOT NULL COMMENT '消息日志ID',
  file_name varchar(255) NOT NULL COMMENT '源文件名称',
  file_path varchar(255) NOT NULL COMMENT '文件路径',
  file_md5 varchar(48) DEFAULT NULL COMMENT '文件MD5',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  tenant_id varchar(255) DEFAULT 'super' COMMENT '租户ID',
  PRIMARY KEY (id) 
) COMMENT='消息日志附件';


CREATE TABLE msg_mail_property (
  id varchar(48) NOT NULL COMMENT '主键',
  host varchar(255) NOT NULL COMMENT 'SMTP服务地址',
  port int(11) DEFAULT NULL COMMENT 'SMTP服务端口',
  username varchar(255) NOT NULL COMMENT '登录用户名',
  password varchar(255) NOT NULL COMMENT '登录授权码',
  email_addr varchar(255) NOT NULL COMMENT '发件邮箱',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  tenant_id varchar(255) DEFAULT 'super' COMMENT '租户ID',
  PRIMARY KEY (id)
) COMMENT='邮箱配置';


CREATE TABLE msg_offical_account (
  id varchar(48) NOT NULL COMMENT '主键',
  app_id varchar(500) NOT NULL COMMENT 'AppId',
  app_secrect varchar(500) NOT NULL COMMENT 'AppSecrect',
  app_name varchar(500) NOT NULL COMMENT '公众号(云账户)名称',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  tenant_id varchar(255) DEFAULT 'super' COMMENT '租户ID',
  PRIMARY KEY (id)
) COMMENT='微信公众号';

CREATE TABLE msg_offical_account_user (
  id varchar(48) NOT NULL COMMENT '主键',
  offical_account_id varchar(48) DEFAULT NULL COMMENT '公众号ID',
  app_id varchar(48) NOT NULL COMMENT '公众号AppId',
  open_id varchar(255) NOT NULL COMMENT '微信标识',
  wx_name varchar(255) NOT NULL COMMENT '微信名称',
  head_img_url varchar(500) DEFAULT NULL COMMENT '微信头像',
  phone varchar(30) DEFAULT NULL COMMENT '绑定手机',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  tenant_id varchar(255) DEFAULT 'super' COMMENT '租户ID',
  PRIMARY KEY (id)
) COMMENT='微信用户';

CREATE TABLE msg_sms_cloud_account (
  id varchar(48) NOT NULL COMMENT '主键',
  app_id varchar(500) NOT NULL COMMENT 'AppId',
  app_secrect varchar(500) NOT NULL COMMENT 'AppSecrect',
  app_name varchar(500) NOT NULL COMMENT '账户说明',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  tenant_id varchar(255) DEFAULT 'super' COMMENT '租户ID',
  PRIMARY KEY (id)
) COMMENT='短信云账户';


CREATE TABLE msg_template (
  id varchar(48) NOT NULL COMMENT '主键',
  template_name varchar(255) NOT NULL COMMENT '模板名称',
  offical_id varchar(48) DEFAULT NULL COMMENT '模板官方ID',
  content varchar(1000) NOT NULL COMMENT '模板内容',
  notice_method varchar(1) NOT NULL COMMENT '通知方式(1：短信，2：邮件，3：微信，4：钉钉)',
  offical_account_id varchar(48) DEFAULT NULL COMMENT '公众(企业)号ID',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  tenant_id varchar(255) DEFAULT 'super' COMMENT '租户ID',
  PRIMARY KEY (id)
) COMMENT='消息模板';

CREATE TABLE msg_weixin_menu (
  id varchar(48) NOT NULL COMMENT '主键',
  offical_account_id varchar(500) NOT NULL COMMENT '公众号主键',
  app_id varchar(500) NOT NULL COMMENT '公众号AppId',
  menu_json varchar(4000) NOT NULL COMMENT '菜单JSON',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  tenant_id varchar(255) DEFAULT 'super' COMMENT '租户ID',
  PRIMARY KEY (id)
) COMMENT='微信公众号菜单';

CREATE TABLE msg_weixin_menu_reply (
  id varchar(48) NOT NULL COMMENT '主键',
  offical_account_id varchar(500) NOT NULL COMMENT '公众号主键',
  app_id varchar(500) NOT NULL COMMENT '公众号AppId',
  menu_btn_key varchar(255) NOT NULL COMMENT '菜单按钮Key',
  res_type varchar(2) NOT NULL DEFAULT '1' COMMENT '响应类型(1：文本回复)',
  reply_content varchar(4000) DEFAULT NULL COMMENT '回复内容',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  tenant_id varchar(255) DEFAULT 'super' COMMENT '租户ID',
  PRIMARY KEY (id)
) COMMENT='微信公众号菜单回复';


CREATE TABLE person_face_match_log (
  id varchar(48) NOT NULL COMMENT '主键',
  received_seq varchar(48) NOT NULL COMMENT '业务流水号',
  healthcode_log_id varchar(48) DEFAULT NULL COMMENT '健康码日志ID',
  unique_id varchar(48) DEFAULT NULL COMMENT '人员标识',
  dept_id bigint(20) DEFAULT NULL COMMENT '部门ID',
  channel_code varchar(255) DEFAULT NULL COMMENT '渠道编码',
  scene_image varchar(255) NOT NULL COMMENT '现场照路径',
  online_image varchar(255) DEFAULT NULL COMMENT '联网核查照路径',
  chip_image varchar(255) DEFAULT NULL COMMENT '芯片照路径',
  stock_image varchar(255) DEFAULT NULL COMMENT '底库照路径',
  scene_video varchar(255) DEFAULT NULL COMMENT '现场视频路径',
  scene_online_score double DEFAULT NULL COMMENT '现场照与联网核查照比对分值',
  scene_chip_score double DEFAULT NULL COMMENT '现场照与芯片照比对分值',
  scene_stock_score double DEFAULT NULL COMMENT '现场照与库底库照比对分值',
  online_chip_score double DEFAULT NULL COMMENT '联网核查照与芯片照比对分值',
  checklive_score double DEFAULT NULL COMMENT '检活分值',
  scene_online_result varchar(1) DEFAULT NULL COMMENT '现场照与联网核查照比对结果(0通过，1未通过)',
  scene_chip_result varchar(1) DEFAULT NULL COMMENT '现场照与芯片照比对结果(0通过，1未通过)',
  scene_stock_result varchar(1) DEFAULT NULL COMMENT '现场照与库底库照比对结果(0通过，1未通过)',
  online_chip_result varchar(1) DEFAULT NULL COMMENT '联网核查照与芯片照比对结果(0通过，1未通过)',
  checklive_result varchar(1) DEFAULT NULL COMMENT '现场照件检活结果(0通过，1未通过)',
  result varchar(1) DEFAULT NULL COMMENT '比对结果(0通过，1未通过)',
  temperature double DEFAULT NULL COMMENT '温度',
  temperature_floor double DEFAULT NULL COMMENT '温度阈值下限',
  temperature_top double DEFAULT NULL COMMENT '温度阈值上限',
  device_code varchar(255) DEFAULT NULL COMMENT '设备编码',
  device_name varchar(255) DEFAULT NULL COMMENT '设备名称',
  device_model varchar(255) DEFAULT NULL COMMENT '设备型号编码',
  device_ip varchar(255) DEFAULT NULL COMMENT '设备IP',
  device_longitude double DEFAULT NULL COMMENT '设备经度(东经)',
  device_dimension double DEFAULT NULL COMMENT '设备维度(北纬)',
  device_direction varchar(10) DEFAULT 'UNKNOWN' COMMENT '设备方向(IN:进，OUT:出，UNKNOWN：未知)',
  received_time datetime not null COMMENT '请求时间',
  time_used bigint(20) DEFAULT NULL COMMENT '耗时(ms)',
  server_id varchar(255) DEFAULT NULL COMMENT '服务器标识',
  vendor_code varchar(255) DEFAULT NULL COMMENT '厂商',
  algs_version varchar(255) DEFAULT NULL COMMENT '算法版本',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  batch_date datetime DEFAULT NULL COMMENT '定时任务执行时间',
  tenant_id varchar(255) DEFAULT 'super' COMMENT '租户ID',
  PRIMARY KEY ( id, received_time ) 
) PARTITION BY RANGE (TO_DAYS(received_time)) (
   PARTITION p0 VALUES LESS THAN (0)
) ;
ALTER TABLE person_face_match_log COMMENT='人员人脸比对日志表';
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL - 2 DAY), @schemaName, 'person_face_match_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL - 1 DAY), @schemaName, 'person_face_match_log','%Y%m%d');
CALL sp_create_partition(sysdate(), @schemaName, 'person_face_match_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 1 DAY), @schemaName, 'person_face_match_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 2 DAY), @schemaName, 'person_face_match_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 3 DAY), @schemaName, 'person_face_match_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 4 DAY), @schemaName, 'person_face_match_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 5 DAY), @schemaName, 'person_face_match_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 6 DAY), @schemaName, 'person_face_match_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 7 DAY), @schemaName, 'person_face_match_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 8 DAY), @schemaName, 'person_face_match_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 9 DAY), @schemaName, 'person_face_match_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 10 DAY), @schemaName, 'person_face_match_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 11 DAY), @schemaName, 'person_face_match_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 12 DAY), @schemaName, 'person_face_match_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 13 DAY), @schemaName, 'person_face_match_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 14 DAY), @schemaName, 'person_face_match_log','%Y%m%d');

CREATE TABLE person_face_search_log (
  id varchar(48) NOT NULL COMMENT '主键',
  received_seq varchar(48) NOT NULL COMMENT '业务流水号',
  healthcode_log_id varchar(48) DEFAULT NULL COMMENT '健康码日志ID',
  scene_type varchar(1) NOT NULL COMMENT '类型(数据字典 1：基础入库1:N，2：比对接口1:N，3：日志回传)',
  unique_id varchar(48) DEFAULT NULL COMMENT '人员标识',
  dept_id bigint(20) DEFAULT NULL COMMENT '部门ID',
  channel_code varchar(255) DEFAULT NULL COMMENT '渠道编码',
  sub_treasury_code varchar(255) DEFAULT NULL COMMENT '分库编码',
  sub_treasury_name varchar(255) DEFAULT NULL COMMENT '分库名称',
  scene_image varchar(255) NOT NULL COMMENT '现场照路径',
  stock_image varchar(255) DEFAULT NULL COMMENT '底库照路径',
  take_photo1 varchar(255) DEFAULT NULL COMMENT '抓拍图1路径',
  take_photo2 varchar(255) DEFAULT NULL COMMENT '抓拍图2路径',
  scene_video varchar(255) DEFAULT NULL COMMENT '现场视频路径',
  scene_stock_score double DEFAULT NULL COMMENT '分值',
  checklive_score double DEFAULT NULL COMMENT '现场照检活分值',
  checklive_result varchar(1) DEFAULT NULL COMMENT '现场照检活结果(0通过，1未通过)',
  temperature double DEFAULT NULL COMMENT '温度',
  temperature_floor double DEFAULT NULL COMMENT '温度阈值下限',
  temperature_top double DEFAULT NULL COMMENT '温度阈值上限制',
  bio_recognized char(1) DEFAULT 'Y' COMMENT '是否生物识别(Y:是，N：否)',
  result varchar(1) DEFAULT NULL COMMENT '结果(0通过，1未通过)',
  device_code varchar(255) DEFAULT NULL COMMENT '设备编码',
  device_name varchar(255) DEFAULT NULL COMMENT '设备名称',
  device_model varchar(255) DEFAULT NULL COMMENT '设备型号编码',
  device_ip varchar(255) DEFAULT NULL COMMENT '设备IP',
  device_longitude double DEFAULT NULL COMMENT '设备经度(东经)',
  device_dimension double DEFAULT NULL COMMENT '设备维度(北纬)',
  device_direction varchar(10) DEFAULT 'UNKNOWN' COMMENT '设备方向(IN:进，OUT:出，UNKNOWN：未知)',
  received_time datetime NOT NULL COMMENT '请求时间',
  time_used bigint(20) DEFAULT NULL COMMENT '耗时(ms)',
  server_id varchar(255) DEFAULT NULL COMMENT '服务器标识',
  vendor_code varchar(255) DEFAULT NULL COMMENT '厂商',
  algs_version varchar(255) DEFAULT NULL COMMENT '算法版本',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  batch_date datetime DEFAULT NULL COMMENT '定时任务执行时间',
  tenant_id varchar(255) DEFAULT 'super' COMMENT '租户ID',
  PRIMARY KEY ( id, received_time ) 
) PARTITION BY RANGE (TO_DAYS(received_time)) (
   PARTITION p0 VALUES LESS THAN (0)
) ;
ALTER TABLE person_face_search_log COMMENT='人员人脸搜索日志表';
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL - 2 DAY), @schemaName, 'person_face_search_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL - 1 DAY), @schemaName, 'person_face_search_log','%Y%m%d');
CALL sp_create_partition(sysdate(), @schemaName, 'person_face_search_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 1 DAY), @schemaName, 'person_face_search_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 2 DAY), @schemaName, 'person_face_search_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 3 DAY), @schemaName, 'person_face_search_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 4 DAY), @schemaName, 'person_face_search_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 5 DAY), @schemaName, 'person_face_search_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 6 DAY), @schemaName, 'person_face_search_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 7 DAY), @schemaName, 'person_face_search_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 8 DAY), @schemaName, 'person_face_search_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 9 DAY), @schemaName, 'person_face_search_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 10 DAY), @schemaName, 'person_face_search_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 11 DAY), @schemaName, 'person_face_search_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 12 DAY), @schemaName, 'person_face_search_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 13 DAY), @schemaName, 'person_face_search_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 14 DAY), @schemaName, 'person_face_search_log','%Y%m%d');


CREATE TABLE person_finger_match_log (
  id varchar(48) NOT NULL COMMENT '主键',
  received_seq varchar(48) NOT NULL COMMENT '业务流水号',
  unique_id varchar(48) DEFAULT NULL COMMENT '人员标识',
  dept_id bigint(20) DEFAULT NULL COMMENT '部门ID',
  channel_code varchar(255) DEFAULT NULL COMMENT '渠道编码',
  finger_no varchar(2) DEFAULT NULL COMMENT '手指编码',
  scene_image varchar(255) NOT NULL COMMENT '现场照路径',
  stock_image varchar(255) DEFAULT NULL COMMENT '底库照路径',
  scene_stock_score double DEFAULT NULL COMMENT '现场照与库底库照比对分值',
  scene_stock_result varchar(1) DEFAULT NULL COMMENT '现场照与库底库照比对结果(0通过，1未通过)',
  result varchar(1) DEFAULT NULL COMMENT '比对结果(0通过，1未通过)',
  received_time datetime not NULL COMMENT '请求时间',
  time_used bigint(20) DEFAULT NULL COMMENT '耗时(ms)',
  server_id varchar(255) DEFAULT NULL COMMENT '服务器标识',
  vendor_code varchar(255) DEFAULT NULL COMMENT '厂商',
  algs_version varchar(255) DEFAULT NULL COMMENT '算法版本',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  batch_date datetime DEFAULT NULL COMMENT '定时任务执行时间',
  tenant_id varchar(255) DEFAULT 'super' COMMENT '租户ID',
  PRIMARY KEY ( id, received_time ) 
) PARTITION BY RANGE (TO_DAYS(received_time)) (
   PARTITION p0 VALUES LESS THAN (0)
) ;
ALTER TABLE person_finger_match_log COMMENT='人员指纹比对日志表';
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL - 2 DAY), @schemaName, 'person_finger_match_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL - 1 DAY), @schemaName, 'person_finger_match_log','%Y%m%d');
CALL sp_create_partition(sysdate(), @schemaName, 'person_finger_match_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 1 DAY), @schemaName, 'person_finger_match_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 2 DAY), @schemaName, 'person_finger_match_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 3 DAY), @schemaName, 'person_finger_match_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 4 DAY), @schemaName, 'person_finger_match_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 5 DAY), @schemaName, 'person_finger_match_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 6 DAY), @schemaName, 'person_finger_match_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 7 DAY), @schemaName, 'person_finger_match_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 8 DAY), @schemaName, 'person_finger_match_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 9 DAY), @schemaName, 'person_finger_match_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 10 DAY), @schemaName, 'person_finger_match_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 11 DAY), @schemaName, 'person_finger_match_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 12 DAY), @schemaName, 'person_finger_match_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 13 DAY), @schemaName, 'person_finger_match_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 14 DAY), @schemaName, 'person_finger_match_log','%Y%m%d');


CREATE TABLE person_finger_search_log (
  id varchar(48) NOT NULL COMMENT '主键',
  received_seq varchar(48) NOT NULL COMMENT '业务流水号',
  scene_type varchar(1) NOT NULL COMMENT '类型(数据字典 1：基础入库1:N，2：比对接口1:N，3：日志回传)',
  unique_id varchar(48) DEFAULT NULL COMMENT '人员标识',
  dept_id bigint(20) DEFAULT NULL COMMENT '部门ID',
  channel_code varchar(255) DEFAULT NULL COMMENT '渠道编码',
  sub_treasury_code varchar(255) DEFAULT NULL COMMENT '分库编码',
  sub_treasury_name varchar(255) DEFAULT NULL COMMENT '分库名称',
  finger_no varchar(2) DEFAULT NULL COMMENT '手指编码',
  scene_image varchar(255) NOT NULL COMMENT '现场照路径',
  stock_image varchar(255) DEFAULT NULL COMMENT '底库照路径',
  scene_stock_score double DEFAULT NULL COMMENT '分值',
  result varchar(1) DEFAULT NULL COMMENT '结果(0通过，1未通过)',
  received_time datetime NOT NULL COMMENT '请求时间',
  time_used bigint(20) DEFAULT NULL COMMENT '耗时(ms)',
  server_id varchar(255) DEFAULT NULL COMMENT '服务器标识',
  vendor_code varchar(255) DEFAULT NULL COMMENT '厂商',
  algs_version varchar(255) DEFAULT NULL COMMENT '算法版本',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  batch_date datetime DEFAULT NULL COMMENT '定时任务执行时间',
  tenant_id varchar(255) DEFAULT 'super' COMMENT '租户ID',
  PRIMARY KEY ( id, received_time ) 
) PARTITION BY RANGE (TO_DAYS(received_time)) (
   PARTITION p0 VALUES LESS THAN (0)
) ;
ALTER TABLE person_finger_search_log COMMENT='人员指纹搜索日志表';
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL - 2 DAY), @schemaName, 'person_finger_search_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL - 1 DAY), @schemaName, 'person_finger_search_log','%Y%m%d');
CALL sp_create_partition(sysdate(), @schemaName, 'person_finger_search_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 1 DAY), @schemaName, 'person_finger_search_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 2 DAY), @schemaName, 'person_finger_search_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 3 DAY), @schemaName, 'person_finger_search_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 4 DAY), @schemaName, 'person_finger_search_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 5 DAY), @schemaName, 'person_finger_search_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 6 DAY), @schemaName, 'person_finger_search_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 7 DAY), @schemaName, 'person_finger_search_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 8 DAY), @schemaName, 'person_finger_search_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 9 DAY), @schemaName, 'person_finger_search_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 10 DAY), @schemaName, 'person_finger_search_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 11 DAY), @schemaName, 'person_finger_search_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 12 DAY), @schemaName, 'person_finger_search_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 13 DAY), @schemaName, 'person_finger_search_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 14 DAY), @schemaName, 'person_finger_search_log','%Y%m%d');


CREATE TABLE person_iris_match_log (
  id varchar(48) NOT NULL COMMENT '主键',
  received_seq varchar(48) NOT NULL COMMENT '业务流水号',
  unique_id varchar(48) DEFAULT NULL COMMENT '人员标识',
  dept_id bigint(20) DEFAULT NULL COMMENT '部门ID',
  channel_code varchar(255) DEFAULT NULL COMMENT '渠道编码',
  scene_image varchar(255) DEFAULT NULL COMMENT '现场照路径',
  stock_image varchar(255) DEFAULT NULL COMMENT '底库照路径',
  scene_stock_score double DEFAULT NULL COMMENT '现场照与底库照比对分值',
  scene_stock_result varchar(1) DEFAULT NULL COMMENT '现场照与底库照比对结果(0通过，1未通过)',
  result varchar(1) DEFAULT NULL COMMENT '比对结果(0通过，1未通过)',
  received_time datetime NOT NULL COMMENT '请求时间',
  time_used bigint(20) DEFAULT NULL COMMENT '耗时(ms)',
  server_id varchar(255) DEFAULT NULL COMMENT '服务器标识',
  vendor_code varchar(255) DEFAULT NULL COMMENT '厂商',
  algs_version varchar(255) DEFAULT NULL COMMENT '算法版本',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  batch_date datetime DEFAULT NULL COMMENT '定时任务执行时间',
  tenant_id varchar(255) DEFAULT 'super' COMMENT '租户ID',
  PRIMARY KEY ( id, received_time ) 
)  PARTITION BY RANGE (TO_DAYS(received_time)) (
   PARTITION p0 VALUES LESS THAN (0)
) ;
ALTER TABLE person_iris_match_log COMMENT='人员虹膜比对日志表';
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL - 2 DAY), @schemaName, 'person_iris_match_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL - 1 DAY), @schemaName, 'person_iris_match_log','%Y%m%d');
CALL sp_create_partition(sysdate(), @schemaName, 'person_iris_match_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 1 DAY), @schemaName, 'person_iris_match_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 2 DAY), @schemaName, 'person_iris_match_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 3 DAY), @schemaName, 'person_iris_match_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 4 DAY), @schemaName, 'person_iris_match_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 5 DAY), @schemaName, 'person_iris_match_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 6 DAY), @schemaName, 'person_iris_match_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 7 DAY), @schemaName, 'person_iris_match_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 8 DAY), @schemaName, 'person_iris_match_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 9 DAY), @schemaName, 'person_iris_match_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 10 DAY), @schemaName, 'person_iris_match_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 11 DAY), @schemaName, 'person_iris_match_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 12 DAY), @schemaName, 'person_iris_match_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 13 DAY), @schemaName, 'person_iris_match_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 14 DAY), @schemaName, 'person_iris_match_log','%Y%m%d');


CREATE TABLE person_iris_search_log (
  id varchar(48) NOT NULL COMMENT '主键',
  received_seq varchar(48) NOT NULL COMMENT '业务流水号',
  scene_type varchar(1) NOT NULL COMMENT '类型(数据字典 1：基础信息入库1:N，2：比对搜索接口1:N)',
  unique_id varchar(48) DEFAULT NULL COMMENT '人员标识',
  dept_id bigint(20) DEFAULT NULL COMMENT '部门ID',
  channel_code varchar(255) DEFAULT NULL COMMENT '渠道编码',
  sub_treasury_code varchar(255) DEFAULT NULL COMMENT '分库编码',
  sub_treasury_name varchar(255) DEFAULT NULL COMMENT '分库名称',
  scene_image varchar(255) DEFAULT NULL COMMENT '现场照路径',
  stock_image varchar(255) DEFAULT NULL COMMENT '底库照路径',
  scene_stock_score double DEFAULT NULL COMMENT '现场照与底库照比对分值',
  result varchar(1) DEFAULT NULL COMMENT '结果(0通过，1未通过)',
  received_time datetime NOT NULL COMMENT '请求时间',
  time_used bigint(20) DEFAULT NULL COMMENT '耗时(ms)',
  server_id varchar(255) DEFAULT NULL COMMENT '服务器标识',
  vendor_code varchar(255) DEFAULT NULL COMMENT '厂商',
  algs_version varchar(255) DEFAULT NULL COMMENT '算法版本',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  batch_date datetime DEFAULT NULL COMMENT '定时任务执行时间',
  tenant_id varchar(255) DEFAULT 'super' COMMENT '租户ID',
  PRIMARY KEY ( id, received_time ) 
)  PARTITION BY RANGE (TO_DAYS(received_time)) (
   PARTITION p0 VALUES LESS THAN (0)
) ;
ALTER TABLE person_iris_search_log COMMENT='人员虹膜搜索日志表';
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL - 2 DAY), @schemaName, 'person_iris_search_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL - 1 DAY), @schemaName, 'person_iris_search_log','%Y%m%d');
CALL sp_create_partition(sysdate(), @schemaName, 'person_iris_search_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 1 DAY), @schemaName, 'person_iris_search_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 2 DAY), @schemaName, 'person_iris_search_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 3 DAY), @schemaName, 'person_iris_search_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 4 DAY), @schemaName, 'person_iris_search_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 5 DAY), @schemaName, 'person_iris_search_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 6 DAY), @schemaName, 'person_iris_search_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 7 DAY), @schemaName, 'person_iris_search_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 8 DAY), @schemaName, 'person_iris_search_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 9 DAY), @schemaName, 'person_iris_search_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 10 DAY), @schemaName, 'person_iris_search_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 11 DAY), @schemaName, 'person_iris_search_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 12 DAY), @schemaName, 'person_iris_search_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 13 DAY), @schemaName, 'person_iris_search_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 14 DAY), @schemaName, 'person_iris_search_log','%Y%m%d');

CREATE TABLE person_faceiris_search_log (
  id varchar(48) NOT NULL COMMENT '主键',
  received_seq varchar(48) NOT NULL COMMENT '业务流水号',
  healthcode_log_id varchar(48) DEFAULT NULL COMMENT '健康码日志ID',
  scene_type char(1) NOT NULL COMMENT '类型(数据字典 1：基础信息入库1:N，2：比对搜索接口1:N)',
  unique_id varchar(50) DEFAULT NULL COMMENT '人员唯一编号',
  dept_id varchar(50) DEFAULT NULL COMMENT '部门ID',
  channel_code varchar(255) DEFAULT NULL COMMENT '渠道编码',
  sub_treasury_code varchar(255) DEFAULT NULL COMMENT '分库编码',
  scene_face_image varchar(255) DEFAULT NULL COMMENT '人脸现场照路径',
  scene_iris_image varchar(255) DEFAULT NULL COMMENT '虹膜现场照路径',
  scene_face_score double DEFAULT NULL COMMENT '人脸比对分数',
  scene_iris_score double DEFAULT NULL COMMENT '虹膜比对分数',
  stock_face_image varchar(255) DEFAULT NULL COMMENT '底库人脸照路径',
  stock_iris_image varchar(255) DEFAULT NULL COMMENT '底库虹膜照路径',
  match_mode char(1) DEFAULT NULL COMMENT '比对模式字典matchMode',
  match_score double DEFAULT NULL COMMENT '比对分数，比对通过的分数或者融合分数',
  checklive_score double DEFAULT NULL COMMENT '现场照检活分值',
  checklive_result varchar(1) DEFAULT NULL COMMENT '现场照检活结果(0通过，1未通过)',
  result char(1) DEFAULT NULL COMMENT '结果(0通过，1未通过)',
  received_time datetime NOT NULL COMMENT '交易时间',
  time_used bigint(20) DEFAULT NULL COMMENT '比对用时ms',
  face_time_used bigint(20) DEFAULT NULL COMMENT '人脸比对用时ms',
  iris_time_used bigint(20) DEFAULT NULL COMMENT '虹膜比对用时ms',
  temperature double DEFAULT NULL COMMENT '温度',
  temperature_floor double DEFAULT NULL COMMENT '温度阈值下限',
  temperature_top double DEFAULT NULL COMMENT '温度阈值上限制',
  device_sn varchar(50) DEFAULT NULL COMMENT '设备标识',
  device_model varchar(255) DEFAULT NULL COMMENT '设备型号编码',
  device_name varchar(255) DEFAULT NULL COMMENT '设备名称',
  device_ip varchar(255) DEFAULT NULL COMMENT '设备IP',
  device_longitude double DEFAULT NULL COMMENT '设备经度(东经)',
  device_dimension double DEFAULT NULL COMMENT '设备维度(北纬)',
  device_direction varchar(10) DEFAULT 'UNKNOWN' COMMENT '设备方向(IN:进，OUT:出，UNKNOWN：未知)',
  server_id varchar(255) DEFAULT NULL COMMENT '服务器标识',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  batch_date datetime DEFAULT NULL COMMENT '定时任务执行时间',
  remark varchar(255) DEFAULT NULL COMMENT '备注',
  tenant_id varchar(255) DEFAULT 'super' COMMENT '租户ID',
  PRIMARY KEY (id,received_time)
) PARTITION BY RANGE (TO_DAYS(received_time)) (
   PARTITION p0 VALUES LESS THAN (0)
) ;
ALTER TABLE person_faceiris_search_log COMMENT='人脸虹膜多模态搜索日志表';
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL - 2 DAY), @schemaName, 'person_faceiris_search_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL - 1 DAY), @schemaName, 'person_faceiris_search_log','%Y%m%d');
CALL sp_create_partition(sysdate(), @schemaName, 'person_faceiris_search_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 1 DAY), @schemaName, 'person_faceiris_search_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 2 DAY), @schemaName, 'person_faceiris_search_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 3 DAY), @schemaName, 'person_faceiris_search_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 4 DAY), @schemaName, 'person_faceiris_search_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 5 DAY), @schemaName, 'person_faceiris_search_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 6 DAY), @schemaName, 'person_faceiris_search_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 7 DAY), @schemaName, 'person_faceiris_search_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 8 DAY), @schemaName, 'person_faceiris_search_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 9 DAY), @schemaName, 'person_faceiris_search_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 10 DAY), @schemaName, 'person_faceiris_search_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 11 DAY), @schemaName, 'person_faceiris_search_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 12 DAY), @schemaName, 'person_faceiris_search_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 13 DAY), @schemaName, 'person_faceiris_search_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 14 DAY), @schemaName, 'person_faceiris_search_log','%Y%m%d');


CREATE TABLE person_health_code_log (
  id varchar(50) NOT NULL COMMENT '主键',
  received_seq varchar(50) NOT NULL COMMENT '业务流水号',
  unique_id varchar(50) COMMENT '人员唯一标识，对应person_cert',
  received_time datetime NOT NULL COMMENT '请求时间',
  result char(1) DEFAULT NULL COMMENT '结果(0通过，1未通过)',
  message varchar(255) DEFAULT NULL COMMENT '结果描述',
  device_code varchar(50) DEFAULT NULL COMMENT '设备编号',
  temperature varchar(10) DEFAULT NULL COMMENT '人脸测温温度',
  car_no varchar(10) DEFAULT NULL COMMENT '车牌号',
  region varchar(50) DEFAULT NULL COMMENT '区域（调用哪个健康码接口）',
  time_used bigint(20) DEFAULT NULL COMMENT '用时ms',
  create_time datetime NOT NULL COMMENT '创建时间',
  batch_date datetime DEFAULT NULL COMMENT '定时任务执行时间',
  tenant_id varchar(255) DEFAULT 'super' COMMENT '租户ID',
  PRIMARY KEY (id,received_time)
) PARTITION BY RANGE (TO_DAYS(received_time)) (
   PARTITION p0 VALUES LESS THAN (0)
) ; 
ALTER TABLE person_health_code_log COMMENT='健康码请求记录';
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL - 2 DAY), @schemaName, 'person_health_code_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL - 1 DAY), @schemaName, 'person_health_code_log','%Y%m%d');
CALL sp_create_partition(sysdate(), @schemaName, 'person_health_code_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 1 DAY), @schemaName, 'person_health_code_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 2 DAY), @schemaName, 'person_health_code_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 3 DAY), @schemaName, 'person_health_code_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 4 DAY), @schemaName, 'person_health_code_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 5 DAY), @schemaName, 'person_health_code_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 6 DAY), @schemaName, 'person_health_code_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 7 DAY), @schemaName, 'person_health_code_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 8 DAY), @schemaName, 'person_health_code_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 9 DAY), @schemaName, 'person_health_code_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 10 DAY), @schemaName, 'person_health_code_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 11 DAY), @schemaName, 'person_health_code_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 12 DAY), @schemaName, 'person_health_code_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 13 DAY), @schemaName, 'person_health_code_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 14 DAY), @schemaName, 'person_health_code_log','%Y%m%d');

CREATE TABLE trade_req_record (
  id varchar(48) NOT NULL COMMENT '主键',
  trans_code varchar(48) DEFAULT NULL COMMENT '交易码',
  trans_title varchar(48) DEFAULT NULL COMMENT '交易标题',
  received_time datetime NOT NULL COMMENT '请求时间',
  client_ip varchar(48) DEFAULT NULL COMMENT '客户端IP',
  send_time datetime NOT NULL COMMENT '响应时间',
  time_used bigint(20) NOT NULL COMMENT '耗时(ms)',
  status_code varchar(48) NOT NULL DEFAULT '0' COMMENT '状态码',
  trans_url varchar(255) DEFAULT NULL COMMENT '交易请求路径',
  class_method varchar(255) DEFAULT NULL COMMENT '处理方法',
  channel_code varchar(255) DEFAULT NULL COMMENT '渠道编码',
  tenant_id varchar(255) DEFAULT 'super' COMMENT '租户ID',
  detail_file_path varchar(255) DEFAULT NULL  COMMENT '报文详细文件',
  PRIMARY KEY (id,received_time)
) PARTITION BY RANGE (TO_DAYS(received_time)) (
   PARTITION p0 VALUES LESS THAN (0)
) ; 
ALTER TABLE trade_req_record COMMENT='接口交易请求记录';
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL - 2 DAY), @schemaName, 'trade_req_record','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL - 1 DAY), @schemaName, 'trade_req_record','%Y%m%d');
CALL sp_create_partition(sysdate(), @schemaName, 'trade_req_record','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 1 DAY), @schemaName, 'trade_req_record','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 2 DAY), @schemaName, 'trade_req_record','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 3 DAY), @schemaName, 'trade_req_record','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 4 DAY), @schemaName, 'trade_req_record','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 5 DAY), @schemaName, 'trade_req_record','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 6 DAY), @schemaName, 'trade_req_record','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 7 DAY), @schemaName, 'trade_req_record','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 8 DAY), @schemaName, 'trade_req_record','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 9 DAY), @schemaName, 'trade_req_record','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 10 DAY), @schemaName, 'trade_req_record','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 11 DAY), @schemaName, 'trade_req_record','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 12 DAY), @schemaName, 'trade_req_record','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 13 DAY), @schemaName, 'trade_req_record','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 14 DAY), @schemaName, 'trade_req_record','%Y%m%d');

CREATE TABLE sys_user_face (
  id varchar(48) NOT NULL COMMENT '主键',
  user_id bigint(20) NOT NULL COMMENT '用户主键(关联用户表)',
  feature varchar(4000) NOT NULL COMMENT '人脸特征',
  feature_md5 varchar(255) DEFAULT NULL COMMENT '人脸特征MD5',
  quality_score double DEFAULT NULL COMMENT '图像质量得分',
  image_url varchar(255) NOT NULL COMMENT '人脸图像路径',
  vendor_code varchar(48) DEFAULT NULL COMMENT '厂商',
  algs_version varchar(48) DEFAULT NULL COMMENT '算法版本',
  status char(1) NOT NULL DEFAULT '0' COMMENT '状态：0-有效  1:无效',
  remark varchar(255) DEFAULT NULL COMMENT '备注',
  create_by varchar(64) DEFAULT NULL COMMENT '创建人',
  update_by varchar(64) DEFAULT NULL COMMENT '修改人',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_time datetime DEFAULT NULL COMMENT '修改时间',
  tenant_id varchar(255) DEFAULT 'super' COMMENT '租户ID',
  PRIMARY KEY (id)
) COMMENT='用户人脸信息';


CREATE TABLE sys_user_finger (
  id varchar(48) NOT NULL COMMENT '主键',
  user_id bigint(20) NOT NULL COMMENT '用户主键(关联用户表)',
  finger_no varchar(2) NOT NULL COMMENT '手指编号',
  feature varchar(4000) NOT NULL COMMENT '指纹特征',
  feature_md5 varchar(255) DEFAULT NULL COMMENT '指纹特征MD5',
  quality_score double DEFAULT NULL COMMENT '图像质量得分',
  image_url varchar(255) DEFAULT NULL COMMENT '指纹图像路径',
  vendor_code varchar(48) DEFAULT NULL COMMENT '厂商',
  algs_version varchar(48) DEFAULT NULL COMMENT '算法版本',
  status char(1) NOT NULL DEFAULT '0' COMMENT '状态：0-有效  1:无效',
  remark varchar(255) DEFAULT NULL COMMENT '备注',
  create_by varchar(64) DEFAULT NULL COMMENT '创建人',
  update_by varchar(64) DEFAULT NULL COMMENT '修改人',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_time datetime DEFAULT NULL COMMENT '修改时间',
  tenant_id varchar(255) DEFAULT 'super' COMMENT '租户ID',
  PRIMARY KEY (id)
) COMMENT='用户指纹信息';

CREATE TABLE sys_user_iris (
  id varchar(48) NOT NULL COMMENT '主键',
  user_id bigint(20) NOT NULL COMMENT '用户主键(关联用户信息表)',
  feature longtext NOT NULL COMMENT '虹膜特征',
  feature_md5 varchar(255) DEFAULT NULL COMMENT '虹膜特征MD5',
  quality_score double DEFAULT NULL COMMENT '图像质量得分',
  image_url varchar(255) NOT NULL COMMENT '虹膜图像路径',
  vendor_code varchar(48) DEFAULT NULL COMMENT '厂商',
  algs_version varchar(48) DEFAULT NULL COMMENT '算法版本',
  status char(1) NOT NULL DEFAULT '0' COMMENT '状态：0-有效  1:无效',
  remark varchar(255) DEFAULT NULL COMMENT '备注',
  create_by varchar(64) DEFAULT NULL COMMENT '创建人',
  update_by varchar(64) DEFAULT NULL COMMENT '修改人',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_time datetime DEFAULT NULL COMMENT '修改时间',
  tenant_id varchar(255) DEFAULT 'super' COMMENT '租户ID',
  PRIMARY KEY (id)
) COMMENT='用户虹膜信息';


CREATE TABLE sys_user_iris_face (
  id varchar(48) NOT NULL COMMENT '主键',
  user_id bigint(20) NOT NULL COMMENT '用户主键',
  fusion_feature longtext COMMENT '融合特征',
  face_feature longtext COMMENT '人脸特征',
  iris_feature longtext COMMENT '虹膜特征',
  fusion_feature_md5 varchar(255) DEFAULT NULL COMMENT '融合特征md5',
  face_feature_md5 varchar(255) DEFAULT NULL COMMENT '人脸特征md5',
  face_image_url varchar(255) DEFAULT NULL COMMENT '人脸照片路径',
  face_quality double DEFAULT NULL COMMENT '人脸质量分数',
  iris_feature_md5 varchar(255) DEFAULT NULL COMMENT '虹膜特征md5',
  iris_image_url varchar(255) DEFAULT NULL COMMENT '虹膜照片路径',
  iris_quality double DEFAULT NULL COMMENT '虹膜质量分数',
  status char(1) NOT NULL DEFAULT '0' COMMENT '状态：0-有效  1-无效',
  create_by varchar(48) DEFAULT NULL COMMENT '创建人',
  update_by varchar(48) DEFAULT NULL COMMENT '修改人',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_time datetime DEFAULT NULL COMMENT '修改时间',
  tenant_id varchar(255) DEFAULT 'super' COMMENT '租户ID',
  PRIMARY KEY (id)
) COMMENT='用户虹膜人脸多模态信息';


CREATE TABLE ocr_bank_card_log (
  id varchar(48) NOT NULL COMMENT '主键',
  received_seq varchar(48) NOT NULL COMMENT '业务流水号',
  received_time datetime NOT NULL COMMENT '请求时间',
  type varchar(12) DEFAULT NULL COMMENT '图片类型，如果是图睿算法库，此项填Unknown',
  type_id varchar(12) DEFAULT NULL COMMENT '卡证类型，如果是文通算法库，此项填0 ',
  channel_code varchar(255) DEFAULT NULL COMMENT '渠道编码',
  scene_image_url varchar(255) NOT NULL COMMENT '待识别证件照存储路径',
  scene_image_name varchar(255) DEFAULT NULL COMMENT '待识别证件照名称',
  bank_card_expiry_date varchar(20) DEFAULT NULL COMMENT '有效期',
  bank_card_bank_name varchar(255) DEFAULT NULL COMMENT '银行名称',
  bank_card_bank_code varchar(255) DEFAULT NULL COMMENT '银行编号',
  bank_card_name varchar(20) DEFAULT NULL COMMENT '卡名',
  bank_card_type varchar(20) DEFAULT NULL COMMENT '卡类型',
  bank_card_number varchar(50) DEFAULT NULL COMMENT '卡号',
  result char(1) DEFAULT NULL COMMENT '识别结果',
  tenant_id varchar(255) DEFAULT 'super' COMMENT '租户id',
  PRIMARY KEY (id)
) COMMENT='银行卡OCR识别记录';

CREATE TABLE ocr_busi_lic_log (
  id varchar(48) NOT NULL COMMENT '主键',
  received_seq varchar(48) NOT NULL COMMENT '业务流水号',
  received_time datetime NOT NULL COMMENT '请求时间',
  type varchar(12) DEFAULT NULL COMMENT '图片类型，如果是图睿算法库，此项填Unknown',
  type_id varchar(12) DEFAULT NULL COMMENT '卡证类型，如果是文通算法库，此项填0 ',
  channel_code varchar(255) DEFAULT NULL COMMENT '渠道编码',
  scene_image_url varchar(255) NOT NULL COMMENT '待识别证件照存储路径',
  scene_image_name varchar(255) DEFAULT NULL COMMENT '待识别证件照名称',
  busi_lic_registered_no varchar(255) DEFAULT NULL COMMENT '注册号',
  busi_lic_origination_no varchar(255) DEFAULT NULL COMMENT '营业执照签发号',
  busi_lic_tax_no varchar(255) DEFAULT NULL COMMENT '营业执照税号',
  busi_lic_social_insurance_no varchar(255) DEFAULT NULL COMMENT '营业执照社会保险证号',
  busi_lic_statistic_no varchar(255) DEFAULT NULL COMMENT '营业执照统计号',
  busi_lic_name varchar(255) DEFAULT NULL COMMENT '公司名称',
  busi_lic_type varchar(255) DEFAULT NULL COMMENT '公司类型',
  busi_lic_address varchar(255) DEFAULT NULL COMMENT '公司地址',
  busi_lic_owner varchar(20) DEFAULT NULL COMMENT '法定代表人',
  busi_lic_form varchar(255) DEFAULT NULL COMMENT '组成形式',
  busi_lic_registered_capital varchar(20) DEFAULT NULL COMMENT '注册资金',
  busi_lic_registry_date varchar(20) DEFAULT NULL COMMENT '注册日期',
  busi_lic_expiry_date varchar(20) DEFAULT NULL COMMENT '过期日期',
  busi_lic_scope varchar(255) DEFAULT NULL COMMENT '经营范围',
  busi_lic_issure_authority varchar(255) DEFAULT NULL COMMENT '签发机关',
  busi_lic_issure_date varchar(20) DEFAULT NULL COMMENT '签发日期',
  busi_lic_qr_code varchar(255) DEFAULT NULL COMMENT '营业执照二维码',
  result char(1) DEFAULT NULL COMMENT '识别结果',
  tenant_id varchar(255) DEFAULT 'super',
  PRIMARY KEY (id)
) COMMENT='营业执照OCR识别记录';


CREATE TABLE ocr_driver_lic_log (
  id varchar(48) NOT NULL COMMENT '主键',
  received_seq varchar(48) NOT NULL COMMENT '业务流水号',
  received_time datetime NOT NULL COMMENT '请求时间',
  type varchar(12) DEFAULT NULL COMMENT '图片类型，如果是图睿算法库，此项填Unknown',
  type_id varchar(12) DEFAULT NULL COMMENT '卡证类型，如果是文通算法库，此项填0',
  ocr_firm_type varchar(12) DEFAULT NULL COMMENT '算法类型',
  channel_code varchar(255) DEFAULT NULL COMMENT '渠道编码',
  scene_image_url varchar(255) NOT NULL COMMENT '待识别证件照存储路径',
  scene_image_name varchar(255) DEFAULT NULL COMMENT '待识别证件照名称',
  driver_lic_number varchar(50) DEFAULT NULL COMMENT '驾驶证件号',
  driver_lic_head_image varchar(255) DEFAULT NULL COMMENT '证件头像',
  driver_lic_address varchar(255) DEFAULT NULL COMMENT '驾驶证居住地址',
  driver_lic_birth varchar(255) DEFAULT NULL COMMENT '驾驶证出生日期',
  driver_lic_gender varchar(20) DEFAULT NULL COMMENT '驾驶证性别',
  driver_lic_name varchar(20) DEFAULT NULL COMMENT '驾驶证所有人姓名',
  driver_lic_driver_type varchar(20) DEFAULT NULL COMMENT '驾驶证准驾车型',
  driver_lic_first_issue varchar(20) DEFAULT NULL COMMENT '驾驶证初次领证日期',
  driver_lic_valid_from varchar(255) DEFAULT NULL COMMENT '有效期开始时间',
  driver_lic_valid_for varchar(255) DEFAULT NULL COMMENT '有效期区间',
  driver_lic_expiry_date varchar(20) DEFAULT NULL COMMENT '失效日期',
  result char(1) DEFAULT NULL COMMENT '识别结果',
  tenant_id varchar(255) DEFAULT 'super',
  PRIMARY KEY (id)
) COMMENT='驾驶证OCR识别记录';

CREATE TABLE ocr_driving_lic_log (
  id varchar(48) NOT NULL COMMENT '主键',
  received_seq varchar(48) NOT NULL COMMENT '业务流水号',
  received_time datetime NOT NULL COMMENT '请求时间',
  type varchar(12) DEFAULT NULL COMMENT '图片类型，如果是图睿算法库，此项填Unknown',
  type_id varchar(12) DEFAULT NULL COMMENT '卡证类型，如果是文通算法库，此项填0 ',
  channel_code varchar(255) DEFAULT NULL COMMENT '渠道编码',
  scene_image_url varchar(255) NOT NULL COMMENT '待识别证件照存储路径',
  scene_image_name varchar(255) DEFAULT NULL COMMENT '待识别证件照名称',
  driving_lic_model varchar(20) DEFAULT NULL COMMENT '品牌型号',
  driving_lic_address varchar(255) DEFAULT NULL COMMENT '住址',
  driving_lic_car_number varchar(255) DEFAULT NULL COMMENT '车牌号',
  driving_lic_vehicle_type varchar(255) DEFAULT NULL COMMENT '车辆类型',
  driving_lic_issue_date varchar(20) DEFAULT NULL COMMENT '发行日期',
  driving_lic_engine_no varchar(255) DEFAULT NULL COMMENT '发动机号码',
  driving_lic_vin varchar(255) DEFAULT NULL COMMENT '车辆识别号码',
  driving_lic_register_date varchar(20) DEFAULT NULL COMMENT '注册日期',
  driving_lic_use_type varchar(255) DEFAULT NULL COMMENT '用途',
  driving_lic_owner varchar(20) DEFAULT NULL COMMENT '所有人',
  result char(1) DEFAULT NULL COMMENT '识别结果',
  tenant_id varchar(255) DEFAULT 'super',
  PRIMARY KEY (id)
) COMMENT='行驶证OCR识别记录';

CREATE TABLE ocr_hk_mac_pass_log (
  id varchar(48) NOT NULL COMMENT '主键',
  received_seq varchar(48) NOT NULL COMMENT '业务流水号',
  received_time datetime NOT NULL COMMENT '请求时间',
  type varchar(12) DEFAULT NULL COMMENT '图片类型，如果是图睿算法库，此项填Unknown',
  type_id varchar(12) DEFAULT NULL COMMENT '卡证类型，如果是文通算法库，此项填0 ',
  channel_code varchar(255) DEFAULT NULL COMMENT '渠道编码',
  scene_image_url varchar(255) NOT NULL COMMENT '待识别证件照存储路径',
  scene_image_name varchar(255) DEFAULT NULL COMMENT '待识别证件照名称',
  hk_mac_pass_type varchar(20) DEFAULT NULL COMMENT '护照类型',
  hk_mac_pass_mrz_number varchar(255) DEFAULT NULL COMMENT '护照号码mrz',
  hk_mac_pass_national_name varchar(255) DEFAULT NULL COMMENT '本国姓名',
  hk_mac_pass_english_name varchar(255) DEFAULT NULL COMMENT '英文姓名',
  hk_mac_pass_gender varchar(20) DEFAULT NULL COMMENT '性别',
  hk_mac_pass_birth varchar(20) DEFAULT NULL COMMENT '出生日期',
  hk_mac_pass_expiry_date varchar(20) DEFAULT NULL COMMENT '有效日期',
  hk_mac_pass_issue_country varchar(20) DEFAULT NULL COMMENT '签发国代码',
  hk_mac_pass_english_sur_name varchar(20) DEFAULT NULL COMMENT '英文姓',
  result char(1) DEFAULT NULL COMMENT '识别结果',
  tenant_id varchar(255) DEFAULT 'super',
  PRIMARY KEY (id)
) COMMENT='港澳通行证OCR识别记录';

CREATE TABLE ocr_idcard_back_log (
  id varchar(48) NOT NULL COMMENT '主键',
  received_seq varchar(48) NOT NULL COMMENT '业务流水号',
  received_time datetime NOT NULL COMMENT '请求时间',
  type varchar(12) DEFAULT NULL COMMENT '图片类型，如果是图睿算法库，此项填Unknown',
  type_id varchar(12) DEFAULT NULL COMMENT '卡证类型，如果是文通算法库，此项填0 ',
  channel_code varchar(255) DEFAULT NULL COMMENT '渠道编码',
  scene_image_url varchar(255) NOT NULL COMMENT '待识别证件照存储路径',
  scene_image_name varchar(255) DEFAULT NULL COMMENT '待识别证件照名称',
  idcard_date_issue varchar(20) DEFAULT NULL COMMENT '签发日期',
  idcard_authority_issue varchar(255) DEFAULT NULL COMMENT '签发机关',
  idcard_limit varchar(255) DEFAULT NULL COMMENT '有效期',
  idcard_expiry_date varchar(20) DEFAULT NULL COMMENT '过期日期',
  idcard_ocr_firm_type varchar(20) DEFAULT NULL COMMENT '算法类型',
  result char(1) DEFAULT NULL COMMENT '识别结果',
  tenant_id varchar(255) DEFAULT 'super',
  PRIMARY KEY (id)
) COMMENT='身份证背面OCR识别记录';

CREATE TABLE ocr_idcard_front_log (
  id varchar(48) NOT NULL COMMENT '主键',
  received_seq varchar(48) NOT NULL COMMENT '业务流水号',
  received_time datetime NOT NULL COMMENT '请求时间',
  type varchar(12) DEFAULT NULL COMMENT '图片类型，如果是图睿算法库，此项填Unknown',
  type_id varchar(12) DEFAULT NULL COMMENT '卡证类型，如果是文通算法库，此项填0 ',
  channel_code varchar(255) DEFAULT NULL COMMENT '渠道编码',
  scene_image_url varchar(255) NOT NULL COMMENT '待识别证件照存储路径',
  scene_image_name varchar(255) DEFAULT NULL COMMENT '待识别证件照名称',
  idcard_number varchar(20) DEFAULT NULL COMMENT '身份证件号',
  idcard_headimage varchar(255) DEFAULT NULL COMMENT '证件头像',
  idcard_ethnicity varchar(20) DEFAULT NULL COMMENT '民族',
  idcard_address varchar(255) DEFAULT NULL COMMENT '住址',
  idcard_birth varchar(255) DEFAULT NULL COMMENT '出生日期',
  idcard_gender varchar(20) DEFAULT NULL COMMENT '性别',
  idcard_name varchar(20) DEFAULT NULL COMMENT '姓名',
  result char(1) DEFAULT NULL COMMENT '识别结果',
  idcard_ocr_firm_type varchar(20) DEFAULT NULL COMMENT '算法类型',
  tenant_id varchar(255) DEFAULT 'super',
  PRIMARY KEY (id)
) COMMENT='身份证正面OCR识别记录';

CREATE TABLE ocr_passport_log (
  id varchar(48) NOT NULL COMMENT '主键',
  received_seq varchar(48) NOT NULL COMMENT '业务流水号',
  received_time datetime NOT NULL COMMENT '请求时间',
  type varchar(12) DEFAULT NULL COMMENT '图片类型，如果是图睿算法库，此项填Unknown',
  type_id varchar(12) DEFAULT NULL COMMENT '卡证类型，如果是文通算法库，此项填0 ',
  channel_code varchar(255) DEFAULT NULL COMMENT '渠道编码',
  scene_image_url varchar(255) NOT NULL COMMENT '待识别证件照存储路径',
  scene_image_name varchar(255) DEFAULT NULL COMMENT '待识别证件照名称',
  passport_mrz_fir varchar(255) DEFAULT NULL COMMENT '护照机读码第1行',
  passport_mrz_sec varchar(255) DEFAULT NULL COMMENT '护照机读码第2行',
  passport_nationality_code varchar(20) DEFAULT NULL COMMENT '持证人国籍代码',
  passport_number varchar(255) DEFAULT NULL COMMENT '护照号码',
  passport_birth_place varchar(255) DEFAULT NULL COMMENT '出生地点',
  passport_issue_place varchar(255) DEFAULT NULL COMMENT '签发地点',
  passport_issue_date varchar(20) DEFAULT NULL COMMENT '签发日期',
  passport_rfid_mrz varchar(255) DEFAULT NULL COMMENT '完整护照机读码射频识别',
  passport_ocr_mrz varchar(255) DEFAULT NULL COMMENT '完整护照机读码OCR识别',
  passport_birth_place_pinyin varchar(255) DEFAULT NULL COMMENT '出生地点拼音',
  passport_issue_place_pinyin varchar(255) DEFAULT NULL COMMENT '签发地点拼音',
  passport_id_number varchar(20) DEFAULT NULL COMMENT '身份证号码',
  passport_ocr_national_name varchar(255) DEFAULT NULL COMMENT '本国姓名拼音',
  passport_ocr_gender varchar(20) DEFAULT NULL COMMENT '性别',
  passport_ocr_nationality_code varchar(20) DEFAULT NULL COMMENT '国籍代码',
  passport_ocr_birth_date varchar(20) DEFAULT NULL COMMENT '出生日期',
  passport_ocr_expiry_date varchar(20) DEFAULT NULL COMMENT '有效期至',
  passport_ocr_authority varchar(255) DEFAULT NULL COMMENT '签发机关',
  passport_national_surname varchar(20) DEFAULT NULL COMMENT '本国姓',
  passport_national_given_name varchar(20) DEFAULT NULL COMMENT '本国名',
  passport_height varchar(20) DEFAULT NULL COMMENT '身高',
  result char(1) DEFAULT NULL COMMENT '识别结果',
  tenant_id varchar(255) DEFAULT 'super',
  PRIMARY KEY (id)
) COMMENT='护照OCR识别记录';



INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (101, '人脸图片文件夹', 'basedata.face.dir', './eyecool/biapwp/basedata/face/', 'N', 'admin', sysdate(), 'admin', sysdate(), '基础数据--人脸图片文件夹', 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (102, '指纹图片文件夹', 'basedata.finger.dir', './eyecool/biapwp/basedata/finger/', 'N', 'admin', sysdate(), 'admin', sysdate(), '基础数据--指纹图片文件夹', 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (103, '虹膜图片文件夹', 'basedata.iris.dir', './eyecool/biapwp/basedata/iris/', 'N', 'admin', sysdate(), 'admin', sysdate(), '基础数据--虹膜图片文件夹', 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (104, '人脸图片质量检测阈值', 'basedata.face.quality.detect.threshold', '70', 'N', 'admin', sysdate(), 'admin', sysdate(), '人脸图片质量检测阈值', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (105, '证件照文件夹', 'basedata.cert.dir', './eyecool/biapwp/basedata/cert/', 'N', 'admin', sysdate(), 'admin', sysdate(), '证件照文件夹', 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (106, '人脸1:1比对阈值', 'basedata.face.compare.threshold', '80', 'N', 'admin', sysdate(), 'admin', sysdate(), '人脸1:1比对阈值', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (107, '指纹1:1比对阈值', 'basedata.finger.compare.threshold', '40', 'N', 'admin', sysdate(), 'admin', sysdate(), '指纹1:1比对阈值', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (108, '虹膜1:1比对阈值', 'basedata.iris.compare.threshold', '30', 'N', 'admin', sysdate(), 'admin', sysdate(), '虹膜1:1比对阈值', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (111, '重复虹膜阈值', 'basedata.iris.repeat.threshold', '30', 'N', 'admin', sysdate(), 'admin', sysdate(), '虹膜重复阈值(比对得分超过阈值认为重复)', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (112, '重复指纹阈值', 'basedata.finger.repeat.threshold', '40', 'N', 'admin', sysdate(), 'admin', sysdate(), '重复指纹阈值(比对得分超过阈值认为重复)', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (113, '平台接口请求并发量', 'platform.interface.concurrent', '300', 'N', 'admin', sysdate(), 'admin', sysdate(), '平台接口请求并发量', 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (114, '人脸自助采集校验是否校验证件库', 'basedata.face.collect.validte.cert', 'N', 'N', 'admin', sysdate(), 'admin', sysdate(), '人脸自助采集校验不存在底库是否校验证件库(Y/N)', 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (115, '人脸1-N搜索图片文件夹', 'busi.face.searchN.dir', './eyecool/busi/face/search/', 'N', 'admin', sysdate(), 'admin', sysdate(), '人脸1-N搜索图片文件夹', 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (116, '人脸1-N搜索比对阈值', 'basedata.face.searchN.threshold', '80', 'N', 'admin', sysdate(), 'admin', sysdate(), '人脸1-N搜索比对阈值', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (117, '指纹图片质量检测阈值', 'basedata.finger.quality.detect.threshold', '40', 'N', 'admin', sysdate(), 'admin', sysdate(), '指纹图片质量检测阈值', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (118, '人脸静默检活阈值', 'basedata.face.checklive.threshold', '350', 'N', 'admin', sysdate(), 'admin', sysdate(), '人脸检活阈值(静默检活)，必须是数值', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (119, '指纹1-N搜索图片文件夹', 'busi.finger.searchN.dir', './eyecool/busi/finger/search/', 'N', 'admin', sysdate(), 'admin', sysdate(), '指纹1-N搜索图片文件夹', 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (120, '虹膜1-N搜索图片文件夹', 'busi.iris.searchN.dir', './eyecool/busi/iris/search/', 'N', 'admin',sysdate(), 'admin', sysdate(), '虹膜1-N搜索图片文件夹', 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (121, '指纹1-N搜索比对阈值', 'basedata.finger.searchN.threshold', '40', 'N', 'admin', sysdate(), 'admin', sysdate(), '指纹1-N搜索比对阈值', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (122, '虹膜1-N搜索比对阈值', 'basedata.iris.searchN.threshold', '30', 'N', 'admin', sysdate(), 'admin', sysdate(), '虹膜1-N搜索比对阈值', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (123, '人脸1:1认证图片文件夹', 'busi.face.compare.dir', './eyecool/busi/face/compare/', 'N', 'admin', sysdate(), 'admin', sysdate(), '人脸1:1认证图片文件夹', 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (124, '指纹1:1认证图片文件夹', 'busi.finger.compare.dir', './eyecool/busi/finger/compare/', 'N', 'admin', sysdate(), 'admin', sysdate(), '指纹1:1认证图片文件夹', 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (125, '虹膜1:1认证图片文件夹', 'busi.iris.compare.dir', './eyecool/busi/iris/compare/', 'N', 'admin', sysdate(), 'admin', sysdate(), '虹膜1:1认证图片文件夹', 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (127, '人脸入库是否进行1-N校验', 'basedata.face.add.isValidN', 'N', 'N', 'admin', sysdate(), 'admin', sysdate(), '人脸入库是否进行1-N校验(Y/N)', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (130, '指纹入库是否进行1-N校验', 'basedata.finger.add.isValidN', 'N', 'N', 'admin', sysdate(), 'admin', sysdate(), '指纹入库是否进行1-N校验(Y/N)， 用于新增、修改', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (131, '虹膜入库是否进行1-N校验', 'basedata.iris.add.isValidN', 'N', 'N', 'admin', sysdate(), 'admin', sysdate(), '虹膜入库是否进行1-N校验(Y/N)， 用于新增、修改', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (132, '设备APP版本文件存放文件夹', 'device.version.file.dir', './eyecool/device/version/', 'N', 'admin', sysdate(), '', NULL, '设备APP版本文件存放文件夹', 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (133, '消息通知附件存放位置', 'msg.attachment.dir', './eyecool/msg/attachment/', 'N', 'admin', sysdate(), 'admin', sysdate(), '消息通知附件存放位置', 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (134, '人脸1-N识别日志保存是否比对去重', 'busi.face.searchN.log.save.distinct', 'Y', 'N', 'admin', sysdate(), 'busi_admin', sysdate(), '人脸1-N日识别可能存在短时间内（例如5s）连续识别到一个人的情况，是否对识别日志去重处理（Y：是，N：否），是就只保存第一次识别的记录', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (137, '平台是否开启1-N搜索功能', 'platform.searchN.function.isOpen', 'Y', 'N', 'admin', sysdate(), 'admin', sysdate(), '注意：此参数值需要在初始化部署时设定，避免在平台使用过程中改动。\r\n说明：平台是否开启1-N搜索功能（Y:开启，N：不开启）,不开启则不需要部署Datamanager和fox-minisearch微服务。', 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (145, '人脸入库是否进行活体检测', 'basedata.face.add.checkLive', 'N', 'N', 'admin', sysdate(), 'admin', sysdate(), '人脸入库是否进行活体检测(Y/N)，用于新增、修改、自助采集', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (146, '平台是否支持自动绑定和解绑人库关系', 'platform.libperson.isAutobind', 'N', 'N', 'admin', sysdate(), 'admin', sysdate(), '平台是否支持自动绑定和解绑人库关系（用于部分渠道需要绑定全库人员场景，人员入库直接和相关渠道绑定）', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (147, '平台需要自动绑定人库关系的渠道编码', 'platform.libperson.autobind.channel', '-', 'N', 'admin', sysdate(), 'busi_admin', sysdate(), '平台需要自动绑定人库关系的渠道编码(用于部分渠道需要绑定全库人员场景，仅在开启自动绑定时有效，多个渠道编码使用\";\"分割)', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (148, '设备外观图片存放文件夹', 'device.model.image.dir', './eyecool/device/exteriorImage', 'N', 'admin', sysdate(), 'admin', sysdate(), '设备外观图片存放文件夹', 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (150, '人脸视频检活阈值', 'basedata.face.video.checklive.threshold', '350', 'N', 'admin', sysdate(), 'admin', sysdate(), '人脸视频检活阈值(必须是数值)', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (152, '多模态虹膜1v1比对阈值', 'multi.iris.compare.threshold', '30', 'N', 'admin', sysdate(), 'admin', sysdate(), '多模态虹膜阈值', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (153, '多模态人脸1v1比对阈值', 'multi.face.compare.threshold', '80', 'N', 'admin', sysdate(), 'admin', sysdate(), '多模态人脸阈值', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (154, '多模态虹膜人脸融合1v1比对阈值', 'multi.irisface.fusion.compare.threshold', '80', 'N', 'admin', sysdate(), 'admin', sysdate(), '多模态虹膜人脸融合阈值', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (155, '多模态日志照片存储路径', 'busi.mulit.search.dir', './eyecool/busi/mulit/', 'N', 'admin', sysdate(), '', NULL, '多模态日志照片存储路径', 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (156, 'OCR日志图片存放图片路径', 'busi.orc.image.dir', './eyecool/busi/ocr/', 'N', 'admin', sysdate(), 'admin', sysdate(), 'OCR图片存储路径', 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (157, '无感识别陌生人推送开关', 'biapwp.dfrs.stranger.push', 'true', 'N', 'admin', sysdate(), 'admin', sysdate(), '无感识别陌生人推送开关', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (158, '无感识别命中推送开关', 'biapwp.dfrs.hit.push', 'true', 'N', 'admin', sysdate(), 'admin', sysdate(), '无感识别命中推送开关', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (159, '高温报警阈值', 'biapwp.dfrs.hcnet.alarmtem', '37.0', 'N', 'admin', sysdate(), 'admin', sysdate(), '高温报警阈值', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (160, '前端命中显示模版照', 'biapwp.dfrs.hit.showTmpl', 'false', 'N', 'admin', sysdate(), 'admin', sysdate(), '前端命中显示模版照', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (161, '生物信息变化触发信息同步的渠道编码', 'person.biochange.liveupdate.channel', '-', 'N', 'admin', sysdate(), 'busi_admin', sysdate(), '配置生物特征变化才会触发更新的渠道编码，多个使用;隔开', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (162, '平台是否接收场景回传日志', 'platform.accept.baklog.open', 'Y', 'N', 'admin', sysdate(), 'admin', sysdate(), '平台是否接收场景回传日志（是：Y， 否：N）', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (163, '多模态底库照片存储路径', 'base.mulit.dir', './eyecool/base/mulit/', 'N', 'admin', sysdate(), '', NULL, '多模态底库照片存储路径', 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (165, '联网核查接口地址', 'identity_verification_url', 'http://green.eyekey.com/ark-plc-id/checkIdentity', 'N', 'admin', sysdate(), 'busi_admin', sysdate(), '联网身份核查接口请求地址URL', 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (166, '联网核查接口授权码', 'identity_verification_authCode', 'd61c1f9110524f20b491c11779bf120d', 'N', 'admin', sysdate(), 'busi_admin', sysdate(), '联网核查接口授权码', 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (167, '异常健康码推送微信公众号参数', 'abnormal.recog.push.weixin.params', '{}', 'N', 'admin', sysdate(), 'busi_admin', sysdate(), '维护格式为JSON格式，多个收件人手机可以用逗号分隔，例如：{\"appId\":\"123\",\"templateId\":\"123\",\"phone\":\"13011112222,13011113333\"}', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (168, '203获取图片地址', 'biapwp.url', 'https://cloud.eyecool.cn/api/device/adapter/getPersonFace?id=', 'N', 'admin', sysdate(), 'busi_admin', sysdate(), '', 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (169, '开通钉钉数据同步的钉钉APPKEY', 'dingtalk.syncdata.appkey', '-', 'N', 'admin', sysdate(), 'admin', NULL, '', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (170, 'SDK上传文件夹', 'sys.tool.sdkFile.dir', './eyecool/biapwp/tool/sdkFile/', 'N', 'admin', sysdate(), 'admin', sysdate(), NULL, 'N');



INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (100, '数据来源', 'apply_data_source', '0', 'admin', sysdate(), '', NULL, '数据来源');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (101, '人员标记', 'apply_person_flag', '0', 'admin', sysdate(), '', NULL, '人员标记');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (102, '是否加密', 'apply_encrypted', '0', 'admin', sysdate(), '', NULL, '是否加密');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (103, '手指编码', 'bio_finger_code', '0', 'admin', sysdate(), '', NULL, '手指编码');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (104, '身份证指纹', 'bio_idcard_finger', '0', 'admin', sysdate(), '', NULL, '身份证指纹');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (105, '眼睛编码', 'bio_eye_code', '0', 'admin', sysdate(), '', NULL, '眼睛编码');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (106, '证件类型', 'sys_cert_type', '0', 'admin', sysdate(), NULL, NULL, '证件类型列表');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (107, '证件照片类型', 'cert_photo_type', '0', 'admin',  sysdate(), 'admin',  sysdate(), '证件照片类型');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (108, '民族', 'sys_nation', '0', 'admin',  sysdate(), '', NULL, '民族');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (109, '生物信息图片类型', 'bio_photo_type', '0', 'admin',  sysdate(), 'admin',  sysdate(), '生物信息图片类型');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (110, '生物特征开通状态', 'bio_mode_status', '0', 'admin',  sysdate(), '', NULL, '生物特征开通状态');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (112, '渠道1-N识别方式', 'search_n_type', '0', 'admin',  sysdate(), 'admin',  sysdate(), '1-N识别方式');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (113, 'HTTP交易接口', 'http_interface', '0', 'admin',  sysdate(), 'admin',  sysdate(), 'HTTP交易接口');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (115, '渠道管理认证类型', 'channel_bio_attest_type', '0', 'admin',  sysdate(), '', NULL, '渠道管理认证类型');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (116, '渠道管理数据来源', 'channel_business_source', '0', 'admin',  sysdate(), '', NULL, '渠道管理数据来源');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (117, '生物认证识别结果', 'bio_result', '0', 'admin',  sysdate(), 'admin',  sysdate(), '生物认证识别结果');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (118, '生物特征1-N日志场景类型', 'search_log_type', '0', 'admin',  sysdate(), 'admin',  sysdate(), '生物特征1-N日志场景类型');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (121, '设备类型', 'client_device_type', '0', 'admin',  sysdate(), '', NULL, '设备类型');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (122, '设备升级状态', 'device_upgrade_status', '0', 'admin',  sysdate(), '', NULL, '设备升级状态');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (123, '设备升级任务结果', 'device_upgrade_result', '0', 'admin',  sysdate(), '', NULL, '设备升级任务结果');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (124, '消息通知方式', 'msg_notice_method', '0', 'admin',  sysdate(), '', NULL, '消息通知方式');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (125, '消息发送模式', 'msg_type', '0', 'admin',  sysdate(), 'admin',  sysdate(), '消息发送模式');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (126, '消息推送结果', 'msg_result', '0', 'admin',  sysdate(), '', NULL, '消息推送结果');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (127, '邮件类型', 'msg_mail_type', '0', 'admin',  sysdate(), '', NULL, '邮件类型');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (128, '钉钉消息类型', 'msg_dingtalk_type', '0', 'admin',  sysdate(), '', NULL, '钉钉消息类型');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (129, '钉钉媒体文件类型', 'msg_dingtalk_media_type', '0', 'admin',  sysdate(), '', NULL, '钉钉媒体文件类型');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (130, '设备添加方式', 'device_create_method', '0', 'admin',  sysdate(), '', NULL, '设备添加方式');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (131, '设备在线状态', 'device_online_state', '0', 'admin',  sysdate(), '', NULL, '设备在线状态');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (132, '设备动作日志类型', 'device_action_type', '0', 'admin',  sysdate(), '', NULL, '设备动作日志类型');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (133, '设备参数下发结果', 'device_config_result', '0', 'admin',  sysdate(), '', NULL, '设备参数下发结果');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (135, '区域类型', 'area_type', '0', 'admin',  sysdate(), 'admin', sysdate(), '区域类型');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (137, '租户状态', 'sys_tenant_state', '0', 'admin',  sysdate(), '', NULL, '租户状态列表');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (138, '租户来源', 'sys_tenant_source', '0', 'admin',  sysdate(), '', NULL, '租户来源列表');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (139, '多模态类型', 'fusion_type', '0', 'admin',  sysdate(), 'admin',  sysdate(), '多模态类型');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (140, '人脸虹膜多模态比对模式', 'match_mode', '0', 'admin', sysdate(), 'admin',  sysdate(), '人脸虹膜多模态比对模式');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (143, '健康码请求地址', 'healthcode.request.url', '0', 'admin',  sysdate(), '', NULL, '健康码');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (144, 'SDK类型', 'sys_sdk_type', '0', 'admin', sysdate(), '', NULL, 'SDK文件类型');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (145, '设备进出方向', 'device_direction', '0', 'admin', sysdate(), '', NULL, '设备进出方向');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (146, '租户类型', 'sys_tenant_type', '0', 'admin', sysdate(), '', NULL, '租户类型');



INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (100, 1, '内部接口', 'INTERFACE', 'apply_data_source', '', 'success', 'Y', '0', 'admin',  sysdate(), 'admin',  sysdate(), '数据来源--接口');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (101, 2, '数据导入', 'IMP', 'apply_data_source', '', 'info', 'N', '0', 'admin',  sysdate(), 'admin',  sysdate(), '数据来源--数据导入');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (102, 3, 'HTTP接口', 'HTTP', 'apply_data_source', '', 'warning', 'N', '0', 'admin',  sysdate(), 'admin',  sysdate(), '数据来源--HTTP');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (103, 1, '正常', '1', 'apply_person_flag', '', 'success', 'Y', '0', 'admin',  sysdate(), 'admin',  sysdate(), '人员标记--正常');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (104, 2, '红名单', '2', 'apply_person_flag', '', 'primary', 'N', '0', 'admin',  sysdate(), 'admin',  sysdate(), '人员标记--红名单');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (105, 3, '黑名单', '3', 'apply_person_flag', '', 'danger', 'N', '0', 'admin',  sysdate(), 'admin',  sysdate(), '人员标记--黑名单');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (106, 1, '加密', '1', 'apply_encrypted', '', 'primary', 'Y', '0', 'admin',  sysdate(), 'admin',  sysdate(), '是否加密-加密');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (107, 2, '不加密', '0', 'apply_encrypted', '', 'success', 'N', '0', 'admin',  sysdate(), 'admin',  sysdate(), '是否加密-不加密');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (167, 1, '左手拇指', '16', 'bio_finger_code', '', '', 'N', '0', 'admin', sysdate(), 'admin',  sysdate(), '手指编码-左手拇指');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (168, 2, '左手食指', '17', 'bio_finger_code', '', '', 'N', '0', 'admin',  sysdate(), 'admin',  sysdate(), '手指编码-左手食指');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (169, 3, '左手中指', '18', 'bio_finger_code', '', '', 'N', '0', 'admin',  sysdate(), 'admin',  sysdate(), '手指编码-左手中指');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (170, 4, '左手环指', '19', 'bio_finger_code', '', '', 'N', '0', 'admin',  sysdate(), 'admin', sysdate(), '手指编码-左手无名指');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (171, 5, '左手小指', '20', 'bio_finger_code', '', '', 'N', '0', 'admin',  sysdate(), 'admin',  sysdate(), '手指编码-左手小指');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (172, 6, '右手拇指', '11', 'bio_finger_code', '', '', 'N', '0', 'admin',  sysdate(), 'admin',  sysdate(), '手指编码-右手拇指');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (173, 7, '右手食指', '12', 'bio_finger_code', '', '', 'Y', '0', 'admin',  sysdate(), 'admin',  sysdate(), '手指编码-右手食指');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (174, 8, '右手中指', '13', 'bio_finger_code', '', '', 'N', '0', 'admin',  sysdate(), 'admin',  sysdate(), '手指编码-右手中指');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (175, 9, '右手环指', '14', 'bio_finger_code', '', '', 'N', '0', 'admin',  sysdate(), 'admin',  sysdate(), '手指编码-右手无名指');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (176, 10, '右手小指', '15', 'bio_finger_code', '', '', 'N', '0', 'admin',  sysdate(), 'admin',  sysdate(), '手指编码-右手小指');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (179, 1, '身份证指纹A', 'A', 'bio_idcard_finger', '', '', 'Y', '0', 'admin',  sysdate(), '', NULL, '身份证指纹A');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (180, 2, '身份证指纹B', 'B', 'bio_idcard_finger', '', '', 'N', '0', 'admin',  sysdate(), '', NULL, '身份证指纹B');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (181, 1, '左眼', 'L', 'bio_eye_code', '', '', 'Y', '0', 'admin', sysdate(), '', NULL, '眼睛编码-左眼');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (182, 2, '右眼', 'R', 'bio_eye_code', '', '', 'N', '0', 'admin', sysdate(), '', NULL, '眼睛编码-右眼');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (183, 1, '居民身份证', '111', 'sys_cert_type', NULL, 'default', 'Y', '0', 'admin', sysdate(), NULL, NULL, '证件类型-居民身份证');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (184, 2, '临时居民身份证', '112', 'sys_cert_type', NULL, 'primary', 'N', '0', 'admin', sysdate(), NULL, NULL, '证件类型-临时居民身份证');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (185, 3, '户口簿', '113', 'sys_cert_type', NULL, 'success', 'N', '0', 'admin', sysdate(), NULL, NULL, '证件类型-户口簿');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (186, 4, '军官证', '114', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', sysdate(), NULL, NULL, '证件类型-军官证');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (187, 5, '警官证', '123', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', sysdate(), NULL, NULL, '证件类型-警官证');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (188, 6, '学生证', '133', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', sysdate(), NULL, NULL, '证件类型-学生证');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (189, 7, '外交护照', '411', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', sysdate(), NULL, NULL, '证件类型-外交护照');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (190, 8, '公务护照', '412', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', sysdate(), NULL, NULL, '证件类型-公务护照');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (191, 9, '因公普通护照', '413', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', sysdate(), NULL, NULL, '证件类型-因公普通护照');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (192, 10, '护照', '414', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', sysdate(), NULL, NULL, '证件类型-普通护照');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (193, 11, '入出境通行证', '416', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', sysdate(), NULL, NULL, '证件类型-入出境通行证');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (194, 12, '外国人出入境证', '417', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', sysdate(), NULL, NULL, '证件类型-外国人出入境证');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (195, 13, '海员证', '419', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', sysdate(), NULL, NULL, '证件类型-海员证');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (196, 14, '台湾居民来往大陆通行证', '511', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', sysdate(), NULL, NULL, '证件类型-台湾居民来往大陆通行');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (197, 15, '往来港澳通行证', '513', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', sysdate(), NULL, NULL, '证件类型-往来港澳通行证');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (198, 16, '回乡证', '516', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', sysdate(), NULL, NULL, '证件类型-回乡证');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (199, 17, '大陆居民往来台湾通行证', '517', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', sysdate(), NULL, NULL, '证件类型-大陆居民往来台湾通行证');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (200, 18, '中朝边境地区出入境通行证', '733', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', sysdate(), NULL, NULL, '证件类型-中朝边境地区出入境通行证');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (201, 19, '中蒙边境地区出入境通行证', '736', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', sysdate(), NULL, NULL, '证件类型-中蒙边境地区出入境通行证');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (202, 20, '中缅边境地区出入境通行证', '738', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', sysdate(), NULL, NULL, '证件类型-中缅边境地区出入境通行证');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (203, 21, '云南省边境地区境外边民入出境证', '740', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', sysdate(), NULL, NULL, '证件类型-云南省边境地区境外边民入出境证');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (204, 22, '中尼边境地区出入境通行证', '741', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', sysdate(), NULL, NULL, '证件类型-中尼边境地区出入境通行证');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (205, 23, '中越边境地区出入境通行证', '743', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', sysdate(), NULL, NULL, '证件类型-中越边境地区出入境通行证');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (206, 24, '中老边境地区出入境通行证', '745', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', sysdate(), NULL, NULL, '证件类型-中老边境地区出入境通行证');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (207, 25, '中印边境地区出入境通行证', '747', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', sysdate(), NULL, NULL, '证件类型-中印边境地区出入境通行证');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (208, 26, '其他', '990', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', sysdate(), NULL, NULL, '证件类型-其他');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (209, 27, '军人证', '991', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', sysdate(), NULL, NULL, '证件类型-军人证');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (210, 28, '港澳台居住证', '992', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', sysdate(), NULL, NULL, '证件类型-港澳台居住证');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (211, 29, '外国人居住证', '993', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', sysdate(), NULL, NULL, '证件类型-外国人居住证');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (212, 30, '越南身份证', '994', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', sysdate(), NULL, NULL, '证件类型-越南身份证');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (213, 31, '边民证', '995', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', sysdate(), NULL, NULL, '证件类型-边民证');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (214, 32, '无证件', '999', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin',sysdate(), NULL, NULL, '证件类型-无证件');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (215, 2, '入学照片', '1', 'cert_photo_type', '', '', 'N', '0', 'admin', sysdate(), 'admin', sysdate(), '证件照片类型-入学照片');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (216, 3, '在校照片', '2', 'cert_photo_type', '', '', 'N', '0', 'admin', sysdate(), 'admin', sysdate(), '证件照片类型-在校照片');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (217, 4, '毕业照片', '3', 'cert_photo_type', '', '', 'N', '0', 'admin', sysdate(), 'admin', sysdate(), '证件照片类型-毕业照片');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (218, 1, '汉族', '01', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (219, 2, '蒙古族', '02', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (220, 3, '回族', '03', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (221, 4, '藏族', '04', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (222, 5, '维吾尔族', '05', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (223, 6, '苗族', '06', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (224, 7, '彝族', '07', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (225, 8, '壮族', '08', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (226, 9, '布依族', '09', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (227, 10, '朝鲜族', '10', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (228, 11, '满族', '11', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (229, 12, '侗族', '12', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (230, 13, '瑶族', '13', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (231, 14, '白族', '14', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (232, 15, '土家族', '15', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (233, 16, '哈尼族', '16', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (234, 17, '哈萨克族', '17', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (235, 18, '傣族', '18', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (236, 19, '黎族', '19', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (237, 20, '傈僳族', '20', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (238, 21, '佤族', '21', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (239, 22, '畲族', '22', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (240, 23, '高山族', '23', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (241, 24, '拉祜族', '24', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (242, 25, '水族', '25', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (243, 26, '东乡族', '26', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (244, 27, '纳西族', '27', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (245, 28, '景颇族', '28', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (246, 29, '柯尔克孜族', '29', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (247, 30, '土族', '30', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (248, 31, '达斡尔族', '31', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (249, 32, '仫佬族', '32', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (250, 33, '羌族', '33', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (251, 34, '布朗族', '34', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (252, 35, '撒拉族', '35', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (253, 36, '毛南族', '36', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (254, 37, '仡佬族', '37', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (255, 38, '锡伯族', '38', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (256, 39, '阿昌族', '39', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (257, 40, '普米族', '40', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (258, 41, '塔吉克族', '41', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (259, 42, '怒族', '42', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (260, 43, '乌孜别克族', '43', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (261, 44, '俄罗斯族', '44', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (262, 45, '鄂温克族', '45', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (263, 46, '德昂族', '46', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (264, 47, '保安族', '47', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (265, 48, '裕固族', '48', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (266, 49, '京族', '49', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (267, 50, '塔塔尔族', '50', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (268, 51, '独龙族', '51', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (269, 52, '鄂伦春族', '52', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (270, 53, '赫哲族', '53', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (271, 54, '门巴族', '54', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (272, 55, '珞巴族', '55', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (273, 56, '基诺族', '56', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (274, 57, '穿青人族', '81', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (275, 58, '其他', '97', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (276, 59, '外国血统', '98', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (277, 1, '人脸图片', '1', 'bio_photo_type', NULL, NULL, 'Y', '0', 'admin', sysdate(), '', NULL, '生物信息图片类型-人脸图片');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (278, 2, '指纹图片', '2', 'bio_photo_type', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, '生物信息图片类型-指纹图片');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (279, 3, '虹膜图片', '3', 'bio_photo_type', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, '生物信息图片类型-虹膜图片');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (280, 1, '开通', '1', 'bio_mode_status', NULL, 'primary', 'Y', '0', 'admin', sysdate(), '', NULL, '生物特征开通状态-开通');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (281, 2, '不开通', '0', 'bio_mode_status', NULL, 'danger', 'N', '0', 'admin', sysdate(), '', NULL, '生物特征开通状态-不开通');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (285, 1, '校验分库', '1', 'search_n_type', '', 'primary', 'N', '0', 'admin', sysdate(), 'admin', sysdate(), '1-N识别方式-校验分库');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (286, 2, '校验渠道库', '2', 'search_n_type', '', 'warning', 'Y', '0', 'admin', sysdate(), 'admin', sysdate(), '1-N识别方式-校验渠道库');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (287, 3, '校验全库', '3', 'search_n_type', '', 'success', 'N', '0', 'admin', sysdate(), 'admin', sysdate(), '1-N识别方式-校验全库');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (288, 1, '新增人员信息', 'PERSON_INFO_INSERT', 'http_interface', '', '', 'Y', '0', 'admin', sysdate(), 'admin', sysdate(), 'HTTP交易接口-新增人员信息');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (291, 1, '人脸', '0', 'channel_bio_attest_type', '', 'info', 'Y', '0', 'admin', sysdate(), 'admin', sysdate(), '人脸');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (292, 2, '指纹', '1', 'channel_bio_attest_type', NULL, 'info', 'Y', '0', 'admin', sysdate(), '', NULL, '指纹');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (293, 3, '虹膜', '2', 'channel_bio_attest_type', NULL, 'info', 'Y', '0', 'admin', sysdate(), '', NULL, '虹膜');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (294, 4, '指静脉', '3', 'channel_bio_attest_type', NULL, 'info', 'Y', '0', 'admin', sysdate(), '', NULL, '指静脉');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (295, 1, '接口', 'INTERFACE', 'channel_business_source', NULL, 'info', 'Y', '0', 'admin', sysdate(), '', NULL, '接口');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (296, 2, '导入', 'IMP', 'channel_business_source', NULL, 'info', 'Y', '0', 'admin', sysdate(), '', NULL, '导入');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (297, 3, 'HTTP接口', 'HTTP', 'channel_business_source', NULL, 'info', 'Y', '0', 'admin', sysdate(), '', NULL, 'HTTP接口');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (298, 2, '修改人员信息', 'PERSON_INFO_UPDATE', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP交易接口--修改人员信息');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (299, 3, '删除人员信息', 'PERSON_INFO_DELETE', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP交易接口--删除人员信息');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (300, 4, '查询人员信息', 'PERSON_INFO_SELECT', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP交易接口--查询人员信息');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (301, 11, '右手不确定指位', '97', 'bio_finger_code', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, '手指编码--右手不确定指位');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (302, 12, '左手不确定指位', '98', 'bio_finger_code', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, '手指编码--左手不确定指位');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (303, 13, '其他不确定指位', '99', 'bio_finger_code', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, '手指编码--其他不确定指位');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (305, 1, '证件照片', '0', 'cert_photo_type', NULL, NULL, 'Y', '0', 'admin', sysdate(), '', NULL, '证件照片类型--证件照片');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (306, 5, '开通人脸功能', 'PERSON_FACE_OPEN', 'http_interface', '', '', 'N', '0', 'admin', sysdate(), 'admin', sysdate(), 'HTTP交易接口--开通人脸');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (307, 6, '关闭人脸功能', 'PERSON_FACE_CLOSE', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP交易接口--关闭人脸');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (308, 7, '人脸识别(1:N)', 'PERSON_FACE_RECOG', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP交易接口--人脸识别(1:N)');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (309, 8, '人脸认证(1:1)', 'PERSON_FACE_VERIFY', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP交易接口--人脸认证(1:1)');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (310, 9, '开通指纹功能', 'PERSON_FINGER_OPEN', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP交易接口--开通指纹');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (311, 10, '关闭指纹功能', 'PERSON_FINGER_CLOSE', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP交易接口--关闭指纹');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (312, 11, '指纹识别(1:N)', 'PERSON_FINGER_RECOG', 'http_interface', '', '', 'N', '0', 'admin', sysdate(), 'admin', sysdate(), 'HTTP交易接口--指纹识别(1:N)');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (313, 12, '指纹认证(1:1)', 'PERSON_FINGER_VERIFY', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP交易接口--指纹认证(1:1)');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (314, 13, '开通虹膜功能', 'PERSON_IRIS_OPEN', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP交易接口--开通虹膜');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (315, 14, '关闭虹膜功能', 'PERSON_IRIS_CLOSE', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP交易接口--关闭虹膜');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (316, 15, '虹膜识别(1:N)', 'PERSON_IRIS_RECOG', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP交易接口--虹膜识别(1:N)');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (317, 16, '虹膜认证(1:1)', 'PERSON_IRIS_VERIFY', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP交易接口--虹膜认证(1:1)');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (318, 17, '开通指静脉功能', 'PERSON_FVEIN_OPEN', 'http_interface', '', '', 'N', '0', 'admin', sysdate(), 'admin', sysdate(), 'HTTP交易接口--开通指静脉');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (319, 18, '关闭指静脉功能', 'PERSON_FVEIN_CLOSE', 'http_interface', '', '', 'N', '0', 'admin', sysdate(), 'admin', sysdate(), 'HTTP交易接口--关闭指静脉');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (320, 19, '指静脉识别(1:N)', 'PERSON_FVEIN_RECOG', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP交易接口--指静脉识别(1:N)');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (321, 20, '指静脉认证(1:1)', 'PERSON_FVEIN_VERIFY', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP交易接口--指静脉认证(1:1)');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (322, 21, '短信推送功能', 'MESSAGE_SEND_SMS', 'http_interface', '', '', 'N', '0', 'admin', sysdate(), 'admin', sysdate(), 'HTTP交易接口--短信推送');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (323, 22, '微信服务号推送', 'MESSAGE_SEND_WECHAT', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP交易接口--微信服务号推送');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (324, 23, '邮件推送功能', 'MESSAGE_SEND_EMAIL', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP交易接口--邮件推送功能');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (325, 1, '通过', '0', 'bio_result', '', 'primary', 'Y', '0', 'admin', sysdate(), 'admin', sysdate(), '生物认证识别结果--通过');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (326, 2, '未通过', '1', 'bio_result', '', 'danger', 'N', '0', 'admin', sysdate(), 'admin', sysdate(), '生物认证识别结果--未通过');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (327, 1, '基础信息入库1:N', '1', 'search_log_type', '', 'success', 'N', '0', 'admin', sysdate(), 'admin', sysdate(), '生物特征1-N日志场景类型--基础信息入库1:N');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (328, 2, '比对搜索接口1:N', '2', 'search_log_type', '', 'primary', 'Y', '0', 'admin', sysdate(), 'admin', sysdate(), '生物特征1-N日志场景类型--1:N比对搜索接口');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (331, 24, '人员数据实时更新', 'PERSON_LIVE_UPDATE', 'http_interface', '', '', 'N', '0', 'admin', sysdate(), 'admin', sysdate(), 'HTTP交易接口--人员数据实时更新');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (332, 4, '依次校验', '4', 'search_n_type', '', 'danger', 'N', '0', 'admin', sysdate(), 'admin', sysdate(), '按照分库->渠道库->全库依次查询，查询到即返回');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (333, 3, '子系统日志回传', '3', 'search_log_type', '', 'warning', 'N', '0', 'admin', sysdate(), 'admin', sysdate(), '生物特征1-N日志场景类型--子系统日志回传');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (334, 25, '人脸识别日志回传', 'PERSON_FACE_SEARCH_LOG_BAK', 'http_interface', '', '', 'N', '0', 'admin', sysdate(), 'admin', sysdate(), 'HTTP交易接口--人脸识别日志回传');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (335, 26, '获取人脸特征', 'PERSON_FACE_FEATURE', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP交易接口--获取人脸特征');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (336, 27, '分库操作功能', 'PERSON_SUB_TREASURY_OPERATE', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP交易接口--分库操作功能');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (340, 1, '待下载', '1', 'device_upgrade_status', '', 'info', 'Y', '0', 'admin', sysdate(), 'admin', sysdate(), '设备升级状态--待下载');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (341, 2, '待更新', '2', 'device_upgrade_status', '', 'primary', 'N', '0', 'admin', sysdate(), 'admin', sysdate(), '设备升级状态--待更新');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (342, 3, '更新成功', '3', 'device_upgrade_status', NULL, 'success', 'N', '0', 'admin', sysdate(), '', NULL, '设备升级状态--更新成功');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (343, 4, '更新失败', '4', 'device_upgrade_status', NULL, 'danger', 'N', '0', 'admin', sysdate(), '', NULL, '设备升级状态--更新失败');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (344, 5, '跳过', '5', 'device_upgrade_status', NULL, 'warning', 'N', '0', 'admin', sysdate(), '', NULL, '设备升级状态--跳过');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (346, 29, '设备版本下载接口', 'CLIENT_VERSION_DOWNLOAD', 'http_interface', '', '', 'N', '0', 'admin', sysdate(), 'admin', sysdate(), 'HTTP交易接口--设备版本下载接口');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (348, 1, '待执行', '1', 'device_upgrade_result', '', 'info', 'Y', '0', 'admin', sysdate(), 'admin', sysdate(), '设备升级任务结果--待执行');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (349, 2, '成功', '2', 'device_upgrade_result', NULL, 'success', 'Y', '0', 'admin', sysdate(), '', NULL, '设备升级任务结果--成功');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (350, 3, '失败', '3', 'device_upgrade_result', NULL, 'danger', 'Y', '0', 'admin', sysdate(), '', NULL, '设备升级任务结果--失败');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (351, 4, '跳过', '4', 'device_upgrade_result', NULL, 'warning', 'Y', '0', 'admin', sysdate(), '', NULL, '设备升级任务结果--跳过');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (352, 1, '短信', '1', 'msg_notice_method', '', 'info', 'Y', '0', 'admin', sysdate(), 'admin', sysdate(), '消息通知方式--短信');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (353, 2, '邮件', '2', 'msg_notice_method', NULL, 'success', 'N', '0', 'admin', sysdate(), '', NULL, '消息通知方式--邮件');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (354, 3, '微信', '3', 'msg_notice_method', '', 'primary', 'N', '0', 'admin', sysdate(), 'admin', sysdate(), '消息通知方式--微信');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (355, 4, '钉钉', '4', 'msg_notice_method', NULL, 'warning', 'N', '0', 'admin', sysdate(), '', NULL, '消息通知方式--钉钉');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (356, 1, '普通发送', '1', 'msg_type', '', 'primary', 'Y', '0', 'admin', sysdate(), 'admin', sysdate(), '消息类型--普通发送');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (357, 2, '模板发送', '2', 'msg_type', '', 'success', 'N', '0', 'admin', sysdate(), 'admin', sysdate(), '消息类型--模板发送');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (358, 1, '成功', '0', 'msg_result', '', 'primary', 'Y', '0', 'admin', sysdate(), 'admin', sysdate(), '消息推送结果--成功');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (359, 2, '失败', '1', 'msg_result', NULL, 'danger', 'N', '0', 'admin', sysdate(), '', NULL, '消息推送结果--失败');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (360, 1, '文本邮件', '1', 'msg_mail_type', NULL, 'success', 'Y', '0', 'admin', sysdate(), '', NULL, '邮件类型--文本邮件');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (361, 2, 'HTML邮件', '2', 'msg_mail_type', NULL, 'warning', 'N', '0', 'admin', sysdate(), '', NULL, '邮件类型--HTML邮件');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (362, 1, '文本消息', 'text', 'msg_dingtalk_type', '', 'primary', 'Y', '0', 'admin', sysdate(), 'admin', sysdate(), '钉钉消息类型--文本消息');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (363, 2, 'Markdown消息', 'markdown', 'msg_dingtalk_type', NULL, 'success', 'N', '0', 'admin', sysdate(), '', NULL, '钉钉消息类型--Markdown消息');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (364, 3, '链接消息', 'link', 'msg_dingtalk_type', NULL, 'warning', 'N', '0', 'admin', sysdate(), '', NULL, '钉钉消息类型--链接消息');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (365, 1, '图片文件', 'image', 'msg_dingtalk_media_type', '', 'success', 'Y', '0', 'admin', sysdate(), 'admin', sysdate(), '钉钉媒体文件类型--图片文件');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (366, 2, '语音文件', 'voice', 'msg_dingtalk_media_type', NULL, 'primary', 'N', '0', 'admin', sysdate(), '', NULL, '钉钉媒体文件类型--语音文件');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (367, 3, '普通文件', 'file', 'msg_dingtalk_media_type', NULL, 'warning', 'N', '0', 'admin', sysdate(), '', NULL, '钉钉媒体文件类型--普通文件');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (368, 4, '图片消息', 'image', 'msg_dingtalk_type', NULL, 'info', 'N', '0', 'admin', sysdate(), '', NULL, '钉钉消息类型--图片消息');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (369, 5, '语音消息', 'voice', 'msg_dingtalk_type', NULL, 'success', 'N', '0', 'admin', sysdate(), '', NULL, '钉钉消息类型--语音消息');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (370, 6, '普通文件消息', 'file', 'msg_dingtalk_type', NULL, 'success', 'N', '0', 'admin', sysdate(), '', NULL, '钉钉消息类型--普通文件消息');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (371, 31, '钉钉推送功能', 'MESSAGE_SEND_DING', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP交易接口--钉钉推送功能');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (372, 32, '钉钉推送结果查询', 'MESSAGE_SEND_DING_RESULT', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP交易接口--钉钉推送结果查询');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (373, 33, '钉钉媒体文件上传', 'MESSAGE_DING_MEDIA_UPLOAD', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP交易接口--钉钉媒体文件上传');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (374, 4, '实时同步', 'LIVEUPDATE', 'apply_data_source', NULL, 'primary', 'N', '0', 'admin', sysdate(), '', NULL, '数据来源--数据实时同步');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (376, 35, '删除渠道库人员', 'DEL_CHANNEL_PERSON', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP交易接口--删除渠道库人员');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (377, 36, '查询渠道库人员是否存在', 'QUERY_CHANNEL_PERSON_EXISTS', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP交易接口--查询渠道库人员是否存在');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (378, 5, '三方同步', 'SYNC', 'apply_data_source', NULL, 'danger', 'N', '0', 'admin', sysdate(), '', NULL, '数据来源--三方同步');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (379, 1, '后台创建', '1', 'device_create_method', NULL, 'primary', 'N', '0', 'admin', sysdate(), '', NULL, '设备添加方式--后台创建');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (380, 2, '自主注册', '2', 'device_create_method', NULL, 'warning', 'N', '0', 'admin', sysdate(), '', NULL, '设备添加方式--自主注册');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (381, 1, '在线', '1', 'device_online_state', NULL, 'primary', 'N', '0', 'admin', sysdate(), '', NULL, '设备在线状态--在线');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (382, 2, '离线', '2', 'device_online_state', NULL, 'danger', 'N', '0', 'admin', sysdate(), '', NULL, '设备在线状态--离线');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (383, 3, '后台导入', '3', 'device_create_method', NULL, 'danger', 'N', '0', 'admin', sysdate(), '', NULL, '设备添加方式--后台导入');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (384, 1, '注册', '1', 'device_action_type', '', 'success', 'N', '0', 'admin', sysdate(), 'admin', sysdate(), '设备动作日志类型--注册');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (385, 2, '上线', '2', 'device_action_type', '', 'primary', 'N', '0', 'admin', sysdate(), 'admin', sysdate(), '设备动作日志类型--上线');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (386, 3, '下线', '3', 'device_action_type', NULL, 'danger', 'N', '0', 'admin', sysdate(), '', NULL, '设备动作日志类型--下线');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (387, 4, '更新', '4', 'device_action_type', NULL, 'warning', 'N', '0', 'admin', sysdate(), '', NULL, '设备动作日志类型--更新');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (389, 1, '成功', '0', 'device_config_result', NULL, 'primary', 'N', '0', 'admin', sysdate(), '', NULL, '设备参数下发结果--成功');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (390, 2, '失败', '1', 'device_config_result', '', 'danger', 'N', '0', 'admin', sysdate(), 'admin', sysdate(), '设备参数下发结果--失败');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (391, 38, '获取指纹特征', 'PERSON_FINGER_FEATURE', 'http_interface', '', '', 'N', '0', 'admin', sysdate(), 'admin', sysdate(), 'HTTP交易接口--获取指纹特征');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (392, 39, '获取虹膜特征', 'PERSON_IRIS_FEATURE', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP交易接口--获取虹膜特征');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (393, 40, '人脸图片比对', 'PERSON_FACE_IMG_COMPARE', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP交易接口--人脸图片比对');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (394, 41, '指纹图片比对', 'PERSON_FINGER_IMG_COMPARE', 'http_interface', '', '', 'N', '0', 'admin', sysdate(), 'admin', sysdate(), 'HTTP交易接口--指纹图片比对');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (395, 42, '虹膜图片比对', 'PERSON_IRIS_IMG_COMPARE', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP交易接口--虹膜图片比对');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (396, 43, '人脸图片或视频检活', 'PERSON_FACE_CHECKLIVE', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP交易接口--人脸图片或视频检活');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (397, 44, '人脸图片质量检测', 'PERSON_FACE_QUALITY_DETECT', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP交易接口--人脸图片质量检测');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (398, 45, '指纹图片质量检测', 'PERSON_FINGER_QUALITY_DETECT', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP交易接口--指纹图片质量检测');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (409, 1, '正常', 'NORMAL', 'sys_tenant_state', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, '租户状态--正常');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (410, 2, '冻结', 'FROZEN', 'sys_tenant_state', NULL, NULL, 'N', '0', 'admin', sysdate(), 'admin', NULL, '租户状态--冻结');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (411, 1, '用户注册', 'REGIST', 'sys_tenant_source', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, '租户来源--用户注册');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (412, 2, '后台创建', 'BG_CREATE', 'sys_tenant_source', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, '租户来源--后台创建');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (413, 1, '虹膜人脸', '1', 'fusion_type', NULL, 'primary', 'Y', '0', 'admin', sysdate(), '', NULL, '多模态');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (414, 2, '指纹指静脉', '2', 'fusion_type', NULL, 'warning', 'N', '0', 'admin', sysdate(), '', NULL, '多模态');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (415, 1, '虹膜比对', '0', 'match_mode', NULL, 'primary', 'Y', '0', 'admin', sysdate(), '', NULL, '多模态');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (416, 2, '人脸比对', '1', 'match_mode', NULL, 'warning', 'N', '0', 'admin', sysdate(), '', NULL, '多模态');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (417, 3, '虹膜和人脸比对', '2', 'match_mode', NULL, 'warning', 'N', '0', 'admin', sysdate(), '', NULL, '多模态');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (418, 4, '虹膜或人脸比对', '3', 'match_mode', NULL, 'warning', 'N', '0', 'admin', sysdate(), '', NULL, '多模态');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (419, 5, '多模态比对', '4', 'match_mode', NULL, 'warning', 'N', '0', 'admin', sysdate(), '', NULL, '多模态');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (424, 46, '人脸视频检活比对', 'PERSON_FACE_CHECKLIVE_AND_COMPARE', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP交易接口--人脸视频检活比对');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (425, 47, 'OCR识别接口', 'OCR', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', sysdate(), 'HTTP交易接口--OCR识别接口');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (426, 48, '虹膜人脸多模态注册', 'MULIT_IRIS_FACE_REGISTER', 'http_interface', '', 'default', 'Y', '0', 'admin', sysdate(), 'admin', sysdate(), 'http标准接口-多模态人脸虹膜注册');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (427, 49, '虹膜人脸多模态1vN', 'MULIT_IRIS_FACE_SEARCH', 'http_interface', NULL, 'default', 'Y', '0', 'admin', sysdate(), 'admin', sysdate(), 'http标准接口-多模态人脸虹膜搜索');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (428, 50, '分库操作', 'SUB_TREASURY_OPERATE', 'http_interface', NULL, NULL, 'Y', '0', 'admin', sysdate(), '', NULL, NULL);
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (429, 51, '设备查询', 'QUREY_DEVICE', 'http_interface', NULL, NULL, 'Y', '0', 'admin', sysdate(), '', NULL, '中税设备查询');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (430, 52, '人脸识别日志查询', 'PERSON_FACE_SEARCH_LOG_QUERY', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP接口--人脸识别日志查询');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (431, 1, 'shandong', 'https://jd.51yanhome.com:18088/api/v1/ss/check/in', 'healthcode.request.url', '', 'primary', 'Y', '0', 'admin', sysdate(), 'admin', sysdate(), '健康码');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (432, 49, '健康码查询', 'HEALTH_CODE_SEARCH', 'http_interface', NULL, 'default', 'Y', '0', 'admin', sysdate(), 'admin', sysdate(), 'http标准接口-健康码查询');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (433, 53, '开通人脸虹膜多模态', 'PERSON_FACE_IRIS_OPEN', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP交易接口--开通人脸虹膜多模态');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (434, 54, '关闭人脸虹膜多模态', 'PERSON_FACE_IRIS_CLOSE', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP交易接口--关闭人脸虹膜多模态');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (435, 55, '人脸1v1比对日志回传', 'PERSON_FACE_MATCH_LOG_BAK', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP交易接口--人脸1v1比对日志回传');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (436, 56, '人脸虹膜多模态识别日志回传', 'PERSON_FACE_IRIS_MULTI_LOG_BAK', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP交易接口--人脸虹膜多模态识别日志回传');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (437, 1, '人脸SDK', 'FACE', 'sys_sdk_type', NULL, 'success', 'Y', '0', 'admin', sysdate(), '', NULL, 'SDK类型--人脸SDK');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (438, 2, '指纹SDK', 'FINGER', 'sys_sdk_type', NULL, 'success', 'Y', '0', 'admin', sysdate(), '', NULL, 'SDK类型--指纹SDK');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (439, 3, '虹膜SDK', 'IRIS', 'sys_sdk_type', NULL, 'success', 'Y', '0', 'admin', sysdate(), '', NULL, 'SDK类型--虹膜SDK');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (440, 4, '指静脉SDK', 'FVEIN', 'sys_sdk_type', NULL, 'success', 'Y', '0', 'admin', sysdate(), '', NULL, 'SDK类型--指静脉SDK');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (441, 5, '多模态SDK', 'MULTI', 'sys_sdk_type', NULL, 'success', 'Y', '0', 'admin', sysdate(), '', NULL, 'SDK类型--多模态SDK');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (442, 57, '人脸虹膜多模态识别日志查询', 'PERSON_FACE_IRIS_MULTI_LOG_QUERY', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP接口--人脸虹膜多模态识别日志查询');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (444, 1, '常规设备', '1', 'client_device_type', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, '设备类型--常规设备');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (445, 2, '无感设备', '2', 'client_device_type', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, '设备类型--无感设备');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (446, 3, '高空坠物设备', '3', 'client_device_type', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, '设备类型--高空坠物设备');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (448, 59, '联网身份核查接口', 'PERSON_IDENTITY_VERIFICATION', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP接口--联网身份核查接口');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (449, 4, '考勤设备', '4', 'client_device_type', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, '设备类型--考勤设备');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (450, 1, '进', 'IN', 'device_direction', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, '设备进出方向--进');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (451, 2, '出', 'OUT', 'device_direction', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, '设备进出方向--出');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (452, 3, '未知', 'UNKNOWN', 'device_direction', NULL, NULL, 'N', '0', 'admin', sysdate(), 'admin', sysdate(), '设备进出方向--未知');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (453, 60, '人脸1v1比对日志查询', 'PERSON_FACE_MATCH_LOG_QUERY', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP交易接口--人脸1v1比对日志查询');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (454, 5, '人脸虹膜多模态', '4', 'channel_bio_attest_type', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, '人脸虹膜多模态');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (455, 0, '试用', 'TRIAL', 'sys_tenant_type', NULL, NULL, 'N', '0', 'admin', sysdate(), 'admin', sysdate(), '租户类型--试用租户');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (456, 1, '正式', 'FORMAL', 'sys_tenant_type', NULL, NULL, 'N', '0', 'admin', sysdate(), 'admin', sysdate(), '租户类型--正式租户');



INSERT INTO device_model(id, model_code, model_name, exterior_image, model_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355692391', 'ECF201', 'ECF201', NULL, '人脸设备', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_model(id, model_code, model_name, exterior_image, model_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355692392', 'ECF203', 'ECF203', NULL, '人脸设备', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_model(id, model_code, model_name, exterior_image, model_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355692393', 'ECX332', 'ECX332', NULL, '人脸虹膜多模态设备', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_model(id, model_code, model_name, exterior_image, model_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355692394', 'ECX333', 'ECX333', NULL, '带测温和刷卡的人脸虹膜多模态设备', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_model(id, model_code, model_name, exterior_image, model_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355692395', 'ECF106', 'ECF106', NULL, '网络抓拍机', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_model(id, model_code, model_name, exterior_image, model_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355692396', 'Atlas500', 'Atlas500', NULL, 'Atlas500', NULL, NULL, sysdate(), NULL, 'super');


INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691327', 'appManagePwd', 'APP管理密码', 'APP管理密码', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691328', 'keyAlgorithm', '算法激活码', '算法激活码', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691329', 'httpBaseUrl', '接口地址', '接口地址', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691330', 'appKey', 'appKey', 'appKey', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691331', 'appSecret', 'appSecret', 'appSecret', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691332', 'workMode', '识别模式', '识别模式(0：智能模式 1：刷脸模式 2：身份证模式 3：只测温模式)', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691334', 'temperatureCheck', '测温开关', '测温开关(true：开启测温 false：关闭测温)', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691335', 'liveCheck', '检活开关', '检活开关(true：开启检活 false：关闭检活)', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691336', 'matchScore', '比对阈值', '比对阈值(取值范围0-100)', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691337', 'repeatTime', '去重时间', '去重时间(取值范围3-12)', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691338', 'maxFaceSize', '最大人脸', '最大人脸', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691339', 'minFaceSize', '最小人脸', '最小人脸', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691340', 'idVerificationCheck', '人证核验开关', '人证核验开关(true：开启核验 false：关闭核)', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691341', 'personIdcardThreshold', '人证核验阈值', '人证核验阈值(取值范围10-85)', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691342', 'healthCodeCheck', '健康码核验开关', '健康码核验开关(true：开启健康码核验 false：不核验健康码)', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691343', 'comparisonLib', '是否只识别库内人员', '是否只识别库内人员(true：只识别库内人员 false：识别所有人员)', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691344', 'healthDeviceCode', '健康码设备编号', '健康码设备编号', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691345', 'healthAddress', '健康码地址', '健康码地址', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691348', 'deviceStatus', '设备状态', '设备状态(0:禁用 1:启用)', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691349', 'irisMatchScore', '	虹膜比对阈值', '虹膜1:N比对阈值', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691350', 'faceMatchScore', '人脸比对阈值', '人脸1:N比对阈值', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691351', 'multiMatchScore', '多模态比对阈值', '多模态1:N比对阈值', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691352', 'multiScore', '多模态分值', '多模态分值', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691354', 'matchMode', '比对模式', '比对模式(0:后台比对 1:本地比对 2: 二次比对)', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691355', 'matchType', '比对方式', '比对方式(0:只虹膜比对 1:只人脸比对 2:虹膜和人脸同时别对 3:人脸或虹膜 4:多模态)', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691356', 'frameSkip', 'RTSP解析跳帧', 'RTSP解析跳帧', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691357', 'platformSyncFlag', '日志上传标记', '日志上传标记(0:实时 1:匀速 2:关闭)', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691358', 'platformSyncTime', '日志上传匀速间隔时间', '日志上传匀速间隔时间	', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691359', 'streamUrl', 'RTSP地址', 'RTSP地址', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691360', 'timeUrl', '时间服务器', '时间服务器(0:NTP服务器 1:自建API)', NULL, NULL, sysdate(), NULL, 'super');

INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203001942171968', 'ECF201', 'keyAlgorithm', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203001942179136', 'ECF201', 'httpBaseUrl', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203001942184256', 'ECF201', 'appKey', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203001942188352', 'ECF201', 'appSecret', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203001942192448', 'ECF201', 'workMode', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203001942195520', 'ECF201', 'temperatureCheck', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203001942199616', 'ECF201', 'liveCheck', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203001942202688', 'ECF201', 'matchScore', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203001942205760', 'ECF201', 'repeatTime', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203001942208832', 'ECF201', 'maxFaceSize', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203001942212928', 'ECF201', 'minFaceSize', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203001942216000', 'ECF201', 'idVerificationCheck', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203001942219072', 'ECF201', 'personIdcardThreshold', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203001942223168', 'ECF201', 'healthCodeCheck', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203001942226240', 'ECF201', 'comparisonLib', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203001942230336', 'ECF201', 'healthAddress', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203001942233408', 'ECF201', 'matchMode', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203002096651584', 'ECX332', 'httpBaseUrl', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203002096657728', 'ECX332', 'appKey', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203002096661824', 'ECX332', 'appSecret', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203002096664896', 'ECX332', 'deviceStatus', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203002096667968', 'ECX332', 'irisMatchScore', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203002096671040', 'ECX332', 'faceMatchScore', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203002096675136', 'ECX332', 'multiMatchScore', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203002096677184', 'ECX332', 'multiScore', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203002096680256', 'ECX332', 'matchMode', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203002096683328', 'ECX332', 'matchType', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203002179989824', 'ECX333', 'httpBaseUrl', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203002179996992', 'ECX333', 'appKey', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203002180000064', 'ECX333', 'appSecret', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203002180003136', 'ECX333', 'deviceStatus', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203002180006208', 'ECX333', 'irisMatchScore', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203002180011328', 'ECX333', 'faceMatchScore', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203002180014400', 'ECX333', 'multiMatchScore', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203002180018496', 'ECX333', 'multiScore', NULL, NULL,sysdate(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203002180021568', 'ECX333', 'matchMode', NULL, NULL,sysdate(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203002180024640', 'ECX333', 'matchType', NULL, NULL,sysdate(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203002278398272', 'Atlas500', 'httpBaseUrl', NULL, NULL,sysdate(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203002278404416', 'Atlas500', 'appKey', NULL, NULL,sysdate(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203002278407488', 'Atlas500', 'appSecret', NULL, NULL,sysdate(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203002278410560', 'Atlas500', 'matchScore', NULL, NULL,sysdate(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203002278413632', 'Atlas500', 'repeatTime', NULL, NULL,sysdate(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203002278416704', 'Atlas500', 'maxFaceSize', NULL, NULL,sysdate(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203002278420800', 'Atlas500', 'minFaceSize', NULL, NULL,sysdate(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203002278423872', 'Atlas500', 'frameSkip', NULL, NULL,sysdate(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203002278426944', 'Atlas500', 'platformSyncFlag', NULL, NULL,sysdate(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203002278431040', 'Atlas500', 'platformSyncTime', NULL, NULL,sysdate(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203002278434112', 'Atlas500', 'streamUrl', NULL, NULL,sysdate(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203002278437184', 'Atlas500', 'timeUrl', NULL, NULL,sysdate(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203002278437185', 'ECF201', 'appManagePwd', NULL, NULL, sysdate(), NULL, 'super');

INSERT INTO sys_job(job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, update_by, update_time, remark) VALUES (1, '钉钉部门和人员信息同步', 'DEFAULT', 'syncDingTalkDataTask.executeSync()', '0 0 3 /1 * ? *', '1', '1', '0', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_job(job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, update_by, update_time, remark) VALUES (2, '设备升级任务自动发布', 'DEFAULT', 'deviceUpdatePublishTask.execute()', '0 0/2 * * * ? *', '1', '1', '1', 'admin', sysdate(), '', sysdate(), '');
INSERT INTO sys_job(job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, update_by, update_time, remark) VALUES (3, '接口报文日志分区维护', 'DEFAULT', 'requestRecordTask.clearAndAddPartition(\'eyecool_assps\',30)', '0 0 0 * * ? *', '1', '1', '0', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_job(job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, update_by, update_time, remark) VALUES (4, '人脸1v1日志分区维护', 'DEFAULT', 'clearBioMatchLogTask.clearFaceMatchLog(365,false,\'eyecool_assps\')', '0 0 1 * * ? *', '1', '1', '0', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_job(job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, update_by, update_time, remark) VALUES (5, '人脸1vN日志分区维护', 'DEFAULT', 'clearBioMatchLogTask.clearFaceSearchLog(365,false,\'eyecool_assps\')', '0 0 1 * * ? *', '1', '1', '0', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_job(job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, update_by, update_time, remark) VALUES (6, '指纹1v1日志分区维护', 'DEFAULT', 'clearBioMatchLogTask.clearFingerMatchLog(365,false,\'eyecool_assps\')', '0 0 1 * * ? *', '1', '1', '0', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_job(job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, update_by, update_time, remark) VALUES (7, '指纹1vN日志分区维护', 'DEFAULT', 'clearBioMatchLogTask.clearFingerSearchLog(365,false,\'eyecool_assps\')', '0 0 1 * * ? *', '1', '1', '0', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_job(job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, update_by, update_time, remark) VALUES (8, '虹膜1v1日志分区维护', 'DEFAULT', 'clearBioMatchLogTask.clearIrisMatchLog(365,false,\'eyecool_assps\')', '0 0 1 * * ? *', '1', '1', '0', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_job(job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, update_by, update_time, remark) VALUES (9, '虹膜1vN日志分区维护', 'DEFAULT', 'clearBioMatchLogTask.clearIrisSearchLog(365,false,\'eyecool_assps\')', '0 0 1 * * ? *', '1', '1', '0', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_job(job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, update_by, update_time, remark) VALUES (10, '人脸虹膜多模态1vN日志分区维护', 'DEFAULT', 'clearBioMatchLogTask.clearMulitSearchLog(365,false,\'eyecool_assps\')', '0 0 1 * * ? *', '1', '1', '0', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_job(job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, update_by, update_time, remark) VALUES (11, '健康码日志分区维护', 'DEFAULT', 'clearBioMatchLogTask.clearHealthcodeLog(365,\'eyecool_assps\')', '0 0 1 * * ? *', '1', '1', '0', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_job(job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, update_by, update_time, remark) VALUES (12, '消息日志分区维护', 'DEFAULT', 'msgLogTask.clearAndAddPartition(\'eyecool_assps\',365)', '0 0 1 * * ? *', '1', '1', '0', 'admin', sysdate(), '', sysdate(), '');
INSERT INTO sys_job(job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, update_by, update_time, remark) VALUES (13, '203设备人员实时同步', 'DEFAULT', 'syncTask.syncAllDevice()', '*/5 * * * * ?', '1', '1', '0', 'admin', '2021-07-07 17:36:12', '', '2021-07-07 17:36:16', '');


INSERT INTO qrtz_job_details(sched_name, job_name, job_group, description, job_class_name, is_durable, is_nonconcurrent, is_update_data, requests_recovery, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME1', 'DEFAULT', NULL, 'cn.eyecool.quartz.util.QuartzDisallowConcurrentExecution', '0', '1', '0', '0', 0xACED0005737200156F72672E71756172747A2E4A6F62446174614D61709FB083E8BFA9B0CB020000787200266F72672E71756172747A2E7574696C732E537472696E674B65794469727479466C61674D61708208E8C3FBC55D280200015A0013616C6C6F77735472616E7369656E74446174617872001D6F72672E71756172747A2E7574696C732E4469727479466C61674D617013E62EAD28760ACE0200025A000564697274794C00036D617074000F4C6A6176612F7574696C2F4D61703B787001737200116A6176612E7574696C2E486173684D61700507DAC1C31660D103000246000A6C6F6164466163746F724900097468726573686F6C6478703F4000000000000C7708000000100000000174000F5441534B5F50524F504552544945537372001F636E2E657965636F6F6C2E71756172747A2E646F6D61696E2E5379734A6F6200000000000000010200084C000A636F6E63757272656E747400124C6A6176612F6C616E672F537472696E673B4C000E63726F6E45787072657373696F6E71007E00094C000C696E766F6B6554617267657471007E00094C00086A6F6247726F757071007E00094C00056A6F6249647400104C6A6176612F6C616E672F4C6F6E673B4C00076A6F624E616D6571007E00094C000D6D697366697265506F6C69637971007E00094C000673746174757371007E000978720028636E2E657965636F6F6C2E636F6D6D6F6E2E636F72652E646F6D61696E2E42617365456E7469747900000000000000010200094C0009626567696E54696D6571007E00094C0008637265617465427971007E00094C000A63726561746554696D657400104C6A6176612F7574696C2F446174653B4C0007656E6454696D6571007E00094C0006706172616D7371007E00034C000672656D61726B71007E00094C000B73656172636856616C756571007E00094C0008757064617465427971007E00094C000A75706461746554696D6571007E000C78707074000561646D696E7372000E6A6176612E7574696C2E44617465686A81014B5974190300007870770800000179AB78A8907870707400007070707400013174000E3020302033202F31202A203F202A74002273796E6344696E6754616C6B446174615461736B2E6578656375746553796E63282974000744454641554C547372000E6A6176612E6C616E672E4C6F6E673B8BE490CC8F23DF0200014A000576616C7565787200106A6176612E6C616E672E4E756D62657286AC951D0B94E08B02000078700000000000000001740021E99289E99289E983A8E997A8E5928CE4BABAE59198E4BFA1E681AFE5908CE6ADA574000131740001307800);
INSERT INTO qrtz_job_details(sched_name, job_name, job_group, description, job_class_name, is_durable, is_nonconcurrent, is_update_data, requests_recovery, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME10', 'DEFAULT', NULL, 'cn.eyecool.quartz.util.QuartzDisallowConcurrentExecution', '0', '1', '0', '0', 0xACED0005737200156F72672E71756172747A2E4A6F62446174614D61709FB083E8BFA9B0CB020000787200266F72672E71756172747A2E7574696C732E537472696E674B65794469727479466C61674D61708208E8C3FBC55D280200015A0013616C6C6F77735472616E7369656E74446174617872001D6F72672E71756172747A2E7574696C732E4469727479466C61674D617013E62EAD28760ACE0200025A000564697274794C00036D617074000F4C6A6176612F7574696C2F4D61703B787001737200116A6176612E7574696C2E486173684D61700507DAC1C31660D103000246000A6C6F6164466163746F724900097468726573686F6C6478703F4000000000000C7708000000100000000174000F5441534B5F50524F504552544945537372001F636E2E657965636F6F6C2E71756172747A2E646F6D61696E2E5379734A6F6200000000000000010200084C000A636F6E63757272656E747400124C6A6176612F6C616E672F537472696E673B4C000E63726F6E45787072657373696F6E71007E00094C000C696E766F6B6554617267657471007E00094C00086A6F6247726F757071007E00094C00056A6F6249647400104C6A6176612F6C616E672F4C6F6E673B4C00076A6F624E616D6571007E00094C000D6D697366697265506F6C69637971007E00094C000673746174757371007E000978720028636E2E657965636F6F6C2E636F6D6D6F6E2E636F72652E646F6D61696E2E42617365456E7469747900000000000000010200094C0009626567696E54696D6571007E00094C0008637265617465427971007E00094C000A63726561746554696D657400104C6A6176612F7574696C2F446174653B4C0007656E6454696D6571007E00094C0006706172616D7371007E00034C000672656D61726B71007E00094C000B73656172636856616C756571007E00094C0008757064617465427971007E00094C000A75706461746554696D6571007E000C78707074000561646D696E7372000E6A6176612E7574696C2E44617465686A81014B5974190300007870770800000179AB8DB7307870737200176A6176612E7574696C2E4C696E6B6564486173684D617034C04E5C106CC0FB0200015A000B6163636573734F726465727871007E00053F400000000000007708000000100000000078007400007074000561646D696E707400013174000D3020302031202A202A203F202A740043636C65617242696F4D617463684C6F675461736B2E636C6561724D756C69745365617263684C6F67283336352C66616C73652C27657965636F6F6C5F6173737073272974000744454641554C547372000E6A6176612E6C616E672E4C6F6E673B8BE490CC8F23DF0200014A000576616C7565787200106A6176612E6C616E672E4E756D62657286AC951D0B94E08B0200007870000000000000000A74002AE4BABAE884B8E899B9E8869CE5A49AE6A8A1E6808131764EE697A5E5BF97E58886E58CBAE7BBB4E68AA474000131740001307800);
INSERT INTO qrtz_job_details(sched_name, job_name, job_group, description, job_class_name, is_durable, is_nonconcurrent, is_update_data, requests_recovery, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME11', 'DEFAULT', NULL, 'cn.eyecool.quartz.util.QuartzDisallowConcurrentExecution', '0', '1', '0', '0', 0xACED0005737200156F72672E71756172747A2E4A6F62446174614D61709FB083E8BFA9B0CB020000787200266F72672E71756172747A2E7574696C732E537472696E674B65794469727479466C61674D61708208E8C3FBC55D280200015A0013616C6C6F77735472616E7369656E74446174617872001D6F72672E71756172747A2E7574696C732E4469727479466C61674D617013E62EAD28760ACE0200025A000564697274794C00036D617074000F4C6A6176612F7574696C2F4D61703B787001737200116A6176612E7574696C2E486173684D61700507DAC1C31660D103000246000A6C6F6164466163746F724900097468726573686F6C6478703F4000000000000C7708000000100000000174000F5441534B5F50524F504552544945537372001F636E2E657965636F6F6C2E71756172747A2E646F6D61696E2E5379734A6F6200000000000000010200084C000A636F6E63757272656E747400124C6A6176612F6C616E672F537472696E673B4C000E63726F6E45787072657373696F6E71007E00094C000C696E766F6B6554617267657471007E00094C00086A6F6247726F757071007E00094C00056A6F6249647400104C6A6176612F6C616E672F4C6F6E673B4C00076A6F624E616D6571007E00094C000D6D697366697265506F6C69637971007E00094C000673746174757371007E000978720028636E2E657965636F6F6C2E636F6D6D6F6E2E636F72652E646F6D61696E2E42617365456E7469747900000000000000010200094C0009626567696E54696D6571007E00094C0008637265617465427971007E00094C000A63726561746554696D657400104C6A6176612F7574696C2F446174653B4C0007656E6454696D6571007E00094C0006706172616D7371007E00034C000672656D61726B71007E00094C000B73656172636856616C756571007E00094C0008757064617465427971007E00094C000A75706461746554696D6571007E000C78707074000561646D696E7372000E6A6176612E7574696C2E44617465686A81014B5974190300007870770800000179AB8E76987870737200176A6176612E7574696C2E4C696E6B6564486173684D617034C04E5C106CC0FB0200015A000B6163636573734F726465727871007E00053F400000000000007708000000100000000078007400007074000561646D696E707400013174000D3020302031202A202A203F202A74003C636C65617242696F4D617463684C6F675461736B2E636C6561724865616C7468636F64654C6F67283336352C27657965636F6F6C5F6173737073272974000744454641554C547372000E6A6176612E6C616E672E4C6F6E673B8BE490CC8F23DF0200014A000576616C7565787200106A6176612E6C616E672E4E756D62657286AC951D0B94E08B0200007870000000000000000B74001BE581A5E5BAB7E7A081E697A5E5BF97E58886E58CBAE7BBB4E68AA474000131740001307800);
INSERT INTO qrtz_job_details(sched_name, job_name, job_group, description, job_class_name, is_durable, is_nonconcurrent, is_update_data, requests_recovery, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME2', 'DEFAULT', NULL, 'cn.eyecool.quartz.util.QuartzDisallowConcurrentExecution', '0', '1', '0', '0', 0xACED0005737200156F72672E71756172747A2E4A6F62446174614D61709FB083E8BFA9B0CB020000787200266F72672E71756172747A2E7574696C732E537472696E674B65794469727479466C61674D61708208E8C3FBC55D280200015A0013616C6C6F77735472616E7369656E74446174617872001D6F72672E71756172747A2E7574696C732E4469727479466C61674D617013E62EAD28760ACE0200025A000564697274794C00036D617074000F4C6A6176612F7574696C2F4D61703B787001737200116A6176612E7574696C2E486173684D61700507DAC1C31660D103000246000A6C6F6164466163746F724900097468726573686F6C6478703F4000000000000C7708000000100000000174000F5441534B5F50524F504552544945537372001F636E2E657965636F6F6C2E71756172747A2E646F6D61696E2E5379734A6F6200000000000000010200084C000A636F6E63757272656E747400124C6A6176612F6C616E672F537472696E673B4C000E63726F6E45787072657373696F6E71007E00094C000C696E766F6B6554617267657471007E00094C00086A6F6247726F757071007E00094C00056A6F6249647400104C6A6176612F6C616E672F4C6F6E673B4C00076A6F624E616D6571007E00094C000D6D697366697265506F6C69637971007E00094C000673746174757371007E000978720028636E2E657965636F6F6C2E636F6D6D6F6E2E636F72652E646F6D61696E2E42617365456E7469747900000000000000010200094C0009626567696E54696D6571007E00094C0008637265617465427971007E00094C000A63726561746554696D657400104C6A6176612F7574696C2F446174653B4C0007656E6454696D6571007E00094C0006706172616D7371007E00034C000672656D61726B71007E00094C000B73656172636856616C756571007E00094C0008757064617465427971007E00094C000A75706461746554696D6571007E000C78707074000561646D696E7372000E6A6176612E7574696C2E44617465686A81014B5974190300007870770800000179AB7A27607870707400007070707400013174000F3020302F32202A202A202A203F202A7400216465766963655570646174655075626C6973685461736B2E65786563757465282974000744454641554C547372000E6A6176612E6C616E672E4C6F6E673B8BE490CC8F23DF0200014A000576616C7565787200106A6176612E6C616E672E4E756D62657286AC951D0B94E08B0200007870000000000000000274001EE8AEBEE5A487E58D87E7BAA7E4BBBBE58AA1E887AAE58AA8E58F91E5B88374000131740001317800);
INSERT INTO qrtz_job_details(sched_name, job_name, job_group, description, job_class_name, is_durable, is_nonconcurrent, is_update_data, requests_recovery, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME3', 'DEFAULT', NULL, 'cn.eyecool.quartz.util.QuartzDisallowConcurrentExecution', '0', '1', '0', '0', 0xACED0005737200156F72672E71756172747A2E4A6F62446174614D61709FB083E8BFA9B0CB020000787200266F72672E71756172747A2E7574696C732E537472696E674B65794469727479466C61674D61708208E8C3FBC55D280200015A0013616C6C6F77735472616E7369656E74446174617872001D6F72672E71756172747A2E7574696C732E4469727479466C61674D617013E62EAD28760ACE0200025A000564697274794C00036D617074000F4C6A6176612F7574696C2F4D61703B787001737200116A6176612E7574696C2E486173684D61700507DAC1C31660D103000246000A6C6F6164466163746F724900097468726573686F6C6478703F4000000000000C7708000000100000000174000F5441534B5F50524F504552544945537372001F636E2E657965636F6F6C2E71756172747A2E646F6D61696E2E5379734A6F6200000000000000010200084C000A636F6E63757272656E747400124C6A6176612F6C616E672F537472696E673B4C000E63726F6E45787072657373696F6E71007E00094C000C696E766F6B6554617267657471007E00094C00086A6F6247726F757071007E00094C00056A6F6249647400104C6A6176612F6C616E672F4C6F6E673B4C00076A6F624E616D6571007E00094C000D6D697366697265506F6C69637971007E00094C000673746174757371007E000978720028636E2E657965636F6F6C2E636F6D6D6F6E2E636F72652E646F6D61696E2E42617365456E7469747900000000000000010200094C0009626567696E54696D6571007E00094C0008637265617465427971007E00094C000A63726561746554696D657400104C6A6176612F7574696C2F446174653B4C0007656E6454696D6571007E00094C0006706172616D7371007E00034C000672656D61726B71007E00094C000B73656172636856616C756571007E00094C0008757064617465427971007E00094C000A75706461746554696D6571007E000C78707074000561646D696E7372000E6A6176612E7574696C2E44617465686A81014B5974190300007870770800000179AB816AC07870707400007070707400013174000D3020302030202A202A203F202A74003A726571756573745265636F72645461736B2E636C656172416E64416464506172746974696F6E2827657965636F6F6C5F6173737073272C33302974000744454641554C547372000E6A6176612E6C616E672E4C6F6E673B8BE490CC8F23DF0200014A000576616C7565787200106A6176612E6C616E672E4E756D62657286AC951D0B94E08B0200007870000000000000000374001EE68EA5E58FA3E68AA5E69687E697A5E5BF97E58886E58CBAE7BBB4E68AA474000131740001307800);
INSERT INTO qrtz_job_details(sched_name, job_name, job_group, description, job_class_name, is_durable, is_nonconcurrent, is_update_data, requests_recovery, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME4', 'DEFAULT', NULL, 'cn.eyecool.quartz.util.QuartzDisallowConcurrentExecution', '0', '1', '0', '0', 0xACED0005737200156F72672E71756172747A2E4A6F62446174614D61709FB083E8BFA9B0CB020000787200266F72672E71756172747A2E7574696C732E537472696E674B65794469727479466C61674D61708208E8C3FBC55D280200015A0013616C6C6F77735472616E7369656E74446174617872001D6F72672E71756172747A2E7574696C732E4469727479466C61674D617013E62EAD28760ACE0200025A000564697274794C00036D617074000F4C6A6176612F7574696C2F4D61703B787001737200116A6176612E7574696C2E486173684D61700507DAC1C31660D103000246000A6C6F6164466163746F724900097468726573686F6C6478703F4000000000000C7708000000100000000174000F5441534B5F50524F504552544945537372001F636E2E657965636F6F6C2E71756172747A2E646F6D61696E2E5379734A6F6200000000000000010200084C000A636F6E63757272656E747400124C6A6176612F6C616E672F537472696E673B4C000E63726F6E45787072657373696F6E71007E00094C000C696E766F6B6554617267657471007E00094C00086A6F6247726F757071007E00094C00056A6F6249647400104C6A6176612F6C616E672F4C6F6E673B4C00076A6F624E616D6571007E00094C000D6D697366697265506F6C69637971007E00094C000673746174757371007E000978720028636E2E657965636F6F6C2E636F6D6D6F6E2E636F72652E646F6D61696E2E42617365456E7469747900000000000000010200094C0009626567696E54696D6571007E00094C0008637265617465427971007E00094C000A63726561746554696D657400104C6A6176612F7574696C2F446174653B4C0007656E6454696D6571007E00094C0006706172616D7371007E00034C000672656D61726B71007E00094C000B73656172636856616C756571007E00094C0008757064617465427971007E00094C000A75706461746554696D6571007E000C78707074000561646D696E7372000E6A6176612E7574696C2E44617465686A81014B5974190300007870770800000179AB8840C07870707400007070707400013174000D3020302031202A202A203F202A740041636C65617242696F4D617463684C6F675461736B2E636C656172466163654D617463684C6F67283336352C66616C73652C27657965636F6F6C5F6173737073272974000744454641554C547372000E6A6176612E6C616E672E4C6F6E673B8BE490CC8F23DF0200014A000576616C7565787200106A6176612E6C616E672E4E756D62657286AC951D0B94E08B0200007870000000000000000474001BE4BABAE884B8317631E697A5E5BF97E58886E58CBAE7BBB4E68AA474000131740001307800);
INSERT INTO qrtz_job_details(sched_name, job_name, job_group, description, job_class_name, is_durable, is_nonconcurrent, is_update_data, requests_recovery, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME5', 'DEFAULT', NULL, 'cn.eyecool.quartz.util.QuartzDisallowConcurrentExecution', '0', '1', '0', '0', 0xACED0005737200156F72672E71756172747A2E4A6F62446174614D61709FB083E8BFA9B0CB020000787200266F72672E71756172747A2E7574696C732E537472696E674B65794469727479466C61674D61708208E8C3FBC55D280200015A0013616C6C6F77735472616E7369656E74446174617872001D6F72672E71756172747A2E7574696C732E4469727479466C61674D617013E62EAD28760ACE0200025A000564697274794C00036D617074000F4C6A6176612F7574696C2F4D61703B787001737200116A6176612E7574696C2E486173684D61700507DAC1C31660D103000246000A6C6F6164466163746F724900097468726573686F6C6478703F4000000000000C7708000000100000000174000F5441534B5F50524F504552544945537372001F636E2E657965636F6F6C2E71756172747A2E646F6D61696E2E5379734A6F6200000000000000010200084C000A636F6E63757272656E747400124C6A6176612F6C616E672F537472696E673B4C000E63726F6E45787072657373696F6E71007E00094C000C696E766F6B6554617267657471007E00094C00086A6F6247726F757071007E00094C00056A6F6249647400104C6A6176612F6C616E672F4C6F6E673B4C00076A6F624E616D6571007E00094C000D6D697366697265506F6C69637971007E00094C000673746174757371007E000978720028636E2E657965636F6F6C2E636F6D6D6F6E2E636F72652E646F6D61696E2E42617365456E7469747900000000000000010200094C0009626567696E54696D6571007E00094C0008637265617465427971007E00094C000A63726561746554696D657400104C6A6176612F7574696C2F446174653B4C0007656E6454696D6571007E00094C0006706172616D7371007E00034C000672656D61726B71007E00094C000B73656172636856616C756571007E00094C0008757064617465427971007E00094C000A75706461746554696D6571007E000C78707074000561646D696E7372000E6A6176612E7574696C2E44617465686A81014B5974190300007870770800000179AB891F687870707400007070707400013174000D3020302031202A202A203F202A740042636C65617242696F4D617463684C6F675461736B2E636C656172466163655365617263684C6F67283336352C66616C73652C27657965636F6F6C5F6173737073272974000744454641554C547372000E6A6176612E6C616E672E4C6F6E673B8BE490CC8F23DF0200014A000576616C7565787200106A6176612E6C616E672E4E756D62657286AC951D0B94E08B0200007870000000000000000574001BE4BABAE884B831764EE697A5E5BF97E58886E58CBAE7BBB4E68AA474000131740001307800);
INSERT INTO qrtz_job_details(sched_name, job_name, job_group, description, job_class_name, is_durable, is_nonconcurrent, is_update_data, requests_recovery, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME6', 'DEFAULT', NULL, 'cn.eyecool.quartz.util.QuartzDisallowConcurrentExecution', '0', '1', '0', '0', 0xACED0005737200156F72672E71756172747A2E4A6F62446174614D61709FB083E8BFA9B0CB020000787200266F72672E71756172747A2E7574696C732E537472696E674B65794469727479466C61674D61708208E8C3FBC55D280200015A0013616C6C6F77735472616E7369656E74446174617872001D6F72672E71756172747A2E7574696C732E4469727479466C61674D617013E62EAD28760ACE0200025A000564697274794C00036D617074000F4C6A6176612F7574696C2F4D61703B787001737200116A6176612E7574696C2E486173684D61700507DAC1C31660D103000246000A6C6F6164466163746F724900097468726573686F6C6478703F4000000000000C7708000000100000000174000F5441534B5F50524F504552544945537372001F636E2E657965636F6F6C2E71756172747A2E646F6D61696E2E5379734A6F6200000000000000010200084C000A636F6E63757272656E747400124C6A6176612F6C616E672F537472696E673B4C000E63726F6E45787072657373696F6E71007E00094C000C696E766F6B6554617267657471007E00094C00086A6F6247726F757071007E00094C00056A6F6249647400104C6A6176612F6C616E672F4C6F6E673B4C00076A6F624E616D6571007E00094C000D6D697366697265506F6C69637971007E00094C000673746174757371007E000978720028636E2E657965636F6F6C2E636F6D6D6F6E2E636F72652E646F6D61696E2E42617365456E7469747900000000000000010200094C0009626567696E54696D6571007E00094C0008637265617465427971007E00094C000A63726561746554696D657400104C6A6176612F7574696C2F446174653B4C0007656E6454696D6571007E00094C0006706172616D7371007E00034C000672656D61726B71007E00094C000B73656172636856616C756571007E00094C0008757064617465427971007E00094C000A75706461746554696D6571007E000C78707074000561646D696E7372000E6A6176612E7574696C2E44617465686A81014B5974190300007870770800000179AB8A1D507870707400007070707400013174000D3020302031202A202A203F202A740043636C65617242696F4D617463684C6F675461736B2E636C65617246696E6765724D617463684C6F67283336352C66616C73652C27657965636F6F6C5F6173737073272974000744454641554C547372000E6A6176612E6C616E672E4C6F6E673B8BE490CC8F23DF0200014A000576616C7565787200106A6176612E6C616E672E4E756D62657286AC951D0B94E08B0200007870000000000000000674001BE68C87E7BAB9317631E697A5E5BF97E58886E58CBAE7BBB4E68AA474000131740001307800);
INSERT INTO qrtz_job_details(sched_name, job_name, job_group, description, job_class_name, is_durable, is_nonconcurrent, is_update_data, requests_recovery, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME7', 'DEFAULT', NULL, 'cn.eyecool.quartz.util.QuartzDisallowConcurrentExecution', '0', '1', '0', '0', 0xACED0005737200156F72672E71756172747A2E4A6F62446174614D61709FB083E8BFA9B0CB020000787200266F72672E71756172747A2E7574696C732E537472696E674B65794469727479466C61674D61708208E8C3FBC55D280200015A0013616C6C6F77735472616E7369656E74446174617872001D6F72672E71756172747A2E7574696C732E4469727479466C61674D617013E62EAD28760ACE0200025A000564697274794C00036D617074000F4C6A6176612F7574696C2F4D61703B787001737200116A6176612E7574696C2E486173684D61700507DAC1C31660D103000246000A6C6F6164466163746F724900097468726573686F6C6478703F4000000000000C7708000000100000000174000F5441534B5F50524F504552544945537372001F636E2E657965636F6F6C2E71756172747A2E646F6D61696E2E5379734A6F6200000000000000010200084C000A636F6E63757272656E747400124C6A6176612F6C616E672F537472696E673B4C000E63726F6E45787072657373696F6E71007E00094C000C696E766F6B6554617267657471007E00094C00086A6F6247726F757071007E00094C00056A6F6249647400104C6A6176612F6C616E672F4C6F6E673B4C00076A6F624E616D6571007E00094C000D6D697366697265506F6C69637971007E00094C000673746174757371007E000978720028636E2E657965636F6F6C2E636F6D6D6F6E2E636F72652E646F6D61696E2E42617365456E7469747900000000000000010200094C0009626567696E54696D6571007E00094C0008637265617465427971007E00094C000A63726561746554696D657400104C6A6176612F7574696C2F446174653B4C0007656E6454696D6571007E00094C0006706172616D7371007E00034C000672656D61726B71007E00094C000B73656172636856616C756571007E00094C0008757064617465427971007E00094C000A75706461746554696D6571007E000C78707074000561646D696E7372000E6A6176612E7574696C2E44617465686A81014B5974190300007870770800000179AB8AF8107870707400007070707400013174000D3020302031202A202A203F202A740044636C65617242696F4D617463684C6F675461736B2E636C65617246696E6765725365617263684C6F67283336352C66616C73652C27657965636F6F6C5F6173737073272974000744454641554C547372000E6A6176612E6C616E672E4C6F6E673B8BE490CC8F23DF0200014A000576616C7565787200106A6176612E6C616E672E4E756D62657286AC951D0B94E08B0200007870000000000000000774001BE68C87E7BAB931764EE697A5E5BF97E58886E58CBAE7BBB4E68AA474000131740001307800);
INSERT INTO qrtz_job_details(sched_name, job_name, job_group, description, job_class_name, is_durable, is_nonconcurrent, is_update_data, requests_recovery, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME8', 'DEFAULT', NULL, 'cn.eyecool.quartz.util.QuartzDisallowConcurrentExecution', '0', '1', '0', '0', 0xACED0005737200156F72672E71756172747A2E4A6F62446174614D61709FB083E8BFA9B0CB020000787200266F72672E71756172747A2E7574696C732E537472696E674B65794469727479466C61674D61708208E8C3FBC55D280200015A0013616C6C6F77735472616E7369656E74446174617872001D6F72672E71756172747A2E7574696C732E4469727479466C61674D617013E62EAD28760ACE0200025A000564697274794C00036D617074000F4C6A6176612F7574696C2F4D61703B787001737200116A6176612E7574696C2E486173684D61700507DAC1C31660D103000246000A6C6F6164466163746F724900097468726573686F6C6478703F4000000000000C7708000000100000000174000F5441534B5F50524F504552544945537372001F636E2E657965636F6F6C2E71756172747A2E646F6D61696E2E5379734A6F6200000000000000010200084C000A636F6E63757272656E747400124C6A6176612F6C616E672F537472696E673B4C000E63726F6E45787072657373696F6E71007E00094C000C696E766F6B6554617267657471007E00094C00086A6F6247726F757071007E00094C00056A6F6249647400104C6A6176612F6C616E672F4C6F6E673B4C00076A6F624E616D6571007E00094C000D6D697366697265506F6C69637971007E00094C000673746174757371007E000978720028636E2E657965636F6F6C2E636F6D6D6F6E2E636F72652E646F6D61696E2E42617365456E7469747900000000000000010200094C0009626567696E54696D6571007E00094C0008637265617465427971007E00094C000A63726561746554696D657400104C6A6176612F7574696C2F446174653B4C0007656E6454696D6571007E00094C0006706172616D7371007E00034C000672656D61726B71007E00094C000B73656172636856616C756571007E00094C0008757064617465427971007E00094C000A75706461746554696D6571007E000C78707074000561646D696E7372000E6A6176612E7574696C2E44617465686A81014B5974190300007870770800000179AB8BD2D07870707400007070707400013174000D3020302031202A202A203F202A740041636C65617242696F4D617463684C6F675461736B2E636C656172497269734D617463684C6F67283336352C66616C73652C27657965636F6F6C5F6173737073272974000744454641554C547372000E6A6176612E6C616E672E4C6F6E673B8BE490CC8F23DF0200014A000576616C7565787200106A6176612E6C616E672E4E756D62657286AC951D0B94E08B0200007870000000000000000874001BE899B9E8869C317631E697A5E5BF97E58886E58CBAE7BBB4E68AA474000131740001307800);
INSERT INTO qrtz_job_details(sched_name, job_name, job_group, description, job_class_name, is_durable, is_nonconcurrent, is_update_data, requests_recovery, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME9', 'DEFAULT', NULL, 'cn.eyecool.quartz.util.QuartzDisallowConcurrentExecution', '0', '1', '0', '0', 0xACED0005737200156F72672E71756172747A2E4A6F62446174614D61709FB083E8BFA9B0CB020000787200266F72672E71756172747A2E7574696C732E537472696E674B65794469727479466C61674D61708208E8C3FBC55D280200015A0013616C6C6F77735472616E7369656E74446174617872001D6F72672E71756172747A2E7574696C732E4469727479466C61674D617013E62EAD28760ACE0200025A000564697274794C00036D617074000F4C6A6176612F7574696C2F4D61703B787001737200116A6176612E7574696C2E486173684D61700507DAC1C31660D103000246000A6C6F6164466163746F724900097468726573686F6C6478703F4000000000000C7708000000100000000174000F5441534B5F50524F504552544945537372001F636E2E657965636F6F6C2E71756172747A2E646F6D61696E2E5379734A6F6200000000000000010200084C000A636F6E63757272656E747400124C6A6176612F6C616E672F537472696E673B4C000E63726F6E45787072657373696F6E71007E00094C000C696E766F6B6554617267657471007E00094C00086A6F6247726F757071007E00094C00056A6F6249647400104C6A6176612F6C616E672F4C6F6E673B4C00076A6F624E616D6571007E00094C000D6D697366697265506F6C69637971007E00094C000673746174757371007E000978720028636E2E657965636F6F6C2E636F6D6D6F6E2E636F72652E646F6D61696E2E42617365456E7469747900000000000000010200094C0009626567696E54696D6571007E00094C0008637265617465427971007E00094C000A63726561746554696D657400104C6A6176612F7574696C2F446174653B4C0007656E6454696D6571007E00094C0006706172616D7371007E00034C000672656D61726B71007E00094C000B73656172636856616C756571007E00094C0008757064617465427971007E00094C000A75706461746554696D6571007E000C78707074000561646D696E7372000E6A6176612E7574696C2E44617465686A81014B5974190300007870770800000179AB8CA9A87870707400007070707400013174000D3020302031202A202A203F202A740042636C65617242696F4D617463684C6F675461736B2E636C656172497269735365617263684C6F67283336352C66616C73652C27657965636F6F6C5F6173737073272974000744454641554C547372000E6A6176612E6C616E672E4C6F6E673B8BE490CC8F23DF0200014A000576616C7565787200106A6176612E6C616E672E4E756D62657286AC951D0B94E08B0200007870000000000000000974001BE899B9E8869C31764EE697A5E5BF97E58886E58CBAE7BBB4E68AA474000131740001307800);
INSERT INTO qrtz_job_details(sched_name, job_name, job_group, description, job_class_name, is_durable, is_nonconcurrent, is_update_data, requests_recovery, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME12', 'DEFAULT', NULL, 'cn.eyecool.quartz.util.QuartzDisallowConcurrentExecution', '0', '1', '0', '0', 0xACED0005737200156F72672E71756172747A2E4A6F62446174614D61709FB083E8BFA9B0CB020000787200266F72672E71756172747A2E7574696C732E537472696E674B65794469727479466C61674D61708208E8C3FBC55D280200015A0013616C6C6F77735472616E7369656E74446174617872001D6F72672E71756172747A2E7574696C732E4469727479466C61674D617013E62EAD28760ACE0200025A000564697274794C00036D617074000F4C6A6176612F7574696C2F4D61703B787001737200116A6176612E7574696C2E486173684D61700507DAC1C31660D103000246000A6C6F6164466163746F724900097468726573686F6C6478703F4000000000000C7708000000100000000174000F5441534B5F50524F504552544945537372001F636E2E657965636F6F6C2E71756172747A2E646F6D61696E2E5379734A6F6200000000000000010200084C000A636F6E63757272656E747400124C6A6176612F6C616E672F537472696E673B4C000E63726F6E45787072657373696F6E71007E00094C000C696E766F6B6554617267657471007E00094C00086A6F6247726F757071007E00094C00056A6F6249647400104C6A6176612F6C616E672F4C6F6E673B4C00076A6F624E616D6571007E00094C000D6D697366697265506F6C69637971007E00094C000673746174757371007E000978720028636E2E657965636F6F6C2E636F6D6D6F6E2E636F72652E646F6D61696E2E42617365456E7469747900000000000000010200094C0009626567696E54696D6571007E00094C0008637265617465427971007E00094C000A63726561746554696D657400104C6A6176612F7574696C2F446174653B4C0007656E6454696D6571007E00094C0006706172616D7371007E00034C000672656D61726B71007E00094C000B73656172636856616C756571007E00094C0008757064617465427971007E00094C000A75706461746554696D6571007E000C78707074000561646D696E707070707070707400013174000D3020302031202A202A203F202A7400346D73674C6F675461736B2E636C656172416E64416464506172746974696F6E2827657965636F6F6C5F6173737073272C3336352974000744454641554C547372000E6A6176612E6C616E672E4C6F6E673B8BE490CC8F23DF0200014A000576616C7565787200106A6176612E6C616E672E4E756D62657286AC951D0B94E08B0200007870000000000000000C740018E6B688E681AFE697A5E5BF97E58886E58CBAE7BBB4E68AA474000131740001317800);
INSERT INTO qrtz_job_details(sched_name, job_name, job_group, description, job_class_name, is_durable, is_nonconcurrent, is_update_data, requests_recovery, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME13', 'DEFAULT', NULL, 'cn.eyecool.quartz.util.QuartzDisallowConcurrentExecution', '0', '1', '0', '0', 0xACED0005737200156F72672E71756172747A2E4A6F62446174614D61709FB083E8BFA9B0CB020000787200266F72672E71756172747A2E7574696C732E537472696E674B65794469727479466C61674D61708208E8C3FBC55D280200015A0013616C6C6F77735472616E7369656E74446174617872001D6F72672E71756172747A2E7574696C732E4469727479466C61674D617013E62EAD28760ACE0200025A000564697274794C00036D617074000F4C6A6176612F7574696C2F4D61703B787001737200116A6176612E7574696C2E486173684D61700507DAC1C31660D103000246000A6C6F6164466163746F724900097468726573686F6C6478703F4000000000000C7708000000100000000174000F5441534B5F50524F504552544945537372001F636E2E657965636F6F6C2E71756172747A2E646F6D61696E2E5379734A6F6200000000000000010200084C000A636F6E63757272656E747400124C6A6176612F6C616E672F537472696E673B4C000E63726F6E45787072657373696F6E71007E00094C000C696E766F6B6554617267657471007E00094C00086A6F6247726F757071007E00094C00056A6F6249647400104C6A6176612F6C616E672F4C6F6E673B4C00076A6F624E616D6571007E00094C000D6D697366697265506F6C69637971007E00094C000673746174757371007E000978720028636E2E657965636F6F6C2E636F6D6D6F6E2E636F72652E646F6D61696E2E42617365456E7469747900000000000000010200094C0009626567696E54696D6571007E00094C0008637265617465427971007E00094C000A63726561746554696D657400104C6A6176612F7574696C2F446174653B4C0007656E6454696D6571007E00094C0006706172616D7371007E00034C000672656D61726B71007E00094C000B73656172636856616C756571007E00094C0008757064617465427971007E00094C000A75706461746554696D6571007E000C78707074000561646D696E707070707070707400013174000D2A2F35202A202A202A202A203F74001873796E635461736B2E73796E63416C6C446576696365282974000744454641554C547372000E6A6176612E6C616E672E4C6F6E673B8BE490CC8F23DF0200014A000576616C7565787200106A6176612E6C616E672E4E756D62657286AC951D0B94E08B0200007870000000000000000D74001B323033E8AEBEE5A487E4BABAE59198E5AE9EE697B6E5908CE6ADA574000131740001317800);


INSERT INTO qrtz_triggers(sched_name, trigger_name, trigger_group, job_name, job_group, description, next_fire_time, prev_fire_time, priority, trigger_state, trigger_type, start_time, end_time, calendar_name, misfire_instr, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME1', 'DEFAULT', 'TASK_CLASS_NAME1', 'DEFAULT', NULL, 1623092400000, 1623006000000, 5, 'WAITING', 'CRON', 1622799560000, 0, NULL, -1, '');
INSERT INTO qrtz_triggers(sched_name, trigger_name, trigger_group, job_name, job_group, description, next_fire_time, prev_fire_time, priority, trigger_state, trigger_type, start_time, end_time, calendar_name, misfire_instr, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME10', 'DEFAULT', 'TASK_CLASS_NAME10', 'DEFAULT', NULL, 1623171600000, -1, 5, 'WAITING', 'CRON', 1623121699000, 0, NULL, -1, '');
INSERT INTO qrtz_triggers(sched_name, trigger_name, trigger_group, job_name, job_group, description, next_fire_time, prev_fire_time, priority, trigger_state, trigger_type, start_time, end_time, calendar_name, misfire_instr, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME11', 'DEFAULT', 'TASK_CLASS_NAME11', 'DEFAULT', NULL, 1623171600000, -1, 5, 'WAITING', 'CRON', 1623121738000, 0, NULL, -1, '');
INSERT INTO qrtz_triggers(sched_name, trigger_name, trigger_group, job_name, job_group, description, next_fire_time, prev_fire_time, priority, trigger_state, trigger_type, start_time, end_time, calendar_name, misfire_instr, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME2', 'DEFAULT', 'TASK_CLASS_NAME2', 'DEFAULT', NULL, 1622799600000, -1, 5, 'PAUSED', 'CRON', 1622799560000, 0, NULL, -1, '');
INSERT INTO qrtz_triggers(sched_name, trigger_name, trigger_group, job_name, job_group, description, next_fire_time, prev_fire_time, priority, trigger_state, trigger_type, start_time, end_time, calendar_name, misfire_instr, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME3', 'DEFAULT', 'TASK_CLASS_NAME3', 'DEFAULT', NULL, 1623081600000, 1622995200000, 5, 'WAITING', 'CRON', 1622799561000, 0, NULL, -1, '');
INSERT INTO qrtz_triggers(sched_name, trigger_name, trigger_group, job_name, job_group, description, next_fire_time, prev_fire_time, priority, trigger_state, trigger_type, start_time, end_time, calendar_name, misfire_instr, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME4', 'DEFAULT', 'TASK_CLASS_NAME4', 'DEFAULT', NULL, 1623085200000, 1622998800000, 5, 'WAITING', 'CRON', 1622799561000, 0, NULL, -1, '');
INSERT INTO qrtz_triggers(sched_name, trigger_name, trigger_group, job_name, job_group, description, next_fire_time, prev_fire_time, priority, trigger_state, trigger_type, start_time, end_time, calendar_name, misfire_instr, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME5', 'DEFAULT', 'TASK_CLASS_NAME5', 'DEFAULT', NULL, 1623085200000, 1622998800000, 5, 'WAITING', 'CRON', 1622799561000, 0, NULL, -1, '');
INSERT INTO qrtz_triggers(sched_name, trigger_name, trigger_group, job_name, job_group, description, next_fire_time, prev_fire_time, priority, trigger_state, trigger_type, start_time, end_time, calendar_name, misfire_instr, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME6', 'DEFAULT', 'TASK_CLASS_NAME6', 'DEFAULT', NULL, 1623085200000, 1622998800000, 5, 'WAITING', 'CRON', 1622799561000, 0, NULL, -1, '');
INSERT INTO qrtz_triggers(sched_name, trigger_name, trigger_group, job_name, job_group, description, next_fire_time, prev_fire_time, priority, trigger_state, trigger_type, start_time, end_time, calendar_name, misfire_instr, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME7', 'DEFAULT', 'TASK_CLASS_NAME7', 'DEFAULT', NULL, 1623085200000, 1622998800000, 5, 'WAITING', 'CRON', 1622799562000, 0, NULL, -1, '');
INSERT INTO qrtz_triggers(sched_name, trigger_name, trigger_group, job_name, job_group, description, next_fire_time, prev_fire_time, priority, trigger_state, trigger_type, start_time, end_time, calendar_name, misfire_instr, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME8', 'DEFAULT', 'TASK_CLASS_NAME8', 'DEFAULT', NULL, 1623085200000, 1622998800000, 5, 'WAITING', 'CRON', 1622799562000, 0, NULL, -1, '');
INSERT INTO qrtz_triggers(sched_name, trigger_name, trigger_group, job_name, job_group, description, next_fire_time, prev_fire_time, priority, trigger_state, trigger_type, start_time, end_time, calendar_name, misfire_instr, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME9', 'DEFAULT', 'TASK_CLASS_NAME9', 'DEFAULT', NULL, 1623085200000, 1622998800000, 5, 'WAITING', 'CRON', 1622799562000, 0, NULL, -1, '');
INSERT INTO qrtz_triggers(sched_name, trigger_name, trigger_group, job_name, job_group, description, next_fire_time, prev_fire_time, priority, trigger_state, trigger_type, start_time, end_time, calendar_name, misfire_instr, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME12', 'DEFAULT', 'TASK_CLASS_NAME12', 'DEFAULT', NULL, 1623171600000, -1, 5, 'WAITING', 'CRON', 1623128991000, 0, NULL, -1, '');
INSERT INTO qrtz_triggers(sched_name, trigger_name, trigger_group, job_name, job_group, description, next_fire_time, prev_fire_time, priority, trigger_state, trigger_type, start_time, end_time, calendar_name, misfire_instr, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME13', 'DEFAULT', 'TASK_CLASS_NAME13', 'DEFAULT', NULL, 1625650645000, 1625650640000, 5, 'ACQUIRED', 'CRON', 1625650572000, 0, NULL, -1, '');


INSERT INTO qrtz_cron_triggers(sched_name, trigger_name, trigger_group, cron_expression, time_zone_id) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME1', 'DEFAULT', '0 0 3 /1 * ? *', 'Asia/Shanghai');
INSERT INTO qrtz_cron_triggers(sched_name, trigger_name, trigger_group, cron_expression, time_zone_id) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME10', 'DEFAULT', '0 0 1 * * ? *', 'Asia/Shanghai');
INSERT INTO qrtz_cron_triggers(sched_name, trigger_name, trigger_group, cron_expression, time_zone_id) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME11', 'DEFAULT', '0 0 1 * * ? *', 'Asia/Shanghai');
INSERT INTO qrtz_cron_triggers(sched_name, trigger_name, trigger_group, cron_expression, time_zone_id) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME2', 'DEFAULT', '0 0/2 * * * ? *', 'Asia/Shanghai');
INSERT INTO qrtz_cron_triggers(sched_name, trigger_name, trigger_group, cron_expression, time_zone_id) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME3', 'DEFAULT', '0 0 0 * * ? *', 'Asia/Shanghai');
INSERT INTO qrtz_cron_triggers(sched_name, trigger_name, trigger_group, cron_expression, time_zone_id) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME4', 'DEFAULT', '0 0 1 * * ? *', 'Asia/Shanghai');
INSERT INTO qrtz_cron_triggers(sched_name, trigger_name, trigger_group, cron_expression, time_zone_id) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME5', 'DEFAULT', '0 0 1 * * ? *', 'Asia/Shanghai');
INSERT INTO qrtz_cron_triggers(sched_name, trigger_name, trigger_group, cron_expression, time_zone_id) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME6', 'DEFAULT', '0 0 1 * * ? *', 'Asia/Shanghai');
INSERT INTO qrtz_cron_triggers(sched_name, trigger_name, trigger_group, cron_expression, time_zone_id) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME7', 'DEFAULT', '0 0 1 * * ? *', 'Asia/Shanghai');
INSERT INTO qrtz_cron_triggers(sched_name, trigger_name, trigger_group, cron_expression, time_zone_id) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME8', 'DEFAULT', '0 0 1 * * ? *', 'Asia/Shanghai');
INSERT INTO qrtz_cron_triggers(sched_name, trigger_name, trigger_group, cron_expression, time_zone_id) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME9', 'DEFAULT', '0 0 1 * * ? *', 'Asia/Shanghai');
INSERT INTO qrtz_cron_triggers(sched_name, trigger_name, trigger_group, cron_expression, time_zone_id) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME12', 'DEFAULT', '0 0 1 * * ? *', 'Asia/Shanghai');
INSERT INTO qrtz_cron_triggers(sched_name, trigger_name, trigger_group, cron_expression, time_zone_id) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME13', 'DEFAULT', '*/5 * * * * ?', 'Asia/Shanghai');


INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2012, '基础数据', 0, 4, 'basedata', NULL, 1, 0, 'M', '0', '0', NULL, 'base', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2013, '人员管理', 2012, 1, 'person', 'basedata/person/index', 1, 0, 'C', '0', '0', 'basedata:person:list', '#', 'admin', sysdate(), 'admin', sysdate(), '人员基础信息菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2014, '人员基础信息查询', 2013, 1, '#', '', 1, 0, 'F', '0', '0', 'basedata:person:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2015, '人员基础信息新增', 2013, 2, '#', '', 1, 0, 'F', '0', '0', 'basedata:person:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2016, '人员基础信息修改', 2013, 3, '#', '', 1, 0, 'F', '0', '0', 'basedata:person:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2017, '人员基础信息删除', 2013, 4, '#', '', 1, 0, 'F', '0', '0', 'basedata:person:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2018, '人员基础信息导出', 2013, 5, '#', '', 1, 0, 'F', '0', '0', 'basedata:person:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2019, '人脸管理', 2012, 2, 'face', 'basedata/face/index', 1, 0, 'C', '0', '0', 'basedata:face:list', '#', 'admin', sysdate(), 'admin', sysdate(), '人脸图像信息菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2020, '人脸图像信息查询', 2019, 1, '#', '', 1, 0, 'F', '0', '0', 'basedata:face:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2021, '人脸图像信息新增', 2019, 2, '#', '', 1, 0, 'F', '0', '0', 'basedata:face:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2022, '人脸图像信息修改', 2019, 3, '#', '', 1, 0, 'F', '0', '0', 'basedata:face:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2023, '人脸图像信息删除', 2019, 4, '#', '', 1, 0, 'F', '0', '0', 'basedata:face:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2024, '人脸图像信息导出', 2019, 5, '#', '', 1, 0, 'F', '0', '0', 'basedata:face:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2025, '指纹管理', 2012, 3, 'finger', 'basedata/finger/index', 1, 0, 'C', '0', '0', 'basedata:finger:list', '#', 'admin', sysdate(), 'admin', sysdate(), '指纹图像信息菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2026, '指纹图像信息查询', 2025, 1, '#', '', 1, 0, 'F', '0', '0', 'basedata:finger:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2027, '指纹图像信息新增', 2025, 2, '#', '', 1, 0, 'F', '0', '0', 'basedata:finger:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2028, '指纹图像信息修改', 2025, 3, '#', '', 1, 0, 'F', '0', '0', 'basedata:finger:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2029, '指纹图像信息删除', 2025, 4, '#', '', 1, 0, 'F', '0', '0', 'basedata:finger:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2030, '指纹图像信息导出', 2025, 5, '#', '', 1, 0, 'F', '0', '0', 'basedata:finger:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2031, '虹膜管理', 2012, 4, 'iris', 'basedata/iris/index', 1, 0, 'C', '0', '0', 'basedata:iris:list', '#', 'admin', sysdate(), 'admin', sysdate(), '虹膜图像信息菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2032, '虹膜图像信息查询', 2031, 1, '#', '', 1, 0, 'F', '0', '0', 'basedata:iris:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2033, '虹膜图像信息新增', 2031, 2, '#', '', 1, 0, 'F', '0', '0', 'basedata:iris:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2034, '虹膜图像信息修改', 2031, 3, '#', '', 1, 0, 'F', '0', '0', 'basedata:iris:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2035, '虹膜图像信息删除', 2031, 4, '#', '', 1, 0, 'F', '0', '0', 'basedata:iris:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2036, '虹膜图像信息导出', 2031, 5, '#', '', 1, 0, 'F', '0', '0', 'basedata:iris:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2037, '人脸虹膜', 2012, 5, 'faceIris', 'basedata/faceIris/index', 1, 0, 'C', '0', '0', 'basedata:faceIris:list', '#', 'admin', sysdate(), 'admin', sysdate(), '虹膜人脸多模态菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2038, '虹膜人脸多模态查询', 2037, 1, '#', '', 1, 0, 'F', '0', '0', 'basedata:faceIris:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2039, '虹膜人脸多模态新增', 2037, 2, '#', '', 1, 0, 'F', '0', '0', 'basedata:faceIris:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2040, '虹膜人脸多模态修改', 2037, 3, '#', '', 1, 0, 'F', '0', '0', 'basedata:faceIris:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2041, '虹膜人脸多模态删除', 2037, 4, '#', '', 1, 0, 'F', '0', '0', 'basedata:faceIris:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2042, '虹膜人脸多模态导出', 2037, 5, '#', '', 1, 0, 'F', '0', '0', 'basedata:faceIris:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2043, '证件管理', 2012, 6, 'cert', 'basedata/cert/index', 1, 0, 'C', '0', '0', 'basedata:cert:list', '#', 'admin', sysdate(), 'admin', sysdate(), '人员证件信息菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2044, '人员证件信息查询', 2043, 1, '#', '', 1, 0, 'F', '0', '0', 'basedata:cert:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2045, '人员证件信息新增', 2043, 2, '#', '', 1, 0, 'F', '0', '0', 'basedata:cert:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2046, '人员证件信息修改', 2043, 3, '#', '', 1, 0, 'F', '0', '0', 'basedata:cert:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2047, '人员证件信息删除', 2043, 4, '#', '', 1, 0, 'F', '0', '0', 'basedata:cert:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2048, '人员证件信息导出', 2043, 5, '#', '', 1, 0, 'F', '0', '0', 'basedata:cert:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2055, '一键更新人脸特征', 2019, 6, '', NULL, 1, 0, 'F', '0', '0', 'basedata:face:updatefeature', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2056, '一键更新指纹特征', 2025, 6, '', NULL, 1, 0, 'F', '0', '0', 'basedata:finger:updatefeature', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2057, '一键更新虹膜特征', 2031, 6, '', NULL, 1, 0, 'F', '0', '0', 'basedata:iris:updatefeature', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2058, '人员证件信息导入', 2043, 6, '', NULL, 1, 0, 'F', '0', '0', 'basedata:cert:import', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2059, '人员证件照片下载', 2043, 7, '', NULL, 1, 0, 'F', '0', '0', 'basedata:cert:download', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2060, '人脸图像下载', 2019, 7, '', NULL, 1, 0, 'F', '0', '0', 'basedata:face:download', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2061, '指纹图像下载', 2025, 7, '', NULL, 1, 0, 'F', '0', '0', 'basedata:finger:download', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2062, '虹膜图像下载', 2031, 7, '', NULL, 1, 0, 'F', '0', '0', 'basedata:iris:download', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2063, '人员信息一键同步', 2013, 7, '', NULL, 1, 0, 'F', '0', '0', 'basedata:person:syncdata', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2064, '人员基础信息导入', 2013, 8, '', NULL, 1, 0, 'F', '0', '0', 'basedata:person:import', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2065, '人脸图片导入', 2019, 8, '', NULL, 1, 0, 'F', '0', '0', 'basedata:face:import', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2066, '指纹图片导入', 2025, 8, '', NULL, 1, 0, 'F', '0', '0', 'basedata:finger:import', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2067, '虹膜图片导入', 2031, 8, '', NULL, 1, 0, 'F', '0', '0', 'basedata:iris:import', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2068, '场景接入', 0, 5, 'scene', NULL, 1, 0, 'M', '0', '0', NULL, 'scene', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2069, '渠道信息', 2068, 1, 'channel', 'scene/channel/index', 1, 0, 'C', '0', '0', 'scene:channel:list', '#', 'admin', sysdate(), 'admin', sysdate(), '渠道信息菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2070, '渠道信息查询', 2069, 1, '#', '', 1, 0, 'F', '0', '0', 'scene:channel:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2071, '渠道信息新增', 2069, 2, '#', '', 1, 0, 'F', '0', '0', 'scene:channel:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2072, '渠道信息修改', 2069, 3, '#', '', 1, 0, 'F', '0', '0', 'scene:channel:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2073, '渠道信息删除', 2069, 4, '#', '', 1, 0, 'F', '0', '0', 'scene:channel:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2074, '渠道信息导出', 2069, 5, '#', '', 1, 0, 'F', '0', '0', 'scene:channel:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2075, '分库信息', 2068, 1, 'subtreasury', 'scene/subtreasury/index', 1, 0, 'C', '0', '0', 'scene:subtreasury:list', '#', 'admin', sysdate(), 'admin', sysdate(), '分库信息菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2076, '分库信息查询', 2075, 1, '#', '', 1, 0, 'F', '0', '0', 'scene:subtreasury:query', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2077, '分库信息新增', 2075, 2, '#', '', 1, 0, 'F', '0', '0', 'scene:subtreasury:add', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2078, '分库信息修改', 2075, 3, '#', '', 1, 0, 'F', '0', '0', 'scene:subtreasury:edit', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2079, '分库信息删除', 2075, 4, '#', '', 1, 0, 'F', '0', '0', 'scene:subtreasury:remove', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2080, '分库信息导出', 2075, 5, '#', '', 1, 0, 'F', '0', '0', 'scene:subtreasury:export', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2081, '渠道参数', 2068, 1, 'channelParam', 'scene/channelParam/index', 1, 0, 'C', '0', '0', 'scene:channelParam:list', '#', 'admin', sysdate(), 'admin', sysdate(), '渠道参数菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2082, '渠道参数查询', 2081, 1, '#', '', 1, 0, 'F', '0', '0', 'scene:channelParam:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2083, '渠道参数新增', 2081, 2, '#', '', 1, 0, 'F', '0', '0', 'scene:channelParam:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2084, '渠道参数修改', 2081, 3, '#', '', 1, 0, 'F', '0', '0', 'scene:channelParam:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2085, '渠道参数删除', 2081, 4, '#', '', 1, 0, 'F', '0', '0', 'scene:channelParam:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2086, '渠道参数导出', 2081, 5, '#', '', 1, 0, 'F', '0', '0', 'scene:channelParam:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2087, '渠道业务', 2068, 1, 'channelBusi', 'scene/channelBusi/index', 1, 0, 'C', '0', '0', 'scene:channelBusi:list', '#', 'admin', sysdate(), 'admin', sysdate(), '渠道业务菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2088, '渠道业务查询', 2087, 1, '#', '', 1, 0, 'F', '0', '0', 'scene:channelBusi:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2089, '渠道业务新增', 2087, 2, '#', '', 1, 0, 'F', '0', '0', 'scene:channelBusi:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2090, '渠道业务修改', 2087, 3, '#', '', 1, 0, 'F', '0', '0', 'scene:channelBusi:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2091, '渠道业务删除', 2087, 4, '#', '', 1, 0, 'F', '0', '0', 'scene:channelBusi:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2092, '渠道业务导出', 2087, 5, '#', '', 1, 0, 'F', '0', '0', 'scene:channelBusi:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2093, '分库业务', 2068, 1, 'subtreasuryBusi', 'scene/subtreasuryBusi/index', 1, 0, 'C', '0', '0', 'scene:subtreasuryBusi:list', '#', 'admin', sysdate(), 'admin', sysdate(), '分库业务菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2094, '分库业务查询', 2093, 1, '#', '', 1, 0, 'F', '0', '0', 'scene:subtreasuryBusi:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2095, '分库业务新增', 2093, 2, '#', '', 1, 0, 'F', '0', '0', 'scene:subtreasuryBusi:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2096, '分库业务修改', 2093, 3, '#', '', 1, 0, 'F', '0', '0', 'scene:subtreasuryBusi:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2097, '分库业务删除', 2093, 4, '#', '', 1, 0, 'F', '0', '0', 'scene:subtreasuryBusi:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2098, '分库业务导出', 2093, 5, '#', '', 1, 0, 'F', '0', '0', 'scene:subtreasuryBusi:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2099, '渠道业务一键同步', 2087, 6, '', NULL, 1, 0, 'F', '0', '0', 'scene:channelBusi:syncdata', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2100, '渠道业务导入', 2087, 7, '', NULL, 1, 0, 'F', '0', '0', 'scene:channelBusi:import', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2101, '分库业务一键同步', 2093, 6, '', NULL, 1, 0, 'F', '0', '0', 'scene:subtreasuryBusi:syncdata', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2102, '区域管理', 0, 6, 'area', NULL, 1, 0, 'M', '0', '0', '', 'area', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2103, '区域模型', 2102, 1, 'model', 'area/model/index', 1, 0, 'C', '0', '0', 'area:model:list', '#', 'admin', sysdate(), 'admin', sysdate(), '区域菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2104, '区域查询', 2103, 1, '#', '', 1, 0, 'F', '0', '0', 'area:model:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2105, '区域新增', 2103, 2, '#', '', 1, 0, 'F', '0', '0', 'area:model:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2106, '区域修改', 2103, 3, '#', '', 1, 0, 'F', '0', '0', 'area:model:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2107, '区域删除', 2103, 4, '#', '', 1, 0, 'F', '0', '0', 'area:model:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2108, '区域导出', 2103, 5, '#', '', 1, 0, 'F', '0', '0', 'area:model:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2109, '设备管理', 0, 7, 'device', NULL, 1, 0, 'M', '0', '0', NULL, 'device', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2110, '设备型号', 2109, 1, 'model', 'device/model/index', 1, 0, 'C', '0', '0', 'device:model:list', '#', 'admin', sysdate(), 'admin', sysdate(), '设备型号信息菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2111, '设备型号信息查询', 2110, 1, '#', '', 1, 0, 'F', '0', '0', 'device:model:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2112, '设备型号信息新增', 2110, 2, '#', '', 1, 0, 'F', '0', '0', 'device:model:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2113, '设备型号信息修改', 2110, 3, '#', '', 1, 0, 'F', '0', '0', 'device:model:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2114, '设备型号信息删除', 2110, 4, '#', '', 1, 0, 'F', '0', '0', 'device:model:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2115, '设备型号信息导出', 2110, 5, '#', '', 1, 0, 'F', '0', '0', 'device:model:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2116, '设备参数', 2109, 4, 'param', 'device/param/index', 1, 0, 'M', '0', '0', '', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2118, '参数信息', 2116, 1, 'info', 'device/param/info/index', 1, 0, 'C', '0', '0', 'device:paraminfo:list', '#', 'admin', sysdate(), 'admin', sysdate(), '设备参数信息菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2119, '设备参数信息查询', 2118, 1, '#', '', 1, 0, 'F', '0', '0', 'device:paraminfo:query', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2120, '设备参数信息新增', 2118, 2, '#', '', 1, 0, 'F', '0', '0', 'device:paraminfo:add', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2121, '设备参数信息修改', 2118, 3, '#', '', 1, 0, 'F', '0', '0', 'device:paraminfo:edit', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2122, '设备参数信息删除', 2118, 4, '#', '', 1, 0, 'F', '0', '0', 'device:paraminfo:remove', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2123, '设备参数信息导出', 2118, 5, '#', '', 1, 0, 'F', '0', '0', 'device:paraminfo:export', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2124, 'SDK文件', 3, 1, 'file', 'tool/sdkFile/index', 1, 0, 'C', '0', '0', 'tool:sdkFile:list', '#', 'admin', sysdate(), 'admin', sysdate(), 'SDK文件上传菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2125, 'SDK文件上传查询', 2124, 1, '#', '', 1, 0, 'F', '0', '0', 'tool:sdkFile:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2126, 'SDK文件上传新增', 2124, 2, '#', '', 1, 0, 'F', '0', '0', 'tool:sdkFile:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2127, 'SDK文件上传修改', 2124, 3, '#', '', 1, 0, 'F', '0', '0', 'tool:sdkFile:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2128, 'SDK文件上传删除', 2124, 4, '#', '', 1, 0, 'F', '0', '0', 'tool:sdkFile:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2129, 'SDK文件上传导出', 2124, 5, '#', '', 1, 0, 'F', '0', '0', 'tool:sdkFile:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2136, '型号参数', 2116, 1, 'relation', 'device/param/relation/index', 1, 0, 'C', '0', '0', 'device:paramModelRel:list', '#', 'admin', sysdate(), 'admin', sysdate(), '参数型号关系菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2137, '参数型号关系查询', 2136, 1, '#', '', 1, 0, 'F', '0', '0', 'device:paramModelRel:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2138, '参数型号关系新增', 2136, 2, '#', '', 1, 0, 'F', '0', '0', 'device:paramModelRel:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2139, '参数型号关系修改', 2136, 3, '#', '', 1, 0, 'F', '0', '0', 'device:paramModelRel:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2140, '参数型号关系删除', 2136, 4, '#', '', 1, 0, 'F', '0', '0', 'device:paramModelRel:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2141, '参数型号关系导出', 2136, 5, '#', '', 1, 0, 'F', '0', '0', 'device:paramModelRel:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2142, '设备信息', 2109, 2, 'info', 'device/info/index', 1, 0, 'C', '0', '0', 'device:info:list', '#', 'admin', sysdate(), 'admin', sysdate(), '设备信息菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2143, '设备信息查询', 2142, 1, '#', '', 1, 0, 'F', '0', '0', 'device:info:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2144, '设备信息新增', 2142, 2, '#', '', 1, 0, 'F', '0', '0', 'device:info:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2145, '设备信息修改', 2142, 3, '#', '', 1, 0, 'F', '0', '0', 'device:info:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2146, '设备信息删除', 2142, 4, '#', '', 1, 0, 'F', '0', '0', 'device:info:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2147, '设备信息导出', 2142, 5, '#', '', 1, 0, 'F', '0', '0', 'device:info:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2148, '设备信息导入', 2142, 6, '', NULL, 1, 0, 'F', '0', '0', 'device:info:import', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2149, '设备批次', 2109, 3, 'batchlog', 'device/batchlog/index', 1, 0, 'C', '0', '0', 'device:batchlog:list', '#', 'admin', sysdate(), 'admin', sysdate(), '设备批次菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2150, '设备批次查询', 2149, 1, '#', '', 1, 0, 'F', '0', '0', 'device:batchlog:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2153, '设备批次回滚', 2149, 2, '#', '', 1, 0, 'F', '0', '0', 'device:batchlog:rollback', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2154, '设备批次导出', 2149, 3, '#', '', 1, 0, 'F', '0', '0', 'device:batchlog:export', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2155, '设备升级', 2109, 5, 'upgrade', 'device/upgrade/index', 1, 0, 'M', '0', '0', '', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2156, '版本信息', 2155, 1, 'version', 'device/upgrade/version/index', 1, 0, 'C', '0', '0', 'device:upgradeVersion:list', '#', 'admin', sysdate(), 'admin', sysdate(), '版本信息菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2157, '版本信息查询', 2156, 1, '#', '', 1, 0, 'F', '0', '0', 'device:upgradeVersion:query', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2158, '版本信息新增', 2156, 2, '#', '', 1, 0, 'F', '0', '0', 'device:upgradeVersion:add', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2159, '版本信息修改', 2156, 3, '#', '', 1, 0, 'F', '0', '0', 'device:upgradeVersion:edit', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2160, '版本信息删除', 2156, 4, '#', '', 1, 0, 'F', '0', '0', 'device:upgradeVersion:remove', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2161, '版本信息导出', 2156, 5, '#', '', 1, 0, 'F', '0', '0', 'device:upgradeVersion:export', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2162, '升级任务', 2155, 1, 'task', 'device/upgrade/task/index', 1, 0, 'C', '0', '0', 'device:upgradeTask:list', '#', 'admin', sysdate(), 'admin', sysdate(), '升级任务菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2163, '升级任务查询', 2162, 1, '#', '', 1, 0, 'F', '0', '0', 'device:upgradeTask:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2164, '升级任务新增', 2162, 2, '#', '', 1, 0, 'F', '0', '0', 'device:upgradeTask:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2165, '升级任务修改', 2162, 3, '#', '', 1, 0, 'F', '0', '0', 'device:upgradeTask:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2166, '升级任务删除', 2162, 4, '#', '', 1, 0, 'F', '0', '0', 'device:upgradeTask:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2167, '升级任务导出', 2162, 5, '#', '', 1, 0, 'F', '0', '0', 'device:upgradeTask:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2168, '升级日志', 2155, 1, 'upgradelog', 'device/upgrade/log/index', 1, 0, 'C', '0', '0', 'device:upgradelog:list', '#', 'admin', sysdate(), 'admin', sysdate(), '升级日志菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2169, '升级日志查询', 2168, 1, '#', '', 1, 0, 'F', '0', '0', 'device:upgradelog:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2173, '升级日志导出', 2168, 5, '#', '', 1, 0, 'F', '0', '0', 'device:upgradelog:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2174, '参数下发', 2116, 1, 'paramlog', 'device/param/log/index', 1, 0, 'C', '0', '0', 'device:paramlog:list', '#', 'admin', sysdate(), 'admin', sysdate(), '参数下发日志菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2175, '参数下发日志查询', 2174, 1, '#', '', 1, 0, 'F', '0', '0', 'device:paramlog:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2176, '参数下发日志新增', 2174, 2, '#', '', 1, 0, 'F', '0', '0', 'device:paramlog:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2179, '参数下发日志导出', 2174, 5, '#', '', 1, 0, 'F', '0', '0', 'device:paramlog:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2180, '动作日志', 2109, 6, 'actionlog', 'device/actionlog/index', 1, 0, 'C', '0', '0', 'device:actionlog:list', '#', 'admin', sysdate(), 'admin', sysdate(), '设备动作日志菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2181, '设备动作日志查询', 2180, 1, '#', '', 1, 0, 'F', '0', '0', 'device:actionlog:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2182, '设备动作日志新增', 2180, 2, '#', '', 1, 0, 'F', '0', '0', 'device:actionlog:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2183, '设备动作日志修改', 2180, 3, '#', '', 1, 0, 'F', '0', '0', 'device:actionlog:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2184, '设备动作日志删除', 2180, 4, '#', '', 1, 0, 'F', '0', '0', 'device:actionlog:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2185, '设备动作日志导出', 2180, 5, '#', '', 1, 0, 'F', '0', '0', 'device:actionlog:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2186, '应用系统', 0, 8, 'app', NULL, 1, 0, 'M', '0', '0', NULL, 'appmgr', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2187, '应用信息', 2186, 1, 'info', 'app/info/index', 1, 0, 'C', '0', '0', 'app:info:list', '#', 'admin', sysdate(), 'admin', sysdate(), '应用系统信息菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2188, '应用系统信息查询', 2187, 1, '#', '', 1, 0, 'F', '0', '0', 'app:info:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2189, '应用系统信息新增', 2187, 2, '#', '', 1, 0, 'F', '0', '0', 'app:info:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2191, '应用系统信息删除', 2187, 4, '#', '', 1, 0, 'F', '0', '0', 'app:info:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2192, '应用系统信息导出', 2187, 5, '#', '', 1, 0, 'F', '0', '0', 'app:info:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2193, '接口授权', 2186, 2, 'auth', 'app/auth/index', 1, 0, 'C', '0', '0', 'app:auth:list', '#', 'admin', sysdate(), 'admin', sysdate(), '应用接口授权菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2194, '应用接口授权查询', 2193, 1, '#', '', 1, 0, 'F', '0', '0', 'app:auth:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2195, '应用接口授权新增', 2193, 2, '#', '', 1, 0, 'F', '0', '0', 'app:auth:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2196, '应用接口授权修改', 2193, 3, '#', '', 1, 0, 'F', '0', '0', 'app:auth:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2197, '应用接口授权删除', 2193, 4, '#', '', 1, 0, 'F', '0', '0', 'app:auth:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2198, '应用接口授权导出', 2193, 5, '#', '', 1, 0, 'F', '0', '0', 'app:auth:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2199, '消息通知', 0, 9, 'msg', NULL, 1, 0, 'M', '0', '0', NULL, 'msgmgr', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2200, '邮箱配置', 2261, 1, 'mailProperty', 'msg/mail/mailProperty/index', 1, 0, 'C', '0', '0', 'msg:mailProperty:list', '#', 'admin', sysdate(), 'admin', sysdate(), '邮箱配置菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2201, '邮箱配置查询', 2200, 1, '#', '', 1, 0, 'F', '0', '0', 'msg:mailProperty:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2202, '邮箱配置新增', 2200, 2, '#', '', 1, 0, 'F', '0', '0', 'msg:mailProperty:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2203, '邮箱配置修改', 2200, 3, '#', '', 1, 0, 'F', '0', '0', 'msg:mailProperty:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2204, '邮箱配置删除', 2200, 4, '#', '', 1, 0, 'F', '0', '0', 'msg:mailProperty:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2205, '邮箱配置导出', 2200, 5, '#', '', 1, 0, 'F', '0', '0', 'msg:mailProperty:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2206, '钉钉微应用', 2263, 2, 'dingApplication', 'msg/dingtalk/application/index', 1, 0, 'C', '0', '0', 'msg:dingApplication:list', '#', 'admin', sysdate(), 'admin', sysdate(), '钉钉微应用菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2207, '钉钉微应用查询', 2206, 1, '#', '', 1, 0, 'F', '0', '0', 'msg:dingApplication:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2208, '钉钉微应用新增', 2206, 2, '#', '', 1, 0, 'F', '0', '0', 'msg:dingApplication:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2209, '钉钉微应用修改', 2206, 3, '#', '', 1, 0, 'F', '0', '0', 'msg:dingApplication:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2210, '钉钉微应用删除', 2206, 4, '#', '', 1, 0, 'F', '0', '0', 'msg:dingApplication:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2211, '钉钉微应用导出', 2206, 5, '#', '', 1, 0, 'F', '0', '0', 'msg:dingApplication:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2212, '钉钉团队', 2263, 1, 'dingTeam', 'msg/dingtalk/team/index', 1, 0, 'C', '0', '0', 'msg:dingTeam:list', '#', 'admin', sysdate(), 'admin', sysdate(), '钉钉团队(企业)菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2213, '钉钉团队(企业)查询', 2212, 1, '#', '', 1, 0, 'F', '0', '0', 'msg:dingTeam:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2214, '钉钉团队(企业)新增', 2212, 2, '#', '', 1, 0, 'F', '0', '0', 'msg:dingTeam:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2215, '钉钉团队(企业)修改', 2212, 3, '#', '', 1, 0, 'F', '0', '0', 'msg:dingTeam:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2216, '钉钉团队(企业)删除', 2212, 4, '#', '', 1, 0, 'F', '0', '0', 'msg:dingTeam:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2217, '钉钉团队(企业)导出', 2212, 5, '#', '', 1, 0, 'F', '0', '0', 'msg:dingTeam:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2224, '消息附件', 2264, 3, 'logannex', 'msg/log/logannex/index', 1, 0, 'C', '0', '0', 'msg:logannex:list', '#', 'admin', sysdate(), 'admin', sysdate(), '消息日志附件菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2225, '消息日志附件查询', 2224, 1, '#', '', 1, 0, 'F', '0', '0', 'msg:logannex:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2229, '消息日志附件导出', 2224, 5, '#', '', 1, 0, 'F', '0', '0', 'msg:logannex:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2230, '消息日志', 2264, 1, 'msglog', 'msg/log/msglog/index', 1, 0, 'C', '0', '0', 'msg:log:list', '#', 'admin', sysdate(), 'admin', sysdate(), '消息日志菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2231, '消息日志查询', 2230, 1, '#', '', 1, 0, 'F', '0', '0', 'msg:log:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2235, '消息日志导出', 2230, 5, '#', '', 1, 0, 'F', '0', '0', 'msg:log:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2236, '微信公众号', 2262, 1, 'officalAccount', 'msg/wechat/officalAccount/index', 1, 0, 'C', '0', '0', 'msg:officalAccount:list', '#', 'admin', sysdate(), 'admin', sysdate(), '微信公众号菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2237, '微信公众号查询', 2236, 1, '#', '', 1, 0, 'F', '0', '0', 'msg:officalAccount:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2238, '微信公众号新增', 2236, 2, '#', '', 1, 0, 'F', '0', '0', 'msg:officalAccount:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2239, '微信公众号修改', 2236, 3, '#', '', 1, 0, 'F', '0', '0', 'msg:officalAccount:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2240, '微信公众号删除', 2236, 4, '#', '', 1, 0, 'F', '0', '0', 'msg:officalAccount:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2241, '微信公众号导出', 2236, 5, '#', '', 1, 0, 'F', '0', '0', 'msg:officalAccount:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2242, '公众号用户', 2262, 2, 'officalAccountUser', 'msg/wechat/officalAccountUser/index', 1, 0, 'C', '0', '0', 'msg:officalAccountUser:list', '#', 'admin', sysdate(), 'admin', sysdate(), '微信用户菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2243, '微信用户查询', 2242, 1, '#', '', 1, 0, 'F', '0', '0', 'msg:officalAccountUser:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2244, '微信用户拉取', 2242, 2, '#', '', 1, 0, 'F', '0', '0', 'msg:officalAccountUser:pull', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2245, '微信用户修改', 2242, 3, '#', '', 1, 0, 'F', '0', '0', 'msg:officalAccountUser:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2247, '微信用户导出', 2242, 5, '#', '', 1, 0, 'F', '0', '0', 'msg:officalAccountUser:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2248, '短信云账户', 2260, 1, 'smsCloudAccount', 'msg/sms/cloudAccount/index', 1, 0, 'C', '0', '0', 'msg:smsCloudAccount:list', '#', 'admin', sysdate(), 'admin', sysdate(), '短信云账户菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2249, '短信云账户查询', 2248, 1, '#', '', 1, 0, 'F', '0', '0', 'msg:smsCloudAccount:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2250, '短信云账户新增', 2248, 2, '#', '', 1, 0, 'F', '0', '0', 'msg:smsCloudAccount:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2251, '短信云账户修改', 2248, 3, '#', '', 1, 0, 'F', '0', '0', 'msg:smsCloudAccount:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2252, '短信云账户删除', 2248, 4, '#', '', 1, 0, 'F', '0', '0', 'msg:smsCloudAccount:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2253, '短信云账户导出', 2248, 5, '#', '', 1, 0, 'F', '0', '0', 'msg:smsCloudAccount:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2254, '消息模板', 2199, 5, 'template', 'msg/template/index', 1, 0, 'C', '0', '0', 'msg:template:list', '#', 'admin', sysdate(), 'admin', sysdate(), '消息模板菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2255, '消息模板查询', 2254, 1, '#', '', 1, 0, 'F', '0', '0', 'msg:template:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2256, '消息模板新增', 2254, 2, '#', '', 1, 0, 'F', '0', '0', 'msg:template:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2257, '消息模板修改', 2254, 3, '#', '', 1, 0, 'F', '0', '0', 'msg:template:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2258, '消息模板删除', 2254, 4, '#', '', 1, 0, 'F', '0', '0', 'msg:template:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2259, '消息模板导出', 2254, 5, '#', '', 1, 0, 'F', '0', '0', 'msg:template:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2260, '短信通知', 2199, 1, 'sms', 'msg/sms/index', 1, 0, 'M', '0', '0', '', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2261, '邮件通知', 2199, 2, 'mail', 'msg/mail/index', 1, 0, 'M', '0', '0', '', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2262, '微信通知', 2199, 3, 'wechat', 'msg/wechat/index', 1, 0, 'M', '0', '0', '', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2263, '钉钉通知', 2199, 4, 'dingtalk', 'msg/dingtalk/index', 1, 0, 'M', '0', '0', '', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2264, '通知日志', 2199, 6, 'log', 'msg/log/index', 1, 0, 'M', '0', '0', '', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2265, '公众号菜单', 2262, 3, 'weixinMenu', 'msg/wechat/menu/index', 1, 0, 'C', '0', '0', 'msg:weixinMenu:list', '#', 'admin', sysdate(), 'admin', sysdate(), '微信公众号菜单菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2266, '微信公众号菜单查询', 2265, 1, '#', '', 1, 0, 'F', '0', '0', 'msg:weixinMenu:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2267, '微信公众号菜单新增', 2265, 2, '#', '', 1, 0, 'F', '0', '0', 'msg:weixinMenu:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2268, '微信公众号菜单修改', 2265, 3, '#', '', 1, 0, 'F', '0', '0', 'msg:weixinMenu:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2269, '微信公众号菜单删除', 2265, 4, '#', '', 1, 0, 'F', '0', '0', 'msg:weixinMenu:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2270, '微信公众号菜单导出', 2265, 5, '#', '', 1, 0, 'F', '0', '0', 'msg:weixinMenu:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2271, '菜单响应', 2262, 4, 'weixinMenuReply', 'msg/wechat/menuReply/index', 1, 0, 'C', '0', '0', 'msg:weixinMenuReply:list', '#', 'admin', sysdate(), 'admin', sysdate(), '微信公众号菜单回复菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2272, '微信公众号菜单回复查询', 2271, 1, '#', '', 1, 0, 'F', '0', '0', 'msg:weixinMenuReply:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2273, '微信公众号菜单回复新增', 2271, 2, '#', '', 1, 0, 'F', '0', '0', 'msg:weixinMenuReply:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2274, '微信公众号菜单回复修改', 2271, 3, '#', '', 1, 0, 'F', '0', '0', 'msg:weixinMenuReply:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2275, '微信公众号菜单回复删除', 2271, 4, '#', '', 1, 0, 'F', '0', '0', 'msg:weixinMenuReply:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2276, '微信公众号菜单回复导出', 2271, 5, '#', '', 1, 0, 'F', '0', '0', 'msg:weixinMenuReply:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2277, '发送消息', 2199, 7, 'send', 'msg/send/index', 1, 0, 'M', '0', '0', '', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2278, '发送短信', 2277, 1, 'sms', 'msg/send/sms/index', 1, 0, 'C', '0', '0', 'msg:send:sms', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2279, '发送邮件', 2277, 2, 'mail', 'msg/send/mail/index', 1, 0, 'C', '0', '0', 'msg:send:mail', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2280, '发送微信', 2277, 3, 'wechat', 'msg/send/wechat/index', 1, 0, 'C', '0', '0', 'msg:send:weixin', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2281, '发送钉钉', 2277, 4, 'dingtalk', 'msg/send/dingtalk/index', 1, 0, 'C', '0', '0', 'msg:send:dingtalk', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2282, '钉媒体上传', 2277, 5, 'uploadMedia', 'msg/send/dingtalk/upload-media', 1, 0, 'C', '0', '0', 'msg:send:dingMediaUpload', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2284, '交易日志', 0, 10, 'tradelog', NULL, 1, 0, 'M', '0', '0', NULL, 'logmgr', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2285, 'OCR日志', 2284, 2, 'ocr', 'ocr/index', 1, 0, 'M', '0', '0', '', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2297, '人脸比对', 2406, 1, 'facematch', 'tradelog/facematch/index', 1, 0, 'C', '0', '0', 'tradelog:facematch:list', '#', 'admin', sysdate(), 'admin', sysdate(), '人脸比对日志菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2298, '人脸比对日志查询', 2297, 1, '#', '', 1, 0, 'F', '0', '0', 'tradelog:facematch:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2302, '人脸比对日志导出', 2297, 5, '#', '', 1, 0, 'F', '0', '0', 'tradelog:facematch:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2303, '人脸搜索', 2406, 2, 'facesearch', 'tradelog/facesearch/index', 1, 0, 'C', '0', '0', 'tradelog:facesearch:list', '#', 'admin', sysdate(), 'admin', sysdate(), '人脸搜索日志菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2304, '人脸搜索日志查询', 2303, 1, '#', '', 1, 0, 'F', '0', '0', 'tradelog:facesearch:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2308, '人脸搜索日志导出', 2303, 5, '#', '', 1, 0, 'F', '0', '0', 'tradelog:facesearch:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2321, '指纹比对', 2406, 3, 'fingermatch', 'tradelog/fingermatch/index', 1, 0, 'C', '0', '0', 'tradelog:fingermatch:list', '#', 'admin', sysdate(), 'admin', sysdate(), '指纹比对日志菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2322, '指纹比对日志查询', 2321, 1, '#', '', 1, 0, 'F', '0', '0', 'tradelog:fingermatch:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2326, '指纹比对日志导出', 2321, 5, '#', '', 1, 0, 'F', '0', '0', 'tradelog:fingermatch:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2327, '指纹搜索', 2406, 4, 'fingersearch', 'tradelog/fingersearch/index', 1, 0, 'C', '0', '0', 'tradelog:fingersearch:list', '#', 'admin', sysdate(), 'admin', sysdate(), '指纹搜索日志菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2328, '指纹搜索日志查询', 2327, 1, '#', '', 1, 0, 'F', '0', '0', 'tradelog:fingersearch:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2332, '指纹搜索日志导出', 2327, 5, '#', '', 1, 0, 'F', '0', '0', 'tradelog:fingersearch:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2333, '虹膜比对', 2406, 5, 'irismatch', 'tradelog/irismatch/index', 1, 0, 'C', '0', '0', 'tradelog:irismatch:list', '#', 'admin', sysdate(), 'admin', sysdate(), '虹膜比对日志菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2334, '虹膜比对日志查询', 2333, 1, '#', '', 1, 0, 'F', '0', '0', 'tradelog:irismatch:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2338, '虹膜比对日志导出', 2333, 5, '#', '', 1, 0, 'F', '0', '0', 'tradelog:irismatch:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2339, '虹膜搜索', 2406, 6, 'irissearch', 'tradelog/irissearch/index', 1, 0, 'C', '0', '0', 'tradelog:irissearch:list', '#', 'admin', sysdate(), 'admin', sysdate(), '虹膜搜索日志菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2340, '虹膜搜索日志查询', 2339, 1, '#', '', 1, 0, 'F', '0', '0', 'tradelog:irissearch:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2344, '虹膜搜索日志导出', 2339, 5, '#', '', 1, 0, 'F', '0', '0', 'tradelog:irissearch:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2351, '银行卡', 2285, 1, 'bankcard', 'ocr/bankcard/index', 1, 0, 'C', '0', '0', 'ocr:bankcard:list', '#', 'admin', sysdate(), 'admin', sysdate(), '银行卡OCR识别记录菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2352, '银行卡OCR识别记录查询', 2351, 1, '#', '', 1, 0, 'F', '0', '0', 'ocr:bankcard:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2356, '银行卡OCR识别记录导出', 2351, 5, '#', '', 1, 0, 'F', '0', '0', 'ocr:bankcard:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2357, '营业执照', 2285, 2, 'busilic', 'ocr/busilic/index', 1, 0, 'C', '0', '0', 'ocr:busilic:list', '#', 'admin', sysdate(), '', NULL, '营业执照OCR识别记录菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2358, '营业执照OCR识别记录查询', 2357, 1, '#', '', 1, 0, 'F', '0', '0', 'ocr:busilic:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2362, '营业执照OCR识别记录导出', 2357, 5, '#', '', 1, 0, 'F', '0', '0', 'ocr:busilic:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2363, '驾驶证', 2285, 3, 'driverlic', 'ocr/driverlic/index', 1, 0, 'C', '0', '0', 'ocr:driverlic:list', '#', 'admin', sysdate(), '', NULL, '驾驶证OCR识别记录菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2364, '驾驶证OCR识别记录查询', 2363, 1, '#', '', 1, 0, 'F', '0', '0', 'ocr:driverlic:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2368, '驾驶证OCR识别记录导出', 2363, 5, '#', '', 1, 0, 'F', '0', '0', 'ocr:driverlic:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2369, '行驶证', 2285, 4, 'drivinglic', 'ocr/drivinglic/index', 1, 0, 'C', '0', '0', 'ocr:drivinglic:list', '#', 'admin', sysdate(), '', NULL, '行驶证OCR识别记录菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2370, '行驶证OCR识别记录查询', 2369, 1, '#', '', 1, 0, 'F', '0', '0', 'ocr:drivinglic:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2374, '行驶证OCR识别记录导出', 2369, 5, '#', '', 1, 0, 'F', '0', '0', 'ocr:drivinglic:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2375, '港澳通行证', 2285, 5, 'hkmac', 'ocr/hkmac/index', 1, 0, 'C', '0', '0', 'ocr:hkmac:list', '#', 'admin', sysdate(), '', NULL, '港澳通行证OCR识别记录菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2376, '港澳通行证OCR识别记录查询', 2375, 1, '#', '', 1, 0, 'F', '0', '0', 'ocr:hkmac:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2380, '港澳通行证OCR识别记录导出', 2375, 5, '#', '', 1, 0, 'F', '0', '0', 'ocr:hkmac:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2381, '身份证背面', 2285, 6, 'idcardback', 'ocr/idcardback/index', 1, 0, 'C', '0', '0', 'ocr:idcardback:list', '#', 'admin', sysdate(), '', NULL, '身份证背面OCR识别记录菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2382, '身份证背面OCR识别记录查询', 2381, 1, '#', '', 1, 0, 'F', '0', '0', 'ocr:idcardback:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2386, '身份证背面OCR识别记录导出', 2381, 5, '#', '', 1, 0, 'F', '0', '0', 'ocr:idcardback:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2387, '身份证正面', 2285, 7, 'idcardfront', 'ocr/idcardfront/index', 1, 0, 'C', '0', '0', 'ocr:idcardfront:list', '#', 'admin', sysdate(), '', NULL, '身份证正面OCR识别记录菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2388, '身份证正面OCR识别记录查询', 2387, 1, '#', '', 1, 0, 'F', '0', '0', 'ocr:idcardfront:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2392, '身份证正面OCR识别记录导出', 2387, 5, '#', '', 1, 0, 'F', '0', '0', 'ocr:idcardfront:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2393, '护照', 2285, 8, 'passport', 'ocr/passport/index', 1, 0, 'C', '0', '0', 'ocr:passport:list', '#', 'admin', sysdate(), '', NULL, '护照OCR识别记录菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2394, '护照OCR识别记录查询', 2393, 1, '#', '', 1, 0, 'F', '0', '0', 'ocr:passport:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2398, '护照OCR识别记录导出', 2393, 5, '#', '', 1, 0, 'F', '0', '0', 'ocr:passport:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2399, '人脸虹膜搜索', 2406, 7, 'faceirisSearch', 'tradelog/faceirisSearch/index', 1, 0, 'C', '0', '0', 'tradelog:faceirisSearch:list', '#', 'admin', sysdate(), 'admin', sysdate(), '人脸虹膜搜索日志菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2400, '人脸虹膜搜索日志查询', 2399, 1, '#', '', 1, 0, 'F', '0', '0', 'tradelog:faceirisSearch:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2404, '人脸虹膜搜索日志导出', 2399, 5, '#', '', 1, 0, 'F', '0', '0', 'tradelog:faceirisSearch:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2406, '比对日志', 2284, 1, 'compare', 'tradelog/index', 1, 0, 'M', '0', '0', '', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2407, '健康码日志', 2284, 3, 'healthcode', 'tradelog/healthcode/index', 1, 0, 'C', '0', '0', 'tradelog:healthcode:list', '#', 'admin', sysdate(), 'admin', sysdate(), '健康码请求菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2408, '健康码请求查询', 2407, 1, '#', '', 1, 0, 'F', '0', '0', 'tradelog:healthcode:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2412, '健康码请求导出', 2407, 5, '#', '', 1, 0, 'F', '0', '0', 'tradelog:healthcode:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2499, '统计中心', 2109, 8, 'center', 'noninductive/statistics/center', 1, 0, 'C', '1', '0', '', '#', 'admin', NULL, 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2500, '无感设备信息', 2109, 7, 'noninductive', 'noninductive/device/index', 1, 0, 'C', '1', '0', 'noninductive:device:list', '#', 'admin', sysdate(), 'admin', sysdate(), '设备信息菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2501, '接口报文', 2284, 4, 'reqrecord', 'tradelog/reqrecord/index', 1, 0, 'C', '0', '0', 'tradelog:reqrecord:list', '#', 'admin', sysdate(), 'admin', sysdate(), '接口交易请求记录菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2502, '接口交易请求记录查询', 2501, 1, '#', '', 1, 0, 'F', '0', '0', 'tradelog:reqrecord:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2507, '比对体验', 0, 11, 'webtrade', NULL, 1, 0, 'M', '0', '0', '', 'example', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2508, '人脸1v1比对', 2507, 1, 'faceMatchOne', 'webtrade/face/MatchOne', 1, 0, 'C', '0', '0', 'webtrade:face:matchone', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2509, '人脸1vN比对', 2507, 2, 'faceMatchN', 'webtrade/face/MatchN', 1, 0, 'C', '0', '0', 'webtrade:face:matchn', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2510, '人脸图片比较', 2507, 3, 'faceComparetwo', 'webtrade/face/CompareTwo', 1, 0, 'C', '0', '0', 'webtrade:face:comparetwo', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2511, '指纹1v1比对', 2507, 4, 'fingerMatchOne', 'webtrade/finger/MatchOne', 1, 0, 'C', '0', '0', 'webtrade:finger:matchone', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2512, '指纹1vN比对', 2507, 5, 'fingerMatchN', 'webtrade/finger/MatchN', 1, 0, 'C', '0', '0', 'webtrade:finger:matchn', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2513, '指纹图片比对', 2507, 6, 'fingerComparetwo', 'webtrade/finger/CompareTwo', 1, 0, 'C', '0', '0', 'webtrade:finger:comparetwo', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2514, '虹膜1v1比对', 2507, 7, 'irisMatchOne', 'webtrade/iris/MatchOne', 1, 0, 'C', '0', '0', 'webtrade:iris:matchone', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2515, '虹膜1vN比对', 2507, 8, 'irisMatchN', 'webtrade/iris/MatchN', 1, 0, 'C', '0', '0', 'webtrade:iris:matchn', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2516, '虹膜图片比对', 2507, 9, 'irisCompareTwo', 'webtrade/iris/CompareTwo', 1, 0, 'C', '0', '0', 'webtrade:iris:comparetwo', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2517, '多模态1v1比对', 2507, 10, 'faceirisMatchOne', 'webtrade/faceiris/MatchOne', 1, 0, 'C', '0', '0', 'webtrade:faceiris:matchone', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2518, '二维码生成', 3, 4, 'qrcode', 'tool/qrcode/index', 1, 0, 'C', '0', '0', 'tool:qrcode:gen', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2519, '租户管理', 0, 1, 'tenantmgr', NULL, 1, 0, 'M', '0', '0', '', 'tenant', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2520, '租户角色', 2519, 12, 'tenantrole', 'system/tenant/role/index', 1, 0, 'C', '0', '0', 'system:tenantrole:list', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2521, '租户角色查询', 2520, 1, '', '', 1, 0, 'F', '0', '0', 'system:tenantrole:query', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2522, '租户角色新增', 2520, 2, '', '', 1, 0, 'F', '0', '0', 'system:tenantrole:add', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2523, '租户角色修改', 2520, 3, '', '', 1, 0, 'F', '0', '0', 'system:tenantrole:edit', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2524, '租户角色删除', 2520, 4, '', '', 1, 0, 'F', '0', '0', 'system:tenantrole:remove', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2525, '租户角色导出', 2520, 5, '', '', 1, 0, 'F', '0', '0', 'system:tenantrole:export', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2526, '开通试用租户', 2006, 6, '', NULL, 1, 0, 'F', '0', '0', 'system:tenant:opentrial', '#', 'admin', sysdate(), '', NULL, '');

INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2527, '无感设备信息查询', 2500, 1, '#', '', 1, 0, 'F', '0', '0', 'noninductive:device:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2528, '无感设备信息新增', 2500, 2, '#', '', 1, 0, 'F', '0', '0', 'noninductive:device:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2529, '无感设备信息修改', 2500, 3, '#', '', 1, 0, 'F', '0', '0', 'noninductive:device:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2530, '无感设备信息删除', 2500, 4, '#', '', 1, 0, 'F', '0', '0', 'noninductive:device:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2531, '无感设备信息导出', 2500, 5, '#', '', 1, 0, 'F', '0', '0', 'noninductive:device:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2532, '无感设备信息导入', 2500, 6, '', NULL, 1, 0, 'F', '0', '0', 'noninductive:device:import', '#', 'admin', sysdate(), '', NULL, '');
