package cn.eyecool.ocr.domain;

import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonFormat;

import cn.eyecool.common.annotation.Excel;
import cn.eyecool.common.core.domain.BaseEntity;

/**
 * 护照OCR 对象 ocr_passport_log
 * 
 * @author admin
 * @date 2020-12-17
 */
public class OcrPassportLog extends BaseEntity {
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

    /** 护照机读码第1行 */
    @Excel(name = "护照机读码第1行")
    private String passportMrzFir;

    /** 护照机读码第2行 */
    @Excel(name = "护照机读码第2行")
    private String passportMrzSec;

    /** 持证人国籍代码 */
    @Excel(name = "持证人国籍代码")
    private String passportNationalityCode;

    /** 护照号码 */
    @Excel(name = "护照号码")
    private String passportNumber;

    /** 出生地点 */
    @Excel(name = "出生地点")
    private String passportBirthPlace;

    /** 签发地点 */
    @Excel(name = "签发地点")
    private String passportIssuePlace;

    /** 签发日期 */
    @Excel(name = "签发日期")
    private String passportIssueDate;

    /** 完整护照机读码射频识别 */
    @Excel(name = "完整护照机读码射频识别")
    private String passportRfidMrz;

    /** 完整护照机读码OCR识别 */
    @Excel(name = "完整护照机读码OCR识别")
    private String passportOcrMrz;

    /** 出生地点拼音 */
    @Excel(name = "出生地点拼音")
    private String passportBirthPlacePinyin;

    /** 签发地点拼音 */
    @Excel(name = "签发地点拼音")
    private String passportIssuePlacePinyin;

    /** 身份证号码 */
    @Excel(name = "身份证号码")
    private String passportIdNumber;

    /** 本国姓名拼音ocr */
    @Excel(name = "本国姓名拼音ocr")
    private String passportOcrNationalName;

    /** 性别ocr */
    @Excel(name = "性别ocr")
    private String passportOcrGender;

    /** 国籍代码ocr */
    @Excel(name = "国籍代码ocr")
    private String passportOcrNationalityCode;

    /** 出生日期ocr */
    @Excel(name = "出生日期ocr")
    private String passportOcrBirthDate;

    /** 有效期至ocr */
    @Excel(name = "有效期至ocr")
    private String passportOcrExpiryDate;

    /** 签发机关ocr */
    @Excel(name = "签发机关ocr")
    private String passportOcrAuthority;

    /** 本国姓 */
    @Excel(name = "本国姓")
    private String passportNationalSurname;

    /** 本国名 */
    @Excel(name = "本国名")
    private String passportNationalGivenName;

    /** 身高 */
    @Excel(name = "身高")
    private String passportHeight;

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

    public void setPassportMrzFir(String passportMrzFir) {
        this.passportMrzFir = passportMrzFir;
    }

    public String getPassportMrzFir() {
        return passportMrzFir;
    }

    public void setPassportMrzSec(String passportMrzSec) {
        this.passportMrzSec = passportMrzSec;
    }

    public String getPassportMrzSec() {
        return passportMrzSec;
    }

    public void setPassportNationalityCode(String passportNationalityCode) {
        this.passportNationalityCode = passportNationalityCode;
    }

    public String getPassportNationalityCode() {
        return passportNationalityCode;
    }

    public void setPassportNumber(String passportNumber) {
        this.passportNumber = passportNumber;
    }

    public String getPassportNumber() {
        return passportNumber;
    }

    public void setPassportBirthPlace(String passportBirthPlace) {
        this.passportBirthPlace = passportBirthPlace;
    }

    public String getPassportBirthPlace() {
        return passportBirthPlace;
    }

    public void setPassportIssuePlace(String passportIssuePlace) {
        this.passportIssuePlace = passportIssuePlace;
    }

    public String getPassportIssuePlace() {
        return passportIssuePlace;
    }

    public void setPassportIssueDate(String passportIssueDate) {
        this.passportIssueDate = passportIssueDate;
    }

    public String getPassportIssueDate() {
        return passportIssueDate;
    }

    public void setPassportRfidMrz(String passportRfidMrz) {
        this.passportRfidMrz = passportRfidMrz;
    }

    public String getPassportRfidMrz() {
        return passportRfidMrz;
    }

