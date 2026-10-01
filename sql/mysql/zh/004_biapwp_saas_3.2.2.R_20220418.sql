-- 添加子场景人员设置设备管理员字段
ALTER TABLE channel_subtreasury_busi ADD COLUMN device_mgr char(1) default 'N'  COMMENT '设备管理员(Y:是，N:否)';
-- 添加比对模式字典值
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (466, 6, '多模态中虹膜比对', '5', 'match_mode', NULL, NULL, 'N', '0', 'admin', sysdate(), NULL,sysdate(), NULL);

