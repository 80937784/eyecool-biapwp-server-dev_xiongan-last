package cn.eyecool.scene.domain;

import java.util.Date;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonFormat;

import cn.eyecool.common.annotation.Excel;
import cn.eyecool.common.annotation.Excel.Type;
import cn.eyecool.common.core.domain.BaseEntity;

/**
 * 场景人员对象 channel_business
 * 
 * @author admin
 * @date 2021-03-22
 */
public class ChannelBusiness extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private String id;

    /** 场景主键 */
    private String channelId;

    /** 人员主键 */
    private String personId;

    /** 人员唯一标识 */
    @Excel(name = "channel.business.uniqueid")
    private String uniqueId;

    /** 业务号1 */
    @Excel(name = "channel.business.codefirst")
    private String busiCodeFirst;

    /** 业务号2 */
    @Excel(name = "channel.business.codesecond")
    private String busiCodeSecond;

    /** 业务号3 */
    @Excel(name = "channel.business.codethird")
    private String busiCodeThird;

    /** 开通人脸(数据字典:0-不开通 1-开通) */
    @Excel(name = "channel.business.facemode", dictType = "bio_mode_status", type = Type.EXPORT)
    private String faceMode;

    /** 开通指纹(数据字典:0-不开通 1-开通) */
    @Excel(name = "channel.business.fingermode", dictType = "bio_mode_status", type = Type.EXPORT)
    private String fingerMode;

    /** 开通虹膜(数据字典:0-不开通 1-开通) */
    @Excel(name = "channel.business.irismode", dictType = "bio_mode_status", type = Type.EXPORT)
    private String irisMode;

    /** 开通指静脉(数据字典:0-不开通 1-开通) */
    @Excel(name = "channel.business.fveinmode", dictType = "bio_mode_status", type = Type.EXPORT)
    private String fveinMode;

    /** 开通人脸虹膜多模态(数据字典:0-不开通 1-开通) */
    @Excel(name = "channel.business.faceirismode", dictType = "bio_mode_status", type = Type.EXPORT)
    private String faceIrisMode;

    /** 数据来源(数据字典) */
    @Excel(name = "channel.business.datasource", dictType = "apply_data_source", type = Type.EXPORT)
    private String datasource;

    /** 是否锁定(数据字典:N-否 Y-是) */
    private String locked;

    /** 锁定时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lockTime;

    /** 状态(数据字典:0-有效 1:无效) */
    @Excel(name = "channel.business.status", dictType = "sys_normal_disable", type = Type.EXPORT)
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
    @Excel(name = "channel.business.channelname", type = Type.EXPORT)
    private String channelName;

    /** 场景编码 */
    @Excel(name = "channel.business.channelcode", type = Type.EXPORT)
    private String channelCode;

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

    public void setBusiCodeFirst(String busiCodeFirst) {
        this.busiCodeFirst = busiCodeFirst;
    }

    public String getBusiCodeFirst() {
        return busiCodeFirst;
    }

    public void setBusiCodeSecond(String busiCodeSecond) {
        this.busiCodeSecond = busiCodeSecond;
    }

    public String getBusiCodeSecond() {
        return busiCodeSecond;
    }

    public void setBusiCodeThird(String busiCodeThird) {
        this.busiCodeThird = busiCodeThird;
    }

    public String getBusiCodeThird() {
        return busiCodeThird;
    }

    public void setFaceMode(String faceMode) {
        this.faceMode = faceMode;
    }

    public String getFaceMode() {
        return faceMode;
    }

    public void setFingerMode(String fingerMode) {
        this.fingerMode = fingerMode;
    }

    public String getFingerMode() {
        return fingerMode;
    }

    public void setIrisMode(String irisMode) {
        this.irisMode = irisMode;
    }

    public String getIrisMode() {
        return irisMode;
    }

    public void setFveinMode(String fveinMode) {
        this.fveinMode = fveinMode;
    }

    public String getFveinMode() {
        return fveinMode;
    }

    public void setFaceIrisMode(String faceIrisMode) {
        this.faceIrisMode = faceIrisMode;
    }

    public String getFaceIrisMode() {
        return faceIrisMode;
    }

    public void setDatasource(String datasource) {
        this.datasource = datasource;
    }

    public String getDatasource() {
        return datasource;
    }

    public void setLocked(String locked) {
        this.locked = locked;
    }

    public String getLocked() {
        return locked;
    }

    public void setLockTime(Date lockTime) {
        this.lockTime = lockTime;
    }

    public Date getLockTime() {
        return lockTime;
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

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
