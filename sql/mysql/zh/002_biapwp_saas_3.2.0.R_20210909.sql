-- ----------------------------
-- 检活日志表
-- ----------------------------
CREATE TABLE person_face_checklive_log (
  id varchar(48) NOT NULL COMMENT '主键',
  channel_code varchar(255) DEFAULT NULL COMMENT '场景编码',
  scene_image varchar(255) NOT NULL COMMENT '现场照路径',
  scene_video varchar(255) DEFAULT NULL COMMENT '现场视频路径',
  checklive_score double DEFAULT NULL COMMENT '检活分值',
  checklive_msg varchar(255) DEFAULT NULL COMMENT '检活结果信息',
  checklive_result varchar(1) DEFAULT NULL COMMENT '检活结果(0通过，1未通过)',
  threshold double DEFAULT NULL COMMENT '检活阈值',
  received_time datetime NOT NULL COMMENT '请求时间',
  time_used bigint(20) DEFAULT NULL COMMENT '耗时(ms)',
  server_id varchar(255) DEFAULT NULL COMMENT '服务器标识',
  algs_version varchar(255) DEFAULT NULL COMMENT '算法版本',
  vendor_code varchar(255) DEFAULT NULL COMMENT '厂商',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  batch_date datetime DEFAULT NULL COMMENT '定时任务执行时间',
  tenant_id varchar(255) DEFAULT 'super' COMMENT '租户ID',
  PRIMARY KEY ( id, received_time ) 
) PARTITION BY RANGE (TO_DAYS(received_time)) (
   PARTITION p0 VALUES LESS THAN (0)
) ;
ALTER TABLE person_face_checklive_log COMMENT='人员人脸检活日志表';
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL - 2 DAY), @schemaName, 'person_face_checklive_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL - 1 DAY), @schemaName, 'person_face_checklive_log','%Y%m%d');
CALL sp_create_partition(sysdate(), @schemaName, 'person_face_checklive_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 1 DAY), @schemaName, 'person_face_checklive_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 2 DAY), @schemaName, 'person_face_checklive_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 3 DAY), @schemaName, 'person_face_checklive_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 4 DAY), @schemaName, 'person_face_checklive_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 5 DAY), @schemaName, 'person_face_checklive_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 6 DAY), @schemaName, 'person_face_checklive_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 7 DAY), @schemaName, 'person_face_checklive_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 8 DAY), @schemaName, 'person_face_checklive_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 9 DAY), @schemaName, 'person_face_checklive_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 10 DAY), @schemaName, 'person_face_checklive_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 11 DAY), @schemaName, 'person_face_checklive_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 12 DAY), @schemaName, 'person_face_checklive_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 13 DAY), @schemaName, 'person_face_checklive_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 14 DAY), @schemaName, 'person_face_checklive_log','%Y%m%d');

-- ----------------------------
-- 统计信息表
-- ----------------------------
CREATE TABLE statistic_device (
  id varchar(48) NOT NULL COMMENT '主键',
  device_num int(11) NOT NULL COMMENT '设备数量',
  online_num int(11) NOT NULL COMMENT '在线设备数量',
  offline_num int(11) NOT NULL COMMENT '离线设备数量',
  statistic_date varchar(48) NOT NULL COMMENT '统计日期',
  tenant_id varchar(255) DEFAULT 'super' COMMENT '租户ID',
  PRIMARY KEY (id)
) COMMENT='设备数量统计';

CREATE TABLE statistic_trade_log (
  id varchar(48) NOT NULL COMMENT '主键',
  trade_num int(11) NOT NULL COMMENT '交易数量',
  pass_num int(11) NOT NULL COMMENT '通过数量',
  notpass_num int(11) NOT NULL COMMENT '未通过数量',
  statistic_unit varchar(20) NOT NULL COMMENT '统计单位(month：月，day：日)',
  statistic_date varchar(48) NOT NULL COMMENT '统计日期',
  tenant_id varchar(255) DEFAULT 'super' COMMENT '租户ID',
  PRIMARY KEY (id)
) COMMENT='交易日志统计';

