package cn.eyecool.ocr.domain;

import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonFormat;

import cn.eyecool.common.annotation.Excel;
import cn.eyecool.common.core.domain.BaseEntity;

/**
 * 行驶证OCR 对象 ocr_driving_lic_log
 * 
 * @author admin
 * @date 2020-12-17
 */
public class OcrDrivingLicLog extends BaseEntity {
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

    /** 品牌型号 */
    @Excel(name = "品牌型号")
    private String drivingLicModel;

    /** 住址 */
    @Excel(name = "住址")
    private String drivingLicAddress;

    /** 车牌号 */
    @Excel(name = "车牌号")
    private String drivingLicCarNumber;

    /** 车辆类型 */
    @Excel(name = "车辆类型")
    private String drivingLicVehicleType;

    /** 发行日期 */
    @Excel(name = "发行日期")
    private String drivingLicIssueDate;

    /** 发动机号码 */
    @Excel(name = "发动机号码")
    private String drivingLicEngineNo;

    /** 车辆识别号码 */
    @Excel(name = "车辆识别号码")
    private String drivingLicVin;

    /** 注册日期 */
    @Excel(name = "注册日期")
    private String drivingLicRegisterDate;

    /** 用途 */
    @Excel(name = "用途")
    private String drivingLicUseType;

    /** 所有人 */
    @Excel(name = "所有人")
    private String drivingLicOwner;

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

    public void setDrivingLicModel(String drivingLicModel) {
        this.drivingLicModel = drivingLicModel;
    }

    public String getDrivingLicModel() {
        return drivingLicModel;
    }

    public void setDrivingLicAddress(String drivingLicAddress) {
        this.drivingLicAddress = drivingLicAddress;
    }

    public String getDrivingLicAddress() {
        return drivingLicAddress;
    }

    public void setDrivingLicCarNumber(String drivingLicCarNumber) {
        this.drivingLicCarNumber = drivingLicCarNumber;
    }

    public String getDrivingLicCarNumber() {
        return drivingLicCarNumber;
    }

    public void setDrivingLicVehicleType(String drivingLicVehicleType) {
        this.drivingLicVehicleType = drivingLicVehicleType;
    }

    public String getDrivingLicVehicleType() {
        return drivingLicVehicleType;
    }

    public void setDrivingLicIssueDate(String drivingLicIssueDate) {
        this.drivingLicIssueDate = drivingLicIssueDate;
    }

    public String getDrivingLicIssueDate() {
        return drivingLicIssueDate;
    }

    public void setDrivingLicEngineNo(String drivingLicEngineNo) {
        this.drivingLicEngineNo = drivingLicEngineNo;
    }

    public String getDrivingLicEngineNo() {
        return drivingLicEngineNo;
    }

    public void setDrivingLicVin(String drivingLicVin) {
        this.drivingLicVin = drivingLicVin;
    }

    public String getDrivingLicVin() {
        return drivingLicVin;
    }

    public void setDrivingLicRegisterDate(String drivingLicRegisterDate) {
        this.drivingLicRegisterDate = drivingLicRegisterDate;
    }

    public String getDrivingLicRegisterDate() {
        return drivingLicRegisterDate;
    }

    public void setDrivingLicUseType(String drivingLicUseType) {
        this.drivingLicUseType = drivingLicUseType;
    }

    public String getDrivingLicUseType() {
        return drivingLicUseType;
    }

    public void setDrivingLicOwner(String drivingLicOwner) {
        this.drivingLicOwner = drivingLicOwner;
    }

    public String getDrivingLicOwner() {
        return drivingLicOwner;
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
            .append("drivingLicModel", getDrivingLicModel()).append("drivingLicAddress", getDrivingLicAddress())
            .append("drivingLicCarNumber", getDrivingLicCarNumber())
            .append("drivingLicVehicleType", getDrivingLicVehicleType())
            .append("drivingLicIssueDate", getDrivingLicIssueDate())
            .append("drivingLicEngineNo", getDrivingLicEngineNo()).append("drivingLicVin", getDrivingLicVin())
            .append("drivingLicRegisterDate", getDrivingLicRegisterDate())
            .append("drivingLicUseType", getDrivingLicUseType()).append("drivingLicOwner", getDrivingLicOwner())
            .append("result", getResult()).append("tenantId", getTenantId()).toString();
    }
}
