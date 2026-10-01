ALTER TABLE person_face_match_log ADD COLUMN temperature_result varchar(1) COMMENT 'Temperature Measurement Result(0: not deployed, 1: normal temperature,2: high temperature alarm, 3: low temperature alarm, 4: temperature is not measured(not in measurement area, temperature is not confirmed))';
ALTER TABLE person_face_search_log ADD COLUMN temperature_result varchar(1) COMMENT 'Temperature Measurement Result(0: not deployed, 1: normal temperature,2: high temperature alarm, 3: low temperature alarm, 4: temperature is not measured(not in measurement area, temperature is not confirmed))';
ALTER TABLE person_faceiris_search_log ADD COLUMN temperature_result varchar(1) COMMENT 'Temperature Measurement Result(0: not deployed, 1: normal temperature,2: high temperature alarm, 3: low temperature alarm, 4: temperature is not measured(not in measurement area, temperature is not confirmed))';
ALTER TABLE person_face_search_log ADD COLUMN valid_type varchar(2) default '02' COMMENT 'Verification mode(01: ID card verification, 02: face verification, 03: health code verification, 04: card swiping verification)';

-- ----------------------------
-- Today Face Transaction Log View
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
	'Face' AS bio_type,
	'Face 1:N Recognition' AS busi_type,
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
	'Face' AS bio_type,
	'Face 1:1 Comparison' AS busi_type,
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
	
	
-- Multi-period Attendance Main Menu
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2615, 'Multi-period Attendance', 0, 12, 'attendance', NULL, 1, 0, 'M', '0', '0', '', 'table', 'admin',sysdate(), '', NULL, '', 'SIDE');

