package cn.eyecool.tradelog.domain;

import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonFormat;

import cn.eyecool.common.annotation.Excel;
import cn.eyecool.common.annotation.Excel.Type;
import cn.eyecool.common.core.domain.BaseEntity;

/**
 * 人员人脸检活日志对象 person_face_checklive_log
 * 
 * @author admin
 * @date 2021-09-02
 */
public class PersonFaceCheckliveLog extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private String id;

    /** 场景编码 */
    @Excel(name = "channel.info.channelcode")
    private String channelCode;

    /** 现场照路径 */
    private String sceneImage;

    /** 现场视频路径 */
    private String sceneVideo;

    /** 检活分值 */
    @Excel(name = "domain.person.face.checklive.score")
    private Double checkliveScore;

    /** 检活结果信息 */
    @Excel(name = "domain.person.face.checklive.result.msg")
    private String checkliveMsg;

    /** 检活结果(0通过，1未通过) */
    @Excel(name = "domain.person.face.checklive.result", dictType = "bio_result")
    private String checkliveResult;

    /** 检活阈值 */
    @Excel(name = "domain.person.face.checklive.threshold")
    private Double threshold;

    /** 请求时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "domain.person.face.request.time", dateFormat = "yyyy-MM-dd HH:mm:ss", type = Type.EXPORT)
    private Date receivedTime;

    /** 耗时(ms) */
    @Excel(name = "耗时(ms)")
    private Long timeUsed;

    /** 服务器标识 */
    private String serverId;

    /** 算法版本 */
    private String algsVersion;

    /** 厂商 */
    private String vendorCode;

    /** 定时任务执行时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date batchDate;

    /** 租户ID */
    private String tenantId;

    /** 现场照base64 */
    private String sceneImageBase64;

    /** 现场照base64 */
    private String sceneVideoBase64;

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

    public void setSceneImage(String sceneImage) {
        this.sceneImage = sceneImage;
    }

    public String getSceneImage() {
        return sceneImage;
    }

    public void setSceneVideo(String sceneVideo) {
        this.sceneVideo = sceneVideo;
    }

    public String getSceneVideo() {
        return sceneVideo;
    }

    public void setCheckliveScore(Double checkliveScore) {
        this.checkliveScore = checkliveScore;
    }

    public Double getCheckliveScore() {
        return checkliveScore;
    }

    public void setCheckliveMsg(String checkliveMsg) {
        this.checkliveMsg = checkliveMsg;
    }

    public String getCheckliveMsg() {
        return checkliveMsg;
    }

    public void setCheckliveResult(String checkliveResult) {
        this.checkliveResult = checkliveResult;
    }

    public String getCheckliveResult() {
        return checkliveResult;
    }

    public void setThreshold(Double threshold) {
        this.threshold = threshold;
    }

    public Double getThreshold() {
        return threshold;
    }

    public void setReceivedTime(Date receivedTime) {
        this.receivedTime = receivedTime;
    }

    public Date getReceivedTime() {
        return receivedTime;
    }

    public void setTimeUsed(Long timeUsed) {
        this.timeUsed = timeUsed;
    }

    public Long getTimeUsed() {
        return timeUsed;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getServerId() {
        return serverId;
    }

    public void setAlgsVersion(String algsVersion) {
        this.algsVersion = algsVersion;
    }

    public String getAlgsVersion() {
        return algsVersion;
    }

    public void setVendorCode(String vendorCode) {
        this.vendorCode = vendorCode;
    }

    public String getVendorCode() {
        return vendorCode;
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

    public String getSceneImageBase64() {
        return sceneImageBase64;
    }

    public void setSceneImageBase64(String sceneImageBase64) {
        this.sceneImageBase64 = sceneImageBase64;
    }

    public String getSceneVideoBase64() {
        return sceneVideoBase64;
    }

    public void setSceneVideoBase64(String sceneVideoBase64) {
        this.sceneVideoBase64 = sceneVideoBase64;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE).append("id", getId())
            .append("channelCode", getChannelCode()).append("sceneImage", getSceneImage())
            .append("sceneVideo", getSceneVideo()).append("checkliveScore", getCheckliveScore())
            .append("checkliveMsg", getCheckliveMsg()).append("checkliveResult", getCheckliveResult())
            .append("threshold", getThreshold()).append("receivedTime", getReceivedTime())
            .append("timeUsed", getTimeUsed()).append("serverId", getServerId()).append("algsVersion", getAlgsVersion())
            .append("vendorCode", getVendorCode()).append("createTime", getCreateTime())
            .append("batchDate", getBatchDate()).append("tenantId", getTenantId()).toString();
    }
}
