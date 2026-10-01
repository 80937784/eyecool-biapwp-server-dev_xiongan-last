
-- 雄安管委会
-- 添加卡状态字段
alter table base_person_info add column card_status char(1) default '1' comment '卡片状态[1-开通，2-注销，3-挂失，4-解挂]';

-- 一卡通数据同步参数
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain)
VALUES (178, '雄安一卡通系统名称', 'xa.cardpass.sysname', '园区平台系统', 'N', 'admin', sysdate(), 'admin', sysdate(), NULL, 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain)
VALUES (179, '雄安一卡通appId', 'xa.cardpass.appid', '133100018660002', 'N', 'admin', sysdate(), 'admin', sysdate(), NULL, 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain)
VALUES (180, '雄安一卡通appSecret', 'xa.cardpass.appsecret', 'a83510bc600b15ed8eb1b083fe5ef866', 'N', 'admin', sysdate(), 'admin', sysdate(), NULL, 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain)
VALUES (181, '雄安一卡通tokenUrl', 'xa.cardpass.token.url', 'http://121.43.160.85:6062/try/access/service/initCtrl/getToken', 'N', 'admin', sysdate(), 'admin', sysdate(), NULL, 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain)
VALUES (182, '雄安一卡通记录回传url', 'xa.cardpass.record.url', 'http://121.43.160.85:6062/try/access/service/accessCtrl', 'N', 'admin', sysdate(), 'admin', sysdate(), NULL, 'N');
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain)
VALUES (183, '雄安一卡通areacode', 'xa.cardpass.areacode', '1', 'N', 'admin', sysdate(), 'admin', sysdate(), NULL, 'N');

-- 权限回传url
INSERT INTO sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, tenant_maintain)
VALUES (190, '雄安一卡通权限回传url', 'xa.authority.status.url', 'http://121.43.160.85:6062/try/access/service/', 'N', 'admin', sysdate(), 'admin', sysdate(), NULL, 'N');