-- Multi-period Attendance Second Class Menu
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2616, 'Vacation Management', 2615, 1, 'vacation', 'attendance/vacation/index', 1, 0, 'C', '0', '0', 'attendance:vacation:list', 'build', 'admin',sysdate(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2620, 'White List Management', 2615, 0, 'white', 'attendance/white/index', 1, 0, 'C', '0', '0', 'attendance:white:list', 'base', 'admin', sysdate(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2623, 'Time Interval Management', 2615, 2, 'times', 'attendance/times/index', 1, 0, 'C', '0', '0', 'attendance:times:list', 'date', 'admin',sysdate(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2628, 'Attendance Regulations', 2615, 3, 'rules', 'attendance/rules/index', 1, 0, 'C', '0', '0', 'attendance:rules:list', 'base', 'admin', sysdate(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2633, 'Department Attendance Regulations', 2615, 4, 'rule', 'attendance/rule/index', 1, 0, 'C', '0', '0', 'attendance:rule:list', 'build', 'admin', sysdate(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2639, 'Attendance Record', 2615, 5, 'rpt', 'attendance/rpt/index', 1, 0, 'C', '0', '0', 'attendance:rpt:list', 'base', 'admin', sysdate(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2642, 'Personal Attendance Rate', 2615, 6, 'rpteveryone', 'attendance/rpteveryone/index', 1, 0, 'C', '0', '0', 'attendance:rpt:list', 'base', 'admin', sysdate(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2645, 'Department Attendance Rate', 2615, 7, 'rptdept', 'attendance/rptdept/index', 1, 0, 'C', '0', '0', 'attendance:rpt:list', 'base', 'admin', sysdate(), '', NULL, '', 'SIDE');

 -- Multi-period Attendance Second Class Menu Button
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2617, 'Delete Vacation Management', 2616, 3, '', NULL, 1, 0, 'F', '0', '0', 'attendance:vacation:remove', '#', 'admin',sysdate(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2618, 'Update Vacation Management', 2616, 2, '', NULL, 1, 0, 'F', '0', '0', 'attendance:vacation:edit', '#', 'admin',sysdate(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2619, 'Add Vacation Management', 2616, 1, '', NULL, 1, 0, 'F', '0', '0', 'attendance:vacation:add', '#', 'admin', sysdate(), '',NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2621, 'Non Whitelist List', 2620, 1, '', NULL, 1, 0, 'F', '0', '0', 'attendance:white:import', '#', 'admin', sysdate(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2622, 'Delete Attendance White List', 2620, 2, '', NULL, 1, 0, 'F', '0', '0', 'attendance:white:remove', '#', 'admin', sysdate(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2624, 'Export Time Interval Management', 2623, 4, '', NULL, 1, 0, 'F', '0', '0', 'attendance:times:export', '#', 'admin', sysdate(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2625, 'Delete Time Interval Management', 2623, 3, '', NULL, 1, 0, 'F', '0', '0', 'attendance:times:remove', '#', 'admin',sysdate(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2626, 'Update Time Interval Management', 2623, 2, '', NULL, 1, 0, 'F', '0', '0', 'attendance:times:edit', '#', 'admin', sysdate(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2627, 'Add Time Interval Management', 2623, 1, '', NULL, 1, 0, 'F', '0', '0', 'attendance:times:add', '#', 'admin', sysdate(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2629, 'Attendance Regulations Get All the Time Intervals', 2628, 4, '', NULL, 1, 0, 'F', '0', '0', 'attendance:times:query', '#', 'admin', sysdate(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2630, 'Delete Attendance Regulations', 2628, 3, '', NULL, 1, 0, 'F', '0', '0', 'attendance:rules:remove', '#', 'admin', sysdate(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2631, 'Update Attendance Regulations', 2628, 2, '', NULL, 1, 0, 'F', '0', '0', 'attendance:rules:edit', '#', 'admin', sysdate(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2632, 'Add Attendance Regulations', 2628, 1, '', NULL, 1, 0, 'F', '0', '0', 'attendance:rules:add', '#', 'admin', sysdate(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2634, 'Get All the Department Attendance Regulations', 2633, 5, '', NULL, 1, 0, 'F', '0', '0', 'attendance:rule:query', '#', 'admin', sysdate(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2635, 'Department Attendance Regulations Get Department ', 2633, 4, '', NULL, 1, 0, 'F', '0', '0', 'system:dept:list', '#', 'admin', sysdate(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2636, 'Delete Department Attendance Regulations', 2633, 3, '', NULL, 1, 0, 'F', '0', '0', 'attendance:rule:remove', '#', 'admin', sysdate(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2637, 'Update Department Attendance Regulations', 2633, 2, '', NULL, 1, 0, 'F', '0', '0', 'attendance:rule:edit', '#', 'admin', sysdate(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2638, 'Add Department Attendance Regulations', 2633, 1, '', NULL, 1, 0, 'F', '0', '0', 'attendance:rule:add', '#', 'admin', sysdate(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2640, 'Attendance Record Get Department ', 2639, 2, '', NULL, 1, 0, 'F', '0', '0', 'system:dept:list', '#', 'admin', sysdate(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2641, 'Export Attendance Record', 2639, 1, '', NULL, 1, 0, 'F', '0', '0', 'attendance:rpt:export', '#', 'admin', sysdate(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2643, 'Personal Attendance Rate Get Department ', 2642, 2, '', NULL, 1, 0, 'F', '0', '0', 'system:dept:list', '#', 'admin', sysdate(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2644, 'Export Personal Attendance Rate', 2642, 1, '', NULL, 1, 0, 'F', '0', '0', 'attendance:rpt:export', '#', 'admin', sysdate(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2646, 'Department Attendance Rate Get Department ', 2645, 2, '', NULL, 1, 0, 'F', '0', '0', 'system:dept:list', '#', 'admin', sysdate(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2647, 'Export Department Attendance Rate', 2645, 1, '', NULL, 1, 0, 'F', '0', '0', 'attendance:rpt:export', '#', 'admin', sysdate(), '', NULL, '', 'SIDE');





-- Insert Cron Job
INSERT INTO sys_job (job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, update_by, update_time, remark)
VALUES ('20', 'Handle Attendance(with designated date)', 'SYSTEM', 'autoCreateAtdRecordTask.convertBioLogToAtdRecord(\'2021-10-15\')', '0 0 6 * * ?', '1', '1', '1', 'admin', sysdate(), 'admin', sysdate(), '');
INSERT INTO sys_job (job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, update_by, update_time, remark) 
VALUES ('21', 'Handle Attendance', 'SYSTEM', 'autoCreateAtdRecordTask.convertBioLogToAtdRecord()', '0 0 6 * * ?', '1', '1', '0', 'admin', sysdate(), 'admin', sysdate(), '');
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
  id varchar(48) NOT NULL COMMENT 'id primary key',
  dept_id bigint(20) DEFAULT NULL COMMENT 'Department ID',
  rule_id varchar(48) DEFAULT NULL COMMENT ' Regulations id',
  create_by varchar(50) DEFAULT NULL COMMENT 'creator',
  update_by varchar(50) DEFAULT NULL COMMENT 'updater',
  create_time datetime DEFAULT NULL COMMENT 'create time',
  update_time datetime DEFAULT NULL COMMENT 'update time',
  tenant_id varchar(255) DEFAULT 'super' COMMENT 'tenant ID',
  PRIMARY KEY (id) USING BTREE,
  UNIQUE KEY idx_dept_rule (dept_id,rule_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Department Attendance Regulations Table';

-- ----------------------------
-- Table structure for atd_person_white
-- ----------------------------
DROP TABLE IF EXISTS atd_person_white;
CREATE TABLE atd_person_white (
  id varchar(48) NOT NULL,
  psn_id varchar(48) NOT NULL COMMENT 'Personnel id',
  psn_unique_id varchar(48) NOT NULL COMMENT 'Personnel id',
  create_time datetime DEFAULT NULL COMMENT 'create time',
  tenant_id varchar(255) DEFAULT 'super' COMMENT 'tenant ID',
  PRIMARY KEY (id) USING BTREE,
  UNIQUE KEY psn_id (psn_id),
  KEY idx_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Record White List Personnel Table';

-- ----------------------------
-- Table structure for atd_record
-- ----------------------------
DROP TABLE IF EXISTS atd_record;
CREATE TABLE atd_record (
  id varchar(48) NOT NULL COMMENT 'primary key id',
  atd_date varchar(10) DEFAULT NULL COMMENT 'Attendance Date',
  psn_id varchar(48) NOT NULL COMMENT 'Personnel id',
  psn_unique_id varchar(48) NOT NULL COMMENT 'Personnel id',
  psn_name varchar(100) DEFAULT NULL COMMENT 'Name',
  dept_id bigint(20) DEFAULT NULL COMMENT 'Department id',
  dept_name varchar(100) DEFAULT NULL COMMENT 'Department Name',
  psn_no varchar(100) DEFAULT NULL COMMENT 'Personnel number',
  psn_type varchar(10) DEFAULT NULL COMMENT 'Personnel Type',
  psn_type_name varchar(100) DEFAULT NULL COMMENT 'Personnel Type Name',
  detail_times_id varchar(32) DEFAULT NULL COMMENT 'Time Slot id',
  times_name varchar(100) DEFAULT NULL COMMENT 'Corresponding Time Interval Name',
  sign_in varchar(20) DEFAULT NULL COMMENT 'Start Work Time',
  sign_out varchar(20) DEFAULT NULL COMMENT 'Knock Off Time',
  sign_in_time varchar(8) DEFAULT NULL COMMENT 'Sign In Time',
  sign_out_time varchar(8) DEFAULT NULL COMMENT 'Sign Out Time',
  clock_mark varchar(1) DEFAULT 'F' COMMENT 'Clock In Status',
  clock_mark_name varchar(100) DEFAULT NULL COMMENT 'Clock In Status Name',
  create_by varchar(50) DEFAULT NULL COMMENT 'creator',
  update_by varchar(50) DEFAULT NULL COMMENT 'updater',
  create_time datetime DEFAULT NULL COMMENT 'create time',
  update_time datetime DEFAULT NULL COMMENT 'update time',
  tenant_id varchar(255) DEFAULT 'super' COMMENT 'tenant ID',
  status char(1) DEFAULT NULL COMMENT 'Record Status',
  PRIMARY KEY (id) USING BTREE,
  UNIQUE KEY idx_u_id (atd_date,detail_times_id,psn_unique_id,tenant_id) USING BTREE,
  KEY idx_a_r_psnid (psn_id),
  KEY idx_a_r_timeid (detail_times_id),
  KEY idx_tenant_id (tenant_id),
  KEY idx_a_r_date (atd_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Attendance Record Table';

-- ----------------------------
-- Table structure for atd_record_mark_rpt
-- ----------------------------
DROP TABLE IF EXISTS atd_record_mark_rpt;
CREATE TABLE atd_record_mark_rpt (
  id varchar(48) NOT NULL COMMENT 'id',
  atd_date varchar(10) DEFAULT NULL COMMENT 'Attendance Date',
  psn_id varchar(48) NOT NULL COMMENT 'Personnel id',
  psn_unique_id varchar(48) NOT NULL COMMENT 'Personnel id',
  psn_name varchar(100) DEFAULT NULL COMMENT 'Name',
  dept_id bigint(20) DEFAULT NULL COMMENT 'Department id',
  dept_name varchar(100) DEFAULT NULL COMMENT 'Department Name',
  psn_no varchar(100) DEFAULT NULL COMMENT 'Personnel number',
  psn_type varchar(10) DEFAULT NULL COMMENT 'Personnel Type',
  psn_type_name varchar(100) DEFAULT NULL COMMENT 'Personnel Type Name',
  detail_times_id varchar(32) DEFAULT NULL COMMENT 'Time Slot id',
  times_name varchar(100) DEFAULT NULL COMMENT 'Corresponding Time Interval Name',
  clock_mark0 varchar(1) DEFAULT NULL COMMENT 'Clock In Status0',
  clock_mark1 varchar(1) DEFAULT NULL COMMENT 'Clock In Status1',
  clock_mark2 varchar(1) DEFAULT NULL COMMENT 'Clock In Status2',
  clock_mark3 varchar(1) DEFAULT NULL COMMENT 'Clock In Status3',
  clock_mark4 varchar(1) DEFAULT NULL COMMENT 'Clock In Status4',
  clock_mark5 varchar(1) DEFAULT NULL COMMENT 'Clock In Status5',
  create_by varchar(50) DEFAULT NULL COMMENT 'creator',
  update_by varchar(50) DEFAULT NULL COMMENT 'updater',
  create_time datetime DEFAULT NULL COMMENT 'create time',
  update_time datetime DEFAULT NULL COMMENT 'update time',
  status varchar(1) DEFAULT NULL COMMENT 'Attendance Status',
  attendance varchar(1) DEFAULT NULL COMMENT 'Attendance situation',
  tenant_id varchar(255) DEFAULT 'super' COMMENT 'tenant ID',
  PRIMARY KEY (id) USING BTREE,
  UNIQUE KEY idx_rr_uid (atd_date,psn_unique_id,tenant_id) USING BTREE,
  KEY idx_a_rr_psnid (psn_id),
  KEY idx_a_rr_timeid (detail_times_id),
  KEY idx_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Personal Attendance Record mark table';

-- ----------------------------
-- Table structure for atd_rule_detail
-- ----------------------------
DROP TABLE IF EXISTS atd_rule_detail;
CREATE TABLE atd_rule_detail (
  id varchar(48) NOT NULL COMMENT 'primary key id',
  rule_id varchar(48) NOT NULL COMMENT ' Regulations id',
  week varchar(20) DEFAULT NULL COMMENT 'week mark',
  active_flag varchar(1) DEFAULT NULL COMMENT 'open',
  create_by varchar(50) DEFAULT NULL COMMENT 'creator',
  update_by varchar(50) DEFAULT NULL COMMENT 'updater',
  create_time datetime DEFAULT NULL COMMENT 'create time',
  update_time datetime DEFAULT NULL COMMENT 'update time',
  tenant_id varchar(255) DEFAULT 'super' COMMENT 'tenant ID',
  PRIMARY KEY (id) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Corresponding details of each Attendance Regulation';

-- ----------------------------
-- Table structure for atd_rule_detail_time
-- ----------------------------
DROP TABLE IF EXISTS atd_rule_detail_time;
CREATE TABLE atd_rule_detail_time (
  id varchar(48) NOT NULL COMMENT 'id',
  detail_id varchar(48) DEFAULT NULL COMMENT 'Detail id',
  times_name varchar(100) DEFAULT NULL COMMENT 'Time Interval Name',
  sign_in varchar(20) DEFAULT NULL COMMENT 'Start Work Time',
  sign_out varchar(20) DEFAULT NULL COMMENT 'Knock Off Time',
  late_num int(11) DEFAULT NULL COMMENT 'mark as being late time(minute)',
  leave_num int(11) DEFAULT NULL COMMENT 'mark as leaving early time(minute)',
  sign_in_begin varchar(20) DEFAULT NULL COMMENT 'Start to Sign In Time',
  sign_in_end varchar(20) DEFAULT NULL COMMENT 'Stop Signing In Time',
  sign_out_begin varchar(20) DEFAULT NULL COMMENT 'Start to Sign Out Time',
  sign_out_end varchar(20) DEFAULT NULL COMMENT 'Stop Signing Out Time',
  sign_in_flag varchar(1) DEFAULT NULL COMMENT 'Must Sign In',
  sign_out_flag varchar(1) DEFAULT NULL,
  create_by varchar(50) DEFAULT NULL COMMENT 'creator',
  update_by varchar(50) DEFAULT NULL COMMENT 'updater',
  create_time datetime DEFAULT NULL COMMENT 'create time',
  update_time datetime DEFAULT NULL COMMENT 'update time',
  clock_begin varchar(20) DEFAULT NULL COMMENT 'Clock Inb Start Time',
  clock_end varchar(20) DEFAULT NULL COMMENT 'Clock In End Time',
  clock_ref varchar(10) DEFAULT NULL COMMENT 'Reference Regulations',
  tenant_id varchar(255) DEFAULT 'super' COMMENT 'tenant ID',
  PRIMARY KEY (id) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Corresponding Detailed Time Slot Information of Each Attendance Regulation';

-- ----------------------------
-- Table structure for atd_rules
-- ----------------------------
DROP TABLE IF EXISTS atd_rules;
CREATE TABLE atd_rules (
  id varchar(48) NOT NULL COMMENT 'primary key id',
  rule_name varchar(100) DEFAULT NULL COMMENT 'Regulations Name',
  smart_mode varchar(1) DEFAULT NULL COMMENT 'Smart Mode',
  create_by varchar(50) DEFAULT NULL COMMENT 'creator',
  update_by varchar(50) DEFAULT NULL COMMENT 'updater',
  create_time datetime DEFAULT NULL COMMENT 'create time',
  update_time datetime DEFAULT NULL COMMENT 'update time',
  tenant_id varchar(255) DEFAULT 'super' COMMENT 'tenant ID',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Attendance Regulations Table';

-- ----------------------------
-- Table structure for atd_times
-- ----------------------------
DROP TABLE IF EXISTS atd_times;
CREATE TABLE atd_times (
  id varchar(48) NOT NULL COMMENT 'Vacation Management id',
  times_name varchar(100) DEFAULT NULL COMMENT 'Time Interval Name',
  sign_in varchar(20) DEFAULT NULL COMMENT 'Start Work Time',
  sign_out varchar(20) DEFAULT NULL COMMENT 'Knock Off Time',
  late_num int(11) DEFAULT NULL COMMENT 'mark as being late time(minute)',
  leave_num int(11) DEFAULT NULL COMMENT 'mark as leaving early time(minute)',
  sign_in_begin varchar(20) DEFAULT NULL COMMENT 'Start to Sign In Time',
  sign_in_end varchar(20) DEFAULT NULL COMMENT 'Stop Signing In Time',
  sign_out_begin varchar(20) DEFAULT NULL COMMENT 'Start to Sign Out Time',
  sign_out_end varchar(20) DEFAULT NULL COMMENT 'Stop Signing Out Time',
  sign_in_flag varchar(1) DEFAULT NULL COMMENT 'Must Sign In',
  sign_out_flag varchar(1) DEFAULT NULL COMMENT 'Must Sign Out',
  create_by varchar(50) DEFAULT NULL COMMENT 'creator',
  update_by varchar(50) DEFAULT NULL COMMENT 'updater',
  create_time datetime DEFAULT NULL COMMENT 'create time',
  update_time datetime DEFAULT NULL COMMENT 'update time',
  clock_begin varchar(20) DEFAULT NULL COMMENT 'Clock In Start Time',
  clock_end varchar(20) DEFAULT NULL COMMENT 'Clock In End Time',
  clock_ref varchar(10) DEFAULT NULL COMMENT 'Reference Regulations',
  tenant_id varchar(255) DEFAULT 'super' COMMENT 'tenant ID',
  PRIMARY KEY (id) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Attendance Time Slot Table';

-- ----------------------------
-- Table structure for atd_vacation
-- ----------------------------
DROP TABLE IF EXISTS atd_vacation;
CREATE TABLE atd_vacation (
  id varchar(48) NOT NULL COMMENT 'Vacation Management id',
  vacation_name varchar(100) DEFAULT NULL COMMENT 'Vacation Name',
  vacation_type varchar(10) DEFAULT NULL COMMENT 'Vacation Type',
  begin_date datetime DEFAULT NULL COMMENT 'Start Date',
  end_date datetime DEFAULT NULL COMMENT 'End Date',
  create_time datetime DEFAULT NULL COMMENT 'create time',
  tenant_id varchar(255) DEFAULT 'super' COMMENT 'tenant ID',
  PRIMARY KEY (id) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Vacation Management Time';

ALTER TABLE device_info ADD COLUMN device_direction varchar(10) COMMENT 'device direction(IN:in, OUT: out, UNKNOWN: unknown)';
ALTER TABLE base_person_info ADD COLUMN ext_attrs varchar(500) COMMENT 'extension attribute JSON';

-- ----------------------------
-- Update Personnel Basic Information Table Structure
-- ----------------------------

ALTER TABLE base_person_info ADD COLUMN person_type varchar(30) DEFAULT 'user' COMMENT 'Personnel Type: user-Personnel   visitor-visitor(for visitor system)';
ALTER TABLE base_person_info ADD COLUMN inviter_id varchar(48) NULL DEFAULT NULL COMMENT 'interviewee id(for visitor system)';
ALTER TABLE base_person_info ADD COLUMN visitor_status  char(1) NULL DEFAULT '0' COMMENT 'visitor Status: 0-turn on 1-forbidden(for visitor system)';
ALTER TABLE base_person_info ADD COLUMN effective_begin_time datetime(0) NULL DEFAULT NULL COMMENT 'Effective Start Time(for visitor system)';
ALTER TABLE base_person_info ADD COLUMN effective_end_time datetime(0) NULL DEFAULT NULL COMMENT 'Effective End Time(for visitor system)';

-- ----------------------------
-- Update Face Basic Information Table Structure
-- ----------------------------
ALTER TABLE base_person_face ADD COLUMN face_image_md5 VARCHAR(255) NULL DEFAULT NULL COMMENT 'Face base64 MD5';

-- ----------------------------
-- Add Visitor Effective Status into Dictionary
-- ----------------------------
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (148, 'Visitor Effective Status', 'visitor_effective_status', '0', 'admin',sysdate(), '', NULL, 'visitor Effective Status');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (462, 0, 'not Effective ', '0', 'visitor_effective_status', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, NULL);
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (463, 1, 'Effective', '1', 'visitor_effective_status', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, NULL);
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (464, 2, 'invalid', '2', 'visitor_effective_status', NULL, NULL, 'N', '0', 'admin', sysdate(), '', NULL, NULL);


-- ----------------------------
--  Menu Visitor Management in Basic Information
-- ----------------------------

INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2648, 'Visitor Management', 2012, 7, 'visitor', 'basedata/visitor', 1, 0, 'C', '0', '0', '', '#', 'admin', sysdate(), '', NULL, '', 'SIDE');

-- ----------------------------
--  Menu Visitor Management Basic Information-the Second Class Menu
-- ----------------------------
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2649, 'Visitor Information', 2648, 1, 'visitorinfo', 'basedata/visitor/index', 1, 0, 'C', '0', '0', 'basedata:visitor:list', '#', 'admin', sysdate(), '', NULL, '', 'TAB');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2650, 'Visitor Face', 2648, 2, 'visitorface', 'basedata/visitor/visitorface', 1, 0, 'C', '0', '0', 'basedata:visitorFace:list', '#', 'admin', sysdate(), '', NULL, '', 'TAB');

-- ----------------------------
-- Button Visitor Management in Basic Information-the Second Class Menu-Internal Button
-- ----------------------------
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2651, 'Check Visitor Detail', 2649, 1, '', NULL, 1, 0, 'F', '0', '0', 'basedata:visitor:query', '#', 'admin', sysdate(), '',NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2652, 'Delete Visitor', 2649, 2, '', NULL, 1, 0, 'F', '0', '0', 'basedata:visitor:remove', '#', 'admin', sysdate(), '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2653, 'Visitor Face Detail', 2650, 1, '', NULL, 1, 0, 'F', '0', '0', 'basedata:visitorFace:query', '#', 'admin', sysdate(), '', NULL, '', 'SIDE');

ALTER TABLE person_face_search_log MODIFY COLUMN scene_image varchar(255) default NULL COMMENT 'Scene Image Path';
ALTER TABLE person_faceiris_search_log ADD COLUMN valid_type varchar(2) default '02' COMMENT 'verification mode(01:ID card verification, 02: Biometric verification(Face, Iris)，03: Health Code verification 04: Card Swiping verification)';
ALTER TABLE person_face_search_log ADD COLUMN card_no varchar(48) COMMENT 'card number';
ALTER TABLE person_faceiris_search_log ADD COLUMN card_no varchar(48) COMMENT 'card number';


-- ----------------------------
--  System Settings-Add info used in visitor module into Parameter Settings-company name visited
-- ----------------------------
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (174, 'Company Name visited by visitor', 'visitor.visit.company.name', '-', 'Y', 'admin', sysdate(), '', NULL, NULL, 'Y');

INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (465, 49, 'Iris and Face Multi-modal Identification(1:1)', 'MULIT_IRIS_FACE_VERIFY', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate(), 'admin', sysdate(), 'HTTP Transaction Interface--Iris and Face Multi-modal Identification(1:1)');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (177, 'Multi-modal1v1 Log Image Save Path', 'busi.mulit.compare.dir', './eyecool/busi/mulit/compare/', 'N', 'admin', sysdate(), 'admin', sysdate(), NULL, 'N');
UPDATE sys_config SET config_name = 'Multi-modal 1vN Log Image Save Path', config_value = './eyecool/busi/mulit/search/' WHERE config_key = 'busi.mulit.search.dir';

-- Iris and Face Multi-modal 1v1 Log Table
CREATE TABLE person_faceiris_match_log (
  id varchar(48) NOT NULL COMMENT 'primary key',
  received_seq varchar(48) NOT NULL COMMENT 'Business Serial Number',
  healthcode_log_id varchar(48) DEFAULT NULL COMMENT 'Health Code Log ID',
  unique_id varchar(50) DEFAULT NULL COMMENT 'Personnel Unique number',
  dept_id varchar(50) DEFAULT NULL COMMENT 'Department ID',
  dept_name varchar(255) DEFAULT NULL COMMENT 'Department Name',
  person_name varchar(255) DEFAULT NULL COMMENT 'Personnel Name',
  channel_code varchar(255) DEFAULT NULL COMMENT 'Channel Code',
  scene_face_image varchar(255) DEFAULT NULL COMMENT ' Face Scene Image Path',
  scene_iris_image varchar(255) DEFAULT NULL COMMENT 'Iris Scene Image Path',
  scene_face_score double DEFAULT NULL COMMENT ' Face Comparison Score',
  scene_iris_score double DEFAULT NULL COMMENT 'Iris Comparison Score',
  stock_face_image varchar(255) DEFAULT NULL COMMENT 'Database Face Image Path',
  stock_iris_image varchar(255) DEFAULT NULL COMMENT 'Database Iris Image Path',
  match_mode char(1) DEFAULT NULL COMMENT 'Comparison Mode Dictionary matchMode',
  match_score double DEFAULT NULL COMMENT 'Comparison Score, Score or Fusion Score to pass Comparison',
  checklive_score double DEFAULT NULL COMMENT 'Checklive Score',
  checklive_result varchar(1) DEFAULT NULL COMMENT 'Checklive Result(0 pass, 1 fail)',
  result char(1) DEFAULT NULL COMMENT ' Result(0 pass, 1 fail)',
  received_time datetime NOT NULL COMMENT ' Transaction Time',
  time_used bigint(20) DEFAULT NULL COMMENT 'Comparison time ms',
  face_time_used bigint(20) DEFAULT NULL COMMENT ' Face Comparison Time ms',
  iris_time_used bigint(20) DEFAULT NULL COMMENT 'Iris Comparison Time ms',
  temperature double DEFAULT NULL COMMENT 'Temperature',
  temperature_floor double DEFAULT NULL COMMENT 'Temperature Threshold Lower Limit',
  temperature_top double DEFAULT NULL COMMENT 'Temperature Threshold Upper Limit',
  device_sn varchar(50) DEFAULT NULL COMMENT 'device sn',
  device_model varchar(255) DEFAULT NULL COMMENT 'device Model Code',
  device_name varchar(255) DEFAULT NULL COMMENT 'device Name',
  device_ip varchar(255) DEFAULT NULL COMMENT 'device IP',
  device_longitude double DEFAULT NULL COMMENT 'device Longitude(East Longitude)',
  device_dimension double DEFAULT NULL COMMENT 'device Latitude(North Latitude)',
  device_direction varchar(10) DEFAULT 'UNKNOWN' COMMENT 'device direction(IN:in, OUT:out, UNKNOWN: unknown)',
  server_id varchar(255) DEFAULT NULL COMMENT 'Server id',
  create_time datetime DEFAULT NULL COMMENT 'create time',
  batch_date datetime DEFAULT NULL COMMENT 'Cron Job Execution Time',
  remark varchar(255) DEFAULT NULL COMMENT 'Remark',
  tenant_id varchar(255) DEFAULT 'super' COMMENT 'tenant ID',
  temperature_result varchar(1) DEFAULT NULL COMMENT 'Temperature Measurement Result(0: not deployed, 1: normal temperature,2: high temperature alarm, 3: low temperature alarm, 4: temperature is not measured(not in measurement area, temperature is not confirmed))',
  PRIMARY KEY (id,received_time)
) PARTITION BY RANGE (TO_DAYS(received_time)) (
   PARTITION p0 VALUES LESS THAN (0)
) ;
ALTER TABLE person_faceiris_match_log COMMENT='Face and Iris Multi-modal Comparison Log Table';
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

INSERT INTO sys_job(job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, update_by, update_time, remark) VALUES (24, 'Maintain Face and Iris Multi-modal 1v1 Log by Partition', 'DEFAULT', 'clearBioMatchLogTask.clearMulitMatchLog(365,false,\'eyecool_assps\')', '0 0 1 * * ? *', '1', '1', '0', 'admin', '2021-12-09 13:48:58', '', '2021-12-09 13:49:03', '');
INSERT INTO qrtz_job_details(sched_name, job_name, job_group, description, job_class_name, is_durable, is_nonconcurrent, is_update_data, requests_recovery, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME24', 'DEFAULT', NULL, 'cn.eyecool.quartz.util.QuartzDisallowConcurrentExecution', '0', '1', '0', '0', 0xACED0005737200156F72672E71756172747A2E4A6F62446174614D61709FB083E8BFA9B0CB020000787200266F72672E71756172747A2E7574696C732E537472696E674B65794469727479466C61674D61708208E8C3FBC55D280200015A0013616C6C6F77735472616E7369656E74446174617872001D6F72672E71756172747A2E7574696C732E4469727479466C61674D617013E62EAD28760ACE0200025A000564697274794C00036D617074000F4C6A6176612F7574696C2F4D61703B787001737200116A6176612E7574696C2E486173684D61700507DAC1C31660D103000246000A6C6F6164466163746F724900097468726573686F6C6478703F4000000000000C7708000000100000000174000F5441534B5F50524F504552544945537372001F636E2E657965636F6F6C2E71756172747A2E646F6D61696E2E5379734A6F6200000000000000010200084C000A636F6E63757272656E747400124C6A6176612F6C616E672F537472696E673B4C000E63726F6E45787072657373696F6E71007E00094C000C696E766F6B6554617267657471007E00094C00086A6F6247726F757071007E00094C00056A6F6249647400104C6A6176612F6C616E672F4C6F6E673B4C00076A6F624E616D6571007E00094C000D6D697366697265506F6C69637971007E00094C000673746174757371007E000978720028636E2E657965636F6F6C2E636F6D6D6F6E2E636F72652E646F6D61696E2E42617365456E7469747900000000000000010200094C0009626567696E54696D6571007E00094C0008637265617465427971007E00094C000A63726561746554696D657400104C6A6176612F7574696C2F446174653B4C0007656E6454696D6571007E00094C0006706172616D7371007E00034C000672656D61726B71007E00094C000B73656172636856616C756571007E00094C0008757064617465427971007E00094C000A75706461746554696D6571007E000C78707074000561646D696E707070707070707400013174000D3020302031202A202A203F202A740042636C65617242696F4D617463684C6F675461736B2E636C6561724D756C69744D617463684C6F67283336352C66616C73652C27657965636F6F6C5F6173737073272974000744454641554C547372000E6A6176612E6C616E672E4C6F6E673B8BE490CC8F23DF0200014A000576616C7565787200106A6176612E6C616E672E4E756D62657286AC951D0B94E08B0200007870000000000000001874002AE4BABAE884B8E899B9E8869CE5A49AE6A8A1E68081317631E697A5E5BF97E58886E58CBAE7BBB4E68AA474000131740001317800);
INSERT INTO qrtz_triggers(sched_name, trigger_name, trigger_group, job_name, job_group, description, next_fire_time, prev_fire_time, priority, trigger_state, trigger_type, start_time, end_time, calendar_name, misfire_instr, job_data) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME24', 'DEFAULT', 'TASK_CLASS_NAME24', 'DEFAULT', NULL, 1639069200000, -1, 5, 'WAITING', 'CRON', 1639028938000, 0, NULL, -1, '');
INSERT INTO qrtz_cron_triggers(sched_name, trigger_name, trigger_group, cron_expression, time_zone_id) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME24', 'DEFAULT', '0 0 1 * * ? *', 'Asia/Shanghai');

-- Iris and Face 1v1 Log Display Menu
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2654, ' Face and Iris Comparison', 2406, 8, 'faceirisMatch', 'tradelog/faceirisMatch/index', 1, 0, 'C', '0', '0', 'tradelog:faceirisMatch:list', '#', 'admin', sysdate(), 'admin', sysdate(), ' Face and Iris Multi-modal Comparison Log Menu', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2655, ' Face and Iris Comparison Log Query', 2654, 1, '#', '', 1, 0, 'F', '0', '0', 'tradelog:faceirisMatch:query', '#', 'admin', sysdate(), 'admin', sysdate(), '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2656, ' Face and Iris Comparison Log Export', 2654, 5, '#', '', 1, 0, 'F', '0', '0', 'tradelog:faceirisMatch:export', '#', 'admin', sysdate(), 'admin', sysdate(), '', 'SIDE');