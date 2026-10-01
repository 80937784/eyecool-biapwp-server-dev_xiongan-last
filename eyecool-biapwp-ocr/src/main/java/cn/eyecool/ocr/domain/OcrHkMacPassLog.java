package cn.eyecool.ocr.domain;

import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonFormat;

import cn.eyecool.common.annotation.Excel;
import cn.eyecool.common.core.domain.BaseEntity;

/**
 * 港澳通行证OCR 对象 ocr_hk_mac_pass_log
 * 
 * @author admin
 * @date 2020-12-17
 */
public class OcrHkMacPassLog extends BaseEntity {
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

    /** 护照类型 */
    @Excel(name = "护照类型")
    private String hkMacPassType;

    /** 护照号码mrz */
    @Excel(name = "护照号码mrz")
    private String hkMacPassMrzNumber;

    /** 本国姓名 */
    @Excel(name = "本国姓名")
    private String hkMacPassNationalName;

    /** 英文姓名 */
    @Excel(name = "英文姓名")
    private String hkMacPassEnglishName;

    /** 性别 */
    @Excel(name = "性别")
    private String hkMacPassGender;

    /** 出生日期 */
    @Excel(name = "出生日期")
    private String hkMacPassBirth;

    /** 有效日期 */
    @Excel(name = "有效日期")
    private String hkMacPassExpiryDate;

    /** 签发国代码 */
    @Excel(name = "签发国代码")
    private String hkMacPassIssueCountry;

    /** 英文姓 */
    @Excel(name = "英文姓")
    private String hkMacPassEnglishSurName;

    /** 识别结果 */
    @Excel(name = "识别结果", readConverterExp = "0=通过,1=未通过")
    private String result;

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

    public void setHkMacPassType(String hkMacPassType) {
        this.hkMacPassType = hkMacPassType;
    }

    public String getHkMacPassType() {
        return hkMacPassType;
    }

    public void setHkMacPassMrzNumber(String hkMacPassMrzNumber) {
        this.hkMacPassMrzNumber = hkMacPassMrzNumber;
    }

    public String getHkMacPassMrzNumber() {
        return hkMacPassMrzNumber;
    }

    public void setHkMacPassNationalName(String hkMacPassNationalName) {
        this.hkMacPassNationalName = hkMacPassNationalName;
    }

    public String getHkMacPassNationalName() {
        return hkMacPassNationalName;
    }

    public void setHkMacPassEnglishName(String hkMacPassEnglishName) {
        this.hkMacPassEnglishName = hkMacPassEnglishName;
    }

    public String getHkMacPassEnglishName() {
        return hkMacPassEnglishName;
    }

    public void setHkMacPassGender(String hkMacPassGender) {
        this.hkMacPassGender = hkMacPassGender;
    }

    public String getHkMacPassGender() {
        return hkMacPassGender;
    }

    public void setHkMacPassBirth(String hkMacPassBirth) {
        this.hkMacPassBirth = hkMacPassBirth;
    }

    public String getHkMacPassBirth() {
        return hkMacPassBirth;
    }

    public void setHkMacPassExpiryDate(String hkMacPassExpiryDate) {
        this.hkMacPassExpiryDate = hkMacPassExpiryDate;
    }

    public String getHkMacPassExpiryDate() {
        return hkMacPassExpiryDate;
    }

    public void setHkMacPassIssueCountry(String hkMacPassIssueCountry) {
        this.hkMacPassIssueCountry = hkMacPassIssueCountry;
    }

    public String getHkMacPassIssueCountry() {
        return hkMacPassIssueCountry;
    }

    public void setHkMacPassEnglishSurName(String hkMacPassEnglishSurName) {
        this.hkMacPassEnglishSurName = hkMacPassEnglishSurName;
    }

    public String getHkMacPassEnglishSurName() {
        return hkMacPassEnglishSurName;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getResult() {
        return result;
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
            .append("hkMacPassType", getHkMacPassType()).append("hkMacPassMrzNumber", getHkMacPassMrzNumber())
            .append("hkMacPassNationalName", getHkMacPassNationalName())
            .append("hkMacPassEnglishName", getHkMacPassEnglishName()).append("hkMacPassGender", getHkMacPassGender())
            .append("hkMacPassBirth", getHkMacPassBirth()).append("hkMacPassExpiryDate", getHkMacPassExpiryDate())
            .append("hkMacPassIssueCountry", getHkMacPassIssueCountry())
            .append("hkMacPassEnglishSurName", getHkMacPassEnglishSurName()).append("result", getResult())
            .append("tenantId", getTenantId()).toString();
    }
}
