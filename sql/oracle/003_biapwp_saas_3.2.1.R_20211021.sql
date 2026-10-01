ALTER TABLE person_face_match_log ADD temperature_result varchar2(1);
COMMENT ON COLUMN person_face_match_log.temperature_result IS '测温结果(0:未布控,1:体温正常,2:高体温报警,3:低体温报警,4:未测到温度(不在测温区域,体温未确认完成))';
ALTER TABLE person_face_search_log ADD temperature_result varchar2(1);
COMMENT ON COLUMN person_face_search_log.temperature_result IS '测温结果(0:未布控,1:体温正常,2:高体温报警,3:低体温报警,4:未测到温度(不在测温区域,体温未确认完成))';
ALTER TABLE person_faceiris_search_log ADD temperature_result varchar2(1);
COMMENT ON COLUMN person_faceiris_search_log.temperature_result IS '测温结果(0:未布控,1:体温正常,2:高体温报警,3:低体温报警,4:未测到温度(不在测温区域,体温未确认完成))';
ALTER TABLE person_face_search_log ADD valid_type varchar2(2) DEFAULT '02';
COMMENT ON COLUMN person_face_search_log.valid_type IS '核验方式(01:身份证核验，02：刷脸核验，03：健康码核验，04：刷卡核验)';


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
	AND log.received_time >= To_date(To_char(Trunc(SYSDATE), 'yyyy/mm/dd hh24:mi:ss'), 'yyyy/mm/dd hh24:mi:ss') UNION ALL
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
	AND log.received_time >= To_date(To_char(Trunc(SYSDATE), 'yyyy/mm/dd hh24:mi:ss'), 'yyyy/mm/dd hh24:mi:ss');
	
	
-- 多时段考勤主菜单
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2615, '多时段考勤', 0, 12, 'attendance', NULL, 1, 0, 'M', '0', '0', '', 'table', 'admin',sysdate, '', NULL, '', 'SIDE');

-- 多时段考勤二级菜单
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2616, '假期管理', 2615, 1, 'vacation', 'attendance/vacation/index', 1, 0, 'C', '0', '0', 'attendance:vacation:list', 'build', 'admin',sysdate, '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2620, '白名单管理', 2615, 0, 'white', 'attendance/white/index', 1, 0, 'C', '0', '0', 'attendance:white:list', 'base', 'admin', sysdate, '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2623, '时段管理', 2615, 2, 'times', 'attendance/times/index', 1, 0, 'C', '0', '0', 'attendance:times:list', 'date', 'admin',sysdate, '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2628, '考勤规则', 2615, 3, 'rules', 'attendance/rules/index', 1, 0, 'C', '0', '0', 'attendance:rules:list', 'base', 'admin', sysdate, '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2633, '部门考勤规则', 2615, 4, 'rule', 'attendance/rule/index', 1, 0, 'C', '0', '0', 'attendance:rule:list', 'build', 'admin', sysdate, '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2639, '考勤记录', 2615, 5, 'rpt', 'attendance/rpt/index', 1, 0, 'C', '0', '0', 'attendance:rpt:list', 'base', 'admin', sysdate, '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2642, '个人考勤率', 2615, 6, 'rpteveryone', 'attendance/rpteveryone/index', 1, 0, 'C', '0', '0', 'attendance:rpt:list', 'base', 'admin', sysdate, '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2645, '部门考勤率', 2615, 7, 'rptdept', 'attendance/rptdept/index', 1, 0, 'C', '0', '0', 'attendance:rpt:list', 'base', 'admin', sysdate, '', NULL, '', 'SIDE');

 -- 多时段考勤二级菜单按钮
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2617, '假期管理删除', 2616, 3, '', NULL, 1, 0, 'F', '0', '0', 'attendance:vacation:remove', '#', 'admin',sysdate, '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2618, '假期管理修改', 2616, 2, '', NULL, 1, 0, 'F', '0', '0', 'attendance:vacation:edit', '#', 'admin',sysdate, '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2619, '假期管理新增', 2616, 1, '', NULL, 1, 0, 'F', '0', '0', 'attendance:vacation:add', '#', 'admin', sysdate, '',NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2621, '非白名单列表', 2620, 1, '', NULL, 1, 0, 'F', '0', '0', 'attendance:white:import', '#', 'admin', sysdate, '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2622, '考勤白名单删除', 2620, 2, '', NULL, 1, 0, 'F', '0', '0', 'attendance:white:remove', '#', 'admin', sysdate, '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2624, '时段管理导出', 2623, 4, '', NULL, 1, 0, 'F', '0', '0', 'attendance:times:export', '#', 'admin', sysdate, '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2625, '时段管理删除', 2623, 3, '', NULL, 1, 0, 'F', '0', '0', 'attendance:times:remove', '#', 'admin',sysdate, '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2626, '时段管理修改', 2623, 2, '', NULL, 1, 0, 'F', '0', '0', 'attendance:times:edit', '#', 'admin', sysdate, '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2627, '时段管理新增', 2623, 1, '', NULL, 1, 0, 'F', '0', '0', 'attendance:times:add', '#', 'admin', sysdate, '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2629, '考勤规则获取所有时间段', 2628, 4, '', NULL, 1, 0, 'F', '0', '0', 'attendance:times:query', '#', 'admin', sysdate, '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2630, '考勤规则删除', 2628, 3, '', NULL, 1, 0, 'F', '0', '0', 'attendance:rules:remove', '#', 'admin', sysdate, '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2631, '考勤规则修改', 2628, 2, '', NULL, 1, 0, 'F', '0', '0', 'attendance:rules:edit', '#', 'admin', sysdate, '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2632, '考勤规则新增', 2628, 1, '', NULL, 1, 0, 'F', '0', '0', 'attendance:rules:add', '#', 'admin', sysdate, '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2634, '获取所有部门考勤规则', 2633, 5, '', NULL, 1, 0, 'F', '0', '0', 'attendance:rule:query', '#', 'admin', sysdate, '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2635, '部门考勤规则获取部门', 2633, 4, '', NULL, 1, 0, 'F', '0', '0', 'system:dept:list', '#', 'admin', sysdate, '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2636, '部门考勤规则删除', 2633, 3, '', NULL, 1, 0, 'F', '0', '0', 'attendance:rule:remove', '#', 'admin', sysdate, '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2637, '部门考勤规则修改', 2633, 2, '', NULL, 1, 0, 'F', '0', '0', 'attendance:rule:edit', '#', 'admin', sysdate, '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2638, '部门考勤规则新增', 2633, 1, '', NULL, 1, 0, 'F', '0', '0', 'attendance:rule:add', '#', 'admin', sysdate, '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2640, '考勤记录获取部门', 2639, 2, '', NULL, 1, 0, 'F', '0', '0', 'system:dept:list', '#', 'admin', sysdate, '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2641, '考勤记录导出', 2639, 1, '', NULL, 1, 0, 'F', '0', '0', 'attendance:rpt:export', '#', 'admin', sysdate, '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2643, '个人考勤率获取部门', 2642, 2, '', NULL, 1, 0, 'F', '0', '0', 'system:dept:list', '#', 'admin', sysdate, '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2644, '个人考勤率导出', 2642, 1, '', NULL, 1, 0, 'F', '0', '0', 'attendance:rpt:export', '#', 'admin', sysdate, '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2646, '部门考勤率获取部门', 2645, 2, '', NULL, 1, 0, 'F', '0', '0', 'system:dept:list', '#', 'admin', sysdate, '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2647, '部门考勤率导出', 2645, 1, '', NULL, 1, 0, 'F', '0', '0', 'attendance:rpt:export', '#', 'admin', sysdate, '', NULL, '', 'SIDE');

