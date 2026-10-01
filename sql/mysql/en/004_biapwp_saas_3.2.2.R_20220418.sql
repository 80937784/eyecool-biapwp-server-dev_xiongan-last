-- Add setting sub scene personnel as device manager field
ALTER TABLE channel_subtreasury_busi ADD COLUMN device_mgr char(1) default 'N'  COMMENT 'device manager(Y: yes, N: No)';
-- Add comparison mode dictionary value
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (466, 6, 'Iris comparison in multi-modal', '5', 'match_mode', NULL, NULL, 'N', '0', 'admin', sysdate(),null, sysdate(), NULL);

