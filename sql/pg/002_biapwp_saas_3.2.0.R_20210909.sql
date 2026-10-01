-- ----------------------------
-- 检活日志表
-- ----------------------------
CREATE TABLE person_face_checklive_log (
  id varchar(48) NOT NULL,
  channel_code varchar(255),
  scene_image varchar(255) NOT NULL,
  scene_video varchar(255),
  checklive_score float8,
  checklive_msg varchar(255),
  checklive_result varchar(1),
  threshold float8,
  received_time timestamp NOT NULL,
  time_used INT4,
  server_id varchar(255),
  algs_version varchar(255),
  vendor_code varchar(255),
  create_time timestamp,
  batch_date timestamp,
  tenant_id varchar(255) DEFAULT 'super',
  CONSTRAINT pk_person_face_checklive_log PRIMARY KEY (id, received_time)
);
COMMENT ON COLUMN person_face_checklive_log.id IS '主键';
COMMENT ON COLUMN person_face_checklive_log.channel_code IS '渠道编码';
COMMENT ON COLUMN person_face_checklive_log.scene_image IS '现场照路径';
COMMENT ON COLUMN person_face_checklive_log.scene_video IS '现场视频路径';
COMMENT ON COLUMN person_face_checklive_log.threshold IS '检活阈值';
COMMENT ON COLUMN person_face_checklive_log.checklive_score IS '检活分值';
COMMENT ON COLUMN person_face_checklive_log.checklive_result IS '检活结果(0通过，1未通过)';
COMMENT ON COLUMN person_face_checklive_log.checklive_msg IS '检活结果信息';
COMMENT ON COLUMN person_face_checklive_log.received_time IS '请求时间';
COMMENT ON COLUMN person_face_checklive_log.time_used IS '耗时(ms)';
COMMENT ON COLUMN person_face_checklive_log.server_id IS '服务器标识';
COMMENT ON COLUMN person_face_checklive_log.vendor_code IS '厂商';
COMMENT ON COLUMN person_face_checklive_log.algs_version IS '算法版本';
COMMENT ON COLUMN person_face_checklive_log.create_time IS '创建时间';
COMMENT ON COLUMN person_face_checklive_log.batch_date IS '定时任务执行时间';
COMMENT ON COLUMN person_face_checklive_log.tenant_id IS '租户ID';
COMMENT ON TABLE person_face_checklive_log IS '人员人脸检活日志表';
create trigger trigger_person_face_checklive_log before insert on person_face_checklive_log for each row execute procedure ins_record_trigger_fun('yyyymmdd'); 

-- ----------------------------
-- 统计信息表
-- ----------------------------
CREATE TABLE statistic_device (
  id varchar(48) NOT NULL,
  device_num INT4 NOT NULL,
  online_num INT4 NOT NULL,
  offline_num INT4 NOT NULL,
  statistic_date varchar(48) NOT NULL,
  tenant_id varchar(255) DEFAULT 'super',
  CONSTRAINT pk_statistic_device PRIMARY KEY (id)
);
COMMENT ON COLUMN statistic_device.id IS '主键';
COMMENT ON COLUMN statistic_device.device_num IS '设备数量';
COMMENT ON COLUMN statistic_device.online_num IS '在线设备数量';
COMMENT ON COLUMN statistic_device.offline_num IS '离线设备数量';
COMMENT ON COLUMN statistic_device.statistic_date IS '统计日期';
COMMENT ON COLUMN statistic_device.tenant_id IS '租户ID';
COMMENT ON TABLE statistic_device IS '设备数量统计';


CREATE TABLE statistic_trade_log (
  id varchar(48) NOT NULL,
  trade_num INT4 NOT NULL ,
  pass_num INT4 NOT NULL,
  notpass_num INT4 NOT NULL,
  statistic_unit varchar(20) NOT NULL,
  statistic_date varchar(48) NOT NULL,
  tenant_id varchar(255) DEFAULT 'super',
  CONSTRAINT pk_statistic_trade_log PRIMARY KEY (id)
);
COMMENT ON COLUMN statistic_trade_log.id IS '主键';
COMMENT ON COLUMN statistic_trade_log.trade_num IS '交易数量';
COMMENT ON COLUMN statistic_trade_log.pass_num IS '通过数量';
COMMENT ON COLUMN statistic_trade_log.notpass_num IS '未通过数量';
COMMENT ON COLUMN statistic_trade_log.statistic_unit IS '统计单位(month：月，day：日)';
COMMENT ON COLUMN statistic_trade_log.statistic_date IS '统计日期';
COMMENT ON COLUMN statistic_trade_log.tenant_id IS '租户ID';
COMMENT ON TABLE statistic_trade_log IS '交易日志统计';

CREATE TABLE statistic_trade_person (
  id varchar(48) NOT NULL,
  pass_num INT4 NOT NULL,
  statistic_unit varchar(20) NOT NULL,
  statistic_date varchar(48) NOT NULL,
  tenant_id varchar(255) DEFAULT 'super',
  CONSTRAINT pk_statistic_trade_person PRIMARY KEY (id)
);
COMMENT ON COLUMN statistic_trade_person.id IS '主键';
COMMENT ON COLUMN statistic_trade_person.pass_num IS '人员数量';
COMMENT ON COLUMN statistic_trade_person.statistic_unit IS '统计单位(month：月，day：日)';
COMMENT ON COLUMN statistic_trade_person.statistic_date IS '统计日期';
COMMENT ON COLUMN statistic_trade_person.tenant_id IS '租户ID';
COMMENT ON TABLE statistic_trade_person IS '通行人员统计';