-- 定时任务插入
INSERT INTO SYS_JOB(JOB_ID, JOB_NAME, JOB_GROUP, INVOKE_TARGET, CRON_EXPRESSION, MISFIRE_POLICY, CONCURRENT, STATUS, CREATE_BY, CREATE_TIME, UPDATE_BY, UPDATE_TIME, REMARK) VALUES ('40', '处理考勤（带指定日期）', 'DEFAULT', 'autoCreateAtdRecordTask.convertBioLogToAtdRecord(''2021-10-15'')', ' 0 0 6 * * ?', '1', '1', '1', 'admin', TO_DATE('2021-11-02 17:14:43', 'SYYYY-MM-DD HH24:MI:SS'), NULL, NULL, NULL);
INSERT INTO SYS_JOB(JOB_ID, JOB_NAME, JOB_GROUP, INVOKE_TARGET, CRON_EXPRESSION, MISFIRE_POLICY, CONCURRENT, STATUS, CREATE_BY, CREATE_TIME, UPDATE_BY, UPDATE_TIME, REMARK) VALUES ('41', '处理考勤', 'DEFAULT', 'autoCreateAtdRecordTask.convertBioLogToAtdRecord()', '0 0 6 * * ?', '1', '1', '0', 'admin', TO_DATE('2021-11-02 17:15:26', 'SYYYY-MM-DD HH24:MI:SS'), NULL, TO_DATE('2021-11-02 17:16:47', 'SYYYY-MM-DD HH24:MI:SS'), NULL);
INSERT INTO QRTZ_JOB_DETAILS(SCHED_NAME, JOB_NAME, JOB_GROUP, DESCRIPTION, JOB_CLASS_NAME, IS_DURABLE, IS_NONCONCURRENT, IS_UPDATE_DATA, REQUESTS_RECOVERY, JOB_DATA) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME40', 'DEFAULT', NULL, 'cn.eyecool.quartz.util.QuartzDisallowConcurrentExecution', '0', '1', '0', '0', HEXTORAW('ACED0005737200156F72672E71756172747A2E4A6F62446174614D61709FB083E8BFA9B0CB020000787200266F72672E71756172747A2E7574696C732E537472696E674B65794469727479466C61674D61708208E8C3FBC55D280200015A0013616C6C6F77735472616E7369656E74446174617872001D6F72672E71756172747A2E7574696C732E4469727479466C61674D617013E62EAD28760ACE0200025A000564697274794C00036D617074000F4C6A6176612F7574696C2F4D61703B787001737200116A6176612E7574696C2E486173684D61700507DAC1C31660D103000246000A6C6F6164466163746F724900097468726573686F6C6478703F4000000000000C7708000000100000000174000F5441534B5F50524F504552544945537372001F636E2E657965636F6F6C2E71756172747A2E646F6D61696E2E5379734A6F6200000000000000010200084C000A636F6E63757272656E747400124C6A6176612F6C616E672F537472696E673B4C000E63726F6E45787072657373696F6E71007E00094C000C696E766F6B6554617267657471007E00094C00086A6F6247726F757071007E00094C00056A6F6249647400104C6A6176612F6C616E672F4C6F6E673B4C00076A6F624E616D6571007E00094C000D6D697366697265506F6C69637971007E00094C000673746174757371007E000978720028636E2E657965636F6F6C2E636F6D6D6F6E2E636F72652E646F6D61696E2E42617365456E7469747900000000000000010200094C0009626567696E54696D6571007E00094C0008637265617465427971007E00094C000A63726561746554696D657400104C6A6176612F7574696C2F446174653B4C0007656E6454696D6571007E00094C0006706172616D7371007E00034C000672656D61726B71007E00094C000B73656172636856616C756571007E00094C0008757064617465427971007E00094C000A75706461746554696D6571007E000C78707074000561646D696E707070707070707400013174000C203020302036202A202A203F74003E6175746F4372656174654174645265636F72645461736B2E636F6E7665727442696F4C6F67546F4174645265636F72642827323032312D31302D3135272974000744454641554C547372000E6A6176612E6C616E672E4C6F6E673B8BE490CC8F23DF0200014A000576616C7565787200106A6176612E6C616E672E4E756D62657286AC951D0B94E08B02000078700000000000000028740021E5A484E79086E88083E58BA4EFBC88E5B8A6E68C87E5AE9AE697A5E69C9FEFBC8974000131740001317800'));
INSERT INTO QRTZ_JOB_DETAILS(SCHED_NAME, JOB_NAME, JOB_GROUP, DESCRIPTION, JOB_CLASS_NAME, IS_DURABLE, IS_NONCONCURRENT, IS_UPDATE_DATA, REQUESTS_RECOVERY, JOB_DATA) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME41', 'DEFAULT', NULL, 'cn.eyecool.quartz.util.QuartzDisallowConcurrentExecution', '0', '1', '0', '0', HEXTORAW('ACED0005737200156F72672E71756172747A2E4A6F62446174614D61709FB083E8BFA9B0CB020000787200266F72672E71756172747A2E7574696C732E537472696E674B65794469727479466C61674D61708208E8C3FBC55D280200015A0013616C6C6F77735472616E7369656E74446174617872001D6F72672E71756172747A2E7574696C732E4469727479466C61674D617013E62EAD28760ACE0200025A000564697274794C00036D617074000F4C6A6176612F7574696C2F4D61703B787001737200116A6176612E7574696C2E486173684D61700507DAC1C31660D103000246000A6C6F6164466163746F724900097468726573686F6C6478703F4000000000000C7708000000100000000174000F5441534B5F50524F504552544945537372001F636E2E657965636F6F6C2E71756172747A2E646F6D61696E2E5379734A6F6200000000000000010200084C000A636F6E63757272656E747400124C6A6176612F6C616E672F537472696E673B4C000E63726F6E45787072657373696F6E71007E00094C000C696E766F6B6554617267657471007E00094C00086A6F6247726F757071007E00094C00056A6F6249647400104C6A6176612F6C616E672F4C6F6E673B4C00076A6F624E616D6571007E00094C000D6D697366697265506F6C69637971007E00094C000673746174757371007E000978720028636E2E657965636F6F6C2E636F6D6D6F6E2E636F72652E646F6D61696E2E42617365456E7469747900000000000000010200094C0009626567696E54696D6571007E00094C0008637265617465427971007E00094C000A63726561746554696D657400104C6A6176612F7574696C2F446174653B4C0007656E6454696D6571007E00094C0006706172616D7371007E00034C000672656D61726B71007E00094C000B73656172636856616C756571007E00094C0008757064617465427971007E00094C000A75706461746554696D6571007E000C78707074000561646D696E707070707070707400013174000B3020302036202A202A203F7400326175746F4372656174654174645265636F72645461736B2E636F6E7665727442696F4C6F67546F4174645265636F72642829707372000E6A6176612E6C616E672E4C6F6E673B8BE490CC8F23DF0200014A000576616C7565787200106A6176612E6C616E672E4E756D62657286AC951D0B94E08B0200007870000000000000002974000CE5A484E79086E88083E58BA474000131740001317800'));
INSERT INTO QRTZ_TRIGGERS(SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP, JOB_NAME, JOB_GROUP, DESCRIPTION, NEXT_FIRE_TIME, PREV_FIRE_TIME, PRIORITY, TRIGGER_STATE, TRIGGER_TYPE, START_TIME, END_TIME, CALENDAR_NAME, MISFIRE_INSTR, JOB_DATA) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME40', 'DEFAULT', 'TASK_CLASS_NAME40', 'DEFAULT', NULL, '1635890400000', '-1', '5', 'PAUSED', 'CRON', '1635844132000', '0', NULL, '-1', NULL);
INSERT INTO QRTZ_TRIGGERS(SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP, JOB_NAME, JOB_GROUP, DESCRIPTION, NEXT_FIRE_TIME, PREV_FIRE_TIME, PRIORITY, TRIGGER_STATE, TRIGGER_TYPE, START_TIME, END_TIME, CALENDAR_NAME, MISFIRE_INSTR, JOB_DATA) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME41', 'DEFAULT', 'TASK_CLASS_NAME41', 'DEFAULT', NULL, '1635890400000', '-1', '5', 'WAITING', 'CRON', '1635844175000', '0', NULL, '-1', NULL);
INSERT INTO QRTZ_CRON_TRIGGERS(SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP, CRON_EXPRESSION, TIME_ZONE_ID) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME40', 'DEFAULT', ' 0 0 6 * * ?', 'Asia/Shanghai');
INSERT INTO QRTZ_CRON_TRIGGERS(SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP, CRON_EXPRESSION, TIME_ZONE_ID) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME41', 'DEFAULT', '0 0 6 * * ?', 'Asia/Shanghai');


