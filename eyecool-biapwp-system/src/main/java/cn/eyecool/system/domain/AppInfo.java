package cn.eyecool.system.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import cn.eyecool.common.annotation.Excel;
import cn.eyecool.common.core.domain.BaseEntity;

/**
 * 应用系统信息对象 app_info
 * 
 * @author admin
 * @date 2021-04-14
 */
public class AppInfo extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private String id;

    /** 应用系统键值 */
    @Excel(name = "AppKey")
    private String appKey;

    /** 应用系统密钥 */
    @Excel(name = "AppSecrect")
    private String appSecret;

    /** 应用系统描述 */
    @Excel(name = "app.info.appdesc.name")
    private String appDesc;

    /** 是否内置 */
    @Excel(name = "app.info.buildin.name", dictType = "sys_yes_no")
    private String builtIn;

    /** 状态 */
    @Excel(name = "app.info.status.name", dictType = "sys_normal_disable")
    private String status;

    /** 租户ID */
    private String tenantId;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }

    public String getAppSecret() {
        return appSecret;
    }

    public void setAppDesc(String appDesc) {
        this.appDesc = appDesc;
    }

    public String getAppDesc() {
        return appDesc;
    }

    public void setBuiltIn(String builtIn) {
        this.builtIn = builtIn;
    }

    public String getBuiltIn() {
        return builtIn;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
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
            .append("appKey", getAppKey()).append("appSecret", getAppSecret()).append("appDesc", getAppDesc())
            .append("builtIn", getBuiltIn()).append("remark", getRemark()).append("status", getStatus())
            .append("createBy", getCreateBy()).append("updateBy", getUpdateBy()).append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime()).append("tenantId", getTenantId()).toString();
    }
}
