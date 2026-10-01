package cn.eyecool.scene.domain;

import java.util.Date;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonFormat;

import cn.eyecool.common.annotation.Excel;
import cn.eyecool.common.core.domain.BaseEntity;

/**
 * 场景信息对象 channel_info
 * 
 * @author admin
 * @date 2021-03-22
 */
public class ChannelInfo extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private String id;

    /** 场景编码 */
    @Excel(name = "channel.info.channelcode")
    private String channelCode;

    /** 场景名称 */
    @Excel(name = "channel.info.channelname")
    private String channelName;

    /** 开通人脸(数据字典:0-不开通 1-开通) */
    @Excel(name = "channel.info.facemode", dictType = "bio_mode_status")
    private String faceMode;

    /** 开通指纹(数据字典:0-不开通 1-开通) */
    @Excel(name = "channel.info.fingermode", dictType = "bio_mode_status")
    private String fingerMode;

    /** 开通虹膜(数据字典:0-不开通 1-开通) */
    @Excel(name = "channel.info.irismode", dictType = "bio_mode_status")
    private String irisMode;

    /** 开通指静脉(数据字典:0-不开通 1-开通) */
    @Excel(name = "channel.info.fveinmode", dictType = "bio_mode_status")
    private String fveinMode;

    /** 开通人脸虹膜多模态(数据字典:0-不开通 1-开通) */
    @Excel(name = "channel.info.faceirismode", dictType = "bio_mode_status")
    private String faceIrisMode;

    /** 是否支持多人脸(数据字典:N-否 Y-是) */
    private String enableMultiFaces;

    /** 1-N识别方式(数据字典:1-校验子场景、2-校验场景库、3-校验全库, 4-依次校验) */
    @Excel(name = "channel.info.search.1n", dictType = "search_n_type")
    private String searchN;

    /** 挂载设备数量上限 */
    @Excel(name = "channel.info.devicesum.limit")
    private Long deviceNumLimit;

    /** 定时任务执行时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date batchDate;

    /** 租户ID */
    private String tenantId;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode;
    }

    public String getChannelCode() {
        return channelCode;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getChannelName() {
        return channelName;
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

    public void setEnableMultiFaces(String enableMultiFaces) {
        this.enableMultiFaces = enableMultiFaces;
    }

    public String getEnableMultiFaces() {
        return enableMultiFaces;
    }

    public void setSearchN(String searchN) {
        this.searchN = searchN;
    }

    public String getSearchN() {
        return searchN;
    }

    public void setDeviceNumLimit(Long deviceNumLimit) {
        this.deviceNumLimit = deviceNumLimit;
    }

    public Long getDeviceNumLimit() {
        return deviceNumLimit;
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

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
