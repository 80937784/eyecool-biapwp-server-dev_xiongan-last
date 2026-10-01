package cn.eyecool.ocr.domain;

import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonFormat;

import cn.eyecool.common.annotation.Excel;
import cn.eyecool.common.core.domain.BaseEntity;

/**
 * 银行卡OCROCR 对象 ocr_bank_card_log
 * 
 * @author admin
 * @date 2020-12-17
 */
public class OcrBankCardLog extends BaseEntity {
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

    /** 有效期 */
    @Excel(name = "有效期")
    private String bankCardExpiryDate;

    /** 银行名称 */
    @Excel(name = "银行名称")
    private String bankCardBankName;

    /** 银行编号 */
    @Excel(name = "银行编号")
    private String bankCardBankCode;

    /** 卡名 */
    @Excel(name = "卡名")
    private String bankCardName;

    /** 卡类型 */
    @Excel(name = "卡类型")
    private String bankCardType;

    /** 卡号 */
    @Excel(name = "卡号")
    private String bankCardNumber;

    /** 识别结果 */
    @Excel(name = "识别结果", readConverterExp = "0=通过,1=未通过")
    private String result;

    /** 租户id */
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

    public void setBankCardExpiryDate(String bankCardExpiryDate) {
        this.bankCardExpiryDate = bankCardExpiryDate;
    }

    public String getBankCardExpiryDate() {
        return bankCardExpiryDate;
    }

    public void setBankCardBankName(String bankCardBankName) {
        this.bankCardBankName = bankCardBankName;
    }

    public String getBankCardBankName() {
        return bankCardBankName;
    }

    public void setBankCardBankCode(String bankCardBankCode) {
        this.bankCardBankCode = bankCardBankCode;
    }

    public String getBankCardBankCode() {
        return bankCardBankCode;
    }

    public void setBankCardName(String bankCardName) {
        this.bankCardName = bankCardName;
    }

    public String getBankCardName() {
        return bankCardName;
    }

    public void setBankCardType(String bankCardType) {
        this.bankCardType = bankCardType;
    }

    public String getBankCardType() {
        return bankCardType;
    }

    public void setBankCardNumber(String bankCardNumber) {
        this.bankCardNumber = bankCardNumber;
    }

    public String getBankCardNumber() {
        return bankCardNumber;
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
            .append("bankCardExpiryDate", getBankCardExpiryDate()).append("bankCardBankName", getBankCardBankName())
            .append("bankCardBankCode", getBankCardBankCode()).append("bankCardName", getBankCardName())
            .append("bankCardType", getBankCardType()).append("bankCardNumber", getBankCardNumber())
            .append("result", getResult()).append("tenantId", getTenantId()).toString();
    }
}
