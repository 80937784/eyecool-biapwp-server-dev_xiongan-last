ALTER TABLE person_face_match_log ADD COLUMN temperature_result varchar(1) COMMENT '测温结果(0:未布控,1:体温正常,2:高体温报警,3:低体温报警,4:未测到温度(不在测温区域,体温未确认完成))';
ALTER TABLE person_face_search_log ADD COLUMN temperature_result varchar(1) COMMENT '测温结果(0:未布控,1:体温正常,2:高体温报警,3:低体温报警,4:未测到温度(不在测温区域,体温未确认完成))';
ALTER TABLE person_faceiris_search_log ADD COLUMN temperature_result varchar(1) COMMENT '测温结果(0:未布控,1:体温正常,2:高体温报警,3:低体温报警,4:未测到温度(不在测温区域,体温未确认完成))';
ALTER TABLE person_face_search_log ADD COLUMN valid_type varchar(2) default '02' COMMENT '核验方式(01:身份证核验，02：刷脸核验，03：健康码核验 04：刷卡核验)';

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
	log.temperature_result,
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
	log.temperature_result,
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
	
	
-- 多时段考勤主菜单
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2615, '多时段考勤', 0, 12, 'attendance', NULL, 1, 0, 'M', '0', '0', '', 'table', 'admin',sysdate(), '', NULL, '', 'SIDE');