-- ----------------------------
-- 修改菜单、日志表结构
-- ----------------------------
ALTER TABLE sys_menu ADD menu_position varchar(10) DEFAULT 'SIDE';
COMMENT ON COLUMN sys_menu.menu_position IS '菜单位置(SIDE:左侧菜单项 TAB:右侧tab页)';
ALTER TABLE person_face_match_log ADD dept_name varchar(255);
COMMENT ON COLUMN person_face_match_log.dept_name IS '部门名称';
ALTER TABLE person_face_match_log ADD person_name varchar(255);
COMMENT ON COLUMN person_face_match_log.person_name IS '人员姓名';
ALTER TABLE person_face_search_log ADD dept_name varchar(255);
COMMENT ON COLUMN person_face_search_log.dept_name IS '部门名称';
ALTER TABLE person_face_search_log ADD person_name varchar(255);
COMMENT ON COLUMN person_face_search_log.person_name IS '人员姓名';
ALTER TABLE person_faceiris_search_log ADD dept_name varchar(255);
COMMENT ON COLUMN person_faceiris_search_log.dept_name IS '部门名称';
ALTER TABLE person_faceiris_search_log ADD person_name varchar(255);
COMMENT ON COLUMN person_faceiris_search_log.person_name IS '人员姓名';
ALTER TABLE person_finger_match_log ADD dept_name varchar(255);
COMMENT ON COLUMN person_finger_match_log.dept_name IS '部门名称';
ALTER TABLE person_finger_match_log ADD person_name varchar(255);
COMMENT ON COLUMN person_finger_match_log.person_name IS '人员姓名';
ALTER TABLE person_finger_search_log ADD dept_name varchar(255);
COMMENT ON COLUMN person_finger_search_log.dept_name IS '部门名称';
ALTER TABLE person_finger_search_log ADD person_name varchar(255);
COMMENT ON COLUMN person_finger_search_log.person_name IS '人员姓名';
ALTER TABLE person_iris_match_log ADD dept_name varchar(255);
COMMENT ON COLUMN person_iris_match_log.dept_name IS '部门名称';
ALTER TABLE person_iris_match_log ADD person_name varchar(255);
COMMENT ON COLUMN person_iris_match_log.person_name IS '人员姓名';
ALTER TABLE person_iris_search_log ADD dept_name varchar(255);
COMMENT ON COLUMN person_iris_search_log.dept_name IS '部门名称';
ALTER TABLE person_iris_search_log ADD person_name varchar(255);
COMMENT ON COLUMN person_iris_search_log.person_name IS '人员姓名';
	
-- ----------------------------
-- 字典
-- ----------------------------
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (147, '菜单位置', 'menu_positon', '0', 'admin',now(), '', NULL, '菜单位置');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (457, 0, '左侧菜单', 'SIDE', 'menu_positon', NULL, NULL, 'N', '0', 'admin', now(), '', NULL, '菜单位置--左侧菜单');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (458, 1, 'TAB页菜单', 'TAB', 'menu_positon', NULL, NULL, 'N', '0', 'admin', now(), 'admin', now(),  '菜单位置--TAB页菜单');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (459, 28, '设备升级信号查询', 'CLIENT_UPGRADE_SIGNAL', 'http_interface', NULL, NULL, 'N', '0', 'admin', now(), 'admin', now(),  '设备升级信号查询');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (460, 30, '设备升级结果回传', 'CLIENT_UPGRADE_RESULT_BAK', 'http_interface', NULL, NULL, 'N', '0', 'admin', now(), '', NULL, '设备升级结果回传');
SELECT setval('sys_dict_data_dict_code_seq', 460, true);
SELECT setval('sys_dict_type_dict_id_seq', 147, true);
-- ----------------------------
-- 菜单
-- ----------------------------
DELETE FROM sys_menu where perms like 'system:tenant:%';
DELETE FROM sys_menu where perms like 'system:tenantrole:%';
DELETE FROM sys_menu where path = 'tenantmgr';
DELETE FROM sys_menu where perms like 'scene:subtreasury:%';
DELETE FROM sys_menu where perms like 'scene:subtreasuryBusi:%';
DELETE FROM sys_menu where perms = 'scene:channel:list';
DELETE FROM sys_menu where perms = 'scene:channelBusi:list';

INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2534, '人员信息', 2012, 1, 'person', 'basedata/index', 1, 0, 'C', '0', '0', '', '#', 'admin', now(), 'admin', now(), '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2535, '场景管理', 2068, 0, 'scenemanage', 'scene/manage/index', 1, 0, 'C', '0', '0', 'scene:channel:list', '#', 'admin', now(), 'admin', now(), '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2536, '场景人员', 2068, 0, 'sceneperson', 'scene/person/index', 1, 0, 'C', '0', '0', 'scene:channelBusi:list', '#', 'admin', now(), 'admin', now(), '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2537, '工作台', 0, 0, 'workbench', ' ', 1, 0, 'M', '0', '0', '', 'job', 'admin', now(), 'admin', now(), '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2539, '数据分析', 2537, 1, 'analysis', 'workbench/analysis/index', 1, 0, 'C', '0', '0', NULL, '#', 'admin', now(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2542, '检活日志', 2284, 3, 'checklive', 'tradelog/checklive/index', 1, 0, 'C', '0', '0', 'tradelog:checklive:list', '#', 'admin', now(), 'admin', now(), '人员人脸检活日志菜单', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2543, '检活日志查询', 2542, 1, '#', '', 1, 0, 'F', '0', '0', 'tradelog:checklive:query', '#', 'admin', now(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2544, '检活日志导出', 2542, 2, '#', '', 1, 0, 'F', '0', '0', 'tradelog:checklive:export', '#', 'admin', now(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2546, '公众号授权', 2236, 6, '', NULL, 1, 0, 'F', '0', '0', 'msg:officalAccount:uploadMpAuthFile', '#', 'admin', now(),  '', NULL, '', 'SIDE');
SELECT setval('sys_menu_menu_id_seq',2546, true);
UPDATE sys_menu SET menu_name = '基本信息', parent_id = 2534, order_num = 1, path = 'personinfo', menu_position = 'TAB' WHERE perms='basedata:person:list';
UPDATE sys_menu SET parent_id = 2534 WHERE perms='basedata:face:list';
UPDATE sys_menu SET parent_id = 2534 WHERE perms='basedata:finger:list';
UPDATE sys_menu SET parent_id = 2534 WHERE perms='basedata:iris:list';
UPDATE sys_menu SET parent_id = 2534 WHERE perms='basedata:faceIris:list';
UPDATE sys_menu SET menu_position = 'TAB' WHERE perms like 'basedata:person:%';
UPDATE sys_menu SET menu_position = 'TAB' WHERE perms like 'basedata:face:%';
UPDATE sys_menu SET menu_position = 'TAB' WHERE perms like 'basedata:finger:%';
UPDATE sys_menu SET menu_position = 'TAB' WHERE perms like 'basedata:faceIris:%';
UPDATE sys_menu SET menu_position = 'TAB' WHERE perms like 'basedata:iris:%';
UPDATE sys_menu SET parent_id = 2535 WHERE perms like 'scene:channel:%' and perms != 'scene:channel:list';
UPDATE sys_menu SET parent_id = 2536 WHERE perms like 'scene:channelBusi:%' and perms != 'scene:channelBusi:list';
UPDATE sys_menu SET menu_name = '场景信息查询' WHERE menu_name ='渠道信息查询';
UPDATE sys_menu SET menu_name = '场景信息新增' WHERE menu_name ='渠道信息新增';
UPDATE sys_menu SET menu_name = '场景信息修改' WHERE menu_name ='渠道信息修改';
UPDATE sys_menu SET menu_name = '场景信息删除' WHERE menu_name ='渠道信息删除';
UPDATE sys_menu SET menu_name = '场景信息导出' WHERE menu_name ='渠道信息导出';
UPDATE sys_menu SET menu_name = '场景人员查询' WHERE menu_name ='渠道业务查询查询';
UPDATE sys_menu SET menu_name = '场景人员新增' WHERE menu_name ='渠道业务查询新增';
UPDATE sys_menu SET menu_name = '场景人员修改' WHERE menu_name ='渠道业务查询修改';
UPDATE sys_menu SET menu_name = '场景人员删除' WHERE menu_name ='渠道业务查询删除';
UPDATE sys_menu SET menu_name = '场景人员导出' WHERE menu_name ='渠道业务查询导出';
UPDATE sys_menu SET menu_name = '场景人员一键同步' WHERE menu_name ='渠道业务一键同步';
UPDATE sys_menu SET menu_name = '场景人员导入' WHERE menu_name ='渠道业务导入';
UPDATE sys_menu SET menu_name = '场景参数' WHERE menu_name ='渠道参数';
UPDATE sys_menu SET menu_name = '场景参数查询' WHERE menu_name ='渠道参数查询';
UPDATE sys_menu SET menu_name = '场景参数新增' WHERE menu_name ='渠道参数新增';
UPDATE sys_menu SET menu_name = '场景参数修改' WHERE menu_name ='渠道参数修改';
UPDATE sys_menu SET menu_name = '场景参数删除' WHERE menu_name ='渠道参数删除';
UPDATE sys_menu SET menu_name = '场景参数导出' WHERE menu_name ='渠道参数导出';
UPDATE sys_menu SET visible = '0' WHERE perms ='noninductive:device:list';


