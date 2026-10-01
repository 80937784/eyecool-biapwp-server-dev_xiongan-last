package cn.eyecool.device.domain;

import com.alibaba.fastjson.JSON;

import cn.eyecool.common.annotation.Excel;
import cn.eyecool.common.core.domain.BaseEntity;

/**
 * 参数下发日志对象 device_param_distribute_log
 * 
 * @author admin
 * @date 2021-04-13
 */
public class DeviceParamDistributeLog extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private String id;

    /** 设备编号 */
    @Excel(name = "device.code")
    private String deviceNo;

    /** 参数编码 */
    @Excel(name = "device.param.code")
    private String paramCode;

    /** 参数值 */
    @Excel(name = "device.param.value")
    private String paramValue;

    /** 下发结果(0：成功，1：失败) */
    @Excel(name = "device.param.distribute.result", dictType = "device_config_result")
    private String distributeResult;

    /** 下发排序 */
    @Excel(name = "device.param.distribute.sort")
    private Long sortIndex;

    /** 租户ID */
    private String tenantId;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setDeviceNo(String deviceNo) {
        this.deviceNo = deviceNo;
    }

    public String getDeviceNo() {
        return deviceNo;
    }

    public void setParamCode(String paramCode) {
        this.paramCode = paramCode;
    }

    public String getParamCode() {
        return paramCode;
    }

    public void setParamValue(String paramValue) {
        this.paramValue = paramValue;
    }

    public String getParamValue() {
        return paramValue;
    }

    public void setDistributeResult(String distributeResult) {
        this.distributeResult = distributeResult;
    }

    public String getDistributeResult() {
        return distributeResult;
    }

    public void setSortIndex(Long sortIndex) {
        this.sortIndex = sortIndex;
    }

    public Long getSortIndex() {
        return sortIndex;
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