-- 多时段考勤二级菜单
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2616, '假期管理', 2615, 1, 'vacation', 'attendance/vacation/index', 1, 0, 'C', '0', '0', 'attendance:vacation:list', 'build', 'admin',sysdate(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2620, '白名单管理', 2615, 0, 'white', 'attendance/white/index', 1, 0, 'C', '0', '0', 'attendance:white:list', 'base', 'admin', sysdate(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2623, '时段管理', 2615, 2, 'times', 'attendance/times/index', 1, 0, 'C', '0', '0', 'attendance:times:list', 'date', 'admin',sysdate(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2628, '考勤规则', 2615, 3, 'rules', 'attendance/rules/index', 1, 0, 'C', '0', '0', 'attendance:rules:list', 'base', 'admin', sysdate(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2633, '部门考勤规则', 2615, 4, 'rule', 'attendance/rule/index', 1, 0, 'C', '0', '0', 'attendance:rule:list', 'build', 'admin', sysdate(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2639, '考勤记录', 2615, 5, 'rpt', 'attendance/rpt/index', 1, 0, 'C', '0', '0', 'attendance:rpt:list', 'base', 'admin', sysdate(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2642, '个人考勤率', 2615, 6, 'rpteveryone', 'attendance/rpteveryone/index', 1, 0, 'C', '0', '0', 'attendance:rpt:list', 'base', 'admin', sysdate(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2645, '部门考勤率', 2615, 7, 'rptdept', 'attendance/rptdept/index', 1, 0, 'C', '0', '0', 'attendance:rpt:list', 'base', 'admin', sysdate(), '', NULL, '', 'SIDE');

 -- 多时段考勤二级菜单按钮
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2617, '假期管理删除', 2616, 3, '', NULL, 1, 0, 'F', '0', '0', 'attendance:vacation:remove', '#', 'admin',sysdate(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2618, '假期管理修改', 2616, 2, '', NULL, 1, 0, 'F', '0', '0', 'attendance:vacation:edit', '#', 'admin',sysdate(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2619, '假期管理新增', 2616, 1, '', NULL, 1, 0, 'F', '0', '0', 'attendance:vacation:add', '#', 'admin', sysdate(), '',NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2621, '非白名单列表', 2620, 1, '', NULL, 1, 0, 'F', '0', '0', 'attendance:white:import', '#', 'admin', sysdate(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2622, '考勤白名单删除', 2620, 2, '', NULL, 1, 0, 'F', '0', '0', 'attendance:white:remove', '#', 'admin', sysdate(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2624, '时段管理导出', 2623, 4, '', NULL, 1, 0, 'F', '0', '0', 'attendance:times:export', '#', 'admin', sysdate(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2625, '时段管理删除', 2623, 3, '', NULL, 1, 0, 'F', '0', '0', 'attendance:times:remove', '#', 'admin',sysdate(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2626, '时段管理修改', 2623, 2, '', NULL, 1, 0, 'F', '0', '0', 'attendance:times:edit', '#', 'admin', sysdate(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2627, '时段管理新增', 2623, 1, '', NULL, 1, 0, 'F', '0', '0', 'attendance:times:add', '#', 'admin', sysdate(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2629, '考勤规则获取所有时间段', 2628, 4, '', NULL, 1, 0, 'F', '0', '0', 'attendance:times:query', '#', 'admin', sysdate(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2630, '考勤规则删除', 2628, 3, '', NULL, 1, 0, 'F', '0', '0', 'attendance:rules:remove', '#', 'admin', sysdate(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2631, '考勤规则修改', 2628, 2, '', NULL, 1, 0, 'F', '0', '0', 'attendance:rules:edit', '#', 'admin', sysdate(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2632, '考勤规则新增', 2628, 1, '', NULL, 1, 0, 'F', '0', '0', 'attendance:rules:add', '#', 'admin', sysdate(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2634, '获取所有部门考勤规则', 2633, 5, '', NULL, 1, 0, 'F', '0', '0', 'attendance:rule:query', '#', 'admin', sysdate(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2635, '部门考勤规则获取部门', 2633, 4, '', NULL, 1, 0, 'F', '0', '0', 'system:dept:list', '#', 'admin', sysdate(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2636, '部门考勤规则删除', 2633, 3, '', NULL, 1, 0, 'F', '0', '0', 'attendance:rule:remove', '#', 'admin', sysdate(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2637, '部门考勤规则修改', 2633, 2, '', NULL, 1, 0, 'F', '0', '0', 'attendance:rule:edit', '#', 'admin', sysdate(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2638, '部门考勤规则新增', 2633, 1, '', NULL, 1, 0, 'F', '0', '0', 'attendance:rule:add', '#', 'admin', sysdate(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2640, '考勤记录获取部门', 2639, 2, '', NULL, 1, 0, 'F', '0', '0', 'system:dept:list', '#', 'admin', sysdate(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2641, '考勤记录导出', 2639, 1, '', NULL, 1, 0, 'F', '0', '0', 'attendance:rpt:export', '#', 'admin', sysdate(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2643, '个人考勤率获取部门', 2642, 2, '', NULL, 1, 0, 'F', '0', '0', 'system:dept:list', '#', 'admin', sysdate(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2644, '个人考勤率导出', 2642, 1, '', NULL, 1, 0, 'F', '0', '0', 'attendance:rpt:export', '#', 'admin', sysdate(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2646, '部门考勤率获取部门', 2645, 2, '', NULL, 1, 0, 'F', '0', '0', 'system:dept:list', '#', 'admin', sysdate(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2647, '部门考勤率导出', 2645, 1, '', NULL, 1, 0, 'F', '0', '0', 'attendance:rpt:export', '#', 'admin', sysdate(), '', NULL, '', 'SIDE');





-- 定时任务插入
INSERT INTO sys_job (job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, update_by, update_time, remark)
VALUES ('20', '处理考勤（带指定日期）', 'SYSTEM', 'autoCreateAtdRecordTask.convertBioLogToAtdRecord(\'2021-10-15\')', '0 0 6 * * ?', '1', '1', '1', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_job (job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, update_by, update_time, remark) 
VALUES ('21', '处理考勤', 'SYSTEM', 'autoCreateAtdRecordTask.convertBioLogToAtdRecord()', '0 0 6 * * ?', '1', '1', '0', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO qrtz_job_details (sched_name, job_name, job_group, description, job_class_name, is_durable, is_nonconcurrent, is_update_data, requests_recovery, job_data) 
VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME20', 'SYSTEM', null, 'cn.eyecool.quartz.util.QuartzDisallowConcurrentExecution', '0', '1', '0', '0', 0xACED0005737200156F72672E71756172747A2E4A6F62446174614D61709FB083E8BFA9B0CB020000787200266F72672E71756172747A2E7574696C732E537472696E674B65794469727479466C61674D61708208E8C3FBC55D280200015A0013616C6C6F77735472616E7369656E74446174617872001D6F72672E71756172747A2E7574696C732E4469727479466C61674D617013E62EAD28760ACE0200025A000564697274794C00036D617074000F4C6A6176612F7574696C2F4D61703B787001737200116A6176612E7574696C2E486173684D61700507DAC1C31660D103000246000A6C6F6164466163746F724900097468726573686F6C6478703F4000000000000C7708000000100000000174000F5441534B5F50524F504552544945537372001F636E2E657965636F6F6C2E71756172747A2E646F6D61696E2E5379734A6F6200000000000000010200084C000A636F6E63757272656E747400124C6A6176612F6C616E672F537472696E673B4C000E63726F6E45787072657373696F6E71007E00094C000C696E766F6B6554617267657471007E00094C00086A6F6247726F757071007E00094C00056A6F6249647400104C6A6176612F6C616E672F4C6F6E673B4C00076A6F624E616D6571007E00094C000D6D697366697265506F6C69637971007E00094C000673746174757371007E000978720028636E2E657965636F6F6C2E636F6D6D6F6E2E636F72652E646F6D61696E2E42617365456E7469747900000000000000010200094C0009626567696E54696D6571007E00094C0008637265617465427971007E00094C000A63726561746554696D657400104C6A6176612F7574696C2F446174653B4C0007656E6454696D6571007E00094C0006706172616D7371007E00034C000672656D61726B71007E00094C000B73656172636856616C756571007E00094C0008757064617465427971007E00094C000A75706461746554696D6571007E000C78707074000561646D696E7372000E6A6176612E7574696C2E44617465686A81014B597419030000787077080000017C0B994AC07870707400007070707400013174000B3020302036202A202A203F74003E6175746F4372656174654174645265636F72645461736B2E636F6E7665727442696F4C6F67546F4174645265636F72642827323032312D31302D3135272974000653595354454D7372000E6A6176612E6C616E672E4C6F6E673B8BE490CC8F23DF0200014A000576616C7565787200106A6176612E6C616E672E4E756D62657286AC951D0B94E08B02000078700000000000000014740021E5A484E79086E88083E58BA4EFBC88E5B8A6E68C87E5AE9AE697A5E69C9FEFBC8974000131740001317800);
INSERT INTO qrtz_job_details (sched_name, job_name, job_group, description, job_class_name, is_durable, is_nonconcurrent, is_update_data, requests_recovery, job_data)
VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME21', 'SYSTEM', null, 'cn.eyecool.quartz.util.QuartzDisallowConcurrentExecution', '0', '1', '0', '0', 0xACED0005737200156F72672E71756172747A2E4A6F62446174614D61709FB083E8BFA9B0CB020000787200266F72672E71756172747A2E7574696C732E537472696E674B65794469727479466C61674D61708208E8C3FBC55D280200015A0013616C6C6F77735472616E7369656E74446174617872001D6F72672E71756172747A2E7574696C732E4469727479466C61674D617013E62EAD28760ACE0200025A000564697274794C00036D617074000F4C6A6176612F7574696C2F4D61703B787001737200116A6176612E7574696C2E486173684D61700507DAC1C31660D103000246000A6C6F6164466163746F724900097468726573686F6C6478703F4000000000000C7708000000100000000174000F5441534B5F50524F504552544945537372001F636E2E657965636F6F6C2E71756172747A2E646F6D61696E2E5379734A6F6200000000000000010200084C000A636F6E63757272656E747400124C6A6176612F6C616E672F537472696E673B4C000E63726F6E45787072657373696F6E71007E00094C000C696E766F6B6554617267657471007E00094C00086A6F6247726F757071007E00094C00056A6F6249647400104C6A6176612F6C616E672F4C6F6E673B4C00076A6F624E616D6571007E00094C000D6D697366697265506F6C69637971007E00094C000673746174757371007E000978720028636E2E657965636F6F6C2E636F6D6D6F6E2E636F72652E646F6D61696E2E42617365456E7469747900000000000000010200094C0009626567696E54696D6571007E00094C0008637265617465427971007E00094C000A63726561746554696D657400104C6A6176612F7574696C2F446174653B4C0007656E6454696D6571007E00094C0006706172616D7371007E00034C000672656D61726B71007E00094C000B73656172636856616C756571007E00094C0008757064617465427971007E00094C000A75706461746554696D6571007E000C78707074000561646D696E7372000E6A6176612E7574696C2E44617465686A81014B597419030000787077080000017C6D6E42787870707400007070707400013174000B3020302036202A202A203F7400326175746F4372656174654174645265636F72645461736B2E636F6E7665727442696F4C6F67546F4174645265636F7264282974000653595354454D7372000E6A6176612E6C616E672E4C6F6E673B8BE490CC8F23DF0200014A000576616C7565787200106A6176612E6C616E672E4E756D62657286AC951D0B94E08B0200007870000000000000001574000CE5A484E79086E88083E58BA474000131740001307800);
INSERT INTO qrtz_triggers (sched_name, trigger_name, trigger_group, job_name, job_group, description, next_fire_time, prev_fire_time, priority, trigger_state, trigger_type, start_time, end_time, calendar_name, misfire_instr, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME20', 'SYSTEM', 'TASK_CLASS_NAME20', 'SYSTEM', NULL, '1635544800000', '-1', '5', 'PAUSED', 'CRON', '1635471913000', '0', NULL, '-1', '');
INSERT INTO qrtz_triggers (sched_name, trigger_name, trigger_group, job_name, job_group, description, next_fire_time, prev_fire_time, priority, trigger_state, trigger_type, start_time, end_time, calendar_name, misfire_instr, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME21', 'SYSTEM', 'TASK_CLASS_NAME21', 'SYSTEM', NULL, '1635890400000', '1635804000000', '5', 'WAITING', 'CRON', '1635471913000', '0', NULL, '-1', '');
INSERT INTO qrtz_cron_triggers (sched_name, trigger_name, trigger_group, cron_expression, time_zone_id) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME20', 'SYSTEM', '0 0 6 * * ?', 'Asia/Shanghai');
INSERT INTO qrtz_cron_triggers (sched_name, trigger_name, trigger_group, cron_expression, time_zone_id) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME21', 'SYSTEM', '0 0 6 * * ?', 'Asia/Shanghai');

-- ----------------------------
-- Table structure for atd_dept_rule
-- ----------------------------
DROP TABLE IF EXISTS atd_dept_rule;
CREATE TABLE atd_dept_rule (
  id varchar(48) NOT NULL COMMENT 'id 主键',
  dept_id bigint(20) DEFAULT NULL COMMENT '部门ID',
  rule_id varchar(48) DEFAULT NULL COMMENT '规则id',
  create_by varchar(50) DEFAULT NULL COMMENT '创建人',
  update_by varchar(50) DEFAULT NULL COMMENT '更新人',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  tenant_id varchar(255) DEFAULT 'super' COMMENT '租户ID',
  PRIMARY KEY (id) USING BTREE,
  UNIQUE KEY idx_dept_rule (dept_id,rule_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='部门考勤规则表';

-- ----------------------------
-- Table structure for atd_person_white
-- ----------------------------
DROP TABLE IF EXISTS atd_person_white;
CREATE TABLE atd_person_white (
  id varchar(48) NOT NULL,
  psn_id varchar(48) NOT NULL COMMENT '人员id',
  psn_unique_id varchar(48) NOT NULL COMMENT '人员标识',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  tenant_id varchar(255) DEFAULT 'super' COMMENT '租户ID',
  PRIMARY KEY (id) USING BTREE,
  UNIQUE KEY psn_id (psn_id),
  KEY idx_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='记录白名单人员表';

-- ----------------------------
-- Table structure for atd_record
-- ----------------------------
DROP TABLE IF EXISTS atd_record;
CREATE TABLE atd_record (
  id varchar(48) NOT NULL COMMENT '主键 id',
  atd_date varchar(10) DEFAULT NULL COMMENT '考勤日期',
  psn_id varchar(48) NOT NULL COMMENT '人员id',
  psn_unique_id varchar(48) NOT NULL COMMENT '人员标识',
  psn_name varchar(100) DEFAULT NULL COMMENT '姓名',
  dept_id bigint(20) DEFAULT NULL COMMENT '部门id',
  dept_name varchar(100) DEFAULT NULL COMMENT '部门名称',
  psn_no varchar(100) DEFAULT NULL COMMENT '人员编号',
  psn_type varchar(10) DEFAULT NULL COMMENT '人员类型',
  psn_type_name varchar(100) DEFAULT NULL COMMENT '人员类型名称',
  detail_times_id varchar(32) DEFAULT NULL COMMENT '时间段id',
  times_name varchar(100) DEFAULT NULL COMMENT '对应时段名称',
  sign_in varchar(20) DEFAULT NULL COMMENT '上班时间',
  sign_out varchar(20) DEFAULT NULL COMMENT '下班时间',
  sign_in_time varchar(8) DEFAULT NULL COMMENT '签到时间',
  sign_out_time varchar(8) DEFAULT NULL COMMENT '签退时间',
  clock_mark varchar(1) DEFAULT 'F' COMMENT '打卡状态',
  clock_mark_name varchar(100) DEFAULT NULL COMMENT '打卡状态名称',
  create_by varchar(50) DEFAULT NULL COMMENT '创建人',
  update_by varchar(50) DEFAULT NULL COMMENT '更新人',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  tenant_id varchar(255) DEFAULT 'super' COMMENT '租户ID',
  status char(1) DEFAULT NULL COMMENT '记录状态',
  PRIMARY KEY (id) USING BTREE,
  UNIQUE KEY idx_u_id (atd_date,detail_times_id,psn_unique_id,tenant_id) USING BTREE,
  KEY idx_a_r_psnid (psn_id),
  KEY idx_a_r_timeid (detail_times_id),
  KEY idx_tenant_id (tenant_id),
  KEY idx_a_r_date (atd_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='考勤记录表';

-- ----------------------------
-- Table structure for atd_record_mark_rpt
-- ----------------------------
DROP TABLE IF EXISTS atd_record_mark_rpt;
CREATE TABLE atd_record_mark_rpt (
  id varchar(48) NOT NULL COMMENT 'id',
  atd_date varchar(10) DEFAULT NULL COMMENT '考勤日期',
  psn_id varchar(48) NOT NULL COMMENT '人员id',
  psn_unique_id varchar(48) NOT NULL COMMENT '人员标识',
  psn_name varchar(100) DEFAULT NULL COMMENT '姓名',
  dept_id bigint(20) DEFAULT NULL COMMENT '部门id',
  dept_name varchar(100) DEFAULT NULL COMMENT '部门名称',
  psn_no varchar(100) DEFAULT NULL COMMENT '人员编号',
  psn_type varchar(10) DEFAULT NULL COMMENT '人员类型',
  psn_type_name varchar(100) DEFAULT NULL COMMENT '人员类型名称',
  detail_times_id varchar(32) DEFAULT NULL COMMENT '时间段id',
  times_name varchar(100) DEFAULT NULL COMMENT '对应时段名称',
  clock_mark0 varchar(1) DEFAULT NULL COMMENT '打卡状态0',
  clock_mark1 varchar(1) DEFAULT NULL COMMENT '打卡状态1',
  clock_mark2 varchar(1) DEFAULT NULL COMMENT '打卡状态2',
  clock_mark3 varchar(1) DEFAULT NULL COMMENT '打卡状态3',
  clock_mark4 varchar(1) DEFAULT NULL COMMENT '打卡状态4',
  clock_mark5 varchar(1) DEFAULT NULL COMMENT '打卡状态5',
  create_by varchar(50) DEFAULT NULL COMMENT '创建人',
  update_by varchar(50) DEFAULT NULL COMMENT '更新人',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  status varchar(1) DEFAULT NULL COMMENT '考勤状态',
  attendance varchar(1) DEFAULT NULL COMMENT '出勤情况',
  tenant_id varchar(255) DEFAULT 'super' COMMENT '租户ID',
  PRIMARY KEY (id) USING BTREE,
  UNIQUE KEY idx_rr_uid (atd_date,psn_unique_id,tenant_id) USING BTREE,
  KEY idx_a_rr_psnid (psn_id),
  KEY idx_a_rr_timeid (detail_times_id),
  KEY idx_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='个人考勤记录标识表';

-- ----------------------------
-- Table structure for atd_rule_detail
-- ----------------------------
DROP TABLE IF EXISTS atd_rule_detail;
CREATE TABLE atd_rule_detail (
  id varchar(48) NOT NULL COMMENT '主键id',
  rule_id varchar(48) NOT NULL COMMENT '规则id',
  week varchar(20) DEFAULT NULL COMMENT '星期标识',
  active_flag varchar(1) DEFAULT NULL COMMENT '启用',
  create_by varchar(50) DEFAULT NULL COMMENT '创建人',
  update_by varchar(50) DEFAULT NULL COMMENT '更新人',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  tenant_id varchar(255) DEFAULT 'super' COMMENT '租户ID',
  PRIMARY KEY (id) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='每个考勤规则对应的详情';

-- ----------------------------
-- Table structure for atd_rule_detail_time
-- ----------------------------
DROP TABLE IF EXISTS atd_rule_detail_time;
CREATE TABLE atd_rule_detail_time (
  id varchar(48) NOT NULL COMMENT 'id',
  detail_id varchar(48) DEFAULT NULL COMMENT '详情id',
  times_name varchar(100) DEFAULT NULL COMMENT '时段名称',
  sign_in varchar(20) DEFAULT NULL COMMENT '上班时间',
  sign_out varchar(20) DEFAULT NULL COMMENT '下班时间',
  late_num int(11) DEFAULT NULL COMMENT '记迟到时间（分钟）',
  leave_num int(11) DEFAULT NULL COMMENT '记早退时间（分钟）',
  sign_in_begin varchar(20) DEFAULT NULL COMMENT '开始签到时间',
  sign_in_end varchar(20) DEFAULT NULL COMMENT '结束签到时间',
  sign_out_begin varchar(20) DEFAULT NULL COMMENT '开始签退时间',
  sign_out_end varchar(20) DEFAULT NULL COMMENT '结束签退时间结束签退时间',
  sign_in_flag varchar(1) DEFAULT NULL COMMENT '必须签到',
  sign_out_flag varchar(1) DEFAULT NULL,
  create_by varchar(50) DEFAULT NULL COMMENT '创建人',
  update_by varchar(50) DEFAULT NULL COMMENT '更新人',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  clock_begin varchar(20) DEFAULT NULL COMMENT '打卡开始时间',
  clock_end varchar(20) DEFAULT NULL COMMENT '打卡结束时间',
  clock_ref varchar(10) DEFAULT NULL COMMENT '参考规则',
  tenant_id varchar(255) DEFAULT 'super' COMMENT '租户ID',
  PRIMARY KEY (id) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='每个考勤规则对应的详情时间段信息';

-- ----------------------------
-- Table structure for atd_rules
-- ----------------------------
DROP TABLE IF EXISTS atd_rules;
CREATE TABLE atd_rules (
  id varchar(48) NOT NULL COMMENT '主键id',
  rule_name varchar(100) DEFAULT NULL COMMENT '规则名称',
  smart_mode varchar(1) DEFAULT NULL COMMENT '智能模式',
  create_by varchar(50) DEFAULT NULL COMMENT '创建人',
  update_by varchar(50) DEFAULT NULL COMMENT '更新人',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  tenant_id varchar(255) DEFAULT 'super' COMMENT '租户ID',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='考勤规则表';

-- ----------------------------
-- Table structure for atd_times
-- ----------------------------
DROP TABLE IF EXISTS atd_times;
CREATE TABLE atd_times (
  id varchar(48) NOT NULL COMMENT '假期管理id',
  times_name varchar(100) DEFAULT NULL COMMENT '时段名称',
  sign_in varchar(20) DEFAULT NULL COMMENT '上班时间',
  sign_out varchar(20) DEFAULT NULL COMMENT '下班时间',
  late_num int(11) DEFAULT NULL COMMENT '记迟到时间（分钟）',
  leave_num int(11) DEFAULT NULL COMMENT '记早退时间（分钟）',
  sign_in_begin varchar(20) DEFAULT NULL COMMENT '开始签到时间',
  sign_in_end varchar(20) DEFAULT NULL COMMENT '结束签到时间',
  sign_out_begin varchar(20) DEFAULT NULL COMMENT '开始签退时间',
  sign_out_end varchar(20) DEFAULT NULL COMMENT '结束签退时间',
  sign_in_flag varchar(1) DEFAULT NULL COMMENT '必须签到',
  sign_out_flag varchar(1) DEFAULT NULL COMMENT '必须签退',
  create_by varchar(50) DEFAULT NULL COMMENT '创建人',
  update_by varchar(50) DEFAULT NULL COMMENT '更新人',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  clock_begin varchar(20) DEFAULT NULL COMMENT '打卡开始时间',
  clock_end varchar(20) DEFAULT NULL COMMENT '打卡结束时间',
  clock_ref varchar(10) DEFAULT NULL COMMENT '参考规则',
  tenant_id varchar(255) DEFAULT 'super' COMMENT '租户ID',
  PRIMARY KEY (id) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='考勤时间段表';

-- ----------------------------
-- Table structure for atd_vacation
-- ----------------------------
DROP TABLE IF EXISTS atd_vacation;
CREATE TABLE atd_vacation (
  id varchar(48) NOT NULL COMMENT '假期管理id',
  vacation_name varchar(100) DEFAULT NULL COMMENT '假期名称',
  vacation_type varchar(10) DEFAULT NULL COMMENT '假期类型',
  begin_date datetime DEFAULT NULL COMMENT '开始日期',
  end_date datetime DEFAULT NULL COMMENT '结束日期',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  tenant_id varchar(255) DEFAULT 'super' COMMENT '租户ID',
  PRIMARY KEY (id) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='假期管理表';

ALTER TABLE device_info ADD COLUMN device_direction varchar(10) COMMENT '设备方向(IN:进，OUT:出，UNKNOWN:未知)';
ALTER TABLE base_person_info ADD COLUMN ext_attrs varchar(500) COMMENT '拓展属性JSON';

-- ----------------------------
-- 修改人员基础信息表结构
-- ----------------------------

ALTER TABLE base_person_info ADD COLUMN person_type varchar(30) DEFAULT 'user' COMMENT '人员类型：user-人员  visitor-访客(用于访客系统)';
ALTER TABLE base_person_info ADD COLUMN inviter_id varchar(48) NULL DEFAULT NULL COMMENT '被访人id(用于访客系统)';
ALTER TABLE base_person_info ADD COLUMN visitor_status  char(1) NULL DEFAULT '0' COMMENT '访客状态：0-启用 1-禁用(用于访客系统)';
ALTER TABLE base_person_info ADD COLUMN effective_begin_time datetime(0) NULL DEFAULT NULL COMMENT '生效开始时间(用于访客系统)';
ALTER TABLE base_person_info ADD COLUMN effective_end_time datetime(0) NULL DEFAULT NULL COMMENT '生效结束时间(用户访客系统)';

-- ----------------------------
-- 修改人脸基础信息表结构
-- ----------------------------
ALTER TABLE base_person_face ADD COLUMN face_image_md5 VARCHAR(255) NULL DEFAULT NULL COMMENT '人脸base64的MD5';

-- ----------------------------
-- 字典中添加一项 访客生效状态
-- ----------------------------
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (148, '访客生效状态', 'visitor_effective_status', '0', 'admin',sysdate(), '', NULL, '访客生效状态');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (462, 0, '未生效', '0', 'visitor_effective_status', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, NULL);
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (463, 1, '生效中', '1', 'visitor_effective_status', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, NULL);
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (464, 2, '已失效', '2', 'visitor_effective_status', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, NULL);


-- ----------------------------
-- 菜单 基础信息中的访客管理
-- ----------------------------

INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2648, '访客管理', 2012, 7, 'visitor', 'basedata/visitor', 1, 0, 'C', '0', '0', '', '#', 'admin', sysdate(), '', NULL, '', 'SIDE');

-- ----------------------------
-- 菜单 基础信息中的访客管理-二级菜单
-- ----------------------------
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2649, '访客信息', 2648, 1, 'visitorinfo', 'basedata/visitor/index', 1, 0, 'C', '0', '0', 'basedata:visitor:list', '#', 'admin', sysdate(), '', NULL, '', 'TAB');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2650, '访客人脸', 2648, 2, 'visitorface', 'basedata/visitor/visitorface', 1, 0, 'C', '0', '0', 'basedata:visitorFace:list', '#', 'admin', sysdate(), '', NULL, '', 'TAB');

-- ----------------------------
-- 按钮 基础信息中的访客管理-二级菜单-内部按钮
-- ----------------------------
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2651, '查看访客详情', 2649, 1, '', NULL, 1, 0, 'F', '0', '0', 'basedata:visitor:query', '#', 'admin', sysdate(), '',NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2652, '删除访客', 2649, 2, '', NULL, 1, 0, 'F', '0', '0', 'basedata:visitor:remove', '#', 'admin', sysdate(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2653, '访客人脸详情', 2650, 1, '', NULL, 1, 0, 'F', '0', '0', 'basedata:visitorFace:query', '#', 'admin', sysdate(), '', NULL, '', 'SIDE');

ALTER TABLE person_face_search_log MODIFY COLUMN scene_image varchar(255) default NULL COMMENT '现场照路径';
ALTER TABLE person_faceiris_search_log ADD COLUMN valid_type varchar(2) default '02' COMMENT '核验方式(01:身份证核验，02：生物核验（人脸、虹膜），03：健康码核验 04：刷卡核验)';
ALTER TABLE person_face_search_log ADD COLUMN card_no varchar(48) COMMENT '卡号';
ALTER TABLE person_faceiris_search_log ADD COLUMN card_no varchar(48) COMMENT '卡号';


-- ----------------------------
-- 系统设置-参数设置中添加一个访客模块使用的-访客访问公司名称
-- ----------------------------
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (174, '访客访问公司名称', 'visitor.visit.company.name', '-', 'Y', 'admin', sysdate(), '', NULL, NULL, 'Y');

INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (465, 49, '虹膜人脸多模态认证(1:1)', 'MULIT_IRIS_FACE_VERIFY', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), 'admin', sysdate(), 'HTTP交易接口--虹膜人脸多模态认证(1:1)');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (177, '多模态1v1日志照片存储路径', 'busi.mulit.compare.dir', './eyecool/busi/mulit/compare/', 'N', 'admin', sysdate(), 'admin', sysdate(), NULL, 'N');
UPDATE sys_config SET config_name = '多模态1vN日志照片存储路径', config_value = './eyecool/busi/mulit/search/' WHERE config_key = 'busi.mulit.search.dir';

-- 虹膜人脸多模态1v1日志表
CREATE TABLE person_faceiris_match_log (
  id varchar(48) NOT NULL COMMENT '主键',
  received_seq varchar(48) NOT NULL COMMENT '业务流水号',
  healthcode_log_id varchar(48) DEFAULT NULL COMMENT '健康码日志ID',
  unique_id varchar(50) DEFAULT NULL COMMENT '人员唯一编号',
  dept_id varchar(50) DEFAULT NULL COMMENT '部门ID',
  dept_name varchar(255) DEFAULT NULL COMMENT '部门名称',
  person_name varchar(255) DEFAULT NULL COMMENT '人员姓名',
  channel_code varchar(255) DEFAULT NULL COMMENT '渠道编码',
  scene_face_image varchar(255) DEFAULT NULL COMMENT '人脸现场照路径',
  scene_iris_image varchar(255) DEFAULT NULL COMMENT '虹膜现场照路径',
  scene_face_score double DEFAULT NULL COMMENT '人脸比对分数',
  scene_iris_score double DEFAULT NULL COMMENT '虹膜比对分数',
  stock_face_image varchar(255) DEFAULT NULL COMMENT '底库人脸照路径',
  stock_iris_image varchar(255) DEFAULT NULL COMMENT '底库虹膜照路径',
  match_mode char(1) DEFAULT NULL COMMENT '比对模式字典matchMode',
  match_score double DEFAULT NULL COMMENT '比对分数，比对通过的分数或者融合分数',
  checklive_score double DEFAULT NULL COMMENT '检活分值',
  checklive_result varchar(1) DEFAULT NULL COMMENT '检活结果(0通过，1未通过)',
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
  temperature_result varchar(1) DEFAULT NULL COMMENT '测温结果(0:未布控,1:体温正常,2:高体温报警,3:低体温报警,4:未测到温度(不在测温区域,体温未确认完成))',
  PRIMARY KEY (id,received_time)
) PARTITION BY RANGE (TO_DAYS(received_time)) (
   PARTITION p0 VALUES LESS THAN (0)
) ;
ALTER TABLE person_faceiris_match_log COMMENT='人脸虹膜多模态比对日志表';
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL - 2 DAY), @schemaName, 'person_faceiris_match_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL - 1 DAY), @schemaName, 'person_faceiris_match_log','%Y%m%d');
CALL sp_create_partition(sysdate(), @schemaName, 'person_faceiris_match_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 1 DAY), @schemaName, 'person_faceiris_match_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 2 DAY), @schemaName, 'person_faceiris_match_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 3 DAY), @schemaName, 'person_faceiris_match_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 4 DAY), @schemaName, 'person_faceiris_match_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 5 DAY), @schemaName, 'person_faceiris_match_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 6 DAY), @schemaName, 'person_faceiris_match_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 7 DAY), @schemaName, 'person_faceiris_match_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 8 DAY), @schemaName, 'person_faceiris_match_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 9 DAY), @schemaName, 'person_faceiris_match_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 10 DAY), @schemaName, 'person_faceiris_match_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 11 DAY), @schemaName, 'person_faceiris_match_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 12 DAY), @schemaName, 'person_faceiris_match_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 13 DAY), @schemaName, 'person_faceiris_match_log','%Y%m%d');
CALL sp_create_partition(DATE_ADD(sysdate(), INTERVAL + 14 DAY), @schemaName, 'person_faceiris_match_log','%Y%m%d');

INSERT INTO sys_job(job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, update_by, update_time, remark) VALUES (24, '人脸虹膜多模态1v1日志分区维护', 'DEFAULT', 'clearBioMatchLogTask.clearMulitMatchLog(365,false,\'eyecool_assps\')', '0 0 1 * * ? *', '1', '1', '0', 'admin', '2021-12-09 13:48:58', '', '2021-12-09 13:49:03', '');
INSERT INTO qrtz_job_details(sched_name, job_name, job_group, description, job_class_name, is_durable, is_nonconcurrent, is_update_data, requests_recovery, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME24', 'DEFAULT', NULL, 'cn.eyecool.quartz.util.QuartzDisallowConcurrentExecution', '0', '1', '0', '0', 0xACED0005737200156F72672E71756172747A2E4A6F62446174614D61709FB083E8BFA9B0CB020000787200266F72672E71756172747A2E7574696C732E537472696E674B65794469727479466C61674D61708208E8C3FBC55D280200015A0013616C6C6F77735472616E7369656E74446174617872001D6F72672E71756172747A2E7574696C732E4469727479466C61674D617013E62EAD28760ACE0200025A000564697274794C00036D617074000F4C6A6176612F7574696C2F4D61703B787001737200116A6176612E7574696C2E486173684D61700507DAC1C31660D103000246000A6C6F6164466163746F724900097468726573686F6C6478703F4000000000000C7708000000100000000174000F5441534B5F50524F504552544945537372001F636E2E657965636F6F6C2E71756172747A2E646F6D61696E2E5379734A6F6200000000000000010200084C000A636F6E63757272656E747400124C6A6176612F6C616E672F537472696E673B4C000E63726F6E45787072657373696F6E71007E00094C000C696E766F6B6554617267657471007E00094C00086A6F6247726F757071007E00094C00056A6F6249647400104C6A6176612F6C616E672F4C6F6E673B4C00076A6F624E616D6571007E00094C000D6D697366697265506F6C69637971007E00094C000673746174757371007E000978720028636E2E657965636F6F6C2E636F6D6D6F6E2E636F72652E646F6D61696E2E42617365456E7469747900000000000000010200094C0009626567696E54696D6571007E00094C0008637265617465427971007E00094C000A63726561746554696D657400104C6A6176612F7574696C2F446174653B4C0007656E6454696D6571007E00094C0006706172616D7371007E00034C000672656D61726B71007E00094C000B73656172636856616C756571007E00094C0008757064617465427971007E00094C000A75706461746554696D6571007E000C78707074000561646D696E707070707070707400013174000D3020302031202A202A203F202A740042636C65617242696F4D617463684C6F675461736B2E636C6561724D756C69744D617463684C6F67283336352C66616C73652C27657965636F6F6C5F6173737073272974000744454641554C547372000E6A6176612E6C616E672E4C6F6E673B8BE490CC8F23DF0200014A000576616C7565787200106A6176612E6C616E672E4E756D62657286AC951D0B94E08B0200007870000000000000001874002AE4BABAE884B8E899B9E8869CE5A49AE6A8A1E68081317631E697A5E5BF97E58886E58CBAE7BBB4E68AA474000131740001317800);
INSERT INTO qrtz_triggers(sched_name, trigger_name, trigger_group, job_name, job_group, description, next_fire_time, prev_fire_time, priority, trigger_state, trigger_type, start_time, end_time, calendar_name, misfire_instr, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME24', 'DEFAULT', 'TASK_CLASS_NAME24', 'DEFAULT', NULL, 1639069200000, -1, 5, 'WAITING', 'CRON', 1639028938000, 0, NULL, -1, '');
INSERT INTO qrtz_cron_triggers(sched_name, trigger_name, trigger_group, cron_expression, time_zone_id) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME24', 'DEFAULT', '0 0 1 * * ? *', 'Asia/Shanghai');

-- 虹膜人脸1v1日志展示菜单
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2654, '人脸虹膜比对', 2406, 8, 'faceirisMatch', 'tradelog/faceirisMatch/index', 1, 0, 'C', '0', '0', 'tradelog:faceirisMatch:list', '#', 'admin', sysdate(), 'admin', sysdate(), '人脸虹膜多模态比对日志菜单', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2655, '人脸虹膜比对日志查询', 2654, 1, '#', '', 1, 0, 'F', '0', '0', 'tradelog:faceirisMatch:query', '#', 'admin', sysdate(), 'admin', sysdate(), '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2656, '人脸虹膜比对日志导出', 2654, 5, '#', '', 1, 0, 'F', '0', '0', 'tradelog:faceirisMatch:export', '#', 'admin', sysdate(), 'admin', sysdate(), '', 'SIDE');