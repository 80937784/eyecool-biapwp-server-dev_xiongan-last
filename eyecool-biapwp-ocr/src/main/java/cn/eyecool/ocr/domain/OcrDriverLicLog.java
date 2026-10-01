package cn.eyecool.ocr.domain;

import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonFormat;

import cn.eyecool.common.annotation.Excel;
import cn.eyecool.common.core.domain.BaseEntity;

/**
 * 驾驶证OCR 对象 ocr_driver_lic_log
 * 
 * @author admin
 * @date 2020-12-17
 */
public class OcrDriverLicLog extends BaseEntity {
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
    @Excel(name = "卡证类型")
    private String typeId;

    /** 算法类型 */
    @Excel(name = "算法类型")
    private String ocrFirmType;

    /** 场景编码 */
    @Excel(name = "场景编码")
    private String channelCode;

    /** 待识别证件照存储路径 */
    @Excel(name = "待识别证件照存储路径")
    private String sceneImageUrl;

    /** 待识别证件照名称 */
    @Excel(name = "待识别证件照名称")
    private String sceneImageName;

    /** 驾驶证件号 */
    @Excel(name = "驾驶证件号")
    private String driverLicNumber;

    /** 证件头像 */
    @Excel(name = "证件头像")
    private String driverLicHeadImage;

    /** 驾驶证居住地址 */
    @Excel(name = "驾驶证居住地址")
    private String driverLicAddress;

    /** 驾驶证出生日期 */
    @Excel(name = "驾驶证出生日期")
    private String driverLicBirth;

    /** 驾驶证性别 */
    @Excel(name = "驾驶证性别")
    private String driverLicGender;

    /** 驾驶证所有人姓名 */
    @Excel(name = "驾驶证所有人姓名")
    private String driverLicName;

    /** 驾驶证准驾车型 */
    @Excel(name = "驾驶证准驾车型")
    private String driverLicDriverType;

    /** 驾驶证初次领证日期 */
    @Excel(name = "驾驶证初次领证日期")
    private String driverLicFirstIssue;

    /** 有效期开始时间 */
    @Excel(name = "有效期开始时间")
    private String driverLicValidFrom;

    /** $column.columnComment */
    @Excel(name = "有效期开始时间")
    private String driverLicValidFor;

    /** 失效日期 */
    @Excel(name = "失效日期")
    private String driverLicExpiryDate;

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

    public void setOcrFirmType(String ocrFirmType) {
        this.ocrFirmType = ocrFirmType;
    }

    public String getOcrFirmType() {
        return ocrFirmType;
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

    public void setDriverLicNumber(String driverLicNumber) {
        this.driverLicNumber = driverLicNumber;
    }

    public String getDriverLicNumber() {
        return driverLicNumber;
    }

    public void setDriverLicHeadImage(String driverLicHeadImage) {
        this.driverLicHeadImage = driverLicHeadImage;
    }

    public String getDriverLicHeadImage() {
        return driverLicHeadImage;
    }

    public void setDriverLicAddress(String driverLicAddress) {
        this.driverLicAddress = driverLicAddress;
    }

    public String getDriverLicAddress() {
        return driverLicAddress;
    }

    public void setDriverLicBirth(String driverLicBirth) {
        this.driverLicBirth = driverLicBirth;
    }

    public String getDriverLicBirth() {
        return driverLicBirth;
    }

    public void setDriverLicGender(String driverLicGender) {
        this.driverLicGender = driverLicGender;
    }

    public String getDriverLicGender() {
        return driverLicGender;
    }

    public void setDriverLicName(String driverLicName) {
        this.driverLicName = driverLicName;
    }

    public String getDriverLicName() {
        return driverLicName;
    }

    public void setDriverLicDriverType(String driverLicDriverType) {
        this.driverLicDriverType = driverLicDriverType;
    }

    public String getDriverLicDriverType() {
        return driverLicDriverType;
    }

    public void setDriverLicFirstIssue(String driverLicFirstIssue) {
        this.driverLicFirstIssue = driverLicFirstIssue;
    }

    public String getDriverLicFirstIssue() {
        return driverLicFirstIssue;
    }

    public void setDriverLicValidFrom(String driverLicValidFrom) {
        this.driverLicValidFrom = driverLicValidFrom;
    }

    public String getDriverLicValidFrom() {
        return driverLicValidFrom;
    }

    public void setDriverLicValidFor(String driverLicValidFor) {
        this.driverLicValidFor = driverLicValidFor;
    }

    public String getDriverLicValidFor() {
        return driverLicValidFor;
    }

    public void setDriverLicExpiryDate(String driverLicExpiryDate) {
        this.driverLicExpiryDate = driverLicExpiryDate;
    }

    public String getDriverLicExpiryDate() {
        return driverLicExpiryDate;
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
            .append("typeId", getTypeId()).append("ocrFirmType", getOcrFirmType())
            .append("channelCode", getChannelCode()).append("sceneImageUrl", getSceneImageUrl())
            .append("sceneImageName", getSceneImageName()).append("driverLicNumber", getDriverLicNumber())
            .append("driverLicHeadImage", getDriverLicHeadImage()).append("driverLicAddress", getDriverLicAddress())
            .append("driverLicBirth", getDriverLicBirth()).append("driverLicGender", getDriverLicGender())
            .append("driverLicName", getDriverLicName()).append("driverLicDriverType", getDriverLicDriverType())
            .append("driverLicFirstIssue", getDriverLicFirstIssue())
            .append("driverLicValidFrom", getDriverLicValidFrom()).append("driverLicValidFor", getDriverLicValidFor())
            .append("driverLicExpiryDate", getDriverLicExpiryDate()).append("result", getResult())
            .append("tenantId", getTenantId()).toString();
    }
}
