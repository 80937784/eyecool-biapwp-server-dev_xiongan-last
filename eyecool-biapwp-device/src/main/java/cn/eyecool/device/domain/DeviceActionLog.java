package cn.eyecool.device.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import cn.eyecool.common.annotation.Excel;
import cn.eyecool.common.core.domain.BaseEntity;

/**
 * 设备动作日志对象 device_action_log
 * 
 * @author admin
 * @date 2021-04-14
 */
public class DeviceActionLog extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private String id;

    /** 设备名称 */
    @Excel(name = "device.name")
    private String deviceName;

    /** 设备编号 */
    @Excel(name = "device.code")
    private String deviceNo;

    /** 设备场景编码 */
    @Excel(name = "device.channel.code")
    private String channelCode;

    /** 设备型号编码 */
    @Excel(name = "device.model.code")
    private String modelCode;

    /** 动作类型 */
    @Excel(name = "device.action.type", dictType = "device_action_type")
    private String actionType;

    /** 租户ID */
    private String tenantId;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceNo(String deviceNo) {
        this.deviceNo = deviceNo;
    }

    public String getDeviceNo() {
        return deviceNo;
    }

    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode;
    }

    public String getChannelCode() {
        return channelCode;
    }

    public void setModelCode(String modelCode) {
        this.modelCode = modelCode;
    }

    public String getModelCode() {
        return modelCode;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getActionType() {
        return actionType;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantId() {
        return tenantId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE).append("id", getId())
            .append("deviceName", getDeviceName()).append("deviceNo", getDeviceNo())
            .append("channelCode", getChannelCode()).append("modelCode", getModelCode())
            .append("actionType", getActionType()).append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime()).append("tenantId", getTenantId()).toString();
    }
}