-- ----------------------------
-- 部门考勤规则表
-- ----------------------------
CREATE TABLE atd_dept_rule ( 
	id varchar2(48)   NOT NULL,
	dept_id NUMBER(20,0),
	rule_id varchar2(48),
	create_by varchar2(50),
	update_by varchar2(50),
	create_time DATE,
	update_time DATE,
	tenant_id varchar2(255)  DEFAULT 'super',
  CONSTRAINT pk_atd_dept_rule PRIMARY KEY (id) 
 );
 CREATE UNIQUE INDEX idx_dept_rule ON atd_dept_rule (dept_id, rule_id);
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
	id varchar2(48)   NOT NULL,
	psn_id varchar2(48)   NOT NULL,
	psn_unique_id varchar2(48)   NOT NULL,
	create_time DATE,
	tenant_id varchar2(255)  DEFAULT 'super',
  CONSTRAINT pk_atd_person_white PRIMARY KEY (id) 
 );
 CREATE UNIQUE INDEX apw_psn_id_index ON atd_person_white (psn_id); 
 CREATE INDEX tenant_id_index_apw ON atd_person_white (tenant_id); 
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
	id varchar2(48)   NOT NULL,
	atd_date varchar2(10),
	psn_id varchar2(48)   NOT NULL,
	psn_unique_id varchar2(48)   NOT NULL,
	psn_name varchar2(100),
	dept_id NUMBER(20,0),
	dept_name varchar2(100),
	psn_no varchar2(100),
	psn_type varchar2(10),
	psn_type_name varchar2(100),
	detail_times_id varchar2(32),
	times_name varchar2(100),
	sign_in varchar2(20),
	sign_out varchar2(20),
	sign_in_time varchar2(8),
	sign_out_time varchar2(8),
	clock_mark char(1)  DEFAULT 'F',
	clock_mark_name varchar2(100),
	create_by varchar2(50),
	update_by varchar2(50),
	create_time DATE,
	update_time DATE,
	tenant_id varchar2(255)  DEFAULT 'super',
	status char(1),
  CONSTRAINT pk_atd_record PRIMARY KEY (id) 
 );
