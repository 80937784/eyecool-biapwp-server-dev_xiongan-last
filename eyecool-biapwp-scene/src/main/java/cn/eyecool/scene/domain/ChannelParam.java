package cn.eyecool.scene.domain;

import java.util.Date;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonFormat;

import cn.eyecool.common.annotation.Excel;
import cn.eyecool.common.annotation.Excel.Type;
import cn.eyecool.common.core.domain.BaseEntity;

/**
 * 场景参数对象 channel_param
 * 
 * @author admin
 * @date 2021-03-22
 */
public class ChannelParam extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private String id;

    /** 场景主键 */
    private String channelId;

    /** 参数编码 */
    @Excel(name = "channel.param.code")
    private String paramCode;

    /** 参数名称 */
    @Excel(name = "channel.param.name")
    private String paramName;

    /** 参数键值 */
    @Excel(name = "channel.param.value")
    private String paramValue;

    /** 认证类型(数据字典::0-人脸 1指纹 2虹膜 3指静脉) */
    @Excel(name = "channel.param.auth.type", dictType = "channel_bio_attest_type")
    private String bioAttestType;

    /** 定时任务执行时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date batchDate;

    /** 租户ID */
    private String tenantId;

    /** 场景名称 */
    @Excel(name = "channel.param.channelname", type = Type.EXPORT)
    private String channelName;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getChannelId() {
        return channelId;
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

    public void setParamValue(String paramValue) {
        this.paramValue = paramValue;
    }

    public String getParamValue() {
        return paramValue;
    }

    public void setBioAttestType(String bioAttestType) {
        this.bioAttestType = bioAttestType;
    }

    public String getBioAttestType() {
        return bioAttestType;
    }

    public void setBatchDate(Date batchDate) {
        this.batchDate = batchDate;
    }

    public Date getBatchDate() {
        return batchDate;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
