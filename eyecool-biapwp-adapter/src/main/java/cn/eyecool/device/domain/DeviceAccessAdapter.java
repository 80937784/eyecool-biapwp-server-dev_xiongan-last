package cn.eyecool.device.domain;

import com.alibaba.fastjson.JSON;

import cn.eyecool.common.annotation.Excel;
import cn.eyecool.common.core.domain.BaseEntity;

/**
 * 203设备接入对象 device_access_adapter
 * 
 * @author admin
 * @date 2021-04-25
 */
public class DeviceAccessAdapter extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private String id;

    /** 设备名称 */
    @Excel(name = "device.adapter.device.name")
    private String deviceName;

    /** 设备序列号 */
    @Excel(name = "device.adapter.serial.number")
    private String deviceSn;

    /** 同步序列号 */
    @Excel(name = "device.adapter.synchronized.number")
    private String seriaNum;

    /** 租户id */
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

    public void setDeviceSn(String deviceSn) {
        this.deviceSn = deviceSn;
    }

    public String getDeviceSn() {
        return deviceSn;
    }

    public void setSeriaNum(String seriaNum) {
        this.seriaNum = seriaNum;
    }

    public String getSeriaNum() {
        return seriaNum;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantId() {
        return tenantId;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
