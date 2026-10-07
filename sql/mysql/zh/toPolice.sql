
-- 推送公安参数
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain)
VALUES (191, '雄安推送公安url', 'xa.police.url', 'http://10.245.64.69:7339', 'N', 'admin', sysdate(), 'admin', sysdate(), NULL, 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain)
VALUES (192, '雄安推送公安deviceId', 'xa.police.device.id', '13310001111206232323', 'N', 'admin', sysdate(), 'admin', sysdate(), NULL, 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain)
VALUES (193, '雄安推送公安图片消息url', 'xa.police.data.image.url', 'http://10.245.64.69:14642/VIID/SubscribeNotifications', 'N', 'admin', sysdate(), 'admin', sysdate(), NULL, 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain)
VALUES (194, '雄安推送公安图片消息subscribeId', 'xa.police.data.sub.subscribeId', '130000000000032026093017451713794', 'N', 'admin', sysdate(), 'admin', sysdate(), NULL, 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain)
VALUES (195, '雄安推送公安图片消息userIdentify', 'xa.police.data.sub.userIdentify', '13310016795037860001', 'N', 'admin', sysdate(), 'admin', sysdate(), NULL, 'N');

INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain)
VALUES (196, '雄安推送公安设备点位消息subscribeId', 'xa.police.device.sub.subscribeId', '130000000000032026092923210913282', 'N', 'admin', sysdate(), 'admin', sysdate(), NULL, 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain)
VALUES (197, '雄安推送公安设备点位消息userIdentify', 'xa.police.device.sub.userIdentify', '13310016795037860001', 'N', 'admin', sysdate(), 'admin', sysdate(), NULL, 'N');


ALTER TABLE person_face_search_log ADD to_police char(1) default 'N' COMMENT '是否推送公安';


ALTER TABLE device_info
ADD COLUMN count_server1 VARCHAR(64) DEFAULT NULL COMMENT '服务器1计数',
ADD COLUMN count_server2 VARCHAR(64) DEFAULT NULL COMMENT '服务器2计数',
ADD COLUMN last_id_server1 VARCHAR(64) DEFAULT NULL COMMENT '服务器1最后ID',
ADD COLUMN last_id_server2 VARCHAR(64) DEFAULT NULL COMMENT '服务器2最后ID';

