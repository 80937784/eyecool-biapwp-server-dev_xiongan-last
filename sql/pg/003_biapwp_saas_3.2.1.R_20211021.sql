ALTER TABLE person_face_match_log ADD temperature_result varchar(1);
COMMENT ON COLUMN person_face_match_log.temperature_result IS '测温结果(0:未布控,1:体温正常,2:高体温报警,3:低体温报警,4:未测到温度(不在测温区域,体温未确认完成))';
ALTER TABLE person_face_search_log ADD temperature_result varchar(1);
COMMENT ON COLUMN person_face_search_log.temperature_result IS '测温结果(0:未布控,1:体温正常,2:高体温报警,3:低体温报警,4:未测到温度(不在测温区域,体温未确认完成))';
ALTER TABLE person_faceiris_search_log ADD temperature_result varchar(1);
COMMENT ON COLUMN person_faceiris_search_log.temperature_result IS '测温结果(0:未布控,1:体温正常,2:高体温报警,3:低体温报警,4:未测到温度(不在测温区域,体温未确认完成))';
ALTER TABLE person_face_search_log ADD valid_type varchar(2) DEFAULT '02';
COMMENT ON COLUMN person_face_search_log.valid_type IS '核验方式(01:身份证核验，02：刷脸核验，03：健康码核验，04：刷卡核验)';

-- ----------------------------
-- 今日人脸交易日志视图
-- ------------------------------ 今日人脸交易日志视图-- ----------------------------
DROP VIEW view_today_face_log ;
CREATE 
	OR REPLACE VIEW view_today_face_log AS SELECT
	log.received_seq,
	log.received_time,
	log.RESULT,
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
	log.temperature_result,
	health.message AS health_message,
	health.RESULT AS health_result,
	log.tenant_id 
FROM
	person_face_search_log log
	LEFT JOIN device_info device ON log.device_code = device.device_no
	LEFT JOIN person_health_code_log health ON health.ID = log.healthcode_log_id 
WHERE
	log.device_code IS NOT NULL 
	AND log.received_time >= CURRENT_DATE UNION ALL
SELECT
	log.received_seq,
	log.received_time,
	log.RESULT,
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
	log.temperature_result,
	health.message AS health_message,
	health.RESULT AS health_result,
	log.tenant_id 
FROM
	person_face_match_log log
	LEFT JOIN device_info device ON log.device_code = device.device_no
	LEFT JOIN person_health_code_log health ON health.ID = log.healthcode_log_id 
WHERE
	log.device_code IS NOT NULL 
	AND log.received_time >= CURRENT_DATE;
	

-- ----------------------------
-- 初始化多时段考勤菜单
-- ----------------------------
-- 多时段考勤主菜单
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2615, '多时段考勤', 0, 12, 'attendance', NULL, 1, 0, 'M', '0', '0', '', 'table', 'admin',now(), '', NULL, '', 'SIDE');

