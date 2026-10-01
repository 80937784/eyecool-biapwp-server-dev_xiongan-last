-- ----------------------------
-- To execute this SQL file, please modify following database name to actual database name
-- ----------------------------
SET @schemaName = 'eyecool_biapwp_global';

-- ----------------------------
-- 1, Store detailed information of each configured jobDetail
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
-- 2, Store information of configured Trigger
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
-- 3, Store simple Trigger, including repeat times, interval and triggered times
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
-- 4, Store Cron Trigger, including Cron expression and time zone information
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
-- 5, Trigger is stored as Blob data type(used when Quartz users use JDBC to create their customized Trigger type, JobStore does not know how to store instance)
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
-- 6, Store calendar information as Blob data type, quartz can configure one calendar to designate time range
-- ---------------------------- 
drop table if exists QRTZ_CALENDARS;
create table QRTZ_CALENDARS (
    sched_name           varchar(120)    not null,
    calendar_name        varchar(200)    not null,
    calendar             blob            not null,
    primary key (sched_name,calendar_name)
) engine=innodb;

-- ----------------------------
-- 7, Store paused Trigger group information
-- ---------------------------- 
drop table if exists QRTZ_PAUSED_TRIGGER_GRPS;
create table QRTZ_PAUSED_TRIGGER_GRPS (
    sched_name           varchar(120)    not null,
    trigger_group        varchar(200)    not null,
    primary key (sched_name,trigger_group)
) engine=innodb;

-- ----------------------------
-- 8, Store status information related to triggered Trigger and execution information of associated Job
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
-- 9, Store small amount status information related to Scheduler, if used in cluster, can see other Scheduler instances
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
-- 10, Store program pessimistic lock information(if used pessimistic lock)
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
-- Related table SQL of System management, etc.
-- ----------------------------
-- ----------------------------
-- 1, Department Table
-- ----------------------------
drop table if exists sys_dept;
create table sys_dept (
  dept_id           bigint(20)      not null auto_increment    comment 'department id',
  parent_id         bigint(20)      default 0                  comment 'parent department id',
  ancestors         varchar(50)     default ''                 comment 'ancestor list',
  dept_code         varchar(50)     default ''                 comment 'department code',
  dept_name         varchar(30)     default ''                 comment 'department name',
  order_num         int(4)          default 0                  comment 'display order',
  leader            varchar(20)     default null               comment 'person in charge',
  phone             varchar(11)     default null               comment 'contact number',
  email             varchar(50)     default null               comment 'email address',
  status            char(1)         default '0'                comment 'department status(0 normal, 1 deactive)',
  del_flag          char(1)         default '0'                comment 'delete flag(0 exist, 2 delete)',
  create_by         varchar(64)     default ''                 comment 'creator',
  create_time 	    datetime                                   comment 'create time',
  update_by         varchar(64)     default ''                 comment 'updater',
  update_time       datetime                                   comment 'update time',
  primary key (dept_id)
) engine=innodb auto_increment=200 comment = 'department table';

