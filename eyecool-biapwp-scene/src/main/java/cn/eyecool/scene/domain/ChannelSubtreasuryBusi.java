package cn.eyecool.scene.domain;

import java.util.Date;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonFormat;

import cn.eyecool.common.annotation.Excel;
import cn.eyecool.common.annotation.Excel.Type;
import cn.eyecool.common.core.domain.BaseEntity;

/**
 * 子场景人员对象 channel_subtreasury_busi
 * 
 * @author admin
 * @date 2021-03-22
 */
public class ChannelSubtreasuryBusi extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private String id;

    /** 场景主键 */
    private String channelId;

    /** 子场景主键 */
    @Excel(name = "channel.sub.sceneid", type = Type.EXPORT)
    private String subTreasuryId;

    /** 人员主键 */
    private String personId;

    /** 人员唯一标识 */
    @Excel(name = "channel.sub.uniqueid")
    private String uniqueId;

    /** 数据来源(数据字典 */
    @Excel(name = "channel.sub.datasource", dictType = "apply_data_source", type = Type.EXPORT)
    private String datasource;

    /** 状态(数据字典:0-有效 1:无效) */
    @Excel(name = "channel.sub.status", dictType = "sys_normal_disable", type = Type.EXPORT)
    private String status;

    /** 定时任务执行时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date batchDate;

    /** 租户ID */
    private String tenantId;

    /** 业务更新标识流水码 */
    private Long updateSeriaNum;

    /** 人员姓名 */
    private String personName;

    /** 场景名称 */
    @Excel(name = "channel.sub.channelname", type = Type.EXPORT)
    private String channelName;

    /** 场景编码 */
    private String channelCode;

    /** 子场景名称 */
    @Excel(name = "channel.sub.subchannelname", type = Type.EXPORT)
    private String subTreasuryName;

    /** 子场景编码 */
    private String subTreasuryCode;

    /** 是否是系统管理员 */
    private String deviceMgr;

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

    public void setSubTreasuryId(String subTreasuryId) {
        this.subTreasuryId = subTreasuryId;
    }

    public String getSubTreasuryId() {
        return subTreasuryId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public String getPersonId() {
        return personId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setDatasource(String datasource) {
        this.datasource = datasource;
    }

    public String getDatasource() {
        return datasource;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
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

    public void setUpdateSeriaNum(Long updateSeriaNum) {
        this.updateSeriaNum = updateSeriaNum;
    }

    public Long getUpdateSeriaNum() {
        return updateSeriaNum;
    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getChannelCode() {
        return channelCode;
    }

    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode;
    }

    public String getSubTreasuryName() {
        return subTreasuryName;
    }

    public void setSubTreasuryName(String subTreasuryName) {
        this.subTreasuryName = subTreasuryName;
    }

    public String getSubTreasuryCode() {
        return subTreasuryCode;
    }

    public void setSubTreasuryCode(String subTreasuryCode) {
        this.subTreasuryCode = subTreasuryCode;
    }

    public String getDeviceMgr() {
        return deviceMgr;
    }

    public void setDeviceMgr(String deviceMgr) {
        this.deviceMgr = deviceMgr;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