CREATE UNIQUE INDEX idx_u_id_ar ON atd_record (atd_date, detail_times_id,psn_unique_id,tenant_id);
CREATE INDEX idx_tenant_id_ar ON atd_record (tenant_id);
CREATE INDEX idx_a_r_psnid_ar ON atd_record (psn_id);
CREATE INDEX idx_a_r_timeid_ar ON atd_record (detail_times_id);
CREATE INDEX idx_a_r_date_ar ON atd_record (atd_date); 
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
	id varchar2(48)   NOT NULL,
	atd_date varchar2(10),
	psn_id varchar2(48)   NOT NULL,
	psn_unique_id varchar2(48)   NOT NULL,
	psn_name varchar2(100),
	dept_id NUMBER(20,0),
	dept_name varchar2(100),
	psn_no varchar2(100),
	psn_type varchar2(10),
	psn_type_name varchar2(100),
	detail_times_id varchar2(32),
	times_name varchar2(100),
	clock_mark0 char(1),
	clock_mark1 char(1),
	clock_mark2 char(1),
	clock_mark3 char(1),
	clock_mark4 char(1),
	clock_mark5 char(1),
	create_by varchar2(50),
	update_by varchar2(50),
	create_time DATE,
	update_time DATE,
	status char(1),
	attendance char(1),
	tenant_id varchar2(255)  DEFAULT 'super',
  CONSTRAINT pk_atd_record_mark_rpt PRIMARY KEY (id) 
 );
 CREATE UNIQUE INDEX idx_atd_date_psn_unique_id ON atd_record_mark_rpt (atd_date,psn_unique_id,tenant_id);
 CREATE INDEX armr_psn_id_index ON atd_record_mark_rpt (psn_id); 
 CREATE INDEX detail_times_id_index ON atd_record_mark_rpt (detail_times_id); 
 CREATE INDEX tenant_id_index_armr ON atd_record_mark_rpt (tenant_id); 
 
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
	id varchar2(48)   NOT NULL,
	rule_id varchar2(48)   NOT NULL,
	week varchar2(20),
	active_flag char(1),
	create_by varchar2(50),
	update_by varchar2(50),
	create_time DATE,
	update_time DATE,
	tenant_id varchar2(255)  DEFAULT 'super',
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
	id varchar2(48)   NOT NULL,
	detail_id varchar2(48),
	times_name varchar2(100),
	sign_in varchar2(20),
	sign_out varchar2(20),
	late_num NUMBER(11,0),
	leave_num NUMBER(11,0),
	sign_in_begin varchar2(20),
	sign_in_end varchar2(20),
	sign_out_begin varchar2(20),
	sign_out_end varchar2(20),
	sign_in_flag char(1),
	sign_out_flag char(1),
	create_by varchar2(50),
	update_by varchar2(50),
	create_time DATE,
	update_time DATE,
	clock_begin varchar2(20),
	clock_end varchar2(20),
	clock_ref varchar2(10),
	tenant_id varchar2(255)  DEFAULT 'super',
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
	id varchar2(48)   NOT NULL,
	rule_name varchar2(100),
	smart_mode char(1),
	create_by varchar2(50),
	update_by varchar2(50),
	create_time DATE,
	update_time DATE,
	tenant_id varchar2(255)  DEFAULT 'super',
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
	id varchar2(48)   NOT NULL,
	times_name varchar2(100),
	sign_in varchar2(20),
	sign_out varchar2(20),
	late_num NUMBER(11,0),
	leave_num NUMBER(11,0),
	sign_in_begin varchar2(20),
	sign_in_end varchar2(20),
	sign_out_begin varchar2(20),
	sign_out_end varchar2(20),
	sign_in_flag char(1),
	sign_out_flag char(1),
	create_by varchar2(50),
	update_by varchar2(50),
	create_time DATE,
	update_time DATE,
	clock_begin varchar2(20),
	clock_end varchar2(20),
	clock_ref varchar2(10),
	tenant_id varchar2(255)  DEFAULT 'super',
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
	id varchar2(48)   NOT NULL,
	vacation_name varchar2(100),
	vacation_type varchar2(10),
	begin_date DATE,
	end_date DATE,
	create_time DATE,
	tenant_id varchar2(255)  DEFAULT 'super',
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