    public void setPassportOcrMrz(String passportOcrMrz) {
        this.passportOcrMrz = passportOcrMrz;
    }

    public String getPassportOcrMrz() {
        return passportOcrMrz;
    }

    public void setPassportBirthPlacePinyin(String passportBirthPlacePinyin) {
        this.passportBirthPlacePinyin = passportBirthPlacePinyin;
    }

    public String getPassportBirthPlacePinyin() {
        return passportBirthPlacePinyin;
    }

    public void setPassportIssuePlacePinyin(String passportIssuePlacePinyin) {
        this.passportIssuePlacePinyin = passportIssuePlacePinyin;
    }

    public String getPassportIssuePlacePinyin() {
        return passportIssuePlacePinyin;
    }

    public void setPassportIdNumber(String passportIdNumber) {
        this.passportIdNumber = passportIdNumber;
    }

    public String getPassportIdNumber() {
        return passportIdNumber;
    }

    public void setPassportOcrNationalName(String passportOcrNationalName) {
        this.passportOcrNationalName = passportOcrNationalName;
    }

    public String getPassportOcrNationalName() {
        return passportOcrNationalName;
    }

    public void setPassportOcrGender(String passportOcrGender) {
        this.passportOcrGender = passportOcrGender;
    }

    public String getPassportOcrGender() {
        return passportOcrGender;
    }

    public void setPassportOcrNationalityCode(String passportOcrNationalityCode) {
        this.passportOcrNationalityCode = passportOcrNationalityCode;
    }

    public String getPassportOcrNationalityCode() {
        return passportOcrNationalityCode;
    }

    public void setPassportOcrBirthDate(String passportOcrBirthDate) {
        this.passportOcrBirthDate = passportOcrBirthDate;
    }

    public String getPassportOcrBirthDate() {
        return passportOcrBirthDate;
    }

    public void setPassportOcrExpiryDate(String passportOcrExpiryDate) {
        this.passportOcrExpiryDate = passportOcrExpiryDate;
    }

    public String getPassportOcrExpiryDate() {
        return passportOcrExpiryDate;
    }

    public void setPassportOcrAuthority(String passportOcrAuthority) {
        this.passportOcrAuthority = passportOcrAuthority;
    }

    public String getPassportOcrAuthority() {
        return passportOcrAuthority;
    }

    public void setPassportNationalSurname(String passportNationalSurname) {
        this.passportNationalSurname = passportNationalSurname;
    }

    public String getPassportNationalSurname() {
        return passportNationalSurname;
    }

    public void setPassportNationalGivenName(String passportNationalGivenName) {
        this.passportNationalGivenName = passportNationalGivenName;
    }

    public String getPassportNationalGivenName() {
        return passportNationalGivenName;
    }

    public void setPassportHeight(String passportHeight) {
        this.passportHeight = passportHeight;
    }

    public String getPassportHeight() {
        return passportHeight;
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
            .append("passportMrzFir", getPassportMrzFir()).append("passportMrzSec", getPassportMrzSec())
            .append("passportNationalityCode", getPassportNationalityCode())
            .append("passportNumber", getPassportNumber()).append("passportBirthPlace", getPassportBirthPlace())
            .append("passportIssuePlace", getPassportIssuePlace()).append("passportIssueDate", getPassportIssueDate())
            .append("passportRfidMrz", getPassportRfidMrz()).append("passportOcrMrz", getPassportOcrMrz())
            .append("passportBirthPlacePinyin", getPassportBirthPlacePinyin())
            .append("passportIssuePlacePinyin", getPassportIssuePlacePinyin())
            .append("passportIdNumber", getPassportIdNumber())
            .append("passportOcrNationalName", getPassportOcrNationalName())
            .append("passportOcrGender", getPassportOcrGender())
            .append("passportOcrNationalityCode", getPassportOcrNationalityCode())
            .append("passportOcrBirthDate", getPassportOcrBirthDate())
            .append("passportOcrExpiryDate", getPassportOcrExpiryDate())
            .append("passportOcrAuthority", getPassportOcrAuthority())
            .append("passportNationalSurname", getPassportNationalSurname())
            .append("passportNationalGivenName", getPassportNationalGivenName())
            .append("passportHeight", getPassportHeight()).append("result", getResult())
            .append("tenantId", getTenantId()).toString();
    }
}