-- 多时段考勤二级菜单
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2616, '假期管理', 2615, 1, 'vacation', 'attendance/vacation/index', 1, 0, 'C', '0', '0', 'attendance:vacation:list', 'build', 'admin',now(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2620, '白名单管理', 2615, 0, 'white', 'attendance/white/index', 1, 0, 'C', '0', '0', 'attendance:white:list', 'base', 'admin', now(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2623, '时段管理', 2615, 2, 'times', 'attendance/times/index', 1, 0, 'C', '0', '0', 'attendance:times:list', 'date', 'admin',now(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2628, '考勤规则', 2615, 3, 'rules', 'attendance/rules/index', 1, 0, 'C', '0', '0', 'attendance:rules:list', 'base', 'admin', now(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2633, '部门考勤规则', 2615, 4, 'rule', 'attendance/rule/index', 1, 0, 'C', '0', '0', 'attendance:rule:list', 'build', 'admin', now(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2639, '考勤记录', 2615, 5, 'rpt', 'attendance/rpt/index', 1, 0, 'C', '0', '0', 'attendance:rpt:list', 'base', 'admin', now(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2642, '个人考勤率', 2615, 6, 'rpteveryone', 'attendance/rpteveryone/index', 1, 0, 'C', '0', '0', 'attendance:rpt:list', 'base', 'admin', now(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2645, '部门考勤率', 2615, 7, 'rptdept', 'attendance/rptdept/index', 1, 0, 'C', '0', '0', 'attendance:rpt:list', 'base', 'admin', now(), '', NULL, '', 'SIDE');

-- 多时段考勤二级菜单按钮
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2617, '假期管理删除', 2616, 3, '', NULL, 1, 0, 'F', '0', '0', 'attendance:vacation:remove', '#', 'admin',now(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2618, '假期管理修改', 2616, 2, '', NULL, 1, 0, 'F', '0', '0', 'attendance:vacation:edit', '#', 'admin',now(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2619, '假期管理新增', 2616, 1, '', NULL, 1, 0, 'F', '0', '0', 'attendance:vacation:add', '#', 'admin', now(), '',NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2621, '非白名单列表', 2620, 1, '', NULL, 1, 0, 'F', '0', '0', 'attendance:white:import', '#', 'admin', now(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2622, '考勤白名单删除', 2620, 2, '', NULL, 1, 0, 'F', '0', '0', 'attendance:white:remove', '#', 'admin', now(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2624, '时段管理导出', 2623, 4, '', NULL, 1, 0, 'F', '0', '0', 'attendance:times:export', '#', 'admin', now(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2625, '时段管理删除', 2623, 3, '', NULL, 1, 0, 'F', '0', '0', 'attendance:times:remove', '#', 'admin',now(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2626, '时段管理修改', 2623, 2, '', NULL, 1, 0, 'F', '0', '0', 'attendance:times:edit', '#', 'admin', now(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2627, '时段管理新增', 2623, 1, '', NULL, 1, 0, 'F', '0', '0', 'attendance:times:add', '#', 'admin', now(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2629, '考勤规则获取所有时间段', 2628, 4, '', NULL, 1, 0, 'F', '0', '0', 'attendance:times:query', '#', 'admin', now(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2630, '考勤规则删除', 2628, 3, '', NULL, 1, 0, 'F', '0', '0', 'attendance:rules:remove', '#', 'admin', now(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2631, '考勤规则修改', 2628, 2, '', NULL, 1, 0, 'F', '0', '0', 'attendance:rules:edit', '#', 'admin', now(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2632, '考勤规则新增', 2628, 1, '', NULL, 1, 0, 'F', '0', '0', 'attendance:rules:add', '#', 'admin', now(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2634, '获取所有部门考勤规则', 2633, 5, '', NULL, 1, 0, 'F', '0', '0', 'attendance:rule:query', '#', 'admin', now(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2635, '部门考勤规则获取部门', 2633, 4, '', NULL, 1, 0, 'F', '0', '0', 'system:dept:list', '#', 'admin', now(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2636, '部门考勤规则删除', 2633, 3, '', NULL, 1, 0, 'F', '0', '0', 'attendance:rule:remove', '#', 'admin', now(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2637, '部门考勤规则修改', 2633, 2, '', NULL, 1, 0, 'F', '0', '0', 'attendance:rule:edit', '#', 'admin', now(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2638, '部门考勤规则新增', 2633, 1, '', NULL, 1, 0, 'F', '0', '0', 'attendance:rule:add', '#', 'admin', now(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2640, '考勤记录获取部门', 2639, 2, '', NULL, 1, 0, 'F', '0', '0', 'system:dept:list', '#', 'admin', now(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2641, '考勤记录导出', 2639, 1, '', NULL, 1, 0, 'F', '0', '0', 'attendance:rpt:export', '#', 'admin', now(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2643, '个人考勤率获取部门', 2642, 2, '', NULL, 1, 0, 'F', '0', '0', 'system:dept:list', '#', 'admin', now(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2644, '个人考勤率导出', 2642, 1, '', NULL, 1, 0, 'F', '0', '0', 'attendance:rpt:export', '#', 'admin', now(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2646, '部门考勤率获取部门', 2645, 2, '', NULL, 1, 0, 'F', '0', '0', 'system:dept:list', '#', 'admin', now(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2647, '部门考勤率导出', 2645, 1, '', NULL, 1, 0, 'F', '0', '0', 'attendance:rpt:export', '#', 'admin', now(), '', NULL, '', 'SIDE');


-- 定时任务插入
INSERT INTO sys_job(job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, update_by, update_time, remark) VALUES (22, '处理考勤（带指定日期）', 'DEFAULT', 'autoCreateAtdRecordTask.convertBioLogToAtdRecord(''2021-10-15'')', '0 0 6 * * ?', '1', '1', '1', 'admin', '2021-11-02 16:53:09.227877', 'admin', '2021-11-02 16:55:55.232142', '');
INSERT INTO sys_job(job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, update_by, update_time, remark) VALUES (23, '处理考勤', 'DEFAULT', 'autoCreateAtdRecordTask.convertBioLogToAtdRecord()', '0 0 6 * * ?', '1', '1', '0', 'admin', '2021-11-02 16:54:08.158363', 'admin', '2021-11-02 16:56:22.346362', '');
INSERT INTO qrtz_job_details(sched_name, job_name, job_group, description, job_class_name, is_durable, is_nonconcurrent, is_update_data, requests_recovery, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME22', 'DEFAULT', NULL, 'cn.eyecool.quartz.util.QuartzDisallowConcurrentExecution', 'false', 'true', 'false', 'false', E'\\254\\355\\000\\005sr\\000\\025org.quartz.JobDataMap\\237\\260\\203\\350\\277\\251\\260\\313\\002\\000\\000xr\\000&org.quartz.utils.StringKeyDirtyFlagMap\\202\\010\\350\\303\\373\\305](\\002\\000\\001Z\\000\\023allowsTransientDataxr\\000\\035org.quartz.utils.DirtyFlagMap\\023\\346.\\255(v\\012\\316\\002\\000\\002Z\\000\\005dirtyL\\000\\003mapt\\000\\017Ljava/util/Map;xp\\001sr\\000\\021java.util.HashMap\\005\\007\\332\\301\\303\\026`\\321\\003\\000\\002F\\000\\012loadFactorI\\000\\011thresholdxp?@\\000\\000\\000\\000\\000\\014w\\010\\000\\000\\000\\020\\000\\000\\000\\001t\\000\\017TASK_PROPERTIESsr\\000\\037cn.eyecool.quartz.domain.SysJob\\000\\000\\000\\000\\000\\000\\000\\001\\002\\000\\010L\\000\\012concurrentt\\000\\022Ljava/lang/String;L\\000\\016cronExpressionq\\000~\\000\\011L\\000\\014invokeTargetq\\000~\\000\\011L\\000\\010jobGroupq\\000~\\000\\011L\\000\\005jobIdt\\000\\020Ljava/lang/Long;L\\000\\007jobNameq\\000~\\000\\011L\\000\\015misfirePolicyq\\000~\\000\\011L\\000\\006statusq\\000~\\000\\011xr\\000(cn.eyecool.common.core.domain.BaseEntity\\000\\000\\000\\000\\000\\000\\000\\001\\002\\000\\011L\\000\\011beginTimeq\\000~\\000\\011L\\000\\010createByq\\000~\\000\\011L\\000\\012createTimet\\000\\020Ljava/util/Date;L\\000\\007endTimeq\\000~\\000\\011L\\000\\006paramsq\\000~\\000\\003L\\000\\006remarkq\\000~\\000\\011L\\000\\013searchValueq\\000~\\000\\011L\\000\\010updateByq\\000~\\000\\011L\\000\\012updateTimeq\\000~\\000\\014xppt\\000\\005adminsr\\000\\016java.util.Datehj\\201\\001KYt\\031\\003\\000\\000xpw\\010\\000\\000\\001|\\337\\332)\\010xpsr\\000\\027java.util.LinkedHashMap4\\300N\\\\\\020l\\300\\373\\002\\000\\001Z\\000\\013accessOrderxq\\000~\\000\\005?@\\000\\000\\000\\000\\000\\000w\\010\\000\\000\\000\\020\\000\\000\\000\\000x\\000t\\000\\000pt\\000\\005adminpt\\000\\0011t\\000\\0130 0 6 * * ?t\\000>autoCreateAtdRecordTask.convertBioLogToAtdRecord(''2021-10-15'')t\\000\\007DEFAULTsr\\000\\016java.lang.Long;\\213\\344\\220\\314\\217#\\337\\002\\000\\001J\\000\\005valuexr\\000\\020java.lang.Number\\206\\254\\225\\035\\013\\224\\340\\213\\002\\000\\000xp\\000\\000\\000\\000\\000\\000\\000\\026t\\000!\\345\\244\\204\\347\\220\\206\\350\\200\\203\\345\\213\\244\\357\\274\\210\\345\\270\\246\\346\\214\\207\\345\\256\\232\\346\\227\\245\\346\\234\\237\\357\\274\\211t\\000\\0011t\\000\\0011x\\000'::bytea);
INSERT INTO qrtz_job_details(sched_name, job_name, job_group, description, job_class_name, is_durable, is_nonconcurrent, is_update_data, requests_recovery, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME23', 'DEFAULT', NULL, 'cn.eyecool.quartz.util.QuartzDisallowConcurrentExecution', 'false', 'true', 'false', 'false', E'\\254\\355\\000\\005sr\\000\\025org.quartz.JobDataMap\\237\\260\\203\\350\\277\\251\\260\\313\\002\\000\\000xr\\000&org.quartz.utils.StringKeyDirtyFlagMap\\202\\010\\350\\303\\373\\305](\\002\\000\\001Z\\000\\023allowsTransientDataxr\\000\\035org.quartz.utils.DirtyFlagMap\\023\\346.\\255(v\\012\\316\\002\\000\\002Z\\000\\005dirtyL\\000\\003mapt\\000\\017Ljava/util/Map;xp\\001sr\\000\\021java.util.HashMap\\005\\007\\332\\301\\303\\026`\\321\\003\\000\\002F\\000\\012loadFactorI\\000\\011thresholdxp?@\\000\\000\\000\\000\\000\\014w\\010\\000\\000\\000\\020\\000\\000\\000\\001t\\000\\017TASK_PROPERTIESsr\\000\\037cn.eyecool.quartz.domain.SysJob\\000\\000\\000\\000\\000\\000\\000\\001\\002\\000\\010L\\000\\012concurrentt\\000\\022Ljava/lang/String;L\\000\\016cronExpressionq\\000~\\000\\011L\\000\\014invokeTargetq\\000~\\000\\011L\\000\\010jobGroupq\\000~\\000\\011L\\000\\005jobIdt\\000\\020Ljava/lang/Long;L\\000\\007jobNameq\\000~\\000\\011L\\000\\015misfirePolicyq\\000~\\000\\011L\\000\\006statusq\\000~\\000\\011xr\\000(cn.eyecool.common.core.domain.BaseEntity\\000\\000\\000\\000\\000\\000\\000\\001\\002\\000\\011L\\000\\011beginTimeq\\000~\\000\\011L\\000\\010createByq\\000~\\000\\011L\\000\\012createTimet\\000\\020Ljava/util/Date;L\\000\\007endTimeq\\000~\\000\\011L\\000\\006paramsq\\000~\\000\\003L\\000\\006remarkq\\000~\\000\\011L\\000\\013searchValueq\\000~\\000\\011L\\000\\010updateByq\\000~\\000\\011L\\000\\012updateTimeq\\000~\\000\\014xppt\\000\\005adminsr\\000\\016java.util.Datehj\\201\\001KYt\\031\\003\\000\\000xpw\\010\\000\\000\\001|\\337\\333\\017\\200xpsr\\000\\027java.util.LinkedHashMap4\\300N\\\\\\020l\\300\\373\\002\\000\\001Z\\000\\013accessOrderxq\\000~\\000\\005?@\\000\\000\\000\\000\\000\\000w\\010\\000\\000\\000\\020\\000\\000\\000\\000x\\000t\\000\\000pt\\000\\005adminpt\\000\\0011t\\000\\0130 0 6 * * ?t\\0002autoCreateAtdRecordTask.convertBioLogToAtdRecord()t\\000\\007DEFAULTsr\\000\\016java.lang.Long;\\213\\344\\220\\314\\217#\\337\\002\\000\\001J\\000\\005valuexr\\000\\020java.lang.Number\\206\\254\\225\\035\\013\\224\\340\\213\\002\\000\\000xp\\000\\000\\000\\000\\000\\000\\000\\027t\\000\\014\\345\\244\\204\\347\\220\\206\\350\\200\\203\\345\\213\\244t\\000\\0011t\\000\\0010x\\000'::bytea);
INSERT INTO qrtz_triggers(sched_name, trigger_name, trigger_group, job_name, job_group, description, next_fire_time, prev_fire_time, priority, trigger_state, trigger_type, start_time, end_time, calendar_name, misfire_instr, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME23', 'DEFAULT', 'TASK_CLASS_NAME23', 'DEFAULT', NULL, 1635890400000, -1, 5, 'WAITING', 'CRON', 1635843383000, 0, NULL, -1, E'\\\\x'::bytea);
INSERT INTO qrtz_triggers(sched_name, trigger_name, trigger_group, job_name, job_group, description, next_fire_time, prev_fire_time, priority, trigger_state, trigger_type, start_time, end_time, calendar_name, misfire_instr, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME22', 'DEFAULT', 'TASK_CLASS_NAME22', 'DEFAULT', NULL, 1635890400000, -1, 5, 'PAUSED', 'CRON', 1635843362000, 0, NULL, -1, E'\\\\x'::bytea);
INSERT INTO qrtz_cron_triggers(sched_name, trigger_name, trigger_group, cron_expression, time_zone_id) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME22', 'DEFAULT', '0 0 6 * * ?', 'Asia/Shanghai');
INSERT INTO qrtz_cron_triggers(sched_name, trigger_name, trigger_group, cron_expression, time_zone_id) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME23', 'DEFAULT', '0 0 6 * * ?', 'Asia/Shanghai');

-- ----------------------------
-- 部门考勤规则表
-- ----------------------------
CREATE TABLE atd_dept_rule ( 
	id varchar(48)   NOT NULL,
	dept_id INT8,
	rule_id varchar(48), 
	create_by varchar(50),
	update_by varchar(50),
	create_time timestamp,
	update_time timestamp,
	tenant_id varchar(255)  DEFAULT 'super',
  CONSTRAINT pk_atd_dept_rule PRIMARY KEY (id) 
 );
 CREATE UNIQUE INDEX dept_idatd_dept_rule_index ON atd_dept_rule (dept_id, rule_id);
 COMMENT ON COLUMN atd_dept_rule.id IS'id 主键';
 COMMENT ON COLUMN atd_dept_rule.dept_id IS'部门ID';
 COMMENT ON COLUMN atd_dept_rule.rule_id IS'规则id';
 COMMENT ON COLUMN atd_dept_rule.create_by IS'创建人';
 COMMENT ON COLUMN atd_dept_rule.update_by IS'更新人';
 COMMENT ON COLUMN atd_dept_rule.create_time IS'创建时间';
 COMMENT ON COLUMN atd_dept_rule.update_time IS'更新时间';
 COMMENT ON COLUMN atd_dept_rule.tenant_id IS'租户ID';
 COMMENT ON TABLE atd_dept_rule IS '部门考勤规则表';
-- ----------------------------
-- 记录白名单人员表
-- ----------------------------
CREATE TABLE atd_person_white ( 
	id varchar(48)   NOT NULL,
	psn_id varchar(48)   NOT NULL,
	psn_unique_id varchar(48)   NOT NULL,
	create_time timestamp,
	tenant_id varchar(255)  DEFAULT 'super',
  CONSTRAINT pk_atd_person_white PRIMARY KEY (id) 
 );
 CREATE UNIQUE INDEX psn_idatd_person_white_index ON atd_person_white (psn_id); 
 CREATE INDEX tenant_idatd_person_white_index ON atd_person_white (tenant_id); 
 COMMENT ON COLUMN atd_person_white.id IS'';
 COMMENT ON COLUMN atd_person_white.psn_id IS'人员id';
 COMMENT ON COLUMN atd_person_white.psn_unique_id IS'人员标识';
 COMMENT ON COLUMN atd_person_white.create_time IS'创建时间';
 COMMENT ON COLUMN atd_person_white.tenant_id IS'租户ID';
 COMMENT ON TABLE atd_person_white IS '记录白名单人员表';
-- ----------------------------
-- 考勤记录表
-- ----------------------------
CREATE TABLE atd_record ( 
	id varchar(48)   NOT NULL,
	atd_date varchar(10),
	psn_id varchar(48)   NOT NULL,
	psn_unique_id varchar(48)   NOT NULL,
	psn_name varchar(100),
	dept_id INT8,
	dept_name varchar(100),
	psn_no varchar(100),
	psn_type varchar(10),
	psn_type_name varchar(100),
	detail_times_id varchar(32),
	times_name varchar(100),
	sign_in varchar(20),
	sign_out varchar(20),
	sign_in_time varchar(8),
	sign_out_time varchar(8),
	clock_mark char(1)  DEFAULT 'F',
	clock_mark_name varchar(100),
	create_by varchar(50),
	update_by varchar(50),
	create_time timestamp,
	update_time timestamp,
	tenant_id varchar(255)  DEFAULT 'super',
	status char(1),
  CONSTRAINT pk_atd_record PRIMARY KEY (id) 
 );

 CREATE UNIQUE INDEX atd_dateatd_record_index ON atd_record (atd_date, detail_times_id,psn_unique_id,tenant_id);
 CREATE INDEX psn_idatd_record_index ON atd_record (psn_id); 
 CREATE INDEX detail_times_idatd_record_index ON atd_record (detail_times_id); 
 CREATE INDEX tenant_idatd_record_index ON atd_record (tenant_id); 
 COMMENT ON COLUMN atd_record.id IS'主键 id';
 COMMENT ON COLUMN atd_record.atd_date IS'考勤日期';
 COMMENT ON COLUMN atd_record.psn_id IS'人员id';
 COMMENT ON COLUMN atd_record.psn_unique_id IS'人员标识';
 COMMENT ON COLUMN atd_record.psn_name IS'姓名';
 COMMENT ON COLUMN atd_record.dept_id IS'部门id';
 COMMENT ON COLUMN atd_record.dept_name IS'部门名称';
 COMMENT ON COLUMN atd_record.psn_no IS'人员编号';
 COMMENT ON COLUMN atd_record.psn_type IS'人员类型';
 COMMENT ON COLUMN atd_record.psn_type_name IS'人员类型名称';
 COMMENT ON COLUMN atd_record.detail_times_id IS'时间段id';
 COMMENT ON COLUMN atd_record.times_name IS'对应时段名称';
 COMMENT ON COLUMN atd_record.sign_in IS'上班时间';
 COMMENT ON COLUMN atd_record.sign_out IS'下班时间';
 COMMENT ON COLUMN atd_record.sign_in_time IS'签到时间';
 COMMENT ON COLUMN atd_record.sign_out_time IS'签退时间';
 COMMENT ON COLUMN atd_record.clock_mark IS'打卡状态';
 COMMENT ON COLUMN atd_record.clock_mark_name IS'打卡状态名称';
 COMMENT ON COLUMN atd_record.create_by IS'创建人';
 COMMENT ON COLUMN atd_record.update_by IS'更新人';
 COMMENT ON COLUMN atd_record.create_time IS'创建时间';
 COMMENT ON COLUMN atd_record.update_time IS'更新时间';
 COMMENT ON COLUMN atd_record.tenant_id IS'租户ID';
 COMMENT ON COLUMN atd_record.status IS'记录状态';
 COMMENT ON TABLE atd_record IS '考勤记录表';
-- ----------------------------
-- 个人考勤记录标识表
-- ----------------------------
CREATE TABLE atd_record_mark_rpt ( 
	id varchar(48)   NOT NULL,
	atd_date varchar(10),
	psn_id varchar(48)   NOT NULL,
	psn_unique_id varchar(48)   NOT NULL,
	psn_name varchar(100),
	dept_id INT8,
	dept_name varchar(100),
	psn_no varchar(100),
	psn_type varchar(10),
	psn_type_name varchar(100),
	detail_times_id varchar(32),
	times_name varchar(100),
	clock_mark0 char(1),
	clock_mark1 char(1),
	clock_mark2 char(1),
	clock_mark3 char(1),
	clock_mark4 char(1),
	clock_mark5 char(1),
	create_by varchar(50),
	update_by varchar(50),
	create_time timestamp,
	update_time timestamp,
	status char(1),
	attendance char(1),
	tenant_id varchar(255)  DEFAULT 'super',
  CONSTRAINT pk_atd_record_mark_rpt PRIMARY KEY (id) 
 );
 CREATE UNIQUE INDEX atd_dateatd_record_mark_rpt_index ON atd_record_mark_rpt (atd_date,psn_unique_id,tenant_id);
 CREATE INDEX psn_idatd_record_mark_rpt_index ON atd_record_mark_rpt (psn_id); 
 CREATE INDEX detail_times_idatd_record_mark_rpt_index ON atd_record_mark_rpt (detail_times_id); 
 CREATE INDEX tenant_idatd_record_mark_rpt_index ON atd_record_mark_rpt (tenant_id); 
 COMMENT ON COLUMN atd_record_mark_rpt.id IS'id';
 COMMENT ON COLUMN atd_record_mark_rpt.atd_date IS'考勤日期';
 COMMENT ON COLUMN atd_record_mark_rpt.psn_id IS'人员id';
 COMMENT ON COLUMN atd_record_mark_rpt.psn_unique_id IS'人员标识';
 COMMENT ON COLUMN atd_record_mark_rpt.psn_name IS'姓名';
 COMMENT ON COLUMN atd_record_mark_rpt.dept_id IS'部门id';
 COMMENT ON COLUMN atd_record_mark_rpt.dept_name IS'部门名称';
 COMMENT ON COLUMN atd_record_mark_rpt.psn_no IS'人员编号';
 COMMENT ON COLUMN atd_record_mark_rpt.psn_type IS'人员类型';
 COMMENT ON COLUMN atd_record_mark_rpt.psn_type_name IS'人员类型名称';
 COMMENT ON COLUMN atd_record_mark_rpt.detail_times_id IS'时间段id';
 COMMENT ON COLUMN atd_record_mark_rpt.times_name IS'对应时段名称';
 COMMENT ON COLUMN atd_record_mark_rpt.clock_mark0 IS'打卡状态0';
 COMMENT ON COLUMN atd_record_mark_rpt.clock_mark1 IS'打卡状态1';
 COMMENT ON COLUMN atd_record_mark_rpt.clock_mark2 IS'打卡状态2';
 COMMENT ON COLUMN atd_record_mark_rpt.clock_mark3 IS'打卡状态3';
 COMMENT ON COLUMN atd_record_mark_rpt.clock_mark4 IS'打卡状态4';
 COMMENT ON COLUMN atd_record_mark_rpt.clock_mark5 IS'打卡状态5';
 COMMENT ON COLUMN atd_record_mark_rpt.create_by IS'创建人';
 COMMENT ON COLUMN atd_record_mark_rpt.update_by IS'更新人';
 COMMENT ON COLUMN atd_record_mark_rpt.create_time IS'创建时间';
 COMMENT ON COLUMN atd_record_mark_rpt.update_time IS'更新时间';
 COMMENT ON COLUMN atd_record_mark_rpt.status IS'考勤状态';
 COMMENT ON COLUMN atd_record_mark_rpt.attendance IS'出勤情况';
 COMMENT ON COLUMN atd_record_mark_rpt.tenant_id IS'租户ID';
 COMMENT ON TABLE atd_record_mark_rpt IS '个人考勤记录标识表';
-- ----------------------------
-- 各考勤规则对应的详情表
-- ----------------------------
CREATE TABLE atd_rule_detail ( 
	id varchar(48)   NOT NULL,
	rule_id varchar(48)   NOT NULL,
	week varchar(20),
	active_flag char(1),
	create_by varchar(50),
	update_by varchar(50),
	create_time timestamp,
	update_time timestamp,
	tenant_id varchar(255)  DEFAULT 'super',
  CONSTRAINT pk_atd_rule_detail PRIMARY KEY (id) 
 );
 COMMENT ON COLUMN atd_rule_detail.id IS'主键id';
 COMMENT ON COLUMN atd_rule_detail.rule_id IS'规则id';
 COMMENT ON COLUMN atd_rule_detail.week IS'星期标识';
 COMMENT ON COLUMN atd_rule_detail.active_flag IS'启用';
 COMMENT ON COLUMN atd_rule_detail.create_by IS'创建人';
 COMMENT ON COLUMN atd_rule_detail.update_by IS'更新人';
 COMMENT ON COLUMN atd_rule_detail.create_time IS'创建时间';
 COMMENT ON COLUMN atd_rule_detail.update_time IS'更新时间';
 COMMENT ON COLUMN atd_rule_detail.tenant_id IS'租户ID';
 COMMENT ON TABLE atd_rule_detail IS '各考勤规则对应的详情表';
-- ----------------------------
-- 各考勤规则对应的详情时间段信息表
-- ----------------------------
CREATE TABLE atd_rule_detail_time ( 
	id varchar(48)   NOT NULL,
	detail_id varchar(48),
	times_name varchar(100),
	sign_in varchar(20),
	sign_out varchar(20),
	late_num INT4,
	leave_num INT4,
	sign_in_begin varchar(20),
	sign_in_end varchar(20),
	sign_out_begin varchar(20),
	sign_out_end varchar(20),
	sign_in_flag char(1),
	sign_out_flag char(1),
	create_by varchar(50),
	update_by varchar(50),
	create_time timestamp,
	update_time timestamp,
	clock_begin varchar(20),
	clock_end varchar(20),
	clock_ref varchar(10),
	tenant_id varchar(255)  DEFAULT 'super',
  CONSTRAINT pk_atd_rule_detail_time PRIMARY KEY (id) 
 );
 COMMENT ON COLUMN atd_rule_detail_time.id IS'id';
 COMMENT ON COLUMN atd_rule_detail_time.detail_id IS'详情id';
 COMMENT ON COLUMN atd_rule_detail_time.times_name IS'时段名称';
 COMMENT ON COLUMN atd_rule_detail_time.sign_in IS'上班时间';
 COMMENT ON COLUMN atd_rule_detail_time.sign_out IS'下班时间';
 COMMENT ON COLUMN atd_rule_detail_time.late_num IS'记迟到时间（分钟）';
 COMMENT ON COLUMN atd_rule_detail_time.leave_num IS'记早退时间（分钟）';
 COMMENT ON COLUMN atd_rule_detail_time.sign_in_begin IS'开始签到时间';
 COMMENT ON COLUMN atd_rule_detail_time.sign_in_end IS'结束签到时间';
 COMMENT ON COLUMN atd_rule_detail_time.sign_out_begin IS'开始签退时间';
 COMMENT ON COLUMN atd_rule_detail_time.sign_out_end IS'结束签退时间结束签退时间';
 COMMENT ON COLUMN atd_rule_detail_time.sign_in_flag IS'必须签到';
 COMMENT ON COLUMN atd_rule_detail_time.sign_out_flag IS'';
 COMMENT ON COLUMN atd_rule_detail_time.create_by IS'创建人';
 COMMENT ON COLUMN atd_rule_detail_time.update_by IS'更新人';
 COMMENT ON COLUMN atd_rule_detail_time.create_time IS'创建时间';
 COMMENT ON COLUMN atd_rule_detail_time.update_time IS'更新时间';
 COMMENT ON COLUMN atd_rule_detail_time.clock_begin IS'打卡开始时间';
 COMMENT ON COLUMN atd_rule_detail_time.clock_end IS'打卡结束时间';
 COMMENT ON COLUMN atd_rule_detail_time.clock_ref IS'参考规则';
 COMMENT ON COLUMN atd_rule_detail_time.tenant_id IS'租户ID';
 COMMENT ON TABLE atd_rule_detail_time IS '各考勤规则对应的详情时间段信息表';
-- ----------------------------
-- 考勤规则表
-- ----------------------------
CREATE TABLE atd_rules ( 
	id varchar(48)   NOT NULL,
	rule_name varchar(100),
	smart_mode char(1),
	create_by varchar(50),
	update_by varchar(50),
	create_time timestamp,
	update_time timestamp,
	tenant_id varchar(255)  DEFAULT 'super',
  CONSTRAINT pk_atd_rules PRIMARY KEY (id) 
 );
 COMMENT ON COLUMN atd_rules.id IS'主键id';
 COMMENT ON COLUMN atd_rules.rule_name IS'规则名称';
 COMMENT ON COLUMN atd_rules.smart_mode IS'智能模式';
 COMMENT ON COLUMN atd_rules.create_by IS'创建人';
 COMMENT ON COLUMN atd_rules.update_by IS'更新人';
 COMMENT ON COLUMN atd_rules.create_time IS'创建时间';
 COMMENT ON COLUMN atd_rules.update_time IS'更新时间';
 COMMENT ON COLUMN atd_rules.tenant_id IS'租户ID';
 COMMENT ON TABLE atd_rules IS '考勤规则表';
-- ----------------------------
-- 考勤时间段表
-- ----------------------------
CREATE TABLE atd_times ( 
	id varchar(48)   NOT NULL,
	times_name varchar(100),
	sign_in varchar(20),
	sign_out varchar(20),
	late_num INT4,
	leave_num INT4,
	sign_in_begin varchar(20),
	sign_in_end varchar(20),
	sign_out_begin varchar(20),
	sign_out_end varchar(20),
	sign_in_flag char(1),
	sign_out_flag char(1),
	create_by varchar(50),
	update_by varchar(50),
	create_time timestamp,
	update_time timestamp,
	clock_begin varchar(20),
	clock_end varchar(20),
	clock_ref varchar(10),
	tenant_id varchar(255)  DEFAULT 'super',
  CONSTRAINT pk_atd_times PRIMARY KEY (id) 
 );
 COMMENT ON COLUMN atd_times.id IS'假期管理id';
 COMMENT ON COLUMN atd_times.times_name IS'时段名称';
 COMMENT ON COLUMN atd_times.sign_in IS'上班时间';
 COMMENT ON COLUMN atd_times.sign_out IS'下班时间';
 COMMENT ON COLUMN atd_times.late_num IS'记迟到时间（分钟）';
 COMMENT ON COLUMN atd_times.leave_num IS'记早退时间（分钟）';
 COMMENT ON COLUMN atd_times.sign_in_begin IS'开始签到时间';
 COMMENT ON COLUMN atd_times.sign_in_end IS'结束签到时间';
 COMMENT ON COLUMN atd_times.sign_out_begin IS'开始签退时间';
 COMMENT ON COLUMN atd_times.sign_out_end IS'结束签退时间';
 COMMENT ON COLUMN atd_times.sign_in_flag IS'必须签到';
 COMMENT ON COLUMN atd_times.sign_out_flag IS'必须签退';
 COMMENT ON COLUMN atd_times.create_by IS'创建人';
 COMMENT ON COLUMN atd_times.update_by IS'更新人';
 COMMENT ON COLUMN atd_times.create_time IS'创建时间';
 COMMENT ON COLUMN atd_times.update_time IS'更新时间';
 COMMENT ON COLUMN atd_times.clock_begin IS'打卡开始时间';
 COMMENT ON COLUMN atd_times.clock_end IS'打卡结束时间';
 COMMENT ON COLUMN atd_times.clock_ref IS'参考规则';
 COMMENT ON COLUMN atd_times.tenant_id IS'租户ID';
 COMMENT ON TABLE atd_times IS '考勤时间段表';
-- ----------------------------
-- 假期管理表
-- ----------------------------
CREATE TABLE atd_vacation ( 
	id varchar(48)   NOT NULL,
	vacation_name varchar(100),
	vacation_type varchar(10),
	begin_date timestamp,
	end_date timestamp,
	create_time timestamp,
	tenant_id varchar(255)  DEFAULT 'super',
  CONSTRAINT pk_atd_vacation PRIMARY KEY (id) 
 );
 COMMENT ON COLUMN atd_vacation.id IS'假期管理id';
 COMMENT ON COLUMN atd_vacation.vacation_name IS'假期名称';
 COMMENT ON COLUMN atd_vacation.vacation_type IS'假期类型';
 COMMENT ON COLUMN atd_vacation.begin_date IS'开始日期';
 COMMENT ON COLUMN atd_vacation.end_date IS'结束日期';
 COMMENT ON COLUMN atd_vacation.create_time IS'创建时间';
 COMMENT ON COLUMN atd_vacation.tenant_id IS'租户ID';
 COMMENT ON TABLE atd_vacation IS '假期管理表';

ALTER TABLE device_info ADD device_direction varchar(10);
COMMENT ON COLUMN device_info.device_direction IS '设备方向(IN:进，OUT:出，UNKNOWN:未知)';
ALTER TABLE base_person_info ADD ext_attrs varchar(500);
COMMENT ON COLUMN base_person_info.ext_attrs IS '拓展属性JSON';


-- ----------------------------
-- 修改人员基础信息表结构
-- ----------------------------
ALTER TABLE base_person_info ADD  person_type varchar(30) DEFAULT 'user' ;
COMMENT ON COLUMN base_person_info.person_type IS '人员类型：user-人员  visitor-访客(用于访客系统)';

ALTER TABLE base_person_info ADD  inviter_id varchar(48) DEFAULT null;
COMMENT ON COLUMN base_person_info.inviter_id IS '被访人id(用于访客系统)';

ALTER TABLE base_person_info ADD  visitor_status char(1) DEFAULT '0';
COMMENT ON COLUMN base_person_info.visitor_status IS '访客状态：0-启用 1-禁用(用于访客系统)';

ALTER TABLE base_person_info ADD  effective_begin_time timestamp DEFAULT null;
COMMENT ON COLUMN base_person_info.effective_begin_time IS '生效开始时间(用于访客系统)';

ALTER TABLE base_person_info ADD  effective_end_time timestamp DEFAULT null;
COMMENT ON COLUMN base_person_info.effective_end_time IS '生效结束时间(用户访客系统)';


-- ----------------------------
-- 修改人脸基础信息表结构
-- ----------------------------
ALTER TABLE base_person_face ADD  face_image_md5 varchar(255) DEFAULT null;
COMMENT ON COLUMN base_person_face.face_image_md5 IS '人脸base64的MD5';

-- ----------------------------
-- 字典中添加一项 访客生效状态
-- ----------------------------
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (148, '访客生效状态', 'visitor_effective_status', '0', 'admin',now(), '', NULL, '访客生效状态');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (462, 0, '未生效', '0', 'visitor_effective_status', NULL, NULL, 'N', '0', 'admin', now(), '', NULL, NULL);
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (463, 1, '生效中', '1', 'visitor_effective_status', NULL, NULL, 'N', '0', 'admin', now(), '', NULL, NULL);
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (464, 2, '已失效', '2', 'visitor_effective_status', NULL, NULL, 'N', '0', 'admin', now(), '', NULL, NULL);

-- ----------------------------
-- 菜单 基础信息中的访客管理
-- ----------------------------
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2648, '访客管理', 2012, 7, 'visitor', 'basedata/visitor', 1, 0, 'C', '0', '0', '', '#', 'admin', now(), '', NULL, '', 'SIDE');

-- ----------------------------
-- 菜单 基础信息中的访客管理-二级菜单
-- ----------------------------
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2649, '访客信息', 2648, 1, 'visitorinfo', 'basedata/visitor/index', 1, 0, 'C', '0', '0', 'basedata:visitor:list', '#', 'admin', now(), '', NULL, '', 'TAB');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2650, '访客人脸', 2648, 2, 'visitorface', 'basedata/visitor/visitorface', 1, 0, 'C', '0', '0', 'basedata:visitorFace:list', '#', 'admin', now(), '', NULL, '', 'TAB');

-- ----------------------------
-- 按钮 基础信息中的访客管理-二级菜单-内部按钮
-- ----------------------------
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2651, '查看访客详情', 2649, 1, '', NULL, 1, 0, 'F', '0', '0', 'basedata:visitor:query', '#', 'admin', now(), '',NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2652, '删除访客', 2649, 2, '', NULL, 1, 0, 'F', '0', '0', 'basedata:visitor:remove', '#', 'admin', now(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2653, '访客人脸详情', 2650, 1, '', NULL, 1, 0, 'F', '0', '0', 'basedata:visitorFace:query', '#', 'admin', now(), '', NULL, '', 'SIDE');

ALTER TABLE person_face_search_log ALTER COLUMN scene_image DROP NOT NULL;
ALTER TABLE person_faceiris_search_log ADD valid_type varchar(2) DEFAULT '02';
COMMENT ON COLUMN person_faceiris_search_log.valid_type IS '核验方式(01:身份证核验，02：刷脸核验，03：健康码核验，04：刷卡核验)';
ALTER TABLE person_face_search_log ADD card_no varchar(48) DEFAULT '02';
COMMENT ON COLUMN person_face_search_log.card_no IS '卡号';
ALTER TABLE person_faceiris_search_log ADD card_no varchar(48) DEFAULT '02';
COMMENT ON COLUMN person_faceiris_search_log.card_no IS '卡号';


-- ----------------------------
-- 系统设置-参数设置中添加一个访客模块使用的-访客访问公司名称
-- ----------------------------
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (174, '访客访问公司名称', 'visitor.visit.company.name', '-', 'Y', 'admin', now(), '', NULL, NULL, 'Y');

INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (465, 49, '虹膜人脸多模态认证(1:1)', 'MULIT_IRIS_FACE_VERIFY', 'http_interface', NULL, NULL, 'N', '0', 'admin', now(), 'admin', now(), 'HTTP交易接口--虹膜人脸多模态认证(1:1)');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (177, '多模态1v1日志照片存储路径', 'busi.mulit.compare.dir', './eyecool/busi/mulit/compare/', 'N', 'admin', now(), 'admin', now(), NULL, 'N');
UPDATE sys_config SET config_name = '多模态1vN日志照片存储路径', config_value = './eyecool/busi/mulit/search/' WHERE config_key = 'busi.mulit.search.dir';

CREATE TABLE person_faceiris_match_log (
  id varchar(48) NOT NULL,
  received_seq varchar(48) NOT NULL,
  healthcode_log_id varchar(48),
  unique_id varchar(50),
  dept_id varchar(50),
  dept_name varchar(255),
  person_name varchar(255),
  channel_code varchar(255),
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
  time_used INT8 ,
  face_time_used INT8,
  iris_time_used INT8,
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
  temperature_result varchar(1),
  CONSTRAINT pk_person_faceiris_match_log PRIMARY KEY (id, received_time)
);
COMMENT ON COLUMN person_faceiris_match_log.id IS '主键';
COMMENT ON COLUMN person_faceiris_match_log.received_seq IS '业务流水号';
COMMENT ON COLUMN person_faceiris_match_log.healthcode_log_id IS '健康码日志ID';
COMMENT ON COLUMN person_faceiris_match_log.unique_id IS '人员唯一编号';
COMMENT ON COLUMN person_faceiris_match_log.dept_id IS '部门ID';
COMMENT ON COLUMN person_faceiris_match_log.dept_name IS '部门名称';
COMMENT ON COLUMN person_faceiris_match_log.person_name IS '人员姓名';
COMMENT ON COLUMN person_faceiris_match_log.channel_code IS '渠道编码';
COMMENT ON COLUMN person_faceiris_match_log.scene_face_image IS '人脸现场照路径';
COMMENT ON COLUMN person_faceiris_match_log.scene_iris_image IS '虹膜现场照路径';
COMMENT ON COLUMN person_faceiris_match_log.scene_face_score IS '人脸比对分数';
COMMENT ON COLUMN person_faceiris_match_log.scene_iris_score IS '虹膜比对分数';
COMMENT ON COLUMN person_faceiris_match_log.stock_face_image IS '底库人脸照路径';
COMMENT ON COLUMN person_faceiris_match_log.stock_iris_image IS '底库虹膜照路径';
COMMENT ON COLUMN person_faceiris_match_log.match_mode IS '比对模式字典matchMode';
COMMENT ON COLUMN person_faceiris_match_log.match_score IS '比对分数，比对通过的分数或者融合分数';
COMMENT ON COLUMN person_faceiris_match_log.checklive_score IS '现场照检活分值';
COMMENT ON COLUMN person_faceiris_match_log.checklive_result IS '现场照检活结果(0通过，1未通过)';
COMMENT ON COLUMN person_faceiris_match_log.result IS '结果(0通过，1未通过)';
COMMENT ON COLUMN person_faceiris_match_log.received_time IS '交易时间';
COMMENT ON COLUMN person_faceiris_match_log.time_used IS '比对用时ms';
COMMENT ON COLUMN person_faceiris_match_log.face_time_used IS '人脸比对用时ms';
COMMENT ON COLUMN person_faceiris_match_log.iris_time_used IS '虹膜比对用时ms';
COMMENT ON COLUMN person_faceiris_match_log.temperature IS '温度';
COMMENT ON COLUMN person_faceiris_match_log.temperature_floor IS '温度阈值下限';
COMMENT ON COLUMN person_faceiris_match_log.temperature_top IS '温度阈值上限制';
COMMENT ON COLUMN person_faceiris_match_log.device_sn IS '设备标识';
COMMENT ON COLUMN person_faceiris_match_log.device_model IS '设备型号编码';
COMMENT ON COLUMN person_faceiris_match_log.device_name IS '设备名称';
COMMENT ON COLUMN person_faceiris_match_log.device_ip IS '设备IP';
COMMENT ON COLUMN person_faceiris_match_log.device_longitude IS '设备经度(东经)';
COMMENT ON COLUMN person_faceiris_match_log.device_dimension IS '设备维度(北纬)';
COMMENT ON COLUMN person_faceiris_match_log.device_direction IS '设备方向(IN:进，OUT:出，UNKNOWN：未知)';
COMMENT ON COLUMN person_faceiris_match_log.server_id IS '服务器标识';
COMMENT ON COLUMN person_faceiris_match_log.create_time IS '创建时间';
COMMENT ON COLUMN person_faceiris_match_log.batch_date IS '定时任务执行时间';
COMMENT ON COLUMN person_faceiris_match_log.remark IS '备注';
COMMENT ON COLUMN person_faceiris_match_log.tenant_id IS '租户ID';
COMMENT ON COLUMN person_faceiris_match_log.temperature_result IS '测温结果(0:未布控,1:体温正常,2:高体温报警,3:低体温报警,4:未测到温度(不在测温区域,体温未确认完成))';
COMMENT ON TABLE person_faceiris_match_log IS '人脸虹膜多模态比对日志表';
create trigger trigger_faceiris_match_log before insert on person_faceiris_match_log for each row execute procedure ins_record_trigger_fun('yyyymmdd'); 

INSERT INTO sys_job(job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, update_by, update_time, remark) VALUES (24, '人脸虹膜多模态1v1日志分区维护', 'DEFAULT', 'clearBioMatchLogTask.clearMulitMatchLog(365,false,''eyecool_assps'')', '0 0 1 * * ? *', '1', '1', '0', 'admin', '2021-12-09 14:46:06.431', '', '2021-12-09 14:46:13.038722', '');
INSERT INTO qrtz_job_details(sched_name, job_name, job_group, description, job_class_name, is_durable, is_nonconcurrent, is_update_data, requests_recovery, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME24', 'DEFAULT', NULL, 'cn.eyecool.quartz.util.QuartzDisallowConcurrentExecution', 'false', 'true', 'false', 'false', E'\\254\\355\\000\\005sr\\000\\025org.quartz.JobDataMap\\237\\260\\203\\350\\277\\251\\260\\313\\002\\000\\000xr\\000&org.quartz.utils.StringKeyDirtyFlagMap\\202\\010\\350\\303\\373\\305](\\002\\000\\001Z\\000\\023allowsTransientDataxr\\000\\035org.quartz.utils.DirtyFlagMap\\023\\346.\\255(v\\012\\316\\002\\000\\002Z\\000\\005dirtyL\\000\\003mapt\\000\\017Ljava/util/Map;xp\\001sr\\000\\021java.util.HashMap\\005\\007\\332\\301\\303\\026`\\321\\003\\000\\002F\\000\\012loadFactorI\\000\\011thresholdxp?@\\000\\000\\000\\000\\000\\014w\\010\\000\\000\\000\\020\\000\\000\\000\\001t\\000\\017TASK_PROPERTIESsr\\000\\037cn.eyecool.quartz.domain.SysJob\\000\\000\\000\\000\\000\\000\\000\\001\\002\\000\\010L\\000\\012concurrentt\\000\\022Ljava/lang/String;L\\000\\016cronExpressionq\\000~\\000\\011L\\000\\014invokeTargetq\\000~\\000\\011L\\000\\010jobGroupq\\000~\\000\\011L\\000\\005jobIdt\\000\\020Ljava/lang/Long;L\\000\\007jobNameq\\000~\\000\\011L\\000\\015misfirePolicyq\\000~\\000\\011L\\000\\006statusq\\000~\\000\\011xr\\000(cn.eyecool.common.core.domain.BaseEntity\\000\\000\\000\\000\\000\\000\\000\\001\\002\\000\\011L\\000\\011beginTimeq\\000~\\000\\011L\\000\\010createByq\\000~\\000\\011L\\000\\012createTimet\\000\\020Ljava/util/Date;L\\000\\007endTimeq\\000~\\000\\011L\\000\\006paramsq\\000~\\000\\003L\\000\\006remarkq\\000~\\000\\011L\\000\\013searchValueq\\000~\\000\\011L\\000\\010updateByq\\000~\\000\\011L\\000\\012updateTimeq\\000~\\000\\014xppt\\000\\005adminpppppppt\\000\\0011t\\000\\0150 0 1 * * ? *t\\000BclearBioMatchLogTask.clearMulitMatchLog(365,false,''eyecool_assps'')t\\000\\007DEFAULTsr\\000\\016java.lang.Long;\\213\\344\\220\\314\\217#\\337\\002\\000\\001J\\000\\005valuexr\\000\\020java.lang.Number\\206\\254\\225\\035\\013\\224\\340\\213\\002\\000\\000xp\\000\\000\\000\\000\\000\\000\\000\\030t\\000*\\344\\272\\272\\350\\204\\270\\350\\231\\271\\350\\206\\234\\345\\244\\232\\346\\250\\241\\346\\200\\2011v1\\346\\227\\245\\345\\277\\227\\345\\210\\206\\345\\214\\272\\347\\273\\264\\346\\212\\244t\\000\\0011t\\000\\0011x\\000');
INSERT INTO qrtz_triggers(sched_name, trigger_name, trigger_group, job_name, job_group, description, next_fire_time, prev_fire_time, priority, trigger_state, trigger_type, start_time, end_time, calendar_name, misfire_instr, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME24', 'DEFAULT', 'TASK_CLASS_NAME24', 'DEFAULT', NULL, 1639069200000, -1, 5, 'WAITING', 'CRON', 1639032366000, 0, NULL, -1, E'\\\\x');
INSERT INTO qrtz_cron_triggers(sched_name, trigger_name, trigger_group, cron_expression, time_zone_id) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME24', 'DEFAULT', '0 0 1 * * ? *', 'Asia/Shanghai');
SELECT setval('sys_config_config_id_seq', 177, true);
SELECT setval('sys_menu_menu_id_seq',2653, true);
SELECT setval('sys_dict_data_dict_code_seq', 465, true);
SELECT setval('sys_dict_type_dict_id_seq', 147, true);
SELECT setval('sys_job_job_id_seq', 24, true);

-- 虹膜人脸1v1日志展示菜单
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2654, '人脸虹膜比对', 2406, 8, 'faceirisMatch', 'tradelog/faceirisMatch/index', 1, 0, 'C', '0', '0', 'tradelog:faceirisMatch:list', '#', 'admin', now(), 'admin', now(), '人脸虹膜多模态比对日志菜单', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2655, '人脸虹膜比对日志查询', 2654, 1, '#', '', 1, 0, 'F', '0', '0', 'tradelog:faceirisMatch:query', '#', 'admin', now(), 'admin', now(), '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2656, '人脸虹膜比对日志导出', 2654, 5, '#', '', 1, 0, 'F', '0', '0', 'tradelog:faceirisMatch:export', '#', 'admin', now(), 'admin', now(), '', 'SIDE');
SELECT setval('sys_menu_menu_id_seq',2656, true);