ALTER TABLE device_info ADD device_direction varchar2(10);
COMMENT ON COLUMN device_info.device_direction IS '设备方向(IN:进，OUT:出，UNKNOWN:未知)';
ALTER TABLE base_person_info ADD ext_attrs varchar2(500);
COMMENT ON COLUMN base_person_info.ext_attrs IS '拓展属性JSON';


-- ----------------------------
-- 修改人员基础信息表结构
-- ----------------------------
ALTER TABLE base_person_info ADD  person_type varchar2(30) DEFAULT 'user' ;
COMMENT ON COLUMN base_person_info.person_type IS '人员类型：user-人员  visitor-访客(用于访客系统)';

ALTER TABLE base_person_info ADD  inviter_id varchar2(48) DEFAULT null;
COMMENT ON COLUMN base_person_info.inviter_id IS '被访人id(用于访客系统)';

ALTER TABLE base_person_info ADD  visitor_status char(1) DEFAULT '0';
COMMENT ON COLUMN base_person_info.visitor_status IS '访客状态：0-启用 1-禁用(用于访客系统)';

ALTER TABLE base_person_info ADD  effective_begin_time DATE DEFAULT null;
COMMENT ON COLUMN base_person_info.effective_begin_time IS '生效开始时间(用于访客系统)';

ALTER TABLE base_person_info ADD  effective_end_time DATE DEFAULT null;
COMMENT ON COLUMN base_person_info.effective_end_time IS '生效结束时间(用户访客系统)';

