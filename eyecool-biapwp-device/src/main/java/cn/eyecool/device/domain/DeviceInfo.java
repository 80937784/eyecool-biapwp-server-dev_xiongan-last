package cn.eyecool.device.domain;

import java.util.Date;

import com.alibaba.fastjson.JSON;

import cn.eyecool.common.annotation.Excel;
import cn.eyecool.common.annotation.Excel.Type;
import cn.eyecool.common.core.domain.BaseEntity;

/**
 * 设备信息对象 device_info
 * 
 * @author admin
 * @date 2021-04-01
 */
public class DeviceInfo extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private String id;

    /** 设备编号 */
    @Excel(name = "device.info.deviceno")
    private String deviceNo;

    /** 设备名称 */
    @Excel(name = "device.name")
    private String deviceName;

    /** 安装地点 */
    @Excel(name = "device.info.address", type = Type.EXPORT)
    private String deviceAddr;

    /** 型号编码 */
    @Excel(name = "device.info.model.code")
    private String deviceModelCode;

    /** 设备IP */
    @Excel(name = "device.info.ip", type = Type.EXPORT)
    private String deviceIp;

    /** 设备Mac */
    @Excel(name = "device.info.mac", type = Type.EXPORT)
    private String deviceMac;

    /** 设备经度 */
    @Excel(name = "device.info.longitude", type = Type.EXPORT)
    private Double longitude;

    /** 设备纬度 */
    @Excel(name = "device.info.latitue", type = Type.EXPORT)
    private Double latitude;

    /** 场景编码 */
    @Excel(name = "device.channel.code", type = Type.EXPORT)
    private String channelCode;

    /** 区域主键 */
    private Long areaId;

    /** 子场景编码(用,分隔) */
    @Excel(name = "device.sub.channel.code", type = Type.EXPORT)
    private String subtreasuryCode;

    /** 设备状态(1:在线，2：离线) */
    private String deviceState;

    /** 导入批次 */
    @Excel(name = "device.info.import.batch", type = Type.EXPORT)
    private String importBatchNum;
    @Excel(name = "device.info.count.server1", type = Type.EXPORT)
    private String countServer1;
    @Excel(name = "device.info.import.server2", type = Type.EXPORT)
    private String countServer2;
    @Excel(name = "device.info.last.id.server1", type = Type.EXPORT)
    private String lastIdServer1;
    @Excel(name = "device.info.last.id.server2", type = Type.EXPORT)
    private String lastIdServer2;

    /** 创建方式(1: 后台添加，2：自主注册) */
    private String createMethod;

    /** 是否进行全量拉取（1：是，0：否） */
    private String pullAllFlag;

    /** 主子场景编码 */
    @Excel(name = "device.primary.sub.code", type = Type.EXPORT)
    private String primarySubCode;

    /** 后端比对去重时长(ms) */
    @Excel(name = "device.info.match.deduplication.time", type = Type.EXPORT)
    private Long duplicateTime;

    /** 设备类型(字典 1:常规 2..) */
    @Excel(name = "device.info.type", type = Type.EXPORT, dictType = "client_device_type")
    private String deviceType;

    /** MQTT连接密码 */
    private String mqttPwd;

    /** MQTT密码盐值 */
    private String mqttSalt;

    /** 扩展信息 */
    @Excel(name = "device.info.extinfo", type = Type.EXPORT)
    private String extInfo;

    /** 定时任务执行时间 */
    private Date batchDate;

    /** 租户ID */
    private String tenantId;

    /** 设备外观图片Base64 */
    private String imageBase64;

    /** 场景名称 */
    private String channelName;

    /** 子场景名称(,分割) */
    private String subtreasuryName;

    /** 主子场景名称 */
    private String primarySubName;

    /** 场景Id */
    private String channelId;

    /** 设备进出方向(IN:进 OUT:出 UNKNOWN:未知) */
    @Excel(name = "device.info.direction", type = Type.EXPORT, dictType = "device_direction")
    private String deviceDirection;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDeviceNo() {
        return deviceNo;
    }

    public void setDeviceNo(String deviceNo) {
        this.deviceNo = deviceNo;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceAddr() {
        return deviceAddr;
    }

    public void setDeviceAddr(String deviceAddr) {
        this.deviceAddr = deviceAddr;
    }

    public String getDeviceModelCode() {
        return deviceModelCode;
    }

    public void setDeviceModelCode(String deviceModelCode) {
        this.deviceModelCode = deviceModelCode;
    }

    public String getDeviceIp() {
        return deviceIp;
    }

    public void setDeviceIp(String deviceIp) {
        this.deviceIp = deviceIp;
    }

    public String getDeviceMac() {
        return deviceMac;
    }

    public void setDeviceMac(String deviceMac) {
        this.deviceMac = deviceMac;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public String getChannelCode() {
        return channelCode;
    }

    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode;
    }

    public Long getAreaId() {
        return areaId;
    }

    public void setAreaId(Long areaId) {
        this.areaId = areaId;
    }

    public String getSubtreasuryCode() {
        return subtreasuryCode;
    }

    public void setSubtreasuryCode(String subtreasuryCode) {
        this.subtreasuryCode = subtreasuryCode;
    }

    public String getDeviceState() {
        return deviceState;
    }

    public void setDeviceState(String deviceState) {
        this.deviceState = deviceState;
    }

    public String getImportBatchNum() {
        return importBatchNum;
    }

    public void setImportBatchNum(String importBatchNum) {
        this.importBatchNum = importBatchNum;
    }

    public String getCreateMethod() {
        return createMethod;
    }

    public void setCreateMethod(String createMethod) {
        this.createMethod = createMethod;
    }

    public String getPullAllFlag() {
        return pullAllFlag;
    }

    public void setPullAllFlag(String pullAllFlag) {
        this.pullAllFlag = pullAllFlag;
    }

    public String getPrimarySubCode() {
        return primarySubCode;
    }

    public void setPrimarySubCode(String primarySubCode) {
        this.primarySubCode = primarySubCode;
    }

    public Long getDuplicateTime() {
        return duplicateTime;
    }

    public void setDuplicateTime(Long duplicateTime) {
        this.duplicateTime = duplicateTime;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getMqttPwd() {
        return mqttPwd;
    }

    public void setMqttPwd(String mqttPwd) {
        this.mqttPwd = mqttPwd;
    }

    public String getMqttSalt() {
        return mqttSalt;
    }

    public void setMqttSalt(String mqttSalt) {
        this.mqttSalt = mqttSalt;
    }

    public String getExtInfo() {
        return extInfo;
    }

    public void setExtInfo(String extInfo) {
        this.extInfo = extInfo;
    }

    public Date getBatchDate() {
        return batchDate;
    }

    public void setBatchDate(Date batchDate) {
        this.batchDate = batchDate;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getSubtreasuryName() {
        return subtreasuryName;
    }

    public void setSubtreasuryName(String subtreasuryName) {
        this.subtreasuryName = subtreasuryName;
    }

    public String getPrimarySubName() {
        return primarySubName;
    }

    public void setPrimarySubName(String primarySubName) {
        this.primarySubName = primarySubName;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getDeviceDirection() {
        return deviceDirection;
    }

    public void setDeviceDirection(String deviceDirection) {
        this.deviceDirection = deviceDirection;
    }

    public String getCountServer1() {
        return countServer1;
    }

    public void setCountServer1(String countServer1) {
        this.countServer1 = countServer1;
    }

    public String getCountServer2() {
        return countServer2;
    }

    public void setCountServer2(String countServer2) {
        this.countServer2 = countServer2;
    }

    public String getLastIdServer1() {
        return lastIdServer1;
    }

    public void setLastIdServer1(String lastIdServer1) {
        this.lastIdServer1 = lastIdServer1;
    }

    public String getLastIdServer2() {
        return lastIdServer2;
    }

    public void setLastIdServer2(String lastIdServer2) {
        this.lastIdServer2 = lastIdServer2;
    }

    /**
     * 子类用于其他功能的调用
     */
    public void afterDataSet() {}

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