CREATE TABLE statistic_trade_person (
  id varchar(48) NOT NULL COMMENT '主键',
  pass_num int(11) NOT NULL COMMENT '人员数量',
  statistic_unit varchar(20) NOT NULL COMMENT '统计单位(month：月，day：日)',
  statistic_date varchar(48) NOT NULL COMMENT '统计日期',
  tenant_id varchar(255) DEFAULT 'super' COMMENT '租户ID',
  PRIMARY KEY (id)
) COMMENT='通行人员统计';


-- ----------------------------
-- 修改菜单、日志表结构
-- ----------------------------
ALTER TABLE sys_menu ADD COLUMN menu_position varchar(10) DEFAULT 'SIDE' COMMENT '菜单位置(SIDE:左侧菜单项 TAB:右侧tab页)';
ALTER TABLE person_face_match_log ADD COLUMN dept_name varchar(255) NULL DEFAULT NULL COMMENT '部门名称';
ALTER TABLE person_face_match_log ADD COLUMN person_name varchar(255) NULL DEFAULT NULL COMMENT '人员姓名';
ALTER TABLE person_face_search_log ADD COLUMN dept_name varchar(255) NULL DEFAULT NULL COMMENT '部门名称';
ALTER TABLE person_face_search_log ADD COLUMN person_name varchar(255) NULL DEFAULT NULL COMMENT '人员姓名';
ALTER TABLE person_faceiris_search_log ADD COLUMN dept_name varchar(255) NULL DEFAULT NULL COMMENT '部门名称';
ALTER TABLE person_faceiris_search_log ADD COLUMN person_name varchar(255) NULL DEFAULT NULL COMMENT '人员姓名';
ALTER TABLE person_finger_match_log ADD COLUMN dept_name varchar(255) NULL DEFAULT NULL COMMENT '部门名称';
ALTER TABLE person_finger_match_log ADD COLUMN person_name varchar(255) NULL DEFAULT NULL COMMENT '人员姓名';
ALTER TABLE person_finger_search_log ADD COLUMN dept_name varchar(255) NULL DEFAULT NULL COMMENT '部门名称';
ALTER TABLE person_finger_search_log ADD COLUMN person_name varchar(255) NULL DEFAULT NULL COMMENT '人员姓名';
ALTER TABLE person_iris_match_log ADD COLUMN dept_name varchar(255) NULL DEFAULT NULL COMMENT '部门名称';
ALTER TABLE person_iris_match_log ADD COLUMN person_name varchar(255) NULL DEFAULT NULL COMMENT '人员姓名';
ALTER TABLE person_iris_search_log ADD COLUMN dept_name varchar(255) NULL DEFAULT NULL COMMENT '部门名称';
ALTER TABLE person_iris_search_log ADD COLUMN person_name varchar(255) NULL DEFAULT NULL COMMENT '人员姓名';



-- ----------------------------
-- 字典
-- ----------------------------
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (147, '菜单位置', 'menu_positon', '0', 'admin',sysdate(), '', NULL, '菜单位置');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (457, 0, '左侧菜单', 'SIDE', 'menu_positon', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, '菜单位置--左侧菜单');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (458, 1, 'TAB页菜单', 'TAB', 'menu_positon', NULL, NULL, 'N', '0', 'admin', sysdate(), 'admin', sysdate(),  '菜单位置--TAB页菜单');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (459, 28, '设备升级信号查询', 'CLIENT_UPGRADE_SIGNAL', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), 'admin', sysdate(),  '设备升级信号查询');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (460, 30, '设备升级结果回传', 'CLIENT_UPGRADE_RESULT_BAK', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, '设备升级结果回传');

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

INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2534, '人员信息', 2012, 1, 'person', 'basedata/index', 1, 0, 'C', '0', '0', '', '#', 'admin', sysdate(), 'admin', sysdate(), '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2535, '场景管理', 2068, 0, 'scenemanage', 'scene/manage/index', 1, 0, 'C', '0', '0', 'scene:channel:list', '#', 'admin', sysdate(), 'admin', sysdate(), '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2536, '场景人员', 2068, 0, 'sceneperson', 'scene/person/index', 1, 0, 'C', '0', '0', 'scene:channelBusi:list', '#', 'admin', sysdate(), 'admin', sysdate(), '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2537, '工作台', 0, 0, 'workbench', ' ', 1, 0, 'M', '0', '0', '', 'job', 'admin', sysdate(), 'admin', sysdate(), '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2539, '数据分析', 2537, 1, 'analysis', 'workbench/analysis/index', 1, 0, 'C', '0', '0', NULL, '#', 'admin', sysdate(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2542, '检活日志', 2284, 3, 'checklive', 'tradelog/checklive/index', 1, 0, 'C', '0', '0', 'tradelog:checklive:list', '#', 'admin', sysdate(), 'admin', sysdate(), '人员人脸检活日志菜单', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2543, '检活日志查询', 2542, 1, '#', '', 1, 0, 'F', '0', '0', 'tradelog:checklive:query', '#', 'admin', sysdate(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2544, '检活日志导出', 2542, 2, '#', '', 1, 0, 'F', '0', '0', 'tradelog:checklive:export', '#', 'admin', sysdate(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2546, '公众号授权', 2236, 6, '', NULL, 1, 0, 'F', '0', '0', 'msg:officalAccount:uploadMpAuthFile', '#', 'admin', sysdate(),  '', NULL, '', 'SIDE');


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
INSERT INTO sys_job(job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, update_by, update_time, remark) VALUES (16, '日通行人员数量统计', 'DEFAULT', 'statisticPassPersonTask.execute()', '0  0  4  *  *  ? ', '1', '1', '0', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_job(job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, update_by, update_time, remark) VALUES (17, '日识别日志数量统计', 'DEFAULT', 'statisticTradeLogTask.execute()', '0  0  4  *  *  ?', '1', '1', '0', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_job(job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, update_by, update_time, remark) VALUES (18, '日设备数量统计', 'DEFAULT', 'statisticDeviceTask.execute()', '0 30 22 ? * *', '1', '1', '0', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_job(job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, update_by, update_time, remark) VALUES (19, '检活日志分区维护', 'DEFAULT', 'checkliveLogTask.clearAndAddPartition(365,false,\'eyecool_assps\')', '0 0 2 * * ? * ', '1', '1', '0', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO qrtz_job_details(sched_name, job_name, job_group, description, job_class_name, is_durable, is_nonconcurrent, is_update_data, requests_recovery, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME16', 'DEFAULT', NULL, 'cn.eyecool.quartz.util.QuartzDisallowConcurrentExecution', '0', '1', '0', '0', 0xACED0005737200156F72672E71756172747A2E4A6F62446174614D61709FB083E8BFA9B0CB020000787200266F72672E71756172747A2E7574696C732E537472696E674B65794469727479466C61674D61708208E8C3FBC55D280200015A0013616C6C6F77735472616E7369656E74446174617872001D6F72672E71756172747A2E7574696C732E4469727479466C61674D617013E62EAD28760ACE0200025A000564697274794C00036D617074000F4C6A6176612F7574696C2F4D61703B787001737200116A6176612E7574696C2E486173684D61700507DAC1C31660D103000246000A6C6F6164466163746F724900097468726573686F6C6478703F4000000000000C7708000000100000000174000F5441534B5F50524F504552544945537372001F636E2E657965636F6F6C2E71756172747A2E646F6D61696E2E5379734A6F6200000000000000010200084C000A636F6E63757272656E747400124C6A6176612F6C616E672F537472696E673B4C000E63726F6E45787072657373696F6E71007E00094C000C696E766F6B6554617267657471007E00094C00086A6F6247726F757071007E00094C00056A6F6249647400104C6A6176612F6C616E672F4C6F6E673B4C00076A6F624E616D6571007E00094C000D6D697366697265506F6C69637971007E00094C000673746174757371007E000978720028636E2E657965636F6F6C2E636F6D6D6F6E2E636F72652E646F6D61696E2E42617365456E7469747900000000000000010200094C0009626567696E54696D6571007E00094C0008637265617465427971007E00094C000A63726561746554696D657400104C6A6176612F7574696C2F446174653B4C0007656E6454696D6571007E00094C0006706172616D7371007E00034C000672656D61726B71007E00094C000B73656172636856616C756571007E00094C0008757064617465427971007E00094C000A75706461746554696D6571007E000C78707074000561646D696E7372000E6A6176612E7574696C2E44617465686A81014B597419030000787077080000017BA092B3B0787070740000707070740001317400113020203020203420202A20202A20203F2074002173746174697374696350617373506572736F6E5461736B2E65786563757465282974000744454641554C547372000E6A6176612E6C616E672E4C6F6E673B8BE490CC8F23DF0200014A000576616C7565787200106A6176612E6C616E672E4E756D62657286AC951D0B94E08B0200007870000000000000001074001BE697A5E9809AE8A18CE4BABAE59198E695B0E9878FE7BB9FE8AEA174000131740001307800);
INSERT INTO qrtz_job_details(sched_name, job_name, job_group, description, job_class_name, is_durable, is_nonconcurrent, is_update_data, requests_recovery, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME17', 'DEFAULT', NULL, 'cn.eyecool.quartz.util.QuartzDisallowConcurrentExecution', '0', '1', '0', '0', 0xACED0005737200156F72672E71756172747A2E4A6F62446174614D61709FB083E8BFA9B0CB020000787200266F72672E71756172747A2E7574696C732E537472696E674B65794469727479466C61674D61708208E8C3FBC55D280200015A0013616C6C6F77735472616E7369656E74446174617872001D6F72672E71756172747A2E7574696C732E4469727479466C61674D617013E62EAD28760ACE0200025A000564697274794C00036D617074000F4C6A6176612F7574696C2F4D61703B787001737200116A6176612E7574696C2E486173684D61700507DAC1C31660D103000246000A6C6F6164466163746F724900097468726573686F6C6478703F4000000000000C7708000000100000000174000F5441534B5F50524F504552544945537372001F636E2E657965636F6F6C2E71756172747A2E646F6D61696E2E5379734A6F6200000000000000010200084C000A636F6E63757272656E747400124C6A6176612F6C616E672F537472696E673B4C000E63726F6E45787072657373696F6E71007E00094C000C696E766F6B6554617267657471007E00094C00086A6F6247726F757071007E00094C00056A6F6249647400104C6A6176612F6C616E672F4C6F6E673B4C00076A6F624E616D6571007E00094C000D6D697366697265506F6C69637971007E00094C000673746174757371007E000978720028636E2E657965636F6F6C2E636F6D6D6F6E2E636F72652E646F6D61696E2E42617365456E7469747900000000000000010200094C0009626567696E54696D6571007E00094C0008637265617465427971007E00094C000A63726561746554696D657400104C6A6176612F7574696C2F446174653B4C0007656E6454696D6571007E00094C0006706172616D7371007E00034C000672656D61726B71007E00094C000B73656172636856616C756571007E00094C0008757064617465427971007E00094C000A75706461746554696D6571007E000C78707074000561646D696E7372000E6A6176612E7574696C2E44617465686A81014B597419030000787077080000017BA097C878787070740000707070740001317400103020203020203420202A20202A20203F74001F73746174697374696354726164654C6F675461736B2E65786563757465282974000744454641554C547372000E6A6176612E6C616E672E4C6F6E673B8BE490CC8F23DF0200014A000576616C7565787200106A6176612E6C616E672E4E756D62657286AC951D0B94E08B0200007870000000000000001174001BE697A5E8AF86E588ABE697A5E5BF97E695B0E9878FE7BB9FE8AEA174000131740001307800);
INSERT INTO qrtz_job_details(sched_name, job_name, job_group, description, job_class_name, is_durable, is_nonconcurrent, is_update_data, requests_recovery, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME18', 'DEFAULT', NULL, 'cn.eyecool.quartz.util.QuartzDisallowConcurrentExecution', '0', '1', '0', '0', 0xACED0005737200156F72672E71756172747A2E4A6F62446174614D61709FB083E8BFA9B0CB020000787200266F72672E71756172747A2E7574696C732E537472696E674B65794469727479466C61674D61708208E8C3FBC55D280200015A0013616C6C6F77735472616E7369656E74446174617872001D6F72672E71756172747A2E7574696C732E4469727479466C61674D617013E62EAD28760ACE0200025A000564697274794C00036D617074000F4C6A6176612F7574696C2F4D61703B787001737200116A6176612E7574696C2E486173684D61700507DAC1C31660D103000246000A6C6F6164466163746F724900097468726573686F6C6478703F4000000000000C7708000000100000000174000F5441534B5F50524F504552544945537372001F636E2E657965636F6F6C2E71756172747A2E646F6D61696E2E5379734A6F6200000000000000010200084C000A636F6E63757272656E747400124C6A6176612F6C616E672F537472696E673B4C000E63726F6E45787072657373696F6E71007E00094C000C696E766F6B6554617267657471007E00094C00086A6F6247726F757071007E00094C00056A6F6249647400104C6A6176612F6C616E672F4C6F6E673B4C00076A6F624E616D6571007E00094C000D6D697366697265506F6C69637971007E00094C000673746174757371007E000978720028636E2E657965636F6F6C2E636F6D6D6F6E2E636F72652E646F6D61696E2E42617365456E7469747900000000000000010200094C0009626567696E54696D6571007E00094C0008637265617465427971007E00094C000A63726561746554696D657400104C6A6176612F7574696C2F446174653B4C0007656E6454696D6571007E00094C0006706172616D7371007E00034C000672656D61726B71007E00094C000B73656172636856616C756571007E00094C0008757064617465427971007E00094C000A75706461746554696D6571007E000C78707074000561646D696E7372000E6A6176612E7574696C2E44617465686A81014B597419030000787077080000017BA0988FB07870707400007070707400013174000D30203330203232203F202A202A74001D7374617469737469634465766963655461736B2E65786563757465282974000744454641554C547372000E6A6176612E6C616E672E4C6F6E673B8BE490CC8F23DF0200014A000576616C7565787200106A6176612E6C616E672E4E756D62657286AC951D0B94E08B02000078700000000000000012740015E697A5E8AEBEE5A487E695B0E9878FE7BB9FE8AEA174000131740001307800);
INSERT INTO qrtz_job_details(sched_name, job_name, job_group, description, job_class_name, is_durable, is_nonconcurrent, is_update_data, requests_recovery, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME19', 'DEFAULT', NULL, 'cn.eyecool.quartz.util.QuartzDisallowConcurrentExecution', '0', '1', '0', '0', 0xACED0005737200156F72672E71756172747A2E4A6F62446174614D61709FB083E8BFA9B0CB020000787200266F72672E71756172747A2E7574696C732E537472696E674B65794469727479466C61674D61708208E8C3FBC55D280200015A0013616C6C6F77735472616E7369656E74446174617872001D6F72672E71756172747A2E7574696C732E4469727479466C61674D617013E62EAD28760ACE0200025A000564697274794C00036D617074000F4C6A6176612F7574696C2F4D61703B787001737200116A6176612E7574696C2E486173684D61700507DAC1C31660D103000246000A6C6F6164466163746F724900097468726573686F6C6478703F4000000000000C7708000000100000000174000F5441534B5F50524F504552544945537372001F636E2E657965636F6F6C2E71756172747A2E646F6D61696E2E5379734A6F6200000000000000010200084C000A636F6E63757272656E747400124C6A6176612F6C616E672F537472696E673B4C000E63726F6E45787072657373696F6E71007E00094C000C696E766F6B6554617267657471007E00094C00086A6F6247726F757071007E00094C00056A6F6249647400104C6A6176612F6C616E672F4C6F6E673B4C00076A6F624E616D6571007E00094C000D6D697366697265506F6C69637971007E00094C000673746174757371007E000978720028636E2E657965636F6F6C2E636F6D6D6F6E2E636F72652E646F6D61696E2E42617365456E7469747900000000000000010200094C0009626567696E54696D6571007E00094C0008637265617465427971007E00094C000A63726561746554696D657400104C6A6176612F7574696C2F446174653B4C0007656E6454696D6571007E00094C0006706172616D7371007E00034C000672656D61726B71007E00094C000B73656172636856616C756571007E00094C0008757064617465427971007E00094C000A75706461746554696D6571007E000C78707074000561646D696E7372000E6A6176612E7574696C2E44617465686A81014B597419030000787077080000017BA5C901B87870707400007070707400013174000E3020302032202A202A203F202A20740040636865636B6C6976654C6F675461736B2E636C656172416E64416464506172746974696F6E283336352C66616C73652C27657965636F6F6C5F6173737073272974000744454641554C547372000E6A6176612E6C616E672E4C6F6E673B8BE490CC8F23DF0200014A000576616C7565787200106A6176612E6C616E672E4E756D62657286AC951D0B94E08B02000078700000000000000013740018E6A380E6B4BBE697A5E5BF97E58886E58CBAE7BBB4E68AA474000131740001307800);
INSERT INTO qrtz_triggers(sched_name, trigger_name, trigger_group, job_name, job_group, description, next_fire_time, prev_fire_time, priority, trigger_state, trigger_type, start_time, end_time, calendar_name, misfire_instr, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME16', 'DEFAULT', 'TASK_CLASS_NAME16', 'DEFAULT', NULL, 1631304000000, 1631217600000, 5, 'WAITING', 'CRON', 1631181353000, 0, NULL, -1, '');
INSERT INTO qrtz_triggers(sched_name, trigger_name, trigger_group, job_name, job_group, description, next_fire_time, prev_fire_time, priority, trigger_state, trigger_type, start_time, end_time, calendar_name, misfire_instr, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME17', 'DEFAULT', 'TASK_CLASS_NAME17', 'DEFAULT', NULL, 1631304000000, 1631217600000, 5, 'WAITING', 'CRON', 1631181353000, 0, NULL, -1, '');
INSERT INTO qrtz_triggers(sched_name, trigger_name, trigger_group, job_name, job_group, description, next_fire_time, prev_fire_time, priority, trigger_state, trigger_type, start_time, end_time, calendar_name, misfire_instr, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME18', 'DEFAULT', 'TASK_CLASS_NAME18', 'DEFAULT', NULL, 1631284200000, 1631197800000, 5, 'WAITING', 'CRON', 1631181353000, 0, NULL, -1, '');
INSERT INTO qrtz_triggers(sched_name, trigger_name, trigger_group, job_name, job_group, description, next_fire_time, prev_fire_time, priority, trigger_state, trigger_type, start_time, end_time, calendar_name, misfire_instr, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME19', 'DEFAULT', 'TASK_CLASS_NAME19', 'DEFAULT', NULL, 1631296800000, 1631210400000, 5, 'WAITING', 'CRON', 1631181353000, 0, NULL, -1, '');
INSERT INTO qrtz_cron_triggers(sched_name, trigger_name, trigger_group, cron_expression, time_zone_id) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME16', 'DEFAULT', '0  0  4  *  *  ? ', 'Asia/Shanghai');
INSERT INTO qrtz_cron_triggers(sched_name, trigger_name, trigger_group, cron_expression, time_zone_id) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME17', 'DEFAULT', '0  0  4  *  *  ?', 'Asia/Shanghai');
INSERT INTO qrtz_cron_triggers(sched_name, trigger_name, trigger_group, cron_expression, time_zone_id) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME18', 'DEFAULT', '0 30 22 ? * *', 'Asia/Shanghai');
INSERT INTO qrtz_cron_triggers(sched_name, trigger_name, trigger_group, cron_expression, time_zone_id) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME19', 'DEFAULT', '0 0 2 * * ? * ', 'Asia/Shanghai');


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
	AND log.received_time >= str_to_date( DATE_FORMAT( NOW( ), '%Y-%m-%d' ), '%Y-%m-%d %H:%i:%s' ) UNION ALL
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
	AND log.received_time >= str_to_date( DATE_FORMAT( NOW( ), '%Y-%m-%d' ), '%Y-%m-%d %H:%i:%s' );
	
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (173, '人脸检活图片存放目录', 'busi.face.checklive.dir', './eyecool/busi/face/checklive/', 'Y', 'admin', sysdate(), '', NULL, '人脸检活图片存放目录', 'N');
update sys_config set remark = '平台是否支持自动绑定和解绑人库关系（用于部分场景需要绑定全库人员情况，人员入库直接和相关场景绑定）' where config_key = 'platform.libperson.isAutobind';
update sys_config set config_name = '平台需要自动绑定人库关系的场景编码', remark = '平台需要自动绑定人库关系的场景编码(用于部分场景需要绑定全库人员情况，仅在开启自动绑定时有效，多个场景编码使用\";\"分割)' where config_key = 'platform.libperson.autobind.channel';
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
  id varchar(48) NOT NULL COMMENT '主键',
  trade_num int(11) NOT NULL COMMENT '交易数量',
  pass_num int(11) NOT NULL COMMENT '通过数量',
  notpass_num int(11) NOT NULL COMMENT '未通过数量',
  statistic_unit varchar(20) NOT NULL COMMENT '统计单位(month：月，day：日)',
  statistic_date varchar(48) NOT NULL COMMENT '统计日期',
  tenant_id varchar(255) DEFAULT 'super' COMMENT '租户ID',
  PRIMARY KEY (id)
) COMMENT='人脸识别日志统计';

CREATE TABLE statistic_idcard_verify_log (
  id varchar(48) NOT NULL COMMENT '主键',
  trade_num int(11) NOT NULL COMMENT '交易数量',
  pass_num int(11) NOT NULL COMMENT '通过数量',
  notpass_num int(11) NOT NULL COMMENT '未通过数量',
  statistic_unit varchar(20) NOT NULL COMMENT '统计单位(month：月，day：日)',
  statistic_date varchar(48) NOT NULL COMMENT '统计日期',
  tenant_id varchar(255) DEFAULT 'super' COMMENT '租户ID',
  PRIMARY KEY (id)
) COMMENT='身份证比对日志统计';


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
	AND log.received_time >= str_to_date( DATE_FORMAT( NOW( ), '%Y-%m-%d' ), '%Y-%m-%d %H:%i:%s' ) UNION ALL
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
	AND log.received_time >= str_to_date( DATE_FORMAT( NOW( ), '%Y-%m-%d' ), '%Y-%m-%d %H:%i:%s' );
	
	
INSERT INTO sys_job(job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, update_by, update_time, remark) VALUES (22, '日识别人脸日志数量统计', 'DEFAULT', 'statisticFaceRecogLogTask.execute()', '0  0  4  *  *  ?', '1', '1', '0', 'admin', '2021-10-14 15:53:56', '', '2021-10-14 15:55:08', '');
INSERT INTO sys_job(job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, update_by, update_time, remark) VALUES (23, '日身份证比对日志数量统计', 'DEFAULT', 'statisticIdcardVerifyLogTask.execute()', '0  0  4  *  *  ?', '1', '1', '0', 'admin', '2021-10-14 15:54:59', '', '2021-10-14 15:55:05', '');
INSERT INTO qrtz_job_details(sched_name, job_name, job_group, description, job_class_name, is_durable, is_nonconcurrent, is_update_data, requests_recovery, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME22', 'DEFAULT', NULL, 'cn.eyecool.quartz.util.QuartzDisallowConcurrentExecution', '0', '1', '0', '0', 0xACED0005737200156F72672E71756172747A2E4A6F62446174614D61709FB083E8BFA9B0CB020000787200266F72672E71756172747A2E7574696C732E537472696E674B65794469727479466C61674D61708208E8C3FBC55D280200015A0013616C6C6F77735472616E7369656E74446174617872001D6F72672E71756172747A2E7574696C732E4469727479466C61674D617013E62EAD28760ACE0200025A000564697274794C00036D617074000F4C6A6176612F7574696C2F4D61703B787001737200116A6176612E7574696C2E486173684D61700507DAC1C31660D103000246000A6C6F6164466163746F724900097468726573686F6C6478703F4000000000000C7708000000100000000174000F5441534B5F50524F504552544945537372001F636E2E657965636F6F6C2E71756172747A2E646F6D61696E2E5379734A6F6200000000000000010200084C000A636F6E63757272656E747400124C6A6176612F6C616E672F537472696E673B4C000E63726F6E45787072657373696F6E71007E00094C000C696E766F6B6554617267657471007E00094C00086A6F6247726F757071007E00094C00056A6F6249647400104C6A6176612F6C616E672F4C6F6E673B4C00076A6F624E616D6571007E00094C000D6D697366697265506F6C69637971007E00094C000673746174757371007E000978720028636E2E657965636F6F6C2E636F6D6D6F6E2E636F72652E646F6D61696E2E42617365456E7469747900000000000000010200094C0009626567696E54696D6571007E00094C0008637265617465427971007E00094C000A63726561746554696D657400104C6A6176612F7574696C2F446174653B4C0007656E6454696D6571007E00094C0006706172616D7371007E00034C000672656D61726B71007E00094C000B73656172636856616C756571007E00094C0008757064617465427971007E00094C000A75706461746554696D6571007E000C78707074000561646D696E70707070707070740001317400103020203020203420202A20202A20203F740023737461746973746963466163655265636F674C6F675461736B2E65786563757465282974000744454641554C547372000E6A6176612E6C616E672E4C6F6E673B8BE490CC8F23DF0200014A000576616C7565787200106A6176612E6C616E672E4E756D62657286AC951D0B94E08B02000078700000000000000016740021E697A5E8AF86E588ABE4BABAE884B8E697A5E5BF97E695B0E9878FE7BB9FE8AEA174000131740001317800);
INSERT INTO qrtz_job_details(sched_name, job_name, job_group, description, job_class_name, is_durable, is_nonconcurrent, is_update_data, requests_recovery, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME23', 'DEFAULT', NULL, 'cn.eyecool.quartz.util.QuartzDisallowConcurrentExecution', '0', '1', '0', '0', 0xACED0005737200156F72672E71756172747A2E4A6F62446174614D61709FB083E8BFA9B0CB020000787200266F72672E71756172747A2E7574696C732E537472696E674B65794469727479466C61674D61708208E8C3FBC55D280200015A0013616C6C6F77735472616E7369656E74446174617872001D6F72672E71756172747A2E7574696C732E4469727479466C61674D617013E62EAD28760ACE0200025A000564697274794C00036D617074000F4C6A6176612F7574696C2F4D61703B787001737200116A6176612E7574696C2E486173684D61700507DAC1C31660D103000246000A6C6F6164466163746F724900097468726573686F6C6478703F4000000000000C7708000000100000000174000F5441534B5F50524F504552544945537372001F636E2E657965636F6F6C2E71756172747A2E646F6D61696E2E5379734A6F6200000000000000010200084C000A636F6E63757272656E747400124C6A6176612F6C616E672F537472696E673B4C000E63726F6E45787072657373696F6E71007E00094C000C696E766F6B6554617267657471007E00094C00086A6F6247726F757071007E00094C00056A6F6249647400104C6A6176612F6C616E672F4C6F6E673B4C00076A6F624E616D6571007E00094C000D6D697366697265506F6C69637971007E00094C000673746174757371007E000978720028636E2E657965636F6F6C2E636F6D6D6F6E2E636F72652E646F6D61696E2E42617365456E7469747900000000000000010200094C0009626567696E54696D6571007E00094C0008637265617465427971007E00094C000A63726561746554696D657400104C6A6176612F7574696C2F446174653B4C0007656E6454696D6571007E00094C0006706172616D7371007E00034C000672656D61726B71007E00094C000B73656172636856616C756571007E00094C0008757064617465427971007E00094C000A75706461746554696D6571007E000C78707074000561646D696E70707070707070740001317400103020203020203420202A20202A20203F7400267374617469737469634964636172645665726966794C6F675461736B2E65786563757465282974000744454641554C547372000E6A6176612E6C616E672E4C6F6E673B8BE490CC8F23DF0200014A000576616C7565787200106A6176612E6C616E672E4E756D62657286AC951D0B94E08B02000078700000000000000017740024E697A5E8BAABE4BBBDE8AF81E6AF94E5AFB9E697A5E5BF97E695B0E9878FE7BB9FE8AEA174000131740001317800);
INSERT INTO qrtz_triggers(sched_name, trigger_name, trigger_group, job_name, job_group, description, next_fire_time, prev_fire_time, priority, trigger_state, trigger_type, start_time, end_time, calendar_name, misfire_instr, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME22', 'DEFAULT', 'TASK_CLASS_NAME22', 'DEFAULT', NULL, 1634241600000, -1, 5, 'WAITING', 'CRON', 1634198036000, 0, NULL, -1, '');
INSERT INTO qrtz_triggers(sched_name, trigger_name, trigger_group, job_name, job_group, description, next_fire_time, prev_fire_time, priority, trigger_state, trigger_type, start_time, end_time, calendar_name, misfire_instr, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME23', 'DEFAULT', 'TASK_CLASS_NAME23', 'DEFAULT', NULL, 1634241600000, -1, 5, 'WAITING', 'CRON', 1634198099000, 0, NULL, -1, '');
INSERT INTO qrtz_cron_triggers(sched_name, trigger_name, trigger_group, cron_expression, time_zone_id) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME22', 'DEFAULT', '0  0  4  *  *  ?', 'Asia/Shanghai');
INSERT INTO qrtz_cron_triggers(sched_name, trigger_name, trigger_group, cron_expression, time_zone_id) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME23', 'DEFAULT', '0  0  4  *  *  ?', 'Asia/Shanghai');