-- ----------------------------
-- 修改人脸基础信息表结构
-- ----------------------------
ALTER TABLE base_person_face ADD  face_image_md5 varchar2(255);
COMMENT ON COLUMN base_person_face.face_image_md5 IS '人脸base64的MD5';

-- ----------------------------
-- 字典中添加一项 访客生效状态
-- ----------------------------
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES (148, '访客生效状态', 'visitor_effective_status', '0', 'admin',sysdate, '', NULL, '访客生效状态');
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (462, 0, '未生效', '0', 'visitor_effective_status', NULL, NULL, 'N', '0', 'admin', sysdate, '', NULL, NULL);
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (463, 1, '生效中', '1', 'visitor_effective_status', NULL, NULL, 'N', '0', 'admin', sysdate, '', NULL, NULL);
INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (464, 2, '已失效', '2', 'visitor_effective_status', NULL, NULL, 'N', '0', 'admin', sysdate, '', NULL, NULL);


-- ----------------------------
-- 菜单 基础信息中的访客管理-主菜单
-- ----------------------------
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2648, '访客管理', 2012, 7, 'visitor', 'basedata/visitor', 1, 0, 'C', '0', '0', '', '#', 'admin', sysdate, '', NULL, '', 'SIDE');

-- ----------------------------
-- 菜单 基础信息中的访客管理-二级菜单
-- ----------------------------
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2649, '访客信息', 2648, 1, 'visitorinfo', 'basedata/visitor/index', 1, 0, 'C', '0', '0', 'basedata:visitor:list', '#', 'admin', sysdate, '', NULL, '', 'TAB');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2650, '访客人脸', 2648, 2, 'visitorface', 'basedata/visitor/visitorface', 1, 0, 'C', '0', '0', 'basedata:visitorFace:list', '#', 'admin', sysdate, '', NULL, '', 'TAB');

-- ----------------------------
-- 按钮 基础信息中的访客管理-二级菜单-内部按钮
-- ----------------------------
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2651, '查看访客详情', 2649, 1, '', NULL, 1, 0, 'F', '0', '0', 'basedata:visitor:query', '#', 'admin', sysdate, '',NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2652, '删除访客', 2649, 2, '', NULL, 1, 0, 'F', '0', '0', 'basedata:visitor:remove', '#', 'admin', sysdate, '', NULL, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2653, '访客人脸详情', 2650, 1, '', NULL, 1, 0, 'F', '0', '0', 'basedata:visitorFace:query', '#', 'admin', sysdate, '', NULL, '', 'SIDE');

ALTER TABLE person_face_search_log MODIFY scene_image varchar2(255) NULL;
ALTER TABLE person_faceiris_search_log ADD valid_type varchar2(2) DEFAULT '02';
COMMENT ON COLUMN person_faceiris_search_log.valid_type IS '核验方式(01:身份证核验，02：刷脸核验，03：健康码核验，04：刷卡核验)';
ALTER TABLE person_face_search_log ADD card_no varchar2(48) DEFAULT '02';
COMMENT ON COLUMN person_face_search_log.card_no IS '卡号';
ALTER TABLE person_faceiris_search_log ADD card_no varchar2(48) DEFAULT '02';
COMMENT ON COLUMN person_faceiris_search_log.card_no IS '卡号';

-- ----------------------------
-- 系统设置-参数设置中添加一个访客模块使用的-访客访问公司名称
-- ----------------------------
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (174, '访客访问公司名称', 'visitor.visit.company.name', '-', 'Y', 'admin', sysdate, '', NULL, NULL, 'Y');

INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (465, 49, '虹膜人脸多模态认证(1:1)', 'MULIT_IRIS_FACE_VERIFY', 'http_interface', NULL, NULL, 'N', '0', 'admin', sysdate, 'admin', sysdate, 'HTTP交易接口--虹膜人脸多模态认证(1:1)');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain) VALUES (177, '多模态1v1日志照片存储路径', 'busi.mulit.compare.dir', './eyecool/busi/mulit/compare/', 'N', 'admin', sysdate, 'admin', sysdate, NULL, 'N');
UPDATE sys_config SET config_name = '多模态1vN日志照片存储路径', config_value = './eyecool/busi/mulit/search/' WHERE config_key = 'busi.mulit.search.dir';

