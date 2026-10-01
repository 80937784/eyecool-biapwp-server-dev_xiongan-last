package cn.eyecool.device.domain;

import com.alibaba.fastjson.JSON;

import cn.eyecool.common.annotation.Excel;
import cn.eyecool.common.core.domain.BaseEntity;

/**
 * 参数型号关系对象 device_param_model_rel
 * 
 * @author admin
 * @date 2021-03-31
 */
public class DeviceParamModelRel extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private String id;

    /** 设备型号编码 */
    @Excel(name = "device.model.code")
    private String modelCode;

    /** 设备参数编码 */
    @Excel(name = "device.param.info.code")
    private String paramCode;

    /** 设备参数名称 */
    @Excel(name = "device.param.info.name")
    private String paramName;

    /** 设备参数名称 */
    @Excel(name = "device.param.info.description")
    private String paramDesc;

    /** 租户ID */
    private String tenantId;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setModelCode(String modelCode) {
        this.modelCode = modelCode;
    }

    public String getModelCode() {
        return modelCode;
    }

    public void setParamCode(String paramCode) {
        this.paramCode = paramCode;
    }

    public String getParamCode() {
        return paramCode;
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public String getParamDesc() {
        return paramDesc;
    }

    public void setParamDesc(String paramDesc) {
        this.paramDesc = paramDesc;
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
