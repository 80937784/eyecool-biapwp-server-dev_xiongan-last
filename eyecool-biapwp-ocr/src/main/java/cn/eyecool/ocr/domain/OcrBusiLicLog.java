package cn.eyecool.ocr.domain;

import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonFormat;

import cn.eyecool.common.annotation.Excel;
import cn.eyecool.common.core.domain.BaseEntity;

/**
 * 营业执照OCR 对象 ocr_business_lic_log
 * 
 * @author admin
 * @date 2020-12-17
 */
public class OcrBusiLicLog extends BaseEntity {
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

    /** 注册号 */
    @Excel(name = "注册号")
    private String busiLicRegisteredNo;

    /** 营业执照签发号 */
    @Excel(name = "营业执照签发号")
    private String busiLicOriginationNo;

    /** 营业执照税号 */
    @Excel(name = "营业执照税号")
    private String busiLicTaxNo;

    /** 营业执照社会保险证号 */
    @Excel(name = "营业执照社会保险证号")
    private String busiLicSocialInsuranceNo;

    /** 营业执照统计号 */
    @Excel(name = "营业执照统计号")
    private String busiLicStatisticNo;

    /** 公司名称 */
    @Excel(name = "公司名称")
    private String busiLicName;

    /** 公司类型 */
    @Excel(name = "公司类型")
    private String busiLicType;

    /** 公司地址 */
    @Excel(name = "公司地址")
    private String busiLicAddress;

    /** 法定代表人 */
    @Excel(name = "法定代表人")
    private String busiLicOwner;

    /** 组成形式 */
    @Excel(name = "组成形式")
    private String busiLicForm;

    /** 注册资金 */
    @Excel(name = "注册资金")
    private String busiLicRegisteredCapital;

    /** 注册日期 */
    @Excel(name = "注册日期")
    private String busiLicRegistryDate;

    /** 过期日期 */
    @Excel(name = "过期日期")
    private String busiLicExpiryDate;

    /** 经营范围 */
    @Excel(name = "经营范围")
    private String busiLicScope;

    /** 签发机关 */
    @Excel(name = "签发机关")
    private String busiLicIssureAuthority;

    /** 签发日期 */
    @Excel(name = "签发日期")
    private String busiLicIssureDate;

    /** 营业执照二维码 */
    @Excel(name = "营业执照二维码")
    private String busiLicQrCode;

    /** 识别结果 */
    @Excel(name = "识别结果", readConverterExp = "0=通过,1=未通过")
    private String result;

    /** $column.columnComment */
    private String tenantId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReceivedSeq() {
        return receivedSeq;
    }

    public void setReceivedSeq(String receivedSeq) {
        this.receivedSeq = receivedSeq;
    }

    public Date getReceivedTime() {
        return receivedTime;
    }

    public void setReceivedTime(Date receivedTime) {
        this.receivedTime = receivedTime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    public String getChannelCode() {
        return channelCode;
    }

    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode;
    }

    public String getSceneImageUrl() {
        return sceneImageUrl;
    }

    public void setSceneImageUrl(String sceneImageUrl) {
        this.sceneImageUrl = sceneImageUrl;
    }

    public String getSceneImageName() {
        return sceneImageName;
    }

    public void setSceneImageName(String sceneImageName) {
        this.sceneImageName = sceneImageName;
    }

    public String getBusiLicRegisteredNo() {
        return busiLicRegisteredNo;
    }

    public void setBusiLicRegisteredNo(String busiLicRegisteredNo) {
        this.busiLicRegisteredNo = busiLicRegisteredNo;
    }

    public String getBusiLicOriginationNo() {
        return busiLicOriginationNo;
    }

    public void setBusiLicOriginationNo(String busiLicOriginationNo) {
        this.busiLicOriginationNo = busiLicOriginationNo;
    }

    public String getBusiLicTaxNo() {
        return busiLicTaxNo;
    }

    public void setBusiLicTaxNo(String busiLicTaxNo) {
        this.busiLicTaxNo = busiLicTaxNo;
    }

    public String getBusiLicSocialInsuranceNo() {
        return busiLicSocialInsuranceNo;
    }

    public void setBusiLicSocialInsuranceNo(String busiLicSocialInsuranceNo) {
        this.busiLicSocialInsuranceNo = busiLicSocialInsuranceNo;
    }