CREATE TABLE person_faceiris_match_log (
  id varchar2(48) NOT NULL,
  received_seq varchar2(48) NOT NULL,
  healthcode_log_id varchar2(48),
  unique_id varchar2(50),
  dept_id varchar2(50),
  dept_name varchar2(255),
  person_name varchar2(255),
  channel_code varchar2(255),
  scene_face_image varchar2(255),
  scene_iris_image varchar2(255),
  scene_face_score BINARY_DOUBLE,
  scene_iris_score BINARY_DOUBLE,
  stock_face_image varchar2(255),
  stock_iris_image varchar2(255),
  match_mode char(1),
  match_score BINARY_DOUBLE,
  checklive_score BINARY_DOUBLE,
  checklive_result varchar2(1),
  result char(1),
  received_time DATE NOT NULL,
  time_used NUMBER(20,0),
  face_time_used NUMBER(20,0),
  iris_time_used NUMBER(20,0),
  temperature BINARY_DOUBLE,
  temperature_floor BINARY_DOUBLE,
  temperature_top BINARY_DOUBLE,
  device_sn varchar2(50),
  device_model varchar2(255),
  device_name varchar2(255),
  device_ip varchar2(255),
  device_longitude BINARY_DOUBLE,
  device_dimension BINARY_DOUBLE,
  device_direction varchar2(10),
  server_id varchar2(255),
  create_time DATE,
  batch_date DATE,
  remark varchar2(255),
  tenant_id varchar2(255) DEFAULT 'super',
  temperature_result varchar2(1),
  CONSTRAINT pk_person_faceiris_match_log PRIMARY KEY (id, received_time)
) PARTITION BY RANGE (received_time)
(partition p_19700101 values less than(to_date('1970-01-02','YYYY-MM-DD')));
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
-- 作用是：允许分区表的分区键是可更新。
-- 当某一行更新时，如果更新的是分区列，并且更新后的列植不属于原来的这个分区，
-- 如果开启了这个选项，就会把这行从这个分区中 delete 掉，并加到更新后所属的分区，此时就会发生 rowid 的改变。
-- 相当于一个隐式的 delete + insert ，但是不会触发 insert/delete 触发器。
ALTER TABLE person_faceiris_match_log enable ROW movement;
CALL sp_create_partition(sysdate - 2, 'person_faceiris_match_log','YYYYMMDD');
CALL sp_create_partition(sysdate - 1, 'person_faceiris_match_log','YYYYMMDD');
CALL sp_create_partition(sysdate, 'person_faceiris_match_log','YYYYMMDD');
CALL sp_create_partition(sysdate + 1, 'person_faceiris_match_log','YYYYMMDD');
CALL sp_create_partition(sysdate + 2, 'person_faceiris_match_log','YYYYMMDD');
CALL sp_create_partition(sysdate + 3, 'person_faceiris_match_log','YYYYMMDD');
CALL sp_create_partition(sysdate + 4, 'person_faceiris_match_log','YYYYMMDD');
CALL sp_create_partition(sysdate + 5, 'person_faceiris_match_log','YYYYMMDD');
CALL sp_create_partition(sysdate + 6, 'person_faceiris_match_log','YYYYMMDD');
CALL sp_create_partition(sysdate + 7, 'person_faceiris_match_log','YYYYMMDD');
CALL sp_create_partition(sysdate + 8, 'person_faceiris_match_log','YYYYMMDD');
CALL sp_create_partition(sysdate + 9, 'person_faceiris_match_log','YYYYMMDD');
CALL sp_create_partition(sysdate + 10, 'person_faceiris_match_log','YYYYMMDD');
CALL sp_create_partition(sysdate + 11, 'person_faceiris_match_log','YYYYMMDD');
CALL sp_create_partition(sysdate + 12, 'person_faceiris_match_log','YYYYMMDD');
CALL sp_create_partition(sysdate + 13, 'person_faceiris_match_log','YYYYMMDD');
CALL sp_create_partition(sysdate + 14, 'person_faceiris_match_log','YYYYMMDD');

