/*===========================================================================*/
/* 函数 ，代替mysql的find_in_set												 */
/* 例如： select * from sys_dept where FIND_IN_SET (101,ancestors)	 		 */
/*===========================================================================*/
CREATE 
	OR REPLACE FUNCTION find_in_set( arg1 IN int8, arg2 IN VARCHAR ) RETURNS bool AS $$ DECLARE
	result bool;
BEGIN
	result = ( SELECT '' || arg1 || '' = ANY ( STRING_TO_ARRAY ( arg2, ',' ) ) );
RETURN result;

END;
$$ LANGUAGE 'plpgsql' VOLATILE;

CREATE 
	OR REPLACE FUNCTION find_in_set( arg1 IN VARCHAR, arg2 IN VARCHAR ) RETURNS bool AS $$ DECLARE
	result bool;
BEGIN
	result = ( SELECT arg1 = ANY ( STRING_TO_ARRAY ( arg2, ',' ) ) );
RETURN result;

END;
$$ LANGUAGE 'plpgsql' VOLATILE;

/*==============================================================*/
/* 创建写入触发器函数    										*/
/* 参数TG_ARGV[0]是 分区表后缀格式化：'yyyymmdd'或者'yyyymm'   */
/*==============================================================*/
create or replace function ins_record_trigger_fun() returns trigger as $$  
declare  
	-- 表名
	table_name varchar(255) := TG_TABLE_NAME;
	-- 分区表后缀格式化
	par_name_suffix_formatter varchar(255) := TG_ARGV[0];
  -- 分区后缀  
  suffix text := to_char(NEW.received_time,par_name_suffix_formatter);  
begin  
  -- 乐观实现，执行动态SQL  
  execute format(concat('insert into ',table_name,'_%s select $1.*'), suffix) using NEW;  
  return null;  
  exception   
    when others then  
      -- 自动建分区, 有必要的话，可以把分区表的分区键约束也在这个QUERY中添加一下。  
      execute format(concat('create table IF NOT EXISTS ',table_name,'_%s (like ',table_name,') inherits(',table_name,')'), suffix);  
      -- 执行动态SQL  
      execute format(concat('insert into ',table_name,'_%s select $1.*'), suffix) using NEW;  
      return null;  
end;  
$$ language plpgsql strict;  

/*==============================================================*/
/* Table: sys_job                                               */
/*==============================================================*/
CREATE TABLE sys_job (
job_id SERIAL8 NOT NULL,
job_name VARCHAR ( 64 ) NOT NULL DEFAULT '',
job_group VARCHAR ( 64 ) NOT NULL DEFAULT 'DEFAULT',
invoke_target VARCHAR ( 500 ) NOT NULL,
cron_expression VARCHAR ( 255 ) NULL DEFAULT '',
misfire_policy VARCHAR ( 20 ) NULL DEFAULT '3',
concurrent CHAR ( 1 ) NULL DEFAULT '1',
status CHAR ( 1 ) NULL DEFAULT '0',
create_by VARCHAR ( 64 ) NULL DEFAULT '',
create_time TIMESTAMP NULL DEFAULT NULL,
update_by VARCHAR ( 64 ) NULL DEFAULT '',
update_time TIMESTAMP NULL DEFAULT NULL,
remark VARCHAR ( 500 ) NULL DEFAULT '',
CONSTRAINT PK_SYS_JOB PRIMARY KEY ( job_id, job_name, job_group ) 
);
COMMENT ON TABLE sys_job IS '定时任务调度表';
COMMENT ON COLUMN sys_job.job_id IS '任务ID';
COMMENT ON COLUMN sys_job.job_name IS '任务名称';
COMMENT ON COLUMN sys_job.job_group IS '任务组名';
COMMENT ON COLUMN sys_job.invoke_target IS '调用目标字符串';
COMMENT ON COLUMN sys_job.cron_expression IS 'cron执行表达式';
COMMENT ON COLUMN sys_job.misfire_policy IS '计划执行错误策略（1立即执行 2执行一次 3放弃执行）';
COMMENT ON COLUMN sys_job.concurrent IS '是否并发执行（0允许 1禁止）';
COMMENT ON COLUMN sys_job.status IS '状态（0正常 1暂停）';
COMMENT ON COLUMN sys_job.create_by IS '创建者';
COMMENT ON COLUMN sys_job.create_time IS '创建时间';
COMMENT ON COLUMN sys_job.update_by IS '更新者';
COMMENT ON COLUMN sys_job.update_time IS '更新时间';
COMMENT ON COLUMN sys_job.remark IS '备注信息';
/*==============================================================*/
/* Table: sys_job_log                                           */
/*==============================================================*/
CREATE TABLE sys_job_log (
job_log_id SERIAL8 NOT NULL,
job_name VARCHAR ( 64 ) NOT NULL,
job_group VARCHAR ( 64 ) NOT NULL,
invoke_target VARCHAR ( 500 ) NOT NULL,
job_message VARCHAR ( 500 ) NULL DEFAULT NULL,
status CHAR ( 1 ) NULL DEFAULT '0',
exception_info VARCHAR ( 2000 ) NULL DEFAULT '',
create_time TIMESTAMP NULL DEFAULT NULL,
CONSTRAINT PK_SYS_JOB_LOG PRIMARY KEY ( job_log_id ) 
);
COMMENT ON TABLE sys_job_log IS '定时任务调度日志表';
COMMENT ON COLUMN sys_job_log.job_log_id IS '任务日志ID';
COMMENT ON COLUMN sys_job_log.job_name IS '任务名称';
COMMENT ON COLUMN sys_job_log.job_group IS '任务组名';
COMMENT ON COLUMN sys_job_log.invoke_target IS '调用目标字符串';
COMMENT ON COLUMN sys_job_log.job_message IS '日志信息';
COMMENT ON COLUMN sys_job_log.status IS '执行状态（0正常 1失败）';
COMMENT ON COLUMN sys_job_log.exception_info IS '异常信息';
COMMENT ON COLUMN sys_job_log.create_time IS '创建时间';
/*==============================================================*/
/* Table: qrtz_blob_triggers                                    */
/*==============================================================*/
CREATE TABLE qrtz_blob_triggers (
sched_name VARCHAR ( 100 ) NOT NULL,
TRIGGER_NAME VARCHAR ( 150 ) NOT NULL,
trigger_group VARCHAR ( 100 ) NOT NULL,
blob_data BYTEA NULL,
CONSTRAINT PK_QRTZ_BLOB_TRIGGERS PRIMARY KEY ( sched_name, TRIGGER_NAME, trigger_group ) 
);
COMMENT ON TABLE qrtz_blob_triggers IS 'Trigger 作为 Blob 类型存储(用于 Quartz 用户用 JDBC 创建他们自己定制的 Trigger 类型，JobStore 并不知道如何存储实例的时候)';
/*==============================================================*/
/* Table: qrtz_calendars                                        */
/*==============================================================*/
CREATE TABLE qrtz_calendars (
sched_name VARCHAR ( 100 ) NOT NULL,
calendar_name VARCHAR ( 150 ) NOT NULL,
calendar BYTEA NOT NULL,
CONSTRAINT PK_QRTZ_CALENDARS PRIMARY KEY ( sched_name, calendar_name ) 
);
COMMENT ON TABLE qrtz_calendars IS '以 Blob 类型存储存放日历信息， quartz可配置一个日历来指定一个时间范围';
/*==============================================================*/
/* Table: qrtz_cron_triggers                                    */
/*==============================================================*/
CREATE TABLE qrtz_cron_triggers (
sched_name VARCHAR ( 100 ) NOT NULL,
TRIGGER_NAME VARCHAR ( 150 ) NOT NULL,
trigger_group VARCHAR ( 100 ) NOT NULL,
cron_expression VARCHAR ( 200 ) NOT NULL,
time_zone_id VARCHAR ( 80 ) NULL DEFAULT NULL,
CONSTRAINT PK_QRTZ_CRON_TRIGGERS PRIMARY KEY ( sched_name, TRIGGER_NAME, trigger_group ) 
);
COMMENT ON TABLE qrtz_cron_triggers IS '存储 Cron Trigger，包括 Cron 表达式和时区信息';
/*==============================================================*/
/* Table: qrtz_fired_triggers                                   */
/*==============================================================*/
CREATE TABLE qrtz_fired_triggers (
sched_name VARCHAR ( 100 ) NOT NULL,
entry_id VARCHAR ( 95 ) NOT NULL,
TRIGGER_NAME VARCHAR ( 200 ) NOT NULL,
trigger_group VARCHAR ( 200 ) NOT NULL,
instance_name VARCHAR ( 200 ) NOT NULL,
fired_time INT8 NOT NULL,
sched_time INT8 NOT NULL,
priority INT4 NOT NULL,
STATE VARCHAR ( 16 ) NOT NULL,
job_name VARCHAR ( 200 ) NULL DEFAULT NULL,
job_group VARCHAR ( 200 ) NULL DEFAULT NULL,
is_nonconcurrent VARCHAR ( 5 ) NULL DEFAULT NULL,
requests_recovery VARCHAR ( 5 ) NULL DEFAULT NULL,
CONSTRAINT PK_QRTZ_FIRED_TRIGGERS PRIMARY KEY ( sched_name, entry_id ) 
);
COMMENT ON TABLE qrtz_fired_triggers IS '存储与已触发的 Trigger 相关的状态信息，以及相联 Job 的执行信息';
/*==============================================================*/
/* Table: qrtz_job_details                                      */
/*==============================================================*/
CREATE TABLE qrtz_job_details (
sched_name VARCHAR ( 100 ) NOT NULL,
job_name VARCHAR ( 150 ) NOT NULL,
job_group VARCHAR ( 100 ) NOT NULL,
description VARCHAR ( 250 ) NULL DEFAULT NULL,
job_class_name VARCHAR ( 250 ) NOT NULL,
is_durable VARCHAR ( 5 ) NOT NULL,
is_nonconcurrent VARCHAR ( 5 ) NOT NULL,
is_update_data VARCHAR ( 5 ) NOT NULL,
requests_recovery VARCHAR ( 5 ) NOT NULL,
job_data BYTEA NULL,
CONSTRAINT PK_QRTZ_JOB_DETAILS PRIMARY KEY ( sched_name, job_name, job_group ) 
);
COMMENT ON TABLE qrtz_job_details IS '存储每一个已配置的 jobDetail 的详细信息';
/*==============================================================*/
/* Table: qrtz_locks                                            */
/*==============================================================*/
CREATE TABLE qrtz_locks ( sched_name VARCHAR ( 120 ) NOT NULL, lock_name VARCHAR ( 40 ) NOT NULL, CONSTRAINT PK_QRTZ_LOCKS PRIMARY KEY ( sched_name, lock_name ) );
COMMENT ON TABLE qrtz_locks IS '存储程序的悲观锁的信息(假如使用了悲观锁)';
/*==============================================================*/
/* Table: qrtz_paused_trigger_grps                              */
/*==============================================================*/
CREATE TABLE qrtz_paused_trigger_grps ( sched_name VARCHAR ( 120 ) NOT NULL, trigger_group VARCHAR ( 200 ) NOT NULL, CONSTRAINT PK_QRTZ_PAUSED_TRIGGER_GRPS PRIMARY KEY ( sched_name, trigger_group ) );
COMMENT ON TABLE qrtz_paused_trigger_grps IS '存储已暂停的 Trigger 组的信息';
/*==============================================================*/
/* Table: qrtz_scheduler_state                                  */
/*==============================================================*/
CREATE TABLE qrtz_scheduler_state (
sched_name VARCHAR ( 100 ) NOT NULL,
instance_name VARCHAR ( 150 ) NOT NULL,
last_checkin_time INT8 NOT NULL,
checkin_interval INT8 NOT NULL,
CONSTRAINT PK_QRTZ_SCHEDULER_STATE PRIMARY KEY ( sched_name, instance_name ) 
);
COMMENT ON TABLE qrtz_scheduler_state IS ' 存储少量的有关 Scheduler 的状态信息，假如是用于集群中，可以看到其他的 Scheduler 实例';
/*==============================================================*/
/* Table: qrtz_simple_triggers                                  */
/*==============================================================*/
CREATE TABLE qrtz_simple_triggers (
sched_name VARCHAR ( 100 ) NOT NULL,
TRIGGER_NAME VARCHAR ( 150 ) NOT NULL,
trigger_group VARCHAR ( 100 ) NOT NULL,
repeat_count INT8 NOT NULL,
repeat_interval INT8 NOT NULL,
times_triggered INT8 NOT NULL,
CONSTRAINT PK_QRTZ_SIMPLE_TRIGGERS PRIMARY KEY ( sched_name, TRIGGER_NAME, trigger_group ) 
);
COMMENT ON TABLE qrtz_simple_triggers IS '存储简单的 Trigger，包括重复次数，间隔，以及已触发的次数';
/*==============================================================*/
/* Table: qrtz_simprop_triggers                                 */
/*==============================================================*/
CREATE TABLE qrtz_simprop_triggers (
sched_name VARCHAR ( 100 ) NOT NULL,
TRIGGER_NAME VARCHAR ( 150 ) NOT NULL,
trigger_group VARCHAR ( 100 ) NOT NULL,
str_prop_1 VARCHAR ( 512 ) NULL DEFAULT NULL,
str_prop_2 VARCHAR ( 512 ) NULL DEFAULT NULL,
str_prop_3 VARCHAR ( 512 ) NULL DEFAULT NULL,
int_prop_1 INT4 NULL DEFAULT NULL,
int_prop_2 INT4 NULL DEFAULT NULL,
long_prop_1 INT8 NULL DEFAULT NULL,
long_prop_2 INT8 NULL DEFAULT NULL,
dec_prop_1 DECIMAL ( 13, 4 ) NULL DEFAULT NULL,
dec_prop_2 DECIMAL ( 13, 4 ) NULL DEFAULT NULL,
bool_prop_1 VARCHAR ( 5 ) NULL DEFAULT NULL,
bool_prop_2 VARCHAR ( 5 ) NULL DEFAULT NULL,
CONSTRAINT PK_QRTZ_SIMPROP_TRIGGERS PRIMARY KEY ( sched_name, TRIGGER_NAME, trigger_group ) 
);
/*==============================================================*/
/* Table: qrtz_triggers                                         */
/*==============================================================*/
CREATE TABLE qrtz_triggers (
sched_name VARCHAR ( 100 ) NOT NULL,
TRIGGER_NAME VARCHAR ( 150 ) NOT NULL,
trigger_group VARCHAR ( 100 ) NOT NULL,
job_name VARCHAR ( 150 ) NOT NULL,
job_group VARCHAR ( 200 ) NOT NULL,
description VARCHAR ( 250 ) NULL DEFAULT NULL,
next_fire_time INT8 NULL DEFAULT NULL,
prev_fire_time INT8 NULL DEFAULT NULL,
priority INT4 NULL DEFAULT NULL,
trigger_state VARCHAR ( 16 ) NOT NULL,
trigger_type VARCHAR ( 8 ) NOT NULL,
start_time INT8 NOT NULL,
end_time INT8 NULL DEFAULT NULL,
calendar_name VARCHAR ( 200 ) NULL DEFAULT NULL,
misfire_instr INT2 NULL DEFAULT NULL,
job_data BYTEA NULL,
CONSTRAINT PK_QRTZ_TRIGGERS PRIMARY KEY ( sched_name, TRIGGER_NAME, trigger_group )
);
COMMENT ON TABLE qrtz_triggers IS '存储已配置的 Trigger 的信息';
ALTER TABLE qrtz_triggers ADD CONSTRAINT qrtz_triggers_ibfk_1 FOREIGN KEY ( sched_name, job_name, job_group ) REFERENCES qrtz_job_details ( sched_name, job_name, job_group );

/*==============================================================*/
/* Table: sys_config                                            */
/*==============================================================*/
CREATE TABLE sys_config (
config_id SERIAL8 NOT NULL,
config_name VARCHAR ( 100 ) NULL DEFAULT '',
config_key VARCHAR ( 100 ) NULL DEFAULT '',
config_value VARCHAR ( 500 ) NULL DEFAULT '',
config_type CHAR ( 1 ) NULL DEFAULT 'N',
create_by VARCHAR ( 64 ) NULL DEFAULT '',
create_time TIMESTAMP NULL DEFAULT NULL,
update_by VARCHAR ( 64 ) NULL DEFAULT '',
update_time TIMESTAMP NULL DEFAULT NULL,
remark VARCHAR ( 500 ) NULL DEFAULT NULL,
CONSTRAINT PK_SYS_CONFIG PRIMARY KEY ( config_id ) 
);
COMMENT ON TABLE sys_config IS '参数配置表';
COMMENT ON COLUMN sys_config.config_id IS '参数主键';
COMMENT ON COLUMN sys_config.config_name IS '参数名称';
COMMENT ON COLUMN sys_config.config_key IS '参数键名';
COMMENT ON COLUMN sys_config.config_value IS '参数键值';
COMMENT ON COLUMN sys_config.config_type IS '系统内置（Y是 N否）';
COMMENT ON COLUMN sys_config.create_by IS '创建者';
COMMENT ON COLUMN sys_config.create_time IS '创建时间';
COMMENT ON COLUMN sys_config.update_by IS '更新者';
COMMENT ON COLUMN sys_config.update_time IS '更新时间';
COMMENT ON COLUMN sys_config.remark IS '备注';
/*==============================================================*/
/* Table: sys_dept                                              */
/*==============================================================*/
CREATE TABLE sys_dept (
dept_id SERIAL8 NOT NULL,
parent_id INT8 NULL DEFAULT 0,
ancestors VARCHAR ( 50 ) NULL DEFAULT '',
dept_code VARCHAR( 50 )  NULL DEFAULT '',
dept_name VARCHAR ( 30 ) NULL DEFAULT '',
order_num INT4 NULL DEFAULT 0,
leader VARCHAR ( 20 ) NULL DEFAULT NULL,
phone VARCHAR ( 11 ) NULL DEFAULT NULL,
email VARCHAR ( 50 ) NULL DEFAULT NULL,
status CHAR ( 1 ) NULL DEFAULT '0',
del_flag CHAR ( 1 ) NULL DEFAULT '0',
create_by VARCHAR ( 64 ) NULL DEFAULT '',
create_time TIMESTAMP NULL DEFAULT NULL,
update_by VARCHAR ( 64 ) NULL DEFAULT '',
update_time TIMESTAMP NULL DEFAULT NULL,
CONSTRAINT PK_SYS_DEPT PRIMARY KEY ( dept_id ) 
);
COMMENT ON TABLE sys_dept IS '部门表';
COMMENT ON COLUMN sys_dept.dept_id IS '部门id';
COMMENT ON COLUMN sys_dept.parent_id IS '父部门id';
COMMENT ON COLUMN sys_dept.ancestors IS '祖级列表';
COMMENT ON COLUMN sys_dept.dept_code IS '部门编码';
COMMENT ON COLUMN sys_dept.dept_name IS '部门名称';
COMMENT ON COLUMN sys_dept.order_num IS '显示顺序';
COMMENT ON COLUMN sys_dept.leader IS '负责人';
COMMENT ON COLUMN sys_dept.phone IS '联系电话';
COMMENT ON COLUMN sys_dept.email IS '邮箱';
COMMENT ON COLUMN sys_dept.status IS '部门状态（0正常 1停用）';
COMMENT ON COLUMN sys_dept.del_flag IS '删除标志（0代表存在 2代表删除）';
COMMENT ON COLUMN sys_dept.create_by IS '创建者';
COMMENT ON COLUMN sys_dept.create_time IS '创建时间';
COMMENT ON COLUMN sys_dept.update_by IS '更新者';
COMMENT ON COLUMN sys_dept.update_time IS '更新时间';
/*==============================================================*/
/* Table: sys_dict_data                                         */
/*==============================================================*/
CREATE TABLE sys_dict_data (
dict_code SERIAL8 NOT NULL,
dict_sort INT4 NULL DEFAULT 0,
dict_label VARCHAR ( 100 ) NULL DEFAULT '',
dict_value VARCHAR ( 100 ) NULL DEFAULT '',
dict_type VARCHAR ( 100 ) NULL DEFAULT '',
css_class VARCHAR ( 100 ) NULL DEFAULT NULL,
list_class VARCHAR ( 100 ) NULL DEFAULT NULL,
is_default CHAR ( 1 ) NULL DEFAULT 'N',
status CHAR ( 1 ) NULL DEFAULT '0',
create_by VARCHAR ( 64 ) NULL DEFAULT '',
create_time TIMESTAMP NULL DEFAULT NULL,
update_by VARCHAR ( 64 ) NULL DEFAULT '',
update_time TIMESTAMP NULL DEFAULT NULL,
remark VARCHAR ( 500 ) NULL DEFAULT NULL,
CONSTRAINT PK_SYS_DICT_DATA PRIMARY KEY ( dict_code ) 
);
COMMENT ON TABLE sys_dict_data IS '字典数据表';
COMMENT ON COLUMN sys_dict_data.dict_code IS '字典编码';
COMMENT ON COLUMN sys_dict_data.dict_sort IS '字典排序';
COMMENT ON COLUMN sys_dict_data.dict_label IS '字典标签';
COMMENT ON COLUMN sys_dict_data.dict_value IS '字典键值';
COMMENT ON COLUMN sys_dict_data.dict_type IS '字典类型';
COMMENT ON COLUMN sys_dict_data.css_class IS '样式属性（其他样式扩展）';
COMMENT ON COLUMN sys_dict_data.list_class IS '表格回显样式';
COMMENT ON COLUMN sys_dict_data.is_default IS '是否默认（Y是 N否）';
COMMENT ON COLUMN sys_dict_data.status IS '状态（0正常 1停用）';
COMMENT ON COLUMN sys_dict_data.create_by IS '创建者';
COMMENT ON COLUMN sys_dict_data.create_time IS '创建时间';
COMMENT ON COLUMN sys_dict_data.update_by IS '更新者';
COMMENT ON COLUMN sys_dict_data.update_time IS '更新时间';
COMMENT ON COLUMN sys_dict_data.remark IS '备注';
/*==============================================================*/
/* Table: sys_dict_type                                         */
/*==============================================================*/
CREATE TABLE sys_dict_type (
dict_id SERIAL8 NOT NULL,
dict_name VARCHAR ( 100 ) NULL DEFAULT '',
dict_type VARCHAR ( 100 ) NULL DEFAULT '',
status CHAR ( 1 ) NULL DEFAULT '0',
create_by VARCHAR ( 64 ) NULL DEFAULT '',
create_time TIMESTAMP NULL DEFAULT NULL,
update_by VARCHAR ( 64 ) NULL DEFAULT '',
update_time TIMESTAMP NULL DEFAULT NULL,
remark VARCHAR ( 500 ) NULL DEFAULT NULL,
CONSTRAINT PK_SYS_DICT_TYPE PRIMARY KEY ( dict_id ),
CONSTRAINT dict_type UNIQUE ( dict_type ) 
);
COMMENT ON TABLE sys_dict_type IS '字典类型表';
COMMENT ON COLUMN sys_dict_type.dict_id IS '字典主键';
COMMENT ON COLUMN sys_dict_type.dict_name IS '字典名称';
COMMENT ON COLUMN sys_dict_type.dict_type IS '字典类型';
COMMENT ON COLUMN sys_dict_type.status IS '状态（0正常 1停用）';
COMMENT ON COLUMN sys_dict_type.create_by IS '创建者';
COMMENT ON COLUMN sys_dict_type.create_time IS '创建时间';
COMMENT ON COLUMN sys_dict_type.update_by IS '更新者';
COMMENT ON COLUMN sys_dict_type.update_time IS '更新时间';
COMMENT ON COLUMN sys_dict_type.remark IS '备注';
/*==============================================================*/
/* Table: sys_logininfor                                        */
/*==============================================================*/
CREATE TABLE sys_logininfor (
info_id SERIAL8 NOT NULL,
user_name VARCHAR ( 50 ) NULL DEFAULT '',
ipaddr VARCHAR ( 50 ) NULL DEFAULT '',
login_location VARCHAR ( 255 ) NULL DEFAULT '',
browser VARCHAR ( 50 ) NULL DEFAULT '',
os VARCHAR ( 50 ) NULL DEFAULT '',
status CHAR ( 1 ) NULL DEFAULT '0',
msg VARCHAR ( 255 ) NULL DEFAULT '',
login_time TIMESTAMP NULL DEFAULT NULL,
CONSTRAINT PK_SYS_LOGININFOR PRIMARY KEY ( info_id ) 
);
COMMENT ON TABLE sys_logininfor IS '系统访问记录';
COMMENT ON COLUMN sys_logininfor.info_id IS '访问ID';
COMMENT ON COLUMN sys_logininfor.user_name IS '登录账号';
COMMENT ON COLUMN sys_logininfor.ipaddr IS '登录IP地址';
COMMENT ON COLUMN sys_logininfor.login_location IS '登录地点';
COMMENT ON COLUMN sys_logininfor.browser IS '浏览器类型';
COMMENT ON COLUMN sys_logininfor.os IS '操作系统';
COMMENT ON COLUMN sys_logininfor.status IS '登录状态（0成功 1失败）';
COMMENT ON COLUMN sys_logininfor.msg IS '提示消息';
COMMENT ON COLUMN sys_logininfor.login_time IS '访问时间';
/*==============================================================*/
/* Table: sys_menu                                              */
/*==============================================================*/
CREATE TABLE sys_menu (
menu_id SERIAL8 NOT NULL,
menu_name VARCHAR ( 50 ) NOT NULL,
parent_id INT8 NULL DEFAULT 0,
order_num INT4 NULL DEFAULT 0,
path  VARCHAR ( 200 ) DEFAULT '',
component  VARCHAR ( 255 ) DEFAULT NULL,
is_frame  INT2 DEFAULT 1,
is_cache INT2 DEFAULT 0,
menu_type CHAR ( 1 ) NULL DEFAULT '',
visible CHAR ( 1 ) NULL DEFAULT '0',
status CHAR ( 1 ) DEFAULT '0',
perms VARCHAR ( 100 ) NULL DEFAULT NULL,
icon VARCHAR ( 100 ) NULL DEFAULT '#',
create_by VARCHAR ( 64 ) NULL DEFAULT '',
create_time TIMESTAMP NULL DEFAULT NULL,
update_by VARCHAR ( 64 ) NULL DEFAULT '',
update_time TIMESTAMP NULL DEFAULT NULL,
remark VARCHAR ( 500 ) NULL DEFAULT '',
CONSTRAINT PK_SYS_MENU PRIMARY KEY ( menu_id ) 
);
COMMENT ON TABLE sys_menu IS '菜单权限表';
COMMENT ON COLUMN sys_menu.menu_id IS '菜单ID';
COMMENT ON COLUMN sys_menu.menu_name IS '菜单名称';
COMMENT ON COLUMN sys_menu.parent_id IS '父菜单ID';
COMMENT ON COLUMN sys_menu.order_num IS '显示顺序';
COMMENT ON COLUMN sys_menu.path IS '路由地址';
COMMENT ON COLUMN sys_menu.component IS '路由组件';
COMMENT ON COLUMN sys_menu.is_frame IS '是否为外链（0是 1否）';
COMMENT ON COLUMN sys_menu.is_cache IS '是否缓存（0缓存 1不缓存）';
COMMENT ON COLUMN sys_menu.menu_type IS '菜单类型（M目录 C菜单 F按钮）';
COMMENT ON COLUMN sys_menu.visible IS '菜单状态（0显示 1隐藏）';
COMMENT ON COLUMN sys_menu.status IS '菜单状态（0正常 1停用）';
COMMENT ON COLUMN sys_menu.perms IS '权限标识';
COMMENT ON COLUMN sys_menu.icon IS '菜单图标';
COMMENT ON COLUMN sys_menu.create_by IS '创建者';
COMMENT ON COLUMN sys_menu.create_time IS '创建时间';
COMMENT ON COLUMN sys_menu.update_by IS '更新者';
COMMENT ON COLUMN sys_menu.update_time IS '更新时间';
COMMENT ON COLUMN sys_menu.remark IS '备注';
/*==============================================================*/
/* Table: sys_notice                                            */
/*==============================================================*/
CREATE TABLE sys_notice (
notice_id SERIAL8 NOT NULL,
notice_title VARCHAR ( 50 ) NOT NULL,
notice_type CHAR ( 1 ) NOT NULL,
notice_content VARCHAR ( 2000 ) NULL DEFAULT NULL,
status CHAR ( 1 ) NULL DEFAULT '0',
create_by VARCHAR ( 64 ) NULL DEFAULT '',
create_time TIMESTAMP NULL DEFAULT NULL,
update_by VARCHAR ( 64 ) NULL DEFAULT '',
update_time TIMESTAMP NULL DEFAULT NULL,
remark VARCHAR ( 255 ) NULL DEFAULT NULL,
CONSTRAINT PK_SYS_NOTICE PRIMARY KEY ( notice_id ) 
);
COMMENT ON TABLE sys_notice IS '通知公告表';
COMMENT ON COLUMN sys_notice.notice_id IS '公告ID';
COMMENT ON COLUMN sys_notice.notice_title IS '公告标题';
COMMENT ON COLUMN sys_notice.notice_type IS '公告类型（1通知 2公告）';
COMMENT ON COLUMN sys_notice.notice_content IS '公告内容';
COMMENT ON COLUMN sys_notice.status IS '公告状态（0正常 1关闭）';
COMMENT ON COLUMN sys_notice.create_by IS '创建者';
COMMENT ON COLUMN sys_notice.create_time IS '创建时间';
COMMENT ON COLUMN sys_notice.update_by IS '更新者';
COMMENT ON COLUMN sys_notice.update_time IS '更新时间';
COMMENT ON COLUMN sys_notice.remark IS '备注';
/*==============================================================*/
/* Table: sys_oper_log                                          */
/*==============================================================*/
CREATE TABLE sys_oper_log (
oper_id SERIAL8 NOT NULL,
title VARCHAR ( 50 ) NULL DEFAULT '',
business_type INT4 NULL DEFAULT 0,
METHOD VARCHAR ( 100 ) NULL DEFAULT '',
request_method VARCHAR ( 10 ) NULL DEFAULT '',
operator_type INT4 NULL DEFAULT 0,
oper_name VARCHAR ( 50 ) NULL DEFAULT '',
dept_name VARCHAR ( 50 ) NULL DEFAULT '',
oper_url VARCHAR ( 255 ) NULL DEFAULT '',
oper_ip VARCHAR ( 50 ) NULL DEFAULT '',
oper_location VARCHAR ( 255 ) NULL DEFAULT '',
oper_param VARCHAR ( 2000 ) NULL DEFAULT '',
json_result VARCHAR ( 2000 ) NULL DEFAULT '',
status INT4 NULL DEFAULT 0,
error_msg VARCHAR ( 2000 ) NULL DEFAULT '',
oper_time TIMESTAMP NULL DEFAULT NULL,
CONSTRAINT PK_SYS_OPER_LOG PRIMARY KEY ( oper_id ) 
);
COMMENT ON TABLE sys_oper_log IS '操作日志记录';
COMMENT ON COLUMN sys_oper_log.oper_id IS '日志主键';
COMMENT ON COLUMN sys_oper_log.title IS '模块标题';
COMMENT ON COLUMN sys_oper_log.business_type IS '业务类型（0其它 1新增 2修改 3删除）';
COMMENT ON COLUMN sys_oper_log.METHOD IS '方法名称';
COMMENT ON COLUMN sys_oper_log.request_method IS '请求方式';
COMMENT ON COLUMN sys_oper_log.operator_type IS '操作类别（0其它 1后台用户 2手机端用户）';
COMMENT ON COLUMN sys_oper_log.oper_name IS '操作人员';
COMMENT ON COLUMN sys_oper_log.dept_name IS '部门名称';
COMMENT ON COLUMN sys_oper_log.oper_url IS '请求URL';
COMMENT ON COLUMN sys_oper_log.oper_ip IS '主机地址';
COMMENT ON COLUMN sys_oper_log.oper_location IS '操作地点';
COMMENT ON COLUMN sys_oper_log.oper_param IS '请求参数';
COMMENT ON COLUMN sys_oper_log.json_result IS '返回参数';
COMMENT ON COLUMN sys_oper_log.status IS '操作状态（0正常 1异常）';
COMMENT ON COLUMN sys_oper_log.error_msg IS '错误消息';
COMMENT ON COLUMN sys_oper_log.oper_time IS '操作时间';
/*==============================================================*/
/* Table: sys_post                                              */
/*==============================================================*/
CREATE TABLE sys_post (
post_id SERIAL8 NOT NULL,
post_code VARCHAR ( 64 ) NOT NULL,
post_name VARCHAR ( 50 ) NOT NULL,
post_sort INT4 NOT NULL,
status CHAR ( 1 ) NOT NULL,
create_by VARCHAR ( 64 ) NULL DEFAULT '',
create_time TIMESTAMP NULL DEFAULT NULL,
update_by VARCHAR ( 64 ) NULL DEFAULT '',
update_time TIMESTAMP NULL DEFAULT NULL,
remark VARCHAR ( 500 ) NULL DEFAULT NULL,
CONSTRAINT PK_SYS_POST PRIMARY KEY ( post_id ) 
);
COMMENT ON TABLE sys_post IS '岗位信息表';
COMMENT ON COLUMN sys_post.post_id IS '岗位ID';
COMMENT ON COLUMN sys_post.post_code IS '岗位编码';
COMMENT ON COLUMN sys_post.post_name IS '岗位名称';
COMMENT ON COLUMN sys_post.post_sort IS '显示顺序';
COMMENT ON COLUMN sys_post.status IS '状态（0正常 1停用）';
COMMENT ON COLUMN sys_post.create_by IS '创建者';
COMMENT ON COLUMN sys_post.create_time IS '创建时间';
COMMENT ON COLUMN sys_post.update_by IS '更新者';
COMMENT ON COLUMN sys_post.update_time IS '更新时间';
COMMENT ON COLUMN sys_post.remark IS '备注';
/*==============================================================*/
/* Table: sys_role                                              */
/*==============================================================*/
CREATE TABLE sys_role (
role_id SERIAL8 NOT NULL,
role_name VARCHAR ( 30 ) NOT NULL,
role_key VARCHAR ( 100 ) NOT NULL,
role_sort INT4 NOT NULL,
data_scope CHAR ( 1 ) NULL DEFAULT '1',
menu_check_strictly  INT2  DEFAULT 1,
dept_check_strictly  INT2  DEFAULT 1,
status CHAR ( 1 ) NOT NULL,
del_flag CHAR ( 1 ) NULL DEFAULT '0',
create_by VARCHAR ( 64 ) NULL DEFAULT '',
create_time TIMESTAMP NULL DEFAULT NULL,
update_by VARCHAR ( 64 ) NULL DEFAULT '',
update_time TIMESTAMP NULL DEFAULT NULL,
remark VARCHAR ( 500 ) NULL DEFAULT NULL,
CONSTRAINT PK_SYS_ROLE PRIMARY KEY ( role_id ) 
);
COMMENT ON TABLE sys_role IS '角色信息表';
COMMENT ON COLUMN sys_role.role_id IS '角色ID';
COMMENT ON COLUMN sys_role.role_name IS '角色名称';
COMMENT ON COLUMN sys_role.role_key IS '角色权限字符串';
COMMENT ON COLUMN sys_role.role_sort IS '显示顺序';
COMMENT ON COLUMN sys_role.data_scope IS '数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）';
COMMENT ON COLUMN sys_role.menu_check_strictly IS '菜单树选择项是否关联显示';
COMMENT ON COLUMN sys_role.dept_check_strictly IS '部门树选择项是否关联显示';
COMMENT ON COLUMN sys_role.status IS '角色状态（0正常 1停用）';
COMMENT ON COLUMN sys_role.del_flag IS '删除标志（0代表存在 2代表删除）';
COMMENT ON COLUMN sys_role.create_by IS '创建者';
COMMENT ON COLUMN sys_role.create_time IS '创建时间';
COMMENT ON COLUMN sys_role.update_by IS '更新者';
COMMENT ON COLUMN sys_role.update_time IS '更新时间';
COMMENT ON COLUMN sys_role.remark IS '备注';
/*==============================================================*/
/* Table: sys_role_dept                                         */
/*==============================================================*/
CREATE TABLE sys_role_dept ( role_id INT8 NOT NULL, dept_id INT8 NOT NULL, CONSTRAINT PK_SYS_ROLE_DEPT PRIMARY KEY ( role_id, dept_id ) );
COMMENT ON TABLE sys_role_dept IS '角色和部门关联表';
COMMENT ON COLUMN sys_role_dept.role_id IS '角色ID';
COMMENT ON COLUMN sys_role_dept.dept_id IS '部门ID';
/*==============================================================*/
/* Table: sys_role_menu                                         */
/*==============================================================*/
CREATE TABLE sys_role_menu ( role_id INT8 NOT NULL, menu_id INT8 NOT NULL, CONSTRAINT PK_SYS_ROLE_MENU PRIMARY KEY ( role_id, menu_id ) );
COMMENT ON TABLE sys_role_menu IS '角色和菜单关联表';
COMMENT ON COLUMN sys_role_menu.role_id IS '角色ID';
COMMENT ON COLUMN sys_role_menu.menu_id IS '菜单ID';
/*==============================================================*/
/* Table: sys_user                                              */
/*==============================================================*/
CREATE TABLE sys_user (
user_id SERIAL8 NOT NULL,
dept_id INT8 NULL DEFAULT NULL,
user_name VARCHAR ( 30 ) NOT NULL,
nick_name VARCHAR ( 30 ) NULL DEFAULT '',
user_type VARCHAR ( 2 ) NULL DEFAULT '00',
email VARCHAR ( 50 ) NULL DEFAULT '',
phonenumber VARCHAR ( 11 ) NULL DEFAULT '',
sex CHAR ( 1 ) NULL DEFAULT '0',
avatar VARCHAR ( 100 ) NULL DEFAULT '',
PASSWORD VARCHAR ( 100 ) NULL DEFAULT '',
salt VARCHAR ( 20 ) NULL DEFAULT '',
status CHAR ( 1 ) NULL DEFAULT '0',
del_flag CHAR ( 1 ) NULL DEFAULT '0',
login_ip VARCHAR ( 50 ) NULL DEFAULT '',
login_date TIMESTAMP NULL DEFAULT NULL,
create_by VARCHAR ( 64 ) NULL DEFAULT '',
create_time TIMESTAMP NULL DEFAULT NULL,
update_by VARCHAR ( 64 ) NULL DEFAULT '',
update_time TIMESTAMP NULL DEFAULT NULL,
remark VARCHAR ( 500 ) NULL DEFAULT NULL,
CONSTRAINT PK_SYS_USER PRIMARY KEY ( user_id ) 
);
COMMENT ON TABLE sys_user IS '用户信息表';
COMMENT ON COLUMN sys_user.user_id IS '用户ID';
COMMENT ON COLUMN sys_user.dept_id IS '部门ID';
COMMENT ON COLUMN sys_user.user_name IS '登录账号';
COMMENT ON COLUMN sys_user.nick_name IS '用户昵称';
COMMENT ON COLUMN sys_user.user_type IS '用户类型（00系统用户 01注册用户）';
COMMENT ON COLUMN sys_user.email IS '用户邮箱';
COMMENT ON COLUMN sys_user.phonenumber IS '手机号码';
COMMENT ON COLUMN sys_user.sex IS '用户性别（0男 1女 2未知）';
COMMENT ON COLUMN sys_user.avatar IS '头像路径';
COMMENT ON COLUMN sys_user.PASSWORD IS '密码';
COMMENT ON COLUMN sys_user.salt IS '盐加密';
COMMENT ON COLUMN sys_user.status IS '帐号状态（0正常 1停用）';
COMMENT ON COLUMN sys_user.del_flag IS '删除标志（0代表存在 2代表删除）';
COMMENT ON COLUMN sys_user.login_ip IS '最后登陆IP';
COMMENT ON COLUMN sys_user.login_date IS '最后登陆时间';
COMMENT ON COLUMN sys_user.create_by IS '创建者';
COMMENT ON COLUMN sys_user.create_time IS '创建时间';
COMMENT ON COLUMN sys_user.update_by IS '更新者';
COMMENT ON COLUMN sys_user.update_time IS '更新时间';
COMMENT ON COLUMN sys_user.remark IS '备注';
/*==============================================================*/
/* Table: sys_user_post                                         */
/*==============================================================*/
CREATE TABLE sys_user_post ( user_id INT8 NOT NULL, post_id INT8 NOT NULL, CONSTRAINT PK_SYS_USER_POST PRIMARY KEY ( user_id, post_id ) );
COMMENT ON TABLE sys_user_post IS '用户与岗位关联表';
COMMENT ON COLUMN sys_user_post.user_id IS '用户ID';
COMMENT ON COLUMN sys_user_post.post_id IS '岗位ID';
/*==============================================================*/
/* Table: sys_user_role                                         */
/*==============================================================*/
CREATE TABLE sys_user_role ( user_id INT8 NOT NULL, role_id INT8 NOT NULL, CONSTRAINT PK_SYS_USER_ROLE PRIMARY KEY ( user_id, role_id ) );
COMMENT ON TABLE sys_user_role IS '用户和角色关联表';
COMMENT ON COLUMN sys_user_role.user_id IS '用户ID';
COMMENT ON COLUMN sys_user_role.role_id IS '角色ID';

/*==============================================================*/
/* Table: gen_table                                             */
/*==============================================================*/
CREATE TABLE gen_table (
table_id SERIAL8 NOT NULL,
table_name VARCHAR ( 200 ) NULL DEFAULT '',
table_comment VARCHAR ( 500 ) NULL DEFAULT '',
class_name VARCHAR ( 100 ) NULL DEFAULT '',
tpl_category VARCHAR ( 200 ) NULL DEFAULT 'crud',
package_name VARCHAR ( 100 ) NULL DEFAULT NULL,
module_name VARCHAR ( 30 ) NULL DEFAULT NULL,
business_name VARCHAR ( 30 ) NULL DEFAULT NULL,
function_name VARCHAR ( 50 ) NULL DEFAULT NULL,
function_author VARCHAR ( 50 ) NULL DEFAULT NULL,
gen_type CHAR ( 1 ) DEFAULT '0',
gen_path VARCHAR ( 200 ) DEFAULT '/',
options VARCHAR ( 1000 ) NULL DEFAULT NULL,
create_by VARCHAR ( 64 ) NULL DEFAULT '',
create_time TIMESTAMP NULL DEFAULT NULL,
update_by VARCHAR ( 64 ) NULL DEFAULT '',
update_time TIMESTAMP NULL DEFAULT NULL,
remark VARCHAR ( 500 ) NULL DEFAULT NULL,
CONSTRAINT PK_GEN_TABLE PRIMARY KEY ( table_id ) 
);
COMMENT ON TABLE gen_table IS '代码生成业务表';
COMMENT ON COLUMN gen_table.table_id IS '编号';
COMMENT ON COLUMN gen_table.table_name IS '表名称';
COMMENT ON COLUMN gen_table.table_comment IS '表描述';
COMMENT ON COLUMN gen_table.class_name IS '实体类名称';
COMMENT ON COLUMN gen_table.tpl_category IS '使用的模板（crud单表操作 tree树表操作）';
COMMENT ON COLUMN gen_table.package_name IS '生成包路径';
COMMENT ON COLUMN gen_table.module_name IS '生成模块名';
COMMENT ON COLUMN gen_table.business_name IS '生成业务名';
COMMENT ON COLUMN gen_table.function_name IS '生成功能名';
COMMENT ON COLUMN gen_table.function_author IS '生成功能作者';
COMMENT ON COLUMN gen_table.gen_type IS '生成代码方式（0zip压缩包 1自定义路径）';
COMMENT ON COLUMN gen_table.gen_path IS '生成路径（不填默认项目路径）';
COMMENT ON COLUMN gen_table.options IS '其它生成选项';
COMMENT ON COLUMN gen_table.create_by IS '创建者';
COMMENT ON COLUMN gen_table.create_time IS '创建时间';
COMMENT ON COLUMN gen_table.update_by IS '更新者';
COMMENT ON COLUMN gen_table.update_time IS '更新时间';
COMMENT ON COLUMN gen_table.remark IS '备注';
/*==============================================================*/
/* Table: gen_table_column                                      */
/*==============================================================*/
CREATE TABLE gen_table_column (
column_id SERIAL8 NOT NULL,
table_id VARCHAR ( 64 ) NULL DEFAULT NULL,
COLUMN_NAME VARCHAR ( 200 ) NULL DEFAULT NULL,
column_comment VARCHAR ( 500 ) NULL DEFAULT NULL,
column_type VARCHAR ( 100 ) NULL DEFAULT NULL,
java_type VARCHAR ( 500 ) NULL DEFAULT NULL,
java_field VARCHAR ( 200 ) NULL DEFAULT NULL,
is_pk CHAR ( 1 ) NULL DEFAULT NULL,
is_increment CHAR ( 1 ) NULL DEFAULT NULL,
is_required CHAR ( 1 ) NULL DEFAULT NULL,
is_insert CHAR ( 1 ) NULL DEFAULT NULL,
is_edit CHAR ( 1 ) NULL DEFAULT NULL,
is_list CHAR ( 1 ) NULL DEFAULT NULL,
is_query CHAR ( 1 ) NULL DEFAULT NULL,
query_type VARCHAR ( 200 ) NULL DEFAULT 'EQ',
html_type VARCHAR ( 200 ) NULL DEFAULT NULL,
dict_type VARCHAR ( 200 ) NULL DEFAULT '',
sort INT4 NULL DEFAULT NULL,
create_by VARCHAR ( 64 ) NULL DEFAULT '',
create_time TIMESTAMP NULL DEFAULT NULL,
update_by VARCHAR ( 64 ) NULL DEFAULT '',
update_time TIMESTAMP NULL DEFAULT NULL,
CONSTRAINT PK_GEN_TABLE_COLUMN PRIMARY KEY ( column_id ) 
);
COMMENT ON TABLE gen_table_column IS '代码生成业务表字段';
COMMENT ON COLUMN gen_table_column.column_id IS '编号';
COMMENT ON COLUMN gen_table_column.table_id IS '归属表编号';
COMMENT ON COLUMN gen_table_column.COLUMN_NAME IS '列名称';
COMMENT ON COLUMN gen_table_column.column_comment IS '列描述';
COMMENT ON COLUMN gen_table_column.column_type IS '列类型';
COMMENT ON COLUMN gen_table_column.java_type IS 'JAVA类型';
COMMENT ON COLUMN gen_table_column.java_field IS 'JAVA字段名';
COMMENT ON COLUMN gen_table_column.is_pk IS '是否主键（1是）';
COMMENT ON COLUMN gen_table_column.is_increment IS '是否自增（1是）';
COMMENT ON COLUMN gen_table_column.is_required IS '是否必填（1是）';
COMMENT ON COLUMN gen_table_column.is_insert IS '是否为插入字段（1是）';
COMMENT ON COLUMN gen_table_column.is_edit IS '是否编辑字段（1是）';
COMMENT ON COLUMN gen_table_column.is_list IS '是否列表字段（1是）';
COMMENT ON COLUMN gen_table_column.is_query IS '是否查询字段（1是）';
COMMENT ON COLUMN gen_table_column.query_type IS '查询方式（等于、不等于、大于、小于、范围）';
COMMENT ON COLUMN gen_table_column.html_type IS '显示类型（文本框、文本域、下拉框、复选框、单选框、日期控件）';
COMMENT ON COLUMN gen_table_column.dict_type IS '字典类型';
COMMENT ON COLUMN gen_table_column.sort IS '排序';
COMMENT ON COLUMN gen_table_column.create_by IS '创建者';
COMMENT ON COLUMN gen_table_column.create_time IS '创建时间';
COMMENT ON COLUMN gen_table_column.update_by IS '更新者';
COMMENT ON COLUMN gen_table_column.update_time IS '更新时间';

INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark) VALUES (1, '主框架页-默认皮肤样式名称', 'sys.index.skinName', 'skin-blue', 'Y', 'admin', '2018-03-16 11:33:00', 'ry', '2018-03-16 11:33:00', '蓝色 skin-blue、绿色 skin-green、紫色 skin-purple、红色 skin-red、黄色 skin-yellow');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark) VALUES (2, '用户管理-账号初始密码', 'sys.user.initPassword', '123456', 'Y', 'admin', '2018-03-16 11:33:00', 'ry', '2018-03-16 11:33:00', '初始化密码 123456');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark) VALUES (3, '主框架页-侧边栏主题', 'sys.index.sideTheme', 'theme-dark', 'Y', 'admin', '2018-03-16 11:33:00', 'ry', '2018-03-16 11:33:00', '深黑主题theme-dark，浅色主题theme-light，深蓝主题theme-blue');

INSERT INTO sys_dept(dept_id, parent_id, ancestors, dept_code, dept_name, order_num, leader, phone, email, status, del_flag, create_by, create_time, update_by, update_time) VALUES (99, 100, '0,100', 'default', '默认部门', 0, '眼神', '15888888888', 'default@eyecool.cn', '0', '0', 'admin', '2018-03-16 11:33:00', 'admin', '2020-01-10 11:31:27');
INSERT INTO sys_dept(dept_id, parent_id, ancestors, dept_code, dept_name, order_num, leader, phone, email, status, del_flag, create_by, create_time, update_by, update_time) VALUES (100, 0, '0', 'eyecool', '眼神科技', 0, '眼神', '15888888888', 'eyecool@qq.com', '0', '0', 'admin', '2018-03-16 11:33:00', 'admin', '2020-04-15 11:07:38.317453');

INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (1, '用户性别', 'sys_user_sex', '0', 'admin', '2018-03-16 11:33:00', 'ry', '2018-03-16 11:33:00', '用户性别列表');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (2, '菜单状态', 'sys_show_hide', '0', 'admin', '2018-03-16 11:33:00', 'ry', '2018-03-16 11:33:00', '菜单状态列表');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (3, '系统开关', 'sys_normal_disable', '0', 'admin', '2018-03-16 11:33:00', 'ry', '2018-03-16 11:33:00', '系统开关列表');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (4, '任务状态', 'sys_job_status', '0', 'admin', '2018-03-16 11:33:00', 'ry', '2018-03-16 11:33:00', '任务状态列表');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (5, '任务分组', 'sys_job_group', '0', 'admin', '2018-03-16 11:33:00', 'ry', '2018-03-16 11:33:00', '任务分组列表');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (6, '系统是否', 'sys_yes_no', '0', 'admin', '2018-03-16 11:33:00', 'ry', '2018-03-16 11:33:00', '系统是否列表');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (7, '通知类型', 'sys_notice_type', '0', 'admin', '2018-03-16 11:33:00', 'ry', '2018-03-16 11:33:00', '通知类型列表');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (8, '通知状态', 'sys_notice_status', '0', 'admin', '2018-03-16 11:33:00', 'ry', '2018-03-16 11:33:00', '通知状态列表');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (9, '操作类型', 'sys_oper_type', '0', 'admin', '2018-03-16 11:33:00', 'ry', '2018-03-16 11:33:00', '操作类型列表');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (10, '系统状态', 'sys_common_status', '0', 'admin', '2018-03-16 11:33:00', 'ry', '2018-03-16 11:33:00', '登录状态列表');


INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (1, 1, '男', '0', 'sys_user_sex', '', '', 'Y', '0', 'admin', '2018-03-16 11:33:00', 'ry', '2018-03-16 11:33:00', '性别男');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (2, 2, '女', '1', 'sys_user_sex', '', '', 'N', '0', 'admin', '2018-03-16 11:33:00', 'ry', '2018-03-16 11:33:00', '性别女');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (3, 3, '未知', '2', 'sys_user_sex', '', '', 'N', '0', 'admin', '2018-03-16 11:33:00', 'ry', '2018-03-16 11:33:00', '性别未知');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (4, 1, '显示', '0', 'sys_show_hide', '', 'primary', 'Y', '0', 'admin', '2018-03-16 11:33:00', 'ry', '2018-03-16 11:33:00', '显示菜单');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (5, 2, '隐藏', '1', 'sys_show_hide', '', 'danger', 'N', '0', 'admin', '2018-03-16 11:33:00', 'ry', '2018-03-16 11:33:00', '隐藏菜单');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (6, 1, '正常', '0', 'sys_normal_disable', '', 'primary', 'Y', '0', 'admin', '2018-03-16 11:33:00', 'ry', '2018-03-16 11:33:00', '正常状态');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (7, 2, '停用', '1', 'sys_normal_disable', '', 'danger', 'N', '0', 'admin', '2018-03-16 11:33:00', 'ry', '2018-03-16 11:33:00', '停用状态');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (8, 1, '正常', '0', 'sys_job_status', '', 'primary', 'Y', '0', 'admin', '2018-03-16 11:33:00', 'ry', '2018-03-16 11:33:00', '正常状态');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (9, 2, '暂停', '1', 'sys_job_status', '', 'danger', 'N', '0', 'admin', '2018-03-16 11:33:00', 'ry', '2018-03-16 11:33:00', '停用状态');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (10, 1, '默认', 'DEFAULT', 'sys_job_group', '', '', 'Y', '0', 'admin', '2018-03-16 11:33:00', 'ry', '2018-03-16 11:33:00', '默认分组');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (11, 2, '系统', 'SYSTEM', 'sys_job_group', '', '', 'N', '0', 'admin', '2018-03-16 11:33:00', 'ry', '2018-03-16 11:33:00', '系统分组');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (12, 1, '是', 'Y', 'sys_yes_no', '', 'primary', 'Y', '0', 'admin', '2018-03-16 11:33:00', 'ry', '2018-03-16 11:33:00', '系统默认是');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (13, 2, '否', 'N', 'sys_yes_no', '', 'danger', 'N', '0', 'admin', '2018-03-16 11:33:00', 'ry', '2018-03-16 11:33:00', '系统默认否');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (14, 1, '通知', '1', 'sys_notice_type', '', 'warning', 'Y', '0', 'admin', '2018-03-16 11:33:00', 'ry', '2018-03-16 11:33:00', '通知');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (15, 2, '公告', '2', 'sys_notice_type', '', 'success', 'N', '0', 'admin', '2018-03-16 11:33:00', 'ry', '2018-03-16 11:33:00', '公告');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (16, 1, '正常', '0', 'sys_notice_status', '', 'primary', 'Y', '0', 'admin', '2018-03-16 11:33:00', 'ry', '2018-03-16 11:33:00', '正常状态');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (17, 2, '关闭', '1', 'sys_notice_status', '', 'danger', 'N', '0', 'admin', '2018-03-16 11:33:00', 'ry', '2018-03-16 11:33:00', '关闭状态');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (18, 99, '其他', '0', 'sys_oper_type', '', 'info', 'N', '0', 'admin', '2018-03-16 11:33:00', 'ry', '2018-03-16 11:33:00', '其他操作');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (19, 1, '新增', '1', 'sys_oper_type', '', 'info', 'N', '0', 'admin', '2018-03-16 11:33:00', 'ry', '2018-03-16 11:33:00', '新增操作');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (20, 2, '修改', '2', 'sys_oper_type', '', 'info', 'N', '0', 'admin', '2018-03-16 11:33:00', 'ry', '2018-03-16 11:33:00', '修改操作');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (21, 3, '删除', '3', 'sys_oper_type', '', 'danger', 'N', '0', 'admin', '2018-03-16 11:33:00', 'ry', '2018-03-16 11:33:00', '删除操作');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (22, 4, '授权', '4', 'sys_oper_type', '', 'primary', 'N', '0', 'admin', '2018-03-16 11:33:00', 'ry', '2018-03-16 11:33:00', '授权操作');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (23, 5, '导出', '5', 'sys_oper_type', '', 'warning', 'N', '0', 'admin', '2018-03-16 11:33:00', 'ry', '2018-03-16 11:33:00', '导出操作');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (24, 6, '导入', '6', 'sys_oper_type', '', 'warning', 'N', '0', 'admin', '2018-03-16 11:33:00', 'ry', '2018-03-16 11:33:00', '导入操作');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (25, 7, '强退', '7', 'sys_oper_type', '', 'danger', 'N', '0', 'admin', '2018-03-16 11:33:00', 'ry', '2018-03-16 11:33:00', '强退操作');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (26, 8, '生成代码', '8', 'sys_oper_type', '', 'warning', 'N', '0', 'admin', '2018-03-16 11:33:00', 'ry', '2018-03-16 11:33:00', '生成操作');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (27, 9, '清空数据', '9', 'sys_oper_type', '', 'danger', 'N', '0', 'admin', '2018-03-16 11:33:00', 'ry', '2018-03-16 11:33:00', '清空操作');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (28, 1, '成功', '0', 'sys_common_status', '', 'primary', 'N', '0', 'admin', '2018-03-16 11:33:00', 'ry', '2018-03-16 11:33:00', '正常状态');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (29, 2, '失败', '1', 'sys_common_status', '', 'danger', 'N', '0', 'admin', '2018-03-16 11:33:00', 'ry', '2018-03-16 11:33:00', '停用状态');


INSERT INTO sys_notice(notice_id, notice_title, notice_type, notice_content, status, create_by, create_time, update_by, update_time, remark) VALUES (1, '温馨提醒：2018-07-01 新版本发布啦', '2', '新版本内容', '0', 'admin', '2018-03-16 11:33:00', 'ry', '2018-03-16 11:33:00', '管理员');
INSERT INTO sys_notice(notice_id, notice_title, notice_type, notice_content, status, create_by, create_time, update_by, update_time, remark) VALUES (2, '维护通知：2018-07-01 系统凌晨维护', '1', '维护内容', '0', 'admin', '2018-03-16 11:33:00', 'ry', '2018-03-16 11:33:00', '管理员');

-- ----------------------------
-- 初始化-菜单信息表数据
-- ----------------------------
-- 一级菜单
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('1', '系统管理', '0', '1', 'system',           null,   1, 0, 'M', '0', '0', '', 'sysmgr',   'admin', now(), '', null, '系统管理目录');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('2', '系统监控', '0', '2', 'monitor',          null,   1, 0, 'M', '0', '0', '', 'sysmonitor',  'admin', now(), '', null, '系统监控目录');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('3', '系统工具', '0', '3', 'tool',             null,   1, 0, 'M', '0', '0', '', 'systool',     'admin', now(), '', null, '系统工具目录');
-- 二级菜单
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('100',  '用户管理', '1',   '1', 'user',       'system/user/index',        1, 0, 'C', '0', '0', 'system:user:list',        '#',          'admin', now(), '', null, '用户管理菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('101',  '角色管理', '1',   '2', 'role',       'system/role/index',        1, 0, 'C', '0', '0', 'system:role:list',        '#',       'admin', now(), '', null, '角色管理菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('102',  '菜单管理', '1',   '3', 'menu',       'system/menu/index',        1, 0, 'C', '0', '0', 'system:menu:list',        '#',    'admin', now(), '', null, '菜单管理菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('103',  '部门管理', '1',   '4', 'dept',       'system/dept/index',        1, 0, 'C', '0', '0', 'system:dept:list',        '#',          'admin', now(), '', null, '部门管理菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('104',  '岗位管理', '1',   '5', 'post',       'system/post/index',        1, 0, 'C', '0', '0', 'system:post:list',        '#',          'admin', now(), '', null, '岗位管理菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('105',  '字典管理', '1',   '6', 'dict',       'system/dict/index',        1, 0, 'C', '0', '0', 'system:dict:list',        '#',          'admin', now(), '', null, '字典管理菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('106',  '参数设置', '1',   '7', 'config',     'system/config/index',      1, 0, 'C', '0', '0', 'system:config:list',      '#',          'admin', now(), '', null, '参数设置菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('107',  '通知公告', '1',   '8', 'notice',     'system/notice/index',      1, 0, 'C', '0', '0', 'system:notice:list',      '#',       'admin', now(), '', null, '通知公告菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('108',  '日志管理', '1',   '9', 'log',        'system/log/index',         1, 0, 'M', '0', '0', '',                        '#',           'admin', now(), '', null, '日志管理菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('109',  '在线用户', '2',   '1', 'online',     'monitor/online/index',     1, 0, 'C', '0', '0', 'monitor:online:list',     '#',        'admin', now(), '', null, '在线用户菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('110',  '定时任务', '2',   '2', 'job',        'monitor/job/index',        1, 0, 'C', '0', '0', 'monitor:job:list',        '#',           'admin', now(), '', null, '定时任务菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('111',  '数据监控', '2',   '3', 'druid',      'monitor/druid/index',      1, 0, 'C', '0', '0', 'monitor:druid:list',      '#',         'admin', now(), '', null, '数据监控菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('112',  '服务监控', '2',   '4', 'server',     'monitor/server/index',     1, 0, 'C', '0', '0', 'monitor:server:list',     '#',        'admin', now(), '', null, '服务监控菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('113',  '表单构建', '3',   '1', 'build',      'tool/build/index',         1, 0, 'C', '0', '0', 'tool:build:list',         '#',         'admin', now(), '', null, '表单构建菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('114',  '代码生成', '3',   '2', 'gen',        'tool/gen/index',           1, 0, 'C', '0', '0', 'tool:gen:list',           '#',          'admin', now(), '', null, '代码生成菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('115',  '系统接口', '3',   '3', 'swagger',    'tool/swagger/index',       1, 0, 'C', '0', '0', 'tool:swagger:list',       '#',       'admin', now(), '', null, '系统接口菜单');
-- 三级菜单
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('500',  '操作日志', '108', '1', 'operlog',    'monitor/operlog/index',    1, 0, 'C', '0', '0', 'monitor:operlog:list',    '#',          'admin', now(), '', null, '操作日志菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('501',  '登录日志', '108', '2', 'logininfor', 'monitor/logininfor/index', 1, 0, 'C', '0', '0', 'monitor:logininfor:list', '#',    'admin', now(), '', null, '登录日志菜单');
-- 用户管理按钮
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('1001', '用户查询', '100', '1',  '', '', 1, 0, 'F', '0', '0', 'system:user:query',          '#', 'admin', now(), '', null, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('1002', '用户新增', '100', '2',  '', '', 1, 0, 'F', '0', '0', 'system:user:add',            '#', 'admin', now(), '', null, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('1003', '用户修改', '100', '3',  '', '', 1, 0, 'F', '0', '0', 'system:user:edit',           '#', 'admin', now(), '', null, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('1004', '用户删除', '100', '4',  '', '', 1, 0, 'F', '0', '0', 'system:user:remove',         '#', 'admin', now(), '', null, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('1005', '用户导出', '100', '5',  '', '', 1, 0, 'F', '0', '0', 'system:user:export',         '#', 'admin', now(), '', null, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('1006', '用户导入', '100', '6',  '', '', 1, 0, 'F', '0', '0', 'system:user:import',         '#', 'admin', now(), '', null, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('1007', '重置密码', '100', '7',  '', '', 1, 0, 'F', '0', '0', 'system:user:resetPwd',       '#', 'admin', now(), '', null, '');
-- 角色管理按钮
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('1008', '角色查询', '101', '1',  '', '', 1, 0, 'F', '0', '0', 'system:role:query',          '#', 'admin', now(), '', null, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('1009', '角色新增', '101', '2',  '', '', 1, 0, 'F', '0', '0', 'system:role:add',            '#', 'admin', now(), '', null, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('1010', '角色修改', '101', '3',  '', '', 1, 0, 'F', '0', '0', 'system:role:edit',           '#', 'admin', now(), '', null, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('1011', '角色删除', '101', '4',  '', '', 1, 0, 'F', '0', '0', 'system:role:remove',         '#', 'admin', now(), '', null, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('1012', '角色导出', '101', '5',  '', '', 1, 0, 'F', '0', '0', 'system:role:export',         '#', 'admin', now(), '', null, '');
-- 菜单管理按钮
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('1013', '菜单查询', '102', '1',  '', '', 1, 0, 'F', '0', '0', 'system:menu:query',          '#', 'admin', now(), '', null, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('1014', '菜单新增', '102', '2',  '', '', 1, 0, 'F', '0', '0', 'system:menu:add',            '#', 'admin', now(), '', null, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('1015', '菜单修改', '102', '3',  '', '', 1, 0, 'F', '0', '0', 'system:menu:edit',           '#', 'admin', now(), '', null, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('1016', '菜单删除', '102', '4',  '', '', 1, 0, 'F', '0', '0', 'system:menu:remove',         '#', 'admin', now(), '', null, '');
-- 部门管理按钮
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('1017', '部门查询', '103', '1',  '', '', 1, 0, 'F', '0', '0', 'system:dept:query',          '#', 'admin', now(), '', null, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('1018', '部门新增', '103', '2',  '', '', 1, 0, 'F', '0', '0', 'system:dept:add',            '#', 'admin', now(), '', null, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('1019', '部门修改', '103', '3',  '', '', 1, 0, 'F', '0', '0', 'system:dept:edit',           '#', 'admin', now(), '', null, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('1020', '部门删除', '103', '4',  '', '', 1, 0, 'F', '0', '0', 'system:dept:remove',         '#', 'admin', now(), '', null, '');
-- 岗位管理按钮
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('1021', '岗位查询', '104', '1',  '', '', 1, 0, 'F', '0', '0', 'system:post:query',          '#', 'admin', now(), '', null, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('1022', '岗位新增', '104', '2',  '', '', 1, 0, 'F', '0', '0', 'system:post:add',            '#', 'admin', now(), '', null, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('1023', '岗位修改', '104', '3',  '', '', 1, 0, 'F', '0', '0', 'system:post:edit',           '#', 'admin', now(), '', null, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('1024', '岗位删除', '104', '4',  '', '', 1, 0, 'F', '0', '0', 'system:post:remove',         '#', 'admin', now(), '', null, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('1025', '岗位导出', '104', '5',  '', '', 1, 0, 'F', '0', '0', 'system:post:export',         '#', 'admin', now(), '', null, '');
-- 字典管理按钮
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('1026', '字典查询', '105', '1', '#', '', 1, 0, 'F', '0', '0', 'system:dict:query',          '#', 'admin', now(), '', null, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('1027', '字典新增', '105', '2', '#', '', 1, 0, 'F', '0', '0', 'system:dict:add',            '#', 'admin', now(), '', null, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('1028', '字典修改', '105', '3', '#', '', 1, 0, 'F', '0', '0', 'system:dict:edit',           '#', 'admin', now(), '', null, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('1029', '字典删除', '105', '4', '#', '', 1, 0, 'F', '0', '0', 'system:dict:remove',         '#', 'admin', now(), '', null, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('1030', '字典导出', '105', '5', '#', '', 1, 0, 'F', '0', '0', 'system:dict:export',         '#', 'admin', now(), '', null, '');
-- 参数设置按钮
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('1031', '参数查询', '106', '1', '#', '', 1, 0, 'F', '0', '0', 'system:config:query',        '#', 'admin', now(), '', null, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('1032', '参数新增', '106', '2', '#', '', 1, 0, 'F', '0', '0', 'system:config:add',          '#', 'admin', now(), '', null, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('1033', '参数修改', '106', '3', '#', '', 1, 0, 'F', '0', '0', 'system:config:edit',         '#', 'admin', now(), '', null, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('1034', '参数删除', '106', '4', '#', '', 1, 0, 'F', '0', '0', 'system:config:remove',       '#', 'admin', now(), '', null, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('1035', '参数导出', '106', '5', '#', '', 1, 0, 'F', '0', '0', 'system:config:export',       '#', 'admin', now(), '', null, '');
-- 通知公告按钮
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('1036', '公告查询', '107', '1', '#', '', 1, 0, 'F', '0', '0', 'system:notice:query',        '#', 'admin', now(), '', null, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('1037', '公告新增', '107', '2', '#', '', 1, 0, 'F', '0', '0', 'system:notice:add',          '#', 'admin', now(), '', null, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('1038', '公告修改', '107', '3', '#', '', 1, 0, 'F', '0', '0', 'system:notice:edit',         '#', 'admin', now(), '', null, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('1039', '公告删除', '107', '4', '#', '', 1, 0, 'F', '0', '0', 'system:notice:remove',       '#', 'admin', now(), '', null, '');
-- 操作日志按钮
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('1040', '操作查询', '500', '1', '#', '', 1, 0, 'F', '0', '0', 'monitor:operlog:query',      '#', 'admin', now(), '', null, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('1041', '操作删除', '500', '2', '#', '', 1, 0, 'F', '0', '0', 'monitor:operlog:remove',     '#', 'admin', now(), '', null, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('1042', '日志导出', '500', '4', '#', '', 1, 0, 'F', '0', '0', 'monitor:operlog:export',     '#', 'admin', now(), '', null, '');
-- 登录日志按钮
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('1043', '登录查询', '501', '1', '#', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:query',   '#', 'admin', now(), '', null, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('1044', '登录删除', '501', '2', '#', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:remove',  '#', 'admin', now(), '', null, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('1045', '日志导出', '501', '3', '#', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:export',  '#', 'admin', now(), '', null, '');
-- 在线用户按钮
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('1046', '在线查询', '109', '1', '#', '', 1, 0, 'F', '0', '0', 'monitor:online:query',       '#', 'admin', now(), '', null, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('1047', '批量强退', '109', '2', '#', '', 1, 0, 'F', '0', '0', 'monitor:online:batchLogout', '#', 'admin', now(), '', null, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('1048', '单条强退', '109', '3', '#', '', 1, 0, 'F', '0', '0', 'monitor:online:forceLogout', '#', 'admin', now(), '', null, '');
-- 定时任务按钮
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('1049', '任务查询', '110', '1', '#', '', 1, 0, 'F', '0', '0', 'monitor:job:query',          '#', 'admin', now(), '', null, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('1050', '任务新增', '110', '2', '#', '', 1, 0, 'F', '0', '0', 'monitor:job:add',            '#', 'admin', now(), '', null, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('1051', '任务修改', '110', '3', '#', '', 1, 0, 'F', '0', '0', 'monitor:job:edit',           '#', 'admin', now(), '', null, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('1052', '任务删除', '110', '4', '#', '', 1, 0, 'F', '0', '0', 'monitor:job:remove',         '#', 'admin', now(), '', null, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('1053', '状态修改', '110', '5', '#', '', 1, 0, 'F', '0', '0', 'monitor:job:changeStatus',   '#', 'admin', now(), '', null, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('1054', '任务导出', '110', '7', '#', '', 1, 0, 'F', '0', '0', 'monitor:job:export',         '#', 'admin', now(), '', null, '');
-- 代码生成按钮
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('1055', '生成查询', '114', '1', '#', '', 1, 0, 'F', '0', '0', 'tool:gen:query',             '#', 'admin', now(), '', null, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('1056', '生成修改', '114', '2', '#', '', 1, 0, 'F', '0', '0', 'tool:gen:edit',              '#', 'admin', now(), '', null, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('1057', '生成删除', '114', '3', '#', '', 1, 0, 'F', '0', '0', 'tool:gen:remove',            '#', 'admin', now(), '', null, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('1058', '导入代码', '114', '2', '#', '', 1, 0, 'F', '0', '0', 'tool:gen:import',            '#', 'admin', now(), '', null, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('1059', '预览代码', '114', '4', '#', '', 1, 0, 'F', '0', '0', 'tool:gen:preview',           '#', 'admin', now(), '', null, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES('1060', '生成代码', '114', '5', '#', '', 1, 0, 'F', '0', '0', 'tool:gen:code',              '#', 'admin', now(), '', null, '');


INSERT INTO sys_post(post_id, post_code, post_name, post_sort, status, create_by, create_time, update_by, update_time, remark) VALUES (1, 'ceo', '董事长', 1, '0', 'admin', '2018-03-16 11:33:00', 'ry', '2018-03-16 11:33:00', '');
INSERT INTO sys_post(post_id, post_code, post_name, post_sort, status, create_by, create_time, update_by, update_time, remark) VALUES (2, 'se', '项目经理', 2, '0', 'admin', '2018-03-16 11:33:00', 'ry', '2018-03-16 11:33:00', '');
INSERT INTO sys_post(post_id, post_code, post_name, post_sort, status, create_by, create_time, update_by, update_time, remark) VALUES (3, 'hr', '人力资源', 3, '0', 'admin', '2018-03-16 11:33:00', 'ry', '2018-03-16 11:33:00', '');
INSERT INTO sys_post(post_id, post_code, post_name, post_sort, status, create_by, create_time, update_by, update_time, remark) VALUES (4, 'user', '普通员工', 4, '0', 'admin', '2018-03-16 11:33:00', 'ry', '2018-03-16 11:33:00', '');

INSERT INTO sys_role(role_id, role_name, role_key, role_sort, data_scope, status, del_flag, create_by, create_time, update_by, update_time, remark) VALUES (1, '管理员', 'admin', 1, '1', '0', '0', 'admin', '2018-03-16 11:33:00', 'ry', '2018-03-16 11:33:00', '管理员');
INSERT INTO sys_role(role_id, role_name, role_key, role_sort, data_scope, status, del_flag, create_by, create_time, update_by, update_time, remark) VALUES (2, '普通角色', 'common', 2, '2', '0', '0', 'admin', '2018-03-16 11:33:00', 'ry', '2018-03-16 11:33:00', '普通角色');

INSERT INTO sys_user(user_id, dept_id, user_name, nick_name, user_type, email, phonenumber, sex, avatar, password, salt, status, del_flag, login_ip, login_date, create_by, create_time, update_by, update_time, remark) VALUES (1, 99, 'admin', 'admin', '00', 'ry@163.com', '15888888888', '1', NULL, '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '', '0', '0', '127.0.0.1', '2020-07-27 14:02:46.85', 'admin', '2018-03-16 11:33:00', 'ry', '2020-07-27 14:02:46.915382', '管理员');
INSERT INTO sys_user_role(user_id, role_id) VALUES (1, 1);

SELECT setval('sys_config_config_id_seq', 99, true);
SELECT setval('sys_dept_dept_id_seq', 99, true);
SELECT setval('sys_dict_data_dict_code_seq', 99, true);
SELECT setval('sys_dict_type_dict_id_seq', 99, true);
SELECT setval('sys_menu_menu_id_seq',1999, true);
SELECT setval('sys_post_post_id_seq', 99, true);
SELECT setval('sys_role_role_id_seq', 99, true);
SELECT setval('sys_user_user_id_seq', 99, true);
SELECT setval('sys_notice_notice_id_seq', 99, true);

-- ----------------------------
-- 单点登录终端信息管理
-- ----------------------------
CREATE TABLE sys_client_details (
  client_id varchar(255) NOT NULL,
  resource_ids varchar(255) DEFAULT NULL,
  client_secret varchar(255) NOT NULL,
  scope varchar(255) NOT NULL,
  web_server_redirect_uri varchar(255) DEFAULT NULL,
  authorities varchar(255) DEFAULT NULL,
  additional_information varchar(4000) DEFAULT NULL,
  origin_secret varchar(255) NOT NULL,
  constraint pk_sys_client_details primary key ( client_id )
);
COMMENT ON TABLE sys_client_details IS '终端配置表';
COMMENT ON COLUMN sys_client_details.client_id IS '终端编号';
COMMENT ON COLUMN sys_client_details.resource_ids IS '资源ID标识';
COMMENT ON COLUMN sys_client_details.client_secret IS '终端安全码';
COMMENT ON COLUMN sys_client_details.scope IS '终端授权范围';
COMMENT ON COLUMN sys_client_details.web_server_redirect_uri IS '服务器回调地址';
COMMENT ON COLUMN sys_client_details.authorities IS '访问资源所需权限';
COMMENT ON COLUMN sys_client_details.additional_information IS '附加信息';
COMMENT ON COLUMN sys_client_details.origin_secret IS '终端明文安全码';

-- ----------------------------
-- 单点登录终端管理菜单
-- ----------------------------
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2000, '终端配置', 1, 10, 'client', 'system/client/index', 1, 0, 'C', '0', '0', 'system:client:list', 'server', 'admin', now(), '', NULL, '终端配置菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2001, '终端配置查询', 2000, 1, '#', '', 1, 0, 'F', '0', '0', 'system:client:query', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2002, '终端配置新增', 2000, 2, '#', '', 1, 0, 'F', '0', '0', 'system:client:add', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2003, '终端配置修改', 2000, 3, '#', '', 1, 0, 'F', '0', '0', 'system:client:edit', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2004, '终端配置删除', 2000, 4, '#', '', 1, 0, 'F', '0', '0', 'system:client:remove', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2005, '终端配置导出', 2000, 5, '#', '', 1, 0, 'F', '0', '0', 'system:client:export', '#', 'admin', now(), '', NULL, '');
SELECT setval('sys_menu_menu_id_seq',2005, true);
-- ----------------------------
-- WEB终端
-- ----------------------------
INSERT INTO sys_client_details(client_id, resource_ids, client_secret, scope, web_server_redirect_uri, authorities, additional_information, origin_secret) VALUES ('web', NULL, '$2a$10$OsbchgUAxA6MLchR2L1jvejxfTYi5XPoPwQUNERNr.vcH.ivnb5HO', 'server', NULL, NULL, NULL, '123456');
INSERT INTO sys_client_details(client_id, resource_ids, client_secret, scope, web_server_redirect_uri, authorities, additional_information, origin_secret) VALUES ('app', NULL, '$2a$10$uKuoTb8Sa5ocmTxJpap1tu4vChbVifBe4uXqohCcwWJRbZvlqGsdC', 'server', NULL, NULL, NULL, '123456');

CREATE TABLE sys_tenant_role (
	id varchar(48) NOT NULL,
	role_name varchar ( 30 ) NOT NULL,
	role_key varchar ( 100 ) NOT NULL,
	role_sort INT4 NOT NULL,
	menu_check_strictly  INTEGER  DEFAULT 1,
	status CHAR ( 1 ) NOT NULL,
	del_flag CHAR ( 1 ) DEFAULT '0',
	create_by varchar ( 64 ) DEFAULT '',
	create_time TIMESTAMP DEFAULT NULL,
	update_by varchar ( 64 ) DEFAULT '',
	update_time TIMESTAMP DEFAULT NULL,
	remark varchar ( 500 ) DEFAULT NULL,
	constraint pk_sys_tenant_role primary key ( id ) 
);
COMMENT ON TABLE sys_tenant_role IS '租户角色信息表';
COMMENT ON COLUMN sys_tenant_role.id IS '角色ID';
COMMENT ON COLUMN sys_tenant_role.role_name IS '角色名称';
COMMENT ON COLUMN sys_tenant_role.role_key IS '角色权限字符串';
COMMENT ON COLUMN sys_tenant_role.role_sort IS '显示顺序';
COMMENT ON COLUMN sys_tenant_role.menu_check_strictly IS '菜单树选择项是否关联显示';
COMMENT ON COLUMN sys_tenant_role.status IS '角色状态（0正常 1停用）';
COMMENT ON COLUMN sys_tenant_role.del_flag IS '删除标志（0代表存在 2代表删除）';
COMMENT ON COLUMN sys_tenant_role.create_by IS '创建者';
COMMENT ON COLUMN sys_tenant_role.create_time IS '创建时间';
COMMENT ON COLUMN sys_tenant_role.update_by IS '更新者';
COMMENT ON COLUMN sys_tenant_role.update_time IS '更新时间';
COMMENT ON COLUMN sys_tenant_role.remark IS '备注';

CREATE TABLE sys_tenant_role_menu (
  role_id varchar(48) NOT NULL,
  menu_id INT4 NOT NULL,
  constraint pk_sys_tenant_role_menu primary key (role_id,menu_id)
);
COMMENT ON TABLE sys_tenant_role_menu IS '租户角色和菜单关联表';
COMMENT ON COLUMN sys_tenant_role_menu.role_id IS '角色ID';
COMMENT ON COLUMN sys_tenant_role_menu.menu_id IS '菜单ID';

-- ----------------------------
-- 租户信息表
-- ----------------------------
CREATE TABLE sys_tenant (
  id SERIAL8 NOT NULL,
  tenant_id VARCHAR(128) NOT NULL DEFAULT '' ,
  tenant_name VARCHAR(128) DEFAULT '',
  tenant_type VARCHAR(18) DEFAULT 'TRIAL',
  tenant_desc VARCHAR(255) DEFAULT NULL,
  tenant_state VARCHAR(18) DEFAULT 'NORMAL',
  contact_name VARCHAR(255),
  phone VARCHAR(20),
  email VARCHAR(255),
  tenant_role_id VARCHAR(48),
  create_source VARCHAR(32) DEFAULT 'BG_CREATE',
  effective_time TIMESTAMP NOT NULL,
  expire_time TIMESTAMP NOT NULL,
  create_time TIMESTAMP NOT NULL,
  update_time TIMESTAMP NOT NULL,
  constraint pk_sys_tenant primary key( id )
) ;
CREATE UNIQUE INDEX uk_tenant_id ON sys_tenant (tenant_id);
CREATE UNIQUE INDEX uk_tenant_name ON sys_tenant (tenant_name);
COMMENT ON TABLE sys_tenant IS '租户信息表';
COMMENT ON COLUMN sys_tenant.id IS '主键';
COMMENT ON COLUMN sys_tenant.tenant_id IS '租户ID';
COMMENT ON COLUMN sys_tenant.tenant_name IS '租户名称';
COMMENT ON COLUMN sys_tenant.tenant_type IS '租户类型(TRIAL: 试用租户， FORMAL: 正式租户)';
COMMENT ON COLUMN sys_tenant.tenant_desc IS '租户描述';
COMMENT ON COLUMN sys_tenant.tenant_state IS '租户状态(NORMAL: 正常， FROZEN: 冻结)';
COMMENT ON COLUMN sys_tenant.contact_name IS '联系人姓名';
COMMENT ON COLUMN sys_tenant.phone IS '联系手机号';
COMMENT ON COLUMN sys_tenant.email IS '电子邮件';
COMMENT ON COLUMN sys_tenant.tenant_role_id IS '租户角色ID';
COMMENT ON COLUMN sys_tenant.create_source IS '创建来源(REGIST: 注册，BG_CREATE: 后台创建 )';
COMMENT ON COLUMN sys_tenant.effective_time IS '生效时间';
COMMENT ON COLUMN sys_tenant.expire_time IS '失效时间';
COMMENT ON COLUMN sys_tenant.create_time IS '创建时间';
COMMENT ON COLUMN sys_tenant.update_time IS '修改时间';
INSERT INTO sys_tenant(id, tenant_id, tenant_name, tenant_type, tenant_desc, tenant_state, contact_name, phone, create_source, effective_time, expire_time, create_time, update_time) VALUES (1, 'super', '系统租户', 'FORMAL', '系统租户', 'NORMAL', NULL, NULL, 'BG_CREATE', '2020-11-09 00:00:00', '2099-11-30 00:00:00', now(), now());

-- ----------------------------
-- 租户管理菜单
-- ----------------------------
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2006, '租户信息', 2519, 11, 'tenant', 'system/tenant/index', 1, 0, 'C', '0', '0', 'system:tenant:list', '#', 'admin', now(), '', NULL, '租户信息菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2007, '租户信息查询', 2006, 1, '#', '', 1, 0, 'F', '0', '0', 'system:tenant:query', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2008, '租户信息新增', 2006, 2, '#', '', 1, 0, 'F', '0', '0', 'system:tenant:add', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2009, '租户信息修改', 2006, 3, '#', '', 1, 0, 'F', '0', '0', 'system:tenant:edit', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2010, '租户信息删除', 2006, 4, '#', '', 1, 0, 'F', '0', '0', 'system:tenant:remove', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2011, '租户信息导出', 2006, 5, '#', '', 1, 0, 'F', '0', '0', 'system:tenant:export', '#', 'admin', now(), '', NULL, '');
SELECT setval('sys_menu_menu_id_seq',2011, true);

-- ----------------------------
-- 系统表添加租户字段
-- ----------------------------
ALTER TABLE sys_user ADD tenant_id varchar(255) DEFAULT 'super';
COMMENT ON COLUMN sys_user.tenant_id IS '租户ID';
ALTER TABLE sys_role ADD tenant_id varchar(255) DEFAULT 'super';
COMMENT ON COLUMN sys_role.tenant_id IS '租户ID';
ALTER TABLE sys_post ADD tenant_id varchar(255) DEFAULT 'super';
COMMENT ON COLUMN sys_post.tenant_id IS '租户ID';
ALTER TABLE sys_oper_log ADD tenant_id varchar(255) DEFAULT 'super';
COMMENT ON COLUMN sys_oper_log.tenant_id IS '租户ID';
ALTER TABLE sys_notice ADD tenant_id varchar(255) DEFAULT 'super';
COMMENT ON COLUMN sys_notice.tenant_id IS '租户ID';
ALTER TABLE sys_logininfor ADD tenant_id varchar(255) DEFAULT 'super';
COMMENT ON COLUMN sys_logininfor.tenant_id IS '租户ID';
ALTER TABLE sys_dept ADD tenant_id varchar(255) DEFAULT 'super';
COMMENT ON COLUMN sys_dept.tenant_id IS '租户ID';

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
  config_id INT8 NOT NULL,
  tenant_id varchar(255) NOT NULL,
  config_value varchar(500) DEFAULT '',
  update_by varchar(64) DEFAULT '',
  update_time TIMESTAMP DEFAULT NULL,
  constraint pk_sys_tenant_config primary key (config_id,tenant_id)
);
COMMENT ON TABLE sys_tenant_config IS '租户参数配置表';
COMMENT ON COLUMN sys_tenant_config.config_id IS '参数主键';
COMMENT ON COLUMN sys_tenant_config.tenant_id IS '租户ID';
COMMENT ON COLUMN sys_tenant_config.config_value IS '参数键值';
COMMENT ON COLUMN sys_tenant_config.update_by IS '更新者';
COMMENT ON COLUMN sys_tenant_config.update_time IS '更新时间';

-- ----------------------------
-- 用户表添加是否租户管理员字段
-- ----------------------------
ALTER TABLE sys_user ADD tenant_admin char(1) DEFAULT 'N';
COMMENT ON COLUMN sys_user.tenant_admin IS '是否租户管理员(Y是 N否)';


-- 系统参数添加租户是否可以维护控制字段 
ALTER TABLE sys_config ADD tenant_maintain  char(1) NOT NULL DEFAULT 'N';
COMMENT ON COLUMN sys_config.tenant_maintain IS '是否允许租户维护（Y:是，N:否）';

CREATE TABLE base_person_info (
  id varchar(48)  NOT NULL,
  unique_id varchar(48)  NOT NULL,
  name varchar(64) ,
  sex char(1) ,
  phone varchar(48) ,
  card_no varchar(255) ,
  account varchar(255) ,
  email varchar(255) ,
  dept_id int8,
  datasource varchar(48)  NOT NULL DEFAULT 'INTERFACE',
  flag char(1) DEFAULT '1',
  status char(1)  NOT NULL DEFAULT '0',
  remark varchar(255) ,
  create_by varchar(64) ,
  update_by varchar(64) ,
  create_time timestamp,
  update_time timestamp,
  batch_date timestamp,
  tenant_id varchar(255) DEFAULT 'super',
  CONSTRAINT pk_base_person_info PRIMARY KEY (id)
);
CREATE UNIQUE INDEX personInfoUniqueIdIndex ON base_person_info (unique_id, tenant_id);
COMMENT ON COLUMN base_person_info.id IS '主键';
COMMENT ON COLUMN base_person_info.unique_id IS '人员标识';
COMMENT ON COLUMN base_person_info.name IS '姓名';
COMMENT ON COLUMN base_person_info.sex IS '性别：0-男 1-女 2-未知';
COMMENT ON COLUMN base_person_info.phone IS '手机';
COMMENT ON COLUMN base_person_info.card_no IS '卡号';
COMMENT ON COLUMN base_person_info.account IS '账号';
COMMENT ON COLUMN base_person_info.email IS '邮箱';
COMMENT ON COLUMN base_person_info.dept_id IS '部门ID';
COMMENT ON COLUMN base_person_info.datasource IS '数据来源(数据字典)';
COMMENT ON COLUMN base_person_info.flag IS '人员标记：1-正常 2-红名单 3-黑名单';
COMMENT ON COLUMN base_person_info.status IS '状态：0-有效  1:无效';
COMMENT ON COLUMN base_person_info.remark IS '备注';
COMMENT ON COLUMN base_person_info.create_by IS '创建人';
COMMENT ON COLUMN base_person_info.update_by IS '修改人';
COMMENT ON COLUMN base_person_info.create_time IS '创建时间';
COMMENT ON COLUMN base_person_info.update_time IS '修改时间';
COMMENT ON COLUMN base_person_info.batch_date IS '定时任务执行时间';
COMMENT ON COLUMN base_person_info.tenant_id IS '租户ID';
COMMENT ON TABLE base_person_info IS '基础数据_人员基础信息';

CREATE TABLE base_person_face (
  id varchar(48)  NOT NULL,
  person_id varchar(48)  NOT NULL,
  unique_id varchar(48)  NOT NULL,
  feature varchar(4000)  NOT NULL,
  feature_md5 varchar(255) ,
  quality_score float8,
  image_url varchar(255)  NOT NULL,
  vendor_code varchar(48) ,
  algs_version varchar(48) ,
  encrypted char(1) DEFAULT '0',
  datasource varchar(48)  NOT NULL DEFAULT 'INTERFACE',
  remark varchar(255) ,
  status char(1)  NOT NULL DEFAULT '0',
  create_by varchar(64) ,
  update_by varchar(64) ,
  create_time timestamp,
  update_time timestamp,
  batch_date timestamp,
  tenant_id varchar(255) DEFAULT 'super',
  CONSTRAINT pk_base_person_face PRIMARY KEY (id)
);
CREATE INDEX facePersonIdIndex ON base_person_face (person_id);
COMMENT ON COLUMN base_person_face.id IS '主键';
COMMENT ON COLUMN base_person_face.person_id IS '关联人员主键';
COMMENT ON COLUMN base_person_face.unique_id IS '人员标识';
COMMENT ON COLUMN base_person_face.feature IS '人脸特征';
COMMENT ON COLUMN base_person_face.feature_md5 IS '人脸特征MD5';
COMMENT ON COLUMN base_person_face.quality_score IS '图像质量得分';
COMMENT ON COLUMN base_person_face.image_url IS '人脸图像路径';
COMMENT ON COLUMN base_person_face.vendor_code IS '厂商';
COMMENT ON COLUMN base_person_face.algs_version IS '算法版本';
COMMENT ON COLUMN base_person_face.encrypted IS '是否加密： 1-加密 0-不加密';
COMMENT ON COLUMN base_person_face.datasource IS '数据来源(数据字典)';
COMMENT ON COLUMN base_person_face.remark IS '备注';
COMMENT ON COLUMN base_person_face.status IS '状态：0-有效  1:无效';
COMMENT ON COLUMN base_person_face.create_by IS '创建人';
COMMENT ON COLUMN base_person_face.update_by IS '修改人';
COMMENT ON COLUMN base_person_face.create_time IS '创建时间';
COMMENT ON COLUMN base_person_face.update_time IS '修改时间';
COMMENT ON COLUMN base_person_face.batch_date IS '定时任务执行时间';
COMMENT ON COLUMN base_person_face.tenant_id IS '租户ID';
COMMENT ON TABLE base_person_face IS '基础数据_人脸图像信息';

CREATE TABLE base_person_finger (
  id varchar(48) NOT NULL,
  person_id varchar(48) NOT NULL,
  unique_id varchar(48) NOT NULL,
  finger_no varchar(2) NOT NULL,
  coercive_position varchar(2) NOT NULL DEFAULT '0',
  feature varchar(4000) NOT NULL,
  feature_md5 varchar(255) ,
  quality_score float8,
  image_url varchar(255) ,
  vendor_code varchar(48) ,
  algs_version varchar(48) ,
  encrypted char(1) DEFAULT '0',
  datasource varchar(48) NOT NULL DEFAULT 'INTERFACE',
  remark varchar(255) ,
  status char(1) NOT NULL DEFAULT '0',
  create_by varchar(64) ,
  update_by varchar(64) ,
  create_time timestamp,
  update_time timestamp,
  batch_date timestamp,
  tenant_id varchar(255) DEFAULT 'super',
  CONSTRAINT pk_base_person_finger PRIMARY KEY (id)
);
CREATE INDEX fingerPersonIdIndex ON base_person_finger (person_id);
COMMENT ON COLUMN base_person_finger.id IS '主键';
COMMENT ON COLUMN base_person_finger.person_id IS '关联人员主键';
COMMENT ON COLUMN base_person_finger.unique_id IS '人员标识';
COMMENT ON COLUMN base_person_finger.finger_no IS '手指编号';
COMMENT ON COLUMN base_person_finger.coercive_position IS '胁迫位';
COMMENT ON COLUMN base_person_finger.feature IS '指纹特征';
COMMENT ON COLUMN base_person_finger.feature_md5 IS '指纹特征MD5';
COMMENT ON COLUMN base_person_finger.quality_score IS '图像质量得分';
COMMENT ON COLUMN base_person_finger.image_url IS '指纹图像路径';
COMMENT ON COLUMN base_person_finger.vendor_code IS '厂商';
COMMENT ON COLUMN base_person_finger.algs_version IS '算法版本';
COMMENT ON COLUMN base_person_finger.encrypted IS '是否加密： 1-加密 0-不加密';
COMMENT ON COLUMN base_person_finger.datasource IS '数据来源(数据字典)';
COMMENT ON COLUMN base_person_finger.remark IS '备注';
COMMENT ON COLUMN base_person_finger.status IS '状态：0-有效  1:无效';
COMMENT ON COLUMN base_person_finger.create_by IS '创建人';
COMMENT ON COLUMN base_person_finger.update_by IS '修改人';
COMMENT ON COLUMN base_person_finger.create_time IS '创建时间';
COMMENT ON COLUMN base_person_finger.update_time IS '修改时间';
COMMENT ON COLUMN base_person_finger.batch_date IS '定时任务执行时间';
COMMENT ON COLUMN base_person_finger.tenant_id IS '租户ID';
COMMENT ON TABLE base_person_finger IS '基础数据_指纹图像信息';

CREATE TABLE base_person_iris (
  id varchar(48) NOT NULL,
  person_id varchar(48) NOT NULL,
  unique_id varchar(48) NOT NULL,
  feature text NOT NULL,
  feature_md5 varchar(255),
  quality_score float8,
  image_url varchar(255) NOT NULL,
  vendor_code varchar(48),
  algs_version varchar(48),
  encrypted char(1) DEFAULT '0',
  datasource varchar(48) NOT NULL DEFAULT 'INTERFACE',
  remark varchar(255),
  status char(1) NOT NULL DEFAULT '0',
  create_by varchar(64),
  update_by varchar(64),
  create_time timestamp,
  update_time timestamp,
  batch_date timestamp,
  tenant_id varchar(255) DEFAULT 'super',
  CONSTRAINT pk_base_person_iris PRIMARY KEY (id)
);
CREATE INDEX irisPersonIdIndex ON base_person_iris (person_id);
COMMENT ON COLUMN base_person_iris.id IS '主键';
COMMENT ON COLUMN base_person_iris.person_id IS '关联人员主键';
COMMENT ON COLUMN base_person_iris.unique_id IS '人员标识';
COMMENT ON COLUMN base_person_iris.feature IS '虹膜特征';
COMMENT ON COLUMN base_person_iris.feature_md5 IS '虹膜特征MD5';
COMMENT ON COLUMN base_person_iris.quality_score IS '图像质量得分';
COMMENT ON COLUMN base_person_iris.image_url IS '虹膜图像路径';
COMMENT ON COLUMN base_person_iris.vendor_code IS '厂商';
COMMENT ON COLUMN base_person_iris.algs_version IS '算法版本';
COMMENT ON COLUMN base_person_iris.encrypted IS '是否加密： 1-加密 0-不加密';
COMMENT ON COLUMN base_person_iris.datasource IS '数据来源(数据字典)';
COMMENT ON COLUMN base_person_iris.remark IS '备注';
COMMENT ON COLUMN base_person_iris.status IS '状态：0-有效  1:无效';
COMMENT ON COLUMN base_person_iris.create_by IS '创建人';
COMMENT ON COLUMN base_person_iris.update_by IS '修改人';
COMMENT ON COLUMN base_person_iris.create_time IS '创建时间';
COMMENT ON COLUMN base_person_iris.update_time IS '修改时间';
COMMENT ON COLUMN base_person_iris.batch_date IS '定时任务执行时间';
COMMENT ON COLUMN base_person_iris.tenant_id IS '租户ID';
COMMENT ON TABLE base_person_iris IS '基础数据_虹膜图像信息';

CREATE TABLE base_person_iris_face (
  id varchar(48) NOT NULL,
  person_id varchar(48) NOT NULL,
  unique_id varchar(48) NOT NULL,
  fusion_feature text,
  face_feature text,
  iris_feature text,
  fusion_feature_md5 varchar(255),
  face_feature_md5 varchar(255),
  face_image_url varchar(255),
  face_quality float8,
  iris_feature_md5 varchar(255),
  iris_image_url varchar(255),
  iris_quality float8,
  encrypted char(1) DEFAULT '0',
  status char(1) NOT NULL DEFAULT '0',
  datasource varchar(50) DEFAULT 'INTERFACE',
  data_describe varchar(255),
  validity_date timestamp,
  create_by varchar(48),
  update_by varchar(48),
  create_time timestamp,
  update_time timestamp,
  remark varchar(255),
  tenant_id varchar(255) DEFAULT 'super',
  CONSTRAINT pk_base_person_iris_face PRIMARY KEY (id)
);
CREATE INDEX irisfacePersonIdIndex ON base_person_iris_face (person_id);
COMMENT ON COLUMN base_person_iris_face.id IS '主键';
COMMENT ON COLUMN base_person_iris_face.person_id IS '关联人员主键';
COMMENT ON COLUMN base_person_iris_face.unique_id IS '人员标识';
COMMENT ON COLUMN base_person_iris_face.fusion_feature IS '融合特征';
COMMENT ON COLUMN base_person_iris_face.face_feature IS '人脸特征';
COMMENT ON COLUMN base_person_iris_face.iris_feature IS '虹膜特征';
COMMENT ON COLUMN base_person_iris_face.fusion_feature_md5 IS '融合特征md5';
COMMENT ON COLUMN base_person_iris_face.face_feature_md5 IS '人脸特征md5';
COMMENT ON COLUMN base_person_iris_face.face_image_url IS '人脸照片路径';
COMMENT ON COLUMN base_person_iris_face.face_quality IS '人脸质量分数';
COMMENT ON COLUMN base_person_iris_face.iris_feature_md5 IS '虹膜特征md5';
COMMENT ON COLUMN base_person_iris_face.iris_image_url IS '虹膜照片路径';
COMMENT ON COLUMN base_person_iris_face.iris_quality IS '虹膜质量分数';
COMMENT ON COLUMN base_person_iris_face.encrypted IS '是否加密： 1-加密 0-不加密';
COMMENT ON COLUMN base_person_iris_face.status IS '状态：0-有效  1-无效';
COMMENT ON COLUMN base_person_iris_face.datasource IS '数据来源(数据字典)';
COMMENT ON COLUMN base_person_iris_face.data_describe IS '数据描述';
COMMENT ON COLUMN base_person_iris_face.validity_date IS '有效期';
COMMENT ON COLUMN base_person_iris_face.create_by IS '创建人';
COMMENT ON COLUMN base_person_iris_face.update_by IS '修改人';
COMMENT ON COLUMN base_person_iris_face.create_time IS '创建时间';
COMMENT ON COLUMN base_person_iris_face.update_time IS '修改时间';
COMMENT ON COLUMN base_person_iris_face.remark IS '备注';
COMMENT ON COLUMN base_person_iris_face.tenant_id IS '租户ID';
COMMENT ON TABLE base_person_iris_face IS '虹膜人脸多模态图片特征表';

CREATE TABLE base_person_cert (
  id varchar(48) NOT NULL,
  unique_id varchar(48) NOT NULL,
  cert_type varchar(3) NOT NULL,
  cert_num varchar(255) NOT NULL,
  cert_name varchar(64),
  cert_validity varchar(48),
  gender char(1),
  bth_date varchar(48),
  nation varchar(3),
  address varchar(255),
  cert_authority varchar(255),
  cert_img varchar(255),
  encrypted char(1) DEFAULT '0',
  enterschool_img varchar(255),
  inschool_img varchar(255),
  graduate_img varchar(255),
  remark varchar(255),
  create_by varchar(64),
  update_by varchar(64),
  create_time timestamp,
  update_time timestamp,
  batch_date timestamp,
  tenant_id varchar(255) DEFAULT 'super',
  CONSTRAINT pk_base_person_cert PRIMARY KEY (id)
);
COMMENT ON COLUMN base_person_cert.id IS '主键';
COMMENT ON COLUMN base_person_cert.unique_id IS '人员标识';
COMMENT ON COLUMN base_person_cert.cert_type IS '证件类型（字典国标）';
COMMENT ON COLUMN base_person_cert.cert_num IS '证件号码';
COMMENT ON COLUMN base_person_cert.cert_name IS '证件姓名';
COMMENT ON COLUMN base_person_cert.cert_validity IS '证件有效期';
COMMENT ON COLUMN base_person_cert.gender IS '性别：0-男 1-女 2-未知';
COMMENT ON COLUMN base_person_cert.bth_date IS '出生日期';
COMMENT ON COLUMN base_person_cert.nation IS '民族';
COMMENT ON COLUMN base_person_cert.address IS '家庭住址';
COMMENT ON COLUMN base_person_cert.cert_authority IS '发证机关';
COMMENT ON COLUMN base_person_cert.cert_img IS '证件照';
COMMENT ON COLUMN base_person_cert.encrypted IS '是否加密： 1-加密 0-不加密';
COMMENT ON COLUMN base_person_cert.enterschool_img IS '入学照片';
COMMENT ON COLUMN base_person_cert.inschool_img IS '在校照片';
COMMENT ON COLUMN base_person_cert.graduate_img IS '毕业照片';
COMMENT ON COLUMN base_person_cert.remark IS '备注';
COMMENT ON COLUMN base_person_cert.create_by IS '创建人';
COMMENT ON COLUMN base_person_cert.update_by IS '修改人';
COMMENT ON COLUMN base_person_cert.create_time IS '创建时间';
COMMENT ON COLUMN base_person_cert.update_time IS '修改时间';
COMMENT ON COLUMN base_person_cert.batch_date IS '定时任务执行时间';
COMMENT ON COLUMN base_person_cert.tenant_id IS '租户ID';
COMMENT ON TABLE base_person_cert IS '基础数据_人员证件信息';

CREATE TABLE channel_business (
  id varchar(48) NOT NULL,
  channel_id varchar(48) NOT NULL,
  person_id varchar(48) NOT NULL,
  unique_id varchar(48) NOT NULL,
  busi_code_first varchar(255),
  busi_code_second varchar(255),
  busi_code_third varchar(255),
  face_mode varchar(1) NOT NULL DEFAULT '1',
  finger_mode varchar(1) NOT NULL DEFAULT '1',
  iris_mode varchar(1) NOT NULL DEFAULT '1',
  fvein_mode varchar(1) NOT NULL DEFAULT '1',
  face_iris_mode varchar(1) NOT NULL DEFAULT '1',
  datasource varchar(48) NOT NULL DEFAULT 'INTERFACE',
  locked varchar(1) DEFAULT 'N',
  lock_time timestamp,
  status char(1) NOT NULL DEFAULT '0',
  remark varchar(255),
  create_by varchar(64),
  update_by varchar(64),
  create_time timestamp,
  update_time timestamp,
  batch_date timestamp,
  tenant_id varchar(255) DEFAULT 'super',
  update_seria_num int8,
  CONSTRAINT pk_channel_business PRIMARY KEY (id)
);
CREATE UNIQUE INDEX channelIdPersonIdUnique ON channel_business (channel_id, person_id);
CREATE UNIQUE INDEX channelBusiUpdateSeqUnique ON channel_business (update_seria_num);
COMMENT ON COLUMN channel_business.id IS '主键';
COMMENT ON COLUMN channel_business.channel_id IS '渠道主键';
COMMENT ON COLUMN channel_business.person_id IS '人员主键';
COMMENT ON COLUMN channel_business.unique_id IS '人员唯一标识';
COMMENT ON COLUMN channel_business.busi_code_first IS '业务号1';
COMMENT ON COLUMN channel_business.busi_code_second IS '业务号2';
COMMENT ON COLUMN channel_business.busi_code_third IS '业务号3';
COMMENT ON COLUMN channel_business.face_mode IS '开通人脸(数据字典:0-不开通 1-开通)';
COMMENT ON COLUMN channel_business.finger_mode IS '开通指纹(数据字典:0-不开通 1-开通)';
COMMENT ON COLUMN channel_business.iris_mode IS '开通虹膜(数据字典:0-不开通 1-开通)';
COMMENT ON COLUMN channel_business.fvein_mode IS '开通指静脉(数据字典:0-不开通 1-开通)';
COMMENT ON COLUMN channel_business.face_iris_mode IS '开通人脸虹膜多模态(数据字典:0-不开通 1-开通)';
COMMENT ON COLUMN channel_business.datasource IS '数据来源(数据字典)';
COMMENT ON COLUMN channel_business.locked IS '是否锁定(数据字典:N-否 Y-是)';
COMMENT ON COLUMN channel_business.lock_time IS '锁定时间';
COMMENT ON COLUMN channel_business.status IS '状态(数据字典:0-有效  1:无效)';
COMMENT ON COLUMN channel_business.remark IS '备注';
COMMENT ON COLUMN channel_business.create_by IS '创建人';
COMMENT ON COLUMN channel_business.update_by IS '修改人';
COMMENT ON COLUMN channel_business.create_time IS '创建时间';
COMMENT ON COLUMN channel_business.update_time IS '修改时间';
COMMENT ON COLUMN channel_business.batch_date IS '定时任务执行时间';
COMMENT ON COLUMN channel_business.tenant_id IS '租户ID';
COMMENT ON COLUMN channel_business.update_seria_num IS '业务更新标识流水码';
COMMENT ON TABLE channel_business IS '渠道管理_渠道业务表';

CREATE TABLE channel_info (
  id varchar(48) NOT NULL,
  channel_code varchar(255) NOT NULL,
  channel_name varchar(255) NOT NULL,
  face_mode varchar(1) NOT NULL DEFAULT '1',
  finger_mode varchar(1) NOT NULL DEFAULT '1',
  iris_mode varchar(1) NOT NULL DEFAULT '1',
  fvein_mode varchar(1) NOT NULL DEFAULT '1',
  face_iris_mode varchar(1) NOT NULL DEFAULT '1',
  enable_multi_faces varchar(1) DEFAULT 'N',
  search_n varchar(1) DEFAULT '2',
  device_num_limit int4 DEFAULT 0,
  remark varchar(255),
  create_by varchar(64),
  update_by varchar(64),
  create_time timestamp,
  update_time timestamp,
  batch_date timestamp,
  tenant_id varchar(255) DEFAULT 'super',
  CONSTRAINT pk_channel_info PRIMARY KEY (id)
);
COMMENT ON COLUMN channel_info.id IS '主键';
COMMENT ON COLUMN channel_info.channel_code IS '渠道编码';
COMMENT ON COLUMN channel_info.channel_name IS '渠道名称';
COMMENT ON COLUMN channel_info.face_mode IS '开通人脸(数据字典:0-不开通 1-开通)';
COMMENT ON COLUMN channel_info.finger_mode IS '开通指纹(数据字典:0-不开通 1-开通)';
COMMENT ON COLUMN channel_info.iris_mode IS '开通虹膜(数据字典:0-不开通 1-开通)';
COMMENT ON COLUMN channel_info.fvein_mode IS '开通指静脉(数据字典:0-不开通 1-开通)';
COMMENT ON COLUMN channel_info.face_iris_mode IS '开通人脸虹膜多模态(数据字典:0-不开通 1-开通)';
COMMENT ON COLUMN channel_info.enable_multi_faces IS '是否支持多人脸(数据字典:N-否 Y-是)';
COMMENT ON COLUMN channel_info.search_n IS '1-N识别方式(数据字典:1-校验分库、2-校验渠道库、3-校验全库, 4-依次校验)';
COMMENT ON COLUMN channel_info.device_num_limit IS '挂载设备数量上限';
COMMENT ON COLUMN channel_info.remark IS '备注';
COMMENT ON COLUMN channel_info.create_by IS '创建人';
COMMENT ON COLUMN channel_info.update_by IS '修改人';
COMMENT ON COLUMN channel_info.create_time IS '创建时间';
COMMENT ON COLUMN channel_info.update_time IS '修改时间';
COMMENT ON COLUMN channel_info.batch_date IS '定时任务执行时间';
COMMENT ON COLUMN channel_info.tenant_id IS '租户ID';
COMMENT ON TABLE channel_info IS '渠道管理_渠道信息表';

CREATE TABLE channel_param (
  id varchar(48) NOT NULL,
  channel_id varchar(48) NOT NULL,
  param_code varchar(255) NOT NULL,
  param_name varchar(255),
  param_value varchar(255) NOT NULL,
  bio_attest_type varchar(1) NOT NULL,
  remark varchar(255),
  create_by varchar(64),
  update_by varchar(64),
  create_time timestamp,
  update_time timestamp,
  batch_date timestamp,
  tenant_id varchar(255) DEFAULT 'super',
  CONSTRAINT pk_channel_param PRIMARY KEY (id)
);
COMMENT ON COLUMN channel_param.id IS '主键';
COMMENT ON COLUMN channel_param.channel_id IS '渠道主键';
COMMENT ON COLUMN channel_param.param_code IS '参数编码';
COMMENT ON COLUMN channel_param.param_name IS '参数名称';
COMMENT ON COLUMN channel_param.param_value IS '参数键值';
COMMENT ON COLUMN channel_param.bio_attest_type IS '认证类型(数据字典::0-人脸  1指纹 2虹膜 3指静脉)';
COMMENT ON COLUMN channel_param.remark IS '备注';
COMMENT ON COLUMN channel_param.create_by IS '创建人';
COMMENT ON COLUMN channel_param.update_by IS '修改人';
COMMENT ON COLUMN channel_param.create_time IS '创建时间';
COMMENT ON COLUMN channel_param.update_time IS '修改时间';
COMMENT ON COLUMN channel_param.batch_date IS '定时任务执行时间';
COMMENT ON COLUMN channel_param.tenant_id IS '租户ID';
COMMENT ON TABLE channel_param IS '渠道管理_渠道参数表';

CREATE TABLE channel_subtreasury_busi (
  id varchar(48) NOT NULL,
  channel_id varchar(48) NOT NULL,
  sub_treasury_id varchar(48) NOT NULL,
  person_id varchar(48) NOT NULL,
  unique_id varchar(48) NOT NULL,
  datasource varchar(48) NOT NULL DEFAULT 'INTERFACE',
  status char(1) NOT NULL DEFAULT '0',
  remark varchar(255),
  create_by varchar(64),
  update_by varchar(64),
  create_time timestamp,
  update_time timestamp,
  batch_date timestamp,
  tenant_id varchar(255) DEFAULT 'super',
  update_seria_num int8,
  CONSTRAINT pk_channel_subtreasury_busi PRIMARY KEY (id)
);
CREATE UNIQUE INDEX cIdSubCodePIdUnique ON channel_subtreasury_busi (channel_id,sub_treasury_id, person_id);
CREATE UNIQUE INDEX subBusiUpdateSeqUnique ON channel_subtreasury_busi (update_seria_num);
COMMENT ON COLUMN channel_subtreasury_busi.id IS '主键';
COMMENT ON COLUMN channel_subtreasury_busi.channel_id IS '渠道主键';
COMMENT ON COLUMN channel_subtreasury_busi.sub_treasury_id IS '分库主键';
COMMENT ON COLUMN channel_subtreasury_busi.person_id IS '人员主键';
COMMENT ON COLUMN channel_subtreasury_busi.unique_id IS '人员唯一标识';
COMMENT ON COLUMN channel_subtreasury_busi.datasource IS '数据来源(数据字典)';
COMMENT ON COLUMN channel_subtreasury_busi.status IS '状态(数据字典:0-有效  1:无效)';
COMMENT ON COLUMN channel_subtreasury_busi.remark IS '备注';
COMMENT ON COLUMN channel_subtreasury_busi.create_by IS '创建人';
COMMENT ON COLUMN channel_subtreasury_busi.update_by IS '修改人';
COMMENT ON COLUMN channel_subtreasury_busi.create_time IS '创建时间';
COMMENT ON COLUMN channel_subtreasury_busi.update_time IS '修改时间';
COMMENT ON COLUMN channel_subtreasury_busi.batch_date IS '定时任务执行时间';
COMMENT ON COLUMN channel_subtreasury_busi.tenant_id IS '租户ID';
COMMENT ON COLUMN channel_subtreasury_busi.update_seria_num IS '业务更新标识流水码';
COMMENT ON TABLE channel_subtreasury_busi IS '渠道管理_分库业务表';

CREATE TABLE channel_subtreasury_info (
  id varchar(48) NOT NULL,
  channel_id varchar(48) NOT NULL,
  sub_treasury_code varchar(100) NOT NULL,
  sub_treasury_name varchar(100) NOT NULL,
  remark varchar(255),
  create_by varchar(64),
  update_by varchar(64),
  create_time timestamp,
  update_time timestamp,
  batch_date timestamp,
  tenant_id varchar(255) DEFAULT 'super',
  CONSTRAINT pk_channel_subtreasury_info PRIMARY KEY (id)
);
COMMENT ON COLUMN channel_subtreasury_info.id IS '主键';
COMMENT ON COLUMN channel_subtreasury_info.channel_id IS '渠道主键';
COMMENT ON COLUMN channel_subtreasury_info.sub_treasury_code IS '分库号(渠道编码_分库号)';
COMMENT ON COLUMN channel_subtreasury_info.sub_treasury_name IS '分库名称';
COMMENT ON COLUMN channel_subtreasury_info.remark IS '备注';
COMMENT ON COLUMN channel_subtreasury_info.create_by IS '创建人';
COMMENT ON COLUMN channel_subtreasury_info.update_by IS '修改人';
COMMENT ON COLUMN channel_subtreasury_info.create_time IS '创建时间';
COMMENT ON COLUMN channel_subtreasury_info.update_time IS '修改时间';
COMMENT ON COLUMN channel_subtreasury_info.batch_date IS '定时任务执行时间';
COMMENT ON COLUMN channel_subtreasury_info.tenant_id IS '租户ID';
COMMENT ON TABLE channel_subtreasury_info IS '分库信息表';

CREATE TABLE sys_tenant_interface (
  tenant_id varchar(255) NOT NULL,
  transcode varchar(255) NOT NULL,
  expire_time timestamp NOT NULL,
  create_time timestamp,
  update_time timestamp,
  CONSTRAINT pk_sys_tenant_interface PRIMARY KEY (tenant_id, transcode)
);
COMMENT ON COLUMN sys_tenant_interface.tenant_id IS '租户ID';
COMMENT ON COLUMN sys_tenant_interface.transcode IS '接口交易码';
COMMENT ON COLUMN sys_tenant_interface.expire_time IS '过期时间';
COMMENT ON COLUMN sys_tenant_interface.create_time IS '创建时间';
COMMENT ON COLUMN sys_tenant_interface.update_time IS '更新时间';
COMMENT ON TABLE sys_tenant_interface IS '租户和接口关联表';


CREATE TABLE area_model (
  id int8 NOT NULL,
  parent_id int8 DEFAULT '0',
  ancestors varchar(50) DEFAULT '',
  area_name varchar(30) DEFAULT '',
  order_num int4 DEFAULT '0',
  area_type varchar(30) DEFAULT '',
  status char(1) DEFAULT '0' ,
  create_by varchar(64),
  create_time timestamp,
  update_by varchar(64) DEFAULT '',
  update_time timestamp,
  remark varchar(500) DEFAULT '',
  tenant_id varchar(255) DEFAULT 'super',
  CONSTRAINT pk_area_model PRIMARY KEY (id)
);
COMMENT ON COLUMN area_model.id IS '区域id';
COMMENT ON COLUMN area_model.parent_id IS '父区域id';
COMMENT ON COLUMN area_model.ancestors IS '祖级列表';
COMMENT ON COLUMN area_model.area_name IS '区域名称';
COMMENT ON COLUMN area_model.order_num IS '显示顺序';
COMMENT ON COLUMN area_model.area_type IS '区域类型 字典';
COMMENT ON COLUMN area_model.status IS '区域状态（0正常 1停用）';
COMMENT ON COLUMN area_model.create_by IS '创建者';
COMMENT ON COLUMN area_model.create_time IS '创建时间';
COMMENT ON COLUMN area_model.update_by IS '更新者';
COMMENT ON COLUMN area_model.update_time IS '更新时间';
COMMENT ON COLUMN area_model.remark IS '备注';
COMMENT ON COLUMN area_model.tenant_id IS '租户ID';
COMMENT ON TABLE area_model IS '区域表';

CREATE TABLE device_model (
  id varchar(48) NOT NULL,
  model_code varchar(100) NOT NULL,
  model_name varchar(255) NOT NULL,
  exterior_image varchar(255),
  model_desc varchar(2000),
  create_by varchar(64),
  update_by varchar(64),
  create_time timestamp,
  update_time timestamp,
  tenant_id varchar(255) DEFAULT 'super',
  CONSTRAINT pk_device_model PRIMARY KEY (id)
);
CREATE UNIQUE INDEX model_code_index ON device_model (model_code, tenant_id);
COMMENT ON COLUMN device_model.id IS '主键';
COMMENT ON COLUMN device_model.model_code IS '设备型号编码';
COMMENT ON COLUMN device_model.model_name IS '设备型号名称';
COMMENT ON COLUMN device_model.exterior_image IS '设备外观图片';
COMMENT ON COLUMN device_model.model_desc IS '设备型号描述';
COMMENT ON COLUMN device_model.create_by IS '创建人';
COMMENT ON COLUMN device_model.update_by IS '修改人';
COMMENT ON COLUMN device_model.create_time IS '创建时间';
COMMENT ON COLUMN device_model.update_time IS '修改时间';
COMMENT ON COLUMN device_model.tenant_id IS '租户ID';
COMMENT ON TABLE device_model IS '设备型号信息';

CREATE TABLE device_param_info (
  id varchar(48) NOT NULL,
  param_code varchar(100) NOT NULL,
  param_name varchar(255) NOT NULL,
  param_desc varchar(2000),
  create_by varchar(64),
  update_by varchar(64),
  create_time timestamp,
  update_time timestamp,
  tenant_id varchar(255) DEFAULT 'super',
  CONSTRAINT pk_device_param_info PRIMARY KEY (id)
);
CREATE UNIQUE INDEX param_code_index ON device_param_info (param_code, tenant_id);
COMMENT ON COLUMN device_param_info.id IS '主键';
COMMENT ON COLUMN device_param_info.param_code IS '设备参数编码';
COMMENT ON COLUMN device_param_info.param_name IS '设备参数名称';
COMMENT ON COLUMN device_param_info.param_desc IS '设备参数描述';
COMMENT ON COLUMN device_param_info.create_by IS '创建人';
COMMENT ON COLUMN device_param_info.update_by IS '修改人';
COMMENT ON COLUMN device_param_info.create_time IS '创建时间';
COMMENT ON COLUMN device_param_info.update_time IS '修改时间';
COMMENT ON COLUMN device_param_info.tenant_id IS '租户ID';
COMMENT ON TABLE device_param_info IS '设备参数信息';

CREATE TABLE device_param_model_rel (
  id varchar(48) NOT NULL,
  model_code varchar(255) NOT NULL,
  param_code varchar(255) NOT NULL,
  create_by varchar(64),
  update_by varchar(64),
  create_time timestamp,
  update_time timestamp,
  tenant_id varchar(255) DEFAULT 'super',
  CONSTRAINT pk_device_param_model_rel PRIMARY KEY (id)
);
COMMENT ON COLUMN device_param_model_rel.id IS '主键';
COMMENT ON COLUMN device_param_model_rel.model_code IS '设备型号编码';
COMMENT ON COLUMN device_param_model_rel.param_code IS '设备参数编码';
COMMENT ON COLUMN device_param_model_rel.create_by IS '创建人';
COMMENT ON COLUMN device_param_model_rel.update_by IS '修改人';
COMMENT ON COLUMN device_param_model_rel.create_time IS '创建时间';
COMMENT ON COLUMN device_param_model_rel.update_time IS '修改时间';
COMMENT ON COLUMN device_param_model_rel.tenant_id IS '租户ID';
COMMENT ON TABLE device_param_model_rel IS '参数型号关系';

CREATE TABLE device_info (
  id varchar(48) NOT NULL,
  device_no varchar(48) NOT NULL,
  device_name varchar(255),
  device_addr varchar(255),
  device_model_code varchar(48),
  device_ip varchar(48),
  device_mac varchar(48),
  longitude float8,
  latitude float8,
  channel_code varchar(255),
  area_id int8,
  subtreasury_code varchar(255),
  import_batch_num varchar(48),
  create_method char(1) NOT NULL DEFAULT '1',
  pull_all_flag char(1) DEFAULT '1',
  primary_sub_code varchar(48),
  duplicate_time int4,
  device_type char(1) NOT NULL DEFAULT '1',
  mqtt_pwd varchar(255) NOT NULL,
  mqtt_salt varchar(255) NOT NULL,
  ext_info varchar(4000),
  create_by varchar(64),
  update_by varchar(64),
  create_time timestamp,
  update_time timestamp,
  batch_date timestamp,
  tenant_id varchar(255) DEFAULT 'super',
  CONSTRAINT pk_device_info PRIMARY KEY (id)
);
CREATE UNIQUE INDEX deviceno_unique_index ON device_info(device_no);
COMMENT ON COLUMN device_info.id IS '主键';
COMMENT ON COLUMN device_info.device_no IS '设备编号';
COMMENT ON COLUMN device_info.device_name IS '设备名称';
COMMENT ON COLUMN device_info.device_addr IS '安装地点';
COMMENT ON COLUMN device_info.device_model_code IS '型号编码';
COMMENT ON COLUMN device_info.device_ip IS '设备IP';
COMMENT ON COLUMN device_info.device_mac IS '设备Mac';
COMMENT ON COLUMN device_info.longitude IS '设备经度';
COMMENT ON COLUMN device_info.latitude IS '设备纬度';
COMMENT ON COLUMN device_info.channel_code IS '渠道编码';
COMMENT ON COLUMN device_info.area_id IS '区域主键';
COMMENT ON COLUMN device_info.subtreasury_code IS '分库编码(用,分隔)';
COMMENT ON COLUMN device_info.import_batch_num IS '导入批次';
COMMENT ON COLUMN device_info.create_method IS '创建方式(1: 后台添加，2：自主注册)';
COMMENT ON COLUMN device_info.pull_all_flag IS '是否进行全量拉取（1：是，0：否）';
COMMENT ON COLUMN device_info.primary_sub_code IS '主分库编码';
COMMENT ON COLUMN device_info.duplicate_time IS '后端比对去重时长(ms)';
COMMENT ON COLUMN device_info.device_type IS '设备类型(字典 1:常规 2..)';
COMMENT ON COLUMN device_info.mqtt_pwd IS 'MQTT连接密码';
COMMENT ON COLUMN device_info.mqtt_salt IS 'MQTT密码盐值';
COMMENT ON COLUMN device_info.ext_info IS '扩展信息';
COMMENT ON COLUMN device_info.create_by IS '创建人';
COMMENT ON COLUMN device_info.update_by IS '修改人';
COMMENT ON COLUMN device_info.create_time IS '创建时间';
COMMENT ON COLUMN device_info.update_time IS '修改时间';
COMMENT ON COLUMN device_info.batch_date IS '定时任务执行时间';
COMMENT ON COLUMN device_info.tenant_id IS '租户ID';
COMMENT ON TABLE device_info IS '设备信息表';

CREATE TABLE device_batch_log (
  id varchar(48) NOT NULL,
  batch_num varchar(50) NOT NULL,
  batch_desc varchar(255) NOT NULL,
  rollbacked char(1) NOT NULL DEFAULT '0',
  rollback_time timestamp,
  create_time timestamp,
  create_by varchar(255),
  update_time timestamp,
  update_by varchar(255),
  tenant_id varchar(255) DEFAULT 'super',
  CONSTRAINT pk_device_batch_log PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uk_batch_num ON device_batch_log(batch_num);
COMMENT ON COLUMN device_batch_log.id IS '主键';
COMMENT ON COLUMN device_batch_log.batch_num IS '批次号';
COMMENT ON COLUMN device_batch_log.batch_desc IS '批次说明';
COMMENT ON COLUMN device_batch_log.rollbacked IS '是否回滚(1:是， 0:否)';
COMMENT ON COLUMN device_batch_log.rollback_time IS '回滚时间';
COMMENT ON COLUMN device_batch_log.create_time IS '创建时间';
COMMENT ON COLUMN device_batch_log.create_by IS '创建人';
COMMENT ON COLUMN device_batch_log.update_time IS '修改时间';
COMMENT ON COLUMN device_batch_log.update_by IS '修改人';
COMMENT ON COLUMN device_batch_log.tenant_id IS '导入租户';
COMMENT ON TABLE device_batch_log IS '设备导入批次日志';

CREATE TABLE device_upgrade_version (
  id varchar(48) NOT NULL,
  app_name varchar(48) NOT NULL,
  version varchar(48) NOT NULL,
  description varchar(255),
  path varchar(255) NOT NULL,
  file_size int8 NOT NULL,
  filename varchar(255) NOT NULL,
  md5 varchar(255) NOT NULL,
  enabled char(1) DEFAULT '0',
  create_by varchar(64),
  update_by varchar(64),
  create_time timestamp,
  update_time timestamp,
  batch_date timestamp,
  tenant_id varchar(255) DEFAULT 'super',
  CONSTRAINT pk_device_upgrade_version PRIMARY KEY (id)
);
COMMENT ON COLUMN device_upgrade_version.id IS '主键';
COMMENT ON COLUMN device_upgrade_version.app_name IS 'APP名';
COMMENT ON COLUMN device_upgrade_version.version IS '版本号';
COMMENT ON COLUMN device_upgrade_version.description IS '版本描述';
COMMENT ON COLUMN device_upgrade_version.path IS '升级文件';
COMMENT ON COLUMN device_upgrade_version.file_size IS '版本大小(B)';
COMMENT ON COLUMN device_upgrade_version.filename IS '源文件名';
COMMENT ON COLUMN device_upgrade_version.md5 IS '文件MD5';
COMMENT ON COLUMN device_upgrade_version.enabled IS '是否启用(1:启用 0:停用)';
COMMENT ON COLUMN device_upgrade_version.create_by IS '创建人';
COMMENT ON COLUMN device_upgrade_version.update_by IS '修改人';
COMMENT ON COLUMN device_upgrade_version.create_time IS '创建时间';
COMMENT ON COLUMN device_upgrade_version.update_time IS '修改时间';
COMMENT ON COLUMN device_upgrade_version.batch_date IS '定时任务执行时间';
COMMENT ON COLUMN device_upgrade_version.tenant_id IS '租户ID';
COMMENT ON TABLE device_upgrade_version IS '版本信息表';

CREATE TABLE device_upgrade_task (
  id varchar(48) NOT NULL,
  version_id varchar(48) NOT NULL,
  device_id varchar(48) NOT NULL,
  upgrade_time timestamp,
  upgrade_count_limit int4 NOT NULL,
  execute_count int4 NOT NULL DEFAULT '0',
  pub_count int4 DEFAULT '0',
  task_index int4 NOT NULL,
  execute_result varchar(1) NOT NULL DEFAULT '1' ,
  rollback_install char(1) NOT NULL  DEFAULT '0',
  create_by varchar(64),
  update_by varchar(64),
  create_time timestamp,
  update_time timestamp,
  batch_date timestamp,
  tenant_id varchar(255) DEFAULT 'super' ,
  CONSTRAINT pk_device_upgrade_task PRIMARY KEY (id)
);
COMMENT ON COLUMN device_upgrade_task.id IS '主键';
COMMENT ON COLUMN device_upgrade_task.version_id IS '版本主键';
COMMENT ON COLUMN device_upgrade_task.device_id IS '设备主键';
COMMENT ON COLUMN device_upgrade_task.upgrade_time IS '更新时间(如果为空，表示立即更新)';
COMMENT ON COLUMN device_upgrade_task.upgrade_count_limit IS '升级次数上限';
COMMENT ON COLUMN device_upgrade_task.execute_count IS '执行次数';
COMMENT ON COLUMN device_upgrade_task.pub_count IS '发布次数';
COMMENT ON COLUMN device_upgrade_task.task_index IS '任务排序';
COMMENT ON COLUMN device_upgrade_task.execute_result IS '执行结果(1:待执行，2：成功，3：失败，4：跳过)';
COMMENT ON COLUMN device_upgrade_task.rollback_install IS '是否降级安装(1：是，0：否)';
COMMENT ON COLUMN device_upgrade_task.create_by IS '创建人';
COMMENT ON COLUMN device_upgrade_task.update_by IS '修改人';
COMMENT ON COLUMN device_upgrade_task.create_time IS '创建时间';
COMMENT ON COLUMN device_upgrade_task.update_time IS '修改时间';
COMMENT ON COLUMN device_upgrade_task.batch_date IS '定时任务执行时间';
COMMENT ON COLUMN device_upgrade_task.tenant_id IS '租户ID';
COMMENT ON TABLE device_upgrade_task IS '升级任务表';

CREATE TABLE device_upgrade_log (
  id varchar(48) NOT NULL,
  task_id varchar(48) NOT NULL,
  device_no varchar(48) NOT NULL,
  device_name varchar(255),
  before_app_name varchar(48),
  after_app_name varchar(48) NOT NULL,
  before_version varchar(48),
  after_version varchar(48) NOT NULL,
  upgrade_status char(1) NOT NULL,
  client_time timestamp,
  server_time timestamp,
  time_used int4,
  fail_reason varchar(500),
  create_time timestamp,
  update_time timestamp,
  batch_date timestamp,
  tenant_id varchar(255) DEFAULT 'super',
  CONSTRAINT pk_device_upgrade_log PRIMARY KEY (id)
);
COMMENT ON COLUMN device_upgrade_log.id IS '主键';
COMMENT ON COLUMN device_upgrade_log.task_id IS '任务主键';
COMMENT ON COLUMN device_upgrade_log.device_no IS '设备编号';
COMMENT ON COLUMN device_upgrade_log.device_name IS '设备名称';
COMMENT ON COLUMN device_upgrade_log.before_app_name IS '升级前APP名';
COMMENT ON COLUMN device_upgrade_log.after_app_name IS '升级后APP名';
COMMENT ON COLUMN device_upgrade_log.before_version IS '升级前版本';
COMMENT ON COLUMN device_upgrade_log.after_version IS '升级后版本';
COMMENT ON COLUMN device_upgrade_log.upgrade_status IS '更新状态(1:待下载，2：待更新，3：更新成功，4：更新失败，5：跳过)';
COMMENT ON COLUMN device_upgrade_log.client_time IS '升级时前端时间';
COMMENT ON COLUMN device_upgrade_log.server_time IS '升级时后端时间';
COMMENT ON COLUMN device_upgrade_log.time_used IS '升级耗时(单位s)';
COMMENT ON COLUMN device_upgrade_log.fail_reason IS '失败原因';
COMMENT ON COLUMN device_upgrade_log.create_time IS '创建时间';
COMMENT ON COLUMN device_upgrade_log.update_time IS '创建时间';
COMMENT ON COLUMN device_upgrade_log.batch_date IS '定时任务执行时间';
COMMENT ON COLUMN device_upgrade_log.tenant_id IS '租户ID';
COMMENT ON TABLE device_upgrade_log IS '升级日志表';

CREATE TABLE device_param_distribute_log (
  id varchar(48) NOT NULL,
  device_no varchar(48) NOT NULL,
  param_code varchar(255) NOT NULL,
  param_value varchar(255) NOT NULL,
  distribute_result char(1),
  sort_index int8 NOT NULL,
  create_time timestamp,
  update_time timestamp,
  tenant_id varchar(255) DEFAULT 'super',
  CONSTRAINT pk_device_param_distribute_log PRIMARY KEY (id)
);
COMMENT ON COLUMN device_param_distribute_log.id IS '主键';
COMMENT ON COLUMN device_param_distribute_log.device_no IS '设备编号';
COMMENT ON COLUMN device_param_distribute_log.param_code IS '参数编码';
COMMENT ON COLUMN device_param_distribute_log.param_value IS '参数值';
COMMENT ON COLUMN device_param_distribute_log.distribute_result IS '下发结果(0：成功，1：失败)';
COMMENT ON COLUMN device_param_distribute_log.sort_index IS '下发排序';
COMMENT ON COLUMN device_param_distribute_log.create_time IS '创建时间';
COMMENT ON COLUMN device_param_distribute_log.update_time IS '更新时间';
COMMENT ON COLUMN device_param_distribute_log.tenant_id IS '租户ID';
COMMENT ON TABLE device_param_distribute_log IS '参数下发日志';

CREATE TABLE device_action_log (
  id varchar(48) NOT NULL,
  device_name varchar(255),
  device_no varchar(48) NOT NULL,
  channel_code varchar(48),
  model_code varchar(48),
  action_type char(1) NOT NULL,
  create_time timestamp,
  update_time timestamp,
  tenant_id varchar(255) DEFAULT 'super',
  CONSTRAINT pk_device_action_log PRIMARY KEY (id)
);
COMMENT ON COLUMN device_action_log.id IS '主键';
COMMENT ON COLUMN device_action_log.device_name IS '设备名称';
COMMENT ON COLUMN device_action_log.device_no IS '设备编号';
COMMENT ON COLUMN device_action_log.channel_code IS '设备渠道编码';
COMMENT ON COLUMN device_action_log.model_code IS '设备型号编码';
COMMENT ON COLUMN device_action_log.action_type IS '动作类型(1:注册 2:上线 3:下线 4:更新)';
COMMENT ON COLUMN device_action_log.create_time IS '创建时间';
COMMENT ON COLUMN device_action_log.update_time IS '更新时间';
COMMENT ON COLUMN device_action_log.tenant_id IS '租户ID';
COMMENT ON TABLE device_action_log IS '设备动作日志';

CREATE TABLE device_access_adapter (
  id varchar(48) NOT NULL,
  device_name varchar(255),
  device_sn varchar(48) NOT NULL,
  seria_num varchar(100) NOT NULL,
  create_by varchar(64),
  update_by varchar(64),
  create_time timestamp,
  update_time timestamp,
  tenant_id varchar(255) DEFAULT 'super',
  CONSTRAINT pk_device_access_adapter PRIMARY KEY (id)
);
CREATE UNIQUE INDEX idx_sn ON device_access_adapter(device_sn);
COMMENT ON COLUMN device_access_adapter.id IS '主键';
COMMENT ON COLUMN device_access_adapter.device_name IS '设备名称';
COMMENT ON COLUMN device_access_adapter.device_sn IS '设备序列号';
COMMENT ON COLUMN device_access_adapter.seria_num IS '同步序列号';
COMMENT ON COLUMN device_access_adapter.create_by IS '创建者';
COMMENT ON COLUMN device_access_adapter.update_by IS '更新者';
COMMENT ON COLUMN device_access_adapter.create_time IS '创建时间';
COMMENT ON COLUMN device_access_adapter.update_time IS '更新时间';
COMMENT ON COLUMN device_access_adapter.tenant_id IS '租户id';
COMMENT ON TABLE device_access_adapter IS '203设备接入';

CREATE TABLE sys_sdk_file (
  id varchar(48) NOT NULL,
  file_name varchar(255) NOT NULL,
  file_path varchar(255) NOT NULL,
  md5 varchar(255) NOT NULL,
  sdk_type varchar(20) NOT NULL,
  create_by varchar(64),
  update_by varchar(64),
  create_time timestamp,
  update_time timestamp,
  tenant_id varchar(255) NOT NULL,
  sorted_no int4 NOT NULL,
  CONSTRAINT pk_sys_sdk_file PRIMARY KEY (id)
);
COMMENT ON COLUMN sys_sdk_file.id IS '主键';
COMMENT ON COLUMN sys_sdk_file.file_name IS '上传文件名称';
COMMENT ON COLUMN sys_sdk_file.file_path IS '文件存储路径';
COMMENT ON COLUMN sys_sdk_file.md5 IS '文件MD5';
COMMENT ON COLUMN sys_sdk_file.sdk_type IS 'SDK类型';
COMMENT ON COLUMN sys_sdk_file.create_by IS '创建人';
COMMENT ON COLUMN sys_sdk_file.update_by IS '修改人';
COMMENT ON COLUMN sys_sdk_file.create_time IS '创建时间';
COMMENT ON COLUMN sys_sdk_file.update_time IS '修改时间';
COMMENT ON COLUMN sys_sdk_file.tenant_id IS '租户ID';
COMMENT ON COLUMN sys_sdk_file.sorted_no IS '版本排序';
COMMENT ON TABLE sys_sdk_file IS 'SDK文件上传表';

CREATE TABLE app_info (
  id varchar(48) NOT NULL,
  app_key varchar(255) NOT NULL,
  app_secret varchar(255) NOT NULL,
  app_desc varchar(255),
  built_in varchar(1) NOT NULL DEFAULT 'N',
  remark varchar(255),
  status char(1) NOT NULL DEFAULT '0',
  create_by varchar(64),
  update_by varchar(64),
  create_time timestamp,
  update_time timestamp,
  tenant_id varchar(255)  DEFAULT 'super',
  CONSTRAINT pk_app_info PRIMARY KEY (id)
);
COMMENT ON COLUMN app_info.id IS '主键';
COMMENT ON COLUMN app_info.app_key IS '应用系统键值';
COMMENT ON COLUMN app_info.app_secret IS '应用系统密钥';
COMMENT ON COLUMN app_info.app_desc IS '应用系统描述';
COMMENT ON COLUMN app_info.built_in IS '是否内置(Y：是，N：否)';
COMMENT ON COLUMN app_info.remark IS '备注';
COMMENT ON COLUMN app_info.status IS '状态(数据字典:0-有效  1:无效)';
COMMENT ON COLUMN app_info.create_by IS '创建人';
COMMENT ON COLUMN app_info.update_by IS '修改人';
COMMENT ON COLUMN app_info.create_time IS '创建时间';
COMMENT ON COLUMN app_info.update_time IS '修改时间';
COMMENT ON COLUMN app_info.tenant_id IS '租户ID';
COMMENT ON TABLE app_info IS '应用系统信息表';

CREATE TABLE app_interface_auth (
  id varchar(48) NOT NULL,
  app_id varchar(48) NOT NULL,
  trans_code varchar(255) NOT NULL,
  trans_end_time timestamp,
  remark varchar(255),
  create_by varchar(64),
  update_by varchar(64),
  create_time timestamp,
  update_time timestamp,
  tenant_id varchar(255) DEFAULT 'super',
  CONSTRAINT pk_app_interface_auth PRIMARY KEY (id)
);
COMMENT ON COLUMN app_interface_auth.id IS '主键';
COMMENT ON COLUMN app_interface_auth.app_id IS '应用系统主键';
COMMENT ON COLUMN app_interface_auth.trans_code IS '接口交易码(数据字典)';
COMMENT ON COLUMN app_interface_auth.trans_end_time IS '交易有效截止时间';
COMMENT ON COLUMN app_interface_auth.remark IS '备注';
COMMENT ON COLUMN app_interface_auth.create_by IS '创建人';
COMMENT ON COLUMN app_interface_auth.update_by IS '修改人';
COMMENT ON COLUMN app_interface_auth.create_time IS '创建时间';
COMMENT ON COLUMN app_interface_auth.update_time IS '修改时间';
COMMENT ON COLUMN app_interface_auth.tenant_id IS '租户ID';
COMMENT ON TABLE app_interface_auth IS '应用系统接口授权表';

CREATE TABLE msg_ding_application (
  id varchar(48) NOT NULL,
  team_id varchar(255) NOT NULL,
  corp_id varchar(255) NOT NULL,
  app_name varchar(255) NOT NULL,
  agent_id varchar(255) NOT NULL,
  app_key varchar(255) NOT NULL,
  app_secrect varchar(255) NOT NULL,
  short_des varchar(255),
  create_time timestamp,
  update_time timestamp,
  tenant_id varchar(255) DEFAULT 'super',
  CONSTRAINT pk_msg_ding_application PRIMARY KEY (id)
);
COMMENT ON COLUMN msg_ding_application.id IS '主键';
COMMENT ON COLUMN msg_ding_application.team_id IS '团队(企业)主键';
COMMENT ON COLUMN msg_ding_application.corp_id IS '团队(企业)CorpId';
COMMENT ON COLUMN msg_ding_application.app_name IS '微应用名称';
COMMENT ON COLUMN msg_ding_application.agent_id IS '微应用agentId';
COMMENT ON COLUMN msg_ding_application.app_key IS '微应用AppKey';
COMMENT ON COLUMN msg_ding_application.app_secrect IS '微应用AppSecrect';
COMMENT ON COLUMN msg_ding_application.short_des IS '微应用简介';
COMMENT ON COLUMN msg_ding_application.create_time IS '创建时间';
COMMENT ON COLUMN msg_ding_application.update_time IS '更新时间';
COMMENT ON COLUMN msg_ding_application.tenant_id IS '租户ID';
COMMENT ON TABLE msg_ding_application IS '钉钉微应用';

CREATE TABLE msg_ding_team (
  id varchar(48) NOT NULL,
  corp_id varchar(255) NOT NULL,
  team_name varchar(255) NOT NULL,
  team_des varchar(500),
  create_time timestamp,
  update_time timestamp,
  tenant_id varchar(255) DEFAULT 'super',
  CONSTRAINT pk_msg_ding_team PRIMARY KEY (id)
);
COMMENT ON COLUMN msg_ding_team.id IS '主键';
COMMENT ON COLUMN msg_ding_team.corp_id IS '团队(企业)CorpId';
COMMENT ON COLUMN msg_ding_team.team_name IS '团队(企业)名称';
COMMENT ON COLUMN msg_ding_team.team_des IS '团队(企业)说明';
COMMENT ON COLUMN msg_ding_team.create_time IS '创建时间';
COMMENT ON COLUMN msg_ding_team.update_time IS '更新时间';
COMMENT ON COLUMN msg_ding_team.tenant_id IS '租户ID';
COMMENT ON TABLE msg_ding_team IS '钉钉团队(企业)';

CREATE TABLE msg_log (
  id varchar(48) NOT NULL,
  received_seq varchar(48) NOT NULL,
  notice_method varchar(1) NOT NULL,
  msg_subject varchar(255),
  msg_content varchar(4000),
  has_annex varchar(1) NOT NULL DEFAULT 'N',
  from_user varchar(255),
  to_user varchar(4000),
  offical_account_id varchar(48),
  offical_account_name varchar(255),
  scene_remark varchar(48),
  result_status varchar(1) NOT NULL,
  err_msg varchar(255),
  json_response varchar(4000),
  create_time timestamp not null,
  tenant_id varchar(255) DEFAULT 'super',
  CONSTRAINT pk_msg_log PRIMARY KEY (id, create_time)
);
COMMENT ON COLUMN msg_log.id IS '主键';
COMMENT ON COLUMN msg_log.received_seq IS '业务流水号';
COMMENT ON COLUMN msg_log.notice_method IS '通知方式(1：短信，2：邮件，3：微信，4：钉钉)';
COMMENT ON COLUMN msg_log.msg_subject IS '消息主题';
COMMENT ON COLUMN msg_log.msg_content IS '消息内容';
COMMENT ON COLUMN msg_log.has_annex IS '是否包含附件(Y：是，N：否)';
COMMENT ON COLUMN msg_log.from_user IS '发信人标识';
COMMENT ON COLUMN msg_log.to_user IS '收件人标识(多个使用“,”分隔)';
COMMENT ON COLUMN msg_log.offical_account_id IS '公众(企业)号标识';
COMMENT ON COLUMN msg_log.offical_account_name IS '公众(企业)号名称';
COMMENT ON COLUMN msg_log.scene_remark IS '场景标识';
COMMENT ON COLUMN msg_log.result_status IS '发送状态(0：成功，1：失败)';
COMMENT ON COLUMN msg_log.err_msg IS '错误信息';
COMMENT ON COLUMN msg_log.json_response IS '发送结果json';
COMMENT ON COLUMN msg_log.create_time IS '创建时间';
COMMENT ON COLUMN msg_log.tenant_id IS '租户ID';
COMMENT ON TABLE msg_log IS '消息日志';
create trigger trigger_msg_log before insert on msg_log for each row execute procedure ins_record_trigger_fun('yyyymmdd'); 

CREATE TABLE msg_log_annex (
  id varchar(48) NOT NULL,
  log_id varchar(48) NOT NULL,
  file_name varchar(255) NOT NULL,
  file_path varchar(255) NOT NULL,
  file_md5 varchar(48),
  create_time timestamp,
  tenant_id varchar(255) DEFAULT 'super',
  CONSTRAINT pk_msg_log_annex PRIMARY KEY (id)
);
COMMENT ON COLUMN msg_log_annex.id IS '主键';
COMMENT ON COLUMN msg_log_annex.log_id IS '消息日志ID';
COMMENT ON COLUMN msg_log_annex.file_name IS '源文件名称';
COMMENT ON COLUMN msg_log_annex.file_path IS '文件路径';
COMMENT ON COLUMN msg_log_annex.file_md5 IS '文件MD5';
COMMENT ON COLUMN msg_log_annex.create_time IS '创建时间';
COMMENT ON COLUMN msg_log_annex.tenant_id IS '租户ID';
COMMENT ON TABLE msg_log_annex IS '消息日志附件';

CREATE TABLE msg_mail_property (
  id varchar(48) NOT NULL,
  host varchar(255) NOT NULL,
  port int4,
  username varchar(255) NOT NULL,
  password varchar(255) NOT NULL,
  email_addr varchar(255) NOT NULL,
  create_time timestamp,
  update_time timestamp,
  tenant_id varchar(255) DEFAULT 'super',
  CONSTRAINT pk_msg_mail_property PRIMARY KEY (id)
);
COMMENT ON COLUMN msg_mail_property.id IS '主键';
COMMENT ON COLUMN msg_mail_property.host IS 'SMTP服务地址';
COMMENT ON COLUMN msg_mail_property.port IS 'SMTP服务端口';
COMMENT ON COLUMN msg_mail_property.username IS '登录用户名';
COMMENT ON COLUMN msg_mail_property.password IS '登录授权码';
COMMENT ON COLUMN msg_mail_property.email_addr IS '发件邮箱';
COMMENT ON COLUMN msg_mail_property.create_time IS '创建时间';
COMMENT ON COLUMN msg_mail_property.update_time IS '更新时间';
COMMENT ON COLUMN msg_mail_property.tenant_id IS '租户ID';
COMMENT ON TABLE msg_mail_property IS '邮箱配置';

CREATE TABLE msg_offical_account (
  id varchar(48) NOT NULL,
  app_id varchar(500) NOT NULL,
  app_secrect varchar(500) NOT NULL,
  app_name varchar(500) NOT NULL,
  create_time timestamp,
  update_time timestamp,
  tenant_id varchar(255) DEFAULT 'super',
  CONSTRAINT pk_msg_offical_account PRIMARY KEY (id)
);
COMMENT ON COLUMN msg_offical_account.id IS '主键';
COMMENT ON COLUMN msg_offical_account.app_id IS 'AppId';
COMMENT ON COLUMN msg_offical_account.app_secrect IS 'AppSecrect';
COMMENT ON COLUMN msg_offical_account.app_name IS '公众号(云账户)名称';
COMMENT ON COLUMN msg_offical_account.create_time IS '创建时间';
COMMENT ON COLUMN msg_offical_account.update_time IS '更新时间';
COMMENT ON COLUMN msg_offical_account.tenant_id IS '租户ID';
COMMENT ON TABLE msg_offical_account IS '微信公众号';

CREATE TABLE msg_offical_account_user (
  id varchar(48) NOT NULL,
  offical_account_id varchar(48),
  app_id varchar(48) NOT NULL,
  open_id varchar(255) NOT NULL,
  wx_name varchar(255) NOT NULL,
  head_img_url varchar(500),
  phone varchar(30),
  create_time timestamp,
  update_time timestamp,
  tenant_id varchar(255) DEFAULT 'super',
  CONSTRAINT pk_msg_offical_account_user PRIMARY KEY (id)
);
COMMENT ON COLUMN msg_offical_account_user.id IS '主键';
COMMENT ON COLUMN msg_offical_account_user.offical_account_id IS '公众号ID';
COMMENT ON COLUMN msg_offical_account_user.app_id IS '公众号AppId';
COMMENT ON COLUMN msg_offical_account_user.open_id IS '微信标识';
COMMENT ON COLUMN msg_offical_account_user.wx_name IS '微信名称';
COMMENT ON COLUMN msg_offical_account_user.head_img_url IS '微信头像';
COMMENT ON COLUMN msg_offical_account_user.phone IS '绑定手机';
COMMENT ON COLUMN msg_offical_account_user.create_time IS '创建时间';
COMMENT ON COLUMN msg_offical_account_user.update_time IS '更新时间';
COMMENT ON COLUMN msg_offical_account_user.tenant_id IS '租户ID';
COMMENT ON TABLE msg_offical_account_user IS '微信用户';

CREATE TABLE msg_sms_cloud_account (
  id varchar(48) NOT NULL,
  app_id varchar(500) NOT NULL,
  app_secrect varchar(500) NOT NULL,
  app_name varchar(500) NOT NULL,
  create_time timestamp,
  update_time timestamp,
  tenant_id varchar(255) DEFAULT 'super',
  CONSTRAINT pk_msg_sms_cloud_account PRIMARY KEY (id)
);
COMMENT ON COLUMN msg_sms_cloud_account.id IS '主键';
COMMENT ON COLUMN msg_sms_cloud_account.app_id IS 'AppId';
COMMENT ON COLUMN msg_sms_cloud_account.app_secrect IS 'AppSecrect';
COMMENT ON COLUMN msg_sms_cloud_account.app_name IS '账户说明';
COMMENT ON COLUMN msg_sms_cloud_account.create_time IS '创建时间';
COMMENT ON COLUMN msg_sms_cloud_account.update_time IS '更新时间';
COMMENT ON COLUMN msg_sms_cloud_account.tenant_id IS '租户ID';
COMMENT ON TABLE msg_sms_cloud_account IS '短信云账户';

CREATE TABLE msg_template (
  id varchar(48) NOT NULL,
  template_name varchar(255) NOT NULL,
  offical_id varchar(48),
  content varchar(1000) NOT NULL,
  notice_method varchar(1) NOT NULL,
  offical_account_id varchar(48),
  create_time timestamp,
  update_time timestamp,
  tenant_id varchar(255) DEFAULT 'super',
  CONSTRAINT pk_msg_template PRIMARY KEY (id)
);
COMMENT ON COLUMN msg_template.id IS '主键';
COMMENT ON COLUMN msg_template.template_name IS '模板名称';
COMMENT ON COLUMN msg_template.offical_id IS '模板官方ID';
COMMENT ON COLUMN msg_template.content IS '模板内容';
COMMENT ON COLUMN msg_template.notice_method IS '通知方式(1：短信，2：邮件，3：微信，4：钉钉)';
COMMENT ON COLUMN msg_template.offical_account_id IS '公众(企业)号ID';
COMMENT ON COLUMN msg_template.create_time IS '创建时间';
COMMENT ON COLUMN msg_template.update_time IS '更新时间';
COMMENT ON COLUMN msg_template.tenant_id IS '租户ID';
COMMENT ON TABLE msg_template IS '消息模板';

CREATE TABLE msg_weixin_menu (
  id varchar(48) NOT NULL,
  offical_account_id varchar(500) NOT NULL,
  app_id varchar(500) NOT NULL,
  menu_json varchar(4000) NOT NULL,
  create_time timestamp,
  update_time timestamp,
  tenant_id varchar(255) DEFAULT 'super',
  CONSTRAINT pk_msg_weixin_menu PRIMARY KEY (id)
);
COMMENT ON COLUMN msg_weixin_menu.id IS '主键';
COMMENT ON COLUMN msg_weixin_menu.offical_account_id IS '公众号主键';
COMMENT ON COLUMN msg_weixin_menu.app_id IS '公众号AppId';
COMMENT ON COLUMN msg_weixin_menu.menu_json IS '菜单JSON';
COMMENT ON COLUMN msg_weixin_menu.create_time IS '创建时间';
COMMENT ON COLUMN msg_weixin_menu.update_time IS '更新时间';
COMMENT ON COLUMN msg_weixin_menu.tenant_id IS '租户ID';
COMMENT ON TABLE msg_weixin_menu IS '微信公众号菜单';

CREATE TABLE msg_weixin_menu_reply (
  id varchar(48) NOT NULL,
  offical_account_id varchar(500) NOT NULL,
  app_id varchar(500) NOT NULL,
  menu_btn_key varchar(255) NOT NULL,
  res_type varchar(2) NOT NULL,
  reply_content varchar(4000),
  create_time timestamp,
  update_time timestamp,
  tenant_id varchar(255) DEFAULT 'super',
  CONSTRAINT pk_msg_weixin_menu_reply PRIMARY KEY (id)
);
COMMENT ON COLUMN msg_weixin_menu_reply.id IS '主键';
COMMENT ON COLUMN msg_weixin_menu_reply.offical_account_id IS '公众号主键';
COMMENT ON COLUMN msg_weixin_menu_reply.app_id IS '公众号AppId';
COMMENT ON COLUMN msg_weixin_menu_reply.menu_btn_key IS '菜单按钮Key';
COMMENT ON COLUMN msg_weixin_menu_reply.res_type IS '响应类型(1：文本回复)';
COMMENT ON COLUMN msg_weixin_menu_reply.reply_content IS '回复内容';
COMMENT ON COLUMN msg_weixin_menu_reply.create_time IS '创建时间';
COMMENT ON COLUMN msg_weixin_menu_reply.update_time IS '更新时间';
COMMENT ON COLUMN msg_weixin_menu_reply.tenant_id IS '租户ID';
COMMENT ON TABLE msg_weixin_menu_reply IS '微信公众号菜单回复';

CREATE TABLE person_face_match_log (
  id varchar(48) NOT NULL,
  received_seq varchar(48) NOT NULL,
  healthcode_log_id varchar(48),
  unique_id varchar(48),
  dept_id int8,
  channel_code varchar(255),
  scene_image varchar(255) NOT NULL,
  online_image varchar(255),
  chip_image varchar(255),
  stock_image varchar(255),
  scene_video varchar(255),
  scene_online_score float8,
  scene_chip_score float8,
  scene_stock_score float8,
  online_chip_score float8,
  checklive_score float8,
  scene_online_result varchar(1),
  scene_chip_result varchar(1),
  scene_stock_result varchar(1),
  online_chip_result varchar(1),
  checklive_result varchar(1),
  result varchar(1),
  temperature float8,
  temperature_floor float8,
  temperature_top float8,
  device_code varchar(255),
  device_name varchar(255),
  device_model varchar(255),
  device_ip varchar(255),
  device_longitude float8,
  device_dimension float8,
  device_direction varchar(10),
  received_time timestamp NOT NULL,
  time_used int8,
  server_id varchar(255),
  vendor_code varchar(255),
  algs_version varchar(255),
  create_time timestamp,
  batch_date timestamp,
  tenant_id varchar(255) DEFAULT 'super',
  CONSTRAINT pk_person_face_match_log PRIMARY KEY (id, received_time)
);
COMMENT ON COLUMN person_face_match_log.id IS '主键';
COMMENT ON COLUMN person_face_match_log.received_seq IS '业务流水号';
COMMENT ON COLUMN person_face_match_log.healthcode_log_id IS '健康码日志ID';
COMMENT ON COLUMN person_face_match_log.unique_id IS '人员标识';
COMMENT ON COLUMN person_face_match_log.dept_id IS '部门ID';
COMMENT ON COLUMN person_face_match_log.channel_code IS '渠道编码';
COMMENT ON COLUMN person_face_match_log.scene_image IS '现场照路径';
COMMENT ON COLUMN person_face_match_log.online_image IS '联网核查照路径';
COMMENT ON COLUMN person_face_match_log.chip_image IS '芯片照路径';
COMMENT ON COLUMN person_face_match_log.stock_image IS '底库照路径';
COMMENT ON COLUMN person_face_match_log.scene_video IS '现场视频路径';
COMMENT ON COLUMN person_face_match_log.scene_online_score IS '现场照与联网核查照比对分值';
COMMENT ON COLUMN person_face_match_log.scene_chip_score IS '现场照与芯片照比对分值';
COMMENT ON COLUMN person_face_match_log.scene_stock_score IS '现场照与库底库照比对分值';
COMMENT ON COLUMN person_face_match_log.online_chip_score IS '联网核查照与芯片照比对分值';
COMMENT ON COLUMN person_face_match_log.checklive_score IS '检活分值';
COMMENT ON COLUMN person_face_match_log.scene_online_result IS '现场照与联网核查照比对结果(0通过，1未通过)';
COMMENT ON COLUMN person_face_match_log.scene_chip_result IS '现场照与芯片照比对结果(0通过，1未通过)';
COMMENT ON COLUMN person_face_match_log.scene_stock_result IS '现场照与库底库照比对结果(0通过，1未通过)';
COMMENT ON COLUMN person_face_match_log.online_chip_result IS '联网核查照与芯片照比对结果(0通过，1未通过)';
COMMENT ON COLUMN person_face_match_log.checklive_result IS '现场照件检活结果(0通过，1未通过)';
COMMENT ON COLUMN person_face_match_log.result IS '比对结果(0通过，1未通过)';
COMMENT ON COLUMN person_face_match_log.temperature IS '温度';
COMMENT ON COLUMN person_face_match_log.temperature_floor IS '温度阈值下限';
COMMENT ON COLUMN person_face_match_log.temperature_top IS '温度阈值上限';
COMMENT ON COLUMN person_face_match_log.device_code IS '设备编码';
COMMENT ON COLUMN person_face_match_log.device_name IS '设备名称';
COMMENT ON COLUMN person_face_match_log.device_model IS '设备型号编码';
COMMENT ON COLUMN person_face_match_log.device_ip IS '设备IP';
COMMENT ON COLUMN person_face_match_log.device_longitude IS '设备经度(东经)';
COMMENT ON COLUMN person_face_match_log.device_dimension IS '设备维度(北纬)';
COMMENT ON COLUMN person_face_match_log.device_direction IS '设备方向(IN:进，OUT:出，UNKNOWN：未知)';
COMMENT ON COLUMN person_face_match_log.received_time IS '请求时间';
COMMENT ON COLUMN person_face_match_log.time_used IS '耗时(ms)';
COMMENT ON COLUMN person_face_match_log.server_id IS '服务器标识';
COMMENT ON COLUMN person_face_match_log.vendor_code IS '厂商';
COMMENT ON COLUMN person_face_match_log.algs_version IS '算法版本';
COMMENT ON COLUMN person_face_match_log.create_time IS '创建时间';
COMMENT ON COLUMN person_face_match_log.batch_date IS '定时任务执行时间';
COMMENT ON COLUMN person_face_match_log.tenant_id IS '租户ID';
COMMENT ON TABLE person_face_match_log IS '人员人脸比对日志表';
create trigger trigger_face_match_log before insert on person_face_match_log for each row execute procedure ins_record_trigger_fun('yyyymmdd'); 


CREATE TABLE person_face_search_log (
  id varchar(48) NOT NULL,
  received_seq varchar(48) NOT NULL,
  healthcode_log_id varchar(48),
  scene_type varchar(1) NOT NULL,
  unique_id varchar(48),
  dept_id int8,
  channel_code varchar(255),
  sub_treasury_code varchar(255),
  sub_treasury_name varchar(255),
  scene_image varchar(255) NOT NULL,
  stock_image varchar(255),
  take_photo1 varchar(255),
  take_photo2 varchar(255),
  scene_video varchar(255),
  scene_stock_score float8,
  checklive_score float8,
  checklive_result varchar(1),
  temperature float8,
  temperature_floor float8,
  temperature_top float8,
  bio_recognized char(1),
  result varchar(1),
  device_code varchar(255),
  device_name varchar(255),
  device_model varchar(255),
  device_ip varchar(255),
  device_longitude float8,
  device_dimension float8,
  device_direction varchar(10),
  received_time timestamp NOT NULL,
  time_used int8,
  server_id varchar(255),
  vendor_code varchar(255),
  algs_version varchar(255),
  create_time timestamp,
  batch_date timestamp,
  tenant_id varchar(255) DEFAULT 'super',
  CONSTRAINT pk_person_face_search_log PRIMARY KEY (id, received_time)
);
COMMENT ON COLUMN person_face_search_log.id IS '主键';
COMMENT ON COLUMN person_face_search_log.received_seq IS '业务流水号';
COMMENT ON COLUMN person_face_search_log.healthcode_log_id IS '健康码日志ID';
COMMENT ON COLUMN person_face_search_log.scene_type IS '类型(数据字典 1：基础入库1:N，2：比对接口1:N，3：日志回传)';
COMMENT ON COLUMN person_face_search_log.unique_id IS '人员标识';
COMMENT ON COLUMN person_face_search_log.dept_id IS '部门ID';
COMMENT ON COLUMN person_face_search_log.channel_code IS '渠道编码';
COMMENT ON COLUMN person_face_search_log.sub_treasury_code IS '分库编码';
COMMENT ON COLUMN person_face_search_log.sub_treasury_name IS '分库名称';
COMMENT ON COLUMN person_face_search_log.scene_image IS '现场照路径';
COMMENT ON COLUMN person_face_search_log.stock_image IS '底库照路径';
COMMENT ON COLUMN person_face_search_log.take_photo1 IS '抓拍图1路径';
COMMENT ON COLUMN person_face_search_log.take_photo2 IS '抓拍图2路径';
COMMENT ON COLUMN person_face_search_log.scene_video IS '现场视频路径';
COMMENT ON COLUMN person_face_search_log.scene_stock_score IS '分值';
COMMENT ON COLUMN person_face_search_log.checklive_score IS '现场照检活分值';
COMMENT ON COLUMN person_face_search_log.checklive_result IS '现场照检活结果(0通过，1未通过)';
COMMENT ON COLUMN person_face_search_log.temperature IS '温度';
COMMENT ON COLUMN person_face_search_log.temperature_floor IS '温度阈值下限';
COMMENT ON COLUMN person_face_search_log.temperature_top IS '温度阈值上限制';
COMMENT ON COLUMN person_face_search_log.bio_recognized IS '是否生物识别(Y:是，N：否)';
COMMENT ON COLUMN person_face_search_log.result IS '结果(0通过，1未通过)';
COMMENT ON COLUMN person_face_search_log.device_code IS '设备编码';
COMMENT ON COLUMN person_face_search_log.device_name IS '设备名称';
COMMENT ON COLUMN person_face_search_log.device_model IS '设备型号编码';
COMMENT ON COLUMN person_face_search_log.device_ip IS '设备IP';
COMMENT ON COLUMN person_face_search_log.device_longitude IS '设备经度(东经)';
COMMENT ON COLUMN person_face_search_log.device_dimension IS '设备维度(北纬)';
COMMENT ON COLUMN person_face_search_log.device_direction IS '设备方向(IN:进，OUT:出，UNKNOWN：未知)';
COMMENT ON COLUMN person_face_search_log.received_time IS '请求时间';
COMMENT ON COLUMN person_face_search_log.time_used IS '耗时(ms)';
COMMENT ON COLUMN person_face_search_log.server_id IS '服务器标识';
COMMENT ON COLUMN person_face_search_log.vendor_code IS '厂商';
COMMENT ON COLUMN person_face_search_log.algs_version IS '算法版本';
COMMENT ON COLUMN person_face_search_log.create_time IS '创建时间';
COMMENT ON COLUMN person_face_search_log.batch_date IS '定时任务执行时间';
COMMENT ON COLUMN person_face_search_log.tenant_id IS '租户ID';
COMMENT ON TABLE person_face_search_log IS '人员人脸搜索日志表';
create trigger trigger_face_search_log before insert on person_face_search_log for each row execute procedure ins_record_trigger_fun('yyyymmdd'); 

CREATE TABLE person_faceiris_search_log (
  id varchar(48) NOT NULL,
  received_seq varchar(48) NOT NULL,
  healthcode_log_id varchar(48),
  scene_type char(1) NOT NULL,
  unique_id varchar(50),
  dept_id varchar(50),
  channel_code varchar(255),
  sub_treasury_code varchar(255),
  scene_face_image varchar(255),
  scene_iris_image varchar(255),
  scene_face_score float8,
  scene_iris_score float8,
  stock_face_image varchar(255),
  stock_iris_image varchar(255),
  match_mode char(1),
  match_score float8,
  checklive_score float8,
  checklive_result varchar(1),
  result char(1),
  received_time timestamp NOT NULL,
  time_used int8,
  face_time_used int8,
  iris_time_used int8,
  temperature float8,
  temperature_floor float8,
  temperature_top float8,
  device_sn varchar(50),
  device_model varchar(255),
  device_name varchar(255),
  device_ip varchar(255),
  device_longitude float8,
  device_dimension float8,
  device_direction varchar(10),
  server_id varchar(255),
  create_time timestamp,
  batch_date timestamp,
  remark varchar(255),
  tenant_id varchar(255) DEFAULT 'super',
  CONSTRAINT pk_person_faceiris_search_log PRIMARY KEY (id, received_time)
);
COMMENT ON COLUMN person_faceiris_search_log.id IS '主键';
COMMENT ON COLUMN person_faceiris_search_log.received_seq IS '业务流水号';
COMMENT ON COLUMN person_faceiris_search_log.healthcode_log_id IS '健康码日志ID';
COMMENT ON COLUMN person_faceiris_search_log.scene_type IS '类型(数据字典 1：基础信息入库1:N，2：比对搜索接口1:N)';
COMMENT ON COLUMN person_faceiris_search_log.unique_id IS '人员唯一编号';
COMMENT ON COLUMN person_faceiris_search_log.dept_id IS '部门ID';
COMMENT ON COLUMN person_faceiris_search_log.channel_code IS '渠道编码';
COMMENT ON COLUMN person_faceiris_search_log.sub_treasury_code IS '分库编码';
COMMENT ON COLUMN person_faceiris_search_log.scene_face_image IS '人脸现场照路径';
COMMENT ON COLUMN person_faceiris_search_log.scene_iris_image IS '虹膜现场照路径';
COMMENT ON COLUMN person_faceiris_search_log.scene_face_score IS '人脸比对分数';
COMMENT ON COLUMN person_faceiris_search_log.scene_iris_score IS '虹膜比对分数';
COMMENT ON COLUMN person_faceiris_search_log.stock_face_image IS '底库人脸照路径';
COMMENT ON COLUMN person_faceiris_search_log.stock_iris_image IS '底库虹膜照路径';
COMMENT ON COLUMN person_faceiris_search_log.match_mode IS '比对模式字典matchMode';
COMMENT ON COLUMN person_faceiris_search_log.match_score IS '比对分数，比对通过的分数或者融合分数';
COMMENT ON COLUMN person_faceiris_search_log.checklive_score IS '现场照检活分值';
COMMENT ON COLUMN person_faceiris_search_log.checklive_result IS '现场照检活结果(0通过，1未通过)';
COMMENT ON COLUMN person_faceiris_search_log.result IS '结果(0通过，1未通过)';
COMMENT ON COLUMN person_faceiris_search_log.received_time IS '交易时间';
COMMENT ON COLUMN person_faceiris_search_log.time_used IS '比对用时ms';
COMMENT ON COLUMN person_faceiris_search_log.face_time_used IS '人脸比对用时ms';
COMMENT ON COLUMN person_faceiris_search_log.iris_time_used IS '虹膜比对用时ms';
COMMENT ON COLUMN person_faceiris_search_log.temperature IS '温度';
COMMENT ON COLUMN person_faceiris_search_log.temperature_floor IS '温度阈值下限';
COMMENT ON COLUMN person_faceiris_search_log.temperature_top IS '温度阈值上限制';
COMMENT ON COLUMN person_faceiris_search_log.device_sn IS '设备标识';
COMMENT ON COLUMN person_faceiris_search_log.device_model IS '设备型号编码';
COMMENT ON COLUMN person_faceiris_search_log.device_name IS '设备名称';
COMMENT ON COLUMN person_faceiris_search_log.device_ip IS '设备IP';
COMMENT ON COLUMN person_faceiris_search_log.device_longitude IS '设备经度(东经)';
COMMENT ON COLUMN person_faceiris_search_log.device_dimension IS '设备维度(北纬)';
COMMENT ON COLUMN person_faceiris_search_log.device_direction IS '设备方向(IN:进，OUT:出，UNKNOWN：未知)';
COMMENT ON COLUMN person_faceiris_search_log.server_id IS '服务器标识';
COMMENT ON COLUMN person_faceiris_search_log.create_time IS '创建时间';
COMMENT ON COLUMN person_faceiris_search_log.batch_date IS '定时任务执行时间';
COMMENT ON COLUMN person_faceiris_search_log.remark IS '备注';
COMMENT ON COLUMN person_faceiris_search_log.tenant_id IS '租户ID';
COMMENT ON TABLE person_faceiris_search_log IS '人脸虹膜多模态搜索日志表';
create trigger trigger_faceiris_search_log before insert on person_faceiris_search_log for each row execute procedure ins_record_trigger_fun('yyyymmdd'); 

CREATE TABLE person_finger_match_log (
  id varchar(48) NOT NULL,
  received_seq varchar(48) NOT NULL,
  unique_id varchar(48),
  dept_id int8,
  channel_code varchar(255),
  finger_no varchar(2),
  scene_image varchar(255) NOT NULL,
  stock_image varchar(255),
  scene_stock_score float8,
  scene_stock_result varchar(1),
  result varchar(1),
  received_time timestamp NOT NULL,
  time_used int8,
  server_id varchar(255),
  vendor_code varchar(255),
  algs_version varchar(255),
  create_time timestamp,
  batch_date timestamp,
  tenant_id varchar(255) DEFAULT 'super',
  CONSTRAINT pk_person_finger_match_log PRIMARY KEY (id, received_time)
);
COMMENT ON COLUMN person_finger_match_log.id IS '主键';
COMMENT ON COLUMN person_finger_match_log.received_seq IS '业务流水号';
COMMENT ON COLUMN person_finger_match_log.unique_id IS '人员标识';
COMMENT ON COLUMN person_finger_match_log.dept_id IS '部门ID';
COMMENT ON COLUMN person_finger_match_log.channel_code IS '渠道编码';
COMMENT ON COLUMN person_finger_match_log.finger_no IS '手指编码';
COMMENT ON COLUMN person_finger_match_log.scene_image IS '现场照路径';
COMMENT ON COLUMN person_finger_match_log.stock_image IS '底库照路径';
COMMENT ON COLUMN person_finger_match_log.scene_stock_score IS '现场照与库底库照比对分值';
COMMENT ON COLUMN person_finger_match_log.scene_stock_result IS '现场照与库底库照比对结果(0通过，1未通过)';
COMMENT ON COLUMN person_finger_match_log.result IS '比对结果(0通过，1未通过)';
COMMENT ON COLUMN person_finger_match_log.received_time IS '请求时间';
COMMENT ON COLUMN person_finger_match_log.time_used IS '耗时(ms)';
COMMENT ON COLUMN person_finger_match_log.server_id IS '服务器标识';
COMMENT ON COLUMN person_finger_match_log.vendor_code IS '厂商';
COMMENT ON COLUMN person_finger_match_log.algs_version IS '算法版本';
COMMENT ON COLUMN person_finger_match_log.create_time IS '创建时间';
COMMENT ON COLUMN person_finger_match_log.batch_date IS '定时任务执行时间';
COMMENT ON COLUMN person_finger_match_log.tenant_id IS '租户ID';
COMMENT ON TABLE person_finger_match_log IS '人员指纹比对日志表';
create trigger trigger_finger_match_log before insert on person_finger_match_log for each row execute procedure ins_record_trigger_fun('yyyymmdd'); 

CREATE TABLE person_finger_search_log (
  id varchar(48) NOT NULL,
  received_seq varchar(48) NOT NULL,
  scene_type varchar(1) NOT NULL,
  unique_id varchar(48),
  dept_id int8,
  channel_code varchar(255),
  sub_treasury_code varchar(255),
  sub_treasury_name varchar(255),
  finger_no varchar(2),
  scene_image varchar(255) NOT NULL,
  stock_image varchar(255),
  scene_stock_score float8,
  result varchar(1),
  received_time timestamp NOT NULL,
  time_used int8,
  server_id varchar(255),
  vendor_code varchar(255),
  algs_version varchar(255),
  create_time timestamp,
  batch_date timestamp,
  tenant_id varchar(255) DEFAULT 'super',
  CONSTRAINT pk_person_finger_search_log PRIMARY KEY (id, received_time)
);
COMMENT ON COLUMN person_finger_search_log.id IS '主键';
COMMENT ON COLUMN person_finger_search_log.received_seq IS '业务流水号';
COMMENT ON COLUMN person_finger_search_log.scene_type IS '类型(数据字典 1：基础入库1:N，2：比对接口1:N，3：日志回传)';
COMMENT ON COLUMN person_finger_search_log.unique_id IS '人员标识';
COMMENT ON COLUMN person_finger_search_log.dept_id IS '部门ID';
COMMENT ON COLUMN person_finger_search_log.channel_code IS '渠道编码';
COMMENT ON COLUMN person_finger_search_log.sub_treasury_code IS '分库编码';
COMMENT ON COLUMN person_finger_search_log.sub_treasury_name IS '分库名称';
COMMENT ON COLUMN person_finger_search_log.finger_no IS '手指编码';
COMMENT ON COLUMN person_finger_search_log.scene_image IS '现场照路径';
COMMENT ON COLUMN person_finger_search_log.stock_image IS '底库照路径';
COMMENT ON COLUMN person_finger_search_log.scene_stock_score IS '分值';
COMMENT ON COLUMN person_finger_search_log.result IS '结果(0通过，1未通过)';
COMMENT ON COLUMN person_finger_search_log.received_time IS '请求时间';
COMMENT ON COLUMN person_finger_search_log.time_used IS '耗时(ms)';
COMMENT ON COLUMN person_finger_search_log.server_id IS '服务器标识';
COMMENT ON COLUMN person_finger_search_log.vendor_code IS '厂商';
COMMENT ON COLUMN person_finger_search_log.algs_version IS '算法版本';
COMMENT ON COLUMN person_finger_search_log.create_time IS '创建时间';
COMMENT ON COLUMN person_finger_search_log.batch_date IS '定时任务执行时间';
COMMENT ON COLUMN person_finger_search_log.tenant_id IS '租户ID';
COMMENT ON TABLE person_finger_search_log IS '人员指纹搜索日志表';
create trigger trigger_finger_search_log before insert on person_finger_search_log for each row execute procedure ins_record_trigger_fun('yyyymmdd'); 

CREATE TABLE person_health_code_log (
  id varchar(50) NOT NULL,
  received_seq varchar(50) NOT NULL,
  unique_id varchar(50),
  received_time timestamp NOT NULL,
  result char(1),
  message varchar(255),
  device_code varchar(50),
  temperature varchar(10),
  car_no varchar(10),
  region varchar(50),
  time_used int8,
  create_time timestamp NOT NULL,
  batch_date timestamp,
  tenant_id varchar(255) DEFAULT 'super',
  CONSTRAINT pk_person_health_code_log PRIMARY KEY (id, received_time)
);
COMMENT ON COLUMN person_health_code_log.id IS '主键';
COMMENT ON COLUMN person_health_code_log.received_seq IS '业务流水号';
COMMENT ON COLUMN person_health_code_log.unique_id IS '人员唯一标识，对应person_cert';
COMMENT ON COLUMN person_health_code_log.received_time IS '请求时间';
COMMENT ON COLUMN person_health_code_log.result IS '结果(0通过，1未通过)';
COMMENT ON COLUMN person_health_code_log.message IS '结果描述';
COMMENT ON COLUMN person_health_code_log.device_code IS '设备编号';
COMMENT ON COLUMN person_health_code_log.temperature IS '人脸测温温度';
COMMENT ON COLUMN person_health_code_log.car_no IS '车牌号';
COMMENT ON COLUMN person_health_code_log.region IS '区域（调用哪个健康码接口）';
COMMENT ON COLUMN person_health_code_log.time_used IS '用时ms';
COMMENT ON COLUMN person_health_code_log.create_time IS '创建时间';
COMMENT ON COLUMN person_health_code_log.batch_date IS '定时任务执行时间';
COMMENT ON COLUMN person_health_code_log.tenant_id IS '租户ID';
COMMENT ON TABLE person_health_code_log IS '健康码请求记录';
create trigger trigger_health_code_log before insert on person_health_code_log for each row execute procedure ins_record_trigger_fun('yyyymmdd'); 

CREATE TABLE person_iris_match_log (
  id varchar(48) NOT NULL,
  received_seq varchar(48) NOT NULL,
  unique_id varchar(48),
  dept_id int8,
  channel_code varchar(255),
  scene_image varchar(255),
  stock_image varchar(255),
  scene_stock_score float8,
  scene_stock_result varchar(1),
  result varchar(1),
  received_time timestamp NOT NULL,
  time_used int8,
  server_id varchar(255),
  vendor_code varchar(255),
  algs_version varchar(255),
  create_time timestamp,
  batch_date timestamp,
  tenant_id varchar(255) DEFAULT 'super',
  CONSTRAINT pk_person_iris_match_log PRIMARY KEY (id, received_time)
);
COMMENT ON COLUMN person_iris_match_log.id IS '主键';
COMMENT ON COLUMN person_iris_match_log.received_seq IS '业务流水号';
COMMENT ON COLUMN person_iris_match_log.unique_id IS '人员标识';
COMMENT ON COLUMN person_iris_match_log.dept_id IS '部门ID';
COMMENT ON COLUMN person_iris_match_log.channel_code IS '渠道编码';
COMMENT ON COLUMN person_iris_match_log.scene_image IS '现场照路径';
COMMENT ON COLUMN person_iris_match_log.stock_image IS '底库照路径';
COMMENT ON COLUMN person_iris_match_log.scene_stock_score IS '现场照与底库照比对分值';
COMMENT ON COLUMN person_iris_match_log.scene_stock_result IS '现场照与底库照比对结果(0通过，1未通过)';
COMMENT ON COLUMN person_iris_match_log.result IS '比对结果(0通过，1未通过)';
COMMENT ON COLUMN person_iris_match_log.received_time IS '请求时间';
COMMENT ON COLUMN person_iris_match_log.time_used IS '耗时(ms)';
COMMENT ON COLUMN person_iris_match_log.server_id IS '服务器标识';
COMMENT ON COLUMN person_iris_match_log.vendor_code IS '厂商';
COMMENT ON COLUMN person_iris_match_log.algs_version IS '算法版本';
COMMENT ON COLUMN person_iris_match_log.create_time IS '创建时间';
COMMENT ON COLUMN person_iris_match_log.batch_date IS '定时任务执行时间';
COMMENT ON COLUMN person_iris_match_log.tenant_id IS '租户ID';
COMMENT ON TABLE person_iris_match_log IS '人员虹膜比对日志表';
create trigger trigger_iris_match_log before insert on person_iris_match_log for each row execute procedure ins_record_trigger_fun('yyyymmdd'); 

CREATE TABLE person_iris_search_log (
  id varchar(48) NOT NULL,
  received_seq varchar(48) NOT NULL,
  scene_type varchar(1) NOT NULL,
  unique_id varchar(48),
  dept_id int8,
  channel_code varchar(255),
  sub_treasury_code varchar(255),
  sub_treasury_name varchar(255),
  scene_image varchar(255),
  stock_image varchar(255),
  scene_stock_score float8,
  result varchar(1),
  received_time timestamp NOT NULL,
  time_used int8,
  server_id varchar(255),
  vendor_code varchar(255),
  algs_version varchar(255),
  create_time timestamp,
  batch_date timestamp,
  tenant_id varchar(255) DEFAULT 'super',
  CONSTRAINT pk_person_iris_search_log PRIMARY KEY (id, received_time)
);
COMMENT ON COLUMN person_iris_search_log.id IS '主键';
COMMENT ON COLUMN person_iris_search_log.received_seq IS '业务流水号';
COMMENT ON COLUMN person_iris_search_log.scene_type IS '类型(数据字典 1：基础信息入库1:N，2：比对搜索接口1:N)';
COMMENT ON COLUMN person_iris_search_log.unique_id IS '人员标识';
COMMENT ON COLUMN person_iris_search_log.dept_id IS '部门ID';
COMMENT ON COLUMN person_iris_search_log.channel_code IS '渠道编码';
COMMENT ON COLUMN person_iris_search_log.sub_treasury_code IS '分库编码';
COMMENT ON COLUMN person_iris_search_log.sub_treasury_name IS '分库名称';
COMMENT ON COLUMN person_iris_search_log.scene_image IS '现场照路径';
COMMENT ON COLUMN person_iris_search_log.stock_image IS '底库照路径';
COMMENT ON COLUMN person_iris_search_log.scene_stock_score IS '现场照与底库照比对分值';
COMMENT ON COLUMN person_iris_search_log.result IS '结果(0通过，1未通过)';
COMMENT ON COLUMN person_iris_search_log.received_time IS '请求时间';
COMMENT ON COLUMN person_iris_search_log.time_used IS '耗时(ms)';
COMMENT ON COLUMN person_iris_search_log.server_id IS '服务器标识';
COMMENT ON COLUMN person_iris_search_log.vendor_code IS '厂商';
COMMENT ON COLUMN person_iris_search_log.algs_version IS '算法版本';
COMMENT ON COLUMN person_iris_search_log.create_time IS '创建时间';
COMMENT ON COLUMN person_iris_search_log.batch_date IS '定时任务执行时间';
COMMENT ON COLUMN person_iris_search_log.tenant_id IS '租户ID';
COMMENT ON TABLE person_iris_search_log IS '人员虹膜搜索日志表';
create trigger trigger_iris_search_log before insert on person_iris_search_log for each row execute procedure ins_record_trigger_fun('yyyymmdd'); 

CREATE TABLE trade_req_record (
  id varchar(48) NOT NULL,
  trans_code varchar(48),
  trans_title varchar(48),
  received_time timestamp NOT NULL,
  client_ip varchar(48),
  send_time timestamp NOT NULL,
  time_used int8 NOT NULL,
  status_code varchar(48) NOT NULL DEFAULT '0',
  trans_url varchar(255),
  class_method varchar(255),
  channel_code varchar(255),
  tenant_id varchar(255) DEFAULT 'super',
  detail_file_path varchar(255),
  CONSTRAINT pk_trade_req_record PRIMARY KEY (id, received_time)
);
COMMENT ON COLUMN trade_req_record.id IS '主键';
COMMENT ON COLUMN trade_req_record.trans_code IS '交易码';
COMMENT ON COLUMN trade_req_record.trans_title IS '交易标题';
COMMENT ON COLUMN trade_req_record.received_time IS '请求时间';
COMMENT ON COLUMN trade_req_record.client_ip IS '客户端IP';
COMMENT ON COLUMN trade_req_record.send_time IS '响应时间';
COMMENT ON COLUMN trade_req_record.time_used IS '耗时(ms)';
COMMENT ON COLUMN trade_req_record.status_code IS '状态码';
COMMENT ON COLUMN trade_req_record.trans_url IS '交易请求路径';
COMMENT ON COLUMN trade_req_record.class_method IS '处理方法';
COMMENT ON COLUMN trade_req_record.channel_code IS '渠道编码';
COMMENT ON COLUMN trade_req_record.tenant_id IS '租户ID';
COMMENT ON COLUMN trade_req_record.detail_file_path IS '报文详细文件';
COMMENT ON TABLE trade_req_record IS '接口交易请求记录';
create trigger trigger_trade_req_record before insert on trade_req_record for each row execute procedure ins_record_trigger_fun('yyyymmdd'); 


CREATE TABLE sys_user_face (
  id varchar(48) NOT NULL,
  user_id int8 NOT NULL,
  feature varchar(4000) NOT NULL,
  feature_md5 varchar(255),
  quality_score float8,
  image_url varchar(255) NOT NULL,
  vendor_code varchar(48),
  algs_version varchar(48),
  status char(1) NOT NULL DEFAULT '0',
  remark varchar(255),
  create_by varchar(64),
  update_by varchar(64),
  create_time timestamp,
  update_time timestamp,
  tenant_id varchar(255) DEFAULT 'super',
  CONSTRAINT pk_sys_user_face PRIMARY KEY (id)
);
COMMENT ON COLUMN sys_user_face.id IS '主键';
COMMENT ON COLUMN sys_user_face.user_id IS '用户主键(关联用户表)';
COMMENT ON COLUMN sys_user_face.feature IS '人脸特征';
COMMENT ON COLUMN sys_user_face.feature_md5 IS '人脸特征MD5';
COMMENT ON COLUMN sys_user_face.quality_score IS '图像质量得分';
COMMENT ON COLUMN sys_user_face.image_url IS '人脸图像路径';
COMMENT ON COLUMN sys_user_face.vendor_code IS '厂商';
COMMENT ON COLUMN sys_user_face.algs_version IS '算法版本';
COMMENT ON COLUMN sys_user_face.status IS '状态：0-有效  1:无效';
COMMENT ON COLUMN sys_user_face.remark IS '备注';
COMMENT ON COLUMN sys_user_face.create_by IS '创建人';
COMMENT ON COLUMN sys_user_face.update_by IS '修改人';
COMMENT ON COLUMN sys_user_face.create_time IS '创建时间';
COMMENT ON COLUMN sys_user_face.update_time IS '修改时间';
COMMENT ON COLUMN sys_user_face.tenant_id IS '租户ID';
COMMENT ON TABLE sys_user_face IS '用户人脸信息';

CREATE TABLE sys_user_finger (
  id varchar(48) NOT NULL,
  user_id int8 NOT NULL,
  finger_no varchar(2) NOT NULL,
  feature varchar(4000) NOT NULL,
  feature_md5 varchar(255),
  quality_score float8,
  image_url varchar(255),
  vendor_code varchar(48),
  algs_version varchar(48),
  status char(1) NOT NULL DEFAULT '0',
  remark varchar(255),
  create_by varchar(64),
  update_by varchar(64),
  create_time timestamp,
  update_time timestamp,
  tenant_id varchar(255) DEFAULT 'super',
  CONSTRAINT pk_sys_user_finger PRIMARY KEY (id)
);
COMMENT ON COLUMN sys_user_finger.id IS '主键';
COMMENT ON COLUMN sys_user_finger.user_id IS '用户主键(关联人员表)';
COMMENT ON COLUMN sys_user_finger.finger_no IS '手指编号';
COMMENT ON COLUMN sys_user_finger.feature IS '指纹特征';
COMMENT ON COLUMN sys_user_finger.feature_md5 IS '指纹特征MD5';
COMMENT ON COLUMN sys_user_finger.quality_score IS '图像质量得分';
COMMENT ON COLUMN sys_user_finger.image_url IS '指纹图像路径';
COMMENT ON COLUMN sys_user_finger.vendor_code IS '厂商';
COMMENT ON COLUMN sys_user_finger.algs_version IS '算法版本';
COMMENT ON COLUMN sys_user_finger.status IS '状态：0-有效  1:无效';
COMMENT ON COLUMN sys_user_finger.remark IS '备注';
COMMENT ON COLUMN sys_user_finger.create_by IS '创建人';
COMMENT ON COLUMN sys_user_finger.update_by IS '修改人';
COMMENT ON COLUMN sys_user_finger.create_time IS '创建时间';
COMMENT ON COLUMN sys_user_finger.update_time IS '修改时间';
COMMENT ON COLUMN sys_user_finger.tenant_id IS '租户ID';
COMMENT ON TABLE sys_user_finger IS '用户指纹信息';

CREATE TABLE sys_user_iris (
  id varchar(48) NOT NULL,
  user_id int8 NOT NULL,
  feature text NOT NULL,
  feature_md5 varchar(255),
  quality_score float8,
  image_url varchar(255) NOT NULL,
  vendor_code varchar(48),
  algs_version varchar(48),
  status char(1) NOT NULL DEFAULT '0',
  remark varchar(255),
  create_by varchar(64),
  update_by varchar(64),
  create_time timestamp,
  update_time timestamp,
  tenant_id varchar(255) DEFAULT 'super',
  CONSTRAINT pk_sys_user_iris PRIMARY KEY (id)
);
COMMENT ON COLUMN sys_user_iris.id IS '主键';
COMMENT ON COLUMN sys_user_iris.user_id IS '用户主键(关联用户信息表)';
COMMENT ON COLUMN sys_user_iris.feature IS '虹膜特征';
COMMENT ON COLUMN sys_user_iris.feature_md5 IS '虹膜特征MD5';
COMMENT ON COLUMN sys_user_iris.quality_score IS '图像质量得分';
COMMENT ON COLUMN sys_user_iris.image_url IS '虹膜图像路径';
COMMENT ON COLUMN sys_user_iris.vendor_code IS '厂商';
COMMENT ON COLUMN sys_user_iris.algs_version IS '算法版本';
COMMENT ON COLUMN sys_user_iris.status IS '状态：0-有效  1:无效';
COMMENT ON COLUMN sys_user_iris.remark IS '备注';
COMMENT ON COLUMN sys_user_iris.create_by IS '创建人';
COMMENT ON COLUMN sys_user_iris.update_by IS '修改人';
COMMENT ON COLUMN sys_user_iris.create_time IS '创建时间';
COMMENT ON COLUMN sys_user_iris.update_time IS '修改时间';
COMMENT ON COLUMN sys_user_iris.tenant_id IS '租户ID';
COMMENT ON TABLE sys_user_iris IS '用户虹膜信息';

CREATE TABLE sys_user_iris_face (
  id varchar(48) NOT NULL,
  user_id int8 NOT NULL,
  fusion_feature text,
  face_feature text,
  iris_feature text,
  fusion_feature_md5 varchar(255),
  face_feature_md5 varchar(255),
  face_image_url varchar(255),
  face_quality float8,
  iris_feature_md5 varchar(255),
  iris_image_url varchar(255),
  iris_quality float8,
  status char(1) NOT NULL DEFAULT '0',
  create_by varchar(48),
  update_by varchar(48),
  create_time timestamp,
  update_time timestamp,
  tenant_id varchar(255) DEFAULT 'super',
  CONSTRAINT pk_sys_user_iris_face PRIMARY KEY (id)
);
COMMENT ON COLUMN sys_user_iris_face.id IS '主键';
COMMENT ON COLUMN sys_user_iris_face.user_id IS '用户主键';
COMMENT ON COLUMN sys_user_iris_face.fusion_feature IS '融合特征';
COMMENT ON COLUMN sys_user_iris_face.face_feature IS '人脸特征';
COMMENT ON COLUMN sys_user_iris_face.iris_feature IS '虹膜特征';
COMMENT ON COLUMN sys_user_iris_face.fusion_feature_md5 IS '融合特征md5';
COMMENT ON COLUMN sys_user_iris_face.face_feature_md5 IS '人脸特征md5';
COMMENT ON COLUMN sys_user_iris_face.face_image_url IS '人脸照片路径';
COMMENT ON COLUMN sys_user_iris_face.face_quality IS '人脸质量分数';
COMMENT ON COLUMN sys_user_iris_face.iris_feature_md5 IS '虹膜特征md5';
COMMENT ON COLUMN sys_user_iris_face.iris_image_url IS '虹膜照片路径';
COMMENT ON COLUMN sys_user_iris_face.iris_quality IS '虹膜质量分数';
COMMENT ON COLUMN sys_user_iris_face.status IS '状态：0-有效  1-无效';
COMMENT ON COLUMN sys_user_iris_face.create_by IS '创建人';
COMMENT ON COLUMN sys_user_iris_face.update_by IS '修改人';
COMMENT ON COLUMN sys_user_iris_face.create_time IS '创建时间';
COMMENT ON COLUMN sys_user_iris_face.update_time IS '修改时间';
COMMENT ON COLUMN sys_user_iris_face.tenant_id IS '租户ID';
COMMENT ON TABLE sys_user_iris_face IS '用户虹膜人脸多模态信息';


CREATE TABLE ocr_bank_card_log (
  id varchar(48) NOT NULL,
  received_seq varchar(48) NOT NULL,
  received_time timestamp NOT NULL,
  type varchar(12),
  type_id varchar(12),
  channel_code varchar(255),
  scene_image_url varchar(255) NOT NULL,
  scene_image_name varchar(255),
  bank_card_expiry_date varchar(20),
  bank_card_bank_name varchar(255),
  bank_card_bank_code varchar(255),
  bank_card_name varchar(20),
  bank_card_type varchar(20),
  bank_card_number varchar(50),
  result char(1),
  tenant_id varchar(255) DEFAULT 'super',
  CONSTRAINT pk_ocr_bank_card_log PRIMARY KEY (id)
);
COMMENT ON COLUMN ocr_bank_card_log.id IS '主键';
COMMENT ON COLUMN ocr_bank_card_log.received_seq IS '业务流水号';
COMMENT ON COLUMN ocr_bank_card_log.received_time IS '请求时间';
COMMENT ON COLUMN ocr_bank_card_log.type IS '图片类型，如果是图睿算法库，此项填Unknown';
COMMENT ON COLUMN ocr_bank_card_log.type_id IS '卡证类型，如果是文通算法库，此项填0 ';
COMMENT ON COLUMN ocr_bank_card_log.channel_code IS '渠道编码';
COMMENT ON COLUMN ocr_bank_card_log.scene_image_url IS '待识别证件照存储路径';
COMMENT ON COLUMN ocr_bank_card_log.scene_image_name IS '待识别证件照名称';
COMMENT ON COLUMN ocr_bank_card_log.bank_card_expiry_date IS '有效期';
COMMENT ON COLUMN ocr_bank_card_log.bank_card_bank_name IS '银行名称';
COMMENT ON COLUMN ocr_bank_card_log.bank_card_bank_code IS '银行编号';
COMMENT ON COLUMN ocr_bank_card_log.bank_card_name IS '卡名';
COMMENT ON COLUMN ocr_bank_card_log.bank_card_type IS '卡类型';
COMMENT ON COLUMN ocr_bank_card_log.bank_card_number IS '卡号';
COMMENT ON COLUMN ocr_bank_card_log.result IS '识别结果';
COMMENT ON COLUMN ocr_bank_card_log.tenant_id IS '租户id';
COMMENT ON TABLE ocr_bank_card_log IS '银行卡OCR识别记录';

CREATE TABLE ocr_busi_lic_log (
  id varchar(48) NOT NULL,
  received_seq varchar(48) NOT NULL,
  received_time timestamp NOT NULL,
  type varchar(12),
  type_id varchar(12),
  channel_code varchar(255),
  scene_image_url varchar(255) NOT NULL,
  scene_image_name varchar(255),
  busi_lic_registered_no varchar(255),
  busi_lic_origination_no varchar(255),
  busi_lic_tax_no varchar(255),
  busi_lic_social_insurance_no varchar(255),
  busi_lic_statistic_no varchar(255),
  busi_lic_name varchar(255),
  busi_lic_type varchar(255),
  busi_lic_address varchar(255),
  busi_lic_owner varchar(20),
  busi_lic_form varchar(255),
  busi_lic_registered_capital varchar(20),
  busi_lic_registry_date varchar(20),
  busi_lic_expiry_date varchar(20),
  busi_lic_scope varchar(255),
  busi_lic_issure_authority varchar(255),
  busi_lic_issure_date varchar(20),
  busi_lic_qr_code varchar(255),
  result char(1),
  tenant_id varchar(255) DEFAULT 'super',
  CONSTRAINT pk_ocr_busi_lic_log PRIMARY KEY (id)
);
COMMENT ON COLUMN ocr_busi_lic_log.id IS '主键';
COMMENT ON COLUMN ocr_busi_lic_log.received_seq IS '业务流水号';
COMMENT ON COLUMN ocr_busi_lic_log.received_time IS '请求时间';
COMMENT ON COLUMN ocr_busi_lic_log.type IS '图片类型，如果是图睿算法库，此项填Unknown';
COMMENT ON COLUMN ocr_busi_lic_log.type_id IS '卡证类型，如果是文通算法库，此项填0 ';
COMMENT ON COLUMN ocr_busi_lic_log.channel_code IS '渠道编码';
COMMENT ON COLUMN ocr_busi_lic_log.scene_image_url IS '待识别证件照存储路径';
COMMENT ON COLUMN ocr_busi_lic_log.scene_image_name IS '待识别证件照名称';
COMMENT ON COLUMN ocr_busi_lic_log.busi_lic_registered_no IS '注册号';
COMMENT ON COLUMN ocr_busi_lic_log.busi_lic_origination_no IS '营业执照签发号';
COMMENT ON COLUMN ocr_busi_lic_log.busi_lic_tax_no IS '营业执照税号';
COMMENT ON COLUMN ocr_busi_lic_log.busi_lic_social_insurance_no IS '营业执照社会保险证号';
COMMENT ON COLUMN ocr_busi_lic_log.busi_lic_statistic_no IS '营业执照统计号';
COMMENT ON COLUMN ocr_busi_lic_log.busi_lic_name IS '公司名称';
COMMENT ON COLUMN ocr_busi_lic_log.busi_lic_type IS '公司类型';
COMMENT ON COLUMN ocr_busi_lic_log.busi_lic_address IS '公司地址';
COMMENT ON COLUMN ocr_busi_lic_log.busi_lic_owner IS '法定代表人';
COMMENT ON COLUMN ocr_busi_lic_log.busi_lic_form IS '组成形式';
COMMENT ON COLUMN ocr_busi_lic_log.busi_lic_registered_capital IS '注册资金';
COMMENT ON COLUMN ocr_busi_lic_log.busi_lic_registry_date IS '注册日期';
COMMENT ON COLUMN ocr_busi_lic_log.busi_lic_expiry_date IS '过期日期';
COMMENT ON COLUMN ocr_busi_lic_log.busi_lic_scope IS '经营范围';
COMMENT ON COLUMN ocr_busi_lic_log.busi_lic_issure_authority IS '签发机关';
COMMENT ON COLUMN ocr_busi_lic_log.busi_lic_issure_date IS '签发日期';
COMMENT ON COLUMN ocr_busi_lic_log.busi_lic_qr_code IS '营业执照二维码';
COMMENT ON COLUMN ocr_busi_lic_log.result IS '识别结果';
COMMENT ON TABLE ocr_busi_lic_log IS '营业执照OCR识别记录';

CREATE TABLE ocr_driver_lic_log (
  id varchar(48) NOT NULL,
  received_seq varchar(48) NOT NULL,
  received_time timestamp NOT NULL,
  type varchar(12),
  type_id varchar(12),
  ocr_firm_type varchar(12),
  channel_code varchar(255),
  scene_image_url varchar(255) NOT NULL,
  scene_image_name varchar(255),
  driver_lic_number varchar(50),
  driver_lic_head_image varchar(255),
  driver_lic_address varchar(255),
  driver_lic_birth varchar(255),
  driver_lic_gender varchar(20),
  driver_lic_name varchar(20),
  driver_lic_driver_type varchar(20),
  driver_lic_first_issue varchar(20),
  driver_lic_valid_from varchar(255),
  driver_lic_valid_for varchar(255),
  driver_lic_expiry_date varchar(20),
  result char(1),
  tenant_id varchar(255) DEFAULT 'super',
  CONSTRAINT pk_ocr_driver_lic_log PRIMARY KEY (id)
);
COMMENT ON COLUMN ocr_driver_lic_log.id IS '主键';
COMMENT ON COLUMN ocr_driver_lic_log.received_seq IS '业务流水号';
COMMENT ON COLUMN ocr_driver_lic_log.received_time IS '请求时间';
COMMENT ON COLUMN ocr_driver_lic_log.type IS '图片类型，如果是图睿算法库，此项填Unknown';
COMMENT ON COLUMN ocr_driver_lic_log.type_id IS '卡证类型，如果是文通算法库，此项填0';
COMMENT ON COLUMN ocr_driver_lic_log.ocr_firm_type IS '算法类型';
COMMENT ON COLUMN ocr_driver_lic_log.channel_code IS '渠道编码';
COMMENT ON COLUMN ocr_driver_lic_log.scene_image_url IS '待识别证件照存储路径';
COMMENT ON COLUMN ocr_driver_lic_log.scene_image_name IS '待识别证件照名称';
COMMENT ON COLUMN ocr_driver_lic_log.driver_lic_number IS '驾驶证件号';
COMMENT ON COLUMN ocr_driver_lic_log.driver_lic_head_image IS '证件头像';
COMMENT ON COLUMN ocr_driver_lic_log.driver_lic_address IS '驾驶证居住地址';
COMMENT ON COLUMN ocr_driver_lic_log.driver_lic_birth IS '驾驶证出生日期';
COMMENT ON COLUMN ocr_driver_lic_log.driver_lic_gender IS '驾驶证性别';
COMMENT ON COLUMN ocr_driver_lic_log.driver_lic_name IS '驾驶证所有人姓名';
COMMENT ON COLUMN ocr_driver_lic_log.driver_lic_driver_type IS '驾驶证准驾车型';
COMMENT ON COLUMN ocr_driver_lic_log.driver_lic_first_issue IS '驾驶证初次领证日期';
COMMENT ON COLUMN ocr_driver_lic_log.driver_lic_valid_from IS '有效期开始时间';
COMMENT ON COLUMN ocr_driver_lic_log.driver_lic_valid_for IS '有效期区间';
COMMENT ON COLUMN ocr_driver_lic_log.driver_lic_expiry_date IS '失效日期';
COMMENT ON COLUMN ocr_driver_lic_log.result IS '识别结果';
COMMENT ON TABLE ocr_driver_lic_log IS '驾驶证OCR识别记录';

CREATE TABLE ocr_driving_lic_log (
  id varchar(48) NOT NULL,
  received_seq varchar(48) NOT NULL,
  received_time timestamp NOT NULL,
  type varchar(12),
  type_id varchar(12),
  channel_code varchar(255),
  scene_image_url varchar(255) NOT NULL,
  scene_image_name varchar(255),
  driving_lic_model varchar(20),
  driving_lic_address varchar(255),
  driving_lic_car_number varchar(255),
  driving_lic_vehicle_type varchar(255),
  driving_lic_issue_date varchar(20),
  driving_lic_engine_no varchar(255),
  driving_lic_vin varchar(255),
  driving_lic_register_date varchar(20),
  driving_lic_use_type varchar(255),
  driving_lic_owner varchar(20),
  result char(1),
  tenant_id varchar(255) DEFAULT 'super',
  CONSTRAINT pk_ocr_driving_lic_log PRIMARY KEY (id)
);
COMMENT ON COLUMN ocr_driving_lic_log.id IS '主键';
COMMENT ON COLUMN ocr_driving_lic_log.received_seq IS '业务流水号';
COMMENT ON COLUMN ocr_driving_lic_log.received_time IS '请求时间';
COMMENT ON COLUMN ocr_driving_lic_log.type IS '图片类型，如果是图睿算法库，此项填Unknown';
COMMENT ON COLUMN ocr_driving_lic_log.type_id IS '卡证类型，如果是文通算法库，此项填0 ';
COMMENT ON COLUMN ocr_driving_lic_log.channel_code IS '渠道编码';
COMMENT ON COLUMN ocr_driving_lic_log.scene_image_url IS '待识别证件照存储路径';
COMMENT ON COLUMN ocr_driving_lic_log.scene_image_name IS '待识别证件照名称';
COMMENT ON COLUMN ocr_driving_lic_log.driving_lic_model IS '品牌型号';
COMMENT ON COLUMN ocr_driving_lic_log.driving_lic_address IS '住址';
COMMENT ON COLUMN ocr_driving_lic_log.driving_lic_car_number IS '车牌号';
COMMENT ON COLUMN ocr_driving_lic_log.driving_lic_vehicle_type IS '车辆类型';
COMMENT ON COLUMN ocr_driving_lic_log.driving_lic_issue_date IS '发行日期';
COMMENT ON COLUMN ocr_driving_lic_log.driving_lic_engine_no IS '发动机号码';
COMMENT ON COLUMN ocr_driving_lic_log.driving_lic_vin IS '车辆识别号码';
COMMENT ON COLUMN ocr_driving_lic_log.driving_lic_register_date IS '注册日期';
COMMENT ON COLUMN ocr_driving_lic_log.driving_lic_use_type IS '用途';
COMMENT ON COLUMN ocr_driving_lic_log.driving_lic_owner IS '所有人';
COMMENT ON COLUMN ocr_driving_lic_log.result IS '识别结果';
COMMENT ON TABLE ocr_driving_lic_log IS '行驶证OCR识别记录';

CREATE TABLE ocr_hk_mac_pass_log (
  id varchar(48) NOT NULL,
  received_seq varchar(48) NOT NULL,
  received_time timestamp NOT NULL,
  type varchar(12),
  type_id varchar(12),
  channel_code varchar(255),
  scene_image_url varchar(255) NOT NULL,
  scene_image_name varchar(255),
  hk_mac_pass_type varchar(20),
  hk_mac_pass_mrz_number varchar(255),
  hk_mac_pass_national_name varchar(255),
  hk_mac_pass_english_name varchar(255),
  hk_mac_pass_gender varchar(20),
  hk_mac_pass_birth varchar(20),
  hk_mac_pass_expiry_date varchar(20),
  hk_mac_pass_issue_country varchar(20),
  hk_mac_pass_english_sur_name varchar(20),
  result char(1),
  tenant_id varchar(255) DEFAULT 'super',
  CONSTRAINT pk_ocr_hk_mac_pass_log PRIMARY KEY (id)
);
COMMENT ON COLUMN ocr_hk_mac_pass_log.id IS '主键';
COMMENT ON COLUMN ocr_hk_mac_pass_log.received_seq IS '业务流水号';
COMMENT ON COLUMN ocr_hk_mac_pass_log.received_time IS '请求时间';
COMMENT ON COLUMN ocr_hk_mac_pass_log.type IS '图片类型，如果是图睿算法库，此项填Unknown';
COMMENT ON COLUMN ocr_hk_mac_pass_log.type_id IS '卡证类型，如果是文通算法库，此项填0 ';
COMMENT ON COLUMN ocr_hk_mac_pass_log.channel_code IS '渠道编码';
COMMENT ON COLUMN ocr_hk_mac_pass_log.scene_image_url IS '待识别证件照存储路径';
COMMENT ON COLUMN ocr_hk_mac_pass_log.scene_image_name IS '待识别证件照名称';
COMMENT ON COLUMN ocr_hk_mac_pass_log.hk_mac_pass_type IS '护照类型';
COMMENT ON COLUMN ocr_hk_mac_pass_log.hk_mac_pass_mrz_number IS '护照号码mrz';
COMMENT ON COLUMN ocr_hk_mac_pass_log.hk_mac_pass_national_name IS '本国姓名';
COMMENT ON COLUMN ocr_hk_mac_pass_log.hk_mac_pass_english_name IS '英文姓名';
COMMENT ON COLUMN ocr_hk_mac_pass_log.hk_mac_pass_gender IS '性别';
COMMENT ON COLUMN ocr_hk_mac_pass_log.hk_mac_pass_birth IS '出生日期';
COMMENT ON COLUMN ocr_hk_mac_pass_log.hk_mac_pass_expiry_date IS '有效日期';
COMMENT ON COLUMN ocr_hk_mac_pass_log.hk_mac_pass_issue_country IS '签发国代码';
COMMENT ON COLUMN ocr_hk_mac_pass_log.hk_mac_pass_english_sur_name IS '英文姓';
COMMENT ON COLUMN ocr_hk_mac_pass_log.result IS '识别结果';
COMMENT ON TABLE ocr_hk_mac_pass_log IS '港澳通行证OCR识别记录';

CREATE TABLE ocr_idcard_back_log (
  id varchar(48) NOT NULL,
  received_seq varchar(48) NOT NULL,
  received_time timestamp NOT NULL,
  type varchar(12),
  type_id varchar(12),
  channel_code varchar(255),
  scene_image_url varchar(255) NOT NULL,
  scene_image_name varchar(255),
  idcard_date_issue varchar(20),
  idcard_authority_issue varchar(255),
  idcard_limit varchar(255),
  idcard_expiry_date varchar(20),
  idcard_ocr_firm_type varchar(20),
  result char(1),
  tenant_id varchar(255) DEFAULT 'super',
  CONSTRAINT pk_ocr_idcard_back_log PRIMARY KEY (id)
);
COMMENT ON COLUMN ocr_idcard_back_log.id IS '主键';
COMMENT ON COLUMN ocr_idcard_back_log.received_seq IS '业务流水号';
COMMENT ON COLUMN ocr_idcard_back_log.received_time IS '请求时间';
COMMENT ON COLUMN ocr_idcard_back_log.type IS '图片类型，如果是图睿算法库，此项填Unknown';
COMMENT ON COLUMN ocr_idcard_back_log.type_id IS '卡证类型，如果是文通算法库，此项填0 ';
COMMENT ON COLUMN ocr_idcard_back_log.channel_code IS '渠道编码';
COMMENT ON COLUMN ocr_idcard_back_log.scene_image_url IS '待识别证件照存储路径';
COMMENT ON COLUMN ocr_idcard_back_log.scene_image_name IS '待识别证件照名称';
COMMENT ON COLUMN ocr_idcard_back_log.idcard_date_issue IS '签发日期';
COMMENT ON COLUMN ocr_idcard_back_log.idcard_authority_issue IS '签发机关';
COMMENT ON COLUMN ocr_idcard_back_log.idcard_limit IS '有效期';
COMMENT ON COLUMN ocr_idcard_back_log.idcard_expiry_date IS '过期日期';
COMMENT ON COLUMN ocr_idcard_back_log.idcard_ocr_firm_type IS '算法类型';
COMMENT ON COLUMN ocr_idcard_back_log.result IS '识别结果';
COMMENT ON TABLE ocr_idcard_back_log IS '身份证背面OCR识别记录';

CREATE TABLE ocr_idcard_front_log (
  id varchar(48) NOT NULL,
  received_seq varchar(48) NOT NULL,
  received_time timestamp NOT NULL,
  type varchar(12),
  type_id varchar(12),
  channel_code varchar(255),
  scene_image_url varchar(255) NOT NULL,
  scene_image_name varchar(255),
  idcard_number varchar(20),
  idcard_headimage varchar(255),
  idcard_ethnicity varchar(20),
  idcard_address varchar(255),
  idcard_birth varchar(255),
  idcard_gender varchar(20),
  idcard_name varchar(20),
  result char(1),
  idcard_ocr_firm_type varchar(20),
  tenant_id varchar(255) DEFAULT 'super',
  CONSTRAINT pk_ocr_idcard_front_log PRIMARY KEY (id)
);
COMMENT ON COLUMN ocr_idcard_front_log.id IS '主键';
COMMENT ON COLUMN ocr_idcard_front_log.received_seq IS '业务流水号';
COMMENT ON COLUMN ocr_idcard_front_log.received_time IS '请求时间';
COMMENT ON COLUMN ocr_idcard_front_log.type IS '图片类型，如果是图睿算法库，此项填Unknown';
COMMENT ON COLUMN ocr_idcard_front_log.type_id IS '卡证类型，如果是文通算法库，此项填0 ';
COMMENT ON COLUMN ocr_idcard_front_log.channel_code IS '渠道编码';
COMMENT ON COLUMN ocr_idcard_front_log.scene_image_url IS '待识别证件照存储路径';
COMMENT ON COLUMN ocr_idcard_front_log.scene_image_name IS '待识别证件照名称';
COMMENT ON COLUMN ocr_idcard_front_log.idcard_number IS '身份证件号';
COMMENT ON COLUMN ocr_idcard_front_log.idcard_headimage IS '证件头像';
COMMENT ON COLUMN ocr_idcard_front_log.idcard_ethnicity IS '民族';
COMMENT ON COLUMN ocr_idcard_front_log.idcard_address IS '住址';
COMMENT ON COLUMN ocr_idcard_front_log.idcard_birth IS '出生日期';
COMMENT ON COLUMN ocr_idcard_front_log.idcard_gender IS '性别';
COMMENT ON COLUMN ocr_idcard_front_log.idcard_name IS '姓名';
COMMENT ON COLUMN ocr_idcard_front_log.result IS '识别结果';
COMMENT ON COLUMN ocr_idcard_front_log.idcard_ocr_firm_type IS '算法类型';
COMMENT ON TABLE ocr_idcard_front_log IS '身份证正面OCR识别记录';


CREATE TABLE ocr_passport_log (
  id varchar(48) NOT NULL,
  received_seq varchar(48) NOT NULL,
  received_time timestamp NOT NULL,
  type varchar(12),
  type_id varchar(12),
  channel_code varchar(255),
  scene_image_url varchar(255) NOT NULL,
  scene_image_name varchar(255),
  passport_mrz_fir varchar(255),
  passport_mrz_sec varchar(255),
  passport_nationality_code varchar(20),
  passport_number varchar(255),
  passport_birth_place varchar(255),
  passport_issue_place varchar(255),
  passport_issue_date varchar(20),
  passport_rfid_mrz varchar(255),
  passport_ocr_mrz varchar(255),
  passport_birth_place_pinyin varchar(255),
  passport_issue_place_pinyin varchar(255),
  passport_id_number varchar(20),
  passport_ocr_national_name varchar(255),
  passport_ocr_gender varchar(20),
  passport_ocr_nationality_code varchar(20),
  passport_ocr_birth_date varchar(20),
  passport_ocr_expiry_date varchar(20),
  passport_ocr_authority varchar(255),
  passport_national_surname varchar(20),
  passport_national_given_name varchar(20),
  passport_height varchar(20),
  result char(1),
  tenant_id varchar(255) DEFAULT 'super',
  CONSTRAINT pk_ocr_passport_log PRIMARY KEY (id)
);
COMMENT ON COLUMN ocr_passport_log.id IS '主键';
COMMENT ON COLUMN ocr_passport_log.received_seq IS '业务流水号';
COMMENT ON COLUMN ocr_passport_log.received_time IS '请求时间';
COMMENT ON COLUMN ocr_passport_log.type IS '图片类型，如果是图睿算法库，此项填Unknown';
COMMENT ON COLUMN ocr_passport_log.type_id IS '卡证类型，如果是文通算法库，此项填0 ';
COMMENT ON COLUMN ocr_passport_log.channel_code IS '渠道编码';
COMMENT ON COLUMN ocr_passport_log.scene_image_url IS '待识别证件照存储路径';
COMMENT ON COLUMN ocr_passport_log.scene_image_name IS '待识别证件照名称';
COMMENT ON COLUMN ocr_passport_log.passport_mrz_fir IS '护照机读码第1行';
COMMENT ON COLUMN ocr_passport_log.passport_mrz_sec IS '护照机读码第2行';
COMMENT ON COLUMN ocr_passport_log.passport_nationality_code IS '持证人国籍代码';
COMMENT ON COLUMN ocr_passport_log.passport_number IS '护照号码';
COMMENT ON COLUMN ocr_passport_log.passport_birth_place IS '出生地点';
COMMENT ON COLUMN ocr_passport_log.passport_issue_place IS '签发地点';
COMMENT ON COLUMN ocr_passport_log.passport_issue_date IS '签发日期';
COMMENT ON COLUMN ocr_passport_log.passport_rfid_mrz IS '完整护照机读码射频识别';
COMMENT ON COLUMN ocr_passport_log.passport_ocr_mrz IS '完整护照机读码OCR识别';
COMMENT ON COLUMN ocr_passport_log.passport_birth_place_pinyin IS '出生地点拼音';
COMMENT ON COLUMN ocr_passport_log.passport_issue_place_pinyin IS '签发地点拼音';
COMMENT ON COLUMN ocr_passport_log.passport_id_number IS '身份证号码';
COMMENT ON COLUMN ocr_passport_log.passport_ocr_national_name IS '本国姓名拼音';
COMMENT ON COLUMN ocr_passport_log.passport_ocr_gender IS '性别';
COMMENT ON COLUMN ocr_passport_log.passport_ocr_nationality_code IS '国籍代码';
COMMENT ON COLUMN ocr_passport_log.passport_ocr_birth_date IS '出生日期';
COMMENT ON COLUMN ocr_passport_log.passport_ocr_expiry_date IS '有效期至';
COMMENT ON COLUMN ocr_passport_log.passport_ocr_authority IS '签发机关';
COMMENT ON COLUMN ocr_passport_log.passport_national_surname IS '本国姓';
COMMENT ON COLUMN ocr_passport_log.passport_national_given_name IS '本国名';
COMMENT ON COLUMN ocr_passport_log.passport_height IS '身高';
COMMENT ON COLUMN ocr_passport_log.result IS '识别结果';
COMMENT ON TABLE ocr_passport_log IS '护照OCR识别记录';


INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (101, '人脸图片文件夹', 'basedata.face.dir', './eyecool/biapwp/basedata/face/', 'N', 'admin', now(), 'admin', now(), '基础数据--人脸图片文件夹', 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (102, '指纹图片文件夹', 'basedata.finger.dir', './eyecool/biapwp/basedata/finger/', 'N', 'admin', now(), 'admin', now(), '基础数据--指纹图片文件夹', 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (103, '虹膜图片文件夹', 'basedata.iris.dir', './eyecool/biapwp/basedata/iris/', 'N', 'admin', now(), 'admin', now(), '基础数据--虹膜图片文件夹', 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (104, '人脸图片质量检测阈值', 'basedata.face.quality.detect.threshold', '70', 'N', 'admin', now(), 'admin', now(), '人脸图片质量检测阈值', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (105, '证件照文件夹', 'basedata.cert.dir', './eyecool/biapwp/basedata/cert/', 'N', 'admin', now(), 'admin', now(), '证件照文件夹', 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (106, '人脸1:1比对阈值', 'basedata.face.compare.threshold', '80', 'N', 'admin', now(), 'admin', now(), '人脸1:1比对阈值', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (107, '指纹1:1比对阈值', 'basedata.finger.compare.threshold', '40', 'N', 'admin', now(), 'admin', now(), '指纹1:1比对阈值', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (108, '虹膜1:1比对阈值', 'basedata.iris.compare.threshold', '30', 'N', 'admin', now(), 'admin', now(), '虹膜1:1比对阈值', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (111, '重复虹膜阈值', 'basedata.iris.repeat.threshold', '30', 'N', 'admin', now(), 'admin', now(), '虹膜重复阈值(比对得分超过阈值认为重复)', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (112, '重复指纹阈值', 'basedata.finger.repeat.threshold', '40', 'N', 'admin', now(), 'admin', now(), '重复指纹阈值(比对得分超过阈值认为重复)', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (113, '平台接口请求并发量', 'platform.interface.concurrent', '300', 'N', 'admin', now(), 'admin', now(), '平台接口请求并发量', 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (114, '人脸自助采集校验是否校验证件库', 'basedata.face.collect.validte.cert', 'N', 'N', 'admin', now(), 'admin', now(), '人脸自助采集校验不存在底库是否校验证件库(Y/N)', 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (115, '人脸1-N搜索图片文件夹', 'busi.face.searchN.dir', './eyecool/busi/face/search/', 'N', 'admin', now(), 'admin', now(), '人脸1-N搜索图片文件夹', 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (116, '人脸1-N搜索比对阈值', 'basedata.face.searchN.threshold', '80', 'N', 'admin', now(), 'admin', now(), '人脸1-N搜索比对阈值', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (117, '指纹图片质量检测阈值', 'basedata.finger.quality.detect.threshold', '40', 'N', 'admin', now(), 'admin', now(), '指纹图片质量检测阈值', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (118, '人脸静默检活阈值', 'basedata.face.checklive.threshold', '350', 'N', 'admin', now(), 'admin', now(), '人脸检活阈值(静默检活)，必须是数值', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (119, '指纹1-N搜索图片文件夹', 'busi.finger.searchN.dir', './eyecool/busi/finger/search/', 'N', 'admin', now(), 'admin', now(), '指纹1-N搜索图片文件夹', 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (120, '虹膜1-N搜索图片文件夹', 'busi.iris.searchN.dir', './eyecool/busi/iris/search/', 'N', 'admin',now(), 'admin', now(), '虹膜1-N搜索图片文件夹', 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (121, '指纹1-N搜索比对阈值', 'basedata.finger.searchN.threshold', '40', 'N', 'admin', now(), 'admin', now(), '指纹1-N搜索比对阈值', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (122, '虹膜1-N搜索比对阈值', 'basedata.iris.searchN.threshold', '30', 'N', 'admin', now(), 'admin', now(), '虹膜1-N搜索比对阈值', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (123, '人脸1:1认证图片文件夹', 'busi.face.compare.dir', './eyecool/busi/face/compare/', 'N', 'admin', now(), 'admin', now(), '人脸1:1认证图片文件夹', 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (124, '指纹1:1认证图片文件夹', 'busi.finger.compare.dir', './eyecool/busi/finger/compare/', 'N', 'admin', now(), 'admin', now(), '指纹1:1认证图片文件夹', 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (125, '虹膜1:1认证图片文件夹', 'busi.iris.compare.dir', './eyecool/busi/iris/compare/', 'N', 'admin', now(), 'admin', now(), '虹膜1:1认证图片文件夹', 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (127, '人脸入库是否进行1-N校验', 'basedata.face.add.isValidN', 'N', 'N', 'admin', now(), 'admin', now(), '人脸入库是否进行1-N校验(Y/N)', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (130, '指纹入库是否进行1-N校验', 'basedata.finger.add.isValidN', 'N', 'N', 'admin', now(), 'admin', now(), '指纹入库是否进行1-N校验(Y/N)， 用于新增、修改', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (131, '虹膜入库是否进行1-N校验', 'basedata.iris.add.isValidN', 'N', 'N', 'admin', now(), 'admin', now(), '虹膜入库是否进行1-N校验(Y/N)， 用于新增、修改', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (132, '设备APP版本文件存放文件夹', 'device.version.file.dir', './eyecool/device/version/', 'N', 'admin', now(), '', NULL, '设备APP版本文件存放文件夹', 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (133, '消息通知附件存放位置', 'msg.attachment.dir', './eyecool/msg/attachment/', 'N', 'admin', now(), 'admin', now(), '消息通知附件存放位置', 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (134, '人脸1-N识别日志保存是否比对去重', 'busi.face.searchN.log.save.distinct', 'Y', 'N', 'admin', now(), 'busi_admin', now(), '人脸1-N日识别可能存在短时间内（例如5s）连续识别到一个人的情况，是否对识别日志去重处理（Y：是，N：否），是就只保存第一次识别的记录', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (137, '平台是否开启1-N搜索功能', 'platform.searchN.function.isOpen', 'Y', 'N', 'admin', now(), 'admin', now(), '注意：此参数值需要在初始化部署时设定，避免在平台使用过程中改动。\r\n说明：平台是否开启1-N搜索功能（Y:开启，N：不开启）,不开启则不需要部署Datamanager和fox-minisearch微服务。', 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (145, '人脸入库是否进行活体检测', 'basedata.face.add.checkLive', 'N', 'N', 'admin', now(), 'admin', now(), '人脸入库是否进行活体检测(Y/N)，用于新增、修改、自助采集', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (146, '平台是否支持自动绑定和解绑人库关系', 'platform.libperson.isAutobind', 'N', 'N', 'admin', now(), 'admin', now(), '平台是否支持自动绑定和解绑人库关系（用于部分渠道需要绑定全库人员场景，人员入库直接和相关渠道绑定）', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (147, '平台需要自动绑定人库关系的渠道编码', 'platform.libperson.autobind.channel', '-', 'N', 'admin', now(), 'busi_admin', now(), '平台需要自动绑定人库关系的渠道编码(用于部分渠道需要绑定全库人员场景，仅在开启自动绑定时有效，多个渠道编码使用\;\分割)', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (148, '设备外观图片存放文件夹', 'device.model.image.dir', './eyecool/device/exteriorImage', 'N', 'admin', now(), 'admin', now(), '设备外观图片存放文件夹', 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (150, '人脸视频检活阈值', 'basedata.face.video.checklive.threshold', '350', 'N', 'admin', now(), 'admin', now(), '人脸视频检活阈值(必须是数值)', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (152, '多模态虹膜1v1比对阈值', 'multi.iris.compare.threshold', '30', 'N', 'admin', now(), 'admin', now(), '多模态虹膜阈值', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (153, '多模态人脸1v1比对阈值', 'multi.face.compare.threshold', '80', 'N', 'admin', now(), 'admin', now(), '多模态人脸阈值', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (154, '多模态虹膜人脸融合1v1比对阈值', 'multi.irisface.fusion.compare.threshold', '80', 'N', 'admin', now(), 'admin', now(), '多模态虹膜人脸融合阈值', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (155, '多模态日志照片存储路径', 'busi.mulit.search.dir', './eyecool/busi/mulit/', 'N', 'admin', now(), '', NULL, '多模态日志照片存储路径', 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (156, 'OCR日志图片存放图片路径', 'busi.orc.image.dir', './eyecool/busi/ocr/', 'N', 'admin', now(), 'admin', now(), 'OCR图片存储路径', 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (157, '无感识别陌生人推送开关', 'biapwp.dfrs.stranger.push', 'true', 'N', 'admin', now(), 'admin', now(), '无感识别陌生人推送开关', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (158, '无感识别命中推送开关', 'biapwp.dfrs.hit.push', 'true', 'N', 'admin', now(), 'admin', now(), '无感识别命中推送开关', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (159, '高温报警阈值', 'biapwp.dfrs.hcnet.alarmtem', '37.0', 'N', 'admin', now(), 'admin', now(), '高温报警阈值', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (160, '前端命中显示模版照', 'biapwp.dfrs.hit.showTmpl', 'false', 'N', 'admin', now(), 'admin', now(), '前端命中显示模版照', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (161, '生物信息变化触发信息同步的渠道编码', 'person.biochange.liveupdate.channel', '-', 'N', 'admin', now(), 'busi_admin', now(), '配置生物特征变化才会触发更新的渠道编码，多个使用;隔开', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (162, '平台是否接收场景回传日志', 'platform.accept.baklog.open', 'Y', 'N', 'admin', now(), 'admin', now(), '平台是否接收场景回传日志（是：Y， 否：N）', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (163, '多模态底库照片存储路径', 'base.mulit.dir', './eyecool/base/mulit/', 'N', 'admin', now(), '', NULL, '多模态底库照片存储路径', 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (165, '联网核查接口地址', 'identity_verification_url', 'http://green.eyekey.com/ark-plc-id/checkIdentity', 'N', 'admin', now(), 'busi_admin', now(), '联网身份核查接口请求地址URL', 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (166, '联网核查接口授权码', 'identity_verification_authCode', 'd61c1f9110524f20b491c11779bf120d', 'N', 'admin', now(), 'busi_admin', now(), '联网核查接口授权码', 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (167, '异常健康码推送微信公众号参数', 'abnormal.recog.push.weixin.params', '{}', 'N', 'admin', now(), 'busi_admin', now(), '维护格式为JSON格式，多个收件人手机可以用逗号分隔，例如：{\appId\:\123\,\templateId\:\123\,\phone\:\13011112222,13011113333\}', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (168, '203获取图片地址', 'biapwp.url', 'https://cloud.eyecool.cn/api/device/adapter/getPersonFace?id=', 'N', 'admin', now(), 'busi_admin', now(), '', 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (169, '开通钉钉数据同步的钉钉APPKEY', 'dingtalk.syncdata.appkey', '-', 'N', 'admin', now(), 'admin', NULL, '', 'Y');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (170, 'SDK上传文件夹', 'sys.tool.sdkFile.dir', './eyecool/biapwp/tool/sdkFile/', 'N', 'admin', now(), 'admin', now(), NULL, 'N');
SELECT setval('sys_config_config_id_seq', 170, true);

INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (100, '数据来源', 'apply_data_source', '0', 'admin', now(), '', NULL, '数据来源');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (101, '人员标记', 'apply_person_flag', '0', 'admin', now(), '', NULL, '人员标记');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (102, '是否加密', 'apply_encrypted', '0', 'admin', now(), '', NULL, '是否加密');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (103, '手指编码', 'bio_finger_code', '0', 'admin', now(), '', NULL, '手指编码');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (104, '身份证指纹', 'bio_idcard_finger', '0', 'admin', now(), '', NULL, '身份证指纹');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (105, '眼睛编码', 'bio_eye_code', '0', 'admin', now(), '', NULL, '眼睛编码');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (106, '证件类型', 'sys_cert_type', '0', 'admin', now(), NULL, NULL, '证件类型列表');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (107, '证件照片类型', 'cert_photo_type', '0', 'admin',  now(), 'admin',  now(), '证件照片类型');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (108, '民族', 'sys_nation', '0', 'admin',  now(), '', NULL, '民族');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (109, '生物信息图片类型', 'bio_photo_type', '0', 'admin',  now(), 'admin',  now(), '生物信息图片类型');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (110, '生物特征开通状态', 'bio_mode_status', '0', 'admin',  now(), '', NULL, '生物特征开通状态');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (112, '渠道1-N识别方式', 'search_n_type', '0', 'admin',  now(), 'admin',  now(), '1-N识别方式');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (113, 'HTTP交易接口', 'http_interface', '0', 'admin',  now(), 'admin',  now(), 'HTTP交易接口');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (115, '渠道管理认证类型', 'channel_bio_attest_type', '0', 'admin',  now(), '', NULL, '渠道管理认证类型');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (116, '渠道管理数据来源', 'channel_business_source', '0', 'admin',  now(), '', NULL, '渠道管理数据来源');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (117, '生物认证识别结果', 'bio_result', '0', 'admin',  now(), 'admin',  now(), '生物认证识别结果');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (118, '生物特征1-N日志场景类型', 'search_log_type', '0', 'admin',  now(), 'admin',  now(), '生物特征1-N日志场景类型');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (121, '设备类型', 'client_device_type', '0', 'admin',  now(), '', NULL, '设备类型');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (122, '设备升级状态', 'device_upgrade_status', '0', 'admin',  now(), '', NULL, '设备升级状态');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (123, '设备升级任务结果', 'device_upgrade_result', '0', 'admin',  now(), '', NULL, '设备升级任务结果');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (124, '消息通知方式', 'msg_notice_method', '0', 'admin',  now(), '', NULL, '消息通知方式');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (125, '消息发送模式', 'msg_type', '0', 'admin',  now(), 'admin',  now(), '消息发送模式');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (126, '消息推送结果', 'msg_result', '0', 'admin',  now(), '', NULL, '消息推送结果');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (127, '邮件类型', 'msg_mail_type', '0', 'admin',  now(), '', NULL, '邮件类型');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (128, '钉钉消息类型', 'msg_dingtalk_type', '0', 'admin',  now(), '', NULL, '钉钉消息类型');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (129, '钉钉媒体文件类型', 'msg_dingtalk_media_type', '0', 'admin',  now(), '', NULL, '钉钉媒体文件类型');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (130, '设备添加方式', 'device_create_method', '0', 'admin',  now(), '', NULL, '设备添加方式');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (131, '设备在线状态', 'device_online_state', '0', 'admin',  now(), '', NULL, '设备在线状态');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (132, '设备动作日志类型', 'device_action_type', '0', 'admin',  now(), '', NULL, '设备动作日志类型');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (133, '设备参数下发结果', 'device_config_result', '0', 'admin',  now(), '', NULL, '设备参数下发结果');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (135, '区域类型', 'area_type', '0', 'admin',  now(), 'admin', now(), '区域类型');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (137, '租户状态', 'sys_tenant_state', '0', 'admin',  now(), '', NULL, '租户状态列表');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (138, '租户来源', 'sys_tenant_source', '0', 'admin',  now(), '', NULL, '租户来源列表');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (139, '多模态类型', 'fusion_type', '0', 'admin',  now(), 'admin',  now(), '多模态类型');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (140, '人脸虹膜多模态比对模式', 'match_mode', '0', 'admin', now(), 'admin',  now(), '人脸虹膜多模态比对模式');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (143, '健康码请求地址', 'healthcode.request.url', '0', 'admin',  now(), '', NULL, '健康码');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (144, 'SDK类型', 'sys_sdk_type', '0', 'admin', now(), '', NULL, 'SDK文件类型');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (145, '设备进出方向', 'device_direction', '0', 'admin', now(), '', NULL, '设备进出方向');
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (146, '租户类型', 'sys_tenant_type', '0', 'admin', now(), '', NULL, '租户类型');


INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (100, 1, '内部接口', 'INTERFACE', 'apply_data_source', '', 'success', 'Y', '0', 'admin',  now(), 'admin',  now(), '数据来源--接口');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (101, 2, '数据导入', 'IMP', 'apply_data_source', '', 'info', 'N', '0', 'admin',  now(), 'admin',  now(), '数据来源--数据导入');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (102, 3, 'HTTP接口', 'HTTP', 'apply_data_source', '', 'warning', 'N', '0', 'admin',  now(), 'admin',  now(), '数据来源--HTTP');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (103, 1, '正常', '1', 'apply_person_flag', '', 'success', 'Y', '0', 'admin',  now(), 'admin',  now(), '人员标记--正常');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (104, 2, '红名单', '2', 'apply_person_flag', '', 'primary', 'N', '0', 'admin',  now(), 'admin',  now(), '人员标记--红名单');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (105, 3, '黑名单', '3', 'apply_person_flag', '', 'danger', 'N', '0', 'admin',  now(), 'admin',  now(), '人员标记--黑名单');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (106, 1, '加密', '1', 'apply_encrypted', '', 'primary', 'Y', '0', 'admin',  now(), 'admin',  now(), '是否加密-加密');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (107, 2, '不加密', '0', 'apply_encrypted', '', 'success', 'N', '0', 'admin',  now(), 'admin',  now(), '是否加密-不加密');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (167, 1, '左手拇指', '16', 'bio_finger_code', '', '', 'N', '0', 'admin', now(), 'admin',  now(), '手指编码-左手拇指');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (168, 2, '左手食指', '17', 'bio_finger_code', '', '', 'N', '0', 'admin',  now(), 'admin',  now(), '手指编码-左手食指');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (169, 3, '左手中指', '18', 'bio_finger_code', '', '', 'N', '0', 'admin',  now(), 'admin',  now(), '手指编码-左手中指');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (170, 4, '左手环指', '19', 'bio_finger_code', '', '', 'N', '0', 'admin',  now(), 'admin', now(), '手指编码-左手无名指');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (171, 5, '左手小指', '20', 'bio_finger_code', '', '', 'N', '0', 'admin',  now(), 'admin',  now(), '手指编码-左手小指');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (172, 6, '右手拇指', '11', 'bio_finger_code', '', '', 'N', '0', 'admin',  now(), 'admin',  now(), '手指编码-右手拇指');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (173, 7, '右手食指', '12', 'bio_finger_code', '', '', 'Y', '0', 'admin',  now(), 'admin',  now(), '手指编码-右手食指');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (174, 8, '右手中指', '13', 'bio_finger_code', '', '', 'N', '0', 'admin',  now(), 'admin',  now(), '手指编码-右手中指');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (175, 9, '右手环指', '14', 'bio_finger_code', '', '', 'N', '0', 'admin',  now(), 'admin',  now(), '手指编码-右手无名指');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (176, 10, '右手小指', '15', 'bio_finger_code', '', '', 'N', '0', 'admin',  now(), 'admin',  now(), '手指编码-右手小指');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (179, 1, '身份证指纹A', 'A', 'bio_idcard_finger', '', '', 'Y', '0', 'admin',  now(), '', NULL, '身份证指纹A');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (180, 2, '身份证指纹B', 'B', 'bio_idcard_finger', '', '', 'N', '0', 'admin',  now(), '', NULL, '身份证指纹B');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (181, 1, '左眼', 'L', 'bio_eye_code', '', '', 'Y', '0', 'admin', now(), '', NULL, '眼睛编码-左眼');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (182, 2, '右眼', 'R', 'bio_eye_code', '', '', 'N', '0', 'admin', now(), '', NULL, '眼睛编码-右眼');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (183, 1, '居民身份证', '111', 'sys_cert_type', NULL, 'default', 'Y', '0', 'admin', now(), NULL, NULL, '证件类型-居民身份证');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (184, 2, '临时居民身份证', '112', 'sys_cert_type', NULL, 'primary', 'N', '0', 'admin', now(), NULL, NULL, '证件类型-临时居民身份证');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (185, 3, '户口簿', '113', 'sys_cert_type', NULL, 'success', 'N', '0', 'admin', now(), NULL, NULL, '证件类型-户口簿');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (186, 4, '军官证', '114', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', now(), NULL, NULL, '证件类型-军官证');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (187, 5, '警官证', '123', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', now(), NULL, NULL, '证件类型-警官证');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (188, 6, '学生证', '133', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', now(), NULL, NULL, '证件类型-学生证');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (189, 7, '外交护照', '411', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', now(), NULL, NULL, '证件类型-外交护照');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (190, 8, '公务护照', '412', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', now(), NULL, NULL, '证件类型-公务护照');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (191, 9, '因公普通护照', '413', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', now(), NULL, NULL, '证件类型-因公普通护照');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (192, 10, '护照', '414', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', now(), NULL, NULL, '证件类型-普通护照');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (193, 11, '入出境通行证', '416', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', now(), NULL, NULL, '证件类型-入出境通行证');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (194, 12, '外国人出入境证', '417', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', now(), NULL, NULL, '证件类型-外国人出入境证');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (195, 13, '海员证', '419', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', now(), NULL, NULL, '证件类型-海员证');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (196, 14, '台湾居民来往大陆通行证', '511', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', now(), NULL, NULL, '证件类型-台湾居民来往大陆通行');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (197, 15, '往来港澳通行证', '513', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', now(), NULL, NULL, '证件类型-往来港澳通行证');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (198, 16, '回乡证', '516', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', now(), NULL, NULL, '证件类型-回乡证');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (199, 17, '大陆居民往来台湾通行证', '517', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', now(), NULL, NULL, '证件类型-大陆居民往来台湾通行证');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (200, 18, '中朝边境地区出入境通行证', '733', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', now(), NULL, NULL, '证件类型-中朝边境地区出入境通行证');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (201, 19, '中蒙边境地区出入境通行证', '736', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', now(), NULL, NULL, '证件类型-中蒙边境地区出入境通行证');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (202, 20, '中缅边境地区出入境通行证', '738', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', now(), NULL, NULL, '证件类型-中缅边境地区出入境通行证');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (203, 21, '云南省边境地区境外边民入出境证', '740', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', now(), NULL, NULL, '证件类型-云南省边境地区境外边民入出境证');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (204, 22, '中尼边境地区出入境通行证', '741', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', now(), NULL, NULL, '证件类型-中尼边境地区出入境通行证');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (205, 23, '中越边境地区出入境通行证', '743', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', now(), NULL, NULL, '证件类型-中越边境地区出入境通行证');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (206, 24, '中老边境地区出入境通行证', '745', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', now(), NULL, NULL, '证件类型-中老边境地区出入境通行证');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (207, 25, '中印边境地区出入境通行证', '747', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', now(), NULL, NULL, '证件类型-中印边境地区出入境通行证');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (208, 26, '其他', '990', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', now(), NULL, NULL, '证件类型-其他');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (209, 27, '军人证', '991', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', now(), NULL, NULL, '证件类型-军人证');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (210, 28, '港澳台居住证', '992', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', now(), NULL, NULL, '证件类型-港澳台居住证');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (211, 29, '外国人居住证', '993', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', now(), NULL, NULL, '证件类型-外国人居住证');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (212, 30, '越南身份证', '994', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', now(), NULL, NULL, '证件类型-越南身份证');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (213, 31, '边民证', '995', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin', now(), NULL, NULL, '证件类型-边民证');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (214, 32, '无证件', '999', 'sys_cert_type', NULL, 'info', 'N', '0', 'admin',now(), NULL, NULL, '证件类型-无证件');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (215, 2, '入学照片', '1', 'cert_photo_type', '', '', 'N', '0', 'admin', now(), 'admin', now(), '证件照片类型-入学照片');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (216, 3, '在校照片', '2', 'cert_photo_type', '', '', 'N', '0', 'admin', now(), 'admin', now(), '证件照片类型-在校照片');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (217, 4, '毕业照片', '3', 'cert_photo_type', '', '', 'N', '0', 'admin', now(), 'admin', now(), '证件照片类型-毕业照片');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (218, 1, '汉族', '01', 'sys_nation', NULL, 'info', 'N', '0', 'admin', now(), 'ry', now(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (219, 2, '蒙古族', '02', 'sys_nation', NULL, 'info', 'N', '0', 'admin', now(), 'ry', now(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (220, 3, '回族', '03', 'sys_nation', NULL, 'info', 'N', '0', 'admin', now(), 'ry', now(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (221, 4, '藏族', '04', 'sys_nation', NULL, 'info', 'N', '0', 'admin', now(), 'ry', now(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (222, 5, '维吾尔族', '05', 'sys_nation', NULL, 'info', 'N', '0', 'admin', now(), 'ry', now(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (223, 6, '苗族', '06', 'sys_nation', NULL, 'info', 'N', '0', 'admin', now(), 'ry', now(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (224, 7, '彝族', '07', 'sys_nation', NULL, 'info', 'N', '0', 'admin', now(), 'ry', now(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (225, 8, '壮族', '08', 'sys_nation', NULL, 'info', 'N', '0', 'admin', now(), 'ry', now(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (226, 9, '布依族', '09', 'sys_nation', NULL, 'info', 'N', '0', 'admin', now(), 'ry', now(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (227, 10, '朝鲜族', '10', 'sys_nation', NULL, 'info', 'N', '0', 'admin', now(), 'ry', now(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (228, 11, '满族', '11', 'sys_nation', NULL, 'info', 'N', '0', 'admin', now(), 'ry', now(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (229, 12, '侗族', '12', 'sys_nation', NULL, 'info', 'N', '0', 'admin', now(), 'ry', now(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (230, 13, '瑶族', '13', 'sys_nation', NULL, 'info', 'N', '0', 'admin', now(), 'ry', now(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (231, 14, '白族', '14', 'sys_nation', NULL, 'info', 'N', '0', 'admin', now(), 'ry', now(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (232, 15, '土家族', '15', 'sys_nation', NULL, 'info', 'N', '0', 'admin', now(), 'ry', now(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (233, 16, '哈尼族', '16', 'sys_nation', NULL, 'info', 'N', '0', 'admin', now(), 'ry', now(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (234, 17, '哈萨克族', '17', 'sys_nation', NULL, 'info', 'N', '0', 'admin', now(), 'ry', now(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (235, 18, '傣族', '18', 'sys_nation', NULL, 'info', 'N', '0', 'admin', now(), 'ry', now(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (236, 19, '黎族', '19', 'sys_nation', NULL, 'info', 'N', '0', 'admin', now(), 'ry', now(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (237, 20, '傈僳族', '20', 'sys_nation', NULL, 'info', 'N', '0', 'admin', now(), 'ry', now(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (238, 21, '佤族', '21', 'sys_nation', NULL, 'info', 'N', '0', 'admin', now(), 'ry', now(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (239, 22, '畲族', '22', 'sys_nation', NULL, 'info', 'N', '0', 'admin', now(), 'ry', now(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (240, 23, '高山族', '23', 'sys_nation', NULL, 'info', 'N', '0', 'admin', now(), 'ry', now(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (241, 24, '拉祜族', '24', 'sys_nation', NULL, 'info', 'N', '0', 'admin', now(), 'ry', now(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (242, 25, '水族', '25', 'sys_nation', NULL, 'info', 'N', '0', 'admin', now(), 'ry', now(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (243, 26, '东乡族', '26', 'sys_nation', NULL, 'info', 'N', '0', 'admin', now(), 'ry', now(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (244, 27, '纳西族', '27', 'sys_nation', NULL, 'info', 'N', '0', 'admin', now(), 'ry', now(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (245, 28, '景颇族', '28', 'sys_nation', NULL, 'info', 'N', '0', 'admin', now(), 'ry', now(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (246, 29, '柯尔克孜族', '29', 'sys_nation', NULL, 'info', 'N', '0', 'admin', now(), 'ry', now(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (247, 30, '土族', '30', 'sys_nation', NULL, 'info', 'N', '0', 'admin', now(), 'ry', now(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (248, 31, '达斡尔族', '31', 'sys_nation', NULL, 'info', 'N', '0', 'admin', now(), 'ry', now(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (249, 32, '仫佬族', '32', 'sys_nation', NULL, 'info', 'N', '0', 'admin', now(), 'ry', now(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (250, 33, '羌族', '33', 'sys_nation', NULL, 'info', 'N', '0', 'admin', now(), 'ry', now(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (251, 34, '布朗族', '34', 'sys_nation', NULL, 'info', 'N', '0', 'admin', now(), 'ry', now(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (252, 35, '撒拉族', '35', 'sys_nation', NULL, 'info', 'N', '0', 'admin', now(), 'ry', now(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (253, 36, '毛南族', '36', 'sys_nation', NULL, 'info', 'N', '0', 'admin', now(), 'ry', now(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (254, 37, '仡佬族', '37', 'sys_nation', NULL, 'info', 'N', '0', 'admin', now(), 'ry', now(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (255, 38, '锡伯族', '38', 'sys_nation', NULL, 'info', 'N', '0', 'admin', now(), 'ry', now(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (256, 39, '阿昌族', '39', 'sys_nation', NULL, 'info', 'N', '0', 'admin', now(), 'ry', now(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (257, 40, '普米族', '40', 'sys_nation', NULL, 'info', 'N', '0', 'admin', now(), 'ry', now(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (258, 41, '塔吉克族', '41', 'sys_nation', NULL, 'info', 'N', '0', 'admin', now(), 'ry', now(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (259, 42, '怒族', '42', 'sys_nation', NULL, 'info', 'N', '0', 'admin', now(), 'ry', now(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (260, 43, '乌孜别克族', '43', 'sys_nation', NULL, 'info', 'N', '0', 'admin', now(), 'ry', now(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (261, 44, '俄罗斯族', '44', 'sys_nation', NULL, 'info', 'N', '0', 'admin', now(), 'ry', now(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (262, 45, '鄂温克族', '45', 'sys_nation', NULL, 'info', 'N', '0', 'admin', now(), 'ry', now(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (263, 46, '德昂族', '46', 'sys_nation', NULL, 'info', 'N', '0', 'admin', now(), 'ry', now(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (264, 47, '保安族', '47', 'sys_nation', NULL, 'info', 'N', '0', 'admin', now(), 'ry', now(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (265, 48, '裕固族', '48', 'sys_nation', NULL, 'info', 'N', '0', 'admin', now(), 'ry', now(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (266, 49, '京族', '49', 'sys_nation', NULL, 'info', 'N', '0', 'admin', now(), 'ry', now(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (267, 50, '塔塔尔族', '50', 'sys_nation', NULL, 'info', 'N', '0', 'admin', now(), 'ry', now(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (268, 51, '独龙族', '51', 'sys_nation', NULL, 'info', 'N', '0', 'admin', now(), 'ry', now(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (269, 52, '鄂伦春族', '52', 'sys_nation', NULL, 'info', 'N', '0', 'admin', now(), 'ry', now(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (270, 53, '赫哲族', '53', 'sys_nation', NULL, 'info', 'N', '0', 'admin', now(), 'ry', now(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (271, 54, '门巴族', '54', 'sys_nation', NULL, 'info', 'N', '0', 'admin', now(), 'ry', now(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (272, 55, '珞巴族', '55', 'sys_nation', NULL, 'info', 'N', '0', 'admin', now(), 'ry', now(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (273, 56, '基诺族', '56', 'sys_nation', NULL, 'info', 'N', '0', 'admin', now(), 'ry', now(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (274, 57, '穿青人族', '81', 'sys_nation', NULL, 'info', 'N', '0', 'admin', now(), 'ry', now(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (275, 58, '其他', '97', 'sys_nation', NULL, 'info', 'N', '0', 'admin', now(), 'ry', now(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (276, 59, '外国血统', '98', 'sys_nation', NULL, 'info', 'N', '0', 'admin', now(), 'ry', now(), '民族');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (277, 1, '人脸图片', '1', 'bio_photo_type', NULL, NULL, 'Y', '0', 'admin', now(), '', NULL, '生物信息图片类型-人脸图片');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (278, 2, '指纹图片', '2', 'bio_photo_type', NULL, NULL, 'N', '0', 'admin', now(), '', NULL, '生物信息图片类型-指纹图片');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (279, 3, '虹膜图片', '3', 'bio_photo_type', NULL, NULL, 'N', '0', 'admin', now(), '', NULL, '生物信息图片类型-虹膜图片');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (280, 1, '开通', '1', 'bio_mode_status', NULL, 'primary', 'Y', '0', 'admin', now(), '', NULL, '生物特征开通状态-开通');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (281, 2, '不开通', '0', 'bio_mode_status', NULL, 'danger', 'N', '0', 'admin', now(), '', NULL, '生物特征开通状态-不开通');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (285, 1, '校验分库', '1', 'search_n_type', '', 'primary', 'N', '0', 'admin', now(), 'admin', now(), '1-N识别方式-校验分库');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (286, 2, '校验渠道库', '2', 'search_n_type', '', 'warning', 'Y', '0', 'admin', now(), 'admin', now(), '1-N识别方式-校验渠道库');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (287, 3, '校验全库', '3', 'search_n_type', '', 'success', 'N', '0', 'admin', now(), 'admin', now(), '1-N识别方式-校验全库');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (288, 1, '新增人员信息', 'PERSON_INFO_INSERT', 'http_interface', '', '', 'Y', '0', 'admin', now(), 'admin', now(), 'HTTP交易接口-新增人员信息');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (291, 1, '人脸', '0', 'channel_bio_attest_type', '', 'info', 'Y', '0', 'admin', now(), 'admin', now(), '人脸');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (292, 2, '指纹', '1', 'channel_bio_attest_type', NULL, 'info', 'Y', '0', 'admin', now(), '', NULL, '指纹');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (293, 3, '虹膜', '2', 'channel_bio_attest_type', NULL, 'info', 'Y', '0', 'admin', now(), '', NULL, '虹膜');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (294, 4, '指静脉', '3', 'channel_bio_attest_type', NULL, 'info', 'Y', '0', 'admin', now(), '', NULL, '指静脉');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (295, 1, '接口', 'INTERFACE', 'channel_business_source', NULL, 'info', 'Y', '0', 'admin', now(), '', NULL, '接口');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (296, 2, '导入', 'IMP', 'channel_business_source', NULL, 'info', 'Y', '0', 'admin', now(), '', NULL, '导入');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (297, 3, 'HTTP接口', 'HTTP', 'channel_business_source', NULL, 'info', 'Y', '0', 'admin', now(), '', NULL, 'HTTP接口');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (298, 2, '修改人员信息', 'PERSON_INFO_UPDATE', 'http_interface', NULL, NULL, 'N', '0', 'admin', now(), '', NULL, 'HTTP交易接口--修改人员信息');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (299, 3, '删除人员信息', 'PERSON_INFO_DELETE', 'http_interface', NULL, NULL, 'N', '0', 'admin', now(), '', NULL, 'HTTP交易接口--删除人员信息');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (300, 4, '查询人员信息', 'PERSON_INFO_SELECT', 'http_interface', NULL, NULL, 'N', '0', 'admin', now(), '', NULL, 'HTTP交易接口--查询人员信息');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (301, 11, '右手不确定指位', '97', 'bio_finger_code', NULL, NULL, 'N', '0', 'admin', now(), '', NULL, '手指编码--右手不确定指位');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (302, 12, '左手不确定指位', '98', 'bio_finger_code', NULL, NULL, 'N', '0', 'admin', now(), '', NULL, '手指编码--左手不确定指位');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (303, 13, '其他不确定指位', '99', 'bio_finger_code', NULL, NULL, 'N', '0', 'admin', now(), '', NULL, '手指编码--其他不确定指位');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (305, 1, '证件照片', '0', 'cert_photo_type', NULL, NULL, 'Y', '0', 'admin', now(), '', NULL, '证件照片类型--证件照片');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (306, 5, '开通人脸功能', 'PERSON_FACE_OPEN', 'http_interface', '', '', 'N', '0', 'admin', now(), 'admin', now(), 'HTTP交易接口--开通人脸');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (307, 6, '关闭人脸功能', 'PERSON_FACE_CLOSE', 'http_interface', NULL, NULL, 'N', '0', 'admin', now(), '', NULL, 'HTTP交易接口--关闭人脸');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (308, 7, '人脸识别(1:N)', 'PERSON_FACE_RECOG', 'http_interface', NULL, NULL, 'N', '0', 'admin', now(), '', NULL, 'HTTP交易接口--人脸识别(1:N)');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (309, 8, '人脸认证(1:1)', 'PERSON_FACE_VERIFY', 'http_interface', NULL, NULL, 'N', '0', 'admin', now(), '', NULL, 'HTTP交易接口--人脸认证(1:1)');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (310, 9, '开通指纹功能', 'PERSON_FINGER_OPEN', 'http_interface', NULL, NULL, 'N', '0', 'admin', now(), '', NULL, 'HTTP交易接口--开通指纹');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (311, 10, '关闭指纹功能', 'PERSON_FINGER_CLOSE', 'http_interface', NULL, NULL, 'N', '0', 'admin', now(), '', NULL, 'HTTP交易接口--关闭指纹');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (312, 11, '指纹识别(1:N)', 'PERSON_FINGER_RECOG', 'http_interface', '', '', 'N', '0', 'admin', now(), 'admin', now(), 'HTTP交易接口--指纹识别(1:N)');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (313, 12, '指纹认证(1:1)', 'PERSON_FINGER_VERIFY', 'http_interface', NULL, NULL, 'N', '0', 'admin', now(), '', NULL, 'HTTP交易接口--指纹认证(1:1)');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (314, 13, '开通虹膜功能', 'PERSON_IRIS_OPEN', 'http_interface', NULL, NULL, 'N', '0', 'admin', now(), '', NULL, 'HTTP交易接口--开通虹膜');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (315, 14, '关闭虹膜功能', 'PERSON_IRIS_CLOSE', 'http_interface', NULL, NULL, 'N', '0', 'admin', now(), '', NULL, 'HTTP交易接口--关闭虹膜');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (316, 15, '虹膜识别(1:N)', 'PERSON_IRIS_RECOG', 'http_interface', NULL, NULL, 'N', '0', 'admin', now(), '', NULL, 'HTTP交易接口--虹膜识别(1:N)');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (317, 16, '虹膜认证(1:1)', 'PERSON_IRIS_VERIFY', 'http_interface', NULL, NULL, 'N', '0', 'admin', now(), '', NULL, 'HTTP交易接口--虹膜认证(1:1)');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (318, 17, '开通指静脉功能', 'PERSON_FVEIN_OPEN', 'http_interface', '', '', 'N', '0', 'admin', now(), 'admin', now(), 'HTTP交易接口--开通指静脉');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (319, 18, '关闭指静脉功能', 'PERSON_FVEIN_CLOSE', 'http_interface', '', '', 'N', '0', 'admin', now(), 'admin', now(), 'HTTP交易接口--关闭指静脉');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (320, 19, '指静脉识别(1:N)', 'PERSON_FVEIN_RECOG', 'http_interface', NULL, NULL, 'N', '0', 'admin', now(), '', NULL, 'HTTP交易接口--指静脉识别(1:N)');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (321, 20, '指静脉认证(1:1)', 'PERSON_FVEIN_VERIFY', 'http_interface', NULL, NULL, 'N', '0', 'admin', now(), '', NULL, 'HTTP交易接口--指静脉认证(1:1)');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (322, 21, '短信推送功能', 'MESSAGE_SEND_SMS', 'http_interface', '', '', 'N', '0', 'admin', now(), 'admin', now(), 'HTTP交易接口--短信推送');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (323, 22, '微信服务号推送', 'MESSAGE_SEND_WECHAT', 'http_interface', NULL, NULL, 'N', '0', 'admin', now(), '', NULL, 'HTTP交易接口--微信服务号推送');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (324, 23, '邮件推送功能', 'MESSAGE_SEND_EMAIL', 'http_interface', NULL, NULL, 'N', '0', 'admin', now(), '', NULL, 'HTTP交易接口--邮件推送功能');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (325, 1, '通过', '0', 'bio_result', '', 'primary', 'Y', '0', 'admin', now(), 'admin', now(), '生物认证识别结果--通过');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (326, 2, '未通过', '1', 'bio_result', '', 'danger', 'N', '0', 'admin', now(), 'admin', now(), '生物认证识别结果--未通过');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (327, 1, '基础信息入库1:N', '1', 'search_log_type', '', 'success', 'N', '0', 'admin', now(), 'admin', now(), '生物特征1-N日志场景类型--基础信息入库1:N');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (328, 2, '比对搜索接口1:N', '2', 'search_log_type', '', 'primary', 'Y', '0', 'admin', now(), 'admin', now(), '生物特征1-N日志场景类型--1:N比对搜索接口');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (331, 24, '人员数据实时更新', 'PERSON_LIVE_UPDATE', 'http_interface', '', '', 'N', '0', 'admin', now(), 'admin', now(), 'HTTP交易接口--人员数据实时更新');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (332, 4, '依次校验', '4', 'search_n_type', '', 'danger', 'N', '0', 'admin', now(), 'admin', now(), '按照分库->渠道库->全库依次查询，查询到即返回');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (333, 3, '子系统日志回传', '3', 'search_log_type', '', 'warning', 'N', '0', 'admin', now(), 'admin', now(), '生物特征1-N日志场景类型--子系统日志回传');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (334, 25, '人脸识别日志回传', 'PERSON_FACE_SEARCH_LOG_BAK', 'http_interface', '', '', 'N', '0', 'admin', now(), 'admin', now(), 'HTTP交易接口--人脸识别日志回传');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (335, 26, '获取人脸特征', 'PERSON_FACE_FEATURE', 'http_interface', NULL, NULL, 'N', '0', 'admin', now(), '', NULL, 'HTTP交易接口--获取人脸特征');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (336, 27, '分库操作功能', 'PERSON_SUB_TREASURY_OPERATE', 'http_interface', NULL, NULL, 'N', '0', 'admin', now(), '', NULL, 'HTTP交易接口--分库操作功能');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (340, 1, '待下载', '1', 'device_upgrade_status', '', 'info', 'Y', '0', 'admin', now(), 'admin', now(), '设备升级状态--待下载');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (341, 2, '待更新', '2', 'device_upgrade_status', '', 'primary', 'N', '0', 'admin', now(), 'admin', now(), '设备升级状态--待更新');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (342, 3, '更新成功', '3', 'device_upgrade_status', NULL, 'success', 'N', '0', 'admin', now(), '', NULL, '设备升级状态--更新成功');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (343, 4, '更新失败', '4', 'device_upgrade_status', NULL, 'danger', 'N', '0', 'admin', now(), '', NULL, '设备升级状态--更新失败');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (344, 5, '跳过', '5', 'device_upgrade_status', NULL, 'warning', 'N', '0', 'admin', now(), '', NULL, '设备升级状态--跳过');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (346, 29, '设备版本下载接口', 'CLIENT_VERSION_DOWNLOAD', 'http_interface', '', '', 'N', '0', 'admin', now(), 'admin', now(), 'HTTP交易接口--设备版本下载接口');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (348, 1, '待执行', '1', 'device_upgrade_result', '', 'info', 'Y', '0', 'admin', now(), 'admin', now(), '设备升级任务结果--待执行');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (349, 2, '成功', '2', 'device_upgrade_result', NULL, 'success', 'Y', '0', 'admin', now(), '', NULL, '设备升级任务结果--成功');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (350, 3, '失败', '3', 'device_upgrade_result', NULL, 'danger', 'Y', '0', 'admin', now(), '', NULL, '设备升级任务结果--失败');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (351, 4, '跳过', '4', 'device_upgrade_result', NULL, 'warning', 'Y', '0', 'admin', now(), '', NULL, '设备升级任务结果--跳过');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (352, 1, '短信', '1', 'msg_notice_method', '', 'info', 'Y', '0', 'admin', now(), 'admin', now(), '消息通知方式--短信');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (353, 2, '邮件', '2', 'msg_notice_method', NULL, 'success', 'N', '0', 'admin', now(), '', NULL, '消息通知方式--邮件');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (354, 3, '微信', '3', 'msg_notice_method', '', 'primary', 'N', '0', 'admin', now(), 'admin', now(), '消息通知方式--微信');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (355, 4, '钉钉', '4', 'msg_notice_method', NULL, 'warning', 'N', '0', 'admin', now(), '', NULL, '消息通知方式--钉钉');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (356, 1, '普通发送', '1', 'msg_type', '', 'primary', 'Y', '0', 'admin', now(), 'admin', now(), '消息类型--普通发送');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (357, 2, '模板发送', '2', 'msg_type', '', 'success', 'N', '0', 'admin', now(), 'admin', now(), '消息类型--模板发送');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (358, 1, '成功', '0', 'msg_result', '', 'primary', 'Y', '0', 'admin', now(), 'admin', now(), '消息推送结果--成功');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (359, 2, '失败', '1', 'msg_result', NULL, 'danger', 'N', '0', 'admin', now(), '', NULL, '消息推送结果--失败');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (360, 1, '文本邮件', '1', 'msg_mail_type', NULL, 'success', 'Y', '0', 'admin', now(), '', NULL, '邮件类型--文本邮件');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (361, 2, 'HTML邮件', '2', 'msg_mail_type', NULL, 'warning', 'N', '0', 'admin', now(), '', NULL, '邮件类型--HTML邮件');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (362, 1, '文本消息', 'text', 'msg_dingtalk_type', '', 'primary', 'Y', '0', 'admin', now(), 'admin', now(), '钉钉消息类型--文本消息');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (363, 2, 'Markdown消息', 'markdown', 'msg_dingtalk_type', NULL, 'success', 'N', '0', 'admin', now(), '', NULL, '钉钉消息类型--Markdown消息');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (364, 3, '链接消息', 'link', 'msg_dingtalk_type', NULL, 'warning', 'N', '0', 'admin', now(), '', NULL, '钉钉消息类型--链接消息');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (365, 1, '图片文件', 'image', 'msg_dingtalk_media_type', '', 'success', 'Y', '0', 'admin', now(), 'admin', now(), '钉钉媒体文件类型--图片文件');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (366, 2, '语音文件', 'voice', 'msg_dingtalk_media_type', NULL, 'primary', 'N', '0', 'admin', now(), '', NULL, '钉钉媒体文件类型--语音文件');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (367, 3, '普通文件', 'file', 'msg_dingtalk_media_type', NULL, 'warning', 'N', '0', 'admin', now(), '', NULL, '钉钉媒体文件类型--普通文件');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (368, 4, '图片消息', 'image', 'msg_dingtalk_type', NULL, 'info', 'N', '0', 'admin', now(), '', NULL, '钉钉消息类型--图片消息');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (369, 5, '语音消息', 'voice', 'msg_dingtalk_type', NULL, 'success', 'N', '0', 'admin', now(), '', NULL, '钉钉消息类型--语音消息');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (370, 6, '普通文件消息', 'file', 'msg_dingtalk_type', NULL, 'success', 'N', '0', 'admin', now(), '', NULL, '钉钉消息类型--普通文件消息');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (371, 31, '钉钉推送功能', 'MESSAGE_SEND_DING', 'http_interface', NULL, NULL, 'N', '0', 'admin', now(), '', NULL, 'HTTP交易接口--钉钉推送功能');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (372, 32, '钉钉推送结果查询', 'MESSAGE_SEND_DING_RESULT', 'http_interface', NULL, NULL, 'N', '0', 'admin', now(), '', NULL, 'HTTP交易接口--钉钉推送结果查询');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (373, 33, '钉钉媒体文件上传', 'MESSAGE_DING_MEDIA_UPLOAD', 'http_interface', NULL, NULL, 'N', '0', 'admin', now(), '', NULL, 'HTTP交易接口--钉钉媒体文件上传');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (374, 4, '实时同步', 'LIVEUPDATE', 'apply_data_source', NULL, 'primary', 'N', '0', 'admin', now(), '', NULL, '数据来源--数据实时同步');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (376, 35, '删除渠道库人员', 'DEL_CHANNEL_PERSON', 'http_interface', NULL, NULL, 'N', '0', 'admin', now(), '', NULL, 'HTTP交易接口--删除渠道库人员');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (377, 36, '查询渠道库人员是否存在', 'QUERY_CHANNEL_PERSON_EXISTS', 'http_interface', NULL, NULL, 'N', '0', 'admin', now(), '', NULL, 'HTTP交易接口--查询渠道库人员是否存在');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (378, 5, '三方同步', 'SYNC', 'apply_data_source', NULL, 'danger', 'N', '0', 'admin', now(), '', NULL, '数据来源--三方同步');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (379, 1, '后台创建', '1', 'device_create_method', NULL, 'primary', 'N', '0', 'admin', now(), '', NULL, '设备添加方式--后台创建');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (380, 2, '自主注册', '2', 'device_create_method', NULL, 'warning', 'N', '0', 'admin', now(), '', NULL, '设备添加方式--自主注册');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (381, 1, '在线', '1', 'device_online_state', NULL, 'primary', 'N', '0', 'admin', now(), '', NULL, '设备在线状态--在线');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (382, 2, '离线', '2', 'device_online_state', NULL, 'danger', 'N', '0', 'admin', now(), '', NULL, '设备在线状态--离线');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (383, 3, '后台导入', '3', 'device_create_method', NULL, 'danger', 'N', '0', 'admin', now(), '', NULL, '设备添加方式--后台导入');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (384, 1, '注册', '1', 'device_action_type', '', 'success', 'N', '0', 'admin', now(), 'admin', now(), '设备动作日志类型--注册');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (385, 2, '上线', '2', 'device_action_type', '', 'primary', 'N', '0', 'admin', now(), 'admin', now(), '设备动作日志类型--上线');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (386, 3, '下线', '3', 'device_action_type', NULL, 'danger', 'N', '0', 'admin', now(), '', NULL, '设备动作日志类型--下线');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (387, 4, '更新', '4', 'device_action_type', NULL, 'warning', 'N', '0', 'admin', now(), '', NULL, '设备动作日志类型--更新');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (389, 1, '成功', '0', 'device_config_result', NULL, 'primary', 'N', '0', 'admin', now(), '', NULL, '设备参数下发结果--成功');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (390, 2, '失败', '1', 'device_config_result', '', 'danger', 'N', '0', 'admin', now(), 'admin', now(), '设备参数下发结果--失败');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (391, 38, '获取指纹特征', 'PERSON_FINGER_FEATURE', 'http_interface', '', '', 'N', '0', 'admin', now(), 'admin', now(), 'HTTP交易接口--获取指纹特征');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (392, 39, '获取虹膜特征', 'PERSON_IRIS_FEATURE', 'http_interface', NULL, NULL, 'N', '0', 'admin', now(), '', NULL, 'HTTP交易接口--获取虹膜特征');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (393, 40, '人脸图片比对', 'PERSON_FACE_IMG_COMPARE', 'http_interface', NULL, NULL, 'N', '0', 'admin', now(), '', NULL, 'HTTP交易接口--人脸图片比对');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (394, 41, '指纹图片比对', 'PERSON_FINGER_IMG_COMPARE', 'http_interface', '', '', 'N', '0', 'admin', now(), 'admin', now(), 'HTTP交易接口--指纹图片比对');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (395, 42, '虹膜图片比对', 'PERSON_IRIS_IMG_COMPARE', 'http_interface', NULL, NULL, 'N', '0', 'admin', now(), '', NULL, 'HTTP交易接口--虹膜图片比对');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (396, 43, '人脸图片或视频检活', 'PERSON_FACE_CHECKLIVE', 'http_interface', NULL, NULL, 'N', '0', 'admin', now(), '', NULL, 'HTTP交易接口--人脸图片或视频检活');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (397, 44, '人脸图片质量检测', 'PERSON_FACE_QUALITY_DETECT', 'http_interface', NULL, NULL, 'N', '0', 'admin', now(), '', NULL, 'HTTP交易接口--人脸图片质量检测');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (398, 45, '指纹图片质量检测', 'PERSON_FINGER_QUALITY_DETECT', 'http_interface', NULL, NULL, 'N', '0', 'admin', now(), '', NULL, 'HTTP交易接口--指纹图片质量检测');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (409, 1, '正常', 'NORMAL', 'sys_tenant_state', NULL, NULL, 'N', '0', 'admin', now(), '', NULL, '租户状态--正常');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (410, 2, '冻结', 'FROZEN', 'sys_tenant_state', NULL, NULL, 'N', '0', 'admin', now(), 'admin', NULL, '租户状态--冻结');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (411, 1, '用户注册', 'REGIST', 'sys_tenant_source', NULL, NULL, 'N', '0', 'admin', now(), '', NULL, '租户来源--用户注册');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (412, 2, '后台创建', 'BG_CREATE', 'sys_tenant_source', NULL, NULL, 'N', '0', 'admin', now(), '', NULL, '租户来源--后台创建');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (413, 1, '虹膜人脸', '1', 'fusion_type', NULL, 'primary', 'Y', '0', 'admin', now(), '', NULL, '多模态');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (414, 2, '指纹指静脉', '2', 'fusion_type', NULL, 'warning', 'N', '0', 'admin', now(), '', NULL, '多模态');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (415, 1, '虹膜比对', '0', 'match_mode', NULL, 'primary', 'Y', '0', 'admin', now(), '', NULL, '多模态');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (416, 2, '人脸比对', '1', 'match_mode', NULL, 'warning', 'N', '0', 'admin', now(), '', NULL, '多模态');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (417, 3, '虹膜和人脸比对', '2', 'match_mode', NULL, 'warning', 'N', '0', 'admin', now(), '', NULL, '多模态');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (418, 4, '虹膜或人脸比对', '3', 'match_mode', NULL, 'warning', 'N', '0', 'admin', now(), '', NULL, '多模态');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (419, 5, '多模态比对', '4', 'match_mode', NULL, 'warning', 'N', '0', 'admin', now(), '', NULL, '多模态');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (424, 46, '人脸视频检活比对', 'PERSON_FACE_CHECKLIVE_AND_COMPARE', 'http_interface', NULL, NULL, 'N', '0', 'admin', now(), '', NULL, 'HTTP交易接口--人脸视频检活比对');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (425, 47, 'OCR识别接口', 'OCR', 'http_interface', NULL, NULL, 'N', '0', 'admin', now(), '', now(), 'HTTP交易接口--OCR识别接口');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (426, 48, '虹膜人脸多模态注册', 'MULIT_IRIS_FACE_REGISTER', 'http_interface', '', 'default', 'Y', '0', 'admin', now(), 'admin', now(), 'http标准接口-多模态人脸虹膜注册');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (427, 49, '虹膜人脸多模态1vN', 'MULIT_IRIS_FACE_SEARCH', 'http_interface', NULL, 'default', 'Y', '0', 'admin', now(), 'admin', now(), 'http标准接口-多模态人脸虹膜搜索');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (428, 50, '分库操作', 'SUB_TREASURY_OPERATE', 'http_interface', NULL, NULL, 'Y', '0', 'admin', now(), '', NULL, NULL);
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (429, 51, '设备查询', 'QUREY_DEVICE', 'http_interface', NULL, NULL, 'Y', '0', 'admin', now(), '', NULL, '中税设备查询');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (430, 52, '人脸识别日志查询', 'PERSON_FACE_SEARCH_LOG_QUERY', 'http_interface', NULL, NULL, 'N', '0', 'admin', now(), '', NULL, 'HTTP接口--人脸识别日志查询');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (431, 1, 'shandong', 'https://jd.51yanhome.com:18088/api/v1/ss/check/in', 'healthcode.request.url', '', 'primary', 'Y', '0', 'admin', now(), 'admin', now(), '健康码');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (432, 49, '健康码查询', 'HEALTH_CODE_SEARCH', 'http_interface', NULL, 'default', 'Y', '0', 'admin', now(), 'admin', now(), 'http标准接口-健康码查询');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (433, 53, '开通人脸虹膜多模态', 'PERSON_FACE_IRIS_OPEN', 'http_interface', NULL, NULL, 'N', '0', 'admin', now(), '', NULL, 'HTTP交易接口--开通人脸虹膜多模态');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (434, 54, '关闭人脸虹膜多模态', 'PERSON_FACE_IRIS_CLOSE', 'http_interface', NULL, NULL, 'N', '0', 'admin', now(), '', NULL, 'HTTP交易接口--关闭人脸虹膜多模态');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (435, 55, '人脸1v1比对日志回传', 'PERSON_FACE_MATCH_LOG_BAK', 'http_interface', NULL, NULL, 'N', '0', 'admin', now(), '', NULL, 'HTTP交易接口--人脸1v1比对日志回传');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (436, 56, '人脸虹膜多模态识别日志回传', 'PERSON_FACE_IRIS_MULTI_LOG_BAK', 'http_interface', NULL, NULL, 'N', '0', 'admin', now(), '', NULL, 'HTTP交易接口--人脸虹膜多模态识别日志回传');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (437, 1, '人脸SDK', 'FACE', 'sys_sdk_type', NULL, 'success', 'Y', '0', 'admin', now(), '', NULL, 'SDK类型--人脸SDK');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (438, 2, '指纹SDK', 'FINGER', 'sys_sdk_type', NULL, 'success', 'Y', '0', 'admin', now(), '', NULL, 'SDK类型--指纹SDK');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (439, 3, '虹膜SDK', 'IRIS', 'sys_sdk_type', NULL, 'success', 'Y', '0', 'admin', now(), '', NULL, 'SDK类型--虹膜SDK');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (440, 4, '指静脉SDK', 'FVEIN', 'sys_sdk_type', NULL, 'success', 'Y', '0', 'admin', now(), '', NULL, 'SDK类型--指静脉SDK');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (441, 5, '多模态SDK', 'MULTI', 'sys_sdk_type', NULL, 'success', 'Y', '0', 'admin', now(), '', NULL, 'SDK类型--多模态SDK');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (442, 57, '人脸虹膜多模态识别日志查询', 'PERSON_FACE_IRIS_MULTI_LOG_QUERY', 'http_interface', NULL, NULL, 'N', '0', 'admin', now(), '', NULL, 'HTTP接口--人脸虹膜多模态识别日志查询');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (444, 1, '常规设备', '1', 'client_device_type', NULL, NULL, 'N', '0', 'admin', now(), '', NULL, '设备类型--常规设备');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (445, 2, '无感设备', '2', 'client_device_type', NULL, NULL, 'N', '0', 'admin', now(), '', NULL, '设备类型--无感设备');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (446, 3, '高空坠物设备', '3', 'client_device_type', NULL, NULL, 'N', '0', 'admin', now(), '', NULL, '设备类型--高空坠物设备');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (448, 59, '联网身份核查接口', 'PERSON_IDENTITY_VERIFICATION', 'http_interface', NULL, NULL, 'N', '0', 'admin', now(), '', NULL, 'HTTP接口--联网身份核查接口');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (449, 4, '考勤设备', '4', 'client_device_type', NULL, NULL, 'N', '0', 'admin', now(), '', NULL, '设备类型--考勤设备');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (450, 1, '进', 'IN', 'device_direction', NULL, NULL, 'N', '0', 'admin', now(), '', NULL, '设备进出方向--进');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (451, 2, '出', 'OUT', 'device_direction', NULL, NULL, 'N', '0', 'admin', now(), '', NULL, '设备进出方向--出');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (452, 3, '未知', 'UNKNOWN', 'device_direction', NULL, NULL, 'N', '0', 'admin', now(), 'admin', now(), '设备进出方向--未知');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (453, 60, '人脸1v1比对日志查询', 'PERSON_FACE_MATCH_LOG_QUERY', 'http_interface', NULL, NULL, 'N', '0', 'admin', now(), '', NULL, 'HTTP交易接口--人脸1v1比对日志查询');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (454, 5, '人脸虹膜多模态', '4', 'channel_bio_attest_type', NULL, NULL, 'N', '0', 'admin', now(), '', NULL, '人脸虹膜多模态');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (455, 0, '试用', 'TRIAL', 'sys_tenant_type', NULL, NULL, 'N', '0', 'admin', now(), 'admin', now(), '租户类型--试用租户');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (456, 1, '正式', 'FORMAL', 'sys_tenant_type', NULL, NULL, 'N', '0', 'admin', now(), 'admin', now(), '租户类型--正式租户');
SELECT setval('sys_dict_data_dict_code_seq', 146, true);
SELECT setval('sys_dict_type_dict_id_seq', 456, true);



INSERT INTO device_model(id, model_code, model_name, exterior_image, model_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355692391', 'ECF201', 'ECF201', NULL, '人脸设备', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_model(id, model_code, model_name, exterior_image, model_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355692392', 'ECF203', 'ECF203', NULL, '人脸设备', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_model(id, model_code, model_name, exterior_image, model_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355692393', 'ECX332', 'ECX332', NULL, '人脸虹膜多模态设备', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_model(id, model_code, model_name, exterior_image, model_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355692394', 'ECX333', 'ECX333', NULL, '带测温和刷卡的人脸虹膜多模态设备', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_model(id, model_code, model_name, exterior_image, model_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355692395', 'ECF106', 'ECF106', NULL, '网络抓拍机', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_model(id, model_code, model_name, exterior_image, model_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355692396', 'Atlas500', 'Atlas500', NULL, 'Atlas500', NULL, NULL, now(), NULL, 'super');


INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691327', 'appManagePwd', 'APP管理密码', 'APP管理密码', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691328', 'keyAlgorithm', '算法激活码', '算法激活码', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691329', 'httpBaseUrl', '接口地址', '接口地址', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691330', 'appKey', 'appKey', 'appKey', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691331', 'appSecret', 'appSecret', 'appSecret', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691332', 'workMode', '识别模式', '识别模式(0：智能模式 1：刷脸模式 2：身份证模式 3：只测温模式)', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691334', 'temperatureCheck', '测温开关', '测温开关(true：开启测温 false：关闭测温)', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691335', 'liveCheck', '检活开关', '检活开关(true：开启检活 false：关闭检活)', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691336', 'matchScore', '比对阈值', '比对阈值(取值范围0-100)', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691337', 'repeatTime', '去重时间', '去重时间(取值范围3-12)', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691338', 'maxFaceSize', '最大人脸', '最大人脸', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691339', 'minFaceSize', '最小人脸', '最小人脸', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691340', 'idVerificationCheck', '人证核验开关', '人证核验开关(true：开启核验 false：关闭核)', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691341', 'personIdcardThreshold', '人证核验阈值', '人证核验阈值(取值范围10-85)', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691342', 'healthCodeCheck', '健康码核验开关', '健康码核验开关(true：开启健康码核验 false：不核验健康码)', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691343', 'comparisonLib', '是否只识别库内人员', '是否只识别库内人员(true：只识别库内人员 false：识别所有人员)', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691344', 'healthDeviceCode', '健康码设备编号', '健康码设备编号', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691345', 'healthAddress', '健康码地址', '健康码地址', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691348', 'deviceStatus', '设备状态', '设备状态(0:禁用 1:启用)', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691349', 'irisMatchScore', '	虹膜比对阈值', '虹膜1:N比对阈值', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691350', 'faceMatchScore', '人脸比对阈值', '人脸1:N比对阈值', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691351', 'multiMatchScore', '多模态比对阈值', '多模态1:N比对阈值', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691352', 'multiScore', '多模态分值', '多模态分值', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691354', 'matchMode', '比对模式', '比对模式(0:后台比对 1:本地比对 2: 二次比对)', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691355', 'matchType', '比对方式', '比对方式(0:只虹膜比对 1:只人脸比对 2:虹膜和人脸同时别对 3:人脸或虹膜 4:多模态)', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691356', 'frameSkip', 'RTSP解析跳帧', 'RTSP解析跳帧', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691357', 'platformSyncFlag', '日志上传标记', '日志上传标记(0:实时 1:匀速 2:关闭)', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691358', 'platformSyncTime', '日志上传匀速间隔时间', '日志上传匀速间隔时间	', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691359', 'streamUrl', 'RTSP地址', 'RTSP地址', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_param_info(id, param_code, param_name, param_desc, create_by, update_by, create_time, update_time, tenant_id) VALUES ('199839355691360', 'timeUrl', '时间服务器', '时间服务器(0:NTP服务器 1:自建API)', NULL, NULL, now(), NULL, 'super');

INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203001942171968', 'ECF201', 'keyAlgorithm', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203001942179136', 'ECF201', 'httpBaseUrl', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203001942184256', 'ECF201', 'appKey', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203001942188352', 'ECF201', 'appSecret', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203001942192448', 'ECF201', 'workMode', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203001942195520', 'ECF201', 'temperatureCheck', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203001942199616', 'ECF201', 'liveCheck', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203001942202688', 'ECF201', 'matchScore', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203001942205760', 'ECF201', 'repeatTime', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203001942208832', 'ECF201', 'maxFaceSize', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203001942212928', 'ECF201', 'minFaceSize', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203001942216000', 'ECF201', 'idVerificationCheck', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203001942219072', 'ECF201', 'personIdcardThreshold', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203001942223168', 'ECF201', 'healthCodeCheck', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203001942226240', 'ECF201', 'comparisonLib', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203001942230336', 'ECF201', 'healthAddress', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203001942233408', 'ECF201', 'matchMode', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203002096651584', 'ECX332', 'httpBaseUrl', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203002096657728', 'ECX332', 'appKey', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203002096661824', 'ECX332', 'appSecret', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203002096664896', 'ECX332', 'deviceStatus', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203002096667968', 'ECX332', 'irisMatchScore', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203002096671040', 'ECX332', 'faceMatchScore', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203002096675136', 'ECX332', 'multiMatchScore', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203002096677184', 'ECX332', 'multiScore', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203002096680256', 'ECX332', 'matchMode', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203002096683328', 'ECX332', 'matchType', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203002179989824', 'ECX333', 'httpBaseUrl', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203002179996992', 'ECX333', 'appKey', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203002180000064', 'ECX333', 'appSecret', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203002180003136', 'ECX333', 'deviceStatus', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203002180006208', 'ECX333', 'irisMatchScore', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203002180011328', 'ECX333', 'faceMatchScore', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203002180014400', 'ECX333', 'multiMatchScore', NULL, NULL, now(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203002180018496', 'ECX333', 'multiScore', NULL, NULL,now(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203002180021568', 'ECX333', 'matchMode', NULL, NULL,now(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203002180024640', 'ECX333', 'matchType', NULL, NULL,now(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203002278398272', 'Atlas500', 'httpBaseUrl', NULL, NULL,now(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203002278404416', 'Atlas500', 'appKey', NULL, NULL,now(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203002278407488', 'Atlas500', 'appSecret', NULL, NULL,now(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203002278410560', 'Atlas500', 'matchScore', NULL, NULL,now(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203002278413632', 'Atlas500', 'repeatTime', NULL, NULL,now(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203002278416704', 'Atlas500', 'maxFaceSize', NULL, NULL,now(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203002278420800', 'Atlas500', 'minFaceSize', NULL, NULL,now(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203002278423872', 'Atlas500', 'frameSkip', NULL, NULL,now(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203002278426944', 'Atlas500', 'platformSyncFlag', NULL, NULL,now(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203002278431040', 'Atlas500', 'platformSyncTime', NULL, NULL,now(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203002278434112', 'Atlas500', 'streamUrl', NULL, NULL,now(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203002278437184', 'Atlas500', 'timeUrl', NULL, NULL,now(), NULL, 'super');
INSERT INTO device_param_model_rel(id, model_code, param_code, create_by, update_by, create_time, update_time, tenant_id) VALUES ('203002278437185', 'ECF201', 'appManagePwd', NULL, NULL, now(), NULL, 'super');


INSERT INTO sys_job(job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, update_by, update_time, remark) VALUES (9, '虹膜1vN日志分区维护', 'DEFAULT', 'clearBioMatchLogTask.clearIrisSearchLog(365,false,''eyecool_assps'')', '0 0 1 * * ? *', '1', '1', '0', 'admin', now(), '', now(), '');
INSERT INTO sys_job(job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, update_by, update_time, remark) VALUES (3, '接口报文日志分区维护', 'DEFAULT', 'requestRecordTask.clearAndAddPartition(''eyecool_assps'',30)', '0 0 0 * * ? *', '1', '1', '0', 'admin', now(), 'admin', now(), '');
INSERT INTO sys_job(job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, update_by, update_time, remark) VALUES (4, '人脸1v1日志分区维护', 'DEFAULT', 'clearBioMatchLogTask.clearFaceMatchLog(365,false,''eyecool_assps'')', '0 0 1 * * ? *', '1', '1', '0', 'admin', now(), 'admin', now(), '');
INSERT INTO sys_job(job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, update_by, update_time, remark) VALUES (5, '人脸1vN日志分区维护', 'DEFAULT', 'clearBioMatchLogTask.clearFaceSearchLog(365,false,''eyecool_assps'')', '0 0 1 * * ? *', '1', '1', '0', 'admin', now(), 'admin', now(), '');
INSERT INTO sys_job(job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, update_by, update_time, remark) VALUES (6, '指纹1v1日志分区维护', 'DEFAULT', 'clearBioMatchLogTask.clearFingerMatchLog(365,false,''eyecool_assps'')', '0 0 1 * * ? *', '1', '1', '0', 'admin', now(), 'admin', now(), '');
INSERT INTO sys_job(job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, update_by, update_time, remark) VALUES (2, '设备升级任务自动发布', 'DEFAULT', 'deviceUpdatePublishTask.execute()', '0 0/2 * * * ? *', '1', '1', '1', 'admin', now(), 'admin', now(), '');
INSERT INTO sys_job(job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, update_by, update_time, remark) VALUES (7, '指纹1vN日志分区维护', 'DEFAULT', 'clearBioMatchLogTask.clearFingerSearchLog(365,false,''eyecool_assps'')', '0 0 1 * * ? *', '1', '1', '0', 'admin', now(), 'admin', now(), '');
INSERT INTO sys_job(job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, update_by, update_time, remark) VALUES (8, '虹膜1v1日志分区维护', 'DEFAULT', 'clearBioMatchLogTask.clearIrisMatchLog(365,false,''eyecool_assps'')', ' 0 0 1 * * ? *', '1', '1', '0', 'admin', now(), '', now(), '');
INSERT INTO sys_job(job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, update_by, update_time, remark) VALUES (1, '钉钉部门和人员信息同步', 'DEFAULT', 'syncDingTalkDataTask.executeSync()', '0 0 3 /1 * ? *', '1', '1', '0', 'admin', now(), '', now(), '');
INSERT INTO sys_job(job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, update_by, update_time, remark) VALUES (10, '人脸虹膜多模态1vN日志分区维护', 'DEFAULT', 'clearBioMatchLogTask.clearMulitSearchLog(365,false,''eyecool_assps'')', '0 0 1 * * ? *', '1', '1', '0', 'admin', now(), 'admin', now(), '');
INSERT INTO sys_job(job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, update_by, update_time, remark) VALUES (11, '健康码日志分区维护', 'DEFAULT', 'clearBioMatchLogTask.clearHealthcodeLog(365,''eyecool_assps'')', '0 0 1 * * ? *', '1', '1', '0', 'admin', now(), 'admin', now(), '');
INSERT INTO sys_job(job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, update_by, update_time, remark) VALUES (12, '消息日志分区维护', 'DEFAULT', 'msgLogTask.clearAndAddPartition(''eyecool_assps'',365)', '0 0 1 * * ? *', '1', '1', '0', 'admin', now(), '', now(), '');

INSERT INTO qrtz_job_details(sched_name, job_name, job_group, description, job_class_name, is_durable, is_nonconcurrent, is_update_data, requests_recovery, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME1', 'DEFAULT', NULL, 'cn.eyecool.quartz.util.QuartzDisallowConcurrentExecution', 'false', 'true', 'false', 'false', E'\\254\\355\\000\\005sr\\000\\025org.quartz.JobDataMap\\237\\260\\203\\350\\277\\251\\260\\313\\002\\000\\000xr\\000&org.quartz.utils.StringKeyDirtyFlagMap\\202\\010\\350\\303\\373\\305](\\002\\000\\001Z\\000\\023allowsTransientDataxr\\000\\035org.quartz.utils.DirtyFlagMap\\023\\346.\\255(v\\012\\316\\002\\000\\002Z\\000\\005dirtyL\\000\\003mapt\\000\\017Ljava/util/Map;xp\\001sr\\000\\021java.util.HashMap\\005\\007\\332\\301\\303\\026\\321\\003\\000\\002F\\000\\012loadFactorI\\000\\011thresholdxp?@\\000\\000\\000\\000\\000\\014w\\010\\000\\000\\000\\020\\000\\000\\000\\001t\\000\\017TASK_PROPERTIESsr\\000\\037cn.eyecool.quartz.domain.SysJob\\000\\000\\000\\000\\000\\000\\000\\001\\002\\000\\010L\\000\\012concurrentt\\000\\022Ljava/lang/String;L\\000\\016cronExpressionq\\000~\\000\\011L\\000\\014invokeTargetq\\000~\\000\\011L\\000\\010jobGroupq\\000~\\000\\011L\\000\\005jobIdt\\000\\020Ljava/lang/Long;L\\000\\007jobNameq\\000~\\000\\011L\\000\\015misfirePolicyq\\000~\\000\\011L\\000\\006statusq\\000~\\000\\011xr\\000(cn.eyecool.common.core.domain.BaseEntity\\000\\000\\000\\000\\000\\000\\000\\001\\002\\000\\011L\\000\\011beginTimeq\\000~\\000\\011L\\000\\010createByq\\000~\\000\\011L\\000\\012createTimet\\000\\020Ljava/util/Date;L\\000\\007endTimeq\\000~\\000\\011L\\000\\006paramsq\\000~\\000\\003L\\000\\006remarkq\\000~\\000\\011L\\000\\013searchValueq\\000~\\000\\011L\\000\\010updateByq\\000~\\000\\011L\\000\\012updateTimeq\\000~\\000\\014xppt\\000\\005adminpppppppt\\000\\0011t\\000\\0160 0 3 /1 * ? *t\\000syncDingTalkDataTask.executeSync()t\\000\\007DEFAULTsr\\000\\016java.lang.Long;\\213\\344\\220\\314\\217#\\337\\002\\000\\001J\\000\\005valuexr\\000\\020java.lang.Number\\206\\254\\225\\035\\013\\224\\340\\213\\002\\000\\000xp\\000\\000\\000\\000\\000\\000\\000\\001t\\000!\\351\\222\\211\\351\\222\\211\\351\\203\\250\\351\\227\\250\\345\\222\\214\\344\\272\\272\\345\\221\\230\\344\\277\\241\\346\\201\\257\\345\\220\\214\\346\\255\\245t\\000\\0011t\\000\\0011x\\000');
INSERT INTO qrtz_job_details(sched_name, job_name, job_group, description, job_class_name, is_durable, is_nonconcurrent, is_update_data, requests_recovery, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME8', 'DEFAULT', NULL, 'cn.eyecool.quartz.util.QuartzDisallowConcurrentExecution', 'false', 'true', 'false', 'false', E'\\254\\355\\000\\005sr\\000\\025org.quartz.JobDataMap\\237\\260\\203\\350\\277\\251\\260\\313\\002\\000\\000xr\\000&org.quartz.utils.StringKeyDirtyFlagMap\\202\\010\\350\\303\\373\\305](\\002\\000\\001Z\\000\\023allowsTransientDataxr\\000\\035org.quartz.utils.DirtyFlagMap\\023\\346.\\255(v\\012\\316\\002\\000\\002Z\\000\\005dirtyL\\000\\003mapt\\000\\017Ljava/util/Map;xp\\001sr\\000\\021java.util.HashMap\\005\\007\\332\\301\\303\\026\\321\\003\\000\\002F\\000\\012loadFactorI\\000\\011thresholdxp?@\\000\\000\\000\\000\\000\\014w\\010\\000\\000\\000\\020\\000\\000\\000\\001t\\000\\017TASK_PROPERTIESsr\\000\\037cn.eyecool.quartz.domain.SysJob\\000\\000\\000\\000\\000\\000\\000\\001\\002\\000\\010L\\000\\012concurrentt\\000\\022Ljava/lang/String;L\\000\\016cronExpressionq\\000~\\000\\011L\\000\\014invokeTargetq\\000~\\000\\011L\\000\\010jobGroupq\\000~\\000\\011L\\000\\005jobIdt\\000\\020Ljava/lang/Long;L\\000\\007jobNameq\\000~\\000\\011L\\000\\015misfirePolicyq\\000~\\000\\011L\\000\\006statusq\\000~\\000\\011xr\\000(cn.eyecool.common.core.domain.BaseEntity\\000\\000\\000\\000\\000\\000\\000\\001\\002\\000\\011L\\000\\011beginTimeq\\000~\\000\\011L\\000\\010createByq\\000~\\000\\011L\\000\\012createTimet\\000\\020Ljava/util/Date;L\\000\\007endTimeq\\000~\\000\\011L\\000\\006paramsq\\000~\\000\\003L\\000\\006remarkq\\000~\\000\\011L\\000\\013searchValueq\\000~\\000\\011L\\000\\010updateByq\\000~\\000\\011L\\000\\012updateTimeq\\000~\\000\\014xppt\\000\\005adminpppppppt\\000\\0011t\\000\\016 0 0 1 * * ? *t\\000AclearBioMatchLogTask.clearIrisMatchLog(365,false,''eyecool_assps'')t\\000\\007DEFAULTsr\\000\\016java.lang.Long;\\213\\344\\220\\314\\217#\\337\\002\\000\\001J\\000\\005valuexr\\000\\020java.lang.Number\\206\\254\\225\\035\\013\\224\\340\\213\\002\\000\\000xp\\000\\000\\000\\000\\000\\000\\000\\010t\\000\\033\\350\\231\\271\\350\\206\\2341v1\\346\\227\\245\\345\\277\\227\\345\\210\\206\\345\\214\\272\\347\\273\\264\\346\\212\\244t\\000\\0011t\\000\\0011x\\000');
INSERT INTO qrtz_job_details(sched_name, job_name, job_group, description, job_class_name, is_durable, is_nonconcurrent, is_update_data, requests_recovery, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME7', 'DEFAULT', NULL, 'cn.eyecool.quartz.util.QuartzDisallowConcurrentExecution', 'false', 'true', 'false', 'false', E'\\254\\355\\000\\005sr\\000\\025org.quartz.JobDataMap\\237\\260\\203\\350\\277\\251\\260\\313\\002\\000\\000xr\\000&org.quartz.utils.StringKeyDirtyFlagMap\\202\\010\\350\\303\\373\\305](\\002\\000\\001Z\\000\\023allowsTransientDataxr\\000\\035org.quartz.utils.DirtyFlagMap\\023\\346.\\255(v\\012\\316\\002\\000\\002Z\\000\\005dirtyL\\000\\003mapt\\000\\017Ljava/util/Map;xp\\001sr\\000\\021java.util.HashMap\\005\\007\\332\\301\\303\\026\\321\\003\\000\\002F\\000\\012loadFactorI\\000\\011thresholdxp?@\\000\\000\\000\\000\\000\\014w\\010\\000\\000\\000\\020\\000\\000\\000\\001t\\000\\017TASK_PROPERTIESsr\\000\\037cn.eyecool.quartz.domain.SysJob\\000\\000\\000\\000\\000\\000\\000\\001\\002\\000\\010L\\000\\012concurrentt\\000\\022Ljava/lang/String;L\\000\\016cronExpressionq\\000~\\000\\011L\\000\\014invokeTargetq\\000~\\000\\011L\\000\\010jobGroupq\\000~\\000\\011L\\000\\005jobIdt\\000\\020Ljava/lang/Long;L\\000\\007jobNameq\\000~\\000\\011L\\000\\015misfirePolicyq\\000~\\000\\011L\\000\\006statusq\\000~\\000\\011xr\\000(cn.eyecool.common.core.domain.BaseEntity\\000\\000\\000\\000\\000\\000\\000\\001\\002\\000\\011L\\000\\011beginTimeq\\000~\\000\\011L\\000\\010createByq\\000~\\000\\011L\\000\\012createTimet\\000\\020Ljava/util/Date;L\\000\\007endTimeq\\000~\\000\\011L\\000\\006paramsq\\000~\\000\\003L\\000\\006remarkq\\000~\\000\\011L\\000\\013searchValueq\\000~\\000\\011L\\000\\010updateByq\\000~\\000\\011L\\000\\012updateTimeq\\000~\\000\\014xppt\\000\\005adminsr\\000\\016java.util.Datehj\\201\\001KYt\\031\\003\\000\\000xpw\\010\\000\\000\\001y\\345\\333\\216\\320xpsr\\000\\027java.util.LinkedHashMap4\\300N\\\\\\020l\\300\\373\\002\\000\\001Z\\000\\013accessOrderxq\\000~\\000\\005?@\\000\\000\\000\\000\\000\\000w\\010\\000\\000\\000\\020\\000\\000\\000\\000x\\000t\\000\\000pt\\000\\005adminpt\\000\\0011t\\000\\0150 0 1 * * ? *t\\000DclearBioMatchLogTask.clearFingerSearchLog(365,false,''eyecool_assps'')t\\000\\007DEFAULTsr\\000\\016java.lang.Long;\\213\\344\\220\\314\\217#\\337\\002\\000\\001J\\000\\005valuexr\\000\\020java.lang.Number\\206\\254\\225\\035\\013\\224\\340\\213\\002\\000\\000xp\\000\\000\\000\\000\\000\\000\\000\\007t\\000\\033\\346\\214\\207\\347\\272\\2711vN\\346\\227\\245\\345\\277\\227\\345\\210\\206\\345\\214\\272\\347\\273\\264\\346\\212\\244t\\000\\0011t\\000\\0011x\\000');
INSERT INTO qrtz_job_details(sched_name, job_name, job_group, description, job_class_name, is_durable, is_nonconcurrent, is_update_data, requests_recovery, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME6', 'DEFAULT', NULL, 'cn.eyecool.quartz.util.QuartzDisallowConcurrentExecution', 'false', 'true', 'false', 'false', E'\\254\\355\\000\\005sr\\000\\025org.quartz.JobDataMap\\237\\260\\203\\350\\277\\251\\260\\313\\002\\000\\000xr\\000&org.quartz.utils.StringKeyDirtyFlagMap\\202\\010\\350\\303\\373\\305](\\002\\000\\001Z\\000\\023allowsTransientDataxr\\000\\035org.quartz.utils.DirtyFlagMap\\023\\346.\\255(v\\012\\316\\002\\000\\002Z\\000\\005dirtyL\\000\\003mapt\\000\\017Ljava/util/Map;xp\\001sr\\000\\021java.util.HashMap\\005\\007\\332\\301\\303\\026\\321\\003\\000\\002F\\000\\012loadFactorI\\000\\011thresholdxp?@\\000\\000\\000\\000\\000\\014w\\010\\000\\000\\000\\020\\000\\000\\000\\001t\\000\\017TASK_PROPERTIESsr\\000\\037cn.eyecool.quartz.domain.SysJob\\000\\000\\000\\000\\000\\000\\000\\001\\002\\000\\010L\\000\\012concurrentt\\000\\022Ljava/lang/String;L\\000\\016cronExpressionq\\000~\\000\\011L\\000\\014invokeTargetq\\000~\\000\\011L\\000\\010jobGroupq\\000~\\000\\011L\\000\\005jobIdt\\000\\020Ljava/lang/Long;L\\000\\007jobNameq\\000~\\000\\011L\\000\\015misfirePolicyq\\000~\\000\\011L\\000\\006statusq\\000~\\000\\011xr\\000(cn.eyecool.common.core.domain.BaseEntity\\000\\000\\000\\000\\000\\000\\000\\001\\002\\000\\011L\\000\\011beginTimeq\\000~\\000\\011L\\000\\010createByq\\000~\\000\\011L\\000\\012createTimet\\000\\020Ljava/util/Date;L\\000\\007endTimeq\\000~\\000\\011L\\000\\006paramsq\\000~\\000\\003L\\000\\006remarkq\\000~\\000\\011L\\000\\013searchValueq\\000~\\000\\011L\\000\\010updateByq\\000~\\000\\011L\\000\\012updateTimeq\\000~\\000\\014xppt\\000\\005adminsr\\000\\016java.util.Datehj\\201\\001KYt\\031\\003\\000\\000xpw\\010\\000\\000\\001y\\345\\333-(xpsr\\000\\027java.util.LinkedHashMap4\\300N\\\\\\020l\\300\\373\\002\\000\\001Z\\000\\013accessOrderxq\\000~\\000\\005?@\\000\\000\\000\\000\\000\\000w\\010\\000\\000\\000\\020\\000\\000\\000\\000x\\000t\\000\\000pt\\000\\005adminpt\\000\\0011t\\000\\0150 0 1 * * ? *t\\000CclearBioMatchLogTask.clearFingerMatchLog(365,false,''eyecool_assps'')t\\000\\007DEFAULTsr\\000\\016java.lang.Long;\\213\\344\\220\\314\\217#\\337\\002\\000\\001J\\000\\005valuexr\\000\\020java.lang.Number\\206\\254\\225\\035\\013\\224\\340\\213\\002\\000\\000xp\\000\\000\\000\\000\\000\\000\\000\\006t\\000\\033\\346\\214\\207\\347\\272\\2711v1\\346\\227\\245\\345\\277\\227\\345\\210\\206\\345\\214\\272\\347\\273\\264\\346\\212\\244t\\000\\0011t\\000\\0011x\\000');
INSERT INTO qrtz_job_details(sched_name, job_name, job_group, description, job_class_name, is_durable, is_nonconcurrent, is_update_data, requests_recovery, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME5', 'DEFAULT', NULL, 'cn.eyecool.quartz.util.QuartzDisallowConcurrentExecution', 'false', 'true', 'false', 'false', E'\\254\\355\\000\\005sr\\000\\025org.quartz.JobDataMap\\237\\260\\203\\350\\277\\251\\260\\313\\002\\000\\000xr\\000&org.quartz.utils.StringKeyDirtyFlagMap\\202\\010\\350\\303\\373\\305](\\002\\000\\001Z\\000\\023allowsTransientDataxr\\000\\035org.quartz.utils.DirtyFlagMap\\023\\346.\\255(v\\012\\316\\002\\000\\002Z\\000\\005dirtyL\\000\\003mapt\\000\\017Ljava/util/Map;xp\\001sr\\000\\021java.util.HashMap\\005\\007\\332\\301\\303\\026\\321\\003\\000\\002F\\000\\012loadFactorI\\000\\011thresholdxp?@\\000\\000\\000\\000\\000\\014w\\010\\000\\000\\000\\020\\000\\000\\000\\001t\\000\\017TASK_PROPERTIESsr\\000\\037cn.eyecool.quartz.domain.SysJob\\000\\000\\000\\000\\000\\000\\000\\001\\002\\000\\010L\\000\\012concurrentt\\000\\022Ljava/lang/String;L\\000\\016cronExpressionq\\000~\\000\\011L\\000\\014invokeTargetq\\000~\\000\\011L\\000\\010jobGroupq\\000~\\000\\011L\\000\\005jobIdt\\000\\020Ljava/lang/Long;L\\000\\007jobNameq\\000~\\000\\011L\\000\\015misfirePolicyq\\000~\\000\\011L\\000\\006statusq\\000~\\000\\011xr\\000(cn.eyecool.common.core.domain.BaseEntity\\000\\000\\000\\000\\000\\000\\000\\001\\002\\000\\011L\\000\\011beginTimeq\\000~\\000\\011L\\000\\010createByq\\000~\\000\\011L\\000\\012createTimet\\000\\020Ljava/util/Date;L\\000\\007endTimeq\\000~\\000\\011L\\000\\006paramsq\\000~\\000\\003L\\000\\006remarkq\\000~\\000\\011L\\000\\013searchValueq\\000~\\000\\011L\\000\\010updateByq\\000~\\000\\011L\\000\\012updateTimeq\\000~\\000\\014xppt\\000\\005adminsr\\000\\016java.util.Datehj\\201\\001KYt\\031\\003\\000\\000xpw\\010\\000\\000\\001y\\345\\332\\323Pxpsr\\000\\027java.util.LinkedHashMap4\\300N\\\\\\020l\\300\\373\\002\\000\\001Z\\000\\013accessOrderxq\\000~\\000\\005?@\\000\\000\\000\\000\\000\\000w\\010\\000\\000\\000\\020\\000\\000\\000\\000x\\000t\\000\\000pt\\000\\005adminpt\\000\\0011t\\000\\0150 0 1 * * ? *t\\000BclearBioMatchLogTask.clearFaceSearchLog(365,false,''eyecool_assps'')t\\000\\007DEFAULTsr\\000\\016java.lang.Long;\\213\\344\\220\\314\\217#\\337\\002\\000\\001J\\000\\005valuexr\\000\\020java.lang.Number\\206\\254\\225\\035\\013\\224\\340\\213\\002\\000\\000xp\\000\\000\\000\\000\\000\\000\\000\\005t\\000\\033\\344\\272\\272\\350\\204\\2701vN\\346\\227\\245\\345\\277\\227\\345\\210\\206\\345\\214\\272\\347\\273\\264\\346\\212\\244t\\000\\0011t\\000\\0011x\\000');
INSERT INTO qrtz_job_details(sched_name, job_name, job_group, description, job_class_name, is_durable, is_nonconcurrent, is_update_data, requests_recovery, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME4', 'DEFAULT', NULL, 'cn.eyecool.quartz.util.QuartzDisallowConcurrentExecution', 'false', 'true', 'false', 'false', E'\\254\\355\\000\\005sr\\000\\025org.quartz.JobDataMap\\237\\260\\203\\350\\277\\251\\260\\313\\002\\000\\000xr\\000&org.quartz.utils.StringKeyDirtyFlagMap\\202\\010\\350\\303\\373\\305](\\002\\000\\001Z\\000\\023allowsTransientDataxr\\000\\035org.quartz.utils.DirtyFlagMap\\023\\346.\\255(v\\012\\316\\002\\000\\002Z\\000\\005dirtyL\\000\\003mapt\\000\\017Ljava/util/Map;xp\\001sr\\000\\021java.util.HashMap\\005\\007\\332\\301\\303\\026\\321\\003\\000\\002F\\000\\012loadFactorI\\000\\011thresholdxp?@\\000\\000\\000\\000\\000\\014w\\010\\000\\000\\000\\020\\000\\000\\000\\001t\\000\\017TASK_PROPERTIESsr\\000\\037cn.eyecool.quartz.domain.SysJob\\000\\000\\000\\000\\000\\000\\000\\001\\002\\000\\010L\\000\\012concurrentt\\000\\022Ljava/lang/String;L\\000\\016cronExpressionq\\000~\\000\\011L\\000\\014invokeTargetq\\000~\\000\\011L\\000\\010jobGroupq\\000~\\000\\011L\\000\\005jobIdt\\000\\020Ljava/lang/Long;L\\000\\007jobNameq\\000~\\000\\011L\\000\\015misfirePolicyq\\000~\\000\\011L\\000\\006statusq\\000~\\000\\011xr\\000(cn.eyecool.common.core.domain.BaseEntity\\000\\000\\000\\000\\000\\000\\000\\001\\002\\000\\011L\\000\\011beginTimeq\\000~\\000\\011L\\000\\010createByq\\000~\\000\\011L\\000\\012createTimet\\000\\020Ljava/util/Date;L\\000\\007endTimeq\\000~\\000\\011L\\000\\006paramsq\\000~\\000\\003L\\000\\006remarkq\\000~\\000\\011L\\000\\013searchValueq\\000~\\000\\011L\\000\\010updateByq\\000~\\000\\011L\\000\\012updateTimeq\\000~\\000\\014xppt\\000\\005adminsr\\000\\016java.util.Datehj\\201\\001KYt\\031\\003\\000\\000xpw\\010\\000\\000\\001y\\345\\332q\\250xpsr\\000\\027java.util.LinkedHashMap4\\300N\\\\\\020l\\300\\373\\002\\000\\001Z\\000\\013accessOrderxq\\000~\\000\\005?@\\000\\000\\000\\000\\000\\000w\\010\\000\\000\\000\\020\\000\\000\\000\\000x\\000t\\000\\000pt\\000\\005adminpt\\000\\0011t\\000\\0150 0 1 * * ? *t\\000AclearBioMatchLogTask.clearFaceMatchLog(365,false,''eyecool_assps'')t\\000\\007DEFAULTsr\\000\\016java.lang.Long;\\213\\344\\220\\314\\217#\\337\\002\\000\\001J\\000\\005valuexr\\000\\020java.lang.Number\\206\\254\\225\\035\\013\\224\\340\\213\\002\\000\\000xp\\000\\000\\000\\000\\000\\000\\000\\004t\\000\\033\\344\\272\\272\\350\\204\\2701v1\\346\\227\\245\\345\\277\\227\\345\\210\\206\\345\\214\\272\\347\\273\\264\\346\\212\\244t\\000\\0011t\\000\\0011x\\000');
INSERT INTO qrtz_job_details(sched_name, job_name, job_group, description, job_class_name, is_durable, is_nonconcurrent, is_update_data, requests_recovery, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME3', 'DEFAULT', NULL, 'cn.eyecool.quartz.util.QuartzDisallowConcurrentExecution', 'false', 'true', 'false', 'false', E'\\254\\355\\000\\005sr\\000\\025org.quartz.JobDataMap\\237\\260\\203\\350\\277\\251\\260\\313\\002\\000\\000xr\\000&org.quartz.utils.StringKeyDirtyFlagMap\\202\\010\\350\\303\\373\\305](\\002\\000\\001Z\\000\\023allowsTransientDataxr\\000\\035org.quartz.utils.DirtyFlagMap\\023\\346.\\255(v\\012\\316\\002\\000\\002Z\\000\\005dirtyL\\000\\003mapt\\000\\017Ljava/util/Map;xp\\001sr\\000\\021java.util.HashMap\\005\\007\\332\\301\\303\\026\\321\\003\\000\\002F\\000\\012loadFactorI\\000\\011thresholdxp?@\\000\\000\\000\\000\\000\\014w\\010\\000\\000\\000\\020\\000\\000\\000\\001t\\000\\017TASK_PROPERTIESsr\\000\\037cn.eyecool.quartz.domain.SysJob\\000\\000\\000\\000\\000\\000\\000\\001\\002\\000\\010L\\000\\012concurrentt\\000\\022Ljava/lang/String;L\\000\\016cronExpressionq\\000~\\000\\011L\\000\\014invokeTargetq\\000~\\000\\011L\\000\\010jobGroupq\\000~\\000\\011L\\000\\005jobIdt\\000\\020Ljava/lang/Long;L\\000\\007jobNameq\\000~\\000\\011L\\000\\015misfirePolicyq\\000~\\000\\011L\\000\\006statusq\\000~\\000\\011xr\\000(cn.eyecool.common.core.domain.BaseEntity\\000\\000\\000\\000\\000\\000\\000\\001\\002\\000\\011L\\000\\011beginTimeq\\000~\\000\\011L\\000\\010createByq\\000~\\000\\011L\\000\\012createTimet\\000\\020Ljava/util/Date;L\\000\\007endTimeq\\000~\\000\\011L\\000\\006paramsq\\000~\\000\\003L\\000\\006remarkq\\000~\\000\\011L\\000\\013searchValueq\\000~\\000\\011L\\000\\010updateByq\\000~\\000\\011L\\000\\012updateTimeq\\000~\\000\\014xppt\\000\\005adminsr\\000\\016java.util.Datehj\\201\\001KYt\\031\\003\\000\\000xpw\\010\\000\\000\\001y\\345\\331\\364\\250xpsr\\000\\027java.util.LinkedHashMap4\\300N\\\\\\020l\\300\\373\\002\\000\\001Z\\000\\013accessOrderxq\\000~\\000\\005?@\\000\\000\\000\\000\\000\\000w\\010\\000\\000\\000\\020\\000\\000\\000\\000x\\000t\\000\\000pt\\000\\005adminpt\\000\\0011t\\000\\0150 0 0 * * ? *t\\000:requestRecordTask.clearAndAddPartition(''eyecool_assps'',30)t\\000\\007DEFAULTsr\\000\\016java.lang.Long;\\213\\344\\220\\314\\217#\\337\\002\\000\\001J\\000\\005valuexr\\000\\020java.lang.Number\\206\\254\\225\\035\\013\\224\\340\\213\\002\\000\\000xp\\000\\000\\000\\000\\000\\000\\000\\003t\\000\\036\\346\\216\\245\\345\\217\\243\\346\\212\\245\\346\\226\\207\\346\\227\\245\\345\\277\\227\\345\\210\\206\\345\\214\\272\\347\\273\\264\\346\\212\\244t\\000\\0011t\\000\\0011x\\000');
INSERT INTO qrtz_job_details(sched_name, job_name, job_group, description, job_class_name, is_durable, is_nonconcurrent, is_update_data, requests_recovery, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME2', 'DEFAULT', NULL, 'cn.eyecool.quartz.util.QuartzDisallowConcurrentExecution', 'false', 'true', 'false', 'false', E'\\254\\355\\000\\005sr\\000\\025org.quartz.JobDataMap\\237\\260\\203\\350\\277\\251\\260\\313\\002\\000\\000xr\\000&org.quartz.utils.StringKeyDirtyFlagMap\\202\\010\\350\\303\\373\\305](\\002\\000\\001Z\\000\\023allowsTransientDataxr\\000\\035org.quartz.utils.DirtyFlagMap\\023\\346.\\255(v\\012\\316\\002\\000\\002Z\\000\\005dirtyL\\000\\003mapt\\000\\017Ljava/util/Map;xp\\001sr\\000\\021java.util.HashMap\\005\\007\\332\\301\\303\\026\\321\\003\\000\\002F\\000\\012loadFactorI\\000\\011thresholdxp?@\\000\\000\\000\\000\\000\\014w\\010\\000\\000\\000\\020\\000\\000\\000\\001t\\000\\017TASK_PROPERTIESsr\\000\\037cn.eyecool.quartz.domain.SysJob\\000\\000\\000\\000\\000\\000\\000\\001\\002\\000\\010L\\000\\012concurrentt\\000\\022Ljava/lang/String;L\\000\\016cronExpressionq\\000~\\000\\011L\\000\\014invokeTargetq\\000~\\000\\011L\\000\\010jobGroupq\\000~\\000\\011L\\000\\005jobIdt\\000\\020Ljava/lang/Long;L\\000\\007jobNameq\\000~\\000\\011L\\000\\015misfirePolicyq\\000~\\000\\011L\\000\\006statusq\\000~\\000\\011xr\\000(cn.eyecool.common.core.domain.BaseEntity\\000\\000\\000\\000\\000\\000\\000\\001\\002\\000\\011L\\000\\011beginTimeq\\000~\\000\\011L\\000\\010createByq\\000~\\000\\011L\\000\\012createTimet\\000\\020Ljava/util/Date;L\\000\\007endTimeq\\000~\\000\\011L\\000\\006paramsq\\000~\\000\\003L\\000\\006remarkq\\000~\\000\\011L\\000\\013searchValueq\\000~\\000\\011L\\000\\010updateByq\\000~\\000\\011L\\000\\012updateTimeq\\000~\\000\\014xppt\\000\\005adminsr\\000\\016java.util.Datehj\\201\\001KYt\\031\\003\\000\\000xpw\\010\\000\\000\\001y\\345\\331=\\020xpsr\\000\\027java.util.LinkedHashMap4\\300N\\\\\\020l\\300\\373\\002\\000\\001Z\\000\\013accessOrderxq\\000~\\000\\005?@\\000\\000\\000\\000\\000\\000w\\010\\000\\000\\000\\020\\000\\000\\000\\000x\\000t\\000\\000pt\\000\\005adminpt\\000\\0011t\\000\\0170 0/2 * * * ? *t\\000!deviceUpdatePublishTask.execute()t\\000\\007DEFAULTsr\\000\\016java.lang.Long;\\213\\344\\220\\314\\217#\\337\\002\\000\\001J\\000\\005valuexr\\000\\020java.lang.Number\\206\\254\\225\\035\\013\\224\\340\\213\\002\\000\\000xp\\000\\000\\000\\000\\000\\000\\000\\002t\\000\\036\\350\\256\\276\\345\\244\\207\\345\\215\\207\\347\\272\\247\\344\\273\\273\\345\\212\\241\\350\\207\\252\\345\\212\\250\\345\\217\\221\\345\\270\\203t\\000\\0011t\\000\\0011x\\000');
INSERT INTO qrtz_job_details(sched_name, job_name, job_group, description, job_class_name, is_durable, is_nonconcurrent, is_update_data, requests_recovery, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME9', 'DEFAULT', NULL, 'cn.eyecool.quartz.util.QuartzDisallowConcurrentExecution', 'false', 'true', 'false', 'false', E'\\254\\355\\000\\005sr\\000\\025org.quartz.JobDataMap\\237\\260\\203\\350\\277\\251\\260\\313\\002\\000\\000xr\\000&org.quartz.utils.StringKeyDirtyFlagMap\\202\\010\\350\\303\\373\\305](\\002\\000\\001Z\\000\\023allowsTransientDataxr\\000\\035org.quartz.utils.DirtyFlagMap\\023\\346.\\255(v\\012\\316\\002\\000\\002Z\\000\\005dirtyL\\000\\003mapt\\000\\017Ljava/util/Map;xp\\001sr\\000\\021java.util.HashMap\\005\\007\\332\\301\\303\\026\\321\\003\\000\\002F\\000\\012loadFactorI\\000\\011thresholdxp?@\\000\\000\\000\\000\\000\\014w\\010\\000\\000\\000\\020\\000\\000\\000\\001t\\000\\017TASK_PROPERTIESsr\\000\\037cn.eyecool.quartz.domain.SysJob\\000\\000\\000\\000\\000\\000\\000\\001\\002\\000\\010L\\000\\012concurrentt\\000\\022Ljava/lang/String;L\\000\\016cronExpressionq\\000~\\000\\011L\\000\\014invokeTargetq\\000~\\000\\011L\\000\\010jobGroupq\\000~\\000\\011L\\000\\005jobIdt\\000\\020Ljava/lang/Long;L\\000\\007jobNameq\\000~\\000\\011L\\000\\015misfirePolicyq\\000~\\000\\011L\\000\\006statusq\\000~\\000\\011xr\\000(cn.eyecool.common.core.domain.BaseEntity\\000\\000\\000\\000\\000\\000\\000\\001\\002\\000\\011L\\000\\011beginTimeq\\000~\\000\\011L\\000\\010createByq\\000~\\000\\011L\\000\\012createTimet\\000\\020Ljava/util/Date;L\\000\\007endTimeq\\000~\\000\\011L\\000\\006paramsq\\000~\\000\\003L\\000\\006remarkq\\000~\\000\\011L\\000\\013searchValueq\\000~\\000\\011L\\000\\010updateByq\\000~\\000\\011L\\000\\012updateTimeq\\000~\\000\\014xppt\\000\\005adminpppppppt\\000\\0011t\\000\\0150 0 1 * * ? *t\\000BclearBioMatchLogTask.clearIrisSearchLog(365,false,''eyecool_assps'')t\\000\\007DEFAULTsr\\000\\016java.lang.Long;\\213\\344\\220\\314\\217#\\337\\002\\000\\001J\\000\\005valuexr\\000\\020java.lang.Number\\206\\254\\225\\035\\013\\224\\340\\213\\002\\000\\000xp\\000\\000\\000\\000\\000\\000\\000\\011t\\000\\033\\350\\231\\271\\350\\206\\2341vN\\346\\227\\245\\345\\277\\227\\345\\210\\206\\345\\214\\272\\347\\273\\264\\346\\212\\244t\\000\\0011t\\000\\0011x\\000');
INSERT INTO qrtz_job_details(sched_name, job_name, job_group, description, job_class_name, is_durable, is_nonconcurrent, is_update_data, requests_recovery, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME10', 'DEFAULT', NULL, 'cn.eyecool.quartz.util.QuartzDisallowConcurrentExecution', 'false', 'true', 'false', 'false', E'\\254\\355\\000\\005sr\\000\\025org.quartz.JobDataMap\\237\\260\\203\\350\\277\\251\\260\\313\\002\\000\\000xr\\000&org.quartz.utils.StringKeyDirtyFlagMap\\202\\010\\350\\303\\373\\305](\\002\\000\\001Z\\000\\023allowsTransientDataxr\\000\\035org.quartz.utils.DirtyFlagMap\\023\\346.\\255(v\\012\\316\\002\\000\\002Z\\000\\005dirtyL\\000\\003mapt\\000\\017Ljava/util/Map;xp\\001sr\\000\\021java.util.HashMap\\005\\007\\332\\301\\303\\026\\321\\003\\000\\002F\\000\\012loadFactorI\\000\\011thresholdxp?@\\000\\000\\000\\000\\000\\014w\\010\\000\\000\\000\\020\\000\\000\\000\\001t\\000\\017TASK_PROPERTIESsr\\000\\037cn.eyecool.quartz.domain.SysJob\\000\\000\\000\\000\\000\\000\\000\\001\\002\\000\\010L\\000\\012concurrentt\\000\\022Ljava/lang/String;L\\000\\016cronExpressionq\\000~\\000\\011L\\000\\014invokeTargetq\\000~\\000\\011L\\000\\010jobGroupq\\000~\\000\\011L\\000\\005jobIdt\\000\\020Ljava/lang/Long;L\\000\\007jobNameq\\000~\\000\\011L\\000\\015misfirePolicyq\\000~\\000\\011L\\000\\006statusq\\000~\\000\\011xr\\000(cn.eyecool.common.core.domain.BaseEntity\\000\\000\\000\\000\\000\\000\\000\\001\\002\\000\\011L\\000\\011beginTimeq\\000~\\000\\011L\\000\\010createByq\\000~\\000\\011L\\000\\012createTimet\\000\\020Ljava/util/Date;L\\000\\007endTimeq\\000~\\000\\011L\\000\\006paramsq\\000~\\000\\003L\\000\\006remarkq\\000~\\000\\011L\\000\\013searchValueq\\000~\\000\\011L\\000\\010updateByq\\000~\\000\\011L\\000\\012updateTimeq\\000~\\000\\014xppt\\000\\005adminsr\\000\\016java.util.Datehj\\201\\001KYt\\031\\003\\000\\000xpw\\010\\000\\000\\001y\\345\\336>Pxpsr\\000\\027java.util.LinkedHashMap4\\300N\\\\\\020l\\300\\373\\002\\000\\001Z\\000\\013accessOrderxq\\000~\\000\\005?@\\000\\000\\000\\000\\000\\000w\\010\\000\\000\\000\\020\\000\\000\\000\\000x\\000t\\000\\000pt\\000\\005adminpt\\000\\0011t\\000\\0150 0 1 * * ? *t\\000CclearBioMatchLogTask.clearMulitSearchLog(365,false,''eyecool_assps'')t\\000\\007DEFAULTsr\\000\\016java.lang.Long;\\213\\344\\220\\314\\217#\\337\\002\\000\\001J\\000\\005valuexr\\000\\020java.lang.Number\\206\\254\\225\\035\\013\\224\\340\\213\\002\\000\\000xp\\000\\000\\000\\000\\000\\000\\000\\012t\\000*\\344\\272\\272\\350\\204\\270\\350\\231\\271\\350\\206\\234\\345\\244\\232\\346\\250\\241\\346\\200\\2011vN\\346\\227\\245\\345\\277\\227\\345\\210\\206\\345\\214\\272\\347\\273\\264\\346\\212\\244t\\000\\0011t\\000\\0010x\\000');
INSERT INTO qrtz_job_details(sched_name, job_name, job_group, description, job_class_name, is_durable, is_nonconcurrent, is_update_data, requests_recovery, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME11', 'DEFAULT', NULL, 'cn.eyecool.quartz.util.QuartzDisallowConcurrentExecution', 'false', 'true', 'false', 'false', E'\\254\\355\\000\\005sr\\000\\025org.quartz.JobDataMap\\237\\260\\203\\350\\277\\251\\260\\313\\002\\000\\000xr\\000&org.quartz.utils.StringKeyDirtyFlagMap\\202\\010\\350\\303\\373\\305](\\002\\000\\001Z\\000\\023allowsTransientDataxr\\000\\035org.quartz.utils.DirtyFlagMap\\023\\346.\\255(v\\012\\316\\002\\000\\002Z\\000\\005dirtyL\\000\\003mapt\\000\\017Ljava/util/Map;xp\\001sr\\000\\021java.util.HashMap\\005\\007\\332\\301\\303\\026\\321\\003\\000\\002F\\000\\012loadFactorI\\000\\011thresholdxp?@\\000\\000\\000\\000\\000\\014w\\010\\000\\000\\000\\020\\000\\000\\000\\001t\\000\\017TASK_PROPERTIESsr\\000\\037cn.eyecool.quartz.domain.SysJob\\000\\000\\000\\000\\000\\000\\000\\001\\002\\000\\010L\\000\\012concurrentt\\000\\022Ljava/lang/String;L\\000\\016cronExpressionq\\000~\\000\\011L\\000\\014invokeTargetq\\000~\\000\\011L\\000\\010jobGroupq\\000~\\000\\011L\\000\\005jobIdt\\000\\020Ljava/lang/Long;L\\000\\007jobNameq\\000~\\000\\011L\\000\\015misfirePolicyq\\000~\\000\\011L\\000\\006statusq\\000~\\000\\011xr\\000(cn.eyecool.common.core.domain.BaseEntity\\000\\000\\000\\000\\000\\000\\000\\001\\002\\000\\011L\\000\\011beginTimeq\\000~\\000\\011L\\000\\010createByq\\000~\\000\\011L\\000\\012createTimet\\000\\020Ljava/util/Date;L\\000\\007endTimeq\\000~\\000\\011L\\000\\006paramsq\\000~\\000\\003L\\000\\006remarkq\\000~\\000\\011L\\000\\013searchValueq\\000~\\000\\011L\\000\\010updateByq\\000~\\000\\011L\\000\\012updateTimeq\\000~\\000\\014xppt\\000\\005adminsr\\000\\016java.util.Datehj\\201\\001KYt\\031\\003\\000\\000xpw\\010\\000\\000\\001y\\345\\336\\2778xpsr\\000\\027java.util.LinkedHashMap4\\300N\\\\\\020l\\300\\373\\002\\000\\001Z\\000\\013accessOrderxq\\000~\\000\\005?@\\000\\000\\000\\000\\000\\000w\\010\\000\\000\\000\\020\\000\\000\\000\\000x\\000t\\000\\000pt\\000\\005adminpt\\000\\0011t\\000\\0150 0 1 * * ? *t\\000<clearBioMatchLogTask.clearHealthcodeLog(365,''eyecool_assps'')t\\000\\007DEFAULTsr\\000\\016java.lang.Long;\\213\\344\\220\\314\\217#\\337\\002\\000\\001J\\000\\005valuexr\\000\\020java.lang.Number\\206\\254\\225\\035\\013\\224\\340\\213\\002\\000\\000xp\\000\\000\\000\\000\\000\\000\\000\\013t\\000\\033\\345\\201\\245\\345\\272\\267\\347\\240\\201\\346\\227\\245\\345\\277\\227\\345\\210\\206\\345\\214\\272\\347\\273\\264\\346\\212\\244t\\000\\0011t\\000\\0010x\\000');
INSERT INTO qrtz_job_details(sched_name, job_name, job_group, description, job_class_name, is_durable, is_nonconcurrent, is_update_data, requests_recovery, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME12', 'DEFAULT', NULL, 'cn.eyecool.quartz.util.QuartzDisallowConcurrentExecution', 'false', 'true', 'false', 'false', E'\\254\\355\\000\\005sr\\000\\025org.quartz.JobDataMap\\237\\260\\203\\350\\277\\251\\260\\313\\002\\000\\000xr\\000&org.quartz.utils.StringKeyDirtyFlagMap\\202\\010\\350\\303\\373\\305](\\002\\000\\001Z\\000\\023allowsTransientDataxr\\000\\035org.quartz.utils.DirtyFlagMap\\023\\346.\\255(v\\012\\316\\002\\000\\002Z\\000\\005dirtyL\\000\\003mapt\\000\\017Ljava/util/Map;xp\\001sr\\000\\021java.util.HashMap\\005\\007\\332\\301\\303\\026\\321\\003\\000\\002F\\000\\012loadFactorI\\000\\011thresholdxp?@\\000\\000\\000\\000\\000\\014w\\010\\000\\000\\000\\020\\000\\000\\000\\001t\\000\\017TASK_PROPERTIESsr\\000\\037cn.eyecool.quartz.domain.SysJob\\000\\000\\000\\000\\000\\000\\000\\001\\002\\000\\010L\\000\\012concurrentt\\000\\022Ljava/lang/String;L\\000\\016cronExpressionq\\000~\\000\\011L\\000\\014invokeTargetq\\000~\\000\\011L\\000\\010jobGroupq\\000~\\000\\011L\\000\\005jobIdt\\000\\020Ljava/lang/Long;L\\000\\007jobNameq\\000~\\000\\011L\\000\\015misfirePolicyq\\000~\\000\\011L\\000\\006statusq\\000~\\000\\011xr\\000(cn.eyecool.common.core.domain.BaseEntity\\000\\000\\000\\000\\000\\000\\000\\001\\002\\000\\011L\\000\\011beginTimeq\\000~\\000\\011L\\000\\010createByq\\000~\\000\\011L\\000\\012createTimet\\000\\020Ljava/util/Date;L\\000\\007endTimeq\\000~\\000\\011L\\000\\006paramsq\\000~\\000\\003L\\000\\006remarkq\\000~\\000\\011L\\000\\013searchValueq\\000~\\000\\011L\\000\\010updateByq\\000~\\000\\011L\\000\\012updateTimeq\\000~\\000\\014xppt\\000\\005adminpppppppt\\000\\0011t\\000\\0150 0 1 * * ? *t\\0004msgLogTask.clearAndAddPartition(''eyecool_assps'',365)t\\000\\007DEFAULTsr\\000\\016java.lang.Long;\\213\\344\\220\\314\\217#\\337\\002\\000\\001J\\000\\005valuexr\\000\\020java.lang.Number\\206\\254\\225\\035\\013\\224\\340\\213\\002\\000\\000xp\\000\\000\\000\\000\\000\\000\\000\\014t\\000\\030\\346\\266\\210\\346\\201\\257\\346\\227\\245\\345\\277\\227\\345\\210\\206\\345\\214\\272\\347\\273\\264\\346\\212\\244t\\000\\0011t\\000\\0011x\\000');


INSERT INTO qrtz_triggers(sched_name, trigger_name, trigger_group, job_name, job_group, description, next_fire_time, prev_fire_time, priority, trigger_state, trigger_type, start_time, end_time, calendar_name, misfire_instr, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME5', 'DEFAULT', 'TASK_CLASS_NAME5', 'DEFAULT', NULL, 1623085200000, -1, 5, 'WAITING', 'CRON', 1623059104000, 0, NULL, -1, E'\\\\x');
INSERT INTO qrtz_triggers(sched_name, trigger_name, trigger_group, job_name, job_group, description, next_fire_time, prev_fire_time, priority, trigger_state, trigger_type, start_time, end_time, calendar_name, misfire_instr, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME6', 'DEFAULT', 'TASK_CLASS_NAME6', 'DEFAULT', NULL, 1623085200000, -1, 5, 'WAITING', 'CRON', 1623059090000, 0, NULL, -1, E'\\\\x');
INSERT INTO qrtz_triggers(sched_name, trigger_name, trigger_group, job_name, job_group, description, next_fire_time, prev_fire_time, priority, trigger_state, trigger_type, start_time, end_time, calendar_name, misfire_instr, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME2', 'DEFAULT', 'TASK_CLASS_NAME2', 'DEFAULT', NULL, 1623059280000, 1623059160000, 5, 'PAUSED', 'CRON', 1623059124000, 0, NULL, -1, E'\\\\x');
INSERT INTO qrtz_triggers(sched_name, trigger_name, trigger_group, job_name, job_group, description, next_fire_time, prev_fire_time, priority, trigger_state, trigger_type, start_time, end_time, calendar_name, misfire_instr, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME7', 'DEFAULT', 'TASK_CLASS_NAME7', 'DEFAULT', NULL, 1623085200000, -1, 5, 'WAITING', 'CRON', 1623059083000, 0, NULL, -1, E'\\\\x');
INSERT INTO qrtz_triggers(sched_name, trigger_name, trigger_group, job_name, job_group, description, next_fire_time, prev_fire_time, priority, trigger_state, trigger_type, start_time, end_time, calendar_name, misfire_instr, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME8', 'DEFAULT', 'TASK_CLASS_NAME8', 'DEFAULT', NULL, 1623085200000, -1, 5, 'WAITING', 'CRON', 1623059071000, 0, NULL, -1, E'\\\\x');
INSERT INTO qrtz_triggers(sched_name, trigger_name, trigger_group, job_name, job_group, description, next_fire_time, prev_fire_time, priority, trigger_state, trigger_type, start_time, end_time, calendar_name, misfire_instr, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME9', 'DEFAULT', 'TASK_CLASS_NAME9', 'DEFAULT', NULL, 1623085200000, -1, 5, 'WAITING', 'CRON', 1623059185000, 0, NULL, -1, E'\\\\x');
INSERT INTO qrtz_triggers(sched_name, trigger_name, trigger_group, job_name, job_group, description, next_fire_time, prev_fire_time, priority, trigger_state, trigger_type, start_time, end_time, calendar_name, misfire_instr, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME1', 'DEFAULT', 'TASK_CLASS_NAME1', 'DEFAULT', NULL, 1623092400000, -1, 5, 'WAITING', 'CRON', 1623058867000, 0, NULL, -1, E'\\\\x');
INSERT INTO qrtz_triggers(sched_name, trigger_name, trigger_group, job_name, job_group, description, next_fire_time, prev_fire_time, priority, trigger_state, trigger_type, start_time, end_time, calendar_name, misfire_instr, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME3', 'DEFAULT', 'TASK_CLASS_NAME3', 'DEFAULT', NULL, 1623081600000, -1, 5, 'WAITING', 'CRON', 1623059118000, 0, NULL, -1, E'\\\\x');
INSERT INTO qrtz_triggers(sched_name, trigger_name, trigger_group, job_name, job_group, description, next_fire_time, prev_fire_time, priority, trigger_state, trigger_type, start_time, end_time, calendar_name, misfire_instr, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME4', 'DEFAULT', 'TASK_CLASS_NAME4', 'DEFAULT', NULL, 1623085200000, -1, 5, 'WAITING', 'CRON', 1623059111000, 0, NULL, -1, E'\\\\x');
INSERT INTO qrtz_triggers(sched_name, trigger_name, trigger_group, job_name, job_group, description, next_fire_time, prev_fire_time, priority, trigger_state, trigger_type, start_time, end_time, calendar_name, misfire_instr, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME10', 'DEFAULT', 'TASK_CLASS_NAME10', 'DEFAULT', NULL, 1623171600000, -1, 5, 'WAITING', 'CRON', 1623123298000, 0, NULL, -1, E'\\\\x');
INSERT INTO qrtz_triggers(sched_name, trigger_name, trigger_group, job_name, job_group, description, next_fire_time, prev_fire_time, priority, trigger_state, trigger_type, start_time, end_time, calendar_name, misfire_instr, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME11', 'DEFAULT', 'TASK_CLASS_NAME11', 'DEFAULT', NULL, 1623171600000, -1, 5, 'WAITING', 'CRON', 1623123316000, 0, NULL, -1, E'\\\\x');
INSERT INTO qrtz_triggers(sched_name, trigger_name, trigger_group, job_name, job_group, description, next_fire_time, prev_fire_time, priority, trigger_state, trigger_type, start_time, end_time, calendar_name, misfire_instr, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME12', 'DEFAULT', 'TASK_CLASS_NAME12', 'DEFAULT', NULL, 1623171600000, -1, 5, 'WAITING', 'CRON', 1623128232000, 0, NULL, -1, E'\\\\x');


INSERT INTO qrtz_cron_triggers(sched_name, trigger_name, trigger_group, cron_expression, time_zone_id) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME1', 'DEFAULT', '0 0 3 /1 * ? *', 'Asia/Shanghai');
INSERT INTO qrtz_cron_triggers(sched_name, trigger_name, trigger_group, cron_expression, time_zone_id) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME8', 'DEFAULT', ' 0 0 1 * * ? *', 'Asia/Shanghai');
INSERT INTO qrtz_cron_triggers(sched_name, trigger_name, trigger_group, cron_expression, time_zone_id) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME7', 'DEFAULT', '0 0 1 * * ? *', 'Asia/Shanghai');
INSERT INTO qrtz_cron_triggers(sched_name, trigger_name, trigger_group, cron_expression, time_zone_id) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME6', 'DEFAULT', '0 0 1 * * ? *', 'Asia/Shanghai');
INSERT INTO qrtz_cron_triggers(sched_name, trigger_name, trigger_group, cron_expression, time_zone_id) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME5', 'DEFAULT', '0 0 1 * * ? *', 'Asia/Shanghai');
INSERT INTO qrtz_cron_triggers(sched_name, trigger_name, trigger_group, cron_expression, time_zone_id) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME4', 'DEFAULT', '0 0 1 * * ? *', 'Asia/Shanghai');
INSERT INTO qrtz_cron_triggers(sched_name, trigger_name, trigger_group, cron_expression, time_zone_id) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME3', 'DEFAULT', '0 0 0 * * ? *', 'Asia/Shanghai');
INSERT INTO qrtz_cron_triggers(sched_name, trigger_name, trigger_group, cron_expression, time_zone_id) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME9', 'DEFAULT', '0 0 1 * * ? *', 'Asia/Shanghai');
INSERT INTO qrtz_cron_triggers(sched_name, trigger_name, trigger_group, cron_expression, time_zone_id) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME2', 'DEFAULT', '0 0/2 * * * ? *', 'Asia/Shanghai');
INSERT INTO qrtz_cron_triggers(sched_name, trigger_name, trigger_group, cron_expression, time_zone_id) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME10', 'DEFAULT', '0 0 1 * * ? *', 'Asia/Shanghai');
INSERT INTO qrtz_cron_triggers(sched_name, trigger_name, trigger_group, cron_expression, time_zone_id) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME11', 'DEFAULT', '0 0 1 * * ? *', 'Asia/Shanghai');
INSERT INTO qrtz_cron_triggers(sched_name, trigger_name, trigger_group, cron_expression, time_zone_id) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME12', 'DEFAULT', '0 0 1 * * ? *', 'Asia/Shanghai');


INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2012, '基础数据', 0, 4, 'basedata', NULL, 1, 0, 'M', '0', '0', NULL, 'base', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2013, '人员管理', 2012, 1, 'person', 'basedata/person/index', 1, 0, 'C', '0', '0', 'basedata:person:list', '#', 'admin', now(), 'admin', now(), '人员基础信息菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2014, '人员基础信息查询', 2013, 1, '#', '', 1, 0, 'F', '0', '0', 'basedata:person:query', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2015, '人员基础信息新增', 2013, 2, '#', '', 1, 0, 'F', '0', '0', 'basedata:person:add', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2016, '人员基础信息修改', 2013, 3, '#', '', 1, 0, 'F', '0', '0', 'basedata:person:edit', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2017, '人员基础信息删除', 2013, 4, '#', '', 1, 0, 'F', '0', '0', 'basedata:person:remove', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2018, '人员基础信息导出', 2013, 5, '#', '', 1, 0, 'F', '0', '0', 'basedata:person:export', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2019, '人脸管理', 2012, 2, 'face', 'basedata/face/index', 1, 0, 'C', '0', '0', 'basedata:face:list', '#', 'admin', now(), 'admin', now(), '人脸图像信息菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2020, '人脸图像信息查询', 2019, 1, '#', '', 1, 0, 'F', '0', '0', 'basedata:face:query', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2021, '人脸图像信息新增', 2019, 2, '#', '', 1, 0, 'F', '0', '0', 'basedata:face:add', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2022, '人脸图像信息修改', 2019, 3, '#', '', 1, 0, 'F', '0', '0', 'basedata:face:edit', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2023, '人脸图像信息删除', 2019, 4, '#', '', 1, 0, 'F', '0', '0', 'basedata:face:remove', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2024, '人脸图像信息导出', 2019, 5, '#', '', 1, 0, 'F', '0', '0', 'basedata:face:export', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2025, '指纹管理', 2012, 3, 'finger', 'basedata/finger/index', 1, 0, 'C', '0', '0', 'basedata:finger:list', '#', 'admin', now(), 'admin', now(), '指纹图像信息菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2026, '指纹图像信息查询', 2025, 1, '#', '', 1, 0, 'F', '0', '0', 'basedata:finger:query', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2027, '指纹图像信息新增', 2025, 2, '#', '', 1, 0, 'F', '0', '0', 'basedata:finger:add', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2028, '指纹图像信息修改', 2025, 3, '#', '', 1, 0, 'F', '0', '0', 'basedata:finger:edit', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2029, '指纹图像信息删除', 2025, 4, '#', '', 1, 0, 'F', '0', '0', 'basedata:finger:remove', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2030, '指纹图像信息导出', 2025, 5, '#', '', 1, 0, 'F', '0', '0', 'basedata:finger:export', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2031, '虹膜管理', 2012, 4, 'iris', 'basedata/iris/index', 1, 0, 'C', '0', '0', 'basedata:iris:list', '#', 'admin', now(), 'admin', now(), '虹膜图像信息菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2032, '虹膜图像信息查询', 2031, 1, '#', '', 1, 0, 'F', '0', '0', 'basedata:iris:query', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2033, '虹膜图像信息新增', 2031, 2, '#', '', 1, 0, 'F', '0', '0', 'basedata:iris:add', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2034, '虹膜图像信息修改', 2031, 3, '#', '', 1, 0, 'F', '0', '0', 'basedata:iris:edit', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2035, '虹膜图像信息删除', 2031, 4, '#', '', 1, 0, 'F', '0', '0', 'basedata:iris:remove', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2036, '虹膜图像信息导出', 2031, 5, '#', '', 1, 0, 'F', '0', '0', 'basedata:iris:export', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2037, '人脸虹膜', 2012, 5, 'faceIris', 'basedata/faceIris/index', 1, 0, 'C', '0', '0', 'basedata:faceIris:list', '#', 'admin', now(), 'admin', now(), '虹膜人脸多模态菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2038, '虹膜人脸多模态查询', 2037, 1, '#', '', 1, 0, 'F', '0', '0', 'basedata:faceIris:query', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2039, '虹膜人脸多模态新增', 2037, 2, '#', '', 1, 0, 'F', '0', '0', 'basedata:faceIris:add', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2040, '虹膜人脸多模态修改', 2037, 3, '#', '', 1, 0, 'F', '0', '0', 'basedata:faceIris:edit', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2041, '虹膜人脸多模态删除', 2037, 4, '#', '', 1, 0, 'F', '0', '0', 'basedata:faceIris:remove', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2042, '虹膜人脸多模态导出', 2037, 5, '#', '', 1, 0, 'F', '0', '0', 'basedata:faceIris:export', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2043, '证件管理', 2012, 6, 'cert', 'basedata/cert/index', 1, 0, 'C', '0', '0', 'basedata:cert:list', '#', 'admin', now(), 'admin', now(), '人员证件信息菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2044, '人员证件信息查询', 2043, 1, '#', '', 1, 0, 'F', '0', '0', 'basedata:cert:query', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2045, '人员证件信息新增', 2043, 2, '#', '', 1, 0, 'F', '0', '0', 'basedata:cert:add', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2046, '人员证件信息修改', 2043, 3, '#', '', 1, 0, 'F', '0', '0', 'basedata:cert:edit', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2047, '人员证件信息删除', 2043, 4, '#', '', 1, 0, 'F', '0', '0', 'basedata:cert:remove', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2048, '人员证件信息导出', 2043, 5, '#', '', 1, 0, 'F', '0', '0', 'basedata:cert:export', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2055, '一键更新人脸特征', 2019, 6, '', NULL, 1, 0, 'F', '0', '0', 'basedata:face:updatefeature', '#', 'admin', now(), 'admin', now(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2056, '一键更新指纹特征', 2025, 6, '', NULL, 1, 0, 'F', '0', '0', 'basedata:finger:updatefeature', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2057, '一键更新虹膜特征', 2031, 6, '', NULL, 1, 0, 'F', '0', '0', 'basedata:iris:updatefeature', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2058, '人员证件信息导入', 2043, 6, '', NULL, 1, 0, 'F', '0', '0', 'basedata:cert:import', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2059, '人员证件照片下载', 2043, 7, '', NULL, 1, 0, 'F', '0', '0', 'basedata:cert:download', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2060, '人脸图像下载', 2019, 7, '', NULL, 1, 0, 'F', '0', '0', 'basedata:face:download', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2061, '指纹图像下载', 2025, 7, '', NULL, 1, 0, 'F', '0', '0', 'basedata:finger:download', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2062, '虹膜图像下载', 2031, 7, '', NULL, 1, 0, 'F', '0', '0', 'basedata:iris:download', '#', 'admin', now(), 'admin', now(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2063, '人员信息一键同步', 2013, 7, '', NULL, 1, 0, 'F', '0', '0', 'basedata:person:syncdata', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2064, '人员基础信息导入', 2013, 8, '', NULL, 1, 0, 'F', '0', '0', 'basedata:person:import', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2065, '人脸图片导入', 2019, 8, '', NULL, 1, 0, 'F', '0', '0', 'basedata:face:import', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2066, '指纹图片导入', 2025, 8, '', NULL, 1, 0, 'F', '0', '0', 'basedata:finger:import', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2067, '虹膜图片导入', 2031, 8, '', NULL, 1, 0, 'F', '0', '0', 'basedata:iris:import', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2068, '场景接入', 0, 5, 'scene', NULL, 1, 0, 'M', '0', '0', NULL, 'scene', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2069, '渠道信息', 2068, 1, 'channel', 'scene/channel/index', 1, 0, 'C', '0', '0', 'scene:channel:list', '#', 'admin', now(), 'admin', now(), '渠道信息菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2070, '渠道信息查询', 2069, 1, '#', '', 1, 0, 'F', '0', '0', 'scene:channel:query', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2071, '渠道信息新增', 2069, 2, '#', '', 1, 0, 'F', '0', '0', 'scene:channel:add', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2072, '渠道信息修改', 2069, 3, '#', '', 1, 0, 'F', '0', '0', 'scene:channel:edit', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2073, '渠道信息删除', 2069, 4, '#', '', 1, 0, 'F', '0', '0', 'scene:channel:remove', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2074, '渠道信息导出', 2069, 5, '#', '', 1, 0, 'F', '0', '0', 'scene:channel:export', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2075, '分库信息', 2068, 1, 'subtreasury', 'scene/subtreasury/index', 1, 0, 'C', '0', '0', 'scene:subtreasury:list', '#', 'admin', now(), 'admin', now(), '分库信息菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2076, '分库信息查询', 2075, 1, '#', '', 1, 0, 'F', '0', '0', 'scene:subtreasury:query', '#', 'admin', now(), 'admin', now(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2077, '分库信息新增', 2075, 2, '#', '', 1, 0, 'F', '0', '0', 'scene:subtreasury:add', '#', 'admin', now(), 'admin', now(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2078, '分库信息修改', 2075, 3, '#', '', 1, 0, 'F', '0', '0', 'scene:subtreasury:edit', '#', 'admin', now(), 'admin', now(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2079, '分库信息删除', 2075, 4, '#', '', 1, 0, 'F', '0', '0', 'scene:subtreasury:remove', '#', 'admin', now(), 'admin', now(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2080, '分库信息导出', 2075, 5, '#', '', 1, 0, 'F', '0', '0', 'scene:subtreasury:export', '#', 'admin', now(), 'admin', now(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2081, '渠道参数', 2068, 1, 'channelParam', 'scene/channelParam/index', 1, 0, 'C', '0', '0', 'scene:channelParam:list', '#', 'admin', now(), 'admin', now(), '渠道参数菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2082, '渠道参数查询', 2081, 1, '#', '', 1, 0, 'F', '0', '0', 'scene:channelParam:query', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2083, '渠道参数新增', 2081, 2, '#', '', 1, 0, 'F', '0', '0', 'scene:channelParam:add', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2084, '渠道参数修改', 2081, 3, '#', '', 1, 0, 'F', '0', '0', 'scene:channelParam:edit', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2085, '渠道参数删除', 2081, 4, '#', '', 1, 0, 'F', '0', '0', 'scene:channelParam:remove', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2086, '渠道参数导出', 2081, 5, '#', '', 1, 0, 'F', '0', '0', 'scene:channelParam:export', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2087, '渠道业务', 2068, 1, 'channelBusi', 'scene/channelBusi/index', 1, 0, 'C', '0', '0', 'scene:channelBusi:list', '#', 'admin', now(), 'admin', now(), '渠道业务菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2088, '渠道业务查询', 2087, 1, '#', '', 1, 0, 'F', '0', '0', 'scene:channelBusi:query', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2089, '渠道业务新增', 2087, 2, '#', '', 1, 0, 'F', '0', '0', 'scene:channelBusi:add', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2090, '渠道业务修改', 2087, 3, '#', '', 1, 0, 'F', '0', '0', 'scene:channelBusi:edit', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2091, '渠道业务删除', 2087, 4, '#', '', 1, 0, 'F', '0', '0', 'scene:channelBusi:remove', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2092, '渠道业务导出', 2087, 5, '#', '', 1, 0, 'F', '0', '0', 'scene:channelBusi:export', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2093, '分库业务', 2068, 1, 'subtreasuryBusi', 'scene/subtreasuryBusi/index', 1, 0, 'C', '0', '0', 'scene:subtreasuryBusi:list', '#', 'admin', now(), 'admin', now(), '分库业务菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2094, '分库业务查询', 2093, 1, '#', '', 1, 0, 'F', '0', '0', 'scene:subtreasuryBusi:query', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2095, '分库业务新增', 2093, 2, '#', '', 1, 0, 'F', '0', '0', 'scene:subtreasuryBusi:add', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2096, '分库业务修改', 2093, 3, '#', '', 1, 0, 'F', '0', '0', 'scene:subtreasuryBusi:edit', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2097, '分库业务删除', 2093, 4, '#', '', 1, 0, 'F', '0', '0', 'scene:subtreasuryBusi:remove', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2098, '分库业务导出', 2093, 5, '#', '', 1, 0, 'F', '0', '0', 'scene:subtreasuryBusi:export', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2099, '渠道业务一键同步', 2087, 6, '', NULL, 1, 0, 'F', '0', '0', 'scene:channelBusi:syncdata', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2100, '渠道业务导入', 2087, 7, '', NULL, 1, 0, 'F', '0', '0', 'scene:channelBusi:import', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2101, '分库业务一键同步', 2093, 6, '', NULL, 1, 0, 'F', '0', '0', 'scene:subtreasuryBusi:syncdata', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2102, '区域管理', 0, 6, 'area', NULL, 1, 0, 'M', '0', '0', '', 'area', 'admin', now(), 'admin', now(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2103, '区域模型', 2102, 1, 'model', 'area/model/index', 1, 0, 'C', '0', '0', 'area:model:list', '#', 'admin', now(), 'admin', now(), '区域菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2104, '区域查询', 2103, 1, '#', '', 1, 0, 'F', '0', '0', 'area:model:query', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2105, '区域新增', 2103, 2, '#', '', 1, 0, 'F', '0', '0', 'area:model:add', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2106, '区域修改', 2103, 3, '#', '', 1, 0, 'F', '0', '0', 'area:model:edit', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2107, '区域删除', 2103, 4, '#', '', 1, 0, 'F', '0', '0', 'area:model:remove', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2108, '区域导出', 2103, 5, '#', '', 1, 0, 'F', '0', '0', 'area:model:export', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2109, '设备管理', 0, 7, 'device', NULL, 1, 0, 'M', '0', '0', NULL, 'device', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2110, '设备型号', 2109, 1, 'model', 'device/model/index', 1, 0, 'C', '0', '0', 'device:model:list', '#', 'admin', now(), 'admin', now(), '设备型号信息菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2111, '设备型号信息查询', 2110, 1, '#', '', 1, 0, 'F', '0', '0', 'device:model:query', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2112, '设备型号信息新增', 2110, 2, '#', '', 1, 0, 'F', '0', '0', 'device:model:add', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2113, '设备型号信息修改', 2110, 3, '#', '', 1, 0, 'F', '0', '0', 'device:model:edit', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2114, '设备型号信息删除', 2110, 4, '#', '', 1, 0, 'F', '0', '0', 'device:model:remove', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2115, '设备型号信息导出', 2110, 5, '#', '', 1, 0, 'F', '0', '0', 'device:model:export', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2116, '设备参数', 2109, 4, 'param', 'device/param/index', 1, 0, 'M', '0', '0', '', '#', 'admin', now(), 'admin', now(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2118, '参数信息', 2116, 1, 'info', 'device/param/info/index', 1, 0, 'C', '0', '0', 'device:paraminfo:list', '#', 'admin', now(), 'admin', now(), '设备参数信息菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2119, '设备参数信息查询', 2118, 1, '#', '', 1, 0, 'F', '0', '0', 'device:paraminfo:query', '#', 'admin', now(), 'admin', now(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2120, '设备参数信息新增', 2118, 2, '#', '', 1, 0, 'F', '0', '0', 'device:paraminfo:add', '#', 'admin', now(), 'admin', now(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2121, '设备参数信息修改', 2118, 3, '#', '', 1, 0, 'F', '0', '0', 'device:paraminfo:edit', '#', 'admin', now(), 'admin', now(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2122, '设备参数信息删除', 2118, 4, '#', '', 1, 0, 'F', '0', '0', 'device:paraminfo:remove', '#', 'admin', now(), 'admin', now(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2123, '设备参数信息导出', 2118, 5, '#', '', 1, 0, 'F', '0', '0', 'device:paraminfo:export', '#', 'admin', now(), 'admin', now(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2124, 'SDK文件', 3, 1, 'file', 'tool/sdkFile/index', 1, 0, 'C', '0', '0', 'tool:sdkFile:list', '#', 'admin', now(), 'admin', now(), 'SDK文件上传菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2125, 'SDK文件上传查询', 2124, 1, '#', '', 1, 0, 'F', '0', '0', 'tool:sdkFile:query', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2126, 'SDK文件上传新增', 2124, 2, '#', '', 1, 0, 'F', '0', '0', 'tool:sdkFile:add', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2127, 'SDK文件上传修改', 2124, 3, '#', '', 1, 0, 'F', '0', '0', 'tool:sdkFile:edit', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2128, 'SDK文件上传删除', 2124, 4, '#', '', 1, 0, 'F', '0', '0', 'tool:sdkFile:remove', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2129, 'SDK文件上传导出', 2124, 5, '#', '', 1, 0, 'F', '0', '0', 'tool:sdkFile:export', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2136, '型号参数', 2116, 1, 'relation', 'device/param/relation/index', 1, 0, 'C', '0', '0', 'device:paramModelRel:list', '#', 'admin', now(), 'admin', now(), '参数型号关系菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2137, '参数型号关系查询', 2136, 1, '#', '', 1, 0, 'F', '0', '0', 'device:paramModelRel:query', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2138, '参数型号关系新增', 2136, 2, '#', '', 1, 0, 'F', '0', '0', 'device:paramModelRel:add', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2139, '参数型号关系修改', 2136, 3, '#', '', 1, 0, 'F', '0', '0', 'device:paramModelRel:edit', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2140, '参数型号关系删除', 2136, 4, '#', '', 1, 0, 'F', '0', '0', 'device:paramModelRel:remove', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2141, '参数型号关系导出', 2136, 5, '#', '', 1, 0, 'F', '0', '0', 'device:paramModelRel:export', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2142, '设备信息', 2109, 2, 'info', 'device/info/index', 1, 0, 'C', '0', '0', 'device:info:list', '#', 'admin', now(), 'admin', now(), '设备信息菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2143, '设备信息查询', 2142, 1, '#', '', 1, 0, 'F', '0', '0', 'device:info:query', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2144, '设备信息新增', 2142, 2, '#', '', 1, 0, 'F', '0', '0', 'device:info:add', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2145, '设备信息修改', 2142, 3, '#', '', 1, 0, 'F', '0', '0', 'device:info:edit', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2146, '设备信息删除', 2142, 4, '#', '', 1, 0, 'F', '0', '0', 'device:info:remove', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2147, '设备信息导出', 2142, 5, '#', '', 1, 0, 'F', '0', '0', 'device:info:export', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2148, '设备信息导入', 2142, 6, '', NULL, 1, 0, 'F', '0', '0', 'device:info:import', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2149, '设备批次', 2109, 3, 'batchlog', 'device/batchlog/index', 1, 0, 'C', '0', '0', 'device:batchlog:list', '#', 'admin', now(), 'admin', now(), '设备批次菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2150, '设备批次查询', 2149, 1, '#', '', 1, 0, 'F', '0', '0', 'device:batchlog:query', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2153, '设备批次回滚', 2149, 2, '#', '', 1, 0, 'F', '0', '0', 'device:batchlog:rollback', '#', 'admin', now(), 'admin', now(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2154, '设备批次导出', 2149, 3, '#', '', 1, 0, 'F', '0', '0', 'device:batchlog:export', '#', 'admin', now(), 'admin', now(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2155, '设备升级', 2109, 5, 'upgrade', 'device/upgrade/index', 1, 0, 'M', '0', '0', '', '#', 'admin', now(), 'admin', now(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2156, '版本信息', 2155, 1, 'version', 'device/upgrade/version/index', 1, 0, 'C', '0', '0', 'device:upgradeVersion:list', '#', 'admin', now(), 'admin', now(), '版本信息菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2157, '版本信息查询', 2156, 1, '#', '', 1, 0, 'F', '0', '0', 'device:upgradeVersion:query', '#', 'admin', now(), 'admin', now(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2158, '版本信息新增', 2156, 2, '#', '', 1, 0, 'F', '0', '0', 'device:upgradeVersion:add', '#', 'admin', now(), 'admin', now(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2159, '版本信息修改', 2156, 3, '#', '', 1, 0, 'F', '0', '0', 'device:upgradeVersion:edit', '#', 'admin', now(), 'admin', now(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2160, '版本信息删除', 2156, 4, '#', '', 1, 0, 'F', '0', '0', 'device:upgradeVersion:remove', '#', 'admin', now(), 'admin', now(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2161, '版本信息导出', 2156, 5, '#', '', 1, 0, 'F', '0', '0', 'device:upgradeVersion:export', '#', 'admin', now(), 'admin', now(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2162, '升级任务', 2155, 1, 'task', 'device/upgrade/task/index', 1, 0, 'C', '0', '0', 'device:upgradeTask:list', '#', 'admin', now(), 'admin', now(), '升级任务菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2163, '升级任务查询', 2162, 1, '#', '', 1, 0, 'F', '0', '0', 'device:upgradeTask:query', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2164, '升级任务新增', 2162, 2, '#', '', 1, 0, 'F', '0', '0', 'device:upgradeTask:add', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2165, '升级任务修改', 2162, 3, '#', '', 1, 0, 'F', '0', '0', 'device:upgradeTask:edit', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2166, '升级任务删除', 2162, 4, '#', '', 1, 0, 'F', '0', '0', 'device:upgradeTask:remove', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2167, '升级任务导出', 2162, 5, '#', '', 1, 0, 'F', '0', '0', 'device:upgradeTask:export', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2168, '升级日志', 2155, 1, 'upgradelog', 'device/upgrade/log/index', 1, 0, 'C', '0', '0', 'device:upgradelog:list', '#', 'admin', now(), 'admin', now(), '升级日志菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2169, '升级日志查询', 2168, 1, '#', '', 1, 0, 'F', '0', '0', 'device:upgradelog:query', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2173, '升级日志导出', 2168, 5, '#', '', 1, 0, 'F', '0', '0', 'device:upgradelog:export', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2174, '参数下发', 2116, 1, 'paramlog', 'device/param/log/index', 1, 0, 'C', '0', '0', 'device:paramlog:list', '#', 'admin', now(), 'admin', now(), '参数下发日志菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2175, '参数下发日志查询', 2174, 1, '#', '', 1, 0, 'F', '0', '0', 'device:paramlog:query', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2176, '参数下发日志新增', 2174, 2, '#', '', 1, 0, 'F', '0', '0', 'device:paramlog:add', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2179, '参数下发日志导出', 2174, 5, '#', '', 1, 0, 'F', '0', '0', 'device:paramlog:export', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2180, '动作日志', 2109, 6, 'actionlog', 'device/actionlog/index', 1, 0, 'C', '0', '0', 'device:actionlog:list', '#', 'admin', now(), 'admin', now(), '设备动作日志菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2181, '设备动作日志查询', 2180, 1, '#', '', 1, 0, 'F', '0', '0', 'device:actionlog:query', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2182, '设备动作日志新增', 2180, 2, '#', '', 1, 0, 'F', '0', '0', 'device:actionlog:add', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2183, '设备动作日志修改', 2180, 3, '#', '', 1, 0, 'F', '0', '0', 'device:actionlog:edit', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2184, '设备动作日志删除', 2180, 4, '#', '', 1, 0, 'F', '0', '0', 'device:actionlog:remove', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2185, '设备动作日志导出', 2180, 5, '#', '', 1, 0, 'F', '0', '0', 'device:actionlog:export', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2186, '应用系统', 0, 8, 'app', NULL, 1, 0, 'M', '0', '0', NULL, 'appmgr', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2187, '应用信息', 2186, 1, 'info', 'app/info/index', 1, 0, 'C', '0', '0', 'app:info:list', '#', 'admin', now(), 'admin', now(), '应用系统信息菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2188, '应用系统信息查询', 2187, 1, '#', '', 1, 0, 'F', '0', '0', 'app:info:query', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2189, '应用系统信息新增', 2187, 2, '#', '', 1, 0, 'F', '0', '0', 'app:info:add', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2191, '应用系统信息删除', 2187, 4, '#', '', 1, 0, 'F', '0', '0', 'app:info:remove', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2192, '应用系统信息导出', 2187, 5, '#', '', 1, 0, 'F', '0', '0', 'app:info:export', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2193, '接口授权', 2186, 2, 'auth', 'app/auth/index', 1, 0, 'C', '0', '0', 'app:auth:list', '#', 'admin', now(), 'admin', now(), '应用接口授权菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2194, '应用接口授权查询', 2193, 1, '#', '', 1, 0, 'F', '0', '0', 'app:auth:query', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2195, '应用接口授权新增', 2193, 2, '#', '', 1, 0, 'F', '0', '0', 'app:auth:add', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2196, '应用接口授权修改', 2193, 3, '#', '', 1, 0, 'F', '0', '0', 'app:auth:edit', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2197, '应用接口授权删除', 2193, 4, '#', '', 1, 0, 'F', '0', '0', 'app:auth:remove', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2198, '应用接口授权导出', 2193, 5, '#', '', 1, 0, 'F', '0', '0', 'app:auth:export', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2199, '消息通知', 0, 9, 'msg', NULL, 1, 0, 'M', '0', '0', NULL, 'msgmgr', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2200, '邮箱配置', 2261, 1, 'mailProperty', 'msg/mail/mailProperty/index', 1, 0, 'C', '0', '0', 'msg:mailProperty:list', '#', 'admin', now(), 'admin', now(), '邮箱配置菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2201, '邮箱配置查询', 2200, 1, '#', '', 1, 0, 'F', '0', '0', 'msg:mailProperty:query', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2202, '邮箱配置新增', 2200, 2, '#', '', 1, 0, 'F', '0', '0', 'msg:mailProperty:add', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2203, '邮箱配置修改', 2200, 3, '#', '', 1, 0, 'F', '0', '0', 'msg:mailProperty:edit', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2204, '邮箱配置删除', 2200, 4, '#', '', 1, 0, 'F', '0', '0', 'msg:mailProperty:remove', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2205, '邮箱配置导出', 2200, 5, '#', '', 1, 0, 'F', '0', '0', 'msg:mailProperty:export', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2206, '钉钉微应用', 2263, 2, 'dingApplication', 'msg/dingtalk/application/index', 1, 0, 'C', '0', '0', 'msg:dingApplication:list', '#', 'admin', now(), 'admin', now(), '钉钉微应用菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2207, '钉钉微应用查询', 2206, 1, '#', '', 1, 0, 'F', '0', '0', 'msg:dingApplication:query', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2208, '钉钉微应用新增', 2206, 2, '#', '', 1, 0, 'F', '0', '0', 'msg:dingApplication:add', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2209, '钉钉微应用修改', 2206, 3, '#', '', 1, 0, 'F', '0', '0', 'msg:dingApplication:edit', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2210, '钉钉微应用删除', 2206, 4, '#', '', 1, 0, 'F', '0', '0', 'msg:dingApplication:remove', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2211, '钉钉微应用导出', 2206, 5, '#', '', 1, 0, 'F', '0', '0', 'msg:dingApplication:export', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2212, '钉钉团队', 2263, 1, 'dingTeam', 'msg/dingtalk/team/index', 1, 0, 'C', '0', '0', 'msg:dingTeam:list', '#', 'admin', now(), 'admin', now(), '钉钉团队(企业)菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2213, '钉钉团队(企业)查询', 2212, 1, '#', '', 1, 0, 'F', '0', '0', 'msg:dingTeam:query', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2214, '钉钉团队(企业)新增', 2212, 2, '#', '', 1, 0, 'F', '0', '0', 'msg:dingTeam:add', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2215, '钉钉团队(企业)修改', 2212, 3, '#', '', 1, 0, 'F', '0', '0', 'msg:dingTeam:edit', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2216, '钉钉团队(企业)删除', 2212, 4, '#', '', 1, 0, 'F', '0', '0', 'msg:dingTeam:remove', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2217, '钉钉团队(企业)导出', 2212, 5, '#', '', 1, 0, 'F', '0', '0', 'msg:dingTeam:export', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2224, '消息附件', 2264, 3, 'logannex', 'msg/log/logannex/index', 1, 0, 'C', '0', '0', 'msg:logannex:list', '#', 'admin', now(), 'admin', now(), '消息日志附件菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2225, '消息日志附件查询', 2224, 1, '#', '', 1, 0, 'F', '0', '0', 'msg:logannex:query', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2229, '消息日志附件导出', 2224, 5, '#', '', 1, 0, 'F', '0', '0', 'msg:logannex:export', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2230, '消息日志', 2264, 1, 'msglog', 'msg/log/msglog/index', 1, 0, 'C', '0', '0', 'msg:log:list', '#', 'admin', now(), 'admin', now(), '消息日志菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2231, '消息日志查询', 2230, 1, '#', '', 1, 0, 'F', '0', '0', 'msg:log:query', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2235, '消息日志导出', 2230, 5, '#', '', 1, 0, 'F', '0', '0', 'msg:log:export', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2236, '微信公众号', 2262, 1, 'officalAccount', 'msg/wechat/officalAccount/index', 1, 0, 'C', '0', '0', 'msg:officalAccount:list', '#', 'admin', now(), 'admin', now(), '微信公众号菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2237, '微信公众号查询', 2236, 1, '#', '', 1, 0, 'F', '0', '0', 'msg:officalAccount:query', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2238, '微信公众号新增', 2236, 2, '#', '', 1, 0, 'F', '0', '0', 'msg:officalAccount:add', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2239, '微信公众号修改', 2236, 3, '#', '', 1, 0, 'F', '0', '0', 'msg:officalAccount:edit', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2240, '微信公众号删除', 2236, 4, '#', '', 1, 0, 'F', '0', '0', 'msg:officalAccount:remove', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2241, '微信公众号导出', 2236, 5, '#', '', 1, 0, 'F', '0', '0', 'msg:officalAccount:export', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2242, '公众号用户', 2262, 2, 'officalAccountUser', 'msg/wechat/officalAccountUser/index', 1, 0, 'C', '0', '0', 'msg:officalAccountUser:list', '#', 'admin', now(), 'admin', now(), '微信用户菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2243, '微信用户查询', 2242, 1, '#', '', 1, 0, 'F', '0', '0', 'msg:officalAccountUser:query', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2244, '微信用户拉取', 2242, 2, '#', '', 1, 0, 'F', '0', '0', 'msg:officalAccountUser:pull', '#', 'admin', now(), 'admin', now(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2245, '微信用户修改', 2242, 3, '#', '', 1, 0, 'F', '0', '0', 'msg:officalAccountUser:edit', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2247, '微信用户导出', 2242, 5, '#', '', 1, 0, 'F', '0', '0', 'msg:officalAccountUser:export', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2248, '短信云账户', 2260, 1, 'smsCloudAccount', 'msg/sms/cloudAccount/index', 1, 0, 'C', '0', '0', 'msg:smsCloudAccount:list', '#', 'admin', now(), 'admin', now(), '短信云账户菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2249, '短信云账户查询', 2248, 1, '#', '', 1, 0, 'F', '0', '0', 'msg:smsCloudAccount:query', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2250, '短信云账户新增', 2248, 2, '#', '', 1, 0, 'F', '0', '0', 'msg:smsCloudAccount:add', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2251, '短信云账户修改', 2248, 3, '#', '', 1, 0, 'F', '0', '0', 'msg:smsCloudAccount:edit', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2252, '短信云账户删除', 2248, 4, '#', '', 1, 0, 'F', '0', '0', 'msg:smsCloudAccount:remove', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2253, '短信云账户导出', 2248, 5, '#', '', 1, 0, 'F', '0', '0', 'msg:smsCloudAccount:export', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2254, '消息模板', 2199, 5, 'template', 'msg/template/index', 1, 0, 'C', '0', '0', 'msg:template:list', '#', 'admin', now(), 'admin', now(), '消息模板菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2255, '消息模板查询', 2254, 1, '#', '', 1, 0, 'F', '0', '0', 'msg:template:query', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2256, '消息模板新增', 2254, 2, '#', '', 1, 0, 'F', '0', '0', 'msg:template:add', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2257, '消息模板修改', 2254, 3, '#', '', 1, 0, 'F', '0', '0', 'msg:template:edit', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2258, '消息模板删除', 2254, 4, '#', '', 1, 0, 'F', '0', '0', 'msg:template:remove', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2259, '消息模板导出', 2254, 5, '#', '', 1, 0, 'F', '0', '0', 'msg:template:export', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2260, '短信通知', 2199, 1, 'sms', 'msg/sms/index', 1, 0, 'M', '0', '0', '', '#', 'admin', now(), 'admin', now(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2261, '邮件通知', 2199, 2, 'mail', 'msg/mail/index', 1, 0, 'M', '0', '0', '', '#', 'admin', now(), 'admin', now(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2262, '微信通知', 2199, 3, 'wechat', 'msg/wechat/index', 1, 0, 'M', '0', '0', '', '#', 'admin', now(), 'admin', now(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2263, '钉钉通知', 2199, 4, 'dingtalk', 'msg/dingtalk/index', 1, 0, 'M', '0', '0', '', '#', 'admin', now(), 'admin', now(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2264, '通知日志', 2199, 6, 'log', 'msg/log/index', 1, 0, 'M', '0', '0', '', '#', 'admin', now(), 'admin', now(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2265, '公众号菜单', 2262, 3, 'weixinMenu', 'msg/wechat/menu/index', 1, 0, 'C', '0', '0', 'msg:weixinMenu:list', '#', 'admin', now(), 'admin', now(), '微信公众号菜单菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2266, '微信公众号菜单查询', 2265, 1, '#', '', 1, 0, 'F', '0', '0', 'msg:weixinMenu:query', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2267, '微信公众号菜单新增', 2265, 2, '#', '', 1, 0, 'F', '0', '0', 'msg:weixinMenu:add', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2268, '微信公众号菜单修改', 2265, 3, '#', '', 1, 0, 'F', '0', '0', 'msg:weixinMenu:edit', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2269, '微信公众号菜单删除', 2265, 4, '#', '', 1, 0, 'F', '0', '0', 'msg:weixinMenu:remove', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2270, '微信公众号菜单导出', 2265, 5, '#', '', 1, 0, 'F', '0', '0', 'msg:weixinMenu:export', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2271, '菜单响应', 2262, 4, 'weixinMenuReply', 'msg/wechat/menuReply/index', 1, 0, 'C', '0', '0', 'msg:weixinMenuReply:list', '#', 'admin', now(), 'admin', now(), '微信公众号菜单回复菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2272, '微信公众号菜单回复查询', 2271, 1, '#', '', 1, 0, 'F', '0', '0', 'msg:weixinMenuReply:query', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2273, '微信公众号菜单回复新增', 2271, 2, '#', '', 1, 0, 'F', '0', '0', 'msg:weixinMenuReply:add', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2274, '微信公众号菜单回复修改', 2271, 3, '#', '', 1, 0, 'F', '0', '0', 'msg:weixinMenuReply:edit', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2275, '微信公众号菜单回复删除', 2271, 4, '#', '', 1, 0, 'F', '0', '0', 'msg:weixinMenuReply:remove', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2276, '微信公众号菜单回复导出', 2271, 5, '#', '', 1, 0, 'F', '0', '0', 'msg:weixinMenuReply:export', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2277, '发送消息', 2199, 7, 'send', 'msg/send/index', 1, 0, 'M', '0', '0', '', '#', 'admin', now(), 'admin', now(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2278, '发送短信', 2277, 1, 'sms', 'msg/send/sms/index', 1, 0, 'C', '0', '0', 'msg:send:sms', '#', 'admin', now(), 'admin', now(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2279, '发送邮件', 2277, 2, 'mail', 'msg/send/mail/index', 1, 0, 'C', '0', '0', 'msg:send:mail', '#', 'admin', now(), 'admin', now(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2280, '发送微信', 2277, 3, 'wechat', 'msg/send/wechat/index', 1, 0, 'C', '0', '0', 'msg:send:weixin', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2281, '发送钉钉', 2277, 4, 'dingtalk', 'msg/send/dingtalk/index', 1, 0, 'C', '0', '0', 'msg:send:dingtalk', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2282, '钉媒体上传', 2277, 5, 'uploadMedia', 'msg/send/dingtalk/upload-media', 1, 0, 'C', '0', '0', 'msg:send:dingMediaUpload', '#', 'admin', now(), 'admin', now(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2284, '交易日志', 0, 10, 'tradelog', NULL, 1, 0, 'M', '0', '0', NULL, 'logmgr', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2285, 'OCR日志', 2284, 2, 'ocr', 'ocr/index', 1, 0, 'M', '0', '0', '', '#', 'admin', now(), 'admin', now(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2297, '人脸比对', 2406, 1, 'facematch', 'tradelog/facematch/index', 1, 0, 'C', '0', '0', 'tradelog:facematch:list', '#', 'admin', now(), 'admin', now(), '人脸比对日志菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2298, '人脸比对日志查询', 2297, 1, '#', '', 1, 0, 'F', '0', '0', 'tradelog:facematch:query', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2302, '人脸比对日志导出', 2297, 5, '#', '', 1, 0, 'F', '0', '0', 'tradelog:facematch:export', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2303, '人脸搜索', 2406, 2, 'facesearch', 'tradelog/facesearch/index', 1, 0, 'C', '0', '0', 'tradelog:facesearch:list', '#', 'admin', now(), 'admin', now(), '人脸搜索日志菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2304, '人脸搜索日志查询', 2303, 1, '#', '', 1, 0, 'F', '0', '0', 'tradelog:facesearch:query', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2308, '人脸搜索日志导出', 2303, 5, '#', '', 1, 0, 'F', '0', '0', 'tradelog:facesearch:export', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2321, '指纹比对', 2406, 3, 'fingermatch', 'tradelog/fingermatch/index', 1, 0, 'C', '0', '0', 'tradelog:fingermatch:list', '#', 'admin', now(), 'admin', now(), '指纹比对日志菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2322, '指纹比对日志查询', 2321, 1, '#', '', 1, 0, 'F', '0', '0', 'tradelog:fingermatch:query', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2326, '指纹比对日志导出', 2321, 5, '#', '', 1, 0, 'F', '0', '0', 'tradelog:fingermatch:export', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2327, '指纹搜索', 2406, 4, 'fingersearch', 'tradelog/fingersearch/index', 1, 0, 'C', '0', '0', 'tradelog:fingersearch:list', '#', 'admin', now(), 'admin', now(), '指纹搜索日志菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2328, '指纹搜索日志查询', 2327, 1, '#', '', 1, 0, 'F', '0', '0', 'tradelog:fingersearch:query', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2332, '指纹搜索日志导出', 2327, 5, '#', '', 1, 0, 'F', '0', '0', 'tradelog:fingersearch:export', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2333, '虹膜比对', 2406, 5, 'irismatch', 'tradelog/irismatch/index', 1, 0, 'C', '0', '0', 'tradelog:irismatch:list', '#', 'admin', now(), 'admin', now(), '虹膜比对日志菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2334, '虹膜比对日志查询', 2333, 1, '#', '', 1, 0, 'F', '0', '0', 'tradelog:irismatch:query', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2338, '虹膜比对日志导出', 2333, 5, '#', '', 1, 0, 'F', '0', '0', 'tradelog:irismatch:export', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2339, '虹膜搜索', 2406, 6, 'irissearch', 'tradelog/irissearch/index', 1, 0, 'C', '0', '0', 'tradelog:irissearch:list', '#', 'admin', now(), 'admin', now(), '虹膜搜索日志菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2340, '虹膜搜索日志查询', 2339, 1, '#', '', 1, 0, 'F', '0', '0', 'tradelog:irissearch:query', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2344, '虹膜搜索日志导出', 2339, 5, '#', '', 1, 0, 'F', '0', '0', 'tradelog:irissearch:export', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2351, '银行卡', 2285, 1, 'bankcard', 'ocr/bankcard/index', 1, 0, 'C', '0', '0', 'ocr:bankcard:list', '#', 'admin', now(), 'admin', now(), '银行卡OCR识别记录菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2352, '银行卡OCR识别记录查询', 2351, 1, '#', '', 1, 0, 'F', '0', '0', 'ocr:bankcard:query', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2356, '银行卡OCR识别记录导出', 2351, 5, '#', '', 1, 0, 'F', '0', '0', 'ocr:bankcard:export', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2357, '营业执照', 2285, 2, 'busilic', 'ocr/busilic/index', 1, 0, 'C', '0', '0', 'ocr:busilic:list', '#', 'admin', now(), '', NULL, '营业执照OCR识别记录菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2358, '营业执照OCR识别记录查询', 2357, 1, '#', '', 1, 0, 'F', '0', '0', 'ocr:busilic:query', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2362, '营业执照OCR识别记录导出', 2357, 5, '#', '', 1, 0, 'F', '0', '0', 'ocr:busilic:export', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2363, '驾驶证', 2285, 3, 'driverlic', 'ocr/driverlic/index', 1, 0, 'C', '0', '0', 'ocr:driverlic:list', '#', 'admin', now(), '', NULL, '驾驶证OCR识别记录菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2364, '驾驶证OCR识别记录查询', 2363, 1, '#', '', 1, 0, 'F', '0', '0', 'ocr:driverlic:query', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2368, '驾驶证OCR识别记录导出', 2363, 5, '#', '', 1, 0, 'F', '0', '0', 'ocr:driverlic:export', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2369, '行驶证', 2285, 4, 'drivinglic', 'ocr/drivinglic/index', 1, 0, 'C', '0', '0', 'ocr:drivinglic:list', '#', 'admin', now(), '', NULL, '行驶证OCR识别记录菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2370, '行驶证OCR识别记录查询', 2369, 1, '#', '', 1, 0, 'F', '0', '0', 'ocr:drivinglic:query', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2374, '行驶证OCR识别记录导出', 2369, 5, '#', '', 1, 0, 'F', '0', '0', 'ocr:drivinglic:export', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2375, '港澳通行证', 2285, 5, 'hkmac', 'ocr/hkmac/index', 1, 0, 'C', '0', '0', 'ocr:hkmac:list', '#', 'admin', now(), '', NULL, '港澳通行证OCR识别记录菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2376, '港澳通行证OCR识别记录查询', 2375, 1, '#', '', 1, 0, 'F', '0', '0', 'ocr:hkmac:query', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2380, '港澳通行证OCR识别记录导出', 2375, 5, '#', '', 1, 0, 'F', '0', '0', 'ocr:hkmac:export', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2381, '身份证背面', 2285, 6, 'idcardback', 'ocr/idcardback/index', 1, 0, 'C', '0', '0', 'ocr:idcardback:list', '#', 'admin', now(), '', NULL, '身份证背面OCR识别记录菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2382, '身份证背面OCR识别记录查询', 2381, 1, '#', '', 1, 0, 'F', '0', '0', 'ocr:idcardback:query', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2386, '身份证背面OCR识别记录导出', 2381, 5, '#', '', 1, 0, 'F', '0', '0', 'ocr:idcardback:export', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2387, '身份证正面', 2285, 7, 'idcardfront', 'ocr/idcardfront/index', 1, 0, 'C', '0', '0', 'ocr:idcardfront:list', '#', 'admin', now(), '', NULL, '身份证正面OCR识别记录菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2388, '身份证正面OCR识别记录查询', 2387, 1, '#', '', 1, 0, 'F', '0', '0', 'ocr:idcardfront:query', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2392, '身份证正面OCR识别记录导出', 2387, 5, '#', '', 1, 0, 'F', '0', '0', 'ocr:idcardfront:export', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2393, '护照', 2285, 8, 'passport', 'ocr/passport/index', 1, 0, 'C', '0', '0', 'ocr:passport:list', '#', 'admin', now(), '', NULL, '护照OCR识别记录菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2394, '护照OCR识别记录查询', 2393, 1, '#', '', 1, 0, 'F', '0', '0', 'ocr:passport:query', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2398, '护照OCR识别记录导出', 2393, 5, '#', '', 1, 0, 'F', '0', '0', 'ocr:passport:export', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2399, '人脸虹膜搜索', 2406, 7, 'faceirisSearch', 'tradelog/faceirisSearch/index', 1, 0, 'C', '0', '0', 'tradelog:faceirisSearch:list', '#', 'admin', now(), 'admin', now(), '人脸虹膜搜索日志菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2400, '人脸虹膜搜索日志查询', 2399, 1, '#', '', 1, 0, 'F', '0', '0', 'tradelog:faceirisSearch:query', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2404, '人脸虹膜搜索日志导出', 2399, 5, '#', '', 1, 0, 'F', '0', '0', 'tradelog:faceirisSearch:export', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2406, '比对日志', 2284, 1, 'compare', 'tradelog/index', 1, 0, 'M', '0', '0', '', '#', 'admin', now(), 'admin', now(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2407, '健康码日志', 2284, 3, 'healthcode', 'tradelog/healthcode/index', 1, 0, 'C', '0', '0', 'tradelog:healthcode:list', '#', 'admin', now(), 'admin', now(), '健康码请求菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2408, '健康码请求查询', 2407, 1, '#', '', 1, 0, 'F', '0', '0', 'tradelog:healthcode:query', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2412, '健康码请求导出', 2407, 5, '#', '', 1, 0, 'F', '0', '0', 'tradelog:healthcode:export', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2499, '统计中心', 2109, 8, 'center', 'noninductive/statistics/center', 1, 0, 'C', '1', '0', '', '#', 'admin', NULL, 'admin', now(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2500, '无感设备信息', 2109, 7, 'noninductive', 'noninductive/device/index', 1, 0, 'C', '1', '0', 'noninductive:device:list', '#', 'admin', now(), 'admin', now(), '设备信息菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2501, '接口报文', 2284, 4, 'reqrecord', 'tradelog/reqrecord/index', 1, 0, 'C', '0', '0', 'tradelog:reqrecord:list', '#', 'admin', now(), 'admin', now(), '接口交易请求记录菜单');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2502, '接口交易请求记录查询', 2501, 1, '#', '', 1, 0, 'F', '0', '0', 'tradelog:reqrecord:query', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2507, '比对体验', 0, 11, 'webtrade', NULL, 1, 0, 'M', '0', '0', '', 'example', 'admin', now(), 'admin', now(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2508, '人脸1v1比对', 2507, 1, 'faceMatchOne', 'webtrade/face/MatchOne', 1, 0, 'C', '0', '0', 'webtrade:face:matchone', '#', 'admin', now(), 'admin', now(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2509, '人脸1vN比对', 2507, 2, 'faceMatchN', 'webtrade/face/MatchN', 1, 0, 'C', '0', '0', 'webtrade:face:matchn', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2510, '人脸图片比较', 2507, 3, 'faceComparetwo', 'webtrade/face/CompareTwo', 1, 0, 'C', '0', '0', 'webtrade:face:comparetwo', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2511, '指纹1v1比对', 2507, 4, 'fingerMatchOne', 'webtrade/finger/MatchOne', 1, 0, 'C', '0', '0', 'webtrade:finger:matchone', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2512, '指纹1vN比对', 2507, 5, 'fingerMatchN', 'webtrade/finger/MatchN', 1, 0, 'C', '0', '0', 'webtrade:finger:matchn', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2513, '指纹图片比对', 2507, 6, 'fingerComparetwo', 'webtrade/finger/CompareTwo', 1, 0, 'C', '0', '0', 'webtrade:finger:comparetwo', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2514, '虹膜1v1比对', 2507, 7, 'irisMatchOne', 'webtrade/iris/MatchOne', 1, 0, 'C', '0', '0', 'webtrade:iris:matchone', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2515, '虹膜1vN比对', 2507, 8, 'irisMatchN', 'webtrade/iris/MatchN', 1, 0, 'C', '0', '0', 'webtrade:iris:matchn', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2516, '虹膜图片比对', 2507, 9, 'irisCompareTwo', 'webtrade/iris/CompareTwo', 1, 0, 'C', '0', '0', 'webtrade:iris:comparetwo', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2517, '多模态1v1比对', 2507, 10, 'faceirisMatchOne', 'webtrade/faceiris/MatchOne', 1, 0, 'C', '0', '0', 'webtrade:faceiris:matchone', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2518, '二维码生成', 3, 4, 'qrcode', 'tool/qrcode/index', 1, 0, 'C', '0', '0', 'tool:qrcode:gen', '#', 'admin', now(), 'admin', now(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2519, '租户管理', 0, 1, 'tenantmgr', NULL, 1, 0, 'M', '0', '0', '', 'tenant', 'admin', now(), 'admin', now(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2520, '租户角色', 2519, 12, 'tenantrole', 'system/tenant/role/index', 1, 0, 'C', '0', '0', 'system:tenantrole:list', '#', 'admin', now(), 'admin', now(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2521, '租户角色查询', 2520, 1, '', '', 1, 0, 'F', '0', '0', 'system:tenantrole:query', '#', 'admin', now(), 'admin', now(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2522, '租户角色新增', 2520, 2, '', '', 1, 0, 'F', '0', '0', 'system:tenantrole:add', '#', 'admin', now(), 'admin', now(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2523, '租户角色修改', 2520, 3, '', '', 1, 0, 'F', '0', '0', 'system:tenantrole:edit', '#', 'admin', now(), 'admin', now(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2524, '租户角色删除', 2520, 4, '', '', 1, 0, 'F', '0', '0', 'system:tenantrole:remove', '#', 'admin', now(), 'admin', now(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2525, '租户角色导出', 2520, 5, '', '', 1, 0, 'F', '0', '0', 'system:tenantrole:export', '#', 'admin', now(), 'admin', now(), '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2526, '开通试用租户', 2006, 6, '', NULL, 1, 0, 'F', '0', '0', 'system:tenant:opentrial', '#', 'admin', now(), '', NULL, '');
SELECT setval('sys_menu_menu_id_seq', 2526, true);


INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2527, '无感设备信息查询', 2500, 1, '#', '', 1, 0, 'F', '0', '0', 'noninductive:device:query', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2528, '无感设备信息新增', 2500, 2, '#', '', 1, 0, 'F', '0', '0', 'noninductive:device:add', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2529, '无感设备信息修改', 2500, 3, '#', '', 1, 0, 'F', '0', '0', 'noninductive:device:edit', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2530, '无感设备信息删除', 2500, 4, '#', '', 1, 0, 'F', '0', '0', 'noninductive:device:remove', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2531, '无感设备信息导出', 2500, 5, '#', '', 1, 0, 'F', '0', '0', 'noninductive:device:export', '#', 'admin', now(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES (2532, '无感设备信息导入', 2500, 6, '', NULL, 1, 0, 'F', '0', '0', 'noninductive:device:import', '#', 'admin', now(), '', NULL, '');

SELECT setval('sys_menu_menu_id_seq', 2532, true);
