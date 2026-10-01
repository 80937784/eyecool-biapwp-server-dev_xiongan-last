package cn.eyecool.ocr.domain;

import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonFormat;

import cn.eyecool.common.annotation.Excel;
import cn.eyecool.common.core.domain.BaseEntity;

/**
 * 身份证背面OCR 对象 ocr_idcard_back_log
 * 
 * @author admin
 * @date 2020-12-17
 */
public class OcrIdcardBackLog extends BaseEntity {
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

    /** 签发日期 */
    @Excel(name = "签发日期")
    private String idcardDateIssue;

    /** 签发机关 */
    @Excel(name = "签发机关")
    private String idcardAuthorityIssue;

    /** 有效期 */
    @Excel(name = "有效期")
    private String idcardLimit;

    /** 过期日期 */
    @Excel(name = "过期日期")
    private String idcardExpiryDate;

    /** 算法类型 */
    @Excel(name = "算法类型")
    private String idcardOcrFirmType;

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

    public void setIdcardDateIssue(String idcardDateIssue) {
        this.idcardDateIssue = idcardDateIssue;
    }

    public String getIdcardDateIssue() {
        return idcardDateIssue;
    }

    public void setIdcardAuthorityIssue(String idcardAuthorityIssue) {
        this.idcardAuthorityIssue = idcardAuthorityIssue;
    }

    public String getIdcardAuthorityIssue() {
        return idcardAuthorityIssue;
    }

    public void setIdcardLimit(String idcardLimit) {
        this.idcardLimit = idcardLimit;
    }

    public String getIdcardLimit() {
        return idcardLimit;
    }

    public void setIdcardExpiryDate(String idcardExpiryDate) {
        this.idcardExpiryDate = idcardExpiryDate;
    }

    public String getIdcardExpiryDate() {
        return idcardExpiryDate;
    }

    public void setIdcardOcrFirmType(String idcardOcrFirmType) {
        this.idcardOcrFirmType = idcardOcrFirmType;
    }

    public String getIdcardOcrFirmType() {
        return idcardOcrFirmType;
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
            .append("idcardDateIssue", getIdcardDateIssue()).append("idcardAuthorityIssue", getIdcardAuthorityIssue())
            .append("idcardLimit", getIdcardLimit()).append("idcardExpiryDate", getIdcardExpiryDate())
            .append("idcardOcrFirmType", getIdcardOcrFirmType()).append("result", getResult())
            .append("tenantId", getTenantId()).toString();
    }
}