-- ----------------------------
-- Initialize - department table data
-- ----------------------------
insert into sys_dept values(100,  0,   '0',     'eyecool',  'Eyecool Technology',   0, 'Eyecool', '15888888888', 'eyecool@eyecool.cn', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values( 99,  100, '0,100', 'default',  'default department',   0, 'Eyecool', '15666666666', 'default@eyecool.cn', '0', '0', 'admin', sysdate(), '', null);

-- ----------------------------
-- 2, User Information Table
-- ----------------------------
drop table if exists sys_user;
create table sys_user (
  user_id           bigint(20)      not null auto_increment    comment 'user ID',
  dept_id           bigint(20)      default null               comment 'department ID',
  user_name         varchar(30)     not null                   comment 'user account',
  nick_name         varchar(30)     default ''                 comment 'user nick name',
  user_type         varchar(2)      default '00'               comment 'user type(00 system user, 01 registered user)',
  email             varchar(50)     default ''                 comment 'user email address',
  phonenumber       varchar(11)     default ''                 comment 'mobile number',
  sex               char(1)         default '0'                comment 'user sex(0 male, 1 female, 2 unknown)',
  avatar            varchar(100)    default ''                 comment 'avatar address',
  password          varchar(100)    default ''                 comment 'password',
  salt				varchar(20)		default '' 				   comment 'salt encryption',
  status            char(1)         default '0'                comment 'account status(0 normal, 1 deactive)',
  del_flag          char(1)         default '0'                comment 'delete flag(0 exist, 2 delete)',
  login_ip          varchar(50)     default ''                 comment 'last login IP',
  login_date        datetime                                   comment 'last login time',
  create_by         varchar(64)     default ''                 comment 'creator',
  create_time       datetime                                   comment 'create time',
  update_by         varchar(64)     default ''                 comment 'updater',
  update_time       datetime                                   comment 'update time',
  remark            varchar(500)    default null               comment 'remark',
  primary key (user_id)
) engine=innodb auto_increment=100 comment = 'user information table';

-- ----------------------------
-- Initialize - User information table data
-- ----------------------------
insert into sys_user values(1,  99, 'admin', 'Eyecool', '00', 'eyecool@163.com', '15888888888', '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '','0', '0', '127.0.0.1', sysdate(), 'admin', sysdate(), '', null, 'administrator');
insert into sys_user values(2,  99, 'eyecool',    'Eyecool', '00', 'eyecool@eyecool.cn',  '15666666666', '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '', '0', '0', '127.0.0.1', sysdate(), 'admin', sysdate(), '', null, 'tester');


-- ----------------------------
-- 3, Post Information Table
-- ----------------------------
drop table if exists sys_post;
create table sys_post
(
  post_id       bigint(20)      not null auto_increment    comment 'post ID',
  post_code     varchar(64)     not null                   comment 'post code',
  post_name     varchar(50)     not null                   comment 'post name',
  post_sort     int(4)          not null                   comment 'display order',
  status        char(1)         not null                   comment 'status(0 normal, 1 deactive)',
  create_by     varchar(64)     default ''                 comment 'creator',
  create_time   datetime                                   comment 'create time',
  update_by     varchar(64)     default ''			       comment 'updater',
  update_time   datetime                                   comment 'update time',
  remark        varchar(500)    default null               comment 'remark',
  primary key (post_id)
) engine=innodb comment = 'post information table';

-- ----------------------------
-- Initialize - post information data
-- ----------------------------
insert into sys_post values(1, 'ceo',  'CEO',    1, '0', 'admin', sysdate(), '', null, '');
insert into sys_post values(2, 'se',   'project manager',  2, '0', 'admin', sysdate(), '', null, '');
insert into sys_post values(3, 'hr',   'HR',  3, '0', 'admin', sysdate(), '', null, '');
insert into sys_post values(4, 'user', 'staff',  4, '0', 'admin', sysdate(), '', null, '');


-- ----------------------------
-- 4, Role Information Table
-- ----------------------------
drop table if exists sys_role;
create table sys_role (
  role_id              bigint(20)      not null auto_increment    comment 'role ID',
  role_name            varchar(30)     not null                   comment 'role name',
  role_key             varchar(100)    not null                   comment 'role privilege character string',
  role_sort            int(4)          not null                   comment 'display order',
  data_scope           char(1)         default '1'                comment 'data range(1: all data privilege, 2: customize data privilege, 3: belonged department data privilege, 4: belonged department and subsidiaries privilege)',
  menu_check_strictly  tinyint(1)      default 1                  comment 'whether to display associated menu tree options',
  dept_check_strictly  tinyint(1)      default 1                  comment 'whether to display associated department tree options',
  status               char(1)         not null                   comment 'role status(0 normal, 1 deactive)',
  del_flag             char(1)         default '0'                comment 'delete flag(0 exist, 2 delete)',
  create_by            varchar(64)     default ''                 comment 'creator',
  create_time          datetime                                   comment 'create time',
  update_by            varchar(64)     default ''                 comment 'updater',
  update_time          datetime                                   comment 'update time',
  remark               varchar(500)    default null               comment 'remark',
  primary key (role_id)
) engine=innodb auto_increment=100 comment = 'role information table';

-- ----------------------------
-- Initialize - role information table data
-- ----------------------------
insert into sys_role values('1', 'super admin',  'admin',  1, 1, 1, 1, '0', '0', 'admin', sysdate(), '', null, 'super admin');
insert into sys_role values('2', 'common role',    'common', 2, 2, 1, 1, '0', '0', 'admin', sysdate(), '', null, 'common role');

-- ----------------------------
-- 5, Menu Privileges Table
-- ----------------------------
drop table if exists sys_menu;
create table sys_menu (
  menu_id           bigint(20)      not null auto_increment    comment 'menu ID',
  menu_name         varchar(100)     not null                   comment 'menu name',
  parent_id         bigint(20)      default 0                  comment 'parent menu ID',
  order_num         int(4)          default 0                  comment 'display order',
  path              varchar(200)    default ''                 comment 'route path',
  component         varchar(255)    default null               comment 'component path',
  is_frame          int(1)          default 1                  comment 'external link or not(0 yes, 1 no)',
  is_cache          int(1)          default 0                  comment 'cache or not(0 yes, 1 no)',
  menu_type         char(1)         default ''                 comment 'menu type(M catalogue, C menu, F button)',
  visible           char(1)         default 0                  comment 'menu status(0 show, 1 hide)',
  status            char(1)         default 0                  comment 'menu status(0 normal, 1 deactive)',
  perms             varchar(100)    default null               comment 'privilege identification',
  icon              varchar(100)    default '#'                comment 'menu icon',
  create_by         varchar(64)     default ''                 comment 'creator',
  create_time       datetime                                   comment 'create time',
  update_by         varchar(64)     default ''                 comment 'updater',
  update_time       datetime                                   comment 'update time',
  remark            varchar(500)    default ''                 comment 'remark',
  primary key (menu_id)
) engine=innodb auto_increment=2000 comment = 'menu privileges table';

-- ----------------------------
-- Initialize - menu privileges table data
-- ----------------------------
-- First Class Menu
insert into sys_menu values('1', 'System Management', '0', '1', 'system',           null,   1, 0, 'M', '0', '0', '', 'sysmgr',   'admin', sysdate(), '', null, 'system management catalogue');
insert into sys_menu values('2', 'System Monitor', '0', '2', 'monitor',          null,   1, 0, 'M', '0', '0', '', 'sysmonitor',  'admin', sysdate(), '', null, 'system monitor catalogue');
insert into sys_menu values('3', 'System Tool', '0', '3', 'tool',             null,   1, 0, 'M', '0', '0', '', 'systool',     'admin', sysdate(), '', null, 'system tool catalogue');
-- Second Class Menu
insert into sys_menu values('100',  'User Management', '1',   '1', 'user',       'system/user/index',        1, 0, 'C', '0', '0', 'system:user:list',        '#',          'admin', sysdate(), '', null, 'user management menu');
insert into sys_menu values('101',  'Role Management', '1',   '2', 'role',       'system/role/index',        1, 0, 'C', '0', '0', 'system:role:list',        '#',       'admin', sysdate(), '', null, 'role management menu');
insert into sys_menu values('102',  'Menu Management', '1',   '3', 'menu',       'system/menu/index',        1, 0, 'C', '0', '0', 'system:menu:list',        '#',    'admin', sysdate(), '', null, 'menu management menu');
insert into sys_menu values('103',  'Department Management', '1',   '4', 'dept',       'system/dept/index',        1, 0, 'C', '0', '0', 'system:dept:list',        '#',          'admin', sysdate(), '', null, 'department management menu');
insert into sys_menu values('104',  'Post Management', '1',   '5', 'post',       'system/post/index',        1, 0, 'C', '0', '0', 'system:post:list',        '#',          'admin', sysdate(), '', null, 'post management menu');
insert into sys_menu values('105',  'Dictionary Management', '1',   '6', 'dict',       'system/dict/index',        1, 0, 'C', '0', '0', 'system:dict:list',        '#',          'admin', sysdate(), '', null, 'dictionary management menu');
insert into sys_menu values('106',  'Parameter Setting', '1',   '7', 'config',     'system/config/index',      1, 0, 'C', '0', '0', 'system:config:list',      '#',          'admin', sysdate(), '', null, 'parameter setting menu');
insert into sys_menu values('107',  'Notice', '1',   '8', 'notice',     'system/notice/index',      1, 0, 'C', '0', '0', 'system:notice:list',      '#',       'admin', sysdate(), '', null, 'notice menu');
insert into sys_menu values('108',  'Log Management', '1',   '9', 'log',        'system/log/index',         1, 0, 'M', '0', '0', '',                        '#',           'admin', sysdate(), '', null, 'log management menu');
insert into sys_menu values('109',  'Online User', '2',   '1', 'online',     'monitor/online/index',     1, 0, 'C', '0', '0', 'monitor:online:list',     '#',        'admin', sysdate(), '', null, 'online user menu');
insert into sys_menu values('110',  'Cron Job', '2',   '2', 'job',        'monitor/job/index',        1, 0, 'C', '0', '0', 'monitor:job:list',        '#',           'admin', sysdate(), '', null, 'cron job menu');
insert into sys_menu values('111',  'Data Monitor', '2',   '3', 'druid',      'monitor/druid/index',      1, 0, 'C', '0', '0', 'monitor:druid:list',      '#',         'admin', sysdate(), '', null, 'data monitor menu');
insert into sys_menu values('112',  'Service Monitor', '2',   '4', 'server',     'monitor/server/index',     1, 0, 'C', '0', '0', 'monitor:server:list',     '#',        'admin', sysdate(), '', null, 'service monitor menu');
insert into sys_menu values('113',  'Table Build', '3',   '1', 'build',      'tool/build/index',         1, 0, 'C', '0', '0', 'tool:build:list',         '#',         'admin', sysdate(), '', null, 'table build menu');
insert into sys_menu values('114',  'Code Generation', '3',   '2', 'gen',        'tool/gen/index',           1, 0, 'C', '0', '0', 'tool:gen:list',           '#',          'admin', sysdate(), '', null, 'code generation menu');
insert into sys_menu values('115',  'System Interface', '3',   '3', 'swagger',    'tool/swagger/index',       1, 0, 'C', '0', '0', 'tool:swagger:list',       '#',       'admin', sysdate(), '', null, 'system interface menu');
-- Third Class Menu
insert into sys_menu values('500',  'Operation Log', '108', '1', 'operlog',    'monitor/operlog/index',    1, 0, 'C', '0', '0', 'monitor:operlog:list',    '#',          'admin', sysdate(), '', null, 'operation log menu');
insert into sys_menu values('501',  'Login Log', '108', '2', 'logininfor', 'monitor/logininfor/index', 1, 0, 'C', '0', '0', 'monitor:logininfor:list', '#',    'admin', sysdate(), '', null, 'login log menu');
-- User Management Button
insert into sys_menu values('1001', 'Query User', '100', '1',  '', '', 1, 0, 'F', '0', '0', 'system:user:query',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1002', 'Add User', '100', '2',  '', '', 1, 0, 'F', '0', '0', 'system:user:add',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1003', 'Edit User', '100', '3',  '', '', 1, 0, 'F', '0', '0', 'system:user:edit',           '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1004', 'Delete User', '100', '4',  '', '', 1, 0, 'F', '0', '0', 'system:user:remove',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1005', 'Export User', '100', '5',  '', '', 1, 0, 'F', '0', '0', 'system:user:export',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1006', 'Import User', '100', '6',  '', '', 1, 0, 'F', '0', '0', 'system:user:import',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1007', 'Reset Password', '100', '7',  '', '', 1, 0, 'F', '0', '0', 'system:user:resetPwd',       '#', 'admin', sysdate(), '', null, '');
-- Role Management Button
insert into sys_menu values('1008', 'Query Role', '101', '1',  '', '', 1, 0, 'F', '0', '0', 'system:role:query',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1009', 'Add Role', '101', '2',  '', '', 1, 0, 'F', '0', '0', 'system:role:add',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1010', 'Edit Role', '101', '3',  '', '', 1, 0, 'F', '0', '0', 'system:role:edit',           '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1011', 'Delete Role', '101', '4',  '', '', 1, 0, 'F', '0', '0', 'system:role:remove',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1012', 'Export Role', '101', '5',  '', '', 1, 0, 'F', '0', '0', 'system:role:export',         '#', 'admin', sysdate(), '', null, '');
-- Menu Management Button
insert into sys_menu values('1013', 'Query Menu', '102', '1',  '', '', 1, 0, 'F', '0', '0', 'system:menu:query',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1014', 'Add Menu', '102', '2',  '', '', 1, 0, 'F', '0', '0', 'system:menu:add',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1015', 'Edit Menu', '102', '3',  '', '', 1, 0, 'F', '0', '0', 'system:menu:edit',           '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1016', 'Delete Menu', '102', '4',  '', '', 1, 0, 'F', '0', '0', 'system:menu:remove',         '#', 'admin', sysdate(), '', null, '');
-- Department Management Button
insert into sys_menu values('1017', 'Query Department', '103', '1',  '', '', 1, 0, 'F', '0', '0', 'system:dept:query',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1018', 'Add Department', '103', '2',  '', '', 1, 0, 'F', '0', '0', 'system:dept:add',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1019', 'Edit Department', '103', '3',  '', '', 1, 0, 'F', '0', '0', 'system:dept:edit',           '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1020', 'Delete Department', '103', '4',  '', '', 1, 0, 'F', '0', '0', 'system:dept:remove',         '#', 'admin', sysdate(), '', null, '');
-- Post Management Button
insert into sys_menu values('1021', 'Query Post', '104', '1',  '', '', 1, 0, 'F', '0', '0', 'system:post:query',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1022', 'Add Post', '104', '2',  '', '', 1, 0, 'F', '0', '0', 'system:post:add',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1023', 'Edit Post', '104', '3',  '', '', 1, 0, 'F', '0', '0', 'system:post:edit',           '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1024', 'Delete Post', '104', '4',  '', '', 1, 0, 'F', '0', '0', 'system:post:remove',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1025', 'Export Post', '104', '5',  '', '', 1, 0, 'F', '0', '0', 'system:post:export',         '#', 'admin', sysdate(), '', null, '');
-- Dictionary Management Button
insert into sys_menu values('1026', 'Query Dictionary', '105', '1', '#', '', 1, 0, 'F', '0', '0', 'system:dict:query',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1027', 'Add Dictionary', '105', '2', '#', '', 1, 0, 'F', '0', '0', 'system:dict:add',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1028', 'Edit Dictionary', '105', '3', '#', '', 1, 0, 'F', '0', '0', 'system:dict:edit',           '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1029', 'Delete Dictionary', '105', '4', '#', '', 1, 0, 'F', '0', '0', 'system:dict:remove',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1030', 'Export Dictionary', '105', '5', '#', '', 1, 0, 'F', '0', '0', 'system:dict:export',         '#', 'admin', sysdate(), '', null, '');
-- Parameter Setting Button
insert into sys_menu values('1031', 'Query Parameter', '106', '1', '#', '', 1, 0, 'F', '0', '0', 'system:config:query',        '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1032', 'Add Parameter', '106', '2', '#', '', 1, 0, 'F', '0', '0', 'system:config:add',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1033', 'Edit Parameter', '106', '3', '#', '', 1, 0, 'F', '0', '0', 'system:config:edit',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1034', 'Delete Parameter', '106', '4', '#', '', 1, 0, 'F', '0', '0', 'system:config:remove',       '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1035', 'Export Parameter', '106', '5', '#', '', 1, 0, 'F', '0', '0', 'system:config:export',       '#', 'admin', sysdate(), '', null, '');
-- Notice Button
insert into sys_menu values('1036', 'Query Notice', '107', '1', '#', '', 1, 0, 'F', '0', '0', 'system:notice:query',        '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1037', 'Add Notice', '107', '2', '#', '', 1, 0, 'F', '0', '0', 'system:notice:add',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1038', 'Edit Notice', '107', '3', '#', '', 1, 0, 'F', '0', '0', 'system:notice:edit',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1039', 'Delete Notice', '107', '4', '#', '', 1, 0, 'F', '0', '0', 'system:notice:remove',       '#', 'admin', sysdate(), '', null, '');
-- Operation Log Button
insert into sys_menu values('1040', 'Query Operation', '500', '1', '#', '', 1, 0, 'F', '0', '0', 'monitor:operlog:query',      '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1041', 'Delete Operation', '500', '2', '#', '', 1, 0, 'F', '0', '0', 'monitor:operlog:remove',     '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1042', 'Export Log', '500', '4', '#', '', 1, 0, 'F', '0', '0', 'monitor:operlog:export',     '#', 'admin', sysdate(), '', null, '');
-- Login Log Button
insert into sys_menu values('1043', 'Query Login', '501', '1', '#', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:query',   '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1044', 'Delete Login', '501', '2', '#', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:remove',  '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1045', 'Export Log', '501', '3', '#', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:export',  '#', 'admin', sysdate(), '', null, '');
-- Online User Button
insert into sys_menu values('1046', 'Query Online', '109', '1', '#', '', 1, 0, 'F', '0', '0', 'monitor:online:query',       '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1047', 'Batch Force Logout', '109', '2', '#', '', 1, 0, 'F', '0', '0', 'monitor:online:batchLogout', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1048', 'Single Force Logout', '109', '3', '#', '', 1, 0, 'F', '0', '0', 'monitor:online:forceLogout', '#', 'admin', sysdate(), '', null, '');
-- Cron Job Button
insert into sys_menu values('1049', 'Query Job', '110', '1', '#', '', 1, 0, 'F', '0', '0', 'monitor:job:query',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1050', 'Add Job', '110', '2', '#', '', 1, 0, 'F', '0', '0', 'monitor:job:add',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1051', 'Edit Job', '110', '3', '#', '', 1, 0, 'F', '0', '0', 'monitor:job:edit',           '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1052', 'Delete Job', '110', '4', '#', '', 1, 0, 'F', '0', '0', 'monitor:job:remove',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1053', 'Edit Status', '110', '5', '#', '', 1, 0, 'F', '0', '0', 'monitor:job:changeStatus',   '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1054', 'Export Job', '110', '7', '#', '', 1, 0, 'F', '0', '0', 'monitor:job:export',         '#', 'admin', sysdate(), '', null, '');
-- Code Generation Button
insert into sys_menu values('1055', 'Query Generation', '114', '1', '#', '', 1, 0, 'F', '0', '0', 'tool:gen:query',             '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1056', 'Edit Generation', '114', '2', '#', '', 1, 0, 'F', '0', '0', 'tool:gen:edit',              '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1057', 'Delete Generation', '114', '3', '#', '', 1, 0, 'F', '0', '0', 'tool:gen:remove',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1058', 'Import Code', '114', '2', '#', '', 1, 0, 'F', '0', '0', 'tool:gen:import',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1059', 'Preview Code', '114', '4', '#', '', 1, 0, 'F', '0', '0', 'tool:gen:preview',           '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1060', 'Generate Code', '114', '5', '#', '', 1, 0, 'F', '0', '0', 'tool:gen:code',              '#', 'admin', sysdate(), '', null, '');


-- ----------------------------
-- 6, User and Role Relation Table   User N-1 Role
-- ----------------------------
drop table if exists sys_user_role;
create table sys_user_role (
  user_id   bigint(20) not null comment 'user ID',
  role_id   bigint(20) not null comment 'role ID',
  primary key(user_id, role_id)
) engine=innodb comment = 'user and role relation table';

-- ----------------------------
-- Initialize - User and Role Relation Table Data
-- ----------------------------
insert into sys_user_role values ('1', '1');
insert into sys_user_role values ('2', '2');


-- ----------------------------
-- 7, Role and Menu Relation Table   User 1-N Menu
-- ----------------------------
drop table if exists sys_role_menu;
create table sys_role_menu (
  role_id   bigint(20) not null comment 'role ID',
  menu_id   bigint(20) not null comment 'menu ID',
  primary key(role_id, menu_id)
) engine=innodb comment = 'Role and Menu Relation Table';

-- ----------------------------
-- Initialize - Role and Menu Relation Table Data
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
-- 8, Role and Department Relation Table   Role 1-N Department
-- ----------------------------
drop table if exists sys_role_dept;
create table sys_role_dept (
  role_id   bigint(20) not null comment 'role ID',
  dept_id   bigint(20) not null comment 'department ID',
  primary key(role_id, dept_id)
) engine=innodb comment = 'Role and Department Relation Table';

-- ----------------------------
-- Initialize - Role and Department Relation Table Data
-- ----------------------------
insert into sys_role_dept values ('2', '99');

-- ----------------------------
-- 9, User and Post Relation Table    User 1-N Post
-- ----------------------------
drop table if exists sys_user_post;
create table sys_user_post
(
  user_id   bigint(20) not null comment 'user ID',
  post_id   bigint(20) not null comment 'post ID',
  primary key (user_id, post_id)
) engine=innodb comment = 'User and Post Relation Table';

-- ----------------------------
-- Initialize - User and Post Relation Table Data
-- ----------------------------
insert into sys_user_post values ('1', '1');
insert into sys_user_post values ('2', '2');

-- ----------------------------
-- 10, Operation Log Record
-- ----------------------------
drop table if exists sys_oper_log;
create table sys_oper_log (
  oper_id           bigint(20)      not null auto_increment    comment 'log primary key',
  title             varchar(50)     default ''                 comment 'module title',
  business_type     int(2)          default 0                  comment 'business type(0 other, 1 add, 2 edit, 3 delete)',
  method            varchar(100)    default ''                 comment 'method name',
  request_method    varchar(10)     default ''                 comment 'request mode',
  operator_type     int(1)          default 0                  comment 'operation type(0 other, 1 backend user, 2 mobile end user)',
  oper_name         varchar(50)     default ''                 comment 'operation personnel',
  dept_name         varchar(50)     default ''                 comment 'department name',
  oper_url          varchar(255)    default ''                 comment 'request URL',
  oper_ip           varchar(50)     default ''                 comment 'host address',
  oper_location     varchar(255)    default ''                 comment 'operation location',
  oper_param        varchar(2000)   default ''                 comment 'request parameter',
  json_result       varchar(2000)   default ''                 comment 'return parameter',
  status            int(1)          default 0                  comment 'operation status(0 normal, 1 abnormal)',
  error_msg         varchar(2000)   default ''                 comment 'error message',
  oper_time         datetime                                   comment 'operation time',
  primary key (oper_id)
) engine=innodb auto_increment=100 comment = 'Operation Log Record';


-- ----------------------------
-- 11, Dictionary Type Table
-- ----------------------------
drop table if exists sys_dict_type;
create table sys_dict_type
(
  dict_id          bigint(20)      not null auto_increment    comment 'dictionary primary key',
  dict_name        varchar(100)    default ''                 comment 'dictionary name',
  dict_type        varchar(100)    default ''                 comment 'dictionary type',
  status           char(1)         default '0'                comment 'status(0 normal, 1 deactive)',
  create_by        varchar(64)     default ''                 comment 'creator',
  create_time      datetime                                   comment 'create time',
  update_by        varchar(64)     default ''                 comment 'updater',
  update_time      datetime                                   comment 'update time',
  remark           varchar(500)    default null               comment 'remark',
  primary key (dict_id),
  unique (dict_type)
) engine=innodb auto_increment=100 comment = 'Dictionary Type Table';

insert into sys_dict_type values(1,  'User Sex', 'sys_user_sex',        '0', 'admin', sysdate(), '', null, 'User Sex List');
insert into sys_dict_type values(2,  'Menu Status', 'sys_show_hide',       '0', 'admin', sysdate(), '', null, 'Menu Status List');
insert into sys_dict_type values(3,  'System Switch', 'sys_normal_disable',  '0', 'admin', sysdate(), '', null, 'System Switch List');
insert into sys_dict_type values(4,  'Job Status', 'sys_job_status',      '0', 'admin', sysdate(), '', null, 'Job Status List');
insert into sys_dict_type values(5,  'Job Group', 'sys_job_group',       '0', 'admin', sysdate(), '', null, 'Job Group List');
insert into sys_dict_type values(6,  'System Yes/No', 'sys_yes_no',          '0', 'admin', sysdate(), '', null, 'System Yes/No List');
insert into sys_dict_type values(7,  'Notice Type', 'sys_notice_type',     '0', 'admin', sysdate(), '', null, 'Notice Type List');
insert into sys_dict_type values(8,  'Notice Status', 'sys_notice_status',   '0', 'admin', sysdate(), '', null, 'Notice Status List');
insert into sys_dict_type values(9,  'Operation Type', 'sys_oper_type',       '0', 'admin', sysdate(), '', null, 'Operation Type List');
insert into sys_dict_type values(10, 'System Status', 'sys_common_status',   '0', 'admin', sysdate(), '', null, 'System Status List');

-- ----------------------------
-- 12, Dictionary Data Table
-- ----------------------------
drop table if exists sys_dict_data;
create table sys_dict_data
(
  dict_code        bigint(20)      not null auto_increment    comment 'dictionary code',
  dict_sort        int(4)          default 0                  comment 'dictionary sort',
  dict_label       varchar(100)    default ''                 comment 'dictionary label',
  dict_value       varchar(100)    default ''                 comment 'dictionary key value',
  dict_type        varchar(100)    default ''                 comment 'dictionary type',
  css_class        varchar(100)    default null               comment 'style attribute(other style extension)',
  list_class       varchar(100)    default null               comment 'table echo style',
  is_default       char(1)         default 'N'                comment 'default or not(Y: yes, N: no)',
  status           char(1)         default '0'                comment 'status(0 normal, 1 deactive)',
  create_by        varchar(64)     default ''                 comment 'creator',
  create_time      datetime                                   comment 'create time',
  update_by        varchar(64)     default ''                 comment 'updater',
  update_time      datetime                                   comment 'update time',
  remark           varchar(500)    default null               comment 'remark',
  primary key (dict_code)
) engine=innodb auto_increment=100 comment = 'Dictionary Data Table';

insert into sys_dict_data values(1,  1,  'Male',       '0',       'sys_user_sex',        '',   '',        'Y', '0', 'admin', sysdate(), '', null, 'sex is male');
insert into sys_dict_data values(2,  2,  'Female',       '1',       'sys_user_sex',        '',   '',        'N', '0', 'admin', sysdate(), '', null, 'sex is female');
insert into sys_dict_data values(3,  3,  'Unknown',     '2',       'sys_user_sex',        '',   '',        'N', '0', 'admin', sysdate(), '', null, 'unknown sex');
insert into sys_dict_data values(4,  1,  'Show',     '0',       'sys_show_hide',       '',   'primary', 'Y', '0', 'admin', sysdate(), '', null, 'show menu');
insert into sys_dict_data values(5,  2,  'Hide',     '1',       'sys_show_hide',       '',   'danger',  'N', '0', 'admin', sysdate(), '', null, 'hide menu');
insert into sys_dict_data values(6,  1,  'Normal',     '0',       'sys_normal_disable',  '',   'primary', 'Y', '0', 'admin', sysdate(), '', null, 'normal status');
insert into sys_dict_data values(7,  2,  'Deactive',     '1',       'sys_normal_disable',  '',   'danger',  'N', '0', 'admin', sysdate(), '', null, 'deactive status');
insert into sys_dict_data values(8,  1,  'Normal',     '0',       'sys_job_status',      '',   'primary', 'Y', '0', 'admin', sysdate(), '', null, 'normal status');
insert into sys_dict_data values(9,  2,  'Pause',     '1',       'sys_job_status',      '',   'danger',  'N', '0', 'admin', sysdate(), '', null, 'deactive status');
insert into sys_dict_data values(10, 1,  'Default',     'DEFAULT', 'sys_job_group',       '',   '',        'Y', '0', 'admin', sysdate(), '', null, 'default grouping');
insert into sys_dict_data values(11, 2,  'System',     'SYSTEM',  'sys_job_group',       '',   '',        'N', '0', 'admin', sysdate(), '', null, 'system grouping');
insert into sys_dict_data values(12, 1,  'Yes',       'Y',       'sys_yes_no',          '',   'primary', 'Y', '0', 'admin', sysdate(), '', null, 'system default yes');
insert into sys_dict_data values(13, 2,  'No',       'N',       'sys_yes_no',          '',   'danger',  'N', '0', 'admin', sysdate(), '', null, 'system default no');
insert into sys_dict_data values(14, 1,  'Notice',     '1',       'sys_notice_type',     '',   'warning', 'Y', '0', 'admin', sysdate(), '', null, 'notice');
insert into sys_dict_data values(15, 2,  'Announcement',     '2',       'sys_notice_type',     '',   'success', 'N', '0', 'admin', sysdate(), '', null, 'announcement');
insert into sys_dict_data values(16, 1,  'Normal',     '0',       'sys_notice_status',   '',   'primary', 'Y', '0', 'admin', sysdate(), '', null, 'normal status');
insert into sys_dict_data values(17, 2,  'Close',     '1',       'sys_notice_status',   '',   'danger',  'N', '0', 'admin', sysdate(), '', null, 'close status');
insert into sys_dict_data values(18, 1,  'Add',     '1',       'sys_oper_type',       '',   'info',    'N', '0', 'admin', sysdate(), '', null, 'add operation');
insert into sys_dict_data values(19, 2,  'Edit',     '2',       'sys_oper_type',       '',   'info',    'N', '0', 'admin', sysdate(), '', null, 'edit operation');
insert into sys_dict_data values(20, 3,  'Delete',     '3',       'sys_oper_type',       '',   'danger',  'N', '0', 'admin', sysdate(), '', null, 'delete operation');
insert into sys_dict_data values(21, 4,  'Authorize',     '4',       'sys_oper_type',       '',   'primary', 'N', '0', 'admin', sysdate(), '', null, 'authorize operation');
insert into sys_dict_data values(22, 5,  'Export',     '5',       'sys_oper_type',       '',   'warning', 'N', '0', 'admin', sysdate(), '', null, 'export operation');
insert into sys_dict_data values(23, 6,  'Import',     '6',       'sys_oper_type',       '',   'warning', 'N', '0', 'admin', sysdate(), '', null, 'import operation');
insert into sys_dict_data values(24, 7,  'Force Logout',     '7',       'sys_oper_type',       '',   'danger',  'N', '0', 'admin', sysdate(), '', null, 'force logout operation');
insert into sys_dict_data values(25, 8,  'Code Generation', '8',       'sys_oper_type',       '',   'warning', 'N', '0', 'admin', sysdate(), '', null, 'generation operation');
insert into sys_dict_data values(26, 9,  'Clear Data', '9',       'sys_oper_type',       '',   'danger',  'N', '0', 'admin', sysdate(), '', null, 'clear operation');
insert into sys_dict_data values(27, 1,  'Success',     '0',       'sys_common_status',   '',   'primary', 'N', '0', 'admin', sysdate(), '', null, 'normal status');
insert into sys_dict_data values(28, 2,  'Failure',     '1',       'sys_common_status',   '',   'danger',  'N', '0', 'admin', sysdate(), '', null, 'deactive status');


-- ----------------------------
-- 13, Parameter Configuration Table
-- ----------------------------
drop table if exists sys_config;
create table sys_config (
  config_id         int(5)          not null auto_increment    comment 'parameter primary key',
  config_name       varchar(100)    default ''                 comment 'parameter name',
  config_key        varchar(100)    default ''                 comment 'parameter key name',
  config_value      varchar(500)    default ''                 comment 'parameter key value',
  config_type       char(1)         default 'N'                comment 'system built-in(Y: yes, N: no)',
  create_by         varchar(64)     default ''                 comment 'creator',
  create_time       datetime                                   comment 'create time',
  update_by         varchar(64)     default ''                 comment 'updater',
  update_time       datetime                                   comment 'update time',
  remark            varchar(500)    default null               comment 'remark',
  primary key (config_id)
) engine=innodb auto_increment=100 comment = 'Parameter Configuration Table';

insert into sys_config values(1, 'Main Frame Page - Default Skin Style Name', 'sys.index.skinName',     'skin-blue',     'Y', 'admin', sysdate(), '', null, 'blue: skin-blue, green: skin-green, purple: skin-purple, red: skin-red, yellow: skin-yellow' );
insert into sys_config values(2, 'User Management - Account Initial Password',     'sys.user.initPassword',  '123456',        'Y', 'admin', sysdate(), '', null, 'Initial password 123456' );
insert into sys_config values(3, 'Main Frame Page - Sidebar Theme',       'sys.index.sideTheme',    'theme-dark',    'Y', 'admin', sysdate(), '', null, 'dark theme: theme-dark,light theme: theme-light' );


-- ----------------------------
-- 14, System Login Record
-- ----------------------------
drop table if exists sys_logininfor;
create table sys_logininfor (
  info_id        bigint(20)     not null auto_increment   comment 'login ID',
  user_name      varchar(50)    default ''                comment 'user account',
  ipaddr         varchar(50)    default ''                comment 'login IP address',
  login_location varchar(255)   default ''                comment 'login location',
  browser        varchar(50)    default ''                comment 'browser type',
  os             varchar(50)    default ''                comment 'operation system',
  status         char(1)        default '0'               comment 'login status(0 success, 1 failure)',
  msg            varchar(255)   default ''                comment 'prompt message',
  login_time     datetime                                 comment 'login time',
  primary key (info_id)
) engine=innodb auto_increment=100 comment = 'System Login Record';


-- ----------------------------
-- 15, Cron Job Scheduling Table
-- ----------------------------
drop table if exists sys_job;
create table sys_job (
  job_id              bigint(20)    not null auto_increment    comment 'job ID',
  job_name            varchar(64)   default ''                 comment 'job name',
  job_group           varchar(64)   default 'DEFAULT'          comment 'job group name',
  invoke_target       varchar(500)  not null                   comment 'invoke target character string',
  cron_expression     varchar(255)  default ''                 comment 'cron execution expression',
  misfire_policy      varchar(20)   default '3'                comment 'plan to execute false policy(1 execute immediately, 2 execute once, 3 abandon execution)',
  concurrent          char(1)       default '1'                comment 'whether to execute concurrently(0 admit, 1 forbidden)',
  status              char(1)       default '0'                comment 'status(0 normal, 1 pause)',
  create_by           varchar(64)   default ''                 comment 'creator',
  create_time         datetime                                 comment 'create time',
  update_by           varchar(64)   default ''                 comment 'updater',
  update_time         datetime                                 comment 'update time',
  remark              varchar(500)  default ''                 comment 'remark information',
  primary key (job_id, job_name, job_group)
) engine=innodb auto_increment=100 comment = 'Cron Job Scheduling Table';

-- ----------------------------
-- 16, Cron Job Scheduling Log Table
-- ----------------------------
drop table if exists sys_job_log;
create table sys_job_log (
  job_log_id          bigint(20)     not null auto_increment    comment 'job log ID',
  job_name            varchar(64)    not null                   comment 'job name',
  job_group           varchar(64)    not null                   comment 'job group name',
  invoke_target       varchar(500)   not null                   comment 'invoke target character string',
  job_message         varchar(500)                              comment 'log information',
  status              char(1)        default '0'                comment 'execution status(0 normal, 1 failure)',
  exception_info      varchar(2000)  default ''                 comment 'exception information',
  create_time         datetime                                  comment 'create time',
  primary key (job_log_id)
) engine=innodb comment = 'Cron Job Scheduling Log Table';


-- ----------------------------
-- 17, Notice and Announcement Table
-- ----------------------------
drop table if exists sys_notice;
create table sys_notice (
  notice_id         int(4)          not null auto_increment    comment 'notice ID',
  notice_title      varchar(100)     not null                   comment 'notice title',
  notice_type       char(1)         not null                   comment 'notice type(1 notice, 2 announcement)',
  notice_content    longblob        default null               comment 'notice content',
  status            char(1)         default '0'                comment 'notice status(0 normal,1close)',
  create_by         varchar(64)     default ''                 comment 'creator',
  create_time       datetime                                   comment 'create time',
  update_by         varchar(64)     default ''                 comment 'updater',
  update_time       datetime                                   comment 'update time',
  remark            varchar(255)    default null               comment 'remark',
  primary key (notice_id)
) engine=innodb auto_increment=10 comment = 'Notice and Announcement Table';

-- ----------------------------
-- Initialization - Notice Information Table Data
-- ----------------------------
insert into sys_notice values('1', 'Notice: 2018-07-01 Eyecool new version published', '2', 'new version content', '0', 'admin', sysdate(), '', null, 'administrator');
insert into sys_notice values('2', 'Maintenance notice: 2018-07-01 Eyecool system will be maintained in midnight', '1', 'Maintenance content',   '0', 'admin', sysdate(), '', null, 'administrator');


-- ----------------------------
-- 18, Code Generation Business Table
-- ----------------------------
drop table if exists gen_table;
create table gen_table (
  table_id          bigint(20)      not null auto_increment    comment 'ID',
  table_name        varchar(200)    default ''                 comment 'table name',
  table_comment     varchar(500)    default ''                 comment 'table description',
  class_name        varchar(100)    default ''                 comment 'entity class name',
  tpl_category      varchar(200)    default 'crud'             comment 'used template(crud: single table operation, tree: tree table operation)',
  package_name      varchar(100)                               comment 'package generation path',
  module_name       varchar(30)                                comment 'module generation name',
  business_name     varchar(30)                                comment 'business generation name',
  function_name     varchar(50)                                comment 'function generation name',
  function_author   varchar(50)                                comment 'function generation author',
  gen_type          char(1)         default '0'                comment 'code generation mode(0 zip, 1 customized path)',
  gen_path          varchar(200)    default '/'                comment 'generation path(default project path if no content is filled)',
  options           varchar(1000)                              comment 'other generation options',
  create_by         varchar(64)     default ''                 comment 'creator',
  create_time 	    datetime                                   comment 'create time',
  update_by         varchar(64)     default ''                 comment 'updater',
  update_time       datetime                                   comment 'update time',
  remark            varchar(500)    default null               comment 'remark',
  primary key (table_id)
) engine=innodb auto_increment=1 comment = 'Code Generation Business Table';


-- ----------------------------
-- 19, Code Generation Business Table Field
-- ----------------------------
drop table if exists gen_table_column;
create table gen_table_column (
  column_id         bigint(20)      not null auto_increment    comment 'ID',
  table_id          varchar(64)                                comment 'belonged table ID',
  column_name       varchar(200)                               comment 'column name',
  column_comment    varchar(500)                               comment 'column description',
  column_type       varchar(100)                               comment 'column type',
  java_type         varchar(500)                               comment 'JAVA type',
  java_field        varchar(200)                               comment 'JAVA field name',
  is_pk             char(1)                                    comment 'primary key or not(1 yes)',
  is_increment      char(1)                                    comment 'self-increment or not(1 yes)',
  is_required       char(1)                                    comment 'compulsory or not(1 yes)',
  is_insert         char(1)                                    comment 'insert field or not(1 yes)',
  is_edit           char(1)                                    comment 'edit field or not(1 yes)',
  is_list           char(1)                                    comment 'column field or not（1 yes)',
  is_query          char(1)                                    comment 'query field or not(1 yes)',
  query_type        varchar(200)    default 'EQ'               comment 'query mode(equal, not equal, more than, less than, range)',
  html_type         varchar(200)                               comment 'display type(textbox, text field, dropdown, checkbox, radio, date control)',
  dict_type         varchar(200)    default ''                 comment 'dictionary type',
  sort              int                                        comment 'sort',
  create_by         varchar(64)     default ''                 comment 'creator',
  create_time 	    datetime                                   comment 'create time',
  update_by         varchar(64)     default ''                 comment 'updater',
  update_time       datetime                                   comment 'update time',
  primary key (column_id)
) engine=innodb auto_increment=1 comment = 'Code Generation Business Table Field';

-- ----------------------------
-- Single Sign-on Terminal Information Management
-- ----------------------------
CREATE TABLE sys_client_details (
  client_id varchar(255) NOT NULL COMMENT 'terminal ID',
  resource_ids varchar(255) DEFAULT NULL COMMENT 'resource ID flag',
  client_secret varchar(255) NOT NULL COMMENT 'terminal security code',
  scope varchar(255) NOT NULL COMMENT 'terminal authorization range',
  web_server_redirect_uri varchar(255) DEFAULT NULL COMMENT 'server callback address',
  authorities varchar(255) DEFAULT NULL COMMENT 'authority needed to visit resource',
  additional_information varchar(4096) DEFAULT NULL COMMENT 'additional information',
  origin_secret varchar(255) NOT NULL COMMENT 'terminal plaintext security code',
  PRIMARY KEY (client_id)
) COMMENT='terminal configuration table';

-- ----------------------------
-- Single Sign-on Terminal Management Menu
-- ----------------------------
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2000, 'Terminal Configuration', 1, 10, 'client', 'system/client/index', 1, 0, 'C', '0', '0', 'system:client:list', '#', 'admin', sysdate(), '', NULL, 'terminal configuration menu');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2001, 'Query Terminal Configuration ', 2000, 1, '#', '', 1, 0, 'F', '0', '0', 'system:client:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2002, 'Add Terminal Configuration', 2000, 2, '#', '', 1, 0, 'F', '0', '0', 'system:client:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2003, 'Edit Terminal Configuration', 2000, 3, '#', '', 1, 0, 'F', '0', '0', 'system:client:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2004, 'Delete Terminal Configuration', 2000, 4, '#', '', 1, 0, 'F', '0', '0', 'system:client:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2005, 'Export Terminal Configuration', 2000, 5, '#', '', 1, 0, 'F', '0', '0', 'system:client:export', '#', 'admin', sysdate(), '', NULL, '');
-- ----------------------------
-- WEB Terminal
-- ----------------------------
INSERT INTO sys_client_details(client_id, resource_ids, client_secret, scope, web_server_redirect_uri, authorities, additional_information, origin_secret) VALUES ('web', NULL, '$2a$10$OsbchgUAxA6MLchR2L1jvejxfTYi5XPoPwQUNERNr.vcH.ivnb5HO', 'server', NULL, NULL, NULL, '123456');
INSERT INTO sys_client_details(client_id, resource_ids, client_secret, scope, web_server_redirect_uri, authorities, additional_information, origin_secret) VALUES ('app', NULL, '$2a$10$uKuoTb8Sa5ocmTxJpap1tu4vChbVifBe4uXqohCcwWJRbZvlqGsdC', 'server', NULL, NULL, NULL, '123456');

CREATE TABLE sys_tenant_role (
  id varchar(48) NOT NULL COMMENT 'role ID',
  role_name varchar(30) NOT NULL COMMENT 'role name',
  role_key varchar(100) NOT NULL COMMENT 'role privilege character string',
  role_sort int(4) NOT NULL COMMENT 'display order',
  menu_check_strictly tinyint(1) DEFAULT '1' COMMENT 'whether to display associated menu tree options',
  status char(1) NOT NULL COMMENT 'role status(0 normal, 1 deactive)',
  del_flag char(1) DEFAULT '0' COMMENT 'delete flag(0 exist, 2 delete)',
  create_by varchar(64) DEFAULT '' COMMENT 'creator',
  create_time datetime DEFAULT NULL COMMENT 'create time',
  update_by varchar(64) DEFAULT '' COMMENT 'updater',
  update_time datetime DEFAULT NULL COMMENT 'update time',
  remark varchar(500) DEFAULT NULL COMMENT 'remark',
  PRIMARY KEY (id)
) COMMENT='tenant role information table';

CREATE TABLE sys_tenant_role_menu (
  role_id varchar(48) NOT NULL COMMENT 'role ID',
  menu_id bigint(20) NOT NULL COMMENT ' menu ID',
  PRIMARY KEY (role_id,menu_id)
) COMMENT='tenant role and menu relationship table';
-- ----------------------------
-- Tenant Information Table
-- ----------------------------
CREATE TABLE sys_tenant (
  id bigint(20) NOT NULL AUTO_INCREMENT COMMENT ' primary key',
  tenant_id varchar(128) NOT NULL DEFAULT '' COMMENT 'tenant ID',
  tenant_name varchar(128) DEFAULT '' COMMENT 'tenant name',
  tenant_type varchar(18) DEFAULT 'TRIAL' COMMENT 'tenant type(TRIAL: trail tenant, FORMAL: formal tenant)',
  tenant_desc varchar(255) DEFAULT NULL COMMENT 'tenant description',
  tenant_state varchar(18) DEFAULT 'NORMAL' COMMENT 'tenant status(NORMAL: normal, FROZEN: frozen)',
  contact_name varchar(255) DEFAULT NULL COMMENT 'contact name',
  phone varchar(20) DEFAULT NULL COMMENT 'contact number',
  email varchar(255) DEFAULT NULL COMMENT 'Email address',
  tenant_role_id varchar(48) DEFAULT NULL COMMENT 'tenant role ID',
  create_source varchar(32) DEFAULT 'BG_CREATE' COMMENT 'create source(REGIST: register, BG_CREATE: create in backend)',
  effective_time datetime NOT NULL COMMENT 'effective time',
  expire_time datetime NOT NULL COMMENT 'expire time',
  create_time datetime NOT NULL COMMENT 'create time',
  update_time datetime NOT NULL COMMENT 'update time',
  PRIMARY KEY (id),
  UNIQUE KEY uk_tenant_id (tenant_id) COMMENT 'tenant Id unique index',
  UNIQUE KEY uk_tenant_name (tenant_name) COMMENT 'tenant Name unique index'
) COMMENT='tenant information table';
INSERT INTO sys_tenant(id, tenant_id, tenant_name, tenant_type, tenant_desc, tenant_state, contact_name, phone, create_source, effective_time, expire_time, create_time, update_time) VALUES (1, 'super', 'systemtenant', 'FORMAL', 'systemtenant', 'NORMAL', NULL, NULL, 'BG_CREATE', '2020-11-09 00:00:00', '2099-11-30 00:00:00', sysdate(), sysdate());

-- ----------------------------2
-- Tenant Management Menu
-- ----------------------------
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2006, 'Tenant Information', 2519, 11, 'tenant', 'system/tenant/index', 1, 0, 'C', '0', '0', 'system:tenant:list', '#', 'admin', sysdate(), '', NULL, 'tenant information menu');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2007, 'Query Tenant Information', 2006, 1, '#', '', 1, 0, 'F', '0', '0', 'system:tenant:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2008, 'Add Tenant Information', 2006, 2, '#', '', 1, 0, 'F', '0', '0', 'system:tenant:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2009, 'Edit Tenant Information', 2006, 3, '#', '', 1, 0, 'F', '0', '0', 'system:tenant:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2010, 'Delete Tenant Information', 2006, 4, '#', '', 1, 0, 'F', '0', '0', 'system:tenant:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2011, 'Export Tenant Information', 2006, 5, '#', '', 1, 0, 'F', '0', '0', 'system:tenant:export', '#', 'admin', sysdate(), '', NULL, '');

-- ----------------------------
-- Add Tenant Field in System Table
-- ----------------------------
ALTER TABLE sys_user ADD COLUMN tenant_id varchar(255) NULL DEFAULT 'super' COMMENT 'tenant ID';
ALTER TABLE sys_role ADD COLUMN tenant_id varchar(255) NULL DEFAULT 'super' COMMENT 'tenant ID';
ALTER TABLE sys_post ADD COLUMN tenant_id varchar(255) NULL DEFAULT 'super' COMMENT 'tenant ID';
ALTER TABLE sys_oper_log ADD COLUMN tenant_id varchar(255) NULL DEFAULT 'super' COMMENT 'tenant ID';
ALTER TABLE sys_notice ADD COLUMN tenant_id varchar(255) NULL DEFAULT 'super' COMMENT 'tenant ID';
ALTER TABLE sys_logininfor ADD COLUMN tenant_id varchar(255) NULL DEFAULT 'super' COMMENT 'tenant ID';
ALTER TABLE sys_dept ADD COLUMN tenant_id varchar(255) NULL DEFAULT 'super' COMMENT 'tenant ID';

update sys_user set tenant_id = 'super' where tenant_id is null;
update sys_role set tenant_id = 'super' where tenant_id is null;
update sys_post set tenant_id = 'super' where tenant_id is null;
update sys_oper_log set tenant_id = 'super' where tenant_id is null;
update sys_notice set tenant_id = 'super' where tenant_id is null;
update sys_logininfor set tenant_id = 'super' where tenant_id is null;
update sys_dept set tenant_id = 'super' where tenant_id is null;

-- ----------------------------
-- Tenant Parameter Information Table
-- ----------------------------
CREATE TABLE sys_tenant_config (
  config_id int(5) NOT NULL COMMENT 'parameter primary key',
  tenant_id varchar(255) NOT NULL COMMENT 'tenant ID',
  config_value varchar(500) DEFAULT '' COMMENT 'parameter value',
  update_by varchar(64) DEFAULT '' COMMENT 'updater',
  update_time datetime DEFAULT NULL COMMENT 'update time',
  PRIMARY KEY (config_id,tenant_id)
) COMMENT='tenant parameter configuration table';

-- ----------------------------
-- Add 'Tenant Administrator or Not' Field in User Table
-- ----------------------------
ALTER TABLE sys_user ADD COLUMN tenant_admin char(1) DEFAULT 'N' COMMENT 'Tenant Administrator or not(Y yes, N no)';




-- ----------------------------
-- create table partition stored procedure(partition by day/month)
-- par_name_format:'%Y%m%d' or '%Y%m'
-- partition by month, pass last day of the month to day_value
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
-- delete table partition stored procedure, for delete expire partition(partition by day/month)
-- par_name_format:'%Y%m%d' or '%Y%m'
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

-- Add 'Tenant could Be Maintained or Not' Control Field in System Parameter 
ALTER TABLE sys_config ADD COLUMN tenant_maintain char(1) NOT NULL DEFAULT 'N' COMMENT 'whether to admit tenant maintenance(Y: yes, N: no)';

CREATE TABLE base_person_info (
  id varchar(48) NOT NULL COMMENT ' primary key',
  unique_id varchar(48) NOT NULL COMMENT 'personnel id',
  name varchar(64) DEFAULT NULL COMMENT 'name',
  sex char(1) DEFAULT NULL COMMENT 'sex：0-male 1-female 2-unknown',
  phone varchar(48) DEFAULT NULL COMMENT 'phone number',
  card_no varchar(255) DEFAULT NULL COMMENT 'card number',
  account varchar(255) DEFAULT NULL COMMENT 'account',
  email varchar(255) DEFAULT NULL COMMENT 'email address',
  dept_id bigint(20) DEFAULT NULL COMMENT 'department ID',
  datasource varchar(48) NOT NULL DEFAULT 'INTERFACE' COMMENT 'data source(data dictionary)',
  flag char(1) DEFAULT '1' COMMENT 'personnel flag: 1-normal 2-red list 3-black list',
  status char(1) NOT NULL DEFAULT '0' COMMENT 'status: 0-valid  1: invalid',
  remark varchar(255) DEFAULT NULL COMMENT 'remark',
  create_by varchar(64) DEFAULT NULL COMMENT 'creator',
  update_by varchar(64) DEFAULT NULL COMMENT 'updater',
  create_time datetime DEFAULT NULL COMMENT 'create time',
  update_time datetime DEFAULT NULL COMMENT 'update time',
  batch_date datetime DEFAULT NULL COMMENT 'cron job execution time',
  tenant_id varchar(255) DEFAULT 'super' COMMENT 'tenant ID',
  PRIMARY KEY (id) USING BTREE,
  UNIQUE KEY uniqueIdIndex (unique_id,tenant_id)
) COMMENT='basic data_personnel basic information';

CREATE TABLE base_person_face (
  id varchar(48) NOT NULL COMMENT ' primary key',
  person_id varchar(48) NOT NULL COMMENT 'related personnel primary key',
  unique_id varchar(48) NOT NULL COMMENT 'personnel id',
  feature varchar(4000) NOT NULL COMMENT 'face feature',
  feature_md5 varchar(255) DEFAULT NULL COMMENT 'face feature MD5',
  quality_score double DEFAULT NULL COMMENT 'image quality score',
  image_url varchar(255) NOT NULL COMMENT 'face image path',
  vendor_code varchar(48) DEFAULT NULL COMMENT 'vendor',
  algs_version varchar(48) DEFAULT NULL COMMENT 'algorithm version',
  encrypted char(1) DEFAULT '0' COMMENT 'encrypt or not: 1-encrypt, 0-not encrypt',
  datasource varchar(48) NOT NULL DEFAULT 'INTERFACE' COMMENT 'data source(data dictionary)',
  remark varchar(255) DEFAULT NULL COMMENT 'remark',
  status char(1) NOT NULL DEFAULT '0' COMMENT 'status: 0-valid, 1: invalid',
  create_by varchar(64) DEFAULT NULL COMMENT 'creator',
  update_by varchar(64) DEFAULT NULL COMMENT 'updater',
  create_time datetime DEFAULT NULL COMMENT 'create time',
  update_time datetime DEFAULT NULL COMMENT 'update time',
  batch_date datetime DEFAULT NULL COMMENT 'cron job execution time',
  tenant_id varchar(255) DEFAULT 'super' COMMENT 'tenant ID',
  PRIMARY KEY (id) USING BTREE,
  KEY personIdIndex (person_id)
) COMMENT='basic data_face image information';

CREATE TABLE base_person_finger (
  id varchar(48) NOT NULL COMMENT ' primary key',
  person_id varchar(48) NOT NULL COMMENT 'related personnel primary key',
  unique_id varchar(48) NOT NULL COMMENT 'personnel id',
  finger_no varchar(2) NOT NULL COMMENT 'finger ID',
  coercive_position varchar(2) NOT NULL DEFAULT '0' COMMENT 'coercive position',
  feature varchar(4000) NOT NULL COMMENT 'fingerprint feature',
  feature_md5 varchar(255) DEFAULT NULL COMMENT 'fingerprint feature MD5',
  quality_score double DEFAULT NULL COMMENT 'image quality score',
  image_url varchar(255) DEFAULT NULL COMMENT ' fingerprint image path',
  vendor_code varchar(48) DEFAULT NULL COMMENT 'vendor',
  algs_version varchar(48) DEFAULT NULL COMMENT 'algorithm version',
  encrypted char(1) DEFAULT '0' COMMENT 'encrypt or not: 1-encrypt 0-not encrypt',
  datasource varchar(48) NOT NULL DEFAULT 'INTERFACE' COMMENT ' data source(data dictionary)',
  remark varchar(255) DEFAULT NULL COMMENT 'remark',
  status char(1) NOT NULL DEFAULT '0' COMMENT 'status: 0-valid 1:invalid',
  create_by varchar(64) DEFAULT NULL COMMENT 'creator',
  update_by varchar(64) DEFAULT NULL COMMENT 'updator',
  create_time datetime DEFAULT NULL COMMENT 'create time',
  update_time datetime DEFAULT NULL COMMENT 'update time',
  batch_date datetime DEFAULT NULL COMMENT 'cron job execution time',
  tenant_id varchar(255) DEFAULT 'super' COMMENT 'tenant ID',
  PRIMARY KEY (id) USING BTREE,
  KEY personIdIndex (person_id)
) COMMENT='basic data_ fingerprint image information';

CREATE TABLE base_person_iris (
  id varchar(48) NOT NULL COMMENT 'primary key',
  person_id varchar(48) NOT NULL COMMENT 'related personnel primary key',
  unique_id varchar(48) NOT NULL COMMENT 'personnel id',
  feature longtext NOT NULL COMMENT 'iris feature',
  feature_md5 varchar(255) DEFAULT NULL COMMENT 'iris feature MD5',
  quality_score double DEFAULT NULL COMMENT ' image quality score',
  image_url varchar(255) NOT NULL COMMENT 'iris image path',
  vendor_code varchar(48) DEFAULT NULL COMMENT 'vendor',
  algs_version varchar(48) DEFAULT NULL COMMENT 'algorithm version',
  encrypted char(1) DEFAULT '0' COMMENT 'encrypt or not: 1-encrypt 0-not encrypt',
  datasource varchar(48) NOT NULL DEFAULT 'INTERFACE' COMMENT ' data source(data dictionary)',
  remark varchar(255) DEFAULT NULL COMMENT 'remark',
  status char(1) NOT NULL DEFAULT '0' COMMENT 'status：0-valid  1:invalid',
  create_by varchar(64) DEFAULT NULL COMMENT 'creator',
  update_by varchar(64) DEFAULT NULL COMMENT 'updator',
  create_time datetime DEFAULT NULL COMMENT 'create time',
  update_time datetime DEFAULT NULL COMMENT 'update time',
  batch_date datetime DEFAULT NULL COMMENT 'cron job execution time',
  tenant_id varchar(255) DEFAULT 'super' COMMENT 'tenant ID',
  PRIMARY KEY (id) USING BTREE,
  KEY personIdIndex (person_id)
) COMMENT='basic data_iris image information';


CREATE TABLE base_person_iris_face (
  id varchar(48) NOT NULL COMMENT ' primary key',
  person_id varchar(48) NOT NULL COMMENT 'related personnel primary key',
  unique_id varchar(48) NOT NULL COMMENT 'personnel id',
  fusion_feature longtext COMMENT 'fusion feature',
  face_feature longtext COMMENT 'face feature',
  iris_feature longtext COMMENT 'iris feature',
  fusion_feature_md5 varchar(255) DEFAULT NULL COMMENT 'fusion feature md5',
  face_feature_md5 varchar(255) DEFAULT NULL COMMENT 'face feature md5',
  face_image_url varchar(255) DEFAULT NULL COMMENT 'face image path',
  face_quality double DEFAULT NULL COMMENT 'face quality score',
  iris_feature_md5 varchar(255) DEFAULT NULL COMMENT 'iris feature md5',
  iris_image_url varchar(255) DEFAULT NULL COMMENT 'iris image path',
  iris_quality double DEFAULT NULL COMMENT 'iris quality score',
  encrypted char(1) DEFAULT '0' COMMENT 'encrypt or not： 1-encrypt 0-not encrypt',
  status char(1) NOT NULL DEFAULT '0' COMMENT 'status：0-valid  1-invalid',
  datasource varchar(50) DEFAULT 'INTERFACE' COMMENT ' data source(data dictionary)',
  data_describe varchar(255) DEFAULT NULL COMMENT ' data description',
  validity_date datetime DEFAULT NULL COMMENT 'valid date',
  create_by varchar(48) DEFAULT NULL COMMENT 'creator',
  update_by varchar(48) DEFAULT NULL COMMENT 'updator',
  create_time datetime DEFAULT NULL COMMENT 'create time',
  update_time datetime DEFAULT NULL COMMENT 'update time',
  remark varchar(255) DEFAULT NULL COMMENT 'remark',
  tenant_id varchar(255) DEFAULT 'super' COMMENT 'tenant ID',
  PRIMARY KEY (id),
  KEY PERSON_UNIQUEID_INDEX (unique_id)
) COMMENT='iris and face multi-modal image feature table';


CREATE TABLE base_person_cert (
  id varchar(48) NOT NULL COMMENT ' primary key',
  unique_id varchar(48) NOT NULL COMMENT 'personnel id',
  cert_type varchar(3) NOT NULL COMMENT 'certificate type(dictionary national standard)',
  cert_num varchar(255) NOT NULL COMMENT 'certificate number',
  cert_name varchar(64) DEFAULT NULL COMMENT 'certificate name',
  cert_validity varchar(48) DEFAULT NULL COMMENT 'certificate valid date',
  gender char(1) DEFAULT NULL COMMENT 'sex: 0-male 1-female 2-unknown',
  bth_date varchar(48) DEFAULT NULL COMMENT 'birth date',
  nation varchar(3) DEFAULT NULL COMMENT 'nation',
  address varchar(255) DEFAULT NULL COMMENT 'address',
  cert_authority varchar(255) DEFAULT NULL COMMENT 'certificate issuing authority',
  cert_img varchar(255) DEFAULT NULL COMMENT 'certificate image',
  encrypted char(1) DEFAULT '0' COMMENT 'encrypt or not： 1-encrypt 0-not encrypt',
  enterschool_img varchar(255) DEFAULT NULL COMMENT 'enter school image',
  inschool_img varchar(255) DEFAULT NULL COMMENT 'in school image',
  graduate_img varchar(255) DEFAULT NULL COMMENT 'graduation image',
  remark varchar(255) DEFAULT NULL COMMENT 'remark',
  create_by varchar(64) DEFAULT NULL COMMENT 'creator',
  update_by varchar(64) DEFAULT NULL COMMENT 'updator',
  create_time datetime DEFAULT NULL COMMENT 'create time',
  update_time datetime DEFAULT NULL COMMENT 'update time',
  batch_date datetime DEFAULT NULL COMMENT 'cron job execution time',
  tenant_id varchar(255) DEFAULT 'super' COMMENT 'tenant ID',
  PRIMARY KEY (id) USING BTREE
) COMMENT='basic data_personnel certificate information';


CREATE TABLE channel_business  (
  id varchar(48)  NOT NULL COMMENT ' primary key',
  channel_id varchar(48)  NOT NULL COMMENT 'channel primary key',
  person_id varchar(48)  NOT NULL COMMENT 'personnel primary key',
  unique_id varchar(48)  NOT NULL COMMENT 'personnel unique id',
  busi_code_first varchar(255)  NULL DEFAULT NULL COMMENT 'business code 1',
  busi_code_second varchar(255)  NULL DEFAULT NULL COMMENT 'business code2',
  busi_code_third varchar(255)  NULL DEFAULT NULL COMMENT 'business code3',
  face_mode varchar(1)  NOT NULL DEFAULT '1' COMMENT 'open face(data dictionary:0-not open  1-open )',
  finger_mode varchar(1)  NOT NULL DEFAULT '1' COMMENT 'open fingerprint(data dictionary:0-not open  1-open )',
  iris_mode varchar(1)  NOT NULL DEFAULT '1' COMMENT 'open iris( data dictionary:0-not open   1-open )',
  fvein_mode varchar(1)  NOT NULL DEFAULT '1' COMMENT 'open fingervein(data dictionary:0-not open   1-open )',
  face_iris_mode varchar(1)  NOT NULL DEFAULT '1' COMMENT 'open face and iris multi-modal(data dictionary:0-not open  1-open )',
  datasource varchar(48)  NOT NULL DEFAULT 'INTERFACE' COMMENT 'data source(data dictionary)',
  locked varchar(1)  NULL DEFAULT 'N' COMMENT 'lock or not(data dictionary: N-No Y-Yes)',
  lock_time datetime NULL DEFAULT NULL COMMENT 'lock time',
  status char(1)  NOT NULL DEFAULT '0' COMMENT 'status( data dictionary:0-valid  1:invalid)',
  remark varchar(255)  NULL DEFAULT NULL COMMENT 'remark',
  create_by varchar(64)  NULL DEFAULT NULL COMMENT 'creator',
  update_by varchar(64)  NULL DEFAULT NULL COMMENT 'updator',
  create_time datetime NULL DEFAULT NULL COMMENT 'create time',
  update_time datetime NULL DEFAULT NULL COMMENT 'update time',
  batch_date datetime NULL DEFAULT NULL COMMENT 'cron job execution time',
  tenant_id varchar(255)  NULL DEFAULT 'super' COMMENT 'tenant ID',
  update_seria_num bigint(20) NULL DEFAULT NULL COMMENT 'business update serial number',
  PRIMARY KEY (id) USING BTREE,
  UNIQUE INDEX channelIdPersonIdUnique(channel_id, person_id) USING BTREE
) COMMENT = 'channel management_channel business table';


CREATE TABLE channel_info  (
  id varchar(48)  NOT NULL COMMENT ' primary key',
  channel_code varchar(255)  NOT NULL COMMENT 'channel code',
  channel_name varchar(255)  NOT NULL COMMENT 'channel name',
  face_mode varchar(1)  NOT NULL DEFAULT '1' COMMENT 'open face(data dictionary:0-not open   1-open )',
  finger_mode varchar(1)  NOT NULL DEFAULT '1' COMMENT 'open fingerprint(data dictionary:0-not open   1-open )',
  iris_mode varchar(1)  NOT NULL DEFAULT '1' COMMENT 'open iris(data dictionary:0-not open   1-open )',
  fvein_mode varchar(1)  NOT NULL DEFAULT '1' COMMENT 'open fingervein(data dictionary:0-not open   1-open )',
  face_iris_mode varchar(1)  NOT NULL DEFAULT '1' COMMENT 'open face and iris multi-modal(data dictionary:0-not open   1-open )',
  enable_multi_faces varchar(1)  NULL DEFAULT 'N' COMMENT 'enable multi face or not(data dictionary:N-no Y-yes)',
  search_n varchar(1)  NULL DEFAULT '2' COMMENT '1-N identification mode(data dictionary:1-check sub-database, 2-check channel database、3-check the whole database, 4-check one by one)',
  device_num_limit int(11) NULL DEFAULT 0 COMMENT 'mount device maximum number',
  remark varchar(255)  NULL DEFAULT NULL COMMENT 'remark',
  create_by varchar(64)  NULL DEFAULT NULL COMMENT 'creator',
  update_by varchar(64)  NULL DEFAULT NULL COMMENT 'updator',
  create_time datetime NULL DEFAULT NULL COMMENT 'create time',
  update_time datetime NULL DEFAULT NULL COMMENT 'update time',
  batch_date datetime NULL DEFAULT NULL COMMENT 'cron job execution time',
  tenant_id varchar(255)  NULL DEFAULT 'super' COMMENT 'tenant ID',
  PRIMARY KEY (id) USING BTREE
) COMMENT = 'channel management_channel information table';


CREATE TABLE channel_param  (
  id varchar(48)  NOT NULL COMMENT 'primary key',
  channel_id varchar(48)  NOT NULL COMMENT 'channel primary key',
  param_code varchar(255)  NOT NULL COMMENT 'parameter code',
  param_name varchar(255)  NULL DEFAULT NULL COMMENT 'parameter name',
  param_value varchar(255)  NOT NULL COMMENT 'parameter key value',
  bio_attest_type varchar(1)  NOT NULL COMMENT 'identification type(data dictionary:0-face  1-fingerprint 2-iris 3-fingervein)',
  remark varchar(255)  NULL DEFAULT NULL COMMENT 'remark',
  create_by varchar(64)  NULL DEFAULT NULL COMMENT 'creator',
  update_by varchar(64)  NULL DEFAULT NULL COMMENT 'updator',
  create_time datetime NULL DEFAULT NULL COMMENT 'create time',
  update_time datetime NULL DEFAULT NULL COMMENT 'update time',
  batch_date datetime NULL DEFAULT NULL COMMENT 'cron job execution time',
  tenant_id varchar(255)  NULL DEFAULT 'super' COMMENT 'tenant ID',
  PRIMARY KEY (id) USING BTREE
) COMMENT = 'channel management_channel parameter table';


CREATE TABLE channel_subtreasury_busi  (
  id varchar(48)  NOT NULL COMMENT ' primary key',
  channel_id varchar(48)  NOT NULL COMMENT 'channel primary key',
  sub_treasury_id varchar(48)  NOT NULL COMMENT 'sub database primary key',
  person_id varchar(48)  NOT NULL COMMENT 'personnel primary key',
  unique_id varchar(48)  NOT NULL COMMENT 'personnel unique id',
  datasource varchar(48)  NOT NULL DEFAULT 'INTERFACE' COMMENT 'data source(data dictionary)',
  status char(1)  NOT NULL DEFAULT '0' COMMENT 'status(data dictionary:0-valid  1-invalid)',
  remark varchar(255)  NULL DEFAULT NULL COMMENT 'remark',
  create_by varchar(64)  NULL DEFAULT NULL COMMENT 'creator',
  update_by varchar(64)  NULL DEFAULT NULL COMMENT 'updator',
  create_time datetime NULL DEFAULT NULL COMMENT 'create time',
  update_time datetime NULL DEFAULT NULL COMMENT 'update time',
  batch_date datetime NULL DEFAULT NULL COMMENT 'cron job execution time',
  tenant_id varchar(255)  NULL DEFAULT 'super' COMMENT 'tenantID',
  update_seria_num bigint(20) NULL DEFAULT NULL COMMENT 'business update serial number',
  PRIMARY KEY (id) USING BTREE,
  UNIQUE INDEX cIdSubCodePIdUnique(channel_id, sub_treasury_id, person_id) USING BTREE
) COMMENT = 'channel management_sub database business table';


CREATE TABLE channel_subtreasury_info  (
  id varchar(48)  NOT NULL COMMENT ' primary key',
  channel_id varchar(48)  NOT NULL COMMENT 'channel primary key',
  sub_treasury_code varchar(100)  NOT NULL COMMENT 'sub database(channel code_sub database number)',
  sub_treasury_name varchar(100)  NOT NULL COMMENT 'sub database name',
  remark varchar(255)  NULL DEFAULT NULL COMMENT 'remark',
  create_by varchar(64)  NULL DEFAULT NULL COMMENT 'creator',
  update_by varchar(64)  NULL DEFAULT NULL COMMENT 'updator',
  create_time datetime NULL DEFAULT NULL COMMENT 'create time',
  update_time datetime NULL DEFAULT NULL COMMENT 'update time',
  batch_date datetime NULL DEFAULT NULL COMMENT 'cron job execution time',
  tenant_id varchar(255)  NULL DEFAULT 'super' COMMENT 'tenant ID',
  PRIMARY KEY (id) USING BTREE
) COMMENT = 'sub database information table';


CREATE TABLE sys_tenant_interface (
  tenant_id varchar(255) NOT NULL COMMENT 'tenant ID',
  transcode varchar(255) NOT NULL COMMENT 'interface transaction code',
  expire_time datetime NOT NULL COMMENT 'expire time',
  create_time datetime DEFAULT NULL COMMENT 'create time',
  update_time datetime DEFAULT NULL COMMENT 'update time',
  PRIMARY KEY (tenant_id,transcode)
) COMMENT='tenant and interface relation table';


CREATE TABLE area_model (
  id bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'area id',
  parent_id bigint(20) DEFAULT '0' COMMENT 'parent area id',
  ancestors varchar(50) DEFAULT '' COMMENT 'ancestor list',
  area_name varchar(30) DEFAULT '' COMMENT 'area name',
  order_num int(4) DEFAULT '0' COMMENT 'display order',
  area_type varchar(30) DEFAULT '' COMMENT 'area type dictionary',
  status char(1) DEFAULT '0' COMMENT 'area status(0 normal, 1 deactive)',
  create_by varchar(64) DEFAULT '' COMMENT 'creator',
  create_time datetime DEFAULT NULL COMMENT 'create time',
  update_by varchar(64) DEFAULT '' COMMENT 'updater',
  update_time datetime DEFAULT NULL COMMENT 'update time',
  remark varchar(500) DEFAULT '' COMMENT 'remark',
  tenant_id varchar(255) DEFAULT 'super' COMMENT 'tenant ID',
  PRIMARY KEY (id) USING BTREE
) COMMENT='area table';


CREATE TABLE device_model (
  id varchar(48) NOT NULL COMMENT 'primary key',
  model_code varchar(100) NOT NULL COMMENT 'device model code',
  model_name varchar(255) NOT NULL COMMENT 'device model name',
  exterior_image varchar(255) DEFAULT NULL COMMENT 'device appearance image',
  model_desc varchar(2000) DEFAULT NULL COMMENT 'device model description',
  create_by varchar(64) DEFAULT NULL COMMENT 'creator',
  update_by varchar(64) DEFAULT NULL COMMENT 'updator',
  create_time datetime DEFAULT NULL COMMENT 'create time',
  update_time datetime DEFAULT NULL COMMENT 'update time',
  tenant_id varchar(255) DEFAULT 'super' COMMENT 'tenant ID',
  PRIMARY KEY (id),
  UNIQUE KEY model_code_index (model_code,tenant_id)
) COMMENT='device model information';


CREATE TABLE device_param_info (
  id varchar(48) NOT NULL COMMENT 'primary key',
  param_code varchar(100) NOT NULL COMMENT 'device parameter code',
  param_name varchar(255) NOT NULL COMMENT 'device parameter name',
  param_desc varchar(2000) DEFAULT NULL COMMENT 'device parameter description',
  create_by varchar(64) DEFAULT NULL COMMENT 'creator',
  update_by varchar(64) DEFAULT NULL COMMENT 'updator',
  create_time datetime DEFAULT NULL COMMENT 'create time',
  update_time datetime DEFAULT NULL COMMENT 'update time',
  tenant_id varchar(255) DEFAULT 'super' COMMENT 'tenant ID',
  PRIMARY KEY (id),
  UNIQUE KEY param_code_index (param_code,tenant_id)
) COMMENT='device parameter information';


CREATE TABLE device_param_model_rel (
  id varchar(48) NOT NULL COMMENT 'primary key',
  model_code varchar(255) NOT NULL COMMENT 'device model code',
  param_code varchar(255) NOT NULL COMMENT 'device parameter code',
  create_by varchar(64) DEFAULT NULL COMMENT 'creator',
  update_by varchar(64) DEFAULT NULL COMMENT 'updator',
  create_time datetime DEFAULT NULL COMMENT 'create time',
  update_time datetime DEFAULT NULL COMMENT 'update time',
  tenant_id varchar(255) DEFAULT 'super' COMMENT 'tenant ID',
  PRIMARY KEY (id)
) COMMENT='parameter and model relationship';

CREATE TABLE device_info (
  id varchar(48) NOT NULL COMMENT ' primary key',
  device_no varchar(48) NOT NULL COMMENT 'device number',
  device_name varchar(255) DEFAULT NULL COMMENT 'device name',
  device_addr varchar(255) DEFAULT NULL COMMENT 'install location',
  device_model_code varchar(48) DEFAULT NULL COMMENT 'model code',
  device_ip varchar(48) DEFAULT NULL COMMENT 'device IP',
  device_mac varchar(48) DEFAULT NULL COMMENT 'device Mac',
  longitude double DEFAULT NULL COMMENT 'device longitude',
  latitude double DEFAULT NULL COMMENT 'device latitude',
  channel_code varchar(255) DEFAULT NULL COMMENT 'channel code',
  area_id bigint(20) DEFAULT NULL COMMENT 'area primary key',
  subtreasury_code varchar(255) DEFAULT NULL COMMENT 'sub database code(separated by ,)',
  import_batch_num varchar(48) DEFAULT NULL COMMENT 'import batch number',
  create_method char(1) NOT NULL DEFAULT '1' COMMENT 'create mode(1: add in backend, 2: Autonomously register)',
  pull_all_flag char(1) DEFAULT '1' COMMENT 'whether to full pull(1: yes, 0: no)',
  primary_sub_code varchar(48) DEFAULT NULL COMMENT 'primary and sub database code',
  duplicate_time int(11) DEFAULT NULL COMMENT 'comparison and deduplication time(ms) in backend',
  device_type char(1) NOT NULL DEFAULT '1' COMMENT 'device type(dictionary 1: normal, 2..)',
  mqtt_pwd varchar(255) NOT NULL COMMENT 'MQTT connection password',
  mqtt_salt varchar(255) NOT NULL COMMENT 'MQTT password salt',
  ext_info varchar(4000) DEFAULT NULL COMMENT 'extension information',
  create_by varchar(64) DEFAULT NULL COMMENT 'creator',
  update_by varchar(64) DEFAULT NULL COMMENT 'updator',
  create_time datetime DEFAULT NULL COMMENT 'create time',
  update_time datetime DEFAULT NULL COMMENT 'update time',
  batch_date datetime DEFAULT NULL COMMENT 'cron job execution time',
  tenant_id varchar(255) DEFAULT 'super' COMMENT 'tenant ID',
  PRIMARY KEY (id),
  UNIQUE KEY deviceno_unique_index (device_no) USING BTREE COMMENT 'device code unique index'
) COMMENT='device information table';


CREATE TABLE device_batch_log (
  id varchar(48) NOT NULL COMMENT ' primary key',
  batch_num varchar(50) NOT NULL COMMENT 'batch number',
  batch_desc varchar(255) NOT NULL COMMENT 'batch description',
  rollbacked char(1) NOT NULL DEFAULT '0' COMMENT 'rollback or not(1: yes, 0: no)',
  rollback_time datetime DEFAULT NULL COMMENT 'rollback time',
  create_time datetime DEFAULT NULL COMMENT 'create time',
  create_by varchar(255) DEFAULT NULL COMMENT 'creator',
  update_time datetime DEFAULT NULL COMMENT 'update time',
  update_by varchar(255) DEFAULT NULL COMMENT 'updator',
  tenant_id varchar(255) DEFAULT 'super' COMMENT 'import tenant',
  PRIMARY KEY (id),
  UNIQUE KEY uk_batch_num (batch_num) USING BTREE COMMENT 'batch number unique index'
) COMMENT='device import batch log';

CREATE TABLE device_upgrade_version (
  id varchar(48) NOT NULL COMMENT 'primary key',
  app_name varchar(48) NOT NULL COMMENT 'APP name',
  version varchar(48) NOT NULL COMMENT 'version number',
  description varchar(255) DEFAULT NULL COMMENT 'version description',
  path varchar(255) NOT NULL COMMENT 'upgrade file',
  file_size bigint(20) NOT NULL COMMENT 'version size(B)',
  filename varchar(255) NOT NULL COMMENT 'source file name',
  md5 varchar(255) NOT NULL COMMENT 'file MD5',
  enabled char(1) DEFAULT '0' COMMENT 'enable or not(1: enable, 0: deactive)',
  create_by varchar(64) DEFAULT NULL COMMENT 'creator',
  update_by varchar(64) DEFAULT NULL COMMENT 'updator',
  create_time datetime DEFAULT NULL COMMENT 'create time',
  update_time datetime DEFAULT NULL COMMENT 'update time',
  batch_date datetime DEFAULT NULL COMMENT 'cron job execution time',
  tenant_id varchar(255) DEFAULT 'super' COMMENT 'tenant ID',
  PRIMARY KEY (id)
) COMMENT='version information table';

CREATE TABLE device_upgrade_task (
  id varchar(48) NOT NULL COMMENT 'primary key',
  version_id varchar(48) NOT NULL COMMENT 'version primary key',
  device_id varchar(48) NOT NULL COMMENT 'device primary key',
  upgrade_time datetime DEFAULT NULL COMMENT 'update time(update immediately if the value is null)',
  upgrade_count_limit int(11) NOT NULL COMMENT 'maximum upgrade times',
  execute_count int(11) NOT NULL DEFAULT '0' COMMENT 'execution times',
  pub_count int(11) DEFAULT '0' COMMENT 'publish times',
  task_index int(11) NOT NULL COMMENT 'job sort',
  execute_result varchar(1) NOT NULL DEFAULT '1' COMMENT 'execution result(1: to be executed, 2: success, 3: failure, 4: skip)',
  rollback_install char(1) NOT NULL DEFAULT '0' COMMENT 'degraded installation or not(1: yes, 0: no)',
  create_by varchar(64) DEFAULT NULL COMMENT 'creator',
  update_by varchar(64) DEFAULT NULL COMMENT 'updator',
  create_time datetime DEFAULT NULL COMMENT 'create time',
  update_time datetime DEFAULT NULL COMMENT 'update time',
  batch_date datetime DEFAULT NULL COMMENT 'cron job execution time',
  tenant_id varchar(255) DEFAULT 'super' COMMENT 'tenant ID',
  PRIMARY KEY (id)
) COMMENT='upgrade job table';


CREATE TABLE device_upgrade_log (
  id varchar(48) NOT NULL COMMENT 'primary key',
  task_id varchar(48) NOT NULL COMMENT 'job primary key',
  device_no varchar(48) NOT NULL COMMENT 'device number',
  device_name varchar(255) DEFAULT NULL COMMENT 'device name',
  before_app_name varchar(48) DEFAULT NULL COMMENT 'APP name before upgrade',
  after_app_name varchar(48) NOT NULL COMMENT 'APP name after upgrade',
  before_version varchar(48) DEFAULT NULL COMMENT 'version before upgrade',
  after_version varchar(48) NOT NULL COMMENT 'version after upgrade',
  upgrade_status char(1) NOT NULL COMMENT 'update status(1: to be downloaded, 2: to be updated, 3: update successfully, 4: failed to update, 5: skip)',
  client_time datetime DEFAULT NULL COMMENT 'client side time during upgrade',
  server_time datetime DEFAULT NULL COMMENT 'server side time during upgrade',
  time_used int(11) DEFAULT NULL COMMENT 'time used of upgrade(unit s)',
  fail_reason varchar(500) DEFAULT NULL COMMENT 'failure reason',
  create_time datetime DEFAULT NULL COMMENT 'create time',
  update_time datetime DEFAULT NULL COMMENT 'create time',
  batch_date datetime DEFAULT NULL COMMENT 'cron job execution time',
  tenant_id varchar(255) DEFAULT 'super' COMMENT 'tenant ID',
  PRIMARY KEY (id)
) COMMENT='upgrade log table';


CREATE TABLE device_param_distribute_log (
  id varchar(48) NOT NULL COMMENT 'primary key',
  device_no varchar(48) NOT NULL COMMENT 'device number',
  param_code varchar(255) NOT NULL COMMENT 'parameter code',
  param_value varchar(255) NOT NULL COMMENT 'parameter value',
  distribute_result char(1) DEFAULT NULL COMMENT 'distribute result(0: success, 1: failure)',
  sort_index bigint(20) NOT NULL COMMENT 'distribute sort',
  create_time datetime DEFAULT NULL COMMENT 'create time',
  update_time datetime DEFAULT NULL COMMENT 'update time',
  tenant_id varchar(255) DEFAULT 'super' COMMENT 'tenant ID',
  PRIMARY KEY (id)
) COMMENT='parameter distribution log';


CREATE TABLE device_action_log (
  id varchar(48) NOT NULL COMMENT 'primary key',
  device_name varchar(255) DEFAULT NULL COMMENT 'device name',
  device_no varchar(48) NOT NULL COMMENT 'device number',
  channel_code varchar(48) DEFAULT NULL COMMENT 'device channel code',
  model_code varchar(48) DEFAULT NULL COMMENT 'device model code',
  action_type char(1) NOT NULL COMMENT 'action type(1: register, 2: online, 3: offline, 4: update)',
  create_time datetime DEFAULT NULL COMMENT 'create time',
  update_time datetime DEFAULT NULL COMMENT 'update time',
  tenant_id varchar(255) DEFAULT 'super' COMMENT 'tenant ID',
  PRIMARY KEY (id)
) COMMENT='device action log';

CREATE TABLE device_access_adapter (
  id varchar(48) NOT NULL COMMENT 'primary key',
  device_name varchar(255) DEFAULT NULL COMMENT 'device name',
  device_sn varchar(48) NOT NULL COMMENT 'device serial number',
  seria_num varchar(100) NOT NULL COMMENT 'sync serial number',
  create_by varchar(64) DEFAULT NULL COMMENT 'creator',
  update_by varchar(64) DEFAULT NULL COMMENT 'updater',
  create_time datetime DEFAULT NULL COMMENT 'create time',
  update_time datetime DEFAULT NULL COMMENT 'update time',
  tenant_id varchar(255) DEFAULT 'super' COMMENT 'tenant id',
  PRIMARY KEY (id),
  UNIQUE KEY idx_sn (device_sn)
) COMMENT='203 device integration';

CREATE TABLE sys_sdk_file (
  id varchar(48) NOT NULL COMMENT 'primary key',
  file_name varchar(255) NOT NULL COMMENT 'upload file name',
  file_path varchar(255) NOT NULL COMMENT 'file save path',
  md5 varchar(255) NOT NULL COMMENT 'file MD5',
  sdk_type varchar(20) NOT NULL COMMENT 'SDK type',
  create_by varchar(64) DEFAULT NULL COMMENT 'creator',
  update_by varchar(64) DEFAULT NULL COMMENT 'updator',
  create_time datetime DEFAULT NULL COMMENT 'create time',
  update_time datetime DEFAULT NULL COMMENT 'update time',
  tenant_id varchar(255) NOT NULL DEFAULT 'super' COMMENT 'tenant ID',
  sorted_no int(20) NOT NULL COMMENT 'version sort',
  PRIMARY KEY (id)
) COMMENT='SDK file upload table';

CREATE TABLE app_info (
  id varchar(48) NOT NULL COMMENT 'primary key',
  app_key varchar(255) NOT NULL COMMENT 'app system key value',
  app_secret varchar(255) NOT NULL COMMENT 'app system secret key',
  app_desc varchar(255) DEFAULT NULL COMMENT 'app system description',
  built_in varchar(1) NOT NULL DEFAULT 'N' COMMENT 'built-in or not(Y: yes, N: no)',
  remark varchar(255) DEFAULT NULL COMMENT 'remark',
  status char(1) NOT NULL DEFAULT '0' COMMENT 'status(data dictionary: 0-valid  1:invalid)',
  create_by varchar(64) DEFAULT NULL COMMENT 'creator',
  update_by varchar(64) DEFAULT NULL COMMENT 'updator',
  create_time datetime DEFAULT NULL COMMENT 'create time',
  update_time datetime DEFAULT NULL COMMENT 'update time',
  tenant_id varchar(255) DEFAULT 'super' COMMENT 'tenant ID',
  PRIMARY KEY (id)
) COMMENT='app system information table';


CREATE TABLE app_interface_auth (
  id varchar(48) NOT NULL COMMENT 'primary key',
  app_id varchar(48) NOT NULL COMMENT 'app system primary key',
  trans_code varchar(255) NOT NULL COMMENT 'interface transaction code(data dictionary)',
  trans_end_time datetime DEFAULT NULL COMMENT 'transaction valid due time',
  remark varchar(255) DEFAULT NULL COMMENT 'remark',
  create_by varchar(64) DEFAULT NULL COMMENT 'creator',
  update_by varchar(64) DEFAULT NULL COMMENT 'updator',
  create_time datetime DEFAULT NULL COMMENT 'create time',
  update_time datetime DEFAULT NULL COMMENT 'update time',
  tenant_id varchar(255) DEFAULT 'super' COMMENT 'tenant ID',
  PRIMARY KEY (id)
) COMMENT='app system interface authorization table';


CREATE TABLE msg_ding_application (
  id varchar(48) NOT NULL COMMENT 'primary key',
  team_id varchar(255) NOT NULL COMMENT 'team(enterprise) primary key',
  corp_id varchar(255) NOT NULL COMMENT 'team(enterprise) CorpId',
  app_name varchar(255) NOT NULL COMMENT 'micro app name',
  agent_id varchar(255) NOT NULL COMMENT 'micro app agentId',
  app_key varchar(255) NOT NULL COMMENT 'micro app AppKey',
  app_secrect varchar(255) NOT NULL COMMENT 'micro app AppSecrect',
  short_des varchar(255) DEFAULT NULL COMMENT 'micro app short description',
  create_time datetime DEFAULT NULL COMMENT 'create time',
  update_time datetime DEFAULT NULL COMMENT 'update time',
  tenant_id varchar(255) DEFAULT 'super' COMMENT 'tenant ID',
  PRIMARY KEY (id)
) COMMENT='Dingding micro app';


CREATE TABLE msg_ding_team (
  id varchar(48) NOT NULL COMMENT 'primary key',
  corp_id varchar(255) NOT NULL COMMENT 'team(enterprise) CorpId',
  team_name varchar(255) NOT NULL COMMENT 'team(enterprise) name',
  team_des varchar(500) DEFAULT NULL COMMENT 'team(enterprise) description',
  create_time datetime DEFAULT NULL COMMENT 'create time',
  update_time datetime DEFAULT NULL COMMENT 'update time',
  tenant_id varchar(255) DEFAULT 'super' COMMENT 'tenant ID',
  PRIMARY KEY (id)
) COMMENT='Dingding Team(Enterprise)';

CREATE TABLE msg_log (
  id varchar(48) NOT NULL COMMENT 'primary key',
  received_seq varchar(48) NOT NULL COMMENT 'business serial number',
  notice_method varchar(1) NOT NULL COMMENT 'notice mode(1: short message, 2: email, 3: wechat, 4: dingding)',
  msg_subject varchar(255) DEFAULT NULL COMMENT 'message subject',
  msg_content varchar(4000) DEFAULT NULL COMMENT 'message content',
  has_annex varchar(1) NOT NULL DEFAULT 'N' COMMENT 'include attachment or not(Y: yes, N: no)',
  from_user varchar(255) DEFAULT NULL COMMENT 'from flag',
  to_user varchar(4000) DEFAULT NULL COMMENT 'to flag(separated by,)',
  offical_account_id varchar(48) DEFAULT NULL COMMENT 'official(enterprise) account identification',
  offical_account_name varchar(255) DEFAULT NULL COMMENT 'official(enterprise) account name',
  scene_remark varchar(48) DEFAULT NULL COMMENT 'scene remark',
  result_status varchar(1) NOT NULL COMMENT 'send status(0: success, 1: failure)',
  err_msg varchar(255) DEFAULT NULL COMMENT 'error information',
  json_response varchar(4000) DEFAULT NULL COMMENT 'sending result json',
  create_time datetime NOT NULL COMMENT 'create time',
  tenant_id varchar(255) DEFAULT 'super' COMMENT 'tenant ID',
  PRIMARY KEY (id,create_time)
) PARTITION BY RANGE (TO_DAYS(create_time)) (
   PARTITION p0 VALUES LESS THAN (0)
) ;
ALTER TABLE msg_log COMMENT='Message Log';
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
  id varchar(48) NOT NULL COMMENT 'primary key',
  log_id varchar(48) NOT NULL COMMENT 'Message Log ID',
  file_name varchar(255) NOT NULL COMMENT 'source file  name',
  file_path varchar(255) NOT NULL COMMENT 'file path',
  file_md5 varchar(48) DEFAULT NULL COMMENT 'file MD5',
  create_time datetime DEFAULT NULL COMMENT 'create time',
  tenant_id varchar(255) DEFAULT 'super' COMMENT 'tenant ID',
  PRIMARY KEY (id) 
) COMMENT='Message Log attachment';


CREATE TABLE msg_mail_property (
  id varchar(48) NOT NULL COMMENT 'primary key',
  host varchar(255) NOT NULL COMMENT 'SMTP service address',
  port int(11) DEFAULT NULL COMMENT 'SMTP service port',
  username varchar(255) NOT NULL COMMENT 'login username',
  password varchar(255) NOT NULL COMMENT 'login authorization code',
  email_addr varchar(255) NOT NULL COMMENT 'from email address',
  create_time datetime DEFAULT NULL COMMENT 'create time',
  update_time datetime DEFAULT NULL COMMENT 'update time',
  tenant_id varchar(255) DEFAULT 'super' COMMENT 'tenant ID',
  PRIMARY KEY (id)
) COMMENT='email configuration';


CREATE TABLE msg_offical_account (
  id varchar(48) NOT NULL COMMENT 'primary key',
  app_id varchar(500) NOT NULL COMMENT 'AppId',
  app_secrect varchar(500) NOT NULL COMMENT 'AppSecrect',
  app_name varchar(500) NOT NULL COMMENT 'official account(cloud account) name',
  create_time datetime DEFAULT NULL COMMENT 'create time',
  update_time datetime DEFAULT NULL COMMENT 'update time',
  tenant_id varchar(255) DEFAULT 'super' COMMENT 'tenant ID',
  PRIMARY KEY (id)
) COMMENT='wechat official account';

CREATE TABLE msg_offical_account_user (
  id varchar(48) NOT NULL COMMENT 'primary key',
  offical_account_id varchar(48) DEFAULT NULL COMMENT 'official account ID',
  app_id varchar(48) NOT NULL COMMENT 'official account AppId',
  open_id varchar(255) NOT NULL COMMENT 'wechat id',
  wx_name varchar(255) NOT NULL COMMENT 'wechat name',
  head_img_url varchar(500) DEFAULT NULL COMMENT 'wechat avatar',
  phone varchar(30) DEFAULT NULL COMMENT 'bound phone number',
  create_time datetime DEFAULT NULL COMMENT 'create time',
  update_time datetime DEFAULT NULL COMMENT 'update time',
  tenant_id varchar(255) DEFAULT 'super' COMMENT 'tenant ID',
  PRIMARY KEY (id)
) COMMENT='wechat user';

CREATE TABLE msg_sms_cloud_account (
  id varchar(48) NOT NULL COMMENT 'primary key',
  app_id varchar(500) NOT NULL COMMENT 'AppId',
  app_secrect varchar(500) NOT NULL COMMENT 'AppSecrect',
  app_name varchar(500) NOT NULL COMMENT 'account description',
  create_time datetime DEFAULT NULL COMMENT 'create time',
  update_time datetime DEFAULT NULL COMMENT 'update time',
  tenant_id varchar(255) DEFAULT 'super' COMMENT 'tenant ID',
  PRIMARY KEY (id)
) COMMENT='short message cloud account';


CREATE TABLE msg_template (
  id varchar(48) NOT NULL COMMENT 'primary key',
  template_name varchar(255) NOT NULL COMMENT 'template name',
  offical_id varchar(48) DEFAULT NULL COMMENT 'template official ID',
  content varchar(1000) NOT NULL COMMENT 'template content',
  notice_method varchar(1) NOT NULL COMMENT 'notice mode(1: short message, 2: Email，3：wechat，4：dingding)',
  offical_account_id varchar(48) DEFAULT NULL COMMENT 'official(enterprise) account ID',
  create_time datetime DEFAULT NULL COMMENT 'create time',
  update_time datetime DEFAULT NULL COMMENT 'update time',
  tenant_id varchar(255) DEFAULT 'super' COMMENT 'tenant ID',
  PRIMARY KEY (id)
) COMMENT='message template';

CREATE TABLE msg_weixin_menu (
  id varchar(48) NOT NULL COMMENT 'primary key',
  offical_account_id varchar(500) NOT NULL COMMENT 'official account primary key',
  app_id varchar(500) NOT NULL COMMENT 'official account AppId',
  menu_json varchar(4000) NOT NULL COMMENT 'menu JSON',
  create_time datetime DEFAULT NULL COMMENT 'create time',
  update_time datetime DEFAULT NULL COMMENT 'update time',
  tenant_id varchar(255) DEFAULT 'super' COMMENT 'tenant ID',
  PRIMARY KEY (id)
) COMMENT='wechat official account menu';

CREATE TABLE msg_weixin_menu_reply (
  id varchar(48) NOT NULL COMMENT 'primary key',
  offical_account_id varchar(500) NOT NULL COMMENT 'official account primary key',
  app_id varchar(500) NOT NULL COMMENT 'official account AppId',
  menu_btn_key varchar(255) NOT NULL COMMENT 'menu button Key',
  res_type varchar(2) NOT NULL DEFAULT '1' COMMENT 'response type(1: text response)',
  reply_content varchar(4000) DEFAULT NULL COMMENT 'response content',
  create_time datetime DEFAULT NULL COMMENT 'create time',
  update_time datetime DEFAULT NULL COMMENT 'update time',
  tenant_id varchar(255) DEFAULT 'super' COMMENT 'tenant ID',
  PRIMARY KEY (id)
) COMMENT='wechat official account menu response';


CREATE TABLE person_face_match_log (
  id varchar(48) NOT NULL COMMENT 'primary key',
  received_seq varchar(48) NOT NULL COMMENT 'business serial number',
  healthcode_log_id varchar(48) DEFAULT NULL COMMENT 'health code log ID',
  unique_id varchar(48) DEFAULT NULL COMMENT 'personnel id',
  dept_id bigint(20) DEFAULT NULL COMMENT 'department ID',
  channel_code varchar(255) DEFAULT NULL COMMENT 'channel code',
  scene_image varchar(255) NOT NULL COMMENT 'scene image path',
  online_image varchar(255) DEFAULT NULL COMMENT 'online verification image path',
  chip_image varchar(255) DEFAULT NULL COMMENT 'chip image path',
  stock_image varchar(255) DEFAULT NULL COMMENT 'database image path',
  scene_video varchar(255) DEFAULT NULL COMMENT 'scene video path',
  scene_online_score double DEFAULT NULL COMMENT 'comparison score between scene image and online verification image',
  scene_chip_score double DEFAULT NULL COMMENT 'comparison score between scene image and chip image',
  scene_stock_score double DEFAULT NULL COMMENT 'comparison score between scene image and database image',
  online_chip_score double DEFAULT NULL COMMENT 'comparison score between online verification image and chip image',
  checklive_score double DEFAULT NULL COMMENT 'checklive score',
  scene_online_result varchar(1) DEFAULT NULL COMMENT 'comparison result between scene image and online verification image(0 pass, 1 fail)',
  scene_chip_result varchar(1) DEFAULT NULL COMMENT 'comparison result between scene image and chip image(0 pass, 1 not pass)',
  scene_stock_result varchar(1) DEFAULT NULL COMMENT 'comparison result between scene image and database image(0 pass, 1 fail)',
  online_chip_result varchar(1) DEFAULT NULL COMMENT 'comparison result between online verification image and chip image(0 pass, 1 fail)',
  checklive_result varchar(1) DEFAULT NULL COMMENT 'scene image checklive result(0 pass, 1 not pass)',
  result varchar(1) DEFAULT NULL COMMENT 'comparison result(0 pass, 1 not pass)',
  temperature double DEFAULT NULL COMMENT 'temperature',
  temperature_floor double DEFAULT NULL COMMENT 'temperature threshold lower limit',
  temperature_top double DEFAULT NULL COMMENT 'temperature threshold upper limit',
  device_code varchar(255) DEFAULT NULL COMMENT 'device code',
  device_name varchar(255) DEFAULT NULL COMMENT 'device name',
  device_model varchar(255) DEFAULT NULL COMMENT 'device model code',
  device_ip varchar(255) DEFAULT NULL COMMENT 'device IP',
  device_longitude double DEFAULT NULL COMMENT 'device longitude(east longitude)',
  device_dimension double DEFAULT NULL COMMENT 'device dimension(north latitude)',
  device_direction varchar(10) DEFAULT 'UNKNOWN' COMMENT 'device direction(IN: in, OUT: out, UNKNOWN: unknown)',
  received_time datetime not null COMMENT 'request time',
  time_used bigint(20) DEFAULT NULL COMMENT 'time used(ms)',
  server_id varchar(255) DEFAULT NULL COMMENT 'server id',
  vendor_code varchar(255) DEFAULT NULL COMMENT 'vendor',
  algs_version varchar(255) DEFAULT NULL COMMENT 'algorithm version',
  create_time datetime DEFAULT NULL COMMENT 'create time',
  batch_date datetime DEFAULT NULL COMMENT 'cron job execution time',
  tenant_id varchar(255) DEFAULT 'super' COMMENT 'tenant ID',
  PRIMARY KEY ( id, received_time ) 
) PARTITION BY RANGE (TO_DAYS(received_time)) (
   PARTITION p0 VALUES LESS THAN (0)
) ;
ALTER TABLE person_face_match_log COMMENT='personnel face comparison log table';
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
  id varchar(48) NOT NULL COMMENT 'primary key',
  received_seq varchar(48) NOT NULL COMMENT 'business serial number',
  healthcode_log_id varchar(48) DEFAULT NULL COMMENT 'health code log ID',
  scene_type varchar(1) NOT NULL COMMENT 'type(data dictionary 1: basic 1:N in database, 2: comparison interface 1:N, 3: log postback)',
  unique_id varchar(48) DEFAULT NULL COMMENT 'personnel id',
  dept_id bigint(20) DEFAULT NULL COMMENT 'department ID',
  channel_code varchar(255) DEFAULT NULL COMMENT 'channel code',
  sub_treasury_code varchar(255) DEFAULT NULL COMMENT 'sub-database code',
  sub_treasury_name varchar(255) DEFAULT NULL COMMENT 'sub-database name',
  scene_image varchar(255) NOT NULL COMMENT 'scene image path',
  stock_image varchar(255) DEFAULT NULL COMMENT 'database image path',
  take_photo1 varchar(255) DEFAULT NULL COMMENT 'snapshot1 path',
  take_photo2 varchar(255) DEFAULT NULL COMMENT 'snapshot2 path',
  scene_video varchar(255) DEFAULT NULL COMMENT 'scene video path',
  scene_stock_score double DEFAULT NULL COMMENT 'score',
  checklive_score double DEFAULT NULL COMMENT 'scene image checklive score',
  checklive_result varchar(1) DEFAULT NULL COMMENT 'scene image checklive result(0: pass, 1: fail)',
  temperature double DEFAULT NULL COMMENT 'temperature',
  temperature_floor double DEFAULT NULL COMMENT 'temperature threshold lower limit',
  temperature_top double DEFAULT NULL COMMENT 'temperature threshold upper limit',
  bio_recognized char(1) DEFAULT 'Y' COMMENT 'biometric identification or not(Y: yes, N: no)',
  result varchar(1) DEFAULT NULL COMMENT 'result(0: pass, 1: fail)',
  device_code varchar(255) DEFAULT NULL COMMENT 'device code',
  device_name varchar(255) DEFAULT NULL COMMENT 'device name',
  device_model varchar(255) DEFAULT NULL COMMENT 'device model code',
  device_ip varchar(255) DEFAULT NULL COMMENT 'device IP',
  device_longitude double DEFAULT NULL COMMENT 'device longitude(east longitude)',
  device_dimension double DEFAULT NULL COMMENT 'device latitude(north latitude)',
  device_direction varchar(10) DEFAULT 'UNKNOWN' COMMENT 'device direction(IN: in, OUT: out, UNKNOWN: unknown)',
  received_time datetime NOT NULL COMMENT 'request time',
  time_used bigint(20) DEFAULT NULL COMMENT 'time used(ms)',
  server_id varchar(255) DEFAULT NULL COMMENT 'server id',
  vendor_code varchar(255) DEFAULT NULL COMMENT 'vendor',
  algs_version varchar(255) DEFAULT NULL COMMENT 'algorithm version',
  create_time datetime DEFAULT NULL COMMENT 'create time',
  batch_date datetime DEFAULT NULL COMMENT 'cron job execution time',
  tenant_id varchar(255) DEFAULT 'super' COMMENT 'tenant ID',
  PRIMARY KEY ( id, received_time ) 
) PARTITION BY RANGE (TO_DAYS(received_time)) (
   PARTITION p0 VALUES LESS THAN (0)
) ;
ALTER TABLE person_face_search_log COMMENT='personnel face search log table';
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
  id varchar(48) NOT NULL COMMENT 'primary key',
  received_seq varchar(48) NOT NULL COMMENT 'business serial number',
  unique_id varchar(48) DEFAULT NULL COMMENT 'personnel id',
  dept_id bigint(20) DEFAULT NULL COMMENT 'department ID',
  channel_code varchar(255) DEFAULT NULL COMMENT 'channel code',
  finger_no varchar(2) DEFAULT NULL COMMENT 'finger code',
  scene_image varchar(255) NOT NULL COMMENT 'scene image path',
  stock_image varchar(255) DEFAULT NULL COMMENT 'database image path',
  scene_stock_score double DEFAULT NULL COMMENT 'comparison score between scene image and database image',
  scene_stock_result varchar(1) DEFAULT NULL COMMENT 'comparison result between scene image and database image(0: pass, 1: fail)',
  result varchar(1) DEFAULT NULL COMMENT 'comparison result(0: pass, 1: fail)',
  received_time datetime not NULL COMMENT 'request time',
  time_used bigint(20) DEFAULT NULL COMMENT 'time used(ms)',
  server_id varchar(255) DEFAULT NULL COMMENT 'server id',
  vendor_code varchar(255) DEFAULT NULL COMMENT 'vendor',
  algs_version varchar(255) DEFAULT NULL COMMENT 'algorithm version',
  create_time datetime DEFAULT NULL COMMENT 'create time',
  batch_date datetime DEFAULT NULL COMMENT 'cron job execution time',
  tenant_id varchar(255) DEFAULT 'super' COMMENT 'tenant ID',
  PRIMARY KEY ( id, received_time ) 
) PARTITION BY RANGE (TO_DAYS(received_time)) (
   PARTITION p0 VALUES LESS THAN (0)
) ;
ALTER TABLE person_finger_match_log COMMENT='personnel fingerprint comparison log table';
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
  id varchar(48) NOT NULL COMMENT 'primary key',
  received_seq varchar(48) NOT NULL COMMENT 'business serial number',
  scene_type varchar(1) NOT NULL COMMENT 'type(data dictionary 1: basic 1:N in database, 2: comparison interface 1:N, 3: log postback)',
  unique_id varchar(48) DEFAULT NULL COMMENT 'personnel id',
  dept_id bigint(20) DEFAULT NULL COMMENT 'department ID',
  channel_code varchar(255) DEFAULT NULL COMMENT 'channel code',
  sub_treasury_code varchar(255) DEFAULT NULL COMMENT 'sub-database code',
  sub_treasury_name varchar(255) DEFAULT NULL COMMENT 'sub-database name',
  finger_no varchar(2) DEFAULT NULL COMMENT 'finger code',
  scene_image varchar(255) NOT NULL COMMENT 'scene image path',
  stock_image varchar(255) DEFAULT NULL COMMENT 'database image path',
  scene_stock_score double DEFAULT NULL COMMENT 'score',
  result varchar(1) DEFAULT NULL COMMENT ' result(0: pass, 1: fail)',
  received_time datetime NOT NULL COMMENT 'request time',
  time_used bigint(20) DEFAULT NULL COMMENT 'time used(ms)',
  server_id varchar(255) DEFAULT NULL COMMENT 'server id',
  vendor_code varchar(255) DEFAULT NULL COMMENT 'vendor',
  algs_version varchar(255) DEFAULT NULL COMMENT 'algorithm version',
  create_time datetime DEFAULT NULL COMMENT 'create time',
  batch_date datetime DEFAULT NULL COMMENT 'cron job execution time',
  tenant_id varchar(255) DEFAULT 'super' COMMENT 'tenant ID',
  PRIMARY KEY ( id, received_time ) 
) PARTITION BY RANGE (TO_DAYS(received_time)) (
   PARTITION p0 VALUES LESS THAN (0)
) ;
ALTER TABLE person_finger_search_log COMMENT='personnel fingerprint search log table';
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
  id varchar(48) NOT NULL COMMENT 'primary key',
  received_seq varchar(48) NOT NULL COMMENT 'business serial number',
  unique_id varchar(48) DEFAULT NULL COMMENT 'personnel id',
  dept_id bigint(20) DEFAULT NULL COMMENT 'department ID',
  channel_code varchar(255) DEFAULT NULL COMMENT 'channel code',
  scene_image varchar(255) DEFAULT NULL COMMENT 'scene image path',
  stock_image varchar(255) DEFAULT NULL COMMENT 'database image path',
  scene_stock_score double DEFAULT NULL COMMENT 'comparison score between scene image and database image',
  scene_stock_result varchar(1) DEFAULT NULL COMMENT 'comparison result between scene image and database image(0: pass, 1: fail)',
  result varchar(1) DEFAULT NULL COMMENT 'comparison result(0: pass, 1: fail)',
  received_time datetime NOT NULL COMMENT ' request time',
  time_used bigint(20) DEFAULT NULL COMMENT ' time used(ms)',
  server_id varchar(255) DEFAULT NULL COMMENT 'server id',
  vendor_code varchar(255) DEFAULT NULL COMMENT 'vendor',
  algs_version varchar(255) DEFAULT NULL COMMENT 'algorithm version',
  create_time datetime DEFAULT NULL COMMENT 'create time',
  batch_date datetime DEFAULT NULL COMMENT 'cron job execution time',
  tenant_id varchar(255) DEFAULT 'super' COMMENT 'tenant ID',
  PRIMARY KEY ( id, received_time ) 
)  PARTITION BY RANGE (TO_DAYS(received_time)) (
   PARTITION p0 VALUES LESS THAN (0)
) ;
ALTER TABLE person_iris_match_log COMMENT='personnel iris comparison log table';
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
  id varchar(48) NOT NULL COMMENT 'primary key',
  received_seq varchar(48) NOT NULL COMMENT 'business serial number',
  scene_type varchar(1) NOT NULL COMMENT 'type(data dictionary 1：basic information 1:N in database, 2: comparison search interface 1:N)',
  unique_id varchar(48) DEFAULT NULL COMMENT 'personnel id',
  dept_id bigint(20) DEFAULT NULL COMMENT 'department ID',
  channel_code varchar(255) DEFAULT NULL COMMENT 'channel code',
  sub_treasury_code varchar(255) DEFAULT NULL COMMENT 'sub-database code',
  sub_treasury_name varchar(255) DEFAULT NULL COMMENT 'sub-database name',
  scene_image varchar(255) DEFAULT NULL COMMENT 'scene image path',
  stock_image varchar(255) DEFAULT NULL COMMENT 'database image path',
  scene_stock_score double DEFAULT NULL COMMENT 'comparison score between scene image and database image',
  result varchar(1) DEFAULT NULL COMMENT 'result(0: pass, 1: fail)',
  received_time datetime NOT NULL COMMENT 'request time',
  time_used bigint(20) DEFAULT NULL COMMENT 'time used(ms)',
  server_id varchar(255) DEFAULT NULL COMMENT 'server id',
  vendor_code varchar(255) DEFAULT NULL COMMENT 'vendor',
  algs_version varchar(255) DEFAULT NULL COMMENT 'algorithm version',
  create_time datetime DEFAULT NULL COMMENT 'create time',
  batch_date datetime DEFAULT NULL COMMENT 'cron job execution time',
  tenant_id varchar(255) DEFAULT 'super' COMMENT 'tenant ID',
  PRIMARY KEY ( id, received_time ) 
)  PARTITION BY RANGE (TO_DAYS(received_time)) (
   PARTITION p0 VALUES LESS THAN (0)
) ;
ALTER TABLE person_iris_search_log COMMENT='personnel iris search log table';
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
  id varchar(48) NOT NULL COMMENT 'primary key',
  received_seq varchar(48) NOT NULL COMMENT 'business serial number',
  healthcode_log_id varchar(48) DEFAULT NULL COMMENT 'health code log ID',
  scene_type char(1) NOT NULL COMMENT 'type(data dictionary 1：basic information 1:N in database, 2: comparison search interface 1:N)',
  unique_id varchar(50) DEFAULT NULL COMMENT 'personnel unique id',
  dept_id varchar(50) DEFAULT NULL COMMENT 'department ID',
  channel_code varchar(255) DEFAULT NULL COMMENT 'channel code',
  sub_treasury_code varchar(255) DEFAULT NULL COMMENT 'sub-database code',
  scene_face_image varchar(255) DEFAULT NULL COMMENT 'face scene image path',
  scene_iris_image varchar(255) DEFAULT NULL COMMENT 'iris scene image path',
  scene_face_score double DEFAULT NULL COMMENT 'face comparison score',
  scene_iris_score double DEFAULT NULL COMMENT 'iris comparison score',
  stock_face_image varchar(255) DEFAULT NULL COMMENT 'database face image path',
  stock_iris_image varchar(255) DEFAULT NULL COMMENT 'database iris image path',
  match_mode char(1) DEFAULT NULL COMMENT 'comparison mode dictionary match Mode',
  match_score double DEFAULT NULL COMMENT 'comparison score, score or fusion score when pass comparison',
  checklive_score double DEFAULT NULL COMMENT 'scene image checklive score',
  checklive_result varchar(1) DEFAULT NULL COMMENT 'scene image checklive result(0: pass, 1: fail)',
  result char(1) DEFAULT NULL COMMENT 'result(0: pass, 1: fail)',
  received_time datetime NOT NULL COMMENT 'transaction time',
  time_used bigint(20) DEFAULT NULL COMMENT ' comparison time(ms)',
  face_time_used bigint(20) DEFAULT NULL COMMENT 'face comparison time ms',
  iris_time_used bigint(20) DEFAULT NULL COMMENT 'iris comparison time ms',
  temperature double DEFAULT NULL COMMENT 'temperature',
  temperature_floor double DEFAULT NULL COMMENT 'temperature threshold lower limit',
  temperature_top double DEFAULT NULL COMMENT 'temperature threshold upper limit',
  device_sn varchar(50) DEFAULT NULL COMMENT 'device id',
  device_model varchar(255) DEFAULT NULL COMMENT 'device model code',
  device_name varchar(255) DEFAULT NULL COMMENT 'device name',
  device_ip varchar(255) DEFAULT NULL COMMENT 'device IP',
  device_longitude double DEFAULT NULL COMMENT 'device longitude(east longitude)',
  device_dimension double DEFAULT NULL COMMENT 'device latitude(north latitude)',
  device_direction varchar(10) DEFAULT 'UNKNOWN' COMMENT 'device direction(IN: in, OUT: out, UNKNOWN: unknown)',
  server_id varchar(255) DEFAULT NULL COMMENT 'server id',
  create_time datetime DEFAULT NULL COMMENT 'create time',
  batch_date datetime DEFAULT NULL COMMENT 'cron job execution time',
  remark varchar(255) DEFAULT NULL COMMENT 'remark',
  tenant_id varchar(255) DEFAULT 'super' COMMENT 'tenant ID',
  PRIMARY KEY (id,received_time)
) PARTITION BY RANGE (TO_DAYS(received_time)) (
   PARTITION p0 VALUES LESS THAN (0)
) ;
ALTER TABLE person_faceiris_search_log COMMENT='faceiris multi-modal search log table';
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
  id varchar(50) NOT NULL COMMENT 'primary key',
  received_seq varchar(50) NOT NULL COMMENT 'business serial number',
  unique_id varchar(50) COMMENT 'personnel unique id, corresponding person_cert',
  received_time datetime NOT NULL COMMENT 'request time',
  result char(1) DEFAULT NULL COMMENT 'result(0: pass, 1: fail)',
  message varchar(255) DEFAULT NULL COMMENT 'result description',
  device_code varchar(50) DEFAULT NULL COMMENT 'device number',
  temperature varchar(10) DEFAULT NULL COMMENT 'face measure temperature',
  car_no varchar(10) DEFAULT NULL COMMENT 'car number',
  region varchar(50) DEFAULT NULL COMMENT 'area(call which health code interface)',
  time_used bigint(20) DEFAULT NULL COMMENT 'time used ms',
  create_time datetime NOT NULL COMMENT 'create time',
  batch_date datetime DEFAULT NULL COMMENT 'cron job execution time',
  tenant_id varchar(255) DEFAULT 'super' COMMENT 'tenant ID',
  PRIMARY KEY (id,received_time)
) PARTITION BY RANGE (TO_DAYS(received_time)) (
   PARTITION p0 VALUES LESS THAN (0)
) ; 
ALTER TABLE person_health_code_log COMMENT='health code request record';
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
  id varchar(48) NOT NULL COMMENT 'primary key',
  trans_code varchar(48) DEFAULT NULL COMMENT 'transaction code',
  trans_title varchar(48) DEFAULT NULL COMMENT 'transaction title',
  received_time datetime NOT NULL COMMENT 'request time',
  client_ip varchar(48) DEFAULT NULL COMMENT 'client side IP',
  send_time datetime NOT NULL COMMENT 'response time',
  time_used bigint(20) NOT NULL COMMENT 'time used(ms)',
  status_code varchar(48) NOT NULL DEFAULT '0' COMMENT 'status code',
  trans_url varchar(255) DEFAULT NULL COMMENT 'transaction request path',
  class_method varchar(255) DEFAULT NULL COMMENT 'handle method',
  channel_code varchar(255) DEFAULT NULL COMMENT 'channel code',
  tenant_id varchar(255) DEFAULT 'super' COMMENT 'tenant ID',
  detail_file_path varchar(255) DEFAULT NULL  COMMENT 'message detailed file',
  PRIMARY KEY (id,received_time)
) PARTITION BY RANGE (TO_DAYS(received_time)) (
   PARTITION p0 VALUES LESS THAN (0)
) ; 
ALTER TABLE trade_req_record COMMENT='interface transaction request record';
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
  id varchar(48) NOT NULL COMMENT 'primary key',
  user_id bigint(20) NOT NULL COMMENT 'user primary key(related user table)',
  feature varchar(4000) NOT NULL COMMENT 'face feature',
  feature_md5 varchar(255) DEFAULT NULL COMMENT 'face feature MD5',
  quality_score double DEFAULT NULL COMMENT 'image quality score',
  image_url varchar(255) NOT NULL COMMENT 'face image path',
  vendor_code varchar(48) DEFAULT NULL COMMENT 'vendor',
  algs_version varchar(48) DEFAULT NULL COMMENT 'algorithm version',
  status char(1) NOT NULL DEFAULT '0' COMMENT 'status: 0-valid  1: invalid',
  remark varchar(255) DEFAULT NULL COMMENT 'remark',
  create_by varchar(64) DEFAULT NULL COMMENT 'creator',
  update_by varchar(64) DEFAULT NULL COMMENT 'updator',
  create_time datetime DEFAULT NULL COMMENT 'create time',
  update_time datetime DEFAULT NULL COMMENT 'update time',
  tenant_id varchar(255) DEFAULT 'super' COMMENT 'tenant ID',
  PRIMARY KEY (id)
) COMMENT='user face information';


CREATE TABLE sys_user_finger (
  id varchar(48) NOT NULL COMMENT 'primary key',
  user_id bigint(20) NOT NULL COMMENT 'user primary key(related user table)',
  finger_no varchar(2) NOT NULL COMMENT 'finger number',
  feature varchar(4000) NOT NULL COMMENT 'fingerprint feature',
  feature_md5 varchar(255) DEFAULT NULL COMMENT 'fingerprint feature MD5',
  quality_score double DEFAULT NULL COMMENT 'image quality score',
  image_url varchar(255) DEFAULT NULL COMMENT 'fingerprint image path',
  vendor_code varchar(48) DEFAULT NULL COMMENT 'vendor',
  algs_version varchar(48) DEFAULT NULL COMMENT 'algorithm version',
  status char(1) NOT NULL DEFAULT '0' COMMENT 'status: 0-valid  1: invalid',
  remark varchar(255) DEFAULT NULL COMMENT 'remark',
  create_by varchar(64) DEFAULT NULL COMMENT 'creator',
  update_by varchar(64) DEFAULT NULL COMMENT 'updator',
  create_time datetime DEFAULT NULL COMMENT 'create time',
  update_time datetime DEFAULT NULL COMMENT 'update time',
  tenant_id varchar(255) DEFAULT 'super' COMMENT 'tenant ID',
  PRIMARY KEY (id)
) COMMENT='user fingerprint information';

CREATE TABLE sys_user_iris (
  id varchar(48) NOT NULL COMMENT 'primary key',
  user_id bigint(20) NOT NULL COMMENT 'user primary key(related user information table)',
  feature longtext NOT NULL COMMENT 'iris feature',
  feature_md5 varchar(255) DEFAULT NULL COMMENT 'iris feature MD5',
  quality_score double DEFAULT NULL COMMENT 'image quality score',
  image_url varchar(255) NOT NULL COMMENT 'iris image path',
  vendor_code varchar(48) DEFAULT NULL COMMENT 'vendor',
  algs_version varchar(48) DEFAULT NULL COMMENT 'algorithm version',
  status char(1) NOT NULL DEFAULT '0' COMMENT 'status: 0-valid  1-invalid',
  remark varchar(255) DEFAULT NULL COMMENT 'remark',
  create_by varchar(64) DEFAULT NULL COMMENT 'creator',
  update_by varchar(64) DEFAULT NULL COMMENT 'updator',
  create_time datetime DEFAULT NULL COMMENT 'create time',
  update_time datetime DEFAULT NULL COMMENT 'update time',
  tenant_id varchar(255) DEFAULT 'super' COMMENT 'tenant ID',
  PRIMARY KEY (id)
) COMMENT='user iris information';


CREATE TABLE sys_user_iris_face (
  id varchar(48) NOT NULL COMMENT 'primary key',
  user_id bigint(20) NOT NULL COMMENT 'user primary key',
  fusion_feature longtext COMMENT 'fusion feature',
  face_feature longtext COMMENT 'face feature',
  iris_feature longtext COMMENT 'iris feature',
  fusion_feature_md5 varchar(255) DEFAULT NULL COMMENT 'fusion feature md5',
  face_feature_md5 varchar(255) DEFAULT NULL COMMENT 'face feature md5',
  face_image_url varchar(255) DEFAULT NULL COMMENT 'face image path',
  face_quality double DEFAULT NULL COMMENT 'face quality score',
  iris_feature_md5 varchar(255) DEFAULT NULL COMMENT 'iris feature md5',
  iris_image_url varchar(255) DEFAULT NULL COMMENT 'iris image path',
  iris_quality double DEFAULT NULL COMMENT 'iris quality score',
  status char(1) NOT NULL DEFAULT '0' COMMENT 'status: 0-valid  1-invalid',
  create_by varchar(48) DEFAULT NULL COMMENT 'creator',
  update_by varchar(48) DEFAULT NULL COMMENT 'updator',
  create_time datetime DEFAULT NULL COMMENT 'create time',
  update_time datetime DEFAULT NULL COMMENT 'update time',
  tenant_id varchar(255) DEFAULT 'super' COMMENT 'tenant ID',
  PRIMARY KEY (id)
) COMMENT='user iris and face multi-modal information';


CREATE TABLE ocr_bank_card_log (
  id varchar(48) NOT NULL COMMENT 'primary key',
  received_seq varchar(48) NOT NULL COMMENT 'business serial number',
  received_time datetime NOT NULL COMMENT 'request time',
  type varchar(12) DEFAULT NULL COMMENT 'image type, if use turui algorithm library, fill Unknown',
  type_id varchar(12) DEFAULT NULL COMMENT 'card type, if use wentong algorithm library, fill in 0',
  channel_code varchar(255) DEFAULT NULL COMMENT 'channel code',
  scene_image_url varchar(255) NOT NULL COMMENT 'save path of certificate image to be recognized',
  scene_image_name varchar(255) DEFAULT NULL COMMENT 'name of certificate image to be recognized',
  bank_card_expiry_date varchar(20) DEFAULT NULL COMMENT 'valid date',
  bank_card_bank_name varchar(255) DEFAULT NULL COMMENT 'bank name',
  bank_card_bank_code varchar(255) DEFAULT NULL COMMENT 'bank number',
  bank_card_name varchar(20) DEFAULT NULL COMMENT 'card name',
  bank_card_type varchar(20) DEFAULT NULL COMMENT 'card type',
  bank_card_number varchar(50) DEFAULT NULL COMMENT 'card number',
  result char(1) DEFAULT NULL COMMENT 'recognition result',
  tenant_id varchar(255) DEFAULT 'super' COMMENT 'tenant id',
  PRIMARY KEY (id)
) COMMENT='bank card OCR recognition record';

CREATE TABLE ocr_busi_lic_log (
  id varchar(48) NOT NULL COMMENT 'primary key',
  received_seq varchar(48) NOT NULL COMMENT 'business serial number',
  received_time datetime NOT NULL COMMENT 'request time',
  type varchar(12) DEFAULT NULL COMMENT 'image type, if use turui algorithm library, fill in Unknown',
  type_id varchar(12) DEFAULT NULL COMMENT 'card type, if use wentong algorithm library, fill in 0',
  channel_code varchar(255) DEFAULT NULL COMMENT 'channel code',
  scene_image_url varchar(255) NOT NULL COMMENT 'save path of certificate image to be recognized',
  scene_image_name varchar(255) DEFAULT NULL COMMENT 'name of certificate image to be recognized',
  busi_lic_registered_no varchar(255) DEFAULT NULL COMMENT 'register number',
  busi_lic_origination_no varchar(255) DEFAULT NULL COMMENT 'business license issue no',
  busi_lic_tax_no varchar(255) DEFAULT NULL COMMENT 'business license tax no',
  busi_lic_social_insurance_no varchar(255) DEFAULT NULL COMMENT 'business license social insurance no',
  busi_lic_statistic_no varchar(255) DEFAULT NULL COMMENT 'business license statistic no',
  busi_lic_name varchar(255) DEFAULT NULL COMMENT 'company name',
  busi_lic_type varchar(255) DEFAULT NULL COMMENT 'company type',
  busi_lic_address varchar(255) DEFAULT NULL COMMENT 'company address',
  busi_lic_owner varchar(20) DEFAULT NULL COMMENT 'legal person',
  busi_lic_form varchar(255) DEFAULT NULL COMMENT 'composition form',
  busi_lic_registered_capital varchar(20) DEFAULT NULL COMMENT 'register capital',
  busi_lic_registry_date varchar(20) DEFAULT NULL COMMENT 'register date',
  busi_lic_expiry_date varchar(20) DEFAULT NULL COMMENT 'expiry date',
  busi_lic_scope varchar(255) DEFAULT NULL COMMENT 'business scope',
  busi_lic_issure_authority varchar(255) DEFAULT NULL COMMENT 'issue authority',
  busi_lic_issure_date varchar(20) DEFAULT NULL COMMENT 'issue date',
  busi_lic_qr_code varchar(255) DEFAULT NULL COMMENT 'business license QR code',
  result char(1) DEFAULT NULL COMMENT 'recognition result',
  tenant_id varchar(255) DEFAULT 'super',
  PRIMARY KEY (id)
) COMMENT='business license OCR recognition record';


CREATE TABLE ocr_driver_lic_log (
  id varchar(48) NOT NULL COMMENT 'primary key',
  received_seq varchar(48) NOT NULL COMMENT 'business serial number',
  received_time datetime NOT NULL COMMENT ' request time',
  type varchar(12) DEFAULT NULL COMMENT 'image type, if use turui algorithm library, fill in Unknown',
  type_id varchar(12) DEFAULT NULL COMMENT 'card type, if use wentong algorithm library, fill in 0',
  ocr_firm_type varchar(12) DEFAULT NULL COMMENT 'algorithm type',
  channel_code varchar(255) DEFAULT NULL COMMENT 'channel code',
  scene_image_url varchar(255) NOT NULL COMMENT 'save path of certificate image to be recognized',
  scene_image_name varchar(255) DEFAULT NULL COMMENT 'name certificate image to be recognized',
  driver_lic_number varchar(50) DEFAULT NULL COMMENT 'driver certificate number',
  driver_lic_head_image varchar(255) DEFAULT NULL COMMENT 'certificate avatar',
  driver_lic_address varchar(255) DEFAULT NULL COMMENT 'driver certificate home address',
  driver_lic_birth varchar(255) DEFAULT NULL COMMENT 'driver certificate birth date',
  driver_lic_gender varchar(20) DEFAULT NULL COMMENT 'driver certificate sex',
  driver_lic_name varchar(20) DEFAULT NULL COMMENT 'driver certificate owner name',
  driver_lic_driver_type varchar(20) DEFAULT NULL COMMENT 'driver certificate quasi-driving type',
  driver_lic_first_issue varchar(20) DEFAULT NULL COMMENT 'driver certificate first issue date',
  driver_lic_valid_from varchar(255) DEFAULT NULL COMMENT 'valid date start time',
  driver_lic_valid_for varchar(255) DEFAULT NULL COMMENT 'valid duration',
  driver_lic_expiry_date varchar(20) DEFAULT NULL COMMENT 'expiry date',
  result char(1) DEFAULT NULL COMMENT 'recognition result',
  tenant_id varchar(255) DEFAULT 'super',
  PRIMARY KEY (id)
) COMMENT='driver certificate OCR recognition result';

CREATE TABLE ocr_driving_lic_log (
  id varchar(48) NOT NULL COMMENT 'primary key',
  received_seq varchar(48) NOT NULL COMMENT 'business serial number',
  received_time datetime NOT NULL COMMENT 'request time',
  type varchar(12) DEFAULT NULL COMMENT 'image type, if use turui algorithm library, fill in Unknown',
  type_id varchar(12) DEFAULT NULL COMMENT 'card type, if use wentong algorithm library, fill in 0',
  channel_code varchar(255) DEFAULT NULL COMMENT 'channel code',
  scene_image_url varchar(255) NOT NULL COMMENT 'save path of certificate image to be recognized',
  scene_image_name varchar(255) DEFAULT NULL COMMENT 'name certificate image to be recognized',
  driving_lic_model varchar(20) DEFAULT NULL COMMENT 'brand model',
  driving_lic_address varchar(255) DEFAULT NULL COMMENT 'home address',
  driving_lic_car_number varchar(255) DEFAULT NULL COMMENT 'car number',
  driving_lic_vehicle_type varchar(255) DEFAULT NULL COMMENT 'car type',
  driving_lic_issue_date varchar(20) DEFAULT NULL COMMENT 'issue date',
  driving_lic_engine_no varchar(255) DEFAULT NULL COMMENT 'engine number',
  driving_lic_vin varchar(255) DEFAULT NULL COMMENT 'car recognition number',
  driving_lic_register_date varchar(20) DEFAULT NULL COMMENT 'register date',
  driving_lic_use_type varchar(255) DEFAULT NULL COMMENT 'used for',
  driving_lic_owner varchar(20) DEFAULT NULL COMMENT 'owner',
  result char(1) DEFAULT NULL COMMENT 'recognition result',
  tenant_id varchar(255) DEFAULT 'super',
  PRIMARY KEY (id)
) COMMENT='driver license OCR recognition record';

CREATE TABLE ocr_hk_mac_pass_log (
  id varchar(48) NOT NULL COMMENT 'primary key',
  received_seq varchar(48) NOT NULL COMMENT 'business serial number',
  received_time datetime NOT NULL COMMENT 'request time',
  type varchar(12) DEFAULT NULL COMMENT 'image type, if use turui algorithm library, fill in Unknown',
  type_id varchar(12) DEFAULT NULL COMMENT 'card type, if use wentong algorithm library, fill in 0',
  channel_code varchar(255) DEFAULT NULL COMMENT 'channel code',
  scene_image_url varchar(255) NOT NULL COMMENT 'save path of certificate image to be recognized',
  scene_image_name varchar(255) DEFAULT NULL COMMENT 'name of certificate image to be recognized',
  hk_mac_pass_type varchar(20) DEFAULT NULL COMMENT 'passport type',
  hk_mac_pass_mrz_number varchar(255) DEFAULT NULL COMMENT 'passport number mrz',
  hk_mac_pass_national_name varchar(255) DEFAULT NULL COMMENT 'national name',
  hk_mac_pass_english_name varchar(255) DEFAULT NULL COMMENT 'english name',
  hk_mac_pass_gender varchar(20) DEFAULT NULL COMMENT 'sex',
  hk_mac_pass_birth varchar(20) DEFAULT NULL COMMENT 'birth date',
  hk_mac_pass_expiry_date varchar(20) DEFAULT NULL COMMENT 'valid date',
  hk_mac_pass_issue_country varchar(20) DEFAULT NULL COMMENT 'issue country code',
  hk_mac_pass_english_sur_name varchar(20) DEFAULT NULL COMMENT 'english last name',
  result char(1) DEFAULT NULL COMMENT 'recognition result',
  tenant_id varchar(255) DEFAULT 'super',
  PRIMARY KEY (id)
) COMMENT='Hongkong and Macao Pass OCR recognition result';

CREATE TABLE ocr_idcard_back_log (
  id varchar(48) NOT NULL COMMENT 'primary key',
  received_seq varchar(48) NOT NULL COMMENT 'business serial number',
  received_time datetime NOT NULL COMMENT 'request time',
  type varchar(12) DEFAULT NULL COMMENT 'image type, if use turui algorithm library, fill in Unknown',
  type_id varchar(12) DEFAULT NULL COMMENT 'card type, if use wentong algorithm library, fill in 0',
  channel_code varchar(255) DEFAULT NULL COMMENT 'channel code',
  scene_image_url varchar(255) NOT NULL COMMENT 'save path of certificate image to be recognized',
  scene_image_name varchar(255) DEFAULT NULL COMMENT 'name of certificate image to be recognized',
  idcard_date_issue varchar(20) DEFAULT NULL COMMENT 'issue date',
  idcard_authority_issue varchar(255) DEFAULT NULL COMMENT 'issue authority',
  idcard_limit varchar(255) DEFAULT NULL COMMENT 'valid date',
  idcard_expiry_date varchar(20) DEFAULT NULL COMMENT 'expiry date',
  idcard_ocr_firm_type varchar(20) DEFAULT NULL COMMENT 'algorithm type',
  result char(1) DEFAULT NULL COMMENT 'recognition result',
  tenant_id varchar(255) DEFAULT 'super',
  PRIMARY KEY (id)
) COMMENT='back of ID card OCR recognition result';

CREATE TABLE ocr_idcard_front_log (
  id varchar(48) NOT NULL COMMENT 'primary key',
  received_seq varchar(48) NOT NULL COMMENT 'business serial number',
  received_time datetime NOT NULL COMMENT 'request time',
  type varchar(12) DEFAULT NULL COMMENT 'image type, if use turui algorithm library, fill in Unknown',
  type_id varchar(12) DEFAULT NULL COMMENT 'card type, if use wentong algorithm library, fill in 0',
  channel_code varchar(255) DEFAULT NULL COMMENT 'channel code',
  scene_image_url varchar(255) NOT NULL COMMENT 'save path of certificate image to be recognized',
  scene_image_name varchar(255) DEFAULT NULL COMMENT 'name of certificate image to be recognized',
  idcard_number varchar(20) DEFAULT NULL COMMENT 'ID card number',
  idcard_headimage varchar(255) DEFAULT NULL COMMENT 'ID card avatar',
  idcard_ethnicity varchar(20) DEFAULT NULL COMMENT 'nation',
  idcard_address varchar(255) DEFAULT NULL COMMENT 'home address',
  idcard_birth varchar(255) DEFAULT NULL COMMENT 'birth date',
  idcard_gender varchar(20) DEFAULT NULL COMMENT 'sex',
  idcard_name varchar(20) DEFAULT NULL COMMENT 'name',
  result char(1) DEFAULT NULL COMMENT 'recognition result',
  idcard_ocr_firm_type varchar(20) DEFAULT NULL COMMENT 'algorithm type',
  tenant_id varchar(255) DEFAULT 'super',
  PRIMARY KEY (id)
) COMMENT='frontend of ID card OCR recognition result';

CREATE TABLE ocr_passport_log (
  id varchar(48) NOT NULL COMMENT 'primary key',
  received_seq varchar(48) NOT NULL COMMENT 'business serial number',
  received_time datetime NOT NULL COMMENT 'request time',
  type varchar(12) DEFAULT NULL COMMENT 'image type, if use turui algorithm library, fill in Unknown',
  type_id varchar(12) DEFAULT NULL COMMENT 'card type, if use wentong algorithm library, fill in 0',
  channel_code varchar(255) DEFAULT NULL COMMENT 'channel code',
  scene_image_url varchar(255) NOT NULL COMMENT 'save path of certificate image to be recognized',
  scene_image_name varchar(255) DEFAULT NULL COMMENT 'name of certificate image to be recognized',
  passport_mrz_fir varchar(255) DEFAULT NULL COMMENT 'first line of passport machine readable code',
  passport_mrz_sec varchar(255) DEFAULT NULL COMMENT 'second line of passport machine readable code',
  passport_nationality_code varchar(20) DEFAULT NULL COMMENT 'card holder nationality code',
  passport_number varchar(255) DEFAULT NULL COMMENT 'passport number',
  passport_birth_place varchar(255) DEFAULT NULL COMMENT 'birth place',
  passport_issue_place varchar(255) DEFAULT NULL COMMENT 'issue place',
  passport_issue_date varchar(20) DEFAULT NULL COMMENT 'issue date',
  passport_rfid_mrz varchar(255) DEFAULT NULL COMMENT 'complete passport machine readable code radio frequency identification',
  passport_ocr_mrz varchar(255) DEFAULT NULL COMMENT 'complete passport machine readable code OCR recognition',
  passport_birth_place_pinyin varchar(255) DEFAULT NULL COMMENT 'birth place pinyin',
  passport_issue_place_pinyin varchar(255) DEFAULT NULL COMMENT 'issue place pinyin',
  passport_id_number varchar(20) DEFAULT NULL COMMENT 'ID card number',
  passport_ocr_national_name varchar(255) DEFAULT NULL COMMENT 'national name pinyin',
  passport_ocr_gender varchar(20) DEFAULT NULL COMMENT 'sex',
  passport_ocr_nationality_code varchar(20) DEFAULT NULL COMMENT 'nationality code',
  passport_ocr_birth_date varchar(20) DEFAULT NULL COMMENT 'birth date',
  passport_ocr_expiry_date varchar(20) DEFAULT NULL COMMENT 'valid date until',
  passport_ocr_authority varchar(255) DEFAULT NULL COMMENT 'issue authority',
  passport_national_surname varchar(20) DEFAULT NULL COMMENT 'national last name',
  passport_national_given_name varchar(20) DEFAULT NULL COMMENT 'national first name',
  passport_height varchar(20) DEFAULT NULL COMMENT 'height',
  result char(1) DEFAULT NULL COMMENT 'recognition result',
  tenant_id varchar(255) DEFAULT 'super',
  PRIMARY KEY (id)
) COMMENT='passport OCR recognition result';



INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (101, 'Face Image Folder', 'basedata.face.dir', './eyecool/biapwp/basedata/face/', 'N', 'admin', sysdate(), 'admin', sysdate(), 'basic data--face image folder', 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (102, 'Fingerprint Image Folder', 'basedata.finger.dir', './eyecool/biapwp/basedata/finger/', 'N', 'admin', sysdate(), 'admin', sysdate(), 'basic data-- fingerprint image folder', 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (103, 'Iris Image Folder', 'basedata.iris.dir', './eyecool/biapwp/basedata/iris/', 'N', 'admin', sysdate(), 'admin', sysdate(), 'basic data--iris image folder', 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (104, 'Face Image Quality Check Threshold', 'basedata.face.quality.detect.threshold', '70', 'N', 'admin', sysdate(), 'admin', sysdate(), 'face image quality check threshold', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (105, 'Certificate Image Folder', 'basedata.cert.dir', './eyecool/biapwp/basedata/cert/', 'N', 'admin', sysdate(), 'admin', sysdate(), 'certificate image folder', 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (106, 'Face 1:1 Comparison Threshold', 'basedata.face.compare.threshold', '80', 'N', 'admin', sysdate(), 'admin', sysdate(), 'face 1:1 comparison threshold', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (107, 'Fingerprint 1:1 Comparison Threshold', 'basedata.finger.compare.threshold', '40', 'N', 'admin', sysdate(), 'admin', sysdate(), ' fingerprint 1:1 comparison threshold', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (108, 'Iris 1:1 Comparison Threshold', 'basedata.iris.compare.threshold', '30', 'N', 'admin', sysdate(), 'admin', sysdate(), 'iris 1:1 comparison threshold', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (111, 'Duplicate Iris Threshold', 'basedata.iris.repeat.threshold', '30', 'N', 'admin', sysdate(), 'admin', sysdate(), 'iris duplicate threshold(recognized as duplicate when comparison score exceeds threshold)', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (112, 'Duplicate Fingerprint Threshold', 'basedata.finger.repeat.threshold', '40', 'N', 'admin', sysdate(), 'admin', sysdate(), 'duplicate fingerprint threshold(recognized as duplicate when comparison score exceeds threshold)', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (113, 'Platform Interface Request Concurrency', 'platform.interface.concurrent', '300', 'N', 'admin', sysdate(), 'admin', sysdate(), 'platform interface request concurrency', 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (114, 'Whether to Check Certificate Database For Face Self Capture and Check', 'basedata.face.collect.validte.cert', 'N', 'N', 'admin', sysdate(), 'admin', sysdate(), 'whether to check certificate database when face self capture and check not in database(Y/N)', 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (115, 'Face 1-N Search Image Folder', 'busi.face.searchN.dir', './eyecool/busi/face/search/', 'N', 'admin', sysdate(), 'admin', sysdate(), 'face 1-N Search image folder', 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (116, 'Face 1-N Search Comparison Threshold', 'basedata.face.searchN.threshold', '80', 'N', 'admin', sysdate(), 'admin', sysdate(), 'face 1-N Search comparison threshold', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (117, 'Fingerprint Image Quality Check Threshold', 'basedata.finger.quality.detect.threshold', '40', 'N', 'admin', sysdate(), 'admin', sysdate(), 'fingerprint image quality check threshold', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (118, 'Face Still Checklive Threshold', 'basedata.face.checklive.threshold', '350', 'N', 'admin', sysdate(), 'admin', sysdate(), 'face checklive threshold(still checklive), must be number', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (119, 'Fingerprint 1-N Search Image Folder', 'busi.finger.searchN.dir', './eyecool/busi/finger/search/', 'N', 'admin', sysdate(), 'admin', sysdate(), 'fingerprint 1-N Search image folder', 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (120, 'Iris 1-N Search Image Folder', 'busi.iris.searchN.dir', './eyecool/busi/iris/search/', 'N', 'admin',sysdate(), 'admin', sysdate(), 'iris 1-N Search image folder', 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (121, 'Fingerprint 1-N Search Comparison Threshold', 'basedata.finger.searchN.threshold', '40', 'N', 'admin', sysdate(), 'admin', sysdate(), 'fingerprint 1-N Search comparison threshold', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (122, 'Iris 1-N Search Comparison Threshold', 'basedata.iris.searchN.threshold', '30', 'N', 'admin', sysdate(), 'admin', sysdate(), 'iris 1-N Search comparison threshold', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (123, 'Face 1:1 Identification Image Folder', 'busi.face.compare.dir', './eyecool/busi/face/compare/', 'N', 'admin', sysdate(), 'admin', sysdate(), 'face 1:1 Identification image folder', 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (124, 'Fingerprint 1:1 Identification Image Folder', 'busi.finger.compare.dir', './eyecool/busi/finger/compare/', 'N', 'admin', sysdate(), 'admin', sysdate(), 'fingerprint 1:1 Identification image folder', 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (125, 'Iris 1:1 Identification Image Folder', 'busi.iris.compare.dir', './eyecool/busi/iris/compare/', 'N', 'admin', sysdate(), 'admin', sysdate(), 'iris 1:1 Identification image folder', 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (127, '1-N Check or Not When Register Face into Database', 'basedata.face.add.isValidN', 'N', 'N', 'admin', sysdate(), 'admin', sysdate(), '1-N Check or Not When Register Face into Database(Y/N)', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (130, '1-N Check or Not When Register Fingerprint into Database', 'basedata.finger.add.isValidN', 'N', 'N', 'admin', sysdate(), 'admin', sysdate(), '1-N Check or Not When Register Fingerprint into Database(Y/N), used for add, edit', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (131, '1-N Check or Not When Register Iris into Database', 'basedata.iris.add.isValidN', 'N', 'N', 'admin', sysdate(), 'admin', sysdate(), '1-N Check or Not When Register Iris into Database(Y/N), used for add, edit', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (132, 'Device APP Version File Save Folder', 'device.version.file.dir', './eyecool/device/version/', 'N', 'admin', sysdate(), '', NULL, 'device APP version file save folder', 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (133, 'Message Notice Attachment Save Path', 'msg.attachment.dir', './eyecool/msg/attachment/', 'N', 'admin', sysdate(), 'admin', sysdate(), 'message notice attachment save path', 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (134, 'Whether to Compare and Deduplicate When Save Face 1-N Log', 'busi.face.searchN.log.save.distinct', 'Y', 'N', 'admin', sysdate(), 'busi_admin', sysdate(), 'there exists situation that recognize the same person in short time(for example, 5s) for face 1-N identification, whether to deduplicate recognition log(Y: yes, N: no), if yes, then only save the record of first recognition', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (137, 'Whether to Open 1-N Search Function for Platform', 'platform.searchN.function.isOpen', 'Y', 'N', 'admin', sysdate(), 'admin', sysdate(), 'Notice: the parameter value need to be set in initialized deployment, avoid modifying during usage. \r\n description: whether to open 1-N Search function on platform(Y: open, N: not open), no need to deploy Datamanager and fox-minisearch micro service if not open.', 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (145, 'Whether to Check Liveness when Register Face into Database', 'basedata.face.add.checkLive', 'N', 'N', 'admin', sysdate(), 'admin', sysdate(), 'Whether to Check Liveness when Register Face into Database(Y/N), used for add, edit,self capture', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (146, 'Whether to Support Bind/Unbind Personnel and Database Relationship Automatically on Platform', 'platform.libperson.isAutobind', 'N', 'N', 'admin', sysdate(), 'admin', sysdate(), 'Whether to Support Bind/Unbind Personnel and Database Relationship Automatically on Platform(used in scene that requires to bind the whole database personnel in some channel, bind with related channel when register personnel into database)', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (147, 'Platform Requires Channel Code to Bind Personnel and Database Relationship Automatically', 'platform.libperson.autobind.channel', '-', 'N', 'admin', sysdate(), 'busi_admin', sysdate(), 'latform Requires Channel Code to Bind Personnel and Database Relationship Automatically(used in scene that requires to bind the whole database personnel in some channel, only valid when open automatic binding, multi channel code separated by \";\")', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (148, 'Device Appearance Image Save Folder', 'device.model.image.dir', './eyecool/device/exteriorImage', 'N', 'admin', sysdate(), 'admin', sysdate(), 'device appearance image save folder', 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (150, 'Face Video Checklive Threshold', 'basedata.face.video.checklive.threshold', '350', 'N', 'admin', sysdate(), 'admin', sysdate(), 'face video checklive threshold(must be number)', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (152, 'Multi-modal Iris 1v1 Comparison Threshold', 'multi.iris.compare.threshold', '30', 'N', 'admin', sysdate(), 'admin', sysdate(), ' multi-modal iris threshold', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (153, 'Multi-modal Face 1v1 Comparison Threshold', 'multi.face.compare.threshold', '80', 'N', 'admin', sysdate(), 'admin', sysdate(), ' multi-modal face threshold', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (154, 'Multi-modal Iris and Face Fusion 1v1 Comparison Threshold', 'multi.irisface.fusion.compare.threshold', '80', 'N', 'admin', sysdate(), 'admin', sysdate(), ' multi-modal iris face fusion threshold', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (155, 'Multi-modal Log Image Save Path', 'busi.mulit.search.dir', './eyecool/busi/mulit/', 'N', 'admin', sysdate(), '', NULL, ' multi-modal log image save path', 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (156, 'OCR Log Image Save Image Path', 'busi.orc.image.dir', './eyecool/busi/ocr/', 'N', 'admin', sysdate(), 'admin', sysdate(), 'OCR image save path', 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (157, 'Switch to Push Insensitive Recognition of Stranger', 'biapwp.dfrs.stranger.push', 'true', 'N', 'admin', sysdate(), 'admin', sysdate(), 'Switch to Push Insensitive Recognition of Stranger', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (158, 'Switch to Push Insensitive Recognition of Hits', 'biapwp.dfrs.hit.push', 'true', 'N', 'admin', sysdate(), 'admin', sysdate(), 'Switch to Push Insensitive Recognition of Hits', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (159, 'High Temperature Alarm Threshold', 'biapwp.dfrs.hcnet.alarmtem', '37.0', 'N', 'admin', sysdate(), 'admin', sysdate(), 'High Temperature Alarm threshold', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (160, 'Show Template Image of Frontend Hits', 'biapwp.dfrs.hit.showTmpl', 'false', 'N', 'admin', sysdate(), 'admin', sysdate(), 'Show Template Image of Frontend Hits', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (161, 'Channel Code to Sync Information Triggered by Biometric Information Update', 'person.biochange.liveupdate.channel', '-', 'N', 'admin', sysdate(), 'busi_admin', sysdate(), 'configure channel code triggered update only when biometric feature changes, separated by  \";\"', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (162, 'Whether to Receive Scene Postback Log on Platform', 'platform.accept.baklog.open', 'Y', 'N', 'admin', sysdate(), 'admin', sysdate(), 'platform receives scene postback log or not(yes：Y， no：N)', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (163, 'Multi-modal Database Image Save Path', 'base.mulit.dir', './eyecool/base/mulit/', 'N', 'admin', sysdate(), '', NULL, ' multi-modal database image save path', 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (165, 'Online Verification Interface Address', 'identity_verification_url', 'http://green.eyekey.com/ark-plc-id/checkIdentity', 'N', 'admin', sysdate(), 'busi_admin', sysdate(), 'online verification interface request address URL', 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (166, 'Online Verification Interface Authorization Code', 'identity_verification_authCode', 'd61c1f9110524f20b491c11779bf120d', 'N', 'admin', sysdate(), 'busi_admin', sysdate(), 'online verification interface authorization code', 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (167, 'exception health code push wechat official account parameter', 'abnormal.recog.push.weixin.params', '{}', 'N', 'admin', sysdate(), 'busi_admin', sysdate(), 'maintenance format is JSON, multi receiver phone number can be separated by comma, for example: {\"appId\":\"123\",\"templateId\":\"123\",\"phone\":\"13011112222,13011113333\"}', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (168, '203 Get Image Address', 'biapwp.url', 'https://cloud.eyecool.cn/api/device/adapter/getPersonFace?id=', 'N', 'admin', sysdate(), 'busi_admin', sysdate(), '', 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (169, 'open dingding data sync dingding APPKEY', 'dingtalk.syncdata.appkey', '-', 'N', 'admin', sysdate(), 'admin', NULL, '', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (170, 'SDK Upload Folder', 'sys.tool.sdkFile.dir', './eyecool/biapwp/tool/sdkFile/', 'N', 'admin', sysdate(), 'admin', sysdate(), NULL, 'N');



INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (100, 'Data Source', 'apply_data_source', '0', 'admin', sysdate(), '', NULL, 'data source');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (101, 'Personnel ID', 'apply_person_flag', '0', 'admin', sysdate(), '', NULL, 'personnel id');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (102, 'Encrypt Or Not', 'apply_encrypted', '0', 'admin', sysdate(), '', NULL, 'encrypt or not');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (103, 'Finger Code', 'bio_finger_code', '0', 'admin', sysdate(), '', NULL, 'finger code');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (104, 'ID Card Fingerprint', 'bio_idcard_finger', '0', 'admin', sysdate(), '', NULL, 'ID card fingerprint');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (105, 'Eye Code', 'bio_eye_code', '0', 'admin', sysdate(), '', NULL, 'eye code');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (106, 'Certificate Type', 'sys_cert_type', '0', 'admin', sysdate(), NULL, NULL, 'certificate type list');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (107, 'Certificate Image Type', 'cert_photo_type', '0', 'admin',  sysdate(), 'admin',  sysdate(), 'certificate image type');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (108, 'Nation', 'sys_nation', '0', 'admin',  sysdate(), '', NULL, 'nation');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (109, 'Biometric Information Image Type', 'bio_photo_type', '0', 'admin',  sysdate(), 'admin',  sysdate(), 'biometric information image type');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (110, 'Biometric Feature Open Status', 'bio_mode_status', '0', 'admin',  sysdate(), '', NULL, 'biometric feature open status');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (112, 'Channel 1-N Identification Mode', 'search_n_type', '0', 'admin',  sysdate(), 'admin',  sysdate(), '1-N identification mode');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (113, 'HTTP Transaction Interface', 'http_interface', '0', 'admin',  sysdate(), 'admin',  sysdate(), 'HTTP Transaction Interface');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (115, 'Channel Management Recognition Type', 'channel_bio_attest_type', '0', 'admin',  sysdate(), '', NULL, 'Channel Management Recognition Type');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (116, 'Channel Management Data Source', 'channel_business_source', '0', 'admin',  sysdate(), '', NULL, 'channel management data source');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (117, 'Biometric Recognition Result', 'bio_result', '0', 'admin',  sysdate(), 'admin',  sysdate(), 'Biometric Recognition Result');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (118, 'Biometric Feature1-N Log Scene Type', 'search_log_type', '0', 'admin',  sysdate(), 'admin',  sysdate(), 'Biometric Feature1-N Log Scene Type');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (121, 'Device Type', 'client_device_type', '0', 'admin',  sysdate(), '', NULL, 'device type');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (122, 'Device Upgrade Status', 'device_upgrade_status', '0', 'admin',  sysdate(), '', NULL, 'Device Upgrade Status');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (123, 'Device Upgrade Job Result', 'device_upgrade_result', '0', 'admin',  sysdate(), '', NULL, 'device upgrade job result');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (124, 'Message Notice Mode', 'msg_notice_method', '0', 'admin',  sysdate(), '', NULL, 'Message Notice Mode');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (125, 'Message Send Mdode', 'msg_type', '0', 'admin',  sysdate(), 'admin',  sysdate(), 'Message Send Mdode');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (126, 'Message Push Result', 'msg_result', '0', 'admin',  sysdate(), '', NULL, 'Message Push Result');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (127, 'Email Type', 'msg_mail_type', '0', 'admin',  sysdate(), '', NULL, 'Email type');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (128, 'dingding message type', 'msg_dingtalk_type', '0', 'admin',  sysdate(), '', NULL, 'dingding message type');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (129, 'dingding media file type', 'msg_dingtalk_media_type', '0', 'admin',  sysdate(), '', NULL, 'dingding media file type');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (130, 'Device Add Mode', 'device_create_method', '0', 'admin',  sysdate(), '', NULL, 'Device Add Mode');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (131, 'Device Online Status', 'device_online_state', '0', 'admin',  sysdate(), '', NULL, 'device online status');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (132, 'Device Action Log Type', 'device_action_type', '0', 'admin',  sysdate(), '', NULL, 'Device Action Log Type');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (133, 'Device Parameter Issue Result', 'device_config_result', '0', 'admin',  sysdate(), '', NULL, 'Device Parameter Issue Result');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (135, 'Area type', 'area_type', '0', 'admin',  sysdate(), 'admin', sysdate(), 'area type');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (137, 'Tenant Status', 'sys_tenant_state', '0', 'admin',  sysdate(), '', NULL, 'tenant status list');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (138, 'Tenant Source', 'sys_tenant_source', '0', 'admin',  sysdate(), '', NULL, 'tenant source list');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (139, 'Multi-modal Type', 'fusion_type', '0', 'admin',  sysdate(), 'admin',  sysdate(), 'multi-modal type');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (140, 'Face and Iris Multi-modal Comparison Mode', 'match_mode', '0', 'admin', sysdate(), 'admin',  sysdate(), 'Face and Iris Multi-modal Comparison Mode');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (143, 'Health Code Request Address', 'healthcode.request.url', '0', 'admin',  sysdate(), '', NULL, 'health code');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (144, 'SDK Type', 'sys_sdk_type', '0', 'admin', sysdate(), '', NULL, 'SDK file  type');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (145, 'Device In and Out Direction', 'device_direction', '0', 'admin', sysdate(), '', NULL, 'Device In and Out Direction');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (146, 'Tenant Type', 'sys_tenant_type', '0', 'admin', sysdate(), '', NULL, 'tenant type');



INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (100, 1, 'Internal Interface', 'INTERFACE', 'apply_data_source', '', 'success', 'Y', '0', 'admin',  sysdate(), 'admin',  sysdate(), 'data source - interface');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (101, 2, 'Data Import', 'IMP', 'apply_data_source', '', 'info', 'N', '0', 'admin',  sysdate(), 'admin',  sysdate(), 'data source - data import');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (102, 3, 'HTTP Interface', 'HTTP', 'apply_data_source', '', 'warning', 'N', '0', 'admin',  sysdate(), 'admin',  sysdate(), 'data source - HTTP');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (103, 1, 'Normal', '1', 'apply_person_flag', '', 'success', 'Y', '0', 'admin',  sysdate(), 'admin',  sysdate(), 'personnel flag - normal');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (104, 2, 'Red List', '2', 'apply_person_flag', '', 'primary', 'N', '0', 'admin',  sysdate(), 'admin',  sysdate(), 'personnel flag - red list');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (105, 3, 'Black List', '3', 'apply_person_flag', '', 'danger', 'N', '0', 'admin',  sysdate(), 'admin',  sysdate(), 'personnel flag - black list');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (106, 1, 'Encrypt', '1', 'apply_encrypted', '', 'primary', 'Y', '0', 'admin',  sysdate(), 'admin',  sysdate(), 'encrypt or not - encrypt');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (107, 2, 'Not Encrypt', '0', 'apply_encrypted', '', 'success', 'N', '0', 'admin',  sysdate(), 'admin',  sysdate(), 'Encrypt or not - not encrypt');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (167, 1, 'Left Thumb', '16', 'bio_finger_code', '', '', 'N', '0', 'admin', sysdate(), 'admin',  sysdate(), 'finger code-Left Thumb');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (168, 2, 'Left Index Finger', '17', 'bio_finger_code', '', '', 'N', '0', 'admin',  sysdate(), 'admin',  sysdate(), 'finger code-Left Index Finger');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (169, 3, 'Left Middle Finger', '18', 'bio_finger_code', '', '', 'N', '0', 'admin',  sysdate(), 'admin',  sysdate(), 'finger code-Left Middle Finger');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (170, 4, 'Left Ring Finger', '19', 'bio_finger_code', '', '', 'N', '0', 'admin',  sysdate(), 'admin', sysdate(), 'finger code-Left Ring Finger');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (171, 5, 'Left Little Finger', '20', 'bio_finger_code', '', '', 'N', '0', 'admin',  sysdate(), 'admin',  sysdate(), 'finger code-Left Little Finger');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (172, 6, 'Right Thumb', '11', 'bio_finger_code', '', '', 'N', '0', 'admin',  sysdate(), 'admin',  sysdate(), 'finger code-Right Thumb');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (173, 7, 'Right Index Finger', '12', 'bio_finger_code', '', '', 'Y', '0', 'admin',  sysdate(), 'admin',  sysdate(), 'finger code-Right Index Finger');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (174, 8, 'Right Middle Finger', '13', 'bio_finger_code', '', '', 'N', '0', 'admin',  sysdate(), 'admin',  sysdate(), 'finger code-Right Middle Finger');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (175, 9, 'Right Ring Finger', '14', 'bio_finger_code', '', '', 'N', '0', 'admin',  sysdate(), 'admin',  sysdate(), 'finger code-Right Ring Finger');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (176, 10, 'Right Little Finger', '15', 'bio_finger_code', '', '', 'N', '0', 'admin',  sysdate(), 'admin',  sysdate(), 'finger code-Right Little Finger');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (179, 1, 'ID Card fingerprintA', 'A', 'bio_idcard_finger', '', '', 'Y', '0', 'admin',  sysdate(), '', NULL, 'ID Card fingerprintA');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (180, 2, 'ID Card fingerprintB', 'B', 'bio_idcard_finger', '', '', 'N', '0', 'admin',  sysdate(), '', NULL, 'ID Card fingerprintB');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (181, 1, 'Left Eye', 'L', 'bio_eye_code', '', '', 'Y', '0', 'admin', sysdate(), '', NULL, 'Eye code-Left Eye');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (182, 2, 'Right Eye', 'R', 'bio_eye_code', '', '', 'N', '0', 'admin', sysdate(), '', NULL, 'Eye code-Right Eye');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (183, 1, 'Resident ID Card', '111', 'sys_cert_type', NULL, 'default', 'Y', '0', 'admin', sysdate(), NULL, NULL, 'certificate type-Resident ID Card');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (184, 2, 'Temporary Resident ID Card', '112', 'sys_cert_type', NULL, 'primary', 'N', '0', 'admin', sysdate(), NULL, NULL, 'certificate type-Temporary Resident ID Card');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (185, 3, 'Household Register', '113', 'sys_cert_type', NULL, 'success', 'N', '0', 'admin', sysdate(), NULL, NULL, 'certificate type-Household Register');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (186, 4, 'Military ID Card', '114', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', sysdate(), NULL, NULL, 'certificate type-Military ID Card');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (187, 5, 'Police Officer Card', '123', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', sysdate(), NULL, NULL, 'certificate type-Police Officer Card');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (188, 6, 'Student Card', '133', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', sysdate(), NULL, NULL, 'certificate type-Student Card');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (189, 7, 'Diplomatic Passport', '411', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', sysdate(), NULL, NULL, 'certificate type-Diplomatic Passport');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (190, 8, 'Service Passport', '412', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', sysdate(), NULL, NULL, 'certificate type-Service Passport');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (191, 9, 'Passport for Public Affairs', '413', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', sysdate(), NULL, NULL, 'certificate type-Passport for Public Affairs');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (192, 10, 'Passport', '414', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', sysdate(), NULL, NULL, 'certificate type-common passport');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (193, 11, 'Border Entry and Exit Pass', '416', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', sysdate(), NULL, NULL, 'certificate type-Border Entry and Exit Pass');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (194, 12, 'Foreigner Border Entry and Exit Pass', '417', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', sysdate(), NULL, NULL, 'certificate type-Foreigner Entry and Exit Pass');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (195, 13, 'Seaman\' s Book', '419', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', sysdate(), NULL, NULL, 'certificate type-Seaman\' s Book');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (196, 14, 'Mainland Travel Permit for Taiwan Residents', '511', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', sysdate(), NULL, NULL, 'certificate type-Mainland Travel Permit for Taiwan Residents');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (197, 15, 'Exit-Entry Permit for Travelling to and from Hong Kong and Macao', '513', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', sysdate(), NULL, NULL, 'certificate type-Exit-Entry Permit for Travelling to and from Hong Kong and Macao');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (198, 16, 'Reentry Permit', '516', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', sysdate(), NULL, NULL, 'certificate type-Reentry Permit');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (199, 17, 'Travel Permit for Mainlanders to Enter and Exit Taiwan', '517', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', sysdate(), NULL, NULL, 'certificate type-Travel Permit for Mainlanders to Enter and Exit Taiwan');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (200, 18, 'Entry Exit Permit for China-North Korea Border Area', '733', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', sysdate(), NULL, NULL, 'certificate type-Entry Exit Permit for China-North Korea Border Area');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (201, 19, 'Entry Exit Permit for China Mongolia Border Area', '736', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', sysdate(), NULL, NULL, 'certificate type-Entry Exit Permit for China Mongolia Border Area');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (202, 20, 'Entry Exit Permit for China Myanmar Border Area', '738', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', sysdate(), NULL, NULL, 'certificate type-Entry Exit Permit for China Myanmar Border Area');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (203, 21, 'Entry and Exit Permit for Overseas Border People in Border Areas of Yunnan Province', '740', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', sysdate(), NULL, NULL, 'certificate type-Entry and Exit Permit for Overseas Border People in Border Areas of Yunnan Province');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (204, 22, 'Entry Exit Permit for China Nepal Border Area', '741', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', sysdate(), NULL, NULL, 'certificate type-Entry Exit Permit for China Nepal Border Area');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (205, 23, 'Entry Exit Permit for China Vietnam Border Area', '743', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', sysdate(), NULL, NULL, 'certificate type-Entry Exit Permit for China Vietnam Border Area');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (206, 24, 'Entry Exit Permit for China Laos Border Area', '745', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', sysdate(), NULL, NULL, 'certificate type-Entry Exit Permit for China Laos Border Area');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (207, 25, 'Entry Exit Permit for China India Border Area', '747', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', sysdate(), NULL, NULL, 'certificate type-Entry Exit Permit for China India Border Area');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (208, 26, 'Other', '990', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', sysdate(), NULL, NULL, 'certificate type-other');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (209, 27, 'Military Card', '991', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', sysdate(), NULL, NULL, 'certificate type-Military Card');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (210, 28, 'Hong Kong, Macao and Taiwan Residence Permit', '992', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', sysdate(), NULL, NULL, 'certificate type-Hong Kong, Macao and Taiwan Residence Permit');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (211, 29, 'Alien Residence Permit', '993', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', sysdate(), NULL, NULL, 'certificate type-Alien Residence Permit');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (212, 30, 'Vietnam ID Card', '994', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', sysdate(), NULL, NULL, 'certificate type-Vietnam ID Card');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (213, 31, 'Border Resident Certificate', '995', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', sysdate(), NULL, NULL, 'certificate type-Border Resident Certificate');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (214, 32, 'No Certificate', '999', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin',sysdate(), NULL, NULL, 'certificate type-No Certificate');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (215, 2, 'Enter School Image', '1', 'cert_photo_type', '', '', 'N', '0', 'admin', sysdate(), 'admin', sysdate(), 'certificate image type-Enter School Image');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (216, 3, 'In School Image', '2', 'cert_photo_type', '', '', 'N', '0', 'admin', sysdate(), 'admin', sysdate(), 'certificate image type-In School Image');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (217, 4, 'Graduation Image', '3', 'cert_photo_type', '', '', 'N', '0', 'admin', sysdate(), 'admin', sysdate(), 'certificate image type-Graduation Image');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (218, 1, 'the Han Nationality', '01', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), 'nation');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (219, 2, 'the Mongolian Nationality', '02', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), 'nation');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (220, 3, 'the Hui Nationality', '03', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), 'nation');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (221, 4, 'the Tibetan Nationality', '04', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), 'nation');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (222, 5, 'the Uygur Nationality', '05', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), 'nation');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (223, 6, 'the Miao Nationality', '06', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), 'nation');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (224, 7, 'the Yi nationality', '07', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), 'nation');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (225, 8, 'the Zhuang Nationality ', '08', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), 'nation');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (226, 9, 'the Bouyei Nationality', '09', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), 'nation');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (227, 10, 'the Korean Nationality', '10', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), 'nation');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (228, 11, 'the Manchu Nationality', '11', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), 'nation');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (229, 12, 'the Dong Nationality', '12', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), 'nation');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (230, 13, 'the Yao Nationality', '13', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), 'nation');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (231, 14, 'the Bai Nationality', '14', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), 'nation');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (232, 15, 'the Tujia Nationality', '15', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), 'nation');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (233, 16, 'the Hani Nationality', '16', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), 'nation');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (234, 17, 'the Kazak Nationality', '17', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), 'nation');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (235, 18, 'the Dai Nationality', '18', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), 'nation');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (236, 19, 'the Li Nationality', '19', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), 'nation');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (237, 20, 'the Lisu Nationality', '20', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), 'nation');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (238, 21, 'the Wa Nationality', '21', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), 'nation');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (239, 22, 'the She Nationality', '22', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), 'nation');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (240, 23, 'the Gaoshan Nationality', '23', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), 'nation');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (241, 24, 'the Lahu Nationality', '24', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), 'nation');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (242, 25, 'the Shui Nationality', '25', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), 'nation');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (243, 26, 'the Dongxiang Nationality', '26', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), 'nation');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (244, 27, 'the Naxi Nationality', '27', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), 'nation');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (245, 28, 'the Jingpo Nationality', '28', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), 'nation');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (246, 29, 'the Kirgiz Nationality', '29', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), 'nation');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (247, 30, 'the Tu Nationality', '30', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), 'nation');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (248, 31, 'the Daur Nationality', '31', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), 'nation');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (249, 32, 'the Mulao Nationality', '32', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), 'nation');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (250, 33, 'the Qiang Nationality', '33', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), 'nation');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (251, 34, 'the Braun Nationality', '34', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), 'nation');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (252, 35, 'the Salar Nationality', '35', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), 'nation');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (253, 36, 'the Maonan Nationality', '36', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), 'nation');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (254, 37, 'the Gelao Nationality', '37', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), 'nation');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (255, 38, 'the Xibo Nationality', '38', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), 'nation');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (256, 39, 'the Achang Nationality', '39', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), 'nation');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (257, 40, 'the Pumi Nationality', '40', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), 'nation');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (258, 41, 'the Tajik Nationality', '41', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), 'nation');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (259, 42, 'the Nu Nationality', '42', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), 'nation');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (260, 43, 'the Uzbek Nationality', '43', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), 'nation');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (261, 44, 'the Russian Nationality', '44', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), 'nation');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (262, 45, 'the Ewenki Nationality', '45', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), 'nation');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (263, 46, 'the De\'ang Nationality', '46', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), 'nation');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (264, 47, 'the Baoan Nationality', '47', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), 'nation');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (265, 48, 'the Yugur Nationality', '48', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), 'nation');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (266, 49, 'the Jing Nationality', '49', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), 'nation');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (267, 50, 'the Tatar Nationality', '50', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), 'nation');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (268, 51, 'the Dulong Nationality', '51', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), 'nation');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (269, 52, 'the Oroqen Nationality', '52', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), 'nation');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (270, 53, 'the Hezhen Nationality', '53', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), 'nation');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (271, 54, 'the Menba Nationality', '54', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), 'nation');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (272, 55, 'the Lhoba Nationality', '55', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), 'nation');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (273, 56, 'the Jino Nationality', '56', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), 'nation');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (274, 57, 'the Chuanqing Nationality', '81', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), 'nation');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (275, 58, 'Other', '97', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), 'nation');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (276, 59, 'Foreign Origin', '98', 'sys_nation', NULL, 'info', 'N', '0', 'admin', sysdate(), 'ry', sysdate(), 'nation');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (277, 1, 'Face Image', '1', 'bio_photo_type', NULL, NULL, 'Y', '0', 'admin', sysdate(), '', NULL, 'biometric information image type-face image');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (278, 2, 'Fingerprint Image', '2', 'bio_photo_type', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'biometric information image type- fingerprint image');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (279, 3, 'Iris image', '3', 'bio_photo_type', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'biometric information image type-iris image');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (280, 1, 'Open', '1', 'bio_mode_status', NULL, 'primary', 'Y', '0', 'admin', sysdate(), '', NULL, 'biometric feature open status-open');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (281, 2, 'Not Open', '0', 'bio_mode_status', NULL, 'danger', 'N', '0', 'admin', sysdate(), '', NULL, 'biometric feature open status-not open');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (285, 1, 'Check Sub-database', '1', 'search_n_type', '', 'primary', 'N', '0', 'admin', sysdate(), 'admin', sysdate(), '1-N identification mode-check sub-database');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (286, 2, 'Check Channel Database', '2', 'search_n_type', '', 'warning', 'Y', '0', 'admin', sysdate(), 'admin', sysdate(), '1-N identification mode-check channel database');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (287, 3, 'Check the Whole Database', '3', 'search_n_type', '', 'success', 'N', '0', 'admin', sysdate(), 'admin', sysdate(), '1-N identification mode-check the whole database');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (288, 1, 'Add Personnel Information', 'PERSON_INFO_INSERT', 'http_interface', '', '', 'Y', '0', 'admin', sysdate(), 'admin', sysdate(), 'HTTP transaction interface-add personnel information');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (291, 1, 'Face', '0', 'channel_bio_attest_type', '', 'info', 'Y', '0', 'admin', sysdate(), 'admin', sysdate(), 'face');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (292, 2, 'Fingerprint', '1', 'channel_bio_attest_type', NULL, 'info', 'Y', '0', 'admin', sysdate(), '', NULL, 'fingerprint');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (293, 3, 'Iris', '2', 'channel_bio_attest_type', NULL, 'info', 'Y', '0', 'admin', sysdate(), '', NULL, 'iris');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (294, 4, 'Fingervein', '3', 'channel_bio_attest_type', NULL, 'info', 'Y', '0', 'admin', sysdate(), '', NULL, 'fingervein');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (295, 1, 'Interface', 'INTERFACE', 'channel_business_source', NULL, 'info', 'Y', '0', 'admin', sysdate(), '', NULL, 'interface');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (296, 2, 'Import', 'IMP', 'channel_business_source', NULL, 'info', 'Y', '0', 'admin', sysdate(), '', NULL, 'import');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (297, 3, 'HTTP Interface', 'HTTP', 'channel_business_source', NULL, 'info', 'Y', '0', 'admin', sysdate(), '', NULL, 'HTTP interface');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (298, 2, 'Updator information', 'PERSON_INFO_UPDATE', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP transaction interface-updator information');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (299, 3, 'Delete Personnel Information', 'PERSON_INFO_DELETE', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP transaction interface-delete personnel information');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (300, 4, 'Query Personnel Information', 'PERSON_INFO_SELECT', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP transaction interface-query personnel information');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (301, 11, 'Unknown Right Finger', '97', 'bio_finger_code', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'finger code--Unknown Right Finger');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (302, 12, 'Unknown Left Finger', '98', 'bio_finger_code', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'finger code--Unknown Left Finger');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (303, 13, 'Other Unknown Finger', '99', 'bio_finger_code', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'finger code--Other Unknown Finger');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (305, 1, 'Certificate Image', '0', 'cert_photo_type', NULL, NULL, 'Y', '0', 'admin', sysdate(), '', NULL, 'certificate image type--certificate image');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (306, 5, 'Open Face Function', 'PERSON_FACE_OPEN', 'http_interface', '', '', 'N', '0', 'admin', sysdate(), 'admin', sysdate(), 'HTTP transaction interface--open face');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (307, 6, 'Close Face Function', 'PERSON_FACE_CLOSE', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP transaction interface--close face');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (308, 7, 'Face Recognition(1:N)', 'PERSON_FACE_RECOG', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP transaction interface--face recognition(1:N)');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (309, 8, 'Face Identification(1:1)', 'PERSON_FACE_VERIFY', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP transaction interface--face identification(1:1)');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (310, 9, 'Open Fingerprint Function', 'PERSON_FINGER_OPEN', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP transaction interface--open  fingerprint');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (311, 10, 'Close Fingerprint Function', 'PERSON_FINGER_CLOSE', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP transaction interface--close fingerprint');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (312, 11, 'Fingerprint Recognition(1:N)', 'PERSON_FINGER_RECOG', 'http_interface', '', '', 'N', '0', 'admin', sysdate(), 'admin', sysdate(), 'HTTP transaction interface-- fingerprint recognition(1:N)');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (313, 12, 'Fingerprint Identification(1:1)', 'PERSON_FINGER_VERIFY', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP transaction interface-- fingerprint identification(1:1)');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (314, 13, 'Open Iris Function', 'PERSON_IRIS_OPEN', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP transaction interface--open iris');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (315, 14, 'Close Iris Function', 'PERSON_IRIS_CLOSE', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP transaction interface--close iris');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (316, 15, 'Iris Recognition(1:N)', 'PERSON_IRIS_RECOG', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP transaction interface--iris recognition(1:N)');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (317, 16, 'Iris Identification(1:1)', 'PERSON_IRIS_VERIFY', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP transaction interface--iris identification(1:1)');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (318, 17, 'Open Fingervein Function', 'PERSON_FVEIN_OPEN', 'http_interface', '', '', 'N', '0', 'admin', sysdate(), 'admin', sysdate(), 'HTTP transaction interface--open fingervein');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (319, 18, 'Close Fingervein Function', 'PERSON_FVEIN_CLOSE', 'http_interface', '', '', 'N', '0', 'admin', sysdate(), 'admin', sysdate(), 'HTTP transaction interface--close fingervein');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (320, 19, 'Fingervein Recognition(1:N)', 'PERSON_FVEIN_RECOG', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP transaction interface--fingervein recognition(1:N)');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (321, 20, 'Fingervein Identification(1:1)', 'PERSON_FVEIN_VERIFY', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP transaction interface--fingervein identification(1:1)');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (322, 21, 'Short Message Push Function', 'MESSAGE_SEND_SMS', 'http_interface', '', '', 'N', '0', 'admin', sysdate(), 'admin', sysdate(), 'HTTP transaction interface-- short message push');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (323, 22, 'Wechat Service Account Push', 'MESSAGE_SEND_WECHAT', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP transaction interface--Wechat Service Account Push');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (324, 23, 'Email Push function', 'MESSAGE_SEND_EMAIL', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP transaction interface-- Email push function');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (325, 1, 'Pass', '0', 'bio_result', '', 'primary', 'Y', '0', 'admin', sysdate(), 'admin', sysdate(), 'biometric identification recognition result--pass');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (326, 2, 'Failed to pass', '1', 'bio_result', '', 'danger', 'N', '0', 'admin', sysdate(), 'admin', sysdate(), 'biometricidentificationrecognition result--not pass');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (327, 1, '1:N for Basic Information Registered into Database', '1', 'search_log_type', '', 'success', 'N', '0', 'admin', sysdate(), 'admin', sysdate(), 'biometric feature1-N log scene type--1:N for Basic Information Registered into Database');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (328, 2, 'Comparison Search Interface 1:N', '2', 'search_log_type', '', 'primary', 'Y', '0', 'admin', sysdate(), 'admin', sysdate(), 'biometric feature 1-N log scene type--1:N comparison and search interface');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (331, 24, 'Personnel Data Real-time Update', 'PERSON_LIVE_UPDATE', 'http_interface', '', '', 'N', '0', 'admin', sysdate(), 'admin', sysdate(), 'HTTP transaction interface--personnel data real-time update ');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (332, 4, 'Check in Turn', '4', 'search_n_type', '', 'danger', 'N', '0', 'admin', sysdate(), 'admin', sysdate(), 'query in turn according to sub-database->channel database->the whole database order,return immediately when get results');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (333, 3, 'Subsystem Log Postback', '3', 'search_log_type', '', 'warning', 'N', '0', 'admin', sysdate(), 'admin', sysdate(), 'biometric feature 1-N log scene type--subsystem log postback');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (334, 25, 'Face Recognition Log Postback', 'PERSON_FACE_SEARCH_LOG_BAK', 'http_interface', '', '', 'N', '0', 'admin', sysdate(), 'admin', sysdate(), 'HTTP transaction interface--face recognition log postback');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (335, 26, 'Get Face Feature', 'PERSON_FACE_FEATURE', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP transaction interface--get face feature');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (336, 27, 'Sub-database Operation Function', 'PERSON_SUB_TREASURY_OPERATE', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP transaction interface--sub-database operation function');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (340, 1, 'To Be Downloaded', '1', 'device_upgrade_status', '', 'info', 'Y', '0', 'admin', sysdate(), 'admin', sysdate(), 'device upgrade status--to be downloaded');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (341, 2, 'To Be Updated', '2', 'device_upgrade_status', '', 'primary', 'N', '0', 'admin', sysdate(), 'admin', sysdate(), 'device upgrade status--to be updated');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (342, 3, 'Update Successfully', '3', 'device_upgrade_status', NULL, 'success', 'N', '0', 'admin', sysdate(), '', NULL, 'device upgrade status--update successfully');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (343, 4, 'Failed to Update', '4', 'device_upgrade_status', NULL, 'danger', 'N', '0', 'admin', sysdate(), '', NULL, 'device upgrade status--failed to update');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (344, 5, 'Skip', '5', 'device_upgrade_status', NULL, 'warning', 'N', '0', 'admin', sysdate(), '', NULL, 'device upgrade status--Skip');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (346, 29, 'Device Version Download Interface', 'CLIENT_VERSION_DOWNLOAD', 'http_interface', '', '', 'N', '0', 'admin', sysdate(), 'admin', sysdate(), 'HTTP transaction interface--device version download interface');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (348, 1, 'To Be Executed', '1', 'device_upgrade_result', '', 'info', 'Y', '0', 'admin', sysdate(), 'admin', sysdate(), 'device upgrade job result--to be executed');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (349, 2, 'Succeed', '2', 'device_upgrade_result', NULL, 'success', 'Y', '0', 'admin', sysdate(), '', NULL, 'device upgrade job result--succeed');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (350, 3, 'Fail', '3', 'device_upgrade_result', NULL, 'danger', 'Y', '0', 'admin', sysdate(), '', NULL, 'device upgrade job result--fail');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (351, 4, 'Skip', '4', 'device_upgrade_result', NULL, 'warning', 'Y', '0', 'admin', sysdate(), '', NULL, 'device upgrade job result--Skip');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (352, 1, 'Short Message', '1', 'msg_notice_method', '', 'info', 'Y', '0', 'admin', sysdate(), 'admin', sysdate(), 'message notice mode-- short message');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (353, 2, 'Email', '2', 'msg_notice_method', NULL, 'success', 'N', '0', 'admin', sysdate(), '', NULL, 'message notice mode-- Email');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (354, 3, 'Wechat', '3', 'msg_notice_method', '', 'primary', 'N', '0', 'admin', sysdate(), 'admin', sysdate(), 'message notice mode--wechat');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (355, 4, 'Dingding', '4', 'msg_notice_method', NULL, 'warning', 'N', '0', 'admin', sysdate(), '', NULL, 'message notice mode--dingding');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (356, 1, 'Common Transmation', '1', 'msg_type', '', 'primary', 'Y', '0', 'admin', sysdate(), 'admin', sysdate(), 'message type--Common Transmission');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (357, 2, 'Template Transmission', '2', 'msg_type', '', 'success', 'N', '0', 'admin', sysdate(), 'admin', sysdate(), 'message type--Template Transmission');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (358, 1, 'Succeed', '0', 'msg_result', '', 'primary', 'Y', '0', 'admin', sysdate(), 'admin', sysdate(), 'message push result--Succeed');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (359, 2, 'Fail', '1', 'msg_result', NULL, 'danger', 'N', '0', 'admin', sysdate(), '', NULL, 'message push result--Fail');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (360, 1, 'Text Email', '1', 'msg_mail_type', NULL, 'success', 'Y', '0', 'admin', sysdate(), '', NULL, 'Email type--text Email');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (361, 2, 'HTML Email', '2', 'msg_mail_type', NULL, 'warning', 'N', '0', 'admin', sysdate(), '', NULL, 'Email type--HTML Email');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (362, 1, 'Text Message', 'text', 'msg_dingtalk_type', '', 'primary', 'Y', '0', 'admin', sysdate(), 'admin', sysdate(), 'dingding message type--text message');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (363, 2, 'Markdown Message', 'markdown', 'msg_dingtalk_type', NULL, 'success', 'N', '0', 'admin', sysdate(), '', NULL, 'dingding message type--Markdown message');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (364, 3, 'Link Message', 'link', 'msg_dingtalk_type', NULL, 'warning', 'N', '0', 'admin', sysdate(), '', NULL, 'dingding message type--link message');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (365, 1, 'Image File', 'image', 'msg_dingtalk_media_type', '', 'success', 'Y', '0', 'admin', sysdate(), 'admin', sysdate(), 'dingding media file type-- image file');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (366, 2, 'Voice File', 'voice', 'msg_dingtalk_media_type', NULL, 'primary', 'N', '0', 'admin', sysdate(), '', NULL, 'dingding media file type--voice file');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (367, 3, 'Common File', 'file', 'msg_dingtalk_media_type', NULL, 'warning', 'N', '0', 'admin', sysdate(), '', NULL, 'dingding media file type--common file');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (368, 4, 'Image  Message', 'image', 'msg_dingtalk_type', NULL, 'info', 'N', '0', 'admin', sysdate(), '', NULL, 'dingding  message type-- image message');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (369, 5, 'Voice  Message', 'voice', 'msg_dingtalk_type', NULL, 'success', 'N', '0', 'admin', sysdate(), '', NULL, 'dingding  message type--voice message');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (370, 6, 'Common File Message', 'file', 'msg_dingtalk_type', NULL, 'success', 'N', '0', 'admin', sysdate(), '', NULL, 'dingding  message type--common file message');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (371, 31, 'Dingding Push Function', 'MESSAGE_SEND_DING', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP transaction interface--dingding push function');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (372, 32, 'Query Dingding Push Result', 'MESSAGE_SEND_DING_RESULT', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP transaction interface--dingding push result query');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (373, 33, 'Upload Dingding Media File', 'MESSAGE_DING_MEDIA_UPLOAD', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP transaction interface--dingding media file upload');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (374, 4, 'Sync in Real Time', 'LIVEUPDATE', 'apply_data_source', NULL, 'primary', 'N', '0', 'admin', sysdate(), '', NULL, 'data source-- data sync in real time');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (376, 35, 'Delete Channel Database Personnel', 'DEL_CHANNEL_PERSON', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP transaction interface--delete channel database personnel');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (377, 36, 'Query Personnel Exists in Channel Database or Not', 'QUERY_CHANNEL_PERSON_EXISTS', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP transaction interface--query Personnel Exists in Channel Database or Not');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (378, 5, 'Trilateral Sync', 'SYNC', 'apply_data_source', NULL, 'danger', 'N', '0', 'admin', sysdate(), '', NULL, 'data source--Trilateral Sync');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (379, 1, 'Create in Backend', '1', 'device_create_method', NULL, 'primary', 'N', '0', 'admin', sysdate(), '', NULL, 'device add mode--Create in Backend');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (380, 2, 'Autonomously Register', '2', 'device_create_method', NULL, 'warning', 'N', '0', 'admin', sysdate(), '', NULL, 'device add mode--Autonomously Register');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (381, 1, 'Online', '1', 'device_online_state', NULL, 'primary', 'N', '0', 'admin', sysdate(), '', NULL, 'device online status--online');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (382, 2, 'Offline', '2', 'device_online_state', NULL, 'danger', 'N', '0', 'admin', sysdate(), '', NULL, 'device online status--offline');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (383, 3, 'Import in Backend', '3', 'device_create_method', NULL, 'danger', 'N', '0', 'admin', sysdate(), '', NULL, 'device add mode--Import in Backend');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (384, 1, 'Register', '1', 'device_action_type', '', 'success', 'N', '0', 'admin', sysdate(), 'admin', sysdate(), 'device action log type--Register');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (385, 2, 'Login', '2', 'device_action_type', '', 'primary', 'N', '0', 'admin', sysdate(), 'admin', sysdate(), 'device action log type--login');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (386, 3, 'Log Out', '3', 'device_action_type', NULL, 'danger', 'N', '0', 'admin', sysdate(), '', NULL, 'device action log type--log out');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (387, 4, 'Update', '4', 'device_action_type', NULL, 'warning', 'N', '0', 'admin', sysdate(), '', NULL, 'device action log type--update');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (389, 1, 'Succeed', '0', 'device_config_result', NULL, 'primary', 'N', '0', 'admin', sysdate(), '', NULL, 'device parameter issue result--succeed');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (390, 2, 'Fail', '1', 'device_config_result', '', 'danger', 'N', '0', 'admin', sysdate(), 'admin', sysdate(), 'device parameter issue result--fail');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (391, 38, 'Get Fingerprint Feature', 'PERSON_FINGER_FEATURE', 'http_interface', '', '', 'N', '0', 'admin', sysdate(), 'admin', sysdate(), 'HTTP transaction interface--get fingerprint feature');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (392, 39, 'Get Iris Feature', 'PERSON_IRIS_FEATURE', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP transaction interface--get iris feature');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (393, 40, 'Face Image Comparison', 'PERSON_FACE_IMG_COMPARE', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP transaction interface--face image comparison');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (394, 41, 'Fingerprint Image Comparison', 'PERSON_FINGER_IMG_COMPARE', 'http_interface', '', '', 'N', '0', 'admin', sysdate(), 'admin', sysdate(), 'HTTP transaction interface-- fingerprint image comparison');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (395, 42, 'Iris Image Comparison', 'PERSON_IRIS_IMG_COMPARE', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP transaction interface--iris image comparison');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (396, 43, 'Face Image or Video Checklive', 'PERSON_FACE_CHECKLIVE', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP transaction interface--face image or video checklive');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (397, 44, 'Face Image Quality Check', 'PERSON_FACE_QUALITY_DETECT', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP transaction interface--face image quality check');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (398, 45, 'Fingerprint Image Quality Check', 'PERSON_FINGER_QUALITY_DETECT', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP transaction interface-- fingerprint image quality check');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (409, 1, 'Normal', 'NORMAL', 'sys_tenant_state', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'tenant status--normal');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (410, 2, 'Frozen', 'FROZEN', 'sys_tenant_state', NULL, NULL, 'N', '0', 'admin', sysdate(), 'admin', NULL, 'tenant status--frozen');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (411, 1, 'User Registration', 'REGIST', 'sys_tenant_source', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'tenant source--user registration');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (412, 2, 'Create in Backend', 'BG_CREATE', 'sys_tenant_source', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'tenant source--create in backend');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (413, 1, 'Iris and Face', '1', 'fusion_type', NULL, 'primary', 'Y', '0', 'admin', sysdate(), '', NULL, 'multi-modal');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (414, 2, 'Fingerprint and Fingervein', '2', 'fusion_type', NULL, 'warning', 'N', '0', 'admin', sysdate(), '', NULL, 'multi-modal');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (415, 1, 'Iris Comparison', '0', 'match_mode', NULL, 'primary', 'Y', '0', 'admin', sysdate(), '', NULL, 'multi-modal');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (416, 2, 'Face Comparison', '1', 'match_mode', NULL, 'warning', 'N', '0', 'admin', sysdate(), '', NULL, 'multi-modal');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (417, 3, 'Iris and Face Comparison', '2', 'match_mode', NULL, 'warning', 'N', '0', 'admin', sysdate(), '', NULL, 'multi-modal');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (418, 4, 'Iris or Face Comparison', '3', 'match_mode', NULL, 'warning', 'N', '0', 'admin', sysdate(), '', NULL, 'multi-modal');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (419, 5, 'Multi-modal Comparison', '4', 'match_mode', NULL, 'warning', 'N', '0', 'admin', sysdate(), '', NULL, 'multi-modal');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (424, 46, 'Face and Video Checklive and Comparison', 'PERSON_FACE_CHECKLIVE_AND_COMPARE', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP transaction interface--face and video checklive comparison');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (425, 47, 'OCR Recognition Interface', 'OCR', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', sysdate(), 'HTTP transaction interface--OCR recognition interface');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (426, 48, 'Iris and Face Multi-modal Registration', 'MULIT_IRIS_FACE_REGISTER', 'http_interface', '', 'default', 'Y', '0', 'admin', sysdate(), 'admin', sysdate(), 'http standard interface- multi-modal face and iris registration');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (427, 49, 'Iris and Face Multi-modal 1vN', 'MULIT_IRIS_FACE_SEARCH', 'http_interface', NULL, 'default', 'Y', '0', 'admin', sysdate(), 'admin', sysdate(), 'http standard interface- multi-modal face and iris search');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (428, 50, 'Sub-database Operation', 'SUB_TREASURY_OPERATE', 'http_interface', NULL, NULL, 'Y', '0', 'admin', sysdate(), '', NULL, NULL);
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (429, 51, 'Device Query', 'QUREY_DEVICE', 'http_interface', NULL, NULL, 'Y', '0', 'admin', sysdate(), '', NULL, 'tax device query');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (430, 52, 'Face Recognition Log Query', 'PERSON_FACE_SEARCH_LOG_QUERY', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP interface--face recognition log query');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (431, 1, 'Shandong', 'https://jd.51yanhome.com:18088/api/v1/ss/check/in', 'healthcode.request.url', '', 'primary', 'Y', '0', 'admin', sysdate(), 'admin', sysdate(), 'health code');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (432, 49, 'Health Code Query', 'HEALTH_CODE_SEARCH', 'http_interface', NULL, 'default', 'Y', '0', 'admin', sysdate(), 'admin', sysdate(), 'http standard interface-health code query');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (433, 53, 'Open Face and Iris Multi-modal', 'PERSON_FACE_IRIS_OPEN', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP transaction interface--open face and iris multi-modal');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (434, 54, 'Close Face and Iris Multi-modal', 'PERSON_FACE_IRIS_CLOSE', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP transaction interface--close face and iris multi-modal');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (435, 55, 'Face 1v1 Comparison Log Postback', 'PERSON_FACE_MATCH_LOG_BAK', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP transaction interface--face 1v1 comparison log postback');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (436, 56, 'Face and Iris Multi-modal Recognition Log Postback', 'PERSON_FACE_IRIS_MULTI_LOG_BAK', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP transaction interface--face and iris multi-modal recognition log postback');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (437, 1, 'Face SDK', 'FACE', 'sys_sdk_type', NULL, 'success', 'Y', '0', 'admin', sysdate(), '', NULL, 'SDK type--face SDK');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (438, 2, 'Fingerprint SDK', 'FINGER', 'sys_sdk_type', NULL, 'success', 'Y', '0', 'admin', sysdate(), '', NULL, 'SDK type-- fingerprint SDK');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (439, 3, 'Iris SDK', 'IRIS', 'sys_sdk_type', NULL, 'success', 'Y', '0', 'admin', sysdate(), '', NULL, 'SDK type--iris SDK');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (440, 4, 'Fingervein SDK', 'FVEIN', 'sys_sdk_type', NULL, 'success', 'Y', '0', 'admin', sysdate(), '', NULL, 'SDK type--fingervein SDK');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (441, 5, 'Multi-modal SDK', 'MULTI', 'sys_sdk_type', NULL, 'success', 'Y', '0', 'admin', sysdate(), '', NULL, 'SDK type--multi-modal SDK');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (442, 57, 'Face and Iris Multi-modal Recognition Log Query', 'PERSON_FACE_IRIS_MULTI_LOG_QUERY', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP interface--face and iris multi-modal recognition log query');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (444, 1, 'Conventional Device', '1', 'client_device_type', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'device type--Conventional device');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (445, 2, 'Non Inductive Device', '2', 'client_device_type', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'device type--Non Inductive device');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (446, 3, 'Falling Object Device', '3', 'client_device_type', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'device type--Falling Object device');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (448, 59, 'Online Verification Interface', 'PERSON_IDENTITY_VERIFICATION', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP interface--Online Verification interface');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (449, 4, 'Check Attendance Device', '4', 'client_device_type', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'device type--Check Attendance device');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (450, 1, 'In', 'IN', 'device_direction', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'device in and out direction--in');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (451, 2, 'Out', 'OUT', 'device_direction', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'device in and out direction--out');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (452, 3, 'Unknown', 'UNKNOWN', 'device_direction', NULL, NULL, 'N', '0', 'admin', sysdate(), 'admin', sysdate(), 'device in and out direction--unknown');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (453, 60, 'Face 1v1 Comparison Log Query', 'PERSON_FACE_MATCH_LOG_QUERY', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'HTTP transaction interface--face 1v1 comparison log query');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (454, 5, 'Face and Iris Multi-modal', '4', 'channel_bio_attest_type', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, 'face and iris multi-modal');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (455, 0, 'Trial', 'TRIAL', 'sys_tenant_type', NULL, NULL, 'N', '0', 'admin', sysdate(), 'admin', sysdate(), 'tenant type--trial tenant');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (456, 1, 'Formal', 'FORMAL', 'sys_tenant_type', NULL, NULL, 'N', '0', 'admin', sysdate(), 'admin', sysdate(), 'tenant type--formal tenant');



INSERT INTO device_model(id, model_code, model_name, exterior_image, model_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355692391', 'ECF201', 'ECF201', NULL, 'face device', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_model(id, model_code, model_name, exterior_image, model_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355692392', 'ECF203', 'ECF203', NULL, 'face device', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_model(id, model_code, model_name, exterior_image, model_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355692393', 'ECX332', 'ECX332', NULL, 'face and iris multi-modal device', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_model(id, model_code, model_name, exterior_image, model_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355692394', 'ECX333', 'ECX333', NULL, 'face and iris multi-modal device with temperature measurement and card swiping', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_model(id, model_code, model_name, exterior_image, model_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355692395', 'ECF106', 'ECF106', NULL, 'Network snapshot machine', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_model(id, model_code, model_name, exterior_image, model_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355692396', 'Atlas500', 'Atlas500', NULL, 'Atlas500', NULL, NULL, sysdate(), NULL, 'super');


INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691327', 'appManagePwd', 'APP Management Password', 'APP management Password', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691328', 'keyAlgorithm', 'Algorithm Activation Code', 'algorithm Activation Code', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691329', 'httpBaseUrl', 'Interface Address', ' interface address', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691330', 'appKey', 'appKey', 'appKey', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691331', 'appSecret', 'appSecret', 'appSecret', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691332', 'workMode', ' Recognition Mode', 'recognition mode(0: intelligent model, 1: face mode, 2: ID card mode, 3: measure temperature only)', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691334', 'temperatureCheck', 'Temperature Measurement Switch', 'temperature measurement switch(true: open temperature measurement, false: close)', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691335', 'liveCheck', 'Checklive Switch', ' checklive switch(true: open checklive, false: close checklive)', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691336', 'matchScore', 'Comparison Threshold', 'comparison threshold(RANGE 0-100)', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691337', 'repeatTime', 'Deduplicattion Time', 'Deduplicattion time(range 3-12)', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691338', 'maxFaceSize', 'the Max Face', 'the max face', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691339', 'minFaceSize', 'the Minimum Face', 'the minimum face', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691340', 'idVerificationCheck', 'Verification between Personnel and ID Card Switch', 'Verification between Personnel and ID Card Switch(true: open verification, false: close verification)', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691341', 'personIdcardThreshold', 'Verification between Personnel and ID Card Threshold', 'Verification between Personnel and ID Card threshold(range 10-85)', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691342', 'healthCodeCheck', 'Health Code Verification Switch', 'Health Code Verification Switch(true: open health code Verification, false: not verify health code)', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691343', 'comparisonLib', 'Whether to Recognize Personnel in Database Only', 'Whether to Recognize Personnel in Database Only(true: recognize personnel in database only, false: recognize all the personnel', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691344', 'healthDeviceCode', 'Health Code Device Code', 'health code device code', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691345', 'healthAddress', 'Health Code Address', 'health code address', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691348', 'deviceStatus', 'Device Status', 'device status(0: forbidden,1: open)', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691349', 'irisMatchScore', 'Iris Comparison Threshold', 'iris 1:N comparison threshold', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691350', 'faceMatchScore', 'Face Comparison Threshold', 'face 1:N comparison threshold', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691351', 'multiMatchScore', 'Multi-modal Comparison Threshold', 'multi-modal 1:N comparison threshold', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691352', 'multiScore', 'Multi-modal Score', 'multi-modal score', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691354', 'matchMode', 'Comparison Mode', 'comparison mode(0: backend comparison, 1: local comparison, 2: second comparison)', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691355', 'matchType', 'Comparison Mode', ' comparison mode(0: iris comparison only, 1: face comparison only, 2: compare iris and face Simultaneously, 3: face or iris, 4: multi-modal)', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691356', 'frameSkip', 'RTSP Parse Skip Frame', 'RTSP Parse Skip Frame', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691357', 'platformSyncFlag', 'Log Upload Flag', ' log upload flag(0:real-time, 1:uniform speed, 2:close)', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691358', 'platformSyncTime', 'Log Upload Interval Time at a Uniform Speed', 'Log Upload Interval Time at a Uniform Speed', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691359', 'streamUrl', 'RTSP Address', 'RTSP address', NULL, NULL, sysdate(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691360', 'timeUrl', ' Time Server', ' Time Server(0:NTP server, 1:Self-built API)', NULL, NULL, sysdate(), NULL, 'super');

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

INSERT INTO sys_job(job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, update_by, update_time, remark) VALUES (1, 'Dingding department and personnel information synchronization', 'DEFAULT', 'syncDingTalkDataTask.executeSync()', '0 0 3 /1 * ? *', '1', '1', '0', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_job(job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, update_by, update_time, remark) VALUES (2, 'Automatically publish device upgrade job', 'DEFAULT', 'deviceUpdatePublishTask.execute()', '0 0/2 * * * ? *', '1', '1', '1', 'admin', sysdate(), '', sysdate(), '');
INSERT INTO sys_job(job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, update_by, update_time, remark) VALUES (3, 'Maintain interface Message Log by partition', 'DEFAULT', 'requestRecordTask.clearAndAddPartition(\'eyecool_assps\',30)', '0 0 0 * * ? *', '1', '1', '0', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_job(job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, update_by, update_time, remark) VALUES (4, 'Maintain face 1v1 log by partition', 'DEFAULT', 'clearBioMatchLogTask.clearFaceMatchLog(365,false,\'eyecool_assps\')', '0 0 1 * * ? *', '1', '1', '0', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_job(job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, update_by, update_time, remark) VALUES (5, 'Maintain face 1vN log by partition', 'DEFAULT', 'clearBioMatchLogTask.clearFaceSearchLog(365,false,\'eyecool_assps\')', '0 0 1 * * ? *', '1', '1', '0', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_job(job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, update_by, update_time, remark) VALUES (6, 'Maintain fingerprint 1v1 log by partition', 'DEFAULT', 'clearBioMatchLogTask.clearFingerMatchLog(365,false,\'eyecool_assps\')', '0 0 1 * * ? *', '1', '1', '0', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_job(job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, update_by, update_time, remark) VALUES (7, 'Maintain fingerprint 1vN log by partition', 'DEFAULT', 'clearBioMatchLogTask.clearFingerSearchLog(365,false,\'eyecool_assps\')', '0 0 1 * * ? *', '1', '1', '0', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_job(job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, update_by, update_time, remark) VALUES (8, 'Maintain iris 1v1 log by partition', 'DEFAULT', 'clearBioMatchLogTask.clearIrisMatchLog(365,false,\'eyecool_assps\')', '0 0 1 * * ? *', '1', '1', '0', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_job(job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, update_by, update_time, remark) VALUES (9, 'Maintain iris 1vN log by partition', 'DEFAULT', 'clearBioMatchLogTask.clearIrisSearchLog(365,false,\'eyecool_assps\')', '0 0 1 * * ? *', '1', '1', '0', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_job(job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, update_by, update_time, remark) VALUES (10, 'Maintain face and iris multi-modal 1vN log by partition', 'DEFAULT', 'clearBioMatchLogTask.clearMulitSearchLog(365,false,\'eyecool_assps\')', '0 0 1 * * ? *', '1', '1', '0', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_job(job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, update_by, update_time, remark) VALUES (11, 'Maintain health code log by partition', 'DEFAULT', 'clearBioMatchLogTask.clearHealthcodeLog(365,\'eyecool_assps\')', '0 0 1 * * ? *', '1', '1', '0', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_job(job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, update_by, update_time, remark) VALUES (12, 'Maintain Message Log by partition', 'DEFAULT', 'msgLogTask.clearAndAddPartition(\'eyecool_assps\',365)', '0 0 1 * * ? *', '1', '1', '0', 'admin', sysdate(), '', sysdate(), '');
INSERT INTO sys_job(job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, update_by, update_time, remark) VALUES (13, 'Sync 203 device personnel in real time', 'DEFAULT', 'syncTask.syncAllDevice()', '*/5 * * * * ?', '1', '1', '0', 'admin', '2021-07-07 17:36:12', '', '2021-07-07 17:36:16', '');


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


INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2012, 'Basic Data', 0, 4, 'basedata', NULL, 1, 0, 'M', '0', '0', NULL, 'base', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2013, 'Personnel Management', 2012, 1, 'person', 'basedata/person/index', 1, 0, 'C', '0', '0', 'basedata:person:list', '#', 'admin', sysdate(), 'admin', sysdate(), 'personnel basic information menu');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2014, 'Personnel Basic Information Query', 2013, 1, '#', '', 1, 0, 'F', '0', '0', 'basedata:person:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2015, 'Add Personnel Basic Information', 2013, 2, '#', '', 1, 0, 'F', '0', '0', 'basedata:person:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2016, 'Edit Personnel Basic Information', 2013, 3, '#', '', 1, 0, 'F', '0', '0', 'basedata:person:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2017, 'Delete Personnel Basic Information', 2013, 4, '#', '', 1, 0, 'F', '0', '0', 'basedata:person:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2018, 'Export Personnel Basic Information', 2013, 5, '#', '', 1, 0, 'F', '0', '0', 'basedata:person:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2019, 'Face Management', 2012, 2, 'face', 'basedata/face/index', 1, 0, 'C', '0', '0', 'basedata:face:list', '#', 'admin', sysdate(), 'admin', sysdate(), 'face image information menu');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2020, 'Face Image Information Query', 2019, 1, '#', '', 1, 0, 'F', '0', '0', 'basedata:face:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2021, 'Add Face Image Information', 2019, 2, '#', '', 1, 0, 'F', '0', '0', 'basedata:face:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2022, 'Edit Face Image Information', 2019, 3, '#', '', 1, 0, 'F', '0', '0', 'basedata:face:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2023, 'Delete Face Image Information', 2019, 4, '#', '', 1, 0, 'F', '0', '0', 'basedata:face:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2024, 'Export Face Image Information', 2019, 5, '#', '', 1, 0, 'F', '0', '0', 'basedata:face:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2025, 'Fingerprint Management', 2012, 3, 'finger', 'basedata/finger/index', 1, 0, 'C', '0', '0', 'basedata:finger:list', '#', 'admin', sysdate(), 'admin', sysdate(), 'fingerprint image information menu');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2026, 'Fingerprint Image Information Query', 2025, 1, '#', '', 1, 0, 'F', '0', '0', 'basedata:finger:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2027, 'Add Fingerprint Image Information', 2025, 2, '#', '', 1, 0, 'F', '0', '0', 'basedata:finger:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2028, 'Edit Fingerprint Image Information', 2025, 3, '#', '', 1, 0, 'F', '0', '0', 'basedata:finger:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2029, 'Delete Fingerprint Image Information', 2025, 4, '#', '', 1, 0, 'F', '0', '0', 'basedata:finger:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2030, 'Export Fingerprint Image Information', 2025, 5, '#', '', 1, 0, 'F', '0', '0', 'basedata:finger:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2031, 'Iris Management', 2012, 4, 'iris', 'basedata/iris/index', 1, 0, 'C', '0', '0', 'basedata:iris:list', '#', 'admin', sysdate(), 'admin', sysdate(), 'iris image information menu');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2032, 'Iris Image Information Query', 2031, 1, '#', '', 1, 0, 'F', '0', '0', 'basedata:iris:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2033, 'Add Iris Image Information', 2031, 2, '#', '', 1, 0, 'F', '0', '0', 'basedata:iris:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2034, 'Edit Iris Image Information', 2031, 3, '#', '', 1, 0, 'F', '0', '0', 'basedata:iris:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2035, 'Delete Iris Image Information', 2031, 4, '#', '', 1, 0, 'F', '0', '0', 'basedata:iris:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2036, 'Export Iris Image Information', 2031, 5, '#', '', 1, 0, 'F', '0', '0', 'basedata:iris:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2037, 'Face and Iris', 2012, 5, 'faceIris', 'basedata/faceIris/index', 1, 0, 'C', '0', '0', 'basedata:faceIris:list', '#', 'admin', sysdate(), 'admin', sysdate(), 'iris and face multi-modal menu');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2038, 'Iris and Face Multi-modal Query', 2037, 1, '#', '', 1, 0, 'F', '0', '0', 'basedata:faceIris:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2039, 'Add Iris and Face Multi-modal', 2037, 2, '#', '', 1, 0, 'F', '0', '0', 'basedata:faceIris:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2040, 'Edit Iris and Face Multi-modal', 2037, 3, '#', '', 1, 0, 'F', '0', '0', 'basedata:faceIris:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2041, 'Delete Iris and Face Multi-modal', 2037, 4, '#', '', 1, 0, 'F', '0', '0', 'basedata:faceIris:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2042, 'Export Iris and Face Multi-modal', 2037, 5, '#', '', 1, 0, 'F', '0', '0', 'basedata:faceIris:export', '#', 'admin', sysdate(), '', NULL, ''); 
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2043, 'Certificate Management', 2012, 6, 'cert', 'basedata/cert/index', 1, 0, 'C', '0', '0', 'basedata:cert:list', '#', 'admin', sysdate(), 'admin', sysdate(), 'personnel certificate information menu');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2044, 'Personnel Certificate Information Query', 2043, 1, '#', '', 1, 0, 'F', '0', '0', 'basedata:cert:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2045, 'Add Personnel Certificate Information', 2043, 2, '#', '', 1, 0, 'F', '0', '0', 'basedata:cert:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2046, 'Edit Personnel Certificate Information', 2043, 3, '#', '', 1, 0, 'F', '0', '0', 'basedata:cert:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2047, 'Delete Personnel Certificate Information', 2043, 4, '#', '', 1, 0, 'F', '0', '0', 'basedata:cert:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2048, 'Export Personnel Certificate Information', 2043, 5, '#', '', 1, 0, 'F', '0', '0', 'basedata:cert:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2055, 'One Click Update Face Feature', 2019, 6, '', NULL, 1, 0, 'F', '0', '0', 'basedata:face:updatefeature', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2056, 'One Click Update Fingerprint Feature', 2025, 6, '', NULL, 1, 0, 'F', '0', '0', 'basedata:finger:updatefeature', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2057, 'One Click Update Iris Feature', 2031, 6, '', NULL, 1, 0, 'F', '0', '0', 'basedata:iris:updatefeature', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2058, 'Import Personnel Certificate Information', 2043, 6, '', NULL, 1, 0, 'F', '0', '0', 'basedata:cert:import', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2059, 'Download Personnel Certificate Image', 2043, 7, '', NULL, 1, 0, 'F', '0', '0', 'basedata:cert:download', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2060, 'Download Face Image', 2019, 7, '', NULL, 1, 0, 'F', '0', '0', 'basedata:face:download', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2061, 'Download Fingerprint Image', 2025, 7, '', NULL, 1, 0, 'F', '0', '0', 'basedata:finger:download', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2062, 'Download Iris Image', 2031, 7, '', NULL, 1, 0, 'F', '0', '0', 'basedata:iris:download', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2063, 'One Click Sync Personnel Information', 2013, 7, '', NULL, 1, 0, 'F', '0', '0', 'basedata:person:syncdata', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2064, 'Import Personnel Basic Information', 2013, 8, '', NULL, 1, 0, 'F', '0', '0', 'basedata:person:import', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2065, 'Import Face Image', 2019, 8, '', NULL, 1, 0, 'F', '0', '0', 'basedata:face:import', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2066, 'Import Fingerprint Image', 2025, 8, '', NULL, 1, 0, 'F', '0', '0', 'basedata:finger:import', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2067, 'Import Iris Image', 2031, 8, '', NULL, 1, 0, 'F', '0', '0', 'basedata:iris:import', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2068, 'Scene Access', 0, 5, 'scene', NULL, 1, 0, 'M', '0', '0', NULL, 'scene', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2069, 'Channel Information', 2068, 1, 'channel', 'scene/channel/index', 1, 0, 'C', '0', '0', 'scene:channel:list', '#', 'admin', sysdate(), 'admin', sysdate(), 'channel information menu');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2070, 'Channel Information Query', 2069, 1, '#', '', 1, 0, 'F', '0', '0', 'scene:channel:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2071, 'Add Channel Information', 2069, 2, '#', '', 1, 0, 'F', '0', '0', 'scene:channel:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2072, 'Edit Channel Information', 2069, 3, '#', '', 1, 0, 'F', '0', '0', 'scene:channel:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2073, 'Delete Channel Information', 2069, 4, '#', '', 1, 0, 'F', '0', '0', 'scene:channel:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2074, 'Export Channel Information', 2069, 5, '#', '', 1, 0, 'F', '0', '0', 'scene:channel:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2075, 'Sub-database Information', 2068, 1, 'subtreasury', 'scene/subtreasury/index', 1, 0, 'C', '0', '0', 'scene:subtreasury:list', '#', 'admin', sysdate(), 'admin', sysdate(), 'sub-database information menu');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2076, 'Sub-database Information Query', 2075, 1, '#', '', 1, 0, 'F', '0', '0', 'scene:subtreasury:query', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2077, 'Add Sub-database Information', 2075, 2, '#', '', 1, 0, 'F', '0', '0', 'scene:subtreasury:add', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2078, 'Edit Sub-database Information', 2075, 3, '#', '', 1, 0, 'F', '0', '0', 'scene:subtreasury:edit', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2079, 'Delete Sub-database Information', 2075, 4, '#', '', 1, 0, 'F', '0', '0', 'scene:subtreasury:remove', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2080, 'Export Sub-database Information', 2075, 5, '#', '', 1, 0, 'F', '0', '0', 'scene:subtreasury:export', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2081, 'Channel Parameter', 2068, 1, 'channelParam', 'scene/channelParam/index', 1, 0, 'C', '0', '0', 'scene:channelParam:list', '#', 'admin', sysdate(), 'admin', sysdate(), 'channel parameter menu');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2082, 'Channel Parameter Query', 2081, 1, '#', '', 1, 0, 'F', '0', '0', 'scene:channelParam:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2083, 'Add Channel Parameter', 2081, 2, '#', '', 1, 0, 'F', '0', '0', 'scene:channelParam:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2084, 'Edit Channel Parameter', 2081, 3, '#', '', 1, 0, 'F', '0', '0', 'scene:channelParam:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2085, 'Delete Channel Parameter', 2081, 4, '#', '', 1, 0, 'F', '0', '0', 'scene:channelParam:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2086, 'Export Channel Parameter', 2081, 5, '#', '', 1, 0, 'F', '0', '0', 'scene:channelParam:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2087, 'Channel Business', 2068, 1, 'channelBusi', 'scene/channelBusi/index', 1, 0, 'C', '0', '0', 'scene:channelBusi:list', '#', 'admin', sysdate(), 'admin', sysdate(), 'channel business menu');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2088, 'Channel Business Query', 2087, 1, '#', '', 1, 0, 'F', '0', '0', 'scene:channelBusi:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2089, 'Add Channel Business', 2087, 2, '#', '', 1, 0, 'F', '0', '0', 'scene:channelBusi:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2090, 'Edit Channel Business', 2087, 3, '#', '', 1, 0, 'F', '0', '0', 'scene:channelBusi:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2091, 'Delete Channel Business', 2087, 4, '#', '', 1, 0, 'F', '0', '0', 'scene:channelBusi:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2092, 'Export Channel Business', 2087, 5, '#', '', 1, 0, 'F', '0', '0', 'scene:channelBusi:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2093, 'Sub-database Business', 2068, 1, 'subtreasuryBusi', 'scene/subtreasuryBusi/index', 1, 0, 'C', '0', '0', 'scene:subtreasuryBusi:list', '#', 'admin', sysdate(), 'admin', sysdate(), 'sub-database business menu');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2094, 'Sub-database Business Query', 2093, 1, '#', '', 1, 0, 'F', '0', '0', 'scene:subtreasuryBusi:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2095, 'Add Sub-database Business', 2093, 2, '#', '', 1, 0, 'F', '0', '0', 'scene:subtreasuryBusi:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2096, 'Edit Sub-database Business', 2093, 3, '#', '', 1, 0, 'F', '0', '0', 'scene:subtreasuryBusi:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2097, 'Delete Sub-database Business', 2093, 4, '#', '', 1, 0, 'F', '0', '0', 'scene:subtreasuryBusi:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2098, 'Export Sub-database Business', 2093, 5, '#', '', 1, 0, 'F', '0', '0', 'scene:subtreasuryBusi:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2099, 'One Click Sync Channel Business', 2087, 6, '', NULL, 1, 0, 'F', '0', '0', 'scene:channelBusi:syncdata', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2100, 'Import Channel Business', 2087, 7, '', NULL, 1, 0, 'F', '0', '0', 'scene:channelBusi:import', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2101, 'One Click Sync Sub-database Business', 2093, 6, '', NULL, 1, 0, 'F', '0', '0', 'scene:subtreasuryBusi:syncdata', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2102, 'Area Management', 0, 6, 'area', NULL, 1, 0, 'M', '0', '0', '', 'area', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2103, 'Area Model', 2102, 1, 'model', 'area/model/index', 1, 0, 'C', '0', '0', 'area:model:list', '#', 'admin', sysdate(), 'admin', sysdate(), 'area menu');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2104, 'Area Query', 2103, 1, '#', '', 1, 0, 'F', '0', '0', 'area:model:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2105, 'Add Area', 2103, 2, '#', '', 1, 0, 'F', '0', '0', 'area:model:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2106, 'Edit Area', 2103, 3, '#', '', 1, 0, 'F', '0', '0', 'area:model:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2107, 'Delete Area', 2103, 4, '#', '', 1, 0, 'F', '0', '0', 'area:model:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2108, 'Export Area', 2103, 5, '#', '', 1, 0, 'F', '0', '0', 'area:model:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2109, 'Device Management', 0, 7, 'device', NULL, 1, 0, 'M', '0', '0', NULL, 'device', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2110, 'Device Model', 2109, 1, 'model', 'device/model/index', 1, 0, 'C', '0', '0', 'device:model:list', '#', 'admin', sysdate(), 'admin', sysdate(), 'device model information menu');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2111, 'Device Model Information Query', 2110, 1, '#', '', 1, 0, 'F', '0', '0', 'device:model:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2112, 'Add Device Model Information', 2110, 2, '#', '', 1, 0, 'F', '0', '0', 'device:model:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2113, 'Edit Device Model Information', 2110, 3, '#', '', 1, 0, 'F', '0', '0', 'device:model:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2114, 'Delete Device Model Information', 2110, 4, '#', '', 1, 0, 'F', '0', '0', 'device:model:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2115, 'Export Device Model Information', 2110, 5, '#', '', 1, 0, 'F', '0', '0', 'device:model:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2116, 'Device Parameter', 2109, 4, 'param', 'device/param/index', 1, 0, 'M', '0', '0', '', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2118, 'Parameter Information', 2116, 1, 'info', 'device/param/info/index', 1, 0, 'C', '0', '0', 'device:paraminfo:list', '#', 'admin', sysdate(), 'admin', sysdate(), 'device parameter information menu');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2119, 'Device Parameter Information Query', 2118, 1, '#', '', 1, 0, 'F', '0', '0', 'device:paraminfo:query', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2120, 'Add Device Parameter Information', 2118, 2, '#', '', 1, 0, 'F', '0', '0', 'device:paraminfo:add', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2121, 'Edit Device Parameter Information', 2118, 3, '#', '', 1, 0, 'F', '0', '0', 'device:paraminfo:edit', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2122, 'Delete Device Parameter Information', 2118, 4, '#', '', 1, 0, 'F', '0', '0', 'device:paraminfo:remove', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2123, 'Export Device Parameter Information', 2118, 5, '#', '', 1, 0, 'F', '0', '0', 'device:paraminfo:export', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2124, 'SDK File ', 3, 1, 'file', 'tool/sdkFile/index', 1, 0, 'C', '0', '0', 'tool:sdkFile:list', '#', 'admin', sysdate(), 'admin', sysdate(), 'SDK file Upload menu');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2125, 'SDK File Upload Query', 2124, 1, '#', '', 1, 0, 'F', '0', '0', 'tool:sdkFile:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2126, 'Add SDK File Upload', 2124, 2, '#', '', 1, 0, 'F', '0', '0', 'tool:sdkFile:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2127, 'Edit SDK File Upload', 2124, 3, '#', '', 1, 0, 'F', '0', '0', 'tool:sdkFile:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2128, 'Delete SDK File Upload', 2124, 4, '#', '', 1, 0, 'F', '0', '0', 'tool:sdkFile:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2129, 'Export SDK File Upload', 2124, 5, '#', '', 1, 0, 'F', '0', '0', 'tool:sdkFile:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2136, 'Model Parameter', 2116, 1, 'relation', 'device/param/relation/index', 1, 0, 'C', '0', '0', 'device:paramModelRel:list', '#', 'admin', sysdate(), 'admin', sysdate(), 'parameter and model relationship menu');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2137, 'Parameter and Model Relationship Query', 2136, 1, '#', '', 1, 0, 'F', '0', '0', 'device:paramModelRel:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2138, 'Add Parameter and Model Relationship', 2136, 2, '#', '', 1, 0, 'F', '0', '0', 'device:paramModelRel:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2139, 'Edit Parameter and Model Relationship', 2136, 3, '#', '', 1, 0, 'F', '0', '0', 'device:paramModelRel:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2140, 'Delete Parameter Model Relationship', 2136, 4, '#', '', 1, 0, 'F', '0', '0', 'device:paramModelRel:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2141, 'Export Parameter Model Relationship', 2136, 5, '#', '', 1, 0, 'F', '0', '0', 'device:paramModelRel:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2142, 'Device Information', 2109, 2, 'info', 'device/info/index', 1, 0, 'C', '0', '0', 'device:info:list', '#', 'admin', sysdate(), 'admin', sysdate(), 'device information menu');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2143, 'Device Information Query', 2142, 1, '#', '', 1, 0, 'F', '0', '0', 'device:info:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2144, 'Add Device Information', 2142, 2, '#', '', 1, 0, 'F', '0', '0', 'device:info:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2145, 'Edit Device Information', 2142, 3, '#', '', 1, 0, 'F', '0', '0', 'device:info:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2146, 'Delete Device Information', 2142, 4, '#', '', 1, 0, 'F', '0', '0', 'device:info:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2147, 'Export Device Information', 2142, 5, '#', '', 1, 0, 'F', '0', '0', 'device:info:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2148, 'Import Device Information', 2142, 6, '', NULL, 1, 0, 'F', '0', '0', 'device:info:import', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2149, 'Device Batch', 2109, 3, 'batchlog', 'device/batchlog/index', 1, 0, 'C', '0', '0', 'device:batchlog:list', '#', 'admin', sysdate(), 'admin', sysdate(), 'device batch menu');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2150, 'Device Batch Query', 2149, 1, '#', '', 1, 0, 'F', '0', '0', 'device:batchlog:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2153, 'Device Batch Rollback', 2149, 2, '#', '', 1, 0, 'F', '0', '0', 'device:batchlog:rollback', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2154, 'Export Device Batch', 2149, 3, '#', '', 1, 0, 'F', '0', '0', 'device:batchlog:export', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2155, 'Device Upgrade', 2109, 5, 'upgrade', 'device/upgrade/index', 1, 0, 'M', '0', '0', '', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2156, 'Version Information', 2155, 1, 'version', 'device/upgrade/version/index', 1, 0, 'C', '0', '0', 'device:upgradeVersion:list', '#', 'admin', sysdate(), 'admin', sysdate(), 'version information menu');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2157, 'Version Information Query', 2156, 1, '#', '', 1, 0, 'F', '0', '0', 'device:upgradeVersion:query', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2158, 'Add Version Information', 2156, 2, '#', '', 1, 0, 'F', '0', '0', 'device:upgradeVersion:add', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2159, 'Edit Version Information', 2156, 3, '#', '', 1, 0, 'F', '0', '0', 'device:upgradeVersion:edit', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2160, 'Delete Version Information', 2156, 4, '#', '', 1, 0, 'F', '0', '0', 'device:upgradeVersion:remove', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2161, 'Export Version Information', 2156, 5, '#', '', 1, 0, 'F', '0', '0', 'device:upgradeVersion:export', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2162, 'Upgrade Job', 2155, 1, 'task', 'device/upgrade/task/index', 1, 0, 'C', '0', '0', 'device:upgradeTask:list', '#', 'admin', sysdate(), 'admin', sysdate(), 'upgrade job menu');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2163, 'Upgrade Job Query', 2162, 1, '#', '', 1, 0, 'F', '0', '0', 'device:upgradeTask:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2164, 'Add Upgrade Job', 2162, 2, '#', '', 1, 0, 'F', '0', '0', 'device:upgradeTask:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2165, 'Edit Upgrade Job', 2162, 3, '#', '', 1, 0, 'F', '0', '0', 'device:upgradeTask:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2166, 'Delete Upgrade Job', 2162, 4, '#', '', 1, 0, 'F', '0', '0', 'device:upgradeTask:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2167, 'Export Upgrade Job', 2162, 5, '#', '', 1, 0, 'F', '0', '0', 'device:upgradeTask:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2168, 'Upgrade Log', 2155, 1, 'upgradelog', 'device/upgrade/log/index', 1, 0, 'C', '0', '0', 'device:upgradelog:list', '#', 'admin', sysdate(), 'admin', sysdate(), 'upgrade log menu');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2169, 'Upgrade Log Query', 2168, 1, '#', '', 1, 0, 'F', '0', '0', 'device:upgradelog:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2173, 'Export Upgrade Log', 2168, 5, '#', '', 1, 0, 'F', '0', '0', 'device:upgradelog:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2174, 'Parameter Issue', 2116, 1, 'paramlog', 'device/param/log/index', 1, 0, 'C', '0', '0', 'device:paramlog:list', '#', 'admin', sysdate(), 'admin', sysdate(), 'parameter Issue log menu');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2175, 'Parameter Issue Log Query', 2174, 1, '#', '', 1, 0, 'F', '0', '0', 'device:paramlog:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2176, 'Add Parameter Issue Log', 2174, 2, '#', '', 1, 0, 'F', '0', '0', 'device:paramlog:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2179, 'Export Parameter Issue Log', 2174, 5, '#', '', 1, 0, 'F', '0', '0', 'device:paramlog:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2180, 'Action log', 2109, 6, 'actionlog', 'device/actionlog/index', 1, 0, 'C', '0', '0', 'device:actionlog:list', '#', 'admin', sysdate(), 'admin', sysdate(), 'device Action log menu');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2181, 'Device Action Log Query', 2180, 1, '#', '', 1, 0, 'F', '0', '0', 'device:actionlog:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2182, 'Add Device Action Log', 2180, 2, '#', '', 1, 0, 'F', '0', '0', 'device:actionlog:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2183, 'Edit Device Action Log', 2180, 3, '#', '', 1, 0, 'F', '0', '0', 'device:actionlog:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2184, 'Delete Device Action Log', 2180, 4, '#', '', 1, 0, 'F', '0', '0', 'device:actionlog:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2185, 'Export Device Action Log', 2180, 5, '#', '', 1, 0, 'F', '0', '0', 'device:actionlog:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2186, 'App system', 0, 8, 'app', NULL, 1, 0, 'M', '0', '0', NULL, 'appmgr', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2187, 'App Information', 2186, 1, 'info', 'app/info/index', 1, 0, 'C', '0', '0', 'app:info:list', '#', 'admin', sysdate(), 'admin', sysdate(), 'Appsystem information menu');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2188, 'App System Information Query', 2187, 1, '#', '', 1, 0, 'F', '0', '0', 'app:info:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2189, 'Add App System Information', 2187, 2, '#', '', 1, 0, 'F', '0', '0', 'app:info:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2191, 'Delete App System Information', 2187, 4, '#', '', 1, 0, 'F', '0', '0', 'app:info:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2192, 'Export App System Information', 2187, 5, '#', '', 1, 0, 'F', '0', '0', 'app:info:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2193, 'Interface Authorization', 2186, 2, 'auth', 'app/auth/index', 1, 0, 'C', '0', '0', 'app:auth:list', '#', 'admin', sysdate(), 'admin', sysdate(), 'App interface Authorization menu');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2194, 'App Interface Authorization Query', 2193, 1, '#', '', 1, 0, 'F', '0', '0', 'app:auth:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2195, 'Add App Interface Authorization', 2193, 2, '#', '', 1, 0, 'F', '0', '0', 'app:auth:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2196, 'Edit App Interface Authorization', 2193, 3, '#', '', 1, 0, 'F', '0', '0', 'app:auth:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2197, 'Delete App Interface Authorization', 2193, 4, '#', '', 1, 0, 'F', '0', '0', 'app:auth:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2198, 'Export App Interface Authorization', 2193, 5, '#', '', 1, 0, 'F', '0', '0', 'app:auth:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2199, 'Message Notice', 0, 9, 'msg', NULL, 1, 0, 'M', '0', '0', NULL, 'msgmgr', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2200, 'Email Configuration', 2261, 1, 'mailProperty', 'msg/mail/mailProperty/index', 1, 0, 'C', '0', '0', 'msg:mailProperty:list', '#', 'admin', sysdate(), 'admin', sysdate(), 'Email configuration menu');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2201, 'Email Configuration Query', 2200, 1, '#', '', 1, 0, 'F', '0', '0', 'msg:mailProperty:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2202, 'Add Email Configuration', 2200, 2, '#', '', 1, 0, 'F', '0', '0', 'msg:mailProperty:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2203, 'Edit Email Configuration', 2200, 3, '#', '', 1, 0, 'F', '0', '0', 'msg:mailProperty:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2204, 'Delete Email Configuration', 2200, 4, '#', '', 1, 0, 'F', '0', '0', 'msg:mailProperty:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2205, 'Export Email Configuration', 2200, 5, '#', '', 1, 0, 'F', '0', '0', 'msg:mailProperty:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2206, 'Dingding Micro App', 2263, 2, 'dingApplication', 'msg/dingtalk/application/index', 1, 0, 'C', '0', '0', 'msg:dingApplication:list', '#', 'admin', sysdate(), 'admin', sysdate(), 'dingding micro app menu');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2207, 'Dingding Micro App Query', 2206, 1, '#', '', 1, 0, 'F', '0', '0', 'msg:dingApplication:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2208, 'Add Dingding Micro App', 2206, 2, '#', '', 1, 0, 'F', '0', '0', 'msg:dingApplication:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2209, 'Edit Dingding Micro App', 2206, 3, '#', '', 1, 0, 'F', '0', '0', 'msg:dingApplication:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2210, 'Delete Dingding Micro App', 2206, 4, '#', '', 1, 0, 'F', '0', '0', 'msg:dingApplication:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2211, 'Export Dingding Micro App', 2206, 5, '#', '', 1, 0, 'F', '0', '0', 'msg:dingApplication:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2212, 'Dingding Team', 2263, 1, 'dingTeam', 'msg/dingtalk/team/index', 1, 0, 'C', '0', '0', 'msg:dingTeam:list', '#', 'admin', sysdate(), 'admin', sysdate(), 'dingdingteam(enterprise) menu');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2213, 'Dingding Team(Enterprise) Query', 2212, 1, '#', '', 1, 0, 'F', '0', '0', 'msg:dingTeam:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2214, 'Add Dingding Team(Enterprise)', 2212, 2, '#', '', 1, 0, 'F', '0', '0', 'msg:dingTeam:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2215, 'Edit Dingding Team(Enterprise)', 2212, 3, '#', '', 1, 0, 'F', '0', '0', 'msg:dingTeam:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2216, 'Delete Dingding Team(Enterprise)', 2212, 4, '#', '', 1, 0, 'F', '0', '0', 'msg:dingTeam:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2217, 'Export Dingding Team(Enterprise)', 2212, 5, '#', '', 1, 0, 'F', '0', '0', 'msg:dingTeam:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2224, 'Message Attachment', 2264, 3, 'logannex', 'msg/log/logannex/index', 1, 0, 'C', '0', '0', 'msg:logannex:list', '#', 'admin', sysdate(), 'admin', sysdate(), 'Message Log Attachment menu');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2225, 'Message Log Attachment Query', 2224, 1, '#', '', 1, 0, 'F', '0', '0', 'msg:logannex:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2229, 'Export Message Log Attachment', 2224, 5, '#', '', 1, 0, 'F', '0', '0', 'msg:logannex:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2230, 'Message Log', 2264, 1, 'msglog', 'msg/log/msglog/index', 1, 0, 'C', '0', '0', 'msg:log:list', '#', 'admin', sysdate(), 'admin', sysdate(), 'Message Log menu');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2231, 'Message Log Query', 2230, 1, '#', '', 1, 0, 'F', '0', '0', 'msg:log:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2235, 'Export Message Log', 2230, 5, '#', '', 1, 0, 'F', '0', '0', 'msg:log:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2236, 'Wechat Official Account', 2262, 1, 'officalAccount', 'msg/wechat/officalAccount/index', 1, 0, 'C', '0', '0', 'msg:officalAccount:list', '#', 'admin', sysdate(), 'admin', sysdate(), 'Wechat Official Account menu');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2237, 'Wechat Official Account Query', 2236, 1, '#', '', 1, 0, 'F', '0', '0', 'msg:officalAccount:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2238, 'Add Wechat Official Account', 2236, 2, '#', '', 1, 0, 'F', '0', '0', 'msg:officalAccount:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2239, 'Edit Wechat Official Account', 2236, 3, '#', '', 1, 0, 'F', '0', '0', 'msg:officalAccount:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2240, 'Delete Wechat Official Account', 2236, 4, '#', '', 1, 0, 'F', '0', '0', 'msg:officalAccount:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2241, 'Export Wechat Official Account', 2236, 5, '#', '', 1, 0, 'F', '0', '0', 'msg:officalAccount:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2242, 'Official Account User', 2262, 2, 'officalAccountUser', 'msg/wechat/officalAccountUser/index', 1, 0, 'C', '0', '0', 'msg:officalAccountUser:list', '#', 'admin', sysdate(), 'admin', sysdate(), 'Wechat User menu');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2243, 'Wechat User Query', 2242, 1, '#', '', 1, 0, 'F', '0', '0', 'msg:officalAccountUser:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2244, 'Pull Wechat User', 2242, 2, '#', '', 1, 0, 'F', '0', '0', 'msg:officalAccountUser:pull', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2245, 'Edit Wechat User', 2242, 3, '#', '', 1, 0, 'F', '0', '0', 'msg:officalAccountUser:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2247, 'Export Wechat User', 2242, 5, '#', '', 1, 0, 'F', '0', '0', 'msg:officalAccountUser:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2248, 'Short Message Cloud Account', 2260, 1, 'smsCloudAccount', 'msg/sms/cloudAccount/index', 1, 0, 'C', '0', '0', 'msg:smsCloudAccount:list', '#', 'admin', sysdate(), 'admin', sysdate(), 'Short Message Cloud Account menu');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2249, 'Short Message Cloud Account Query', 2248, 1, '#', '', 1, 0, 'F', '0', '0', 'msg:smsCloudAccount:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2250, 'Add Short Message Cloud Account', 2248, 2, '#', '', 1, 0, 'F', '0', '0', 'msg:smsCloudAccount:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2251, 'Edit Short Message Cloud Account', 2248, 3, '#', '', 1, 0, 'F', '0', '0', 'msg:smsCloudAccount:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2252, 'Delete Short Message Cloud Account', 2248, 4, '#', '', 1, 0, 'F', '0', '0', 'msg:smsCloudAccount:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2253, 'Export Short Message Cloud Account', 2248, 5, '#', '', 1, 0, 'F', '0', '0', 'msg:smsCloudAccount:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2254, 'Message Template', 2199, 5, 'template', 'msg/template/index', 1, 0, 'C', '0', '0', 'msg:template:list', '#', 'admin', sysdate(), 'admin', sysdate(), 'Message Template menu');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2255, 'Message Template Query', 2254, 1, '#', '', 1, 0, 'F', '0', '0', 'msg:template:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2256, 'Add Message Template', 2254, 2, '#', '', 1, 0, 'F', '0', '0', 'msg:template:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2257, 'Edit Message Template', 2254, 3, '#', '', 1, 0, 'F', '0', '0', 'msg:template:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2258, 'Delete Message Template', 2254, 4, '#', '', 1, 0, 'F', '0', '0', 'msg:template:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2259, 'Export Message Template', 2254, 5, '#', '', 1, 0, 'F', '0', '0', 'msg:template:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2260, 'Short Message Notice', 2199, 1, 'sms', 'msg/sms/index', 1, 0, 'M', '0', '0', '', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2261, 'Email Notice', 2199, 2, 'mail', 'msg/mail/index', 1, 0, 'M', '0', '0', '', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2262, 'Wechat Notice', 2199, 3, 'wechat', 'msg/wechat/index', 1, 0, 'M', '0', '0', '', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2263, 'Dingding Notice', 2199, 4, 'dingtalk', 'msg/dingtalk/index', 1, 0, 'M', '0', '0', '', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2264, 'Notice Log', 2199, 6, 'log', 'msg/log/index', 1, 0, 'M', '0', '0', '', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2265, 'Official Account Menu', 2262, 3, 'weixinMenu', 'msg/wechat/menu/index', 1, 0, 'C', '0', '0', 'msg:weixinMenu:list', '#', 'admin', sysdate(), 'admin', sysdate(), 'Wechat Official Account menu menu');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2266, 'Wechat Official Account Menu Query', 2265, 1, '#', '', 1, 0, 'F', '0', '0', 'msg:weixinMenu:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2267, 'Add Wechat Official Account Menu', 2265, 2, '#', '', 1, 0, 'F', '0', '0', 'msg:weixinMenu:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2268, 'Edit Wechat Official Account Menu', 2265, 3, '#', '', 1, 0, 'F', '0', '0', 'msg:weixinMenu:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2269, 'Delete Wechat Official Account Menu', 2265, 4, '#', '', 1, 0, 'F', '0', '0', 'msg:weixinMenu:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2270, 'Export Wechat Official Account Menu', 2265, 5, '#', '', 1, 0, 'F', '0', '0', 'msg:weixinMenu:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2271, 'Menu Response', 2262, 4, 'weixinMenuReply', 'msg/wechat/menuReply/index', 1, 0, 'C', '0', '0', 'msg:weixinMenuReply:list', '#', 'admin', sysdate(), 'admin', sysdate(), 'Wechat Official Account menu response menu');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2272, 'Wechat Official Account Menu Response Query', 2271, 1, '#', '', 1, 0, 'F', '0', '0', 'msg:weixinMenuReply:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2273, 'Add Wechat Official Account Nenu Response', 2271, 2, '#', '', 1, 0, 'F', '0', '0', 'msg:weixinMenuReply:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2274, 'Edit Wechat Official Account Menu Response', 2271, 3, '#', '', 1, 0, 'F', '0', '0', 'msg:weixinMenuReply:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2275, 'Delete Wechat Official Account Menu Response', 2271, 4, '#', '', 1, 0, 'F', '0', '0', 'msg:weixinMenuReply:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2276, 'Export Wechat Official Account Menu Response', 2271, 5, '#', '', 1, 0, 'F', '0', '0', 'msg:weixinMenuReply:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2277, 'Send Message', 2199, 7, 'send', 'msg/send/index', 1, 0, 'M', '0', '0', '', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2278, 'Send Short Message', 2277, 1, 'sms', 'msg/send/sms/index', 1, 0, 'C', '0', '0', 'msg:send:sms', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2279, 'Send Email', 2277, 2, 'mail', 'msg/send/mail/index', 1, 0, 'C', '0', '0', 'msg:send:mail', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2280, 'Send Wechat', 2277, 3, 'wechat', 'msg/send/wechat/index', 1, 0, 'C', '0', '0', 'msg:send:weixin', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2281, 'Send Dingding', 2277, 4, 'dingtalk', 'msg/send/dingtalk/index', 1, 0, 'C', '0', '0', 'msg:send:dingtalk', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2282, 'Dingding Media Upload', 2277, 5, 'uploadMedia', 'msg/send/dingtalk/upload-media', 1, 0, 'C', '0', '0', 'msg:send:dingMediaUpload', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2284, 'Transaction Log', 0, 10, 'tradelog', NULL, 1, 0, 'M', '0', '0', NULL, 'logmgr', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2285, 'OCR Log', 2284, 2, 'ocr', 'ocr/index', 1, 0, 'M', '0', '0', '', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2297, 'Face Comparison', 2406, 1, 'facematch', 'tradelog/facematch/index', 1, 0, 'C', '0', '0', 'tradelog:facematch:list', '#', 'admin', sysdate(), 'admin', sysdate(), 'face comparison log menu');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2298, 'Face Comparison log Query', 2297, 1, '#', '', 1, 0, 'F', '0', '0', 'tradelog:facematch:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2302, 'Export Face Comparison Log', 2297, 5, '#', '', 1, 0, 'F', '0', '0', 'tradelog:facematch:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2303, 'Face Search', 2406, 2, 'facesearch', 'tradelog/facesearch/index', 1, 0, 'C', '0', '0', 'tradelog:facesearch:list', '#', 'admin', sysdate(), 'admin', sysdate(), 'face Search log menu');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2304, 'Face Search Log Query', 2303, 1, '#', '', 1, 0, 'F', '0', '0', 'tradelog:facesearch:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2308, 'Export Face Search Log', 2303, 5, '#', '', 1, 0, 'F', '0', '0', 'tradelog:facesearch:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2321, 'Fingerprint Comparison', 2406, 3, 'fingermatch', 'tradelog/fingermatch/index', 1, 0, 'C', '0', '0', 'tradelog:fingermatch:list', '#', 'admin', sysdate(), 'admin', sysdate(), 'fingerprint comparison log menu');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2322, 'Fingerprint Comparison Log Query', 2321, 1, '#', '', 1, 0, 'F', '0', '0', 'tradelog:fingermatch:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2326, 'Export Fingerprint Comparison Log', 2321, 5, '#', '', 1, 0, 'F', '0', '0', 'tradelog:fingermatch:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2327, 'Fingerprint Search', 2406, 4, 'fingersearch', 'tradelog/fingersearch/index', 1, 0, 'C', '0', '0', 'tradelog:fingersearch:list', '#', 'admin', sysdate(), 'admin', sysdate(), 'fingerprint Search log menu');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2328, 'Fingerprint Search Log Query', 2327, 1, '#', '', 1, 0, 'F', '0', '0', 'tradelog:fingersearch:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2332, 'Export Fingerprint Search Log', 2327, 5, '#', '', 1, 0, 'F', '0', '0', 'tradelog:fingersearch:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2333, 'Iris Comparison', 2406, 5, 'irismatch', 'tradelog/irismatch/index', 1, 0, 'C', '0', '0', 'tradelog:irismatch:list', '#', 'admin', sysdate(), 'admin', sysdate(), 'iris comparison log menu');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2334, 'Iris Comparison Log Query', 2333, 1, '#', '', 1, 0, 'F', '0', '0', 'tradelog:irismatch:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2338, 'Export Iris Comparison Log', 2333, 5, '#', '', 1, 0, 'F', '0', '0', 'tradelog:irismatch:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2339, 'Iris Search', 2406, 6, 'irissearch', 'tradelog/irissearch/index', 1, 0, 'C', '0', '0', 'tradelog:irissearch:list', '#', 'admin', sysdate(), 'admin', sysdate(), 'iris Search log menu');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2340, 'Iris Search Log Query', 2339, 1, '#', '', 1, 0, 'F', '0', '0', 'tradelog:irissearch:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2344, 'Export Iris Search Log', 2339, 5, '#', '', 1, 0, 'F', '0', '0', 'tradelog:irissearch:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2351, 'Bank Card', 2285, 1, 'bankcard', 'ocr/bankcard/index', 1, 0, 'C', '0', '0', 'ocr:bankcard:list', '#', 'admin', sysdate(), 'admin', sysdate(), 'Bank Card OCR recognition result menu');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2352, 'Bank Card OCR Recognition Result Query', 2351, 1, '#', '', 1, 0, 'F', '0', '0', 'ocr:bankcard:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2356, 'Export Bank Card OCR Recognition Result', 2351, 5, '#', '', 1, 0, 'F', '0', '0', 'ocr:bankcard:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2357, 'Business License', 2285, 2, 'busilic', 'ocr/busilic/index', 1, 0, 'C', '0', '0', 'ocr:busilic:list', '#', 'admin', sysdate(), '', NULL, 'business license OCR recognition result menu');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2358, 'Business License OCR Recognition Result Query', 2357, 1, '#', '', 1, 0, 'F', '0', '0', 'ocr:busilic:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2362, 'Export Business License OCR Recognition Result', 2357, 5, '#', '', 1, 0, 'F', '0', '0', 'ocr:busilic:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2363, 'Driver Certificate', 2285, 3, 'driverlic', 'ocr/driverlic/index', 1, 0, 'C', '0', '0', 'ocr:driverlic:list', '#', 'admin', sysdate(), '', NULL, 'driver certificate OCR recognition result menu');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2364, 'Driver Certificate OCR Recognition Result Query', 2363, 1, '#', '', 1, 0, 'F', '0', '0', 'ocr:driverlic:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2368, 'Export Driver Certificate OCR Recognition Result', 2363, 5, '#', '', 1, 0, 'F', '0', '0', 'ocr:driverlic:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2369, 'Driving Permit', 2285, 4, 'drivinglic', 'ocr/drivinglic/index', 1, 0, 'C', '0', '0', 'ocr:drivinglic:list', '#', 'admin', sysdate(), '', NULL, 'Driving Permit OCR recognition result menu');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2370, 'Driving Permit OCR Recognition Result Query', 2369, 1, '#', '', 1, 0, 'F', '0', '0', 'ocr:drivinglic:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2374, 'Export Driving Permit OCR Recognition Result', 2369, 5, '#', '', 1, 0, 'F', '0', '0', 'ocr:drivinglic:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2375, 'Exit-Entry Permit for Travelling to and from Hong Kong and Macao ', 2285, 5, 'hkmac', 'ocr/hkmac/index', 1, 0, 'C', '0', '0', 'ocr:hkmac:list', '#', 'admin', sysdate(), '', NULL, 'Exit-Entry Permit for Travelling to and from Hong Kong and Macao OCR recognition result menu');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2376, 'Exit-Entry Permit for Travelling to and from Hong Kong and Macao OCR Recognition Result Query', 2375, 1, '#', '', 1, 0, 'F', '0', '0', 'ocr:hkmac:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2380, 'Export Exit-Entry Permit for Travelling to and from Hong Kong and Macao OCR Recognition Result', 2375, 5, '#', '', 1, 0, 'F', '0', '0', 'ocr:hkmac:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2381, 'the Back of ID Card', 2285, 6, 'idcardback', 'ocr/idcardback/index', 1, 0, 'C', '0', '0', 'ocr:idcardback:list', '#', 'admin', sysdate(), '', NULL, 'the back of ID Card OCR recognition result menu');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2382, 'the Back of ID Card OCR Recognition Result Query', 2381, 1, '#', '', 1, 0, 'F', '0', '0', 'ocr:idcardback:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2386, 'Export the Back of ID Card OCR Recognition Result', 2381, 5, '#', '', 1, 0, 'F', '0', '0', 'ocr:idcardback:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2387, 'the Front of ID Card', 2285, 7, 'idcardfront', 'ocr/idcardfront/index', 1, 0, 'C', '0', '0', 'ocr:idcardfront:list', '#', 'admin', sysdate(), '', NULL, 'the front of ID Card OCR recognition result menu');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2388, 'the Front of ID Card OCR Recognition Result Query', 2387, 1, '#', '', 1, 0, 'F', '0', '0', 'ocr:idcardfront:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2392, 'Export the Front of ID Card OCR Recognition Result', 2387, 5, '#', '', 1, 0, 'F', '0', '0', 'ocr:idcardfront:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2393, 'Passport', 2285, 8, 'passport', 'ocr/passport/index', 1, 0, 'C', '0', '0', 'ocr:passport:list', '#', 'admin', sysdate(), '', NULL, 'passport OCR recognition result menu');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2394, 'Passport OCR Recognition Result Query', 2393, 1, '#', '', 1, 0, 'F', '0', '0', 'ocr:passport:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2398, 'Export Passport OCR Recognition Result', 2393, 5, '#', '', 1, 0, 'F', '0', '0', 'ocr:passport:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2399, 'Face and Iris Search', 2406, 7, 'faceirisSearch', 'tradelog/faceirisSearch/index', 1, 0, 'C', '0', '0', 'tradelog:faceirisSearch:list', '#', 'admin', sysdate(), 'admin', sysdate(), 'face and iris Search log menu');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2400, 'Face and Iris Search Log Query', 2399, 1, '#', '', 1, 0, 'F', '0', '0', 'tradelog:faceirisSearch:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2404, 'Export Face and Iris Search Log', 2399, 5, '#', '', 1, 0, 'F', '0', '0', 'tradelog:faceirisSearch:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2406, 'Comparison Log', 2284, 1, 'compare', 'tradelog/index', 1, 0, 'M', '0', '0', '', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2407, 'Health Code Log', 2284, 3, 'healthcode', 'tradelog/healthcode/index', 1, 0, 'C', '0', '0', 'tradelog:healthcode:list', '#', 'admin', sysdate(), 'admin', sysdate(), 'health code request menu');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2408, 'Health Code Request Query', 2407, 1, '#', '', 1, 0, 'F', '0', '0', 'tradelog:healthcode:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2412, 'Export Health Code Request', 2407, 5, '#', '', 1, 0, 'F', '0', '0', 'tradelog:healthcode:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2499, 'the Center for Statistics', 2109, 8, 'center', 'noninductive/statistics/center', 1, 0, 'C', '1', '0', '', '#', 'admin', NULL, 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2500, 'Non Inductive Device Information', 2109, 7, 'noninductive', 'noninductive/device/index', 1, 0, 'C', '1', '0', 'noninductive:device:list', '#', 'admin', sysdate(), 'admin', sysdate(), 'device information menu');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2501, 'Interface Message', 2284, 4, 'reqrecord', 'tradelog/reqrecord/index', 1, 0, 'C', '0', '0', 'tradelog:reqrecord:list', '#', 'admin', sysdate(), 'admin', sysdate(), 'interface transaction request record menu');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2502, 'Interface Transaction Request Record Query', 2501, 1, '#', '', 1, 0, 'F', '0', '0', 'tradelog:reqrecord:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2507, 'Comparison Experience', 0, 11, 'webtrade', NULL, 1, 0, 'M', '0', '0', '', 'example', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2508, 'Face 1v1 Comparison', 2507, 1, 'faceMatchOne', 'webtrade/face/MatchOne', 1, 0, 'C', '0', '0', 'webtrade:face:matchone', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2509, 'Face 1vN Comparison', 2507, 2, 'faceMatchN', 'webtrade/face/MatchN', 1, 0, 'C', '0', '0', 'webtrade:face:matchn', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2510, 'Face Image Comparison', 2507, 3, 'faceComparetwo', 'webtrade/face/CompareTwo', 1, 0, 'C', '0', '0', 'webtrade:face:comparetwo', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2511, 'Fingerprint 1v1 Comparison', 2507, 4, 'fingerMatchOne', 'webtrade/finger/MatchOne', 1, 0, 'C', '0', '0', 'webtrade:finger:matchone', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2512, 'Fingerprint 1vN Comparison', 2507, 5, 'fingerMatchN', 'webtrade/finger/MatchN', 1, 0, 'C', '0', '0', 'webtrade:finger:matchn', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2513, 'Fingerprint Image Comparison', 2507, 6, 'fingerComparetwo', 'webtrade/finger/CompareTwo', 1, 0, 'C', '0', '0', 'webtrade:finger:comparetwo', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2514, 'Iris 1v1 Comparison', 2507, 7, 'irisMatchOne', 'webtrade/iris/MatchOne', 1, 0, 'C', '0', '0', 'webtrade:iris:matchone', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2515, 'Iris 1vN Comparison', 2507, 8, 'irisMatchN', 'webtrade/iris/MatchN', 1, 0, 'C', '0', '0', 'webtrade:iris:matchn', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2516, 'Iris Image Comparison', 2507, 9, 'irisCompareTwo', 'webtrade/iris/CompareTwo', 1, 0, 'C', '0', '0', 'webtrade:iris:comparetwo', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2517, 'Multi-modal 1v1 Comparison', 2507, 10, 'faceirisMatchOne', 'webtrade/faceiris/MatchOne', 1, 0, 'C', '0', '0', 'webtrade:faceiris:matchone', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2518, 'QR Code Generation', 3, 4, 'qrcode', 'tool/qrcode/index', 1, 0, 'C', '0', '0', 'tool:qrcode:gen', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2519, 'Tenant Management', 0, 1, 'tenantmgr', NULL, 1, 0, 'M', '0', '0', '', 'tenant', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2520, 'Tenant Role', 2519, 12, 'tenantrole', 'system/tenant/role/index', 1, 0, 'C', '0', '0', 'system:tenantrole:list', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2521, 'Tenant Role Query', 2520, 1, '', '', 1, 0, 'F', '0', '0', 'system:tenantrole:query', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2522, 'Add Tenant Role', 2520, 2, '', '', 1, 0, 'F', '0', '0', 'system:tenantrole:add', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2523, 'Edit Tenant Role', 2520, 3, '', '', 1, 0, 'F', '0', '0', 'system:tenantrole:edit', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2524, 'Delete Tenant Role', 2520, 4, '', '', 1, 0, 'F', '0', '0', 'system:tenantrole:remove', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2525, 'Export Tenant Role', 2520, 5, '', '', 1, 0, 'F', '0', '0', 'system:tenantrole:export', '#', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2526, 'Open Trial Tenant', 2006, 6, '', NULL, 1, 0, 'F', '0', '0', 'system:tenant:opentrial', '#', 'admin', sysdate(), '', NULL, '');

INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2527, 'Non Inductive Device Information Query', 2500, 1, '#', '', 1, 0, 'F', '0', '0', 'noninductive:device:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2528, 'Add Non Inductive Device Information', 2500, 2, '#', '', 1, 0, 'F', '0', '0', 'noninductive:device:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2529, 'Edit Non Inductive Device Information', 2500, 3, '#', '', 1, 0, 'F', '0', '0', 'noninductive:device:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2530, 'Delete Non Inductive Device Information', 2500, 4, '#', '', 1, 0, 'F', '0', '0', 'noninductive:device:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2531, 'Export Non Inductive Device Information', 2500, 5, '#', '', 1, 0, 'F', '0', '0', 'noninductive:device:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2532, 'Import Non Inductive Device Information', 2500, 6, '', NULL, 1, 0, 'F', '0', '0', 'noninductive:device:import', '#', 'admin', sysdate(), '', NULL, '');
