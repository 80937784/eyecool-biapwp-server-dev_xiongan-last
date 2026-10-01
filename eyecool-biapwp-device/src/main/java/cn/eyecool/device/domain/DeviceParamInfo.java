package cn.eyecool.device.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import cn.eyecool.common.annotation.Excel;
import cn.eyecool.common.core.domain.BaseEntity;

/**
 * 设备参数信息对象 client_param_info
 * 
 * @author admin
 * @date 2021-03-30
 */
public class DeviceParamInfo extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private String id;

    /** 设备参数编码 */
    @Excel(name = "device.param.info.code")
    private String paramCode;

    /** 设备参数名称 */
    @Excel(name = "device.param.info.name")
    private String paramName;

    /** 设备参数描述 */
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

    public void setParamCode(String paramCode) {
        this.paramCode = paramCode;
    }

    public String getParamCode() {
        return paramCode;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamDesc(String paramDesc) {
        this.paramDesc = paramDesc;
    }

    public String getParamDesc() {
        return paramDesc;
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
            .append("paramCode", getParamCode()).append("paramName", getParamName()).append("paramDesc", getParamDesc())
            .append("createBy", getCreateBy()).append("updateBy", getUpdateBy()).append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime()).append("tenantId", getTenantId()).toString();
    }
}