-- ----------------------------
-- 定时任务
-- ----------------------------
INSERT INTO sys_job(job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, update_by, update_time, remark) VALUES (16, '日通行人员数量统计', 'DEFAULT', 'statisticPassPersonTask.execute()', '0 0 4 * * ?', '1', '1', '0', 'admin', '2021-09-10 15:28:20.220684', '', '2021-09-10 15:30:27.571474', '');
INSERT INTO sys_job(job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, update_by, update_time, remark) VALUES (17, '日识别日志数量统计', 'DEFAULT', 'statisticTradeLogTask.execute()', '0 0 4 * * ?', '1', '1', '0', 'admin', '2021-09-10 15:28:36.103501', '', '2021-09-10 15:30:29.529249', '');
INSERT INTO sys_job(job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, update_by, update_time, remark) VALUES (18, '日设备数量统计', 'DEFAULT', 'statisticDeviceTask.execute()', '0 30 22 ? * *', '1', '1', '0', 'admin', '2021-09-10 15:29:03.864826', '', '2021-09-10 15:30:30.433712', '');
INSERT INTO sys_job(job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, update_by, update_time, remark) VALUES (19, '检活日志分区维护', 'DEFAULT', ' checkliveLogTask.clearAndAddPartition(365,false,''eyecool_assps'')', ' 0 0 2 * * ? *', '1', '1', '0', 'admin', '2021-09-10 15:30:21.791533', '', '2021-09-10 15:30:32.458008', '');
INSERT INTO qrtz_job_details(sched_name, job_name, job_group, description, job_class_name, is_durable, is_nonconcurrent, is_update_data, requests_recovery, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME16', 'DEFAULT', NULL, 'cn.eyecool.quartz.util.QuartzDisallowConcurrentExecution', 'false', 'true', 'false', 'false', E'\\254\\355\\000\\005sr\\000\\025org.quartz.JobDataMap\\237\\260\\203\\350\\277\\251\\260\\313\\002\\000\\000xr\\000&org.quartz.utils.StringKeyDirtyFlagMap\\202\\010\\350\\303\\373\\305](\\002\\000\\001Z\\000\\023allowsTransientDataxr\\000\\035org.quartz.utils.DirtyFlagMap\\023\\346.\\255(v\\012\\316\\002\\000\\002Z\\000\\005dirtyL\\000\\003mapt\\000\\017Ljava/util/Map;xp\\001sr\\000\\021java.util.HashMap\\005\\007\\332\\301\\303\\026`\\321\\003\\000\\002F\\000\\012loadFactorI\\000\\011thresholdxp?@\\000\\000\\000\\000\\000\\014w\\010\\000\\000\\000\\020\\000\\000\\000\\001t\\000\\017TASK_PROPERTIESsr\\000\\037cn.eyecool.quartz.domain.SysJob\\000\\000\\000\\000\\000\\000\\000\\001\\002\\000\\010L\\000\\012concurrentt\\000\\022Ljava/lang/String;L\\000\\016cronExpressionq\\000~\\000\\011L\\000\\014invokeTargetq\\000~\\000\\011L\\000\\010jobGroupq\\000~\\000\\011L\\000\\005jobIdt\\000\\020Ljava/lang/Long;L\\000\\007jobNameq\\000~\\000\\011L\\000\\015misfirePolicyq\\000~\\000\\011L\\000\\006statusq\\000~\\000\\011xr\\000(cn.eyecool.common.core.domain.BaseEntity\\000\\000\\000\\000\\000\\000\\000\\001\\002\\000\\011L\\000\\011beginTimeq\\000~\\000\\011L\\000\\010createByq\\000~\\000\\011L\\000\\012createTimet\\000\\020Ljava/util/Date;L\\000\\007endTimeq\\000~\\000\\011L\\000\\006paramsq\\000~\\000\\003L\\000\\006remarkq\\000~\\000\\011L\\000\\013searchValueq\\000~\\000\\011L\\000\\010updateByq\\000~\\000\\011L\\000\\012updateTimeq\\000~\\000\\014xppt\\000\\005adminpppppppt\\000\\0011t\\000\\0130 0 4 * * ?t\\000!statisticPassPersonTask.execute()t\\000\\007DEFAULTsr\\000\\016java.lang.Long;\\213\\344\\220\\314\\217#\\337\\002\\000\\001J\\000\\005valuexr\\000\\020java.lang.Number\\206\\254\\225\\035\\013\\224\\340\\213\\002\\000\\000xp\\000\\000\\000\\000\\000\\000\\000\\020t\\000\\033\\346\\227\\245\\351\\200\\232\\350\\241\\214\\344\\272\\272\\345\\221\\230\\346\\225\\260\\351\\207\\217\\347\\273\\237\\350\\256\\241t\\000\\0011t\\000\\0011x\\000');
INSERT INTO qrtz_job_details(sched_name, job_name, job_group, description, job_class_name, is_durable, is_nonconcurrent, is_update_data, requests_recovery, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME17', 'DEFAULT', NULL, 'cn.eyecool.quartz.util.QuartzDisallowConcurrentExecution', 'false', 'true', 'false', 'false', E'\\254\\355\\000\\005sr\\000\\025org.quartz.JobDataMap\\237\\260\\203\\350\\277\\251\\260\\313\\002\\000\\000xr\\000&org.quartz.utils.StringKeyDirtyFlagMap\\202\\010\\350\\303\\373\\305](\\002\\000\\001Z\\000\\023allowsTransientDataxr\\000\\035org.quartz.utils.DirtyFlagMap\\023\\346.\\255(v\\012\\316\\002\\000\\002Z\\000\\005dirtyL\\000\\003mapt\\000\\017Ljava/util/Map;xp\\001sr\\000\\021java.util.HashMap\\005\\007\\332\\301\\303\\026`\\321\\003\\000\\002F\\000\\012loadFactorI\\000\\011thresholdxp?@\\000\\000\\000\\000\\000\\014w\\010\\000\\000\\000\\020\\000\\000\\000\\001t\\000\\017TASK_PROPERTIESsr\\000\\037cn.eyecool.quartz.domain.SysJob\\000\\000\\000\\000\\000\\000\\000\\001\\002\\000\\010L\\000\\012concurrentt\\000\\022Ljava/lang/String;L\\000\\016cronExpressionq\\000~\\000\\011L\\000\\014invokeTargetq\\000~\\000\\011L\\000\\010jobGroupq\\000~\\000\\011L\\000\\005jobIdt\\000\\020Ljava/lang/Long;L\\000\\007jobNameq\\000~\\000\\011L\\000\\015misfirePolicyq\\000~\\000\\011L\\000\\006statusq\\000~\\000\\011xr\\000(cn.eyecool.common.core.domain.BaseEntity\\000\\000\\000\\000\\000\\000\\000\\001\\002\\000\\011L\\000\\011beginTimeq\\000~\\000\\011L\\000\\010createByq\\000~\\000\\011L\\000\\012createTimet\\000\\020Ljava/util/Date;L\\000\\007endTimeq\\000~\\000\\011L\\000\\006paramsq\\000~\\000\\003L\\000\\006remarkq\\000~\\000\\011L\\000\\013searchValueq\\000~\\000\\011L\\000\\010updateByq\\000~\\000\\011L\\000\\012updateTimeq\\000~\\000\\014xppt\\000\\005adminpppppppt\\000\\0011t\\000\\0130 0 4 * * ?t\\000\\037statisticTradeLogTask.execute()t\\000\\007DEFAULTsr\\000\\016java.lang.Long;\\213\\344\\220\\314\\217#\\337\\002\\000\\001J\\000\\005valuexr\\000\\020java.lang.Number\\206\\254\\225\\035\\013\\224\\340\\213\\002\\000\\000xp\\000\\000\\000\\000\\000\\000\\000\\021t\\000\\033\\346\\227\\245\\350\\257\\206\\345\\210\\253\\346\\227\\245\\345\\277\\227\\346\\225\\260\\351\\207\\217\\347\\273\\237\\350\\256\\241t\\000\\0011t\\000\\0011x\\000');
INSERT INTO qrtz_job_details(sched_name, job_name, job_group, description, job_class_name, is_durable, is_nonconcurrent, is_update_data, requests_recovery, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME18', 'DEFAULT', NULL, 'cn.eyecool.quartz.util.QuartzDisallowConcurrentExecution', 'false', 'true', 'false', 'false', E'\\254\\355\\000\\005sr\\000\\025org.quartz.JobDataMap\\237\\260\\203\\350\\277\\251\\260\\313\\002\\000\\000xr\\000&org.quartz.utils.StringKeyDirtyFlagMap\\202\\010\\350\\303\\373\\305](\\002\\000\\001Z\\000\\023allowsTransientDataxr\\000\\035org.quartz.utils.DirtyFlagMap\\023\\346.\\255(v\\012\\316\\002\\000\\002Z\\000\\005dirtyL\\000\\003mapt\\000\\017Ljava/util/Map;xp\\001sr\\000\\021java.util.HashMap\\005\\007\\332\\301\\303\\026`\\321\\003\\000\\002F\\000\\012loadFactorI\\000\\011thresholdxp?@\\000\\000\\000\\000\\000\\014w\\010\\000\\000\\000\\020\\000\\000\\000\\001t\\000\\017TASK_PROPERTIESsr\\000\\037cn.eyecool.quartz.domain.SysJob\\000\\000\\000\\000\\000\\000\\000\\001\\002\\000\\010L\\000\\012concurrentt\\000\\022Ljava/lang/String;L\\000\\016cronExpressionq\\000~\\000\\011L\\000\\014invokeTargetq\\000~\\000\\011L\\000\\010jobGroupq\\000~\\000\\011L\\000\\005jobIdt\\000\\020Ljava/lang/Long;L\\000\\007jobNameq\\000~\\000\\011L\\000\\015misfirePolicyq\\000~\\000\\011L\\000\\006statusq\\000~\\000\\011xr\\000(cn.eyecool.common.core.domain.BaseEntity\\000\\000\\000\\000\\000\\000\\000\\001\\002\\000\\011L\\000\\011beginTimeq\\000~\\000\\011L\\000\\010createByq\\000~\\000\\011L\\000\\012createTimet\\000\\020Ljava/util/Date;L\\000\\007endTimeq\\000~\\000\\011L\\000\\006paramsq\\000~\\000\\003L\\000\\006remarkq\\000~\\000\\011L\\000\\013searchValueq\\000~\\000\\011L\\000\\010updateByq\\000~\\000\\011L\\000\\012updateTimeq\\000~\\000\\014xppt\\000\\005adminpppppppt\\000\\0011t\\000\\0150 30 22 ? * *t\\000\\035statisticDeviceTask.execute()t\\000\\007DEFAULTsr\\000\\016java.lang.Long;\\213\\344\\220\\314\\217#\\337\\002\\000\\001J\\000\\005valuexr\\000\\020java.lang.Number\\206\\254\\225\\035\\013\\224\\340\\213\\002\\000\\000xp\\000\\000\\000\\000\\000\\000\\000\\022t\\000\\025\\346\\227\\245\\350\\256\\276\\345\\244\\207\\346\\225\\260\\351\\207\\217\\347\\273\\237\\350\\256\\241t\\000\\0011t\\000\\0011x\\000');
INSERT INTO qrtz_job_details(sched_name, job_name, job_group, description, job_class_name, is_durable, is_nonconcurrent, is_update_data, requests_recovery, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME19', 'DEFAULT', NULL, 'cn.eyecool.quartz.util.QuartzDisallowConcurrentExecution', 'false', 'true', 'false', 'false', E'\\254\\355\\000\\005sr\\000\\025org.quartz.JobDataMap\\237\\260\\203\\350\\277\\251\\260\\313\\002\\000\\000xr\\000&org.quartz.utils.StringKeyDirtyFlagMap\\202\\010\\350\\303\\373\\305](\\002\\000\\001Z\\000\\023allowsTransientDataxr\\000\\035org.quartz.utils.DirtyFlagMap\\023\\346.\\255(v\\012\\316\\002\\000\\002Z\\000\\005dirtyL\\000\\003mapt\\000\\017Ljava/util/Map;xp\\001sr\\000\\021java.util.HashMap\\005\\007\\332\\301\\303\\026`\\321\\003\\000\\002F\\000\\012loadFactorI\\000\\011thresholdxp?@\\000\\000\\000\\000\\000\\014w\\010\\000\\000\\000\\020\\000\\000\\000\\001t\\000\\017TASK_PROPERTIESsr\\000\\037cn.eyecool.quartz.domain.SysJob\\000\\000\\000\\000\\000\\000\\000\\001\\002\\000\\010L\\000\\012concurrentt\\000\\022Ljava/lang/String;L\\000\\016cronExpressionq\\000~\\000\\011L\\000\\014invokeTargetq\\000~\\000\\011L\\000\\010jobGroupq\\000~\\000\\011L\\000\\005jobIdt\\000\\020Ljava/lang/Long;L\\000\\007jobNameq\\000~\\000\\011L\\000\\015misfirePolicyq\\000~\\000\\011L\\000\\006statusq\\000~\\000\\011xr\\000(cn.eyecool.common.core.domain.BaseEntity\\000\\000\\000\\000\\000\\000\\000\\001\\002\\000\\011L\\000\\011beginTimeq\\000~\\000\\011L\\000\\010createByq\\000~\\000\\011L\\000\\012createTimet\\000\\020Ljava/util/Date;L\\000\\007endTimeq\\000~\\000\\011L\\000\\006paramsq\\000~\\000\\003L\\000\\006remarkq\\000~\\000\\011L\\000\\013searchValueq\\000~\\000\\011L\\000\\010updateByq\\000~\\000\\011L\\000\\012updateTimeq\\000~\\000\\014xppt\\000\\005adminpppppppt\\000\\0011t\\000\\016 0 0 2 * * ? *t\\000A checkliveLogTask.clearAndAddPartition(365,false,''eyecool_assps'')t\\000\\007DEFAULTsr\\000\\016java.lang.Long;\\213\\344\\220\\314\\217#\\337\\002\\000\\001J\\000\\005valuexr\\000\\020java.lang.Number\\206\\254\\225\\035\\013\\224\\340\\213\\002\\000\\000xp\\000\\000\\000\\000\\000\\000\\000\\023t\\000\\030\\346\\243\\200\\346\\264\\273\\346\\227\\245\\345\\277\\227\\345\\210\\206\\345\\214\\272\\347\\273\\264\\346\\212\\244t\\000\\0011t\\000\\0011x\\000');
INSERT INTO qrtz_triggers(sched_name, trigger_name, trigger_group, job_name, job_group, description, next_fire_time, prev_fire_time, priority, trigger_state, trigger_type, start_time, end_time, calendar_name, misfire_instr, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME16', 'DEFAULT', 'TASK_CLASS_NAME16', 'DEFAULT', NULL, 1631304000000, -1, 5, 'WAITING', 'CRON', 1631258899000, 0, NULL, -1, E'\\\\x');
INSERT INTO qrtz_triggers(sched_name, trigger_name, trigger_group, job_name, job_group, description, next_fire_time, prev_fire_time, priority, trigger_state, trigger_type, start_time, end_time, calendar_name, misfire_instr, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME17', 'DEFAULT', 'TASK_CLASS_NAME17', 'DEFAULT', NULL, 1631304000000, -1, 5, 'WAITING', 'CRON', 1631258915000, 0, NULL, -1, E'\\\\x');
INSERT INTO qrtz_triggers(sched_name, trigger_name, trigger_group, job_name, job_group, description, next_fire_time, prev_fire_time, priority, trigger_state, trigger_type, start_time, end_time, calendar_name, misfire_instr, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME18', 'DEFAULT', 'TASK_CLASS_NAME18', 'DEFAULT', NULL, 1631284200000, -1, 5, 'WAITING', 'CRON', 1631258943000, 0, NULL, -1, E'\\\\x');
INSERT INTO qrtz_triggers(sched_name, trigger_name, trigger_group, job_name, job_group, description, next_fire_time, prev_fire_time, priority, trigger_state, trigger_type, start_time, end_time, calendar_name, misfire_instr, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME19', 'DEFAULT', 'TASK_CLASS_NAME19', 'DEFAULT', NULL, 1631296800000, -1, 5, 'WAITING', 'CRON', 1631259021000, 0, NULL, -1, E'\\\\x');
INSERT INTO qrtz_cron_triggers(sched_name, trigger_name, trigger_group, cron_expression, time_zone_id) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME16', 'DEFAULT', '0 0 4 * * ?', 'Asia/Shanghai');
INSERT INTO qrtz_cron_triggers(sched_name, trigger_name, trigger_group, cron_expression, time_zone_id) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME17', 'DEFAULT', '0 0 4 * * ?', 'Asia/Shanghai');
INSERT INTO qrtz_cron_triggers(sched_name, trigger_name, trigger_group, cron_expression, time_zone_id) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME18', 'DEFAULT', '0 30 22 ? * *', 'Asia/Shanghai');
INSERT INTO qrtz_cron_triggers(sched_name, trigger_name, trigger_group, cron_expression, time_zone_id) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME19', 'DEFAULT', ' 0 0 2 * * ? *', 'Asia/Shanghai');
SELECT setval('sys_job_job_id_seq', 19, true);

-- ----------------------------
-- 今日交易日志视图
-- ----------------------------
CREATE 
	OR REPLACE VIEW view_today_trans_log AS SELECT
	received_seq,
	received_time,
	result,
	unique_id,
	person_name,
	device_no,
	log.device_name,
	device.device_addr,
	'人脸' AS bio_type,
	'人脸1:N识别' AS busi_type,
	log.tenant_id 
FROM
	person_face_search_log log
	LEFT JOIN device_info device ON log.device_code = device.device_no 
WHERE
	log.device_code IS NOT NULL 
	AND log.received_time >= current_date UNION ALL
SELECT
	received_seq,
	received_time,
	result,
	unique_id,
	person_name,
	device_sn AS device_no,
	log.device_name,
	device.device_addr,
	'虹膜人脸多模态' AS bio_type,
	'虹膜人脸多模态1:N识别' AS busi_type,
	log.tenant_id 
FROM
	person_faceiris_search_log log
	LEFT JOIN device_info device ON log.device_sn = device.device_no 
WHERE
	log.device_sn IS NOT NULL 
	AND log.received_time >= current_date;
	
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (173, '人脸检活图片存放目录', 'busi.face.checklive.dir', './eyecool/busi/face/checklive/', 'Y', 'admin', now(), '', NULL, '人脸检活图片存放目录', 'N');
SELECT setval('sys_config_config_id_seq', 173, true);
update sys_config set remark = '平台是否支持自动绑定和解绑人库关系（用于部分场景需要绑定全库人员情况，人员入库直接和相关场景绑定）' where config_key = 'platform.libperson.isAutobind';
update sys_config set config_name = '平台需要自动绑定人库关系的场景编码', remark = '平台需要自动绑定人库关系的场景编码(用于部分场景需要绑定全库人员情况，仅在开启自动绑定时有效，多个场景编码使用\;\分割)' where config_key = 'platform.libperson.autobind.channel';
update sys_config set config_name = '生物信息变化触发信息同步的场景编码', remark = '配置生物特征变化才会触发更新的场景编码，多个使用;隔开' where config_key = 'person.biochange.liveupdate.channel';
update sys_dict_type set dict_name = '场景1-N识别方式' where dict_type = 'search_n_type';
update sys_dict_type set dict_name = '场景管理认证类型', remark = '场景管理认证类型' where dict_type = 'channel_bio_attest_type';
update sys_dict_type set dict_name = '场景管理数据来源', remark = '场景管理数据来源'  where dict_type = 'channel_business_source';
update sys_dict_data set dict_label = '校验子场景', remark = '1-N识别方式-校验子场景' where dict_type = 'search_n_type' and dict_value = '1';
update sys_dict_data set dict_label = '校验场景', remark = '1-N识别方式-校验场景' where dict_type = 'search_n_type' and dict_value = '2';
update sys_dict_data set remark = '按照子场景->场景->全库依次查询，查询到即返回' where dict_type = 'search_n_type' and dict_value = '4';
update sys_dict_data set dict_label = '删除场景人员', remark = 'HTTP交易接口--删除场景人员'  where dict_type = 'http_interface' and dict_value = 'DEL_CHANNEL_PERSON';
update sys_dict_data set dict_label = '查询场景人员是否存在', remark = 'HTTP交易接口--查询场景人员是否存在'  where dict_type = 'http_interface' and dict_value = 'QUERY_CHANNEL_PERSON_EXISTS';
update sys_dict_data set dict_label = '子场景人员操作', remark = 'HTTP交易接口--子场景人员操作'  where dict_type = 'http_interface' and dict_value = 'PERSON_SUB_TREASURY_OPERATE';
update sys_dict_data set dict_label = '子场景操作', remark = 'HTTP交易接口--子场景操作'  where dict_type = 'http_interface' and dict_value = 'SUB_TREASURY_OPERATE';


CREATE TABLE statistic_face_recog_log (
  id varchar(48) NOT NULL,
  trade_num INT4 NOT NULL ,
  pass_num INT4 NOT NULL,
  notpass_num INT4 NOT NULL,
  statistic_unit varchar(20) NOT NULL,
  statistic_date varchar(48) NOT NULL,
  tenant_id varchar(255) DEFAULT 'super',
  CONSTRAINT pk_statistic_face_recog_log PRIMARY KEY (id)
);
COMMENT ON COLUMN statistic_face_recog_log.id IS '主键';
COMMENT ON COLUMN statistic_face_recog_log.trade_num IS '交易数量';
COMMENT ON COLUMN statistic_face_recog_log.pass_num IS '通过数量';
COMMENT ON COLUMN statistic_face_recog_log.notpass_num IS '未通过数量';
COMMENT ON COLUMN statistic_face_recog_log.statistic_unit IS '统计单位(month：月，day：日)';
COMMENT ON COLUMN statistic_face_recog_log.statistic_date IS '统计日期';
COMMENT ON COLUMN statistic_face_recog_log.tenant_id IS '租户ID';
COMMENT ON TABLE statistic_face_recog_log IS '人脸识别日志统计';

CREATE TABLE statistic_idcard_verify_log (
  id varchar(48) NOT NULL,
  trade_num INT4 NOT NULL ,
  pass_num INT4 NOT NULL,
  notpass_num INT4 NOT NULL,
  statistic_unit varchar(20) NOT NULL,
  statistic_date varchar(48) NOT NULL,
  tenant_id varchar(255) DEFAULT 'super',
  CONSTRAINT pk_statistic_idcard_verify_log PRIMARY KEY (id)
);
COMMENT ON COLUMN statistic_idcard_verify_log.id IS '主键';
COMMENT ON COLUMN statistic_idcard_verify_log.trade_num IS '交易数量';
COMMENT ON COLUMN statistic_idcard_verify_log.pass_num IS '通过数量';
COMMENT ON COLUMN statistic_idcard_verify_log.notpass_num IS '未通过数量';
COMMENT ON COLUMN statistic_idcard_verify_log.statistic_unit IS '统计单位(month：月，day：日)';
COMMENT ON COLUMN statistic_idcard_verify_log.statistic_date IS '统计日期';
COMMENT ON COLUMN statistic_idcard_verify_log.tenant_id IS '租户ID';
COMMENT ON TABLE statistic_idcard_verify_log IS '身份证比对日志统计';

-- ----------------------------
-- 今日人脸交易日志视图
-- ----------------------------
CREATE 
	OR REPLACE VIEW view_today_face_log AS SELECT
	log.received_seq,
	log.received_time,
	log.result,
	log.unique_id,
	log.person_name,
	device.device_no,
	log.device_name,
	device.device_addr,
	'人脸' AS bio_type,
	'人脸1:N识别' AS busi_type,
	log.temperature,
	log.temperature_floor,
	log.temperature_top,
	health.message as health_message,
	health.result as health_result,
	log.tenant_id 
FROM
	person_face_search_log log
	LEFT JOIN device_info device ON log.device_code = device.device_no
  	LEFT JOIN person_health_code_log health on health.id = log.healthcode_log_id
WHERE
	log.device_code IS NOT NULL 
	AND log.received_time >= current_date UNION ALL
SELECT
	log.received_seq,
	log.received_time,
	log.result,
	log.unique_id,
	log.person_name,
	device.device_no,
	log.device_name,
	device.device_addr,
	'人脸' AS bio_type,
	'人脸1:1比对' AS busi_type,
	log.temperature,
	log.temperature_floor,
	log.temperature_top,
	health.message as health_message,
	health.result as health_result,
	log.tenant_id 
FROM
	person_face_match_log log
	LEFT JOIN device_info device ON log.device_code = device.device_no 
  	LEFT JOIN person_health_code_log health on health.id = log.healthcode_log_id
WHERE
	log.device_code IS NOT NULL 
	AND log.received_time >= current_date;
	
	
INSERT INTO sys_job(job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, update_by, update_time, remark) VALUES (20, '日识别人脸日志数量统计', 'DEFAULT', 'statisticFaceRecogLogTask.execute()', '0  0  4  *  *  ?', '1', '1', '0', 'admin', '2021-10-14 16:22:15.350771', '', '2021-10-14 16:22:42.873461', '');
INSERT INTO sys_job(job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, update_by, update_time, remark) VALUES (21, '日身份证比对日志数量统计', 'DEFAULT', 'statisticIdcardVerifyLogTask.execute()', '0  0  4  *  *  ?', '1', '1', '0', 'admin', '2021-10-14 16:22:40.169336', '', '2021-10-14 16:22:44.956844', '');
INSERT INTO qrtz_job_details(sched_name, job_name, job_group, description, job_class_name, is_durable, is_nonconcurrent, is_update_data, requests_recovery, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME20', 'DEFAULT', NULL, 'cn.eyecool.quartz.util.QuartzDisallowConcurrentExecution', 'false', 'true', 'false', 'false', E'\\254\\355\\000\\005sr\\000\\025org.quartz.JobDataMap\\237\\260\\203\\350\\277\\251\\260\\313\\002\\000\\000xr\\000&org.quartz.utils.StringKeyDirtyFlagMap\\202\\010\\350\\303\\373\\305](\\002\\000\\001Z\\000\\023allowsTransientDataxr\\000\\035org.quartz.utils.DirtyFlagMap\\023\\346.\\255(v\\012\\316\\002\\000\\002Z\\000\\005dirtyL\\000\\003mapt\\000\\017Ljava/util/Map;xp\\001sr\\000\\021java.util.HashMap\\005\\007\\332\\301\\303\\026`\\321\\003\\000\\002F\\000\\012loadFactorI\\000\\011thresholdxp?@\\000\\000\\000\\000\\000\\014w\\010\\000\\000\\000\\020\\000\\000\\000\\001t\\000\\017TASK_PROPERTIESsr\\000\\037cn.eyecool.quartz.domain.SysJob\\000\\000\\000\\000\\000\\000\\000\\001\\002\\000\\010L\\000\\012concurrentt\\000\\022Ljava/lang/String;L\\000\\016cronExpressionq\\000~\\000\\011L\\000\\014invokeTargetq\\000~\\000\\011L\\000\\010jobGroupq\\000~\\000\\011L\\000\\005jobIdt\\000\\020Ljava/lang/Long;L\\000\\007jobNameq\\000~\\000\\011L\\000\\015misfirePolicyq\\000~\\000\\011L\\000\\006statusq\\000~\\000\\011xr\\000(cn.eyecool.common.core.domain.BaseEntity\\000\\000\\000\\000\\000\\000\\000\\001\\002\\000\\011L\\000\\011beginTimeq\\000~\\000\\011L\\000\\010createByq\\000~\\000\\011L\\000\\012createTimet\\000\\020Ljava/util/Date;L\\000\\007endTimeq\\000~\\000\\011L\\000\\006paramsq\\000~\\000\\003L\\000\\006remarkq\\000~\\000\\011L\\000\\013searchValueq\\000~\\000\\011L\\000\\010updateByq\\000~\\000\\011L\\000\\012updateTimeq\\000~\\000\\014xppt\\000\\005adminpppppppt\\000\\0011t\\000\\0200  0  4  *  *  ?t\\000#statisticFaceRecogLogTask.execute()t\\000\\007DEFAULTsr\\000\\016java.lang.Long;\\213\\344\\220\\314\\217#\\337\\002\\000\\001J\\000\\005valuexr\\000\\020java.lang.Number\\206\\254\\225\\035\\013\\224\\340\\213\\002\\000\\000xp\\000\\000\\000\\000\\000\\000\\000\\024t\\000!\\346\\227\\245\\350\\257\\206\\345\\210\\253\\344\\272\\272\\350\\204\\270\\346\\227\\245\\345\\277\\227\\346\\225\\260\\351\\207\\217\\347\\273\\237\\350\\256\\241t\\000\\0011t\\000\\0011x\\000');
INSERT INTO qrtz_job_details(sched_name, job_name, job_group, description, job_class_name, is_durable, is_nonconcurrent, is_update_data, requests_recovery, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME21', 'DEFAULT', NULL, 'cn.eyecool.quartz.util.QuartzDisallowConcurrentExecution', 'false', 'true', 'false', 'false', E'\\254\\355\\000\\005sr\\000\\025org.quartz.JobDataMap\\237\\260\\203\\350\\277\\251\\260\\313\\002\\000\\000xr\\000&org.quartz.utils.StringKeyDirtyFlagMap\\202\\010\\350\\303\\373\\305](\\002\\000\\001Z\\000\\023allowsTransientDataxr\\000\\035org.quartz.utils.DirtyFlagMap\\023\\346.\\255(v\\012\\316\\002\\000\\002Z\\000\\005dirtyL\\000\\003mapt\\000\\017Ljava/util/Map;xp\\001sr\\000\\021java.util.HashMap\\005\\007\\332\\301\\303\\026`\\321\\003\\000\\002F\\000\\012loadFactorI\\000\\011thresholdxp?@\\000\\000\\000\\000\\000\\014w\\010\\000\\000\\000\\020\\000\\000\\000\\001t\\000\\017TASK_PROPERTIESsr\\000\\037cn.eyecool.quartz.domain.SysJob\\000\\000\\000\\000\\000\\000\\000\\001\\002\\000\\010L\\000\\012concurrentt\\000\\022Ljava/lang/String;L\\000\\016cronExpressionq\\000~\\000\\011L\\000\\014invokeTargetq\\000~\\000\\011L\\000\\010jobGroupq\\000~\\000\\011L\\000\\005jobIdt\\000\\020Ljava/lang/Long;L\\000\\007jobNameq\\000~\\000\\011L\\000\\015misfirePolicyq\\000~\\000\\011L\\000\\006statusq\\000~\\000\\011xr\\000(cn.eyecool.common.core.domain.BaseEntity\\000\\000\\000\\000\\000\\000\\000\\001\\002\\000\\011L\\000\\011beginTimeq\\000~\\000\\011L\\000\\010createByq\\000~\\000\\011L\\000\\012createTimet\\000\\020Ljava/util/Date;L\\000\\007endTimeq\\000~\\000\\011L\\000\\006paramsq\\000~\\000\\003L\\000\\006remarkq\\000~\\000\\011L\\000\\013searchValueq\\000~\\000\\011L\\000\\010updateByq\\000~\\000\\011L\\000\\012updateTimeq\\000~\\000\\014xppt\\000\\005adminpppppppt\\000\\0011t\\000\\0200  0  4  *  *  ?t\\000&statisticIdcardVerifyLogTask.execute()t\\000\\007DEFAULTsr\\000\\016java.lang.Long;\\213\\344\\220\\314\\217#\\337\\002\\000\\001J\\000\\005valuexr\\000\\020java.lang.Number\\206\\254\\225\\035\\013\\224\\340\\213\\002\\000\\000xp\\000\\000\\000\\000\\000\\000\\000\\025t\\000$\\346\\227\\245\\350\\272\\253\\344\\273\\275\\350\\257\\201\\346\\257\\224\\345\\257\\271\\346\\227\\245\\345\\277\\227\\346\\225\\260\\351\\207\\217\\347\\273\\237\\350\\256\\241t\\000\\0011t\\000\\0011x\\000');
INSERT INTO qrtz_triggers(sched_name, trigger_name, trigger_group, job_name, job_group, description, next_fire_time, prev_fire_time, priority, trigger_state, trigger_type, start_time, end_time, calendar_name, misfire_instr, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME20', 'DEFAULT', 'TASK_CLASS_NAME20', 'DEFAULT', NULL, 1634241600000, -1, 5, 'WAITING', 'CRON', 1634199735000, 0, NULL, -1, E'\\\\x');
INSERT INTO qrtz_triggers(sched_name, trigger_name, trigger_group, job_name, job_group, description, next_fire_time, prev_fire_time, priority, trigger_state, trigger_type, start_time, end_time, calendar_name, misfire_instr, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME21', 'DEFAULT', 'TASK_CLASS_NAME21', 'DEFAULT', NULL, 1634241600000, -1, 5, 'WAITING', 'CRON', 1634199760000, 0, NULL, -1, E'\\\\x');
INSERT INTO qrtz_cron_triggers(sched_name, trigger_name, trigger_group, cron_expression, time_zone_id) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME20', 'DEFAULT', '0  0  4  *  *  ?', 'Asia/Shanghai');
INSERT INTO qrtz_cron_triggers(sched_name, trigger_name, trigger_group, cron_expression, time_zone_id) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME21', 'DEFAULT', '0  0  4  *  *  ?', 'Asia/Shanghai');
SELECT setval('sys_job_job_id_seq', 21, true);