INSERT INTO SYS_JOB(JOB_ID, JOB_NAME, JOB_GROUP, INVOKE_TARGET, CRON_EXPRESSION, MISFIRE_POLICY, CONCURRENT, STATUS, CREATE_BY, CREATE_TIME, UPDATE_BY, UPDATE_TIME, REMARK) VALUES ('22', '人脸虹膜多模态1v1日志分区维护', 'DEFAULT', 'clearBioMatchLogTask.clearMulitMatchLog(365,false,''eyecool_assps'')', '0 0 1 * * ? *', '1', '1', '0', 'admin', TO_DATE('2021-12-09 14:43:35', 'SYYYY-MM-DD HH24:MI:SS'), NULL, TO_DATE('2021-12-09 14:44:04', 'SYYYY-MM-DD HH24:MI:SS'), NULL);
INSERT INTO QRTZ_JOB_DETAILS(SCHED_NAME, JOB_NAME, JOB_GROUP, DESCRIPTION, JOB_CLASS_NAME, IS_DURABLE, IS_NONCONCURRENT, IS_UPDATE_DATA, REQUESTS_RECOVERY, JOB_DATA) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME22', 'DEFAULT', NULL, 'cn.eyecool.quartz.util.QuartzDisallowConcurrentExecution', '0', '1', '0', '0', HEXTORAW('ACED0005737200156F72672E71756172747A2E4A6F62446174614D61709FB083E8BFA9B0CB020000787200266F72672E71756172747A2E7574696C732E537472696E674B65794469727479466C61674D61708208E8C3FBC55D280200015A0013616C6C6F77735472616E7369656E74446174617872001D6F72672E71756172747A2E7574696C732E4469727479466C61674D617013E62EAD28760ACE0200025A000564697274794C00036D617074000F4C6A6176612F7574696C2F4D61703B787001737200116A6176612E7574696C2E486173684D61700507DAC1C31660D103000246000A6C6F6164466163746F724900097468726573686F6C6478703F4000000000000C7708000000100000000174000F5441534B5F50524F504552544945537372001F636E2E657965636F6F6C2E71756172747A2E646F6D61696E2E5379734A6F6200000000000000010200084C000A636F6E63757272656E747400124C6A6176612F6C616E672F537472696E673B4C000E63726F6E45787072657373696F6E71007E00094C000C696E766F6B6554617267657471007E00094C00086A6F6247726F757071007E00094C00056A6F6249647400104C6A6176612F6C616E672F4C6F6E673B4C00076A6F624E616D6571007E00094C000D6D697366697265506F6C69637971007E00094C000673746174757371007E000978720028636E2E657965636F6F6C2E636F6D6D6F6E2E636F72652E646F6D61696E2E42617365456E7469747900000000000000010200094C0009626567696E54696D6571007E00094C0008637265617465427971007E00094C000A63726561746554696D657400104C6A6176612F7574696C2F446174653B4C0007656E6454696D6571007E00094C0006706172616D7371007E00034C000672656D61726B71007E00094C000B73656172636856616C756571007E00094C0008757064617465427971007E00094C000A75706461746554696D6571007E000C78707074000561646D696E707070707070707400013174000D3020302031202A202A203F202A740042636C65617242696F4D617463684C6F675461736B2E636C6561724D756C69744D617463684C6F67283336352C66616C73652C27657965636F6F6C5F6173737073272974000744454641554C547372000E6A6176612E6C616E672E4C6F6E673B8BE490CC8F23DF0200014A000576616C7565787200106A6176612E6C616E672E4E756D62657286AC951D0B94E08B0200007870000000000000001674002AE4BABAE884B8E899B9E8869CE5A49AE6A8A1E68081317631E697A5E5BF97E58886E58CBAE7BBB4E68AA474000131740001317800'));
INSERT INTO QRTZ_TRIGGERS(SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP, JOB_NAME, JOB_GROUP, DESCRIPTION, NEXT_FIRE_TIME, PREV_FIRE_TIME, PRIORITY, TRIGGER_STATE, TRIGGER_TYPE, START_TIME, END_TIME, CALENDAR_NAME, MISFIRE_INSTR, JOB_DATA) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME22', 'DEFAULT', 'TASK_CLASS_NAME22', 'DEFAULT', NULL, '1639069200000', '-1', '5', 'WAITING', 'CRON', '1639031853000', '0', NULL, '-1', NULL);
INSERT INTO QRTZ_CRON_TRIGGERS(SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP, CRON_EXPRESSION, TIME_ZONE_ID) VALUES ('EyecoolScheduler', 'TASK_CLASS_NAME22', 'DEFAULT', '0 0 1 * * ? *', 'Asia/Shanghai');
drop sequence S_sys_dict_type;
CREATE sequence S_sys_dict_type INCREMENT BY 1 START WITH 149 nomaxvalue nominvalue cache 20;
drop sequence S_sys_dict_data;
CREATE sequence S_sys_dict_data INCREMENT BY 1 START WITH 465 nomaxvalue nominvalue cache 20;
drop sequence S_sys_menu;
CREATE sequence S_sys_menu INCREMENT BY 1 START WITH 2654 nomaxvalue nominvalue cache 20;
drop sequence S_sys_config;
CREATE sequence S_sys_config INCREMENT BY 1 START WITH 178 nomaxvalue nominvalue cache 20;
drop sequence S_sys_job;
CREATE sequence S_sys_job INCREMENT BY 1 START WITH 23 nomaxvalue nominvalue cache 20;


-- 虹膜人脸1v1日志展示菜单
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2654, '人脸虹膜比对', 2406, 8, 'faceirisMatch', 'tradelog/faceirisMatch/index', 1, 0, 'C', '0', '0', 'tradelog:faceirisMatch:list', '#', 'admin', sysdate, 'admin', sysdate, '人脸虹膜多模态比对日志菜单', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2655, '人脸虹膜比对日志查询', 2654, 1, '#', '', 1, 0, 'F', '0', '0', 'tradelog:faceirisMatch:query', '#', 'admin', sysdate, 'admin', sysdate, '', 'SIDE');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, menu_position) VALUES (2656, '人脸虹膜比对日志导出', 2654, 5, '#', '', 1, 0, 'F', '0', '0', 'tradelog:faceirisMatch:export', '#', 'admin', sysdate, 'admin', sysdate, '', 'SIDE');
drop sequence S_sys_menu;
CREATE sequence S_sys_menu INCREMENT BY 1 START WITH 2657 nomaxvalue nominvalue cache 20;
