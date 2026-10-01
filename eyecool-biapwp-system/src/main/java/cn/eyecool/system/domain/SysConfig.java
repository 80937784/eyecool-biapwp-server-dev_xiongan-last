package cn.eyecool.system.domain;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.alibaba.fastjson.JSON;

import cn.eyecool.common.annotation.Excel;
import cn.eyecool.common.annotation.Excel.ColumnType;
import cn.eyecool.common.annotation.Excel.Type;
import cn.eyecool.common.core.domain.BaseEntity;

/**
 * 参数配置表 sys_config
 * 
 * @author admin
 */
public class SysConfig extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 参数主键 */
    @Excel(name = "sys.config.configid.name", cellType = ColumnType.NUMERIC)
    private Long configId;

    /** 参数名称 */
    @Excel(name = "sys.config.configname.name")
    private String configName;

    /** 参数键名 */
    @Excel(name = "sys.config.configkey.name")
    private String configKey;

    /** 参数键值 */
    @Excel(name = "sys.config.configvalue.name")
    private String configValue;

    /** 系统内置（Y是 N否） */
    @Excel(name = "sys.config.configtype.name", readConverterExp = "Y=sys.config.configtype.y,N=sys.config.configtype.n")
    private String configType;

    /** 租户ID */
    private String tenantId;

    /** 是否允许租户编辑 */
    @Excel(name = "sys.config.tenantmaintain.name", readConverterExp = "Y=sys.config.tenantmaintain.y,N=sys.config.tenantmaintain.n", type = Type.EXPORT)
    private String tenantMaintain;

    public Long getConfigId() {
        return configId;
    }

    public void setConfigId(Long configId) {
        this.configId = configId;
    }

    @NotBlank(message = "sys.config.configname.msg")
    @Size(min = 0, max = 100, message = "sys.config.configname.msg.length")
    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    @NotBlank(message = "sys.config.configkey.msg")
    @Size(min = 0, max = 100, message = "sys.config.configkey.msg.length")
    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    @NotBlank(message = "sys.config.configvalue.msg")
    @Size(min = 0, max = 500, message = "sys.config.configvalue.msg.length")
    public String getConfigValue() {
        return configValue;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }

    public String getConfigType() {
        return configType;
    }

    public void setConfigType(String configType) {
        this.configType = configType;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantMaintain() {
        return tenantMaintain;
    }

    public void setTenantMaintain(String tenantMaintain) {
        this.tenantMaintain = tenantMaintain;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