    public String getBusiLicStatisticNo() {
        return busiLicStatisticNo;
    }

    public void setBusiLicStatisticNo(String busiLicStatisticNo) {
        this.busiLicStatisticNo = busiLicStatisticNo;
    }

    public String getBusiLicName() {
        return busiLicName;
    }

    public void setBusiLicName(String busiLicName) {
        this.busiLicName = busiLicName;
    }

    public String getBusiLicType() {
        return busiLicType;
    }

    public void setBusiLicType(String busiLicType) {
        this.busiLicType = busiLicType;
    }

    public String getBusiLicAddress() {
        return busiLicAddress;
    }

    public void setBusiLicAddress(String busiLicAddress) {
        this.busiLicAddress = busiLicAddress;
    }

    public String getBusiLicOwner() {
        return busiLicOwner;
    }

    public void setBusiLicOwner(String busiLicOwner) {
        this.busiLicOwner = busiLicOwner;
    }

    public String getBusiLicForm() {
        return busiLicForm;
    }

    public void setBusiLicForm(String busiLicForm) {
        this.busiLicForm = busiLicForm;
    }

    public String getBusiLicRegisteredCapital() {
        return busiLicRegisteredCapital;
    }

    public void setBusiLicRegisteredCapital(String busiLicRegisteredCapital) {
        this.busiLicRegisteredCapital = busiLicRegisteredCapital;
    }

    public String getBusiLicRegistryDate() {
        return busiLicRegistryDate;
    }

    public void setBusiLicRegistryDate(String busiLicRegistryDate) {
        this.busiLicRegistryDate = busiLicRegistryDate;
    }

    public String getBusiLicExpiryDate() {
        return busiLicExpiryDate;
    }

    public void setBusiLicExpiryDate(String busiLicExpiryDate) {
        this.busiLicExpiryDate = busiLicExpiryDate;
    }

    public String getBusiLicScope() {
        return busiLicScope;
    }

    public void setBusiLicScope(String busiLicScope) {
        this.busiLicScope = busiLicScope;
    }

    public String getBusiLicIssureAuthority() {
        return busiLicIssureAuthority;
    }

    public void setBusiLicIssureAuthority(String busiLicIssureAuthority) {
        this.busiLicIssureAuthority = busiLicIssureAuthority;
    }

    public String getBusiLicIssureDate() {
        return busiLicIssureDate;
    }

    public void setBusiLicIssureDate(String busiLicIssureDate) {
        this.busiLicIssureDate = busiLicIssureDate;
    }

    public String getBusiLicQrCode() {
        return busiLicQrCode;
    }

    public void setBusiLicQrCode(String busiLicQrCode) {
        this.busiLicQrCode = busiLicQrCode;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE).append("id", getId())
            .append("receivedSeq", getReceivedSeq()).append("receivedTime", getReceivedTime()).append("type", getType())
            .append("typeId", getTypeId()).append("channelCode", getChannelCode())
            .append("sceneImageUrl", getSceneImageUrl()).append("sceneImageName", getSceneImageName())
            .append("busiLicRegisteredNo", getBusiLicRegisteredNo())
            .append("busiLicOriginationNo", getBusiLicOriginationNo()).append("busiLicTaxNo", getBusiLicTaxNo())
            .append("busiLicSocialInsuranceNo", getBusiLicSocialInsuranceNo())
            .append("busiLicStatisticNo", getBusiLicStatisticNo()).append("busiLicName", getBusiLicName())
            .append("busiLicType", getBusiLicType()).append("busiLicAddress", getBusiLicAddress())
            .append("busiLicOwner", getBusiLicOwner()).append("busiLicForm", getBusiLicForm())
            .append("busiLicRegisteredCapital", getBusiLicRegisteredCapital())
            .append("busiLicRegistryDate", getBusiLicRegistryDate()).append("busiLicExpiryDate", getBusiLicExpiryDate())
            .append("busiLicScope", getBusiLicScope()).append("busiLicIssureAuthority", getBusiLicIssureAuthority())
            .append("busiLicIssureDate", getBusiLicIssureDate()).append("busiLicQrCode", getBusiLicQrCode())
            .append("result", getResult()).append("tenantId", getTenantId()).toString();
    }
}
