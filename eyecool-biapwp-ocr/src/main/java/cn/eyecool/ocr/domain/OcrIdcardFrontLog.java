package cn.eyecool.ocr.domain;

import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonFormat;

import cn.eyecool.common.annotation.Excel;
import cn.eyecool.common.core.domain.BaseEntity;

/**
 * 身份证正面OCR 对象 ocr_idcard_front_log
 * 
 * @author admin
 * @date 2020-12-17
 */
public class OcrIdcardFrontLog extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private String id;

    /** 业务流水号 */
    @Excel(name = "业务流水号")
    private String receivedSeq;

    /** 请求时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "请求时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date receivedTime;

    /** 图片类型 */
    @Excel(name = "图片类型", defaultValue = "Unknown")
    private String type;

    /** 卡证类型 */
    @Excel(name = "卡证类型", defaultValue = "0")
    private String typeId;

    /** 场景编码 */
    @Excel(name = "场景编码")
    private String channelCode;

    /** 待识别证件照存储路径 */
    @Excel(name = "待识别证件照存储路径")
    private String sceneImageUrl;

    /** 待识别证件照名称 */
    @Excel(name = "待识别证件照名称")
    private String sceneImageName;

    /** 身份证件号 */
    @Excel(name = "身份证件号")
    private String idcardNumber;

    /** 证件头像 */
    @Excel(name = "证件头像")
    private String idcardHeadimage;

    /** 民族 */
    @Excel(name = "民族")
    private String idcardEthnicity;

    /** 住址 */
    @Excel(name = "住址")
    private String idcardAddress;

    /** 出生日期 */
    @Excel(name = "出生日期")
    private String idcardBirth;

    /** 性别 */
    @Excel(name = "性别")
    private String idcardGender;

    /** 姓名 */
    @Excel(name = "姓名")
    private String idcardName;

    /** 识别结果 */
    @Excel(name = "识别结果", readConverterExp = "0=通过,1=未通过")
    private String result;

    /** 算法类型 */
    @Excel(name = "算法类型")
    private String idcardOcrFirmType;

    /** $column.columnComment */
    private String tenantId;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setReceivedSeq(String receivedSeq) {
        this.receivedSeq = receivedSeq;
    }

    public String getReceivedSeq() {
        return receivedSeq;
    }

    public void setReceivedTime(Date receivedTime) {
        this.receivedTime = receivedTime;
    }

    public Date getReceivedTime() {
        return receivedTime;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    public String getTypeId() {
        return typeId;
    }

    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode;
    }

    public String getChannelCode() {
        return channelCode;
    }

    public void setSceneImageUrl(String sceneImageUrl) {
        this.sceneImageUrl = sceneImageUrl;
    }

    public String getSceneImageUrl() {
        return sceneImageUrl;
    }

    public void setSceneImageName(String sceneImageName) {
        this.sceneImageName = sceneImageName;
    }

    public String getSceneImageName() {
        return sceneImageName;
    }

    public void setIdcardNumber(String idcardNumber) {
        this.idcardNumber = idcardNumber;
    }

    public String getIdcardNumber() {
        return idcardNumber;
    }

    public void setIdcardHeadimage(String idcardHeadimage) {
        this.idcardHeadimage = idcardHeadimage;
    }

    public String getIdcardHeadimage() {
        return idcardHeadimage;
    }

    public void setIdcardEthnicity(String idcardEthnicity) {
        this.idcardEthnicity = idcardEthnicity;
    }

    public String getIdcardEthnicity() {
        return idcardEthnicity;
    }

    public void setIdcardAddress(String idcardAddress) {
        this.idcardAddress = idcardAddress;
    }

    public String getIdcardAddress() {
        return idcardAddress;
    }

    public void setIdcardBirth(String idcardBirth) {
        this.idcardBirth = idcardBirth;
    }

    public String getIdcardBirth() {
        return idcardBirth;
    }

    public void setIdcardGender(String idcardGender) {
        this.idcardGender = idcardGender;
    }

    public String getIdcardGender() {
        return idcardGender;
    }

    public void setIdcardName(String idcardName) {
        this.idcardName = idcardName;
    }

    public String getIdcardName() {
        return idcardName;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getResult() {
        return result;
    }

    public void setIdcardOcrFirmType(String idcardOcrFirmType) {
        this.idcardOcrFirmType = idcardOcrFirmType;
    }

    public String getIdcardOcrFirmType() {
        return idcardOcrFirmType;
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
            .append("receivedSeq", getReceivedSeq()).append("receivedTime", getReceivedTime()).append("type", getType())
            .append("typeId", getTypeId()).append("channelCode", getChannelCode())
            .append("sceneImageUrl", getSceneImageUrl()).append("sceneImageName", getSceneImageName())
            .append("idcardNumber", getIdcardNumber()).append("idcardHeadimage", getIdcardHeadimage())
            .append("idcardEthnicity", getIdcardEthnicity()).append("idcardAddress", getIdcardAddress())
            .append("idcardBirth", getIdcardBirth()).append("idcardGender", getIdcardGender())
            .append("idcardName", getIdcardName()).append("result", getResult())
            .append("idcardOcrFirmType", getIdcardOcrFirmType()).append("tenantId", getTenantId()).toString();
    }
